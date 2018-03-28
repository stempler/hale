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

package eu.esdihumboldt.hale.common.align.diff.test.impl

import java.util.function.Consumer

import org.junit.Test

import eu.esdihumboldt.hale.common.align.diff.AlignmentDiff
import eu.esdihumboldt.hale.common.align.diff.CellChange
import eu.esdihumboldt.hale.common.align.diff.CellInfo
import eu.esdihumboldt.hale.common.align.diff.DiffAlgorithm
import eu.esdihumboldt.hale.common.align.model.Alignment
import eu.esdihumboldt.hale.common.headless.test.AbstractProjectTest

/**
 * TODO Type description
 * 
 * @author Simon Templer
 * 
 * @param <I> the cell information type
 * @param <C> the cell change type
 */
abstract class AbstractDiffTest<I extends CellInfo, C extends CellChange> extends AbstractProjectTest {

	@Test
	void sampleHydro_v5v6() {
		testDiff('sample-hydro', 'v5', 'v6') { AlignmentDiff<I, C> diff ->
			// Retype from Observation to Measurement has been added
			assert diff.cellsAdded.size() == 1
			assert diff.cellsRemoved.size() == 0
			assert diff.cellsChanged.size() == 0
			
			def added = diff.cellsAdded.iterator().next()
			assert added
			assert added.identifier == 'Measurement'
		}
	}

	// helpers

	protected abstract DiffAlgorithm<I, C> getAlgorithm()

	protected AlignmentDiff testDiff(String name, String version1, String version2,
			Class context = AbstractDiffTest, Consumer<AlignmentDiff<I, C>> tester) {
		def differ = getAlgorithm()

		def align1 = getAlignment(name, version1)
		def align2 = getAlignment(name, version2)

		def diff = differ.calculateDiff(align1, align2)

		tester.accept(diff)

		diff
	}

	protected Alignment getAlignment(String name, String version, Class context = AbstractDiffTest) {
		def project = getProject(context.getResource("/testcases/${name}/${version}/A-to-B.halex"))

		project.alignment
	}

}
