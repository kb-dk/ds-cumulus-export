package dk.kb.ds.cumulus.export;

import dk.kb.cumulus.CumulusQuery;
import dk.kb.cumulus.CumulusRecord;
import dk.kb.cumulus.CumulusRecordCollection;
import dk.kb.cumulus.CumulusServer;
import dk.kb.cumulus.utils.ArgumentCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

public class CumulusExport {
    // List of valid types
    private static List<String> listOfType = Arrays.asList("image", "moving_image", "sound", "text", "other");

    private static final Logger log = LoggerFactory.getLogger(CumulusExport.class);

    public static void main(String[] args) throws Exception {

        try (CumulusServer server = new CumulusServer(Configuration.getCumulusConf())) {
            //Select the first Cumulus catalog from the configuration
            String myCatalog = Configuration.getCumulusConf().getCatalogs().get(0);
            CumulusQuery query = CumulusQuery.getQueryForAllInCatalog(myCatalog);
            CumulusRecordCollection recordCollection = server.getItems(myCatalog, query);

            File outputFile = new File(Configuration.getOutputFile().toString());
            OutputStream out = new FileOutputStream(outputFile);
            ArgumentCheck.checkNotNull(out, "OutputStream out");
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document document = docBuilder.newDocument();

            Element rootElement = document.createElement("add");
            document.appendChild(rootElement);
            // Get configurations
            String collection = convertCollectionToSolrFormat(Configuration.getCollection().toString()) ;
            String type = getConfigurationType();
            for (CumulusRecord record : recordCollection) {
                Element docElement = document.createElement("doc");
                rootElement.appendChild(docElement);

                // Get  metadata from Cumulus
                String id = "ds_" + collection + "_" + record.getFieldValueOrNull("guid");
                String title = record.getFieldValueOrNull("Titel");
                String created_date = CalendarUtils.getUTCTime(record.getFieldValueForNonStringField("Item Creation Date"));
                String keyword = record.getFieldValueOrNull("Keywords");
                String subject = record.getFieldValueOrNull("Note");
                String license = record.getFieldValueForNonStringField("Copyright");
                String datetime = record.getFieldValueOrNull("År");  //CalendarUtils.getDateTime("??", record.getFieldValueOrNull("År"));
                String author = record.getFieldValueOrNull("Ophav");


                String[] attributeContent = {id, collection, type, title, created_date, keyword, subject, license,
                    datetime, author};
                String[] attributeName = {"id", "collection", "type", "title", "created_date", "keyword", "subject", "license",
                    "datetime", "author"};

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
            log.debug("Created " + outputFile + " as input for solr.");
        }
    }

    // Check for valid type
    static String getConfigurationType() {
        String retValue = Configuration.getType().toString();
        if (listOfType.contains(retValue)){
            return retValue;
        }
        return "other";
    }

    // Remove special chars and replace space with underscore
    static String convertCollectionToSolrFormat(String toSolr) {
        return toSolr
            .replaceAll("[^\\p{L}\\p{Z}]","")
            .replaceAll("\\s","_");
    }

}
