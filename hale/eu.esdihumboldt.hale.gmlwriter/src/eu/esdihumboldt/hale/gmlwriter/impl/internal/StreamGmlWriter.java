/*
 * HUMBOLDT: A Framework for Data Harmonisation and Service Integration.
 * EU Integrated Project #030962                 01.10.2006 - 30.09.2010
 * 
 * For more information on the project, please refer to the this web site:
 * http://www.esdi-humboldt.eu
 * 
 * LICENSE: For information on the license under which this program is 
 * available, please refer to http:/www.esdi-humboldt.eu/license.html#core
 * (c) the HUMBOLDT Consortium, 2007 to 2010.
 */

package eu.esdihumboldt.hale.gmlwriter.impl.internal;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;
import java.util.Map.Entry;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.geotools.feature.ComplexAttributeImpl;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.gml3.GML;
import org.geotools.gml3.GMLSchema;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.identity.FeatureId;

import com.vividsolutions.jts.geom.Geometry;

import de.cs3d.util.logging.ALogger;
import de.cs3d.util.logging.ALoggerFactory;
import eu.esdihumboldt.hale.gmlwriter.impl.internal.geometry.StreamGeometryWriter;
import eu.esdihumboldt.hale.gmlwriter.impl.internal.simpletype.SimpleTypeUtil;
import eu.esdihumboldt.hale.schemaprovider.Schema;
import eu.esdihumboldt.hale.schemaprovider.model.AttributeDefinition;
import eu.esdihumboldt.hale.schemaprovider.model.SchemaElement;
import eu.esdihumboldt.hale.schemaprovider.model.TypeDefinition;
import eu.esdihumboldt.tools.AttributeProperty;
import eu.esdihumboldt.tools.FeatureInspector;

/**
 * Writes GML using an {@link XMLStreamWriter}
 *
 * @author Simon Templer
 * @partner 01 / Fraunhofer Institute for Computer Graphics Research
 * @version $Id$ 
 */
public class StreamGmlWriter {
	
	/**
	 * Schema instance namespace (for specifying schema locations)
	 */
	private static final String SCHEMA_INSTANCE_NS = "http://www.w3.org/2001/XMLSchema-instance";
	
	private static final ALogger log = ALoggerFactory.getLogger(StreamGmlWriter.class);

	/**
	 * The XML stream writer
	 */
	private final XMLStreamWriter writer;
	
	/**
	 * The target schema
	 */
	private final Schema targetSchema;

	/**
	 * The GML namespace
	 */
	private final String gmlNs;
	
	/**
	 * The common SRS name, may be <code>null</code>
	 */
	private final String commonSrsName;
	
	/**
	 * The type index
	 */
	private final TypeIndex types;
	
	/**
	 * The geometry writer
	 */
	private StreamGeometryWriter geometryWriter;

	/**
	 * Constructor
	 * 
	 * @param targetSchema the target schema
	 * @param out the output stream
	 * @param commonSrsName the common SRS name, may be <code>null</code>
	 * @throws XMLStreamException if setting up the stream writer fails
	 */
	public StreamGmlWriter(Schema targetSchema, OutputStream out,
			String commonSrsName) throws XMLStreamException {
		this.targetSchema = targetSchema;
		this.commonSrsName = commonSrsName;
		
		// create and set-up a writer
		
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		// will set namespaces if these not set explicitly
		outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces",
				Boolean.valueOf(true));
		// create XML stream writer with UTF-8 encoding
		XMLStreamWriter tmpWriter = outputFactory.createXMLStreamWriter(out, "UTF-8");

		String defNamespace = null;
		
		// read the namespaces from the map containing namespaces
		if (targetSchema.getPrefixes() != null) {
			for (Entry<String, String> entry : targetSchema.getPrefixes().entrySet()) {
				if (entry.getValue().isEmpty()) {
					defNamespace = entry.getKey();
				}
				else {
					tmpWriter.setPrefix(entry.getValue(), entry.getKey());
				}
			}
		}
		
		addNamespace(tmpWriter, SCHEMA_INSTANCE_NS, "xsi");
		
		if (defNamespace == null) {
			defNamespace = targetSchema.getNamespace();
			
			//TODO remove prefix for target schema namespace?
		}
		
		tmpWriter.setDefaultNamespace(defNamespace);
		
		writer = new IndentingXMLStreamWriter(tmpWriter);
		
		// determine GML namespace from target schema
		String gml = null;
		if (targetSchema.getPrefixes() != null) {
			for (String ns : targetSchema.getPrefixes().keySet()) {
				if (ns.startsWith("http://www.opengis.net/gml")) {
					gml = ns;
					break;
				}
			}
		}
		
