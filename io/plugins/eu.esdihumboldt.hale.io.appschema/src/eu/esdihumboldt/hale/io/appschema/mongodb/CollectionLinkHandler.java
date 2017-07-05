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

public class CollectionLinkHandler implements TypeTransformationHandler {

	@Override
	public FeatureTypeMapping handleTypeTransformation(Cell typeCell,
			AppSchemaMappingContext context) {
		AppSchemaMappingWrapper mapping = context.getMappingWrapper();
		Property source = Utils.getFirstEntity(typeCell.getSource(), Utils::convertToProperty);
		Property target = Utils.getFirstEntity(typeCell.getTarget(), Utils::convertToProperty);
		TypeDefinition targetType = Utils.getXmlPropertyType(target);
		String jsonPath = Utils.getRelativeJsonPath(source);
		FeatureTypeMapping nested = mapping.getOrCreateFeatureTypeMapping(targetType, null);
		nested.setSourceType(jsonPath);
		// nested.setMappingName(jsonPath.getJsonPath());
		AttributeMappingType containerJoinMapping = mapping.getOrCreateAttributeMapping(
				target.getDefinition().getType(), null, target.getDefinition().getPropertyPath());
		containerJoinMapping.setTargetAttribute(mapping.buildAttributeXPath(
				source.getDefinition().getDefinition().getPropertyType(),
				target.getDefinition().getPropertyPath()));
		containerJoinMapping.setIsMultiple(true);
		ChainConfiguration cg = new ChainConfiguration();
		cg.setNestedTypeTarget(target.getDefinition());
		context.getFeatureChaining().putChain(jsonPath, 0, cg);
		AttributeExpressionMappingType containerSourceExpr = new AttributeExpressionMappingType();
		// join column extracted from join condition
		containerSourceExpr.setOCQL(String.format("collectionLink('%s')", jsonPath));
		containerSourceExpr.setLinkElement(getLinkElementValue(nested));
		String linkField = mapping.getUniqueFeatureLinkAttribute(source.getDefinition().getType(),
				"nestedFTMapping.getMappingName()");
		containerSourceExpr.setLinkField(linkField);
		containerJoinMapping.setSourceExpression(containerSourceExpr);
		AttributeMappingType nestedJoinMapping = new AttributeMappingType();
		AttributeExpressionMappingType nestedSourceExpr = new AttributeExpressionMappingType();
		// join column extracted from join condition
		nestedSourceExpr.setOCQL("nestedCollectionLink()");
		nestedJoinMapping.setSourceExpression(nestedSourceExpr);
		nestedJoinMapping.setTargetAttribute(linkField);
		nested.getAttributeMappings().getAttributeMapping().add(nestedJoinMapping);

		// add collection id to the container
		AttributeMappingType attributeMapping = mapping.getOrCreateAttributeMapping(targetType,
				null, null);
		attributeMapping.setTargetAttribute(nested.getTargetElement());
		// set id expression
		AttributeExpressionMappingType idExpression = new AttributeExpressionMappingType();
		idExpression.setOCQL("collectionId()");
		attributeMapping.setIdExpression(idExpression);

		// return nested feature type definition
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
