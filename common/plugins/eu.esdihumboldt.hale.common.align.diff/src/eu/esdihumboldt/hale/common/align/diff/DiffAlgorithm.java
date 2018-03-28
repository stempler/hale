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

import eu.esdihumboldt.hale.common.align.model.Alignment;

/**
 * Interface for Diff algorithms.
 * 
 * @author Simon Templer
 * 
 * @param <I> the cell information type
 * @param <C> the cell change type
 */
public interface DiffAlgorithm<I extends CellInfo, C extends CellChange> {

	/**
	 * Calculate the diff between two alignments.
	 * 
	 * @param original the original alignment
	 * @param update the updated alignment
	 * @return the calculated diff
	 * @throws Exception if calculating the alignment fails
	 */
	AlignmentDiff<I, C> calculateDiff(Alignment original, Alignment update) throws Exception;

}