		if (gml == null) {
			// default to GML 2/3 namespace
			gml = GML.NAMESPACE;
		}
		
		gmlNs = gml;
		if (log.isDebugEnabled()) {
			log.debug("GML namespace is " + gmlNs);
		}
		
		// fill type index with root types
		types = new TypeIndex();
		for (SchemaElement element : targetSchema.getElements().values()) {
			types.addType(element.getType());
		}
	}

	/**
	 * Add a namespace to XML stream writer
	 * 
	 * @param writer the XML stream writer
	 * @param namespace the namespace to add
	 * @param preferredPrefix the preferred prefix
	 * @throws XMLStreamException if setting a prefix for the namespace fails
	 */
	private void addNamespace(XMLStreamWriter writer,
			String namespace, String preferredPrefix) throws XMLStreamException {
		if (writer.getPrefix(namespace) == null) {
			// no prefix for schema instance namespace
			
			String prefix = preferredPrefix;
			String ns = writer.getNamespaceContext().getNamespaceURI(prefix);
			if (ns == null) {
				// add xsi namespace
				writer.setPrefix(prefix, namespace);
			}
			else {
				int i = 0;
				while (ns != null) {
					ns = writer.getNamespaceContext().getNamespaceURI(prefix + "-" + (++i));
				}
				
				writer.setPrefix(prefix + "-" + i, namespace);
			}
		}
	}

	/**
	 * Write the given features
	 * 
	 * @param features the feature collection
	 * @throws XMLStreamException if writing the feature collection fails 
	 */
	public void write(FeatureCollection<FeatureType, Feature> features) throws XMLStreamException {
		writer.writeStartDocument();
		
		// try to find FeatureCollection element
		Iterator<SchemaElement> it = targetSchema.getAllElements().values().iterator();
		Collection<SchemaElement> fcElements = new HashSet<SchemaElement>();
		while (it.hasNext()) {
			SchemaElement el = it.next();
			if (el.getElementName().getLocalPart().contains("FeatureCollection") &&
					!el.getType().isAbstract() &&
					el.getType().getAttribute("featureMember") != null) { //TODO improve condition?
				fcElements.add(el);
			}
		}
		
		TypeDefinition fcDefinition = null;
		Name fcName;
		if (fcElements.isEmpty()) {
			log.warn("No element describing a FeatureCollection found");
			//TODO include an additional schema with a FC-definition?
			fcName = new NameImpl(gmlNs, "FeatureCollection");
		}
		else {
			// select fc element TODO priorized selection
			SchemaElement fcElement = fcElements.iterator().next();
			fcDefinition = fcElement.getType();
			fcName = fcElement.getElementName();
			
			log.info("Found " + fcElements.size() + " possible FeatureCollection elements" +
					", using element " + fcElement.getElementName());
		}
		
		writer.writeStartElement(fcName.getNamespaceURI(), fcName.getLocalPart());
		
		if (fcDefinition != null) {
			// generate mandatory id attribute
			writeRequiredID(writer, fcDefinition, null, false);
		}
		
		StringBuffer locations = new StringBuffer();
		locations.append(targetSchema.getNamespace());
		locations.append(" ");
		locations.append(targetSchema.getLocation().toString());
		writer.writeAttribute(SCHEMA_INSTANCE_NS, "schemaLocation", locations.toString());
		
		Iterator<Feature> itFeature = features.iterator();
		try {
			while (itFeature.hasNext()) {
				Feature feature = itFeature.next();
				
				// write the feature
				if (fcDefinition != null) {
					AttributeDefinition memberAtt = fcDefinition.getAttribute("featureMember");
					writer.writeStartElement(memberAtt.getNamespace(), memberAtt.getName());
				}
				else {
					writer.writeStartElement(gmlNs, "featureMember");
				}
	            
	            TypeDefinition type = types.getType(feature.getType());
	            writeMember(feature, type);
	            
	            writer.writeEndElement(); // featureMember
			}
		} finally {
			features.close(itFeature);
		}
        
        writer.writeEndElement(); // FeatureCollection
        
        writer.writeEndDocument();
	}

	/**
	 * Write any required ID attribute, generating a random ID if needed
	 * 
	 * @param writer the XML stream writer
	 * @param type the type definition
	 * @param parent the parent object, may be <code>null</code>. If it is set
	 *   the value for the ID will be tried to be retrieved from the parent
	 *   object, otherwise a random ID will be generated
	 * @param onlyIfNotSet if the ID shall only be written if no value is set
	 *   in the parent object
	 * @throws XMLStreamException if an error occurs writing the ID
	 */
	public static void writeRequiredID(XMLStreamWriter writer,
			TypeDefinition type, ComplexAttribute parent, boolean onlyIfNotSet) throws XMLStreamException {
		// find ID attribute
		AttributeDefinition idAtt = null;
		for (AttributeDefinition att : type.getAttributes()) {
			if (att.isAttribute() && att.getMinOccurs() > 0 && isID(att.getAttributeType())) {
				idAtt = att;
				break; // we assume there is only one ID attribute
			}
		}
		
		if (idAtt == null) {
			// no ID attribute found
			return;
		}
		
		Object value = null;
		if (parent != null) {
			Property prop = FeatureInspector.getProperty(parent, Arrays.asList(idAtt.getName()), false);
			if (prop != null) {
				value = prop.getValue();
			}
			
			if (value != null && onlyIfNotSet) {
				// don't write the ID
				return;
			}
		}
		
		if (value != null) {
			writeAttribute(writer, value, idAtt);
		}
		else {
			UUID genID = UUID.randomUUID();
			writeAttribute(writer, "_" + genID.toString(), idAtt);
		}
	}

	/**
	 * Determines if the given type represents a XML ID
	 * 
	 * @param type the type definition
	 * @return if the type represents an ID
	 */
	private static boolean isID(TypeDefinition type) {
		if (type.getName().equals(new NameImpl("http://www.w3.org/2001/XMLSchema", "ID"))) {
			return true;
		}
		
		if (type.getSuperType() != null) {
			return isID(type.getSuperType());
		}
		else {
			return false;
		}
	}

	/**
	 * Write a given feature
	 * 
	 * @param feature the feature to write
	 * @param type the feature type definition
	 * @throws XMLStreamException if writing the feature fails 
	 */
	protected void writeMember(Feature feature, TypeDefinition type) throws XMLStreamException {
		Name elementName = GmlWriterUtil.getElementName(type);
		writer.writeStartElement(elementName.getNamespaceURI(), elementName.getLocalPart());
		
		// feature id
		FeatureId id = feature.getIdentifier();
		
		if (id != null) {
			String idAttribute = "fid"; //XXX GML 2/3 ?
			AttributeDefinition idDef = type.getAttribute(idAttribute);
			if (idDef == null) {
				idAttribute = "id"; // GML 3.2
				idDef = type.getAttribute(idAttribute);
			}
			if (idDef != null && idDef.isAttribute()) {
				// id attribute present in type
				Object idProp = FeatureInspector.getPropertyValue(feature, Arrays.asList(idAttribute), null);
				if (idProp == null) {
					// set id attribute on feature if not set
					FeatureInspector.setPropertyValue(feature, Arrays.asList(idAttribute), id);
				}
			}
			else {
				// manually add id attribute
				writer.writeAttribute(gmlNs, idAttribute, id.toString());
			}
		}
		
		writeProperties(feature, type, true);
		
		writer.writeEndElement(); // type element name
	}

	/**
	 * Write the given feature's properties
	 * 
	 * @param feature the feature
	 * @param type the feature type
	 * @param allowElements if element properties may be written
	 * @throws XMLStreamException if writing the properties fails
	 */
	private void writeProperties(ComplexAttribute feature, TypeDefinition type, boolean allowElements) throws XMLStreamException {
		// eventually generate mandatory ID that is not set
		writeRequiredID(writer, type, feature, true);
		
		// writing the feature is controlled by the type definition
		Collection<AttributeDefinition> attributes = type.getAttributes();
		for (AttributeDefinition attDef : attributes) {
			// attributes must be handled first
			if (attDef.isAttribute()) {
				Property property = FeatureInspector.getProperty(feature, Arrays.asList(attDef.getName()), false);
				Object value = (property == null) ? (null) : (property.getValue());
				
				writeAttribute(value, attDef);
			}
		}
		
		if (allowElements) {
			for (AttributeDefinition attDef : attributes) {
				// elements must be handled after attributes
				if (!attDef.isAttribute()) {
					Property property = FeatureInspector.getProperty(feature, Arrays.asList(attDef.getName()), false);
					Object value = (property == null) ? (null) : (property.getValue());
					
					writeElement(value, property, attDef);
				}
			}
		}
	}

	/**
	 * Write a property element
	 * 
	 * @param value the element value
	 * @param property the property that contained the value (needed for 
	 * attribute values for simple types)
	 * @param attDef the attribute definition
	 * @throws XMLStreamException if writing the element fails
	 */
	private void writeElement(Object value, Property property, AttributeDefinition attDef) throws XMLStreamException {
		// for collections call this method for each item
		if (value instanceof Collection<?>) {
			for (Object item : ((Collection<?>) value)) {
				writeElement(item, null, attDef);
			}
			return;
		}
		
		// single objects
		
		if (value == null) {
			// null value
			if (attDef.getMinOccurs() > 0) {
				// write empty element
				writer.writeEmptyElement(attDef.getNamespace(), attDef.getName());
				
				// but may have attributes
				writeSimpleTypeAttributes(property, attDef);
				
				if (!attDef.isNillable()) {
					log.warn("Non-nillable element " + attDef.getName() + " is null");
				}
				else {
					// nillable -> we may mark it as nil
					writer.writeAttribute(SCHEMA_INSTANCE_NS, "nil", "true");
				}
			}
			// otherwise just skip it
		}
		else {
			// value is set
			
			writer.writeStartElement(attDef.getNamespace(), attDef.getName());
			
			if (value instanceof ComplexAttribute) {
				// write properties
				writeProperties((ComplexAttribute) value, attDef.getAttributeType(), true);
			}
			else if (value instanceof Geometry) {
				// write geometry
				writeGeometry(((Geometry) value), attDef.getAttributeType(), commonSrsName);
			}
			else {
				// write any attributes
				writeSimpleTypeAttributes(property, attDef);
				
				// write value as content
				writer.writeCharacters(SimpleTypeUtil.convert(value, attDef.getAttributeType()));
			}
			
			writer.writeEndElement();
		}
	}

	/**
	 * Write any attributes for simple type elements
	 * 
	 * @param property the property of the simple type element
	 * @param attDef the attribute definition of the simple type element
	 * @throws XMLStreamException if an error occurs writing the attributes
	 */
	private void writeSimpleTypeAttributes(Property property,
			AttributeDefinition attDef) throws XMLStreamException {
		if (property != null && attDef.isElement() // only elements may have properties
				&& !(property instanceof AttributeProperty)) { //XXX this is a dirty hack - find a better solution
			Collection<Property> properties = FeatureInspector.getProperties(property);
			if (properties != null && !properties.isEmpty()) {
				//XXX create dummy attribute for writeProperties TODO better: FeatureInspector must support Property
				ComplexAttribute ca = new ComplexAttributeImpl(properties, GMLSchema.ABSTRACTSTYLETYPE_TYPE, null);
				writeProperties(ca, attDef.getAttributeType(), false);
			}
		}
	}

	/**
	 * Write a geometry
	 * 
	 * @param geometry the geometry
	 * @param attributeType the type definition
	 * @param srsName the common SRS name, may be <code>null</code> 
	 * @throws XMLStreamException if an error occurs writing the geometry  
	 */
	private void writeGeometry(Geometry geometry, TypeDefinition attributeType, 
			String srsName) throws XMLStreamException {
		getGeometryWriter().write(writer, geometry, attributeType, srsName);
	}

	/**
	 * Get the geometry writer
	 * 
	 * @return the geometry writer instance to use 
	 */
	protected StreamGeometryWriter getGeometryWriter() {
		if (geometryWriter == null) {
			geometryWriter = StreamGeometryWriter.getDefaultInstance(gmlNs);
		}
		
		return geometryWriter;
	}
	
	/**
	 * Write a property attribute
	 * 
	 * @param value the attribute value, may be <code>null</code>
	 * @param attDef the attribute definition
	 * @throws XMLStreamException if writing the attribute fails 
	 */
	private void writeAttribute(Object value, 
			AttributeDefinition attDef) throws XMLStreamException {
		writeAttribute(writer, value, attDef);
	}

	/**
	 * Write a property attribute
	 * 
	 * @param writer the XML stream writer 
	 * @param value the attribute value, may be <code>null</code>
	 * @param attDef the attribute definition
	 * @throws XMLStreamException if writing the attribute fails 
	 */
	public static void writeAttribute(XMLStreamWriter writer, Object value, 
			AttributeDefinition attDef) throws XMLStreamException {
		if (value == null) {
			if (attDef.getMinOccurs() > 0) {
				if (!attDef.isNillable()) {
					log.warn("Non-nillable attribute " + attDef.getName() + " is null");
				}
				else {
					//XXX write null attribute?!
					writeAtt(writer, null, attDef);
				}
			}
		}
		else {
			writeAtt(writer, SimpleTypeUtil.convert(value, attDef.getAttributeType()), attDef);
		}
	}

	private static void writeAtt(XMLStreamWriter writer, String value, 
			AttributeDefinition attDef) throws XMLStreamException {
		String ns = attDef.getNamespace();
		if (ns != null && !ns.isEmpty()) {
			writer.writeAttribute(attDef.getNamespace(), attDef.getName(), 
					(value != null)?(value):(null));
		}
		else {
			// no namespace
			writer.writeAttribute(attDef.getName(), 
					(value != null)?(value):(null));
		}
	}

}
