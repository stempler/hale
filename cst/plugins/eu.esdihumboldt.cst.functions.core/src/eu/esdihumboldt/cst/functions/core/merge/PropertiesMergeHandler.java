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

package eu.esdihumboldt.cst.functions.core.merge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import com.google.common.collect.ListMultimap;

import eu.esdihumboldt.hale.common.align.model.ParameterValue;
import eu.esdihumboldt.hale.common.align.model.functions.MergeFunction;
import eu.esdihumboldt.hale.common.align.model.functions.merge.MergeUtil;
import eu.esdihumboldt.hale.common.align.transformation.function.TransformationException;
import eu.esdihumboldt.hale.common.align.transformation.report.TransformationLog;
import eu.esdihumboldt.hale.common.instance.groovy.InstanceAccessor;
import eu.esdihumboldt.hale.common.instance.index.DeepIterableKey;
import eu.esdihumboldt.hale.common.instance.model.Instance;
import eu.esdihumboldt.hale.common.instance.model.InstanceCollection;
import eu.esdihumboldt.hale.common.instance.model.InstanceMetadata;
import eu.esdihumboldt.hale.common.instance.model.MutableInstance;
import eu.esdihumboldt.hale.common.instance.model.ResourceIterator;
import eu.esdihumboldt.hale.common.schema.model.TypeDefinition;

/**
 * Merge based on equal properties.
 * 
 * @author Simon Templer
 */
