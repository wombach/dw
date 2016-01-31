

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.json.JSONObject;
import org.json.XML;
import org.opengroup.xsd.archimate.ModelType;
import org.ow2.orchestra.jaxb.bpmn.Definitions;
import org.xml.sax.SAXException;

import com.mongodb.util.JSON;

public class Main {

	private static final String ORG_OPENGROUP_XSD_ARCHIMATE = "org.opengroup.xsd.archimate";
	private static final String BPMN = "org.ow2.orchestra.jaxb.bpmn";

	public static void main(String[] args) throws JAXBException, SAXException, IOException {
		JAXBContext jaxbContext = JAXBContext.newInstance(ORG_OPENGROUP_XSD_ARCHIMATE);

		//		 org.reflections.Reflections reflections = new Reflections(ORG_OPENGROUP_XSD_ARCHIMATE);
		//
		//		 Set<Class<? extends Object>> allClasses = 
		//		     refs.getSubTypesOf(Object.class);
		//		Claslections[] classes = new Class[4]; 
		//
		//		JAXBContext jaxbContext = JAXBContext.newInstance(classes);

		File file = new File("OTK Sample.xml");
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		StreamSource source = new StreamSource(file);
		JAXBElement<ModelType> result = unmarshaller.unmarshal(source, ModelType.class);
		ModelType model = (ModelType) result.getValue();

		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//		marshaller.marshal(model, System.out);
		// Set the Marshaller media type to JSON or XML
		marshaller.setProperty(MarshallerProperties.MEDIA_TYPE,
				"application/json");
		// Set it to true if you need to include the JSON root element in the JSON output
		marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, true);
		// Set it to true if you need the JSON output to formatted
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		// Marshal the employee object to JSON and print the output to console
		ByteArrayOutputStream st = new ByteArrayOutputStream();
		marshaller.marshal(model, st);
		System.out.println(st.toString());
		
		ByteArrayInputStream in = new ByteArrayInputStream(st.toByteArray());
//		JSONObject jobj = XML.toJSONObject(st.toString());
		Unmarshaller unmarshaller2 = jaxbContext.createUnmarshaller();
		unmarshaller2.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
		StreamSource source2 = new StreamSource(in);
		result = unmarshaller2.unmarshal(source2, ModelType.class);
		model = (ModelType) result.getValue();


		
		
//		JAXBContext jaxbContextBPMN = JAXBContext.newInstance(BPMN);
//
//		com.sun.xml.internal.ws.wsdl.writer.document.Definitions res = JaxbUtil.unmarshalBpmn("gluehhaube_wo_subprocess.bpmn");
//		Unmarshaller unmarshallerB = jaxbContextBPMN.createUnmarshaller();
//		StreamSource sourceB = new StreamSource(fileB);
//		resultB = unmarshallerB.unmarshal(sourceB, org.ow2.orchestra.jaxb.bpmn.Definitions.class);
//		ModelType model = (ModelType) result.getValue();

		
	}

}
