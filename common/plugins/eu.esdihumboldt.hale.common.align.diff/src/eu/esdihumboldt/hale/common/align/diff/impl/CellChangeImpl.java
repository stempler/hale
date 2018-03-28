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

import eu.esdihumboldt.hale.common.align.diff.CellChange;
import eu.esdihumboldt.hale.common.align.diff.CellInfo;

/**
 * TODO Type description
 * 
 * @author simon
 */
public class CellChangeImpl implements CellChange {

	private final CellInfo current;

	/**
	 * @param current
	 */
	public CellChangeImpl(CellInfo current) {
		super();
		this.current = current;
	}

	@Override
	public String getIdentifier() {
		return current.getIdentifier();
	}

	@Override
	public String getLabel() {
		return current.getLabel();
	}

}
