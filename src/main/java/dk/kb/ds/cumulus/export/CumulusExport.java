package dk.kb.ds.cumulus.export;

import dk.kb.cumulus.CumulusQuery;
import dk.kb.cumulus.CumulusRecord;
import dk.kb.cumulus.CumulusRecordCollection;
import dk.kb.cumulus.CumulusServer;
import dk.kb.cumulus.utils.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CumulusExport {
    public static void main(String[] args) throws IOException, TransformerException, ParserConfigurationException {

        try (CumulusServer server = new CumulusServer(Configuration.getCumulusConf())) {
            String myCatalog = Configuration.getCumulusConf().getCatalogs().get(0);
            CumulusQuery query = CumulusQuery.getQueryForAllInCatalog(myCatalog);
            CumulusRecordCollection recordCollection = server.getItems(myCatalog, query);

            File outputFile = new File("solrInputFile.xml");
            OutputStream out = new FileOutputStream(outputFile);

            ArgumentCheck.checkNotNull(out, "OutputStream out");
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document document = docBuilder.newDocument();
            Element rootElement = document.createElement("add");
            document.appendChild(rootElement);

            for (CumulusRecord record : recordCollection) {
                Element docElement = document.createElement("doc");
                rootElement.appendChild(docElement);
                try {
                    String id = record.getFieldValueOrNull("guid");
                    String titleC = record.getFieldValueOrNull("Titel");
                    String dateC = record. getFieldValueForNonStringField("Date Time Digitized");
                    String categoriesC = record.getFieldValueOrNull("Categories");
                    String topicC = record.getFieldValueOrNull("Emneord");
                    String copyrightC = record.getFieldValueOrNull("Copyright Notice");

                    String[] attributeContent = {id, titleC, dateC, categoriesC, topicC, copyrightC};
                    String[] attributeName = {"id", "title", "date", "categories", "topic", "copyright"};

                    for (int i = 0; i < attributeName.length; i++) {
                        if (attributeContent[i] != null) {
                            Element fieldElement = document.createElement("field");
                            fieldElement.setAttribute("name", attributeName[i]);
                            docElement.appendChild(fieldElement);
                            fieldElement.appendChild(document.createTextNode(attributeContent[i]));
                        }
                    }
                } catch (Exception e) {
                    System.err.println(e); // Just during development
                }
            }
                    // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
        }
    }
}
