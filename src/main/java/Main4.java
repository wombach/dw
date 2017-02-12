import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.opengroup.xsd.archimate._3.BusinessProcess;
import org.opengroup.xsd.archimate._3.DataType;
import org.opengroup.xsd.archimate._3.ElementType;
import org.opengroup.xsd.archimate._3.ElementsType;
import org.opengroup.xsd.archimate._3.LangStringType;
import org.opengroup.xsd.archimate._3.ModelType;
import org.opengroup.xsd.archimate._3.ObjectFactory;
import org.opengroup.xsd.archimate._3.PropertiesType;
import org.opengroup.xsd.archimate._3.PropertyDefinitionType;
import org.opengroup.xsd.archimate._3.PropertyDefinitionsType;
import org.opengroup.xsd.archimate._3.PropertyType;

public class Main4 {
	//private static final String ORG_OPENGROUP_XSD_ARCHIMATE3 = "org.opengroup.xsd.archimate._3";

	public static void main(String[] args) throws JAXBException, FileNotFoundException, javax.xml.bind.JAXBException {
		ObjectFactory fac = new ObjectFactory();
		ModelType modelT = fac.createModelType();
		JAXBElement<ModelType> model = fac.createModel(modelT);
		modelT.setIdentifier("test");
		PropertyDefinitionsType defs = fac.createPropertyDefinitionsType();
		List<PropertyDefinitionType> list = defs.getPropertyDefinition();
		PropertyDefinitionType def = fac.createPropertyDefinitionType();
		String id = "hallo12";
		def.setIdentifier(id);
		def.setType(DataType.NUMBER);
		list.add(def);
		def = fac.createPropertyDefinitionType();
		def.setIdentifier("hallo13");
		def.setType(DataType.NUMBER);
		list.add(def);
		modelT.setPropertyDefinitions(defs);
		
		ElementsType elems = fac.createElementsType();
		modelT.setElements(elems);
		List<ElementType> elemList = elems.getElement();
		BusinessProcess elm = fac.createBusinessProcess();
		elemList.add(elm);
		elm.setIdentifier("test2");
		PropertiesType props = fac.createPropertiesType();
		PropertyType prop = fac.createPropertyType();
		List<PropertyType> list2 = props.getProperty();
		list2.add(prop);
		elm.setProperties(props);
		prop.setPropertyDefinitionRef(def);
		List<LangStringType> vals = prop.getValue();
		LangStringType val = fac.createLangStringType();
		val.setValue("test3");
		val.setLang("en");
		vals.add(val);
//		modelT.setElements(elems);
		
		InputStream iStream = Main4.class.getClassLoader().getResourceAsStream("META-INF/binding.xml");
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(JAXBContextProperties.OXM_METADATA_SOURCE, iStream);
		Map<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("http://www.w3.org/2001/XMLSchema-instance", "ns1");
		namespaces.put("namespace2", "ns2");
//		jsonMarshaller.setProperty(MarshallerProperties.NAMESPACE_PREFIX_MAPPER, namespaces);
//		jsonUnmarshaller.setProperty(UnmarshallerProperties.JSON_NAMESPACE_PREFIX_MAPPER, namespaces);
		
//		JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] {ModelType.class},properties);
		JAXBContext jaxbContext =  JAXBContext.newInstance(ModelType.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.setProperty(MarshallerProperties.NAMESPACE_PREFIX_MAPPER, namespaces);
		marshaller.setProperty(MarshallerProperties.JSON_NAMESPACE_SEPARATOR, '_');
//		marshaller.setProperty(MarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@") ;
//		marshaller.marshal(model, System.out);
		// Set the Marshaller media type to JSON or XML
		marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
		// Set it to true if you need to include the JSON root element in the JSON output
		marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, true);
		// Set it to true if you need the JSON output to formatted
//		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		// Marshal the employee object to JSON and print the output to console
		ByteArrayOutputStream st = new ByteArrayOutputStream();
		marshaller.marshal(model, st);
		System.out.println(st.toString());

		ByteArrayInputStream in = new ByteArrayInputStream(st.toByteArray());
//		JSONObject jobj = XML.toJSONObject(st.toString());
		Unmarshaller unmarshaller2 = jaxbContext.createUnmarshaller();
		unmarshaller2.setProperty(UnmarshallerProperties.JSON_NAMESPACE_PREFIX_MAPPER, namespaces);
		unmarshaller2.setProperty(UnmarshallerProperties.JSON_NAMESPACE_SEPARATOR, '_');
		unmarshaller2.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
//		unmarshaller2.setProperty(UnmarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@") ;
		StreamSource source2 = new StreamSource(in);
		JAXBElement<ModelType> result = unmarshaller2.unmarshal(source2, ModelType.class);
		ModelType model2 = (ModelType) result.getValue();

		jaxbContext =  JAXBContext.newInstance(ModelType.class);
		marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(model, System.out);
//		
//
////		System.out.println(model.equals(model2));
//
	}

}
