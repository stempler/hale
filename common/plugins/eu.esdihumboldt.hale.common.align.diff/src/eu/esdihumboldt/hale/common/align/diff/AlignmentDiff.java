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

import java.util.Collection;

/**
 * Interface for diff between two alignments.
 * 
 * @author Simon Templer
 * 
 * @param <I> the cell information type
 * @param <C> the cell change type
 */
public interface AlignmentDiff<I extends CellInfo, C extends CellChange> {

	/**
	 * Get the cells that were added.
	 * 
	 * @return a collection of information on cells that were added, may not be
	 *         modified
	 */
	Collection<I> getCellsAdded();

	/**
	 * Get the cells that were removed.
	 * 
	 * @return a collection of information on cells that were removed, may not
	 *         be modified
	 */
	Collection<I> getCellsRemoved();

	/**
	 * Get the cells that were changed.
	 * 
	 * @return a collection of cell change information, may not be modified
	 */
	Collection<C> getCellsChanged();

}
