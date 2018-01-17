/*
 * Copyright (c) 2017 interactive instruments GmbH
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
 *     interactive instruments GmbH <http://www.interactive-instruments.de>
 */

package eu.esdihumboldt.hale.io.xtraserver.writer.handler;

import de.interactive_instruments.xtraserver.config.util.api.MappingValue;
import eu.esdihumboldt.hale.common.align.model.Cell;
import eu.esdihumboldt.hale.common.align.model.Property;

/**
 * Transforms the custom function 'custom:alignment:adv.uom.toucum' to a
 * {@link MappingValue}
 * 
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
class CustomFunctionAdvToUCUM extends ClassificationMappingHandler {

	public static String FUNCTION_ID = "custom:alignment:adv.uom.toucum";

	CustomFunctionAdvToUCUM(final MappingContext mappingContext) {
		super(mappingContext);
	}

	/**
	 * @see eu.esdihumboldt.hale.io.xtraserver.writer.handler.AbstractPropertyTransformationHandler#doHandle(eu.esdihumboldt.hale.common.align.model.Cell,
	 *      eu.esdihumboldt.hale.common.align.model.Property,
	 *      de.interactive_instruments.xtraserver.config.util.api.MappingValue)
	 */
	@Override
	public void doHandle(final Cell propertyCell, final Property targetProperty,
			final MappingValue mappingValue) {
		mappingValue.setValue("function_void");
		mappingValue.setTarget(buildPath(targetProperty.getDefinition().getPropertyPath()));
		mappingValue.setDbCodes("urn:adv:uom:m2 urn:adv:uom:m urn:adv:uom:km");
		mappingValue.setDbValues("'m2' 'm' 'km'");
	}

}