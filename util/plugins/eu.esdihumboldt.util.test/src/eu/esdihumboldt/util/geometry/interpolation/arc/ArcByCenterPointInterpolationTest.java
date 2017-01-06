/*
 * Copyright (c) 2016 wetransform GmbH
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

package eu.esdihumboldt.util.geometry.interpolation.arc;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

import eu.esdihumboldt.util.geometry.interpolation.AbstractInterpolationTest;
import eu.esdihumboldt.util.geometry.interpolation.ArcByCenterPointInterpolation;
import eu.esdihumboldt.util.geometry.interpolation.Interpolation;
import ru.yandex.qatools.allure.annotations.Features;
import ru.yandex.qatools.allure.annotations.Stories;

/**
 * ArcByCenterPoint interpolation
 * 
 * @author Arun
 */
@Features("Geometries")
@Stories("Arcs")
@RunWith(Parameterized.class)
public class ArcByCenterPointInterpolationTest extends AbstractInterpolationTest {

	private final int testIndex;
	private final Coordinate center;
	private final Class<?> generatedGeometryType;
	private final boolean skipTest;
	private final double radius;
	private final double startAngle;
	private final double endAngle;

	private static final double e = 0.1;

	private static final boolean SKIP_TEST = false;

	/**
	 * Constructor for parameterized test
	 * 
	 * @param testIndex Index of test parameters
	 * @param center center of arc
	 * @param radius radius of arc
	 * @param startAngle specifies the bearing of the arc at the start
	 * @param endAngle specifies the bearing of the arc at the end
	 * @param geometry type of output geometry
	 * @param skipTest if wants to skip test
	 */
	public ArcByCenterPointInterpolationTest(int testIndex, Coordinate center, double radius,
			double startAngle, double endAngle, Class<?> geometry, boolean skipTest) {
		this.testIndex = testIndex;
		this.center = center;
		this.radius = radius;
		this.startAngle = startAngle;
		this.endAngle = endAngle;
		this.generatedGeometryType = geometry;
		this.skipTest = skipTest;
	}

	/**
	 * Before method to skip test
	 */
	@Before
	public void beforeMethod() {
		Assume.assumeFalse(this.skipTest);
	}

	/**
	 * Passing arc geometries
	 * 
	 * @return Collection of arc coordinates and type of generated geometry
	 */
	@SuppressWarnings("rawtypes")
	@Parameters
	public static Collection addCoordiantes() {
		return Arrays.asList(new Object[][] { //
				{ 0, new Coordinate(0, 0), 5, 30, 60, LineString.class, SKIP_TEST }, //
				{ 1, new Coordinate(577869.169, 5917253.678), 5, 30, 60, LineString.class,
						SKIP_TEST }, //
				{ 2, new Coordinate(240, 280), 6.2, 120, 60, LineString.class, SKIP_TEST }, //
				{ 3, new Coordinate(0, -5), 3.9, 290, 120, LineString.class, SKIP_TEST } //
		});
	}

	@SuppressWarnings("javadoc")
	@Test
	public void testInterpolation() throws IOException {
		System.out.println("Test-" + testIndex);
		Interpolation<LineString> interpolation = new ArcByCenterPointInterpolation(center, radius,
				startAngle, endAngle, e);
		Geometry interpolatedArc = interpolation.interpolateRawGeometry();

		// printCoordinates(interpolatedArc);

		assertNotNull(interpolatedArc);
		Assert.assertEquals(interpolatedArc.getClass(), generatedGeometryType);

		checkNeighbourCoordinates(interpolatedArc);

		validateCoordinatesOnGrid(interpolatedArc, 0, e, false);

		drawImage((LineString) interpolatedArc, new Coordinate[] { center }, testIndex);
	}

}