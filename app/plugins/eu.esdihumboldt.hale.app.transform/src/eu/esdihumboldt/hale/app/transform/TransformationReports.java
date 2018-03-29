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

package eu.esdihumboldt.hale.app.transform;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import eu.esdihumboldt.hale.common.core.report.Report;
import eu.esdihumboldt.hale.common.core.report.ReportHandler;
import eu.esdihumboldt.hale.common.core.report.util.StatisticsHelper;
import eu.esdihumboldt.hale.common.headless.report.ReportFile;
import eu.esdihumboldt.util.groovy.collector.StatsCollector;

/**
 * Transformation report handler.
 * 
 * @author Simon Templer
 */
public class TransformationReports implements ReportHandler {

	private final ReportHandler delegateTo;

	private final List<Report<?>> reports = new ArrayList<>();

	/**
	 * Create a report handler.
	 * 
	 * @param reportFile the optional report file to write reports to
	 */
	public TransformationReports(@Nullable File reportFile) {
		if (reportFile != null) {
			delegateTo = new ReportFile(reportFile);
		}
		else {
			delegateTo = null;
		}
	}

	@Override
	public void publishReport(Report<?> report) {
		synchronized (reports) {
			reports.add(report);
		}
		ExecUtil.printSummary(report);
		if (delegateTo != null) {
			delegateTo.publishReport(report);
		}
	}

	/**
	 * Get the transformation statistics.
	 * 
	 * @return the transformation statistics
	 */
	public StatsCollector getStatistics() {
		return new StatisticsHelper().getStatistics(reports);
	}

}
