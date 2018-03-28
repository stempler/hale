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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import eu.esdihumboldt.hale.common.align.diff.AlignmentDiff;
import eu.esdihumboldt.hale.common.align.diff.CellChange;
import eu.esdihumboldt.hale.common.align.diff.CellInfo;

/**
 * TODO Type description
 * 
 * @author Simon Templer
 * 
 * @param <I> the cell information type
 * @param <C> the cell change type
 */
public class AlignmentDiffImpl<I extends CellInfo, C extends CellChange>
		implements AlignmentDiff<I, C> {

	private final Collection<I> cellsAdded;
	private final Collection<I> cellsRemoved;
	private final Collection<C> cellsChanged;

	/**
	 * @param cellAdded
	 * @param cellRemoved
	 * @param cellsChanged
	 */
	public AlignmentDiffImpl(Collection<I> cellsAdded, Collection<I> cellsRemoved,
			Collection<C> cellsChanged) {
		super();
		this.cellsAdded = Collections.unmodifiableCollection(new ArrayList<>(cellsAdded));
		this.cellsRemoved = Collections.unmodifiableCollection(new ArrayList<>(cellsRemoved));
		this.cellsChanged = Collections.unmodifiableCollection(new ArrayList<>(cellsChanged));
	}

	@Override
	public Collection<I> getCellsAdded() {
		return cellsAdded;
	}

	@Override
	public Collection<I> getCellsRemoved() {
		return cellsRemoved;
	}

	@Override
	public Collection<C> getCellsChanged() {
		return cellsChanged;
	}

}
