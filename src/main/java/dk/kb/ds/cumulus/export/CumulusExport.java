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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class CumulusExport {
    public static void main(String[] args) throws Exception {

        try (CumulusServer server = new CumulusServer(Configuration.getCumulusConf())) {
            //Select the first Cumulus catalog from the configuration
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

                //TODO: get those two from the correct place
                String collection = "Samlingsbilleder";
                String type = "image";

                // Get  metadata from Cumulus
                String id = record.getFieldValueOrNull("guid");
                String title = record.getFieldValueOrNull("Titel");
                String tmpCreationDate = record.getFieldValueForNonStringField("Item Creation Date");
                String created_date = getUTCTime(tmpCreationDate);
                String keyword = record.getFieldValueOrNull("Categories");
                String subject = record.getFieldValueOrNull("Emneord");
                String license = record.getFieldValueOrNull("Copyright Notice");


                String[] attributeContent = {id, collection, type, title, created_date, keyword, subject, license};
                String[] attributeName = {"id", "collection", "type", "title", "created_date", "keyword", "subject", "license"};

                //Add the fields above to xml-file
                for (int i = 0; i < attributeName.length; i++) {
                    if (attributeContent[i] != null) {
                        Element fieldElement = document.createElement("field");
                        fieldElement.setAttribute("name", attributeName[i]);
                        docElement.appendChild(fieldElement);
                        fieldElement.appendChild(document.createTextNode(attributeContent[i]));
                    }
                }
            }
            // save the content to xml-file with specific formatting
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
        }
    }

    static String getUTCTime(String created_date_tmp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ccc LLL dd HH:mm:ss zzz yyy");
        LocalDateTime created_date_formatted = LocalDateTime.parse(created_date_tmp, formatter);
        LocalDateTime created_date_UTC = created_date_formatted.atZone(ZoneId.of("Europe/Copenhagen"))
            .withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        DateTimeFormatter isotimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return created_date_UTC.format(isotimeFormatter);
    }
}