public class PropertiesMergeHandler
		extends AbstractMergeHandler<PropertiesMergeHandler.PropertiesMergeConfig, DeepIterableKey>
		implements MergeFunction {

	class PropertiesMergeConfig {

		private final List<List<QName>> keyProperties;
		private final List<List<QName>> additionalProperties;
		private final boolean autoDetect;

		private PropertiesMergeConfig(List<List<QName>> keyProperties,
				List<List<QName>> additionalProperties, boolean autoDetect) {
			super();
			this.keyProperties = keyProperties;
			this.additionalProperties = additionalProperties;
			this.autoDetect = autoDetect;
		}
	}

	@Override
	protected PropertiesMergeConfig createMergeConfiguration(String transformationIdentifier,
			ListMultimap<String, ParameterValue> transformationParameters,
			Map<String, String> executionParameters, TransformationLog log)
					throws TransformationException {
		if (transformationParameters == null) {
			throw new TransformationException("Transformation parameters invalid");
		}

		List<List<QName>> properties = MergeUtil.getProperties(transformationParameters,
				PARAMETER_PROPERTY);

		List<List<QName>> additionalProperties = MergeUtil.getProperties(transformationParameters,
				PARAMETER_ADDITIONAL_PROPERTY);

		boolean autoDetect;
		if (transformationParameters.get(PARAMETER_AUTO_DETECT).isEmpty()) {
			// default to false (original behavior)
			autoDetect = false;
		}
		else {
			autoDetect = Boolean.parseBoolean(
					transformationParameters.get(PARAMETER_AUTO_DETECT).get(0).as(String.class));
		}

		return new PropertiesMergeConfig(properties, additionalProperties, autoDetect);
	}

	@Override
	protected DeepIterableKey getMergeKey(Instance instance, PropertiesMergeConfig mergeConfig) {
		if (mergeConfig.keyProperties.isEmpty()) {
			// merge all instances
			return DeepIterableKey.KEY_ALL;
		}

		List<Object> valueList = new ArrayList<Object>(mergeConfig.keyProperties.size());

		for (List<QName> propertyPath : mergeConfig.keyProperties) {
			// retrieve values from instance
			// XXX should nulls be listed?
			InstanceAccessor accessor = new InstanceAccessor(instance);
			for (QName name : propertyPath) {
				accessor.findChildren(name.getLocalPart(),
						Collections.singletonList(name.getNamespaceURI()));
			}
			valueList.add(accessor.list(true));
		}

		return new DeepIterableKey(valueList);
	}

	@Override
	protected Instance merge(InstanceCollection instances, TypeDefinition type,
			DeepIterableKey mergeKey, PropertiesMergeConfig mergeConfig) {
		if (instances.hasSize() && instances.size() == 1) {
			// early exit if only one instance to merge
			try (ResourceIterator<Instance> it = instances.iterator()) {
				return it.next();
			}
		}

		MutableInstance result = getInstanceFactory().createInstance(type);

		/*
		 * FIXME This a first VERY basic implementation, where only the first
		 * item in each property path is regarded, and that whole tree is added
		 * only once (from the first instance). XXX This especially will be a
		 * problem, if a path contains a choice. XXX For more advanced stuff we
		 * need more advanced test cases.
		 */
		Set<QName> rootNames = new HashSet<QName>();
		Set<QName> nonKeyRootNames = new HashSet<QName>();
		// collect path roots
		for (List<QName> path : mergeConfig.keyProperties) {
			rootNames.add(path.get(0));
		}
		for (List<QName> path : mergeConfig.additionalProperties) {
			nonKeyRootNames.add(path.get(0));
		}

		// XXX what about metadata?!
		// XXX for now only retain IDs
		Set<Object> ids = new HashSet<Object>();

		try (ResourceIterator<Instance> it = instances.iterator()) {
			while (it.hasNext()) {
				Instance instance = it.next();

				for (QName name : instance.getPropertyNames()) {
					if (rootNames.contains(name)) {
						/*
						 * Property is merge key -> only use first occurrence
						 * (as all entries need to be the same)
						 * 
						 * TODO adapt if multiple keys are possible per instance
						 */
						addFirstOccurrence(result, instance, name);
					}
					else if (nonKeyRootNames.contains(name)) {
						/*
						 * Property is additional merge property.
						 * 
						 * Traditional behavior: Only keep unique values.
						 * 
						 * XXX should this be configurable?
						 */
						addUnique(result, instance, name);
					}
					else if (mergeConfig.autoDetect) {
						/*
						 * Auto-detection is enabled.
						 * 
						 * Only keep unique values.
						 * 
						 * XXX This differs from the traditional behavior in
						 * that there only the first value would be used, but
						 * only if all values were equal. That cannot be easily
						 * checked in an iterative approach.
						 */
						addUnique(result, instance, name);
					}
					else {
						/*
						 * Property is not to be merged.
						 * 
						 * XXX but we could do some kind of aggregation
						 * 
						 * XXX for now just add all values
						 */
						addValues(result, instance, name);
					}
				}

				List<Object> instanceIDs = instance.getMetaData(InstanceMetadata.METADATA_ID);
				for (Object id : instanceIDs) {
					ids.add(id);
				}
			}
		}

		// store metadata IDs
		result.setMetaData(InstanceMetadata.METADATA_ID, ids.toArray());

		return result;
	}

	/**
	 * Apply instance property values to the merged result instance. Use the
	 * "first occurrence" strategy that only keeps the values from the first
	 * instance.
	 * 
	 * @param result the result instance
	 * @param instance the instance to merge with the result
	 * @param property the name of the property that should be handled
	 */
	private void addFirstOccurrence(MutableInstance result, Instance instance, QName property) {
		Object[] existingValues = result.getProperty(property);

		if (existingValues == null || existingValues.length <= 0) {
			// no values yet -> add values
			addValues(result, instance, property);
		}
	}

	/**
	 * Apply instance property values to the merged result instance. Use the
	 * "unique" strategy that only keeps unique values.
	 * 
	 * @param result the result instance
	 * @param instance the instance to merge with the result
	 * @param property the name of the property that should be handled
	 */
	private void addUnique(MutableInstance result, Instance instance, QName property) {
		Object[] values = instance.getProperty(property);
		if (values == null || values.length <= 0) {
			return;
		}

		// collect unique values
		Object[] existingValues = result.getProperty(property);
		Set<DeepIterableKey> uniqueValues = new HashSet<>();
		if (existingValues != null) {
			for (Object value : existingValues) {
				uniqueValues.add(new DeepIterableKey(value));
			}
		}

		// add values not contained yet
		for (Object value : values) {
			DeepIterableKey key = new DeepIterableKey(value);
			if (uniqueValues.add(key)) {
				result.addProperty(property, value);
			}
		}
	}

	/**
	 * Apply instance property values to the merged result instance. Use the
	 * "add values" strategy that keeps all values.
	 * 
	 * @param result the result instance
	 * @param instance the instance to merge with the result
	 * @param property the name of the property that should be handled
	 */
	private void addValues(MutableInstance result, Instance instance, QName property) {
		// add all values
		Object[] values = instance.getProperty(property);
		if (values != null) {
			for (Object value : values) {
				result.addProperty(property, value);
			}
		}
	}

	@SuppressWarnings("unused")
	private boolean allEqual(List<Object[]> list) {
		Iterator<Object[]> iter = list.iterator();
		// get first element
		DeepIterableKey first = new DeepIterableKey(iter.next());
		// compare rest to first
		while (iter.hasNext())
			if (!first.equals(new DeepIterableKey(iter.next())))
				return false;
		return true;
	}
}
