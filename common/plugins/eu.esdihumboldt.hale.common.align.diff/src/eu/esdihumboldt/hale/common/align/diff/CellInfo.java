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

package eu.esdihumboldt.hale.common.align.diff;

/**
 * Basic cell information available in a diff.
 * 
 * @author Simon Templer
 */
public interface CellInfo {

	/**
	 * @return the identifier of the cell
	 */
	String getIdentifier();

	/**
	 * @return a human readable cell label
	 */
	String getLabel();

}
