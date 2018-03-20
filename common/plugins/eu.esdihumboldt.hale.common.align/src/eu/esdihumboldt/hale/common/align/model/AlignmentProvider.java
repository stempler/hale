/*
 * Copyright (c) 2017 wetransform GmbH
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

package eu.esdihumboldt.hale.common.align.model;

/**
 * Interface for classes that can provide an {@link Alignment}
 * 
 * @author Florian Esser
 */
public interface AlignmentProvider {

	/**
	 * Get the alignment between source and target schemas.
	 * 
	 * @return the alignment
	 */
	Alignment getAlignment();
}
