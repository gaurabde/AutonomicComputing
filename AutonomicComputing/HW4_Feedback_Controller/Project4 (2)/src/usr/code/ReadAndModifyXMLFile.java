package usr.code;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ReadAndModifyXMLFile {

	public static final String xmlFilePath = "/home/cloudera/Downloads/fair-scheduler.xml";

	public static void main(String argv[]) throws InterruptedException {

		try {

			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

			Document document = documentBuilder.parse(xmlFilePath);

			// Get employee by tag name
			//use item(0) to get the first node with tage name "employee"
			Node pool = document.getElementsByTagName("pool").item(0);

			// loop the employee node and update salary value, and delete a node
			NodeList nodes = pool.getChildNodes();

			for (int i = 0; i < nodes.getLength(); i++) {

				Node element = nodes.item(i);

				if ("maxMaps".equals(element.getNodeName())) {
					element.setTextContent("100");
					System.out.println(element.getTextContent());
				}

			}
			int i=0;
			long starttime=System.currentTimeMillis();
			while(i<10) {
				System.out.println(((0.2+(float)i/10)/((float)(System.currentTimeMillis()-starttime)))*100000);
				i++;
				System.out.print((float)(i)/10);
				Thread.sleep(1000);
			}

			// write the DOM object to the file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();

			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(document);

			StreamResult streamResult = new StreamResult(new File(xmlFilePath));
			transformer.transform(domSource, streamResult);

			System.out.println("The XML File was ");

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SAXException sae) {
			sae.printStackTrace();
		}
	}
}