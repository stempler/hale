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

import eu.esdihumboldt.hale.common.align.diff.CellInfo;
import eu.esdihumboldt.hale.common.align.model.Cell;
import eu.esdihumboldt.hale.common.align.model.CellUtil;

/**
 * TODO Type description
 * 
 * @author simon
 */
public class CellInfoImpl implements CellInfo {

	private final Cell cell;

	/**
	 * @param cell
	 */
	public CellInfoImpl(Cell cell) {
		super();
		this.cell = cell;
	}

	/**
	 * @see eu.esdihumboldt.hale.common.align.diff.CellInfo#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return cell.getId();
	}

	/**
	 * @see eu.esdihumboldt.hale.common.align.diff.CellInfo#getLabel()
	 */
	@Override
	public String getLabel() {
		return CellUtil.getCellDescription(cell, null);
	}

}
