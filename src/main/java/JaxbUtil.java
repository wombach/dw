/**
 * Bull SAS / OW2 Orchestra
 * Copyright (C) 2010 Bull S.A.S, and individual contributors as indicated
 * by the @authors tag.
 *
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 **/
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.ow2.orchestra.jaxb.bpmn.marshaller.ExpressionUnmarshallerListener;
import org.ow2.orchestra.jaxb.bpmn.marshaller.NamespacePrefixMapper;
import org.xml.sax.SAXException;

import com.sun.xml.internal.ws.wsdl.writer.document.Definitions;

/**
 * Utility class used to marshal and unmarshal documents from bpmn-jaxb-model
 *
 * @author Loic Albertin
 */
public final class JaxbUtil {

  private static final String JAXB_CONTEXT =
    "org.ow2.orchestra.jaxb.bpmn:org.ow2.orchestra.jaxb.bpmn.bpmndi:org.ow2.orchestra.jaxb.bpmn.di:org.ow2.orchestra.jaxb.bpmn.dc";

  /**
   * Utility classes should not have public or default constructors.
   */
  private JaxbUtil() {

  }

  /**
   * marshals a {@link Definitions} object into a file using JAXB
   *
   * @param definitions the object to marshal
   * @param outputFile  the file into which definitions will be marshaled.
   * @throws JAXBException Exception throws by JAXB
   * @throws SAXException  Exception related to XML schemas
   * @throws IOException
   */
  public static void marshalBpmn(final Definitions definitions, final File outputFile) throws JAXBException, SAXException, IOException {
    final String bpmnAsString = JaxbUtil.marshalBpmnToString(definitions);

    //Misc.write(bpmnAsString, outputFile);
    System.out.println(bpmnAsString);
  }

  /**
   * marshals a {@link Definitions} object into a file using JAXB
   *
   * @param definitions the object to marshal
   * @param outputFile  the file into which definitions will be marshaled.
   * @throws JAXBException Exception throws by JAXB
   * @throws SAXException  Exception related to XML schemas
   */
  public static String marshalBpmnToString(final Definitions definitions) throws JAXBException, SAXException {
    final JAXBContext jc = JAXBContext.newInstance(JaxbUtil.JAXB_CONTEXT);
    final Marshaller marshaller = jc.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

    marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper());

    final SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
    final Schema schema = sf.newSchema(Definitions.class.getClassLoader().getResource("BPMN20.xsd"));
    marshaller.setSchema(schema);

    final StringWriter stringWriter = new StringWriter();
    marshaller.marshal(definitions, stringWriter);

    return stringWriter.toString();
  }

  /**
   * unmarshals a {@link byte[]} into a {@link Definitions} object using JAXB.
   *
   * @param content The {@link byte[]} of the content to parse.
   * @return Returns the corresponding {@link Definitions} object
   * @throws JAXBException Exception throws by JAXB
   * @throws SAXException  Exception related to XML schemas
   */
  public static Definitions unmarshalBpmn(final byte[] content) throws JAXBException, SAXException {
    final JAXBContext jc = JAXBContext.newInstance(JaxbUtil.JAXB_CONTEXT);
    final Unmarshaller unmarshaller = jc.createUnmarshaller();

    final SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
    final Schema schema = sf.newSchema(Definitions.class.getClassLoader().getResource("BPMN20.xsd"));
    unmarshaller.setSchema(schema);
    unmarshaller.setListener(new ExpressionUnmarshallerListener());
    return (Definitions) unmarshaller.unmarshal(new ByteArrayInputStream(content));
  }


  /**
   * unmarshals an {@link URL} into a {@link Definitions} object using JAXB.
   *
   * @param url The {@link URL} used to locate the file to parse.
   * @return Returns the corresponding {@link Definitions} object
   * @throws JAXBException Exception throws by JAXB
   * @throws SAXException  Exception related to XML schemas
   * @throws IOException   Exception thrown if he parser failed to reader content from the url.
   */
  public static Definitions unmarshalBpmn(final String filename) throws JAXBException, SAXException, IOException {
		File file = new File(filename);
		StreamSource source = new StreamSource(file);
    return JaxbUtil.unmarshalBpmn(source.getReader().toString());
  }

  /**
   * @param file The file to parse.
   * @return Returns the corresponding {@link Definitions} object
   * @throws JAXBException         Exception throws by JAXB
   * @throws SAXException          Exception related to XML schemas
   * @throws IOException           Exception thrown if he parser failed to reader content from the file.
   * @see {@link JaxbUtil#unmarshalBpmn(URL)}
   */
//  public static Definitions unmarshalBpmn(final File file) throws JAXBException, SAXException, IOException {
//    return JaxbUtil.unmarshalBpmn(Misc.getAllContentFrom(file));
//  }


}
