import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class Util {
	public static String createXMLString(Document document) {
		StringWriter writer = new StringWriter();
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
			transformer.transform(new DOMSource(document), new StreamResult(writer));
		} catch (TransformerException e) {
			e.printStackTrace();
		}

		return writer.toString();
	}
}
