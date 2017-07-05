package eu.esdihumboldt.hale.io.appschema.mongodb;

import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ListMultimap;

import eu.esdihumboldt.hale.common.align.model.Cell;
import eu.esdihumboldt.hale.common.align.model.Entity;
import eu.esdihumboldt.hale.common.align.model.ParameterValue;
import eu.esdihumboldt.hale.common.align.model.Property;
import eu.esdihumboldt.hale.common.align.model.functions.RenameFunction;
import eu.esdihumboldt.hale.common.schema.model.ChildDefinition;
import eu.esdihumboldt.hale.common.schema.model.PropertyDefinition;
import eu.esdihumboldt.hale.common.schema.model.TypeDefinition;
import eu.esdihumboldt.hale.common.schema.model.impl.AbstractPropertyDecorator;
import eu.esdihumboldt.hale.io.mongo.JsonPathConstraint;
import eu.esdihumboldt.hale.io.xsd.reader.internal.XmlElementReferenceProperty;

public final class Utils {

	private Utils() {
	}

	public static boolean recursiveMapping(Cell typeCell) {
		String transformation = typeCell.getTransformationIdentifier();
		if (transformation == null
				|| !transformation.equalsIgnoreCase("eu.esdihumboldt.hale.align.retype")) {
			return false;
		}
		ListMultimap<String, ParameterValue> parameters = typeCell.getTransformationParameters();
		List<ParameterValue> parameter = parameters.get(RenameFunction.PARAMETER_STRUCTURAL_RENAME);
		if (parameter == null || parameter.isEmpty()) {
			return false;
		}
		ParameterValue value = parameter.get(0);
		return value != null && value.getValue() != null && value.getValue().equals(Boolean.TRUE);
	}

	public static <T extends Entity> T getFirstEntity(
			ListMultimap<String, ? extends Entity> entities, Function<Entity, T> converter) {
		if (entities == null || entities.isEmpty()) {
			throw new IllegalStateException("No entities available.");
		}
		return converter.apply(entities.values().iterator().next());
	}

	public static Property convertToProperty(Entity entity) {
		if (entity instanceof Property) {
			return (Property) entity;
		}
		throw new IllegalStateException("Not a property.");
	}

	public static TypeDefinition getXmlPropertyType(Property property) {
		TypeDefinition propertyType = property.getDefinition().getDefinition().getPropertyType();
		// check for GML object-property model encoding pattern
		TypeDefinition referencedType = getReferencedType(propertyType);
		return referencedType == null ? propertyType : referencedType;
	}

	private static TypeDefinition getReferencedType(TypeDefinition type) {
		if (type.getDeclaredChildren().size() != 1) {
			return null;
		}
		ChildDefinition<?> child = type.getDeclaredChildren().iterator().next();
		if (!(child instanceof PropertyDefinition)) {
			return null;
		}
		PropertyDefinition propertyDefinition = (PropertyDefinition) child;
		TypeDefinition propertyType = propertyDefinition.getPropertyType();
		if (propertyDefinition instanceof XmlElementReferenceProperty) {
			return propertyType;
		}
		// try to unwrap decorators
		boolean isAbstract = propertyDefinition instanceof AbstractPropertyDecorator;
		while (isAbstract) {
			AbstractPropertyDecorator decorator = (AbstractPropertyDecorator) propertyDefinition;
			propertyDefinition = decorator.getDecoratedProperty();
			if (propertyDefinition instanceof XmlElementReferenceProperty) {
				return propertyType;
			}
			isAbstract = propertyDefinition instanceof AbstractPropertyDecorator;
		}
		// not a reference
		return null;
	}

	public static String getRelativeJsonPath(Property source) {
		JsonPathConstraint propertyConstraint = source.getDefinition().getDefinition()
				.getConstraint(JsonPathConstraint.class);
		if (!propertyConstraint.isValid()) {
			return null;
		}
		JsonPathConstraint typeConstraint = source.getDefinition().getType()
				.getConstraint(JsonPathConstraint.class);
		String rootJsonPath = typeConstraint.getJsonPath();
		String propertyJsonPath = propertyConstraint.getJsonPath();
		if (rootJsonPath == null || propertyJsonPath == null) {
			return propertyJsonPath;
		}
		return StringUtils.removeStart(propertyJsonPath, rootJsonPath + ".");
	}
}
