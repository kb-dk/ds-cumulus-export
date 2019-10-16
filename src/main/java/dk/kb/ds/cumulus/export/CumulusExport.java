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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class CumulusExport {
    // List of valid types
    private static List<String> listOfType = Arrays.asList("image", "moving_image", "sound", "text", "other");

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
                String creationDate = record.getFieldValueForNonStringField("Item Creation Date");
                String created_date = getUTCTime(creationDate);
                String keyword = record.getFieldValueOrNull("Categories");
                String subject = record.getFieldValueOrNull("Emneord");
                String license = record.getFieldValueOrNull("Copyright Notice");
                String image_url = record.getAssetReference("Asset Reference").getPart(0).getDisplayString();

                String[] attributeContent = {id, collection, type, title, created_date, keyword, subject, license, image_url};
                String[] attributeName = {"id", "collection", "type", "title", "created_date", "keyword", "subject", "license", "image_url"};

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

    // Check for valid type
    static String getConfigurationType() {
        String retValue = Configuration.getType().toString();
        if (listOfType.contains(retValue)){
            return retValue;
        }
        return "other";
    }

    // Remove special chars and replace space with underscore
    static String convertCollectionToSolrFormat(String toString) {
        String tmp = toString.replaceAll("[^\\p{L}\\p{Z}]","");
        return tmp.replaceAll("\\s","_");
    }

    static String getUTCTime(String createdDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ccc LLL dd HH:mm:ss zzz yyy");
        LocalDateTime createdDateFormatted = LocalDateTime.parse(createdDate, formatter);
        LocalDateTime createdDateUTC = createdDateFormatted.atZone(ZoneId.of("Europe/Copenhagen"))
            .withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        DateTimeFormatter isoTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return createdDateUTC.format(isoTimeFormatter);
    }
}
