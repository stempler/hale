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

package eu.esdihumboldt.hale.common.core.report;

import eu.esdihumboldt.util.groovy.collector.StatsCollector;

/**
 * Interface for objects providing a statistics collector.
 * 
 * @author Simon Templer
 */
public interface Statistics {

	/**
	 * Get the collector for collecting statistics.
	 * 
	 * @return the statistics collector
	 */
	StatsCollector stats();

}
