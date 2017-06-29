/*
 * Copyright (c) 2015 Data Harmonisation Panel
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

package eu.esdihumboldt.hale.io.appschema.writer.internal;

import eu.esdihumboldt.cst.functions.core.Rename;
import eu.esdihumboldt.hale.common.align.model.Property;
import eu.esdihumboldt.hale.io.appschema.writer.AppSchemaMappingUtils;
import eu.esdihumboldt.hale.io.mongo.JsonPathConstraint;

/**
 * Translates a property cell specifying an {@link Rename} transformation
 * function to an app-schema attribute mapping.
 * 
 * @author Stefano Costa, GeoSolutions
 */
public class RenameHandler extends AbstractPropertyTransformationHandler {

	/**
	 * @see eu.esdihumboldt.hale.io.appschema.writer.internal.AbstractPropertyTransformationHandler#getSourceExpressionAsCQL()
	 */
	@Override
	protected String getSourceExpressionAsCQL() {
		Property source = AppSchemaMappingUtils.getSourceProperty(propertyCell);
		String cqlExpression = source.getDefinition().getDefinition().getName().getLocalPart();
		// apply MongoDB JSOn selection if needed
		JsonPathConstraint jsonConstraint = source.getDefinition().getDefinition()
				.getConstraint(JsonPathConstraint.class);
		if (jsonConstraint.isValid()) {
			cqlExpression = String.format("jsonSelect('%s')", cqlExpression);
		}

		return getConditionalExpression(source.getDefinition(), cqlExpression);
	}

}
