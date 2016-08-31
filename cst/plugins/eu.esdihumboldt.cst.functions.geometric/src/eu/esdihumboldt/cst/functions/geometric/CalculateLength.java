/*
 * Copyright (c) 2012 Data Harmonisation Panel
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
 *     HUMBOLDT EU Integrated Project #030962
 *     Data Harmonisation Panel <http://www.dhpanel.eu>
 */

package eu.esdihumboldt.cst.functions.geometric;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.ListMultimap;
import com.vividsolutions.jts.geom.Geometry;

import eu.esdihumboldt.hale.common.align.model.impl.PropertyEntityDefinition;
import eu.esdihumboldt.hale.common.align.transformation.engine.TransformationEngine;
import eu.esdihumboldt.hale.common.align.transformation.function.PropertyValue;
import eu.esdihumboldt.hale.common.align.transformation.function.TransformationException;
import eu.esdihumboldt.hale.common.align.transformation.function.impl.AbstractSingleTargetPropertyTransformation;
import eu.esdihumboldt.hale.common.align.transformation.function.impl.NoResultException;
import eu.esdihumboldt.hale.common.align.transformation.report.TransformationLog;
import eu.esdihumboldt.hale.common.instance.geometry.GeometryFinder;
import eu.esdihumboldt.hale.common.instance.helper.DepthFirstInstanceTraverser;
import eu.esdihumboldt.hale.common.instance.helper.InstanceTraverser;
import eu.esdihumboldt.hale.common.schema.geometry.GeometryProperty;

/**
 * Calculate length function
 * 
 * @author Kevin Mais
 */
public class CalculateLength
		extends AbstractSingleTargetPropertyTransformation<TransformationEngine>
		implements CalculateLengthFunction {

	/**
	 * @see eu.esdihumboldt.hale.common.align.transformation.function.impl.AbstractSingleTargetPropertyTransformation#evaluate(java.lang.String,
	 *      eu.esdihumboldt.hale.common.align.transformation.engine.TransformationEngine,
	 *      com.google.common.collect.ListMultimap, java.lang.String,
	 *      eu.esdihumboldt.hale.common.align.model.impl.PropertyEntityDefinition,
	 *      java.util.Map,
	 *      eu.esdihumboldt.hale.common.align.transformation.report.TransformationLog)
	 */
	@Override
	protected Object evaluate(String transformationIdentifier, TransformationEngine engine,
			ListMultimap<String, PropertyValue> variables, String resultName,
			PropertyEntityDefinition resultProperty, Map<String, String> executionParameters,
			TransformationLog log) throws TransformationException, NoResultException {

		// get input geometry
		PropertyValue input = variables.get(null).get(0);
		Object inputValue = input.getValue();

		return calculateLength(Collections.singleton(inputValue), log);
	}

	/**
	 * Calculates aggregated geometry length of geometries contained in the
	 * provided objects.
	 * 
	 * @param geometries the geometries or instances containing geometries
	 * @param log the transformation log or <code>null</code>
	 * @return the calculated length
	 * @throws NoResultException if no geometry to determine the length from
	 *             could be found
	 */
	public static double calculateLength(Iterable<?> geometries, @Nullable TransformationLog log)
			throws NoResultException {
		// depth first traverser that on cancel continues traversal but w/o the
		// children of the current object
		InstanceTraverser traverser = new DepthFirstInstanceTraverser(true);

		GeometryFinder geoFind = new GeometryFinder(null);

		for (Object geomHolder : geometries) {
			traverser.traverse(geomHolder, geoFind);
		}

		List<GeometryProperty<?>> geoms = geoFind.getGeometries();

		Geometry geom = null;

		if (geoms.size() > 1) {
			// TODO check if CRS is projected?
			// TODO check/compare CRS axis units?

			int length = 0;

			for (GeometryProperty<?> geoProp : geoms) {
				if (geoProp.getGeometry() != null) {
					length += geoProp.getGeometry().getLength();
				}
			}

			return length;

		}
		else {
			geom = geoms.get(0).getGeometry();
		}

		if (geom != null) {
			return geom.getLength();
		}
		else {
			throw new NoResultException("Geometry for calculate length could not be retrieved.");
		}
	}

}
