package eu.esdihumboldt.hale.io.appschema.mongodb;

import eu.esdihumboldt.hale.common.align.model.Cell;
import eu.esdihumboldt.hale.common.align.model.Property;
import eu.esdihumboldt.hale.common.schema.model.TypeDefinition;
import eu.esdihumboldt.hale.io.appschema.impl.internal.generated.app_schema.AttributeExpressionMappingType;
import eu.esdihumboldt.hale.io.appschema.impl.internal.generated.app_schema.AttributeMappingType;
import eu.esdihumboldt.hale.io.appschema.impl.internal.generated.app_schema.TypeMappingsPropertyType.FeatureTypeMapping;
import eu.esdihumboldt.hale.io.appschema.model.ChainConfiguration;
import eu.esdihumboldt.hale.io.appschema.writer.internal.AppSchemaMappingContext;
import eu.esdihumboldt.hale.io.appschema.writer.internal.AppSchemaMappingWrapper;
import eu.esdihumboldt.hale.io.appschema.writer.internal.TypeTransformationHandler;
import eu.esdihumboldt.hale.io.mongo.JsonPathConstraint;

public class CollectionLinkHandler implements TypeTransformationHandler {

	@Override
	public FeatureTypeMapping handleTypeTransformation(Cell typeCell,
			AppSchemaMappingContext context) {
		AppSchemaMappingWrapper mapping = context.getMappingWrapper();
		Property source = Utils.getFirstEntity(typeCell.getSource(), Utils::convertToProperty);
		Property target = Utils.getFirstEntity(typeCell.getTarget(), Utils::convertToProperty);
		TypeDefinition targetType = Utils.getXmlPropertyType(target);
		FeatureTypeMapping nested = mapping.getOrCreateFeatureTypeMapping(targetType);
		JsonPathConstraint jsonPath = source.getDefinition().getDefinition().getPropertyType()
				.getConstraint(JsonPathConstraint.class);
		AttributeMappingType containerJoinMapping = mapping.getOrCreateAttributeMapping(
				target.getDefinition().getType(), null, target.getDefinition().getPropertyPath());
		containerJoinMapping.setTargetAttribute(mapping.buildAttributeXPath(
				source.getDefinition().getDefinition().getPropertyType(),
				target.getDefinition().getPropertyPath()));
		containerJoinMapping.setIsMultiple(true);
		ChainConfiguration cg = new ChainConfiguration();
		cg.setNestedTypeTarget(target.getDefinition());
		context.getFeatureChaining().putChain(jsonPath.getJsonPath(), 0, cg);
		AttributeExpressionMappingType containerSourceExpr = new AttributeExpressionMappingType();
		// join column extracted from join condition
		containerSourceExpr.setOCQL(String.format("collectionLink('%s')", jsonPath.getJsonPath()));
		containerSourceExpr.setLinkElement(getLinkElementValue(nested));
		String linkField = mapping.getUniqueFeatureLinkAttribute(source.getDefinition().getType(),
				"nestedFTMapping.getMappingName()");
		containerSourceExpr.setLinkField(linkField);
		containerJoinMapping.setSourceExpression(containerSourceExpr);
		AttributeMappingType nestedJoinMapping = new AttributeMappingType();
		AttributeExpressionMappingType nestedSourceExpr = new AttributeExpressionMappingType();
		// join column extracted from join condition
		nestedSourceExpr.setOCQL(String.format("collectionLink('%s')", jsonPath.getJsonPath()));
		nestedJoinMapping.setSourceExpression(nestedSourceExpr);
		nestedJoinMapping.setTargetAttribute(linkField);
		nested.getAttributeMappings().getAttributeMapping().add(nestedJoinMapping);
		return nested;
	}

	private String getLinkElementValue(FeatureTypeMapping nestedFeatureTypeMapping) {
		if (nestedFeatureTypeMapping.getMappingName() != null
				&& !nestedFeatureTypeMapping.getMappingName().isEmpty()) {
			// playing safe: always enclose mapping name in single quotes
			return "'" + nestedFeatureTypeMapping.getMappingName() + "'";
		}
		else {
			return nestedFeatureTypeMapping.getTargetElement();
		}
	}
}
