/*
 * Copyright (c) 2013 Data Harmonisation Panel
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
 *     Data Harmonisation Panel <http://www.dhpanel.eu>
 */

package eu.esdihumboldt.hale.ui.views.resources.internal;


/**
 * Token that represents the current project.
 * 
 * @author Simon Templer
 */
public enum ProjectToken {

	/** The token instance */
	TOKEN;

	@Override
	public String toString() {
		return "Current project";

//		ProjectService ps = (ProjectService) PlatformUI.getWorkbench().getService(
//				ProjectService.class);
//		if (ps != null) {
//			String name = ps.getProjectInfo().getName();
//			if (name != null) {
//				return name;
//			}
//		}
//		return "<Unnamed project>";
	}

}
