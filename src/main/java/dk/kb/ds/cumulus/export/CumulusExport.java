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

        boolean writeAccess = false;
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
                    String title = record.getFieldValue("Titel");
                    String guid = record.getFieldValue("guid");
//                    String date = record.getFieldValue("Date Time Digitized");

                    Element fieldElement = document.createElement("field");
                    docElement.appendChild(fieldElement);
                    Element nameElement = document.createElement("name");
                    fieldElement.appendChild(nameElement);
                    Element idElement = document.createElement("id");
                    idElement.appendChild(document.createTextNode("kb_image_" + "_" + guid));
                    Element titleElement = document.createElement("title");
                    titleElement.appendChild(document.createTextNode(title));
//                    Element dateElement = document.createElement("date");
//                    dateElement.appendChild(document.createTextNode(date));
                    nameElement.appendChild(idElement);
                    nameElement.appendChild(titleElement);
//                    nameElement.appendChild(dateElement);

                } catch (Exception e) {
                    System.err.println(e);
//                    break;
                }
            }
                    // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
        }
    }
}
