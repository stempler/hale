/*
 * Copyright (c) 2018 wetransform GmbH
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     wetransform GmbH <http://www.wetransform.to>
 */

package eu.esdihumboldt.hale.common.align.diff.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.w3c.dom.Element;

import eu.esdihumboldt.hale.common.align.diff.AlignmentDiff;
import eu.esdihumboldt.hale.common.align.diff.CellChange;
import eu.esdihumboldt.hale.common.align.diff.CellInfo;
import eu.esdihumboldt.hale.common.align.diff.DiffAlgorithm;
import eu.esdihumboldt.hale.common.align.io.impl.JaxbAlignmentIO;
import eu.esdihumboldt.hale.common.align.model.Alignment;
import eu.esdihumboldt.hale.common.align.model.Cell;
import eu.esdihumboldt.hale.common.align.model.MutableCell;
import eu.esdihumboldt.util.Pair;

/**
 * Simple diff algorithm only can detect changes in a cell if the ID is stable.
 * 
 * @author Simon Templer
 */
public class SimpleDiff implements DiffAlgorithm {

	@Override
	public AlignmentDiff calculateDiff(Alignment original, Alignment update) throws Exception {

		// organise cells by ID

		Map<String, Cell> orgCells = original.getCells().stream()
				.collect(Collectors.toMap(cell -> cell.getId(), cell -> cell));
		Map<String, Cell> updCells = update.getCells().stream()
				.collect(Collectors.toMap(cell -> cell.getId(), cell -> cell));

		// collect removed cells
		List<Cell> removed = original.getCells().stream()
				.filter(cell -> !updCells.containsKey(cell.getId())).collect(Collectors.toList());

		// collect added cells
		List<Cell> added = update.getCells().stream()
				.filter(cell -> !orgCells.containsKey(cell.getId())).collect(Collectors.toList());

		// XXX algorithm extension - detect matches in added/removed that are
		// really changes?

		// collect potentially changed cells
		Map<Cell, Cell> maybeChanges = original.getCells().stream().map(cell -> {
			Cell upd = updCells.get(cell.getId());
			if (upd != null) {
				return new Pair<Cell, Cell>(cell, upd);
			}
			else {
				return null;
			}
		}).filter(pair -> pair != null)
				.collect(Collectors.toMap(pair -> pair.getFirst(), pair -> pair.getSecond()));

		// compare cells
		Map<Cell, Cell> changes = new HashMap<>();
		for (Entry<Cell, Cell> candidates : maybeChanges.entrySet()) {
			Cell cell1 = candidates.getKey();
			Cell cell2 = candidates.getValue();

			if (cell1 instanceof MutableCell && cell2 instanceof MutableCell) {
				// compare using XML representation
				Element xml1 = JaxbAlignmentIO.toDom((MutableCell) cell1);
				Element xml2 = JaxbAlignmentIO.toDom((MutableCell) cell2);

				boolean equal = xml1.isEqualNode(xml2);
				if (!equal) {
					changes.put(cell1, cell2);
					// XXX calculate some form of diff? e.g. via XML diff?
					// https://stackoverflow.com/questions/141993/best-way-to-compare-2-xml-documents-in-java
				}
			}
			else {
				// FIXME support other cases? what about modifiers?!
			}
		}

		// FIXME
		List<CellInfo> cellsAdded = added.stream().map(cell -> new CellInfoImpl(cell))
				.collect(Collectors.toList());
		List<CellInfo> cellsRemoved = removed.stream().map(cell -> new CellInfoImpl(cell))
				.collect(Collectors.toList());
		List<CellChange> cellsChanged = changes.entrySet().stream()
				.map(entry -> new CellChangeImpl(new CellInfoImpl(entry.getValue())))
				.collect(Collectors.toList());
		return new AlignmentDiffImpl<>(cellsAdded, cellsRemoved, cellsChanged);
	}

}
