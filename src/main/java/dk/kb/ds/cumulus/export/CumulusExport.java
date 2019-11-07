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
import java.util.Properties;

public class CumulusExport {
    // List of valid types
    private static List<String> listOfType = Arrays.asList("image", "moving_image", "sound", "text", "other");

    private static final Logger log = LoggerFactory.getLogger(CumulusExport.class);

    public static void main(String[] args) throws Exception {

        try (CumulusServer server = new CumulusServer(Configuration.getCumulusConf())) {
            Properties exportpro = new Properties();

            exportpro.load(CumulusExport.class.getClassLoader().getResourceAsStream("cumulusExport.properties"));
            boolean limited = Boolean.parseBoolean(exportpro.getProperty("limited"));
            int counter = Integer.parseInt(exportpro.getProperty("counter"));

            //Select the first Cumulus catalog from the configuration
            String myCatalog = Configuration.getCumulusConf().getCatalogs().get(0);
            CumulusQuery query = CumulusQuery.getQueryForAllInCatalog(myCatalog);
            CumulusRecordCollection recordCollection = server.getItems(myCatalog, query);

            File outputFile = new File(Configuration.getOutputFile());
            OutputStream out = new FileOutputStream(outputFile);
            ArgumentCheck.checkNotNull(out, "OutputStream out");
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document document = docBuilder.newDocument();

            Element rootElement = document.createElement("add");
            document.appendChild(rootElement);
            // Get configurations
            String collection = convertCollectionToSolrFormat(Configuration.getCollection()) ;
            String type = getConfigurationType();
            String created_date_verbatim = null;
            String datetime_verbatim = null;
            String image_url = "";

            int loop_counter = 0;
            for (CumulusRecord record : recordCollection) {
                Element docElement = document.createElement("doc");
                rootElement.appendChild(docElement);

                // Get  metadata from Cumulus
                String id = "ds_" + collection + "_" + record.getFieldValueOrNull("guid");
                String title = record.getFieldValueOrNull("Titel");
                String creationDateFromCumulus = record.getFieldValueForNonStringField("Item Creation Date");
                String created_date = CalendarUtils.getUTCTime(creationDateFromCumulus);
                created_date_verbatim = getFallbackString(created_date_verbatim, creationDateFromCumulus, created_date);
                String keyword = record.getFieldValueOrNull("Categories");
                String subject = record.getFieldValueOrNull("Emneord");
                String license = record.getFieldValueForNonStringField("Copyright");
                String url = record.getAssetReference("Asset Reference").getPart(0).getDisplayString();
                if (url != null || url != "") {
                    image_url = ImageUrl.makeUrl(url);
                }
                String datetimeFromCumulus = record.getFieldValueOrNull("År");
                String datetime = CalendarUtils.getUTCTime(datetimeFromCumulus, false);
                datetime_verbatim = getFallbackString(datetime_verbatim, datetimeFromCumulus, datetime);
                String author = record.getFieldValueOrNull("Ophav");

                final String[][] ATTRIBUTES = new String[][]{
                    {id, "id"},
                    {collection, "collection"},
                    {type, "type"},
                    {title, "title"},
                    {created_date, "created_date"},
                    {created_date_verbatim, "created_date_verbatim"},
                    {keyword, "keyword"},
                    {subject, "subject"},
                    {license, "license"},
                    {image_url, "image_url"},
                    {datetime, "datetime"},
                    {datetime_verbatim, "datetime_verbatim"},
                    {author, "author"}
                };
                //Add  attributes to xml-file
                for ( String[] attributes : ATTRIBUTES){
                    if (attributes[0] != null){
                        Element fieldElement = document.createElement("field");
                        fieldElement.setAttribute("name", attributes[1]);
                        docElement.appendChild(fieldElement);
                        fieldElement.appendChild(document.createTextNode(attributes[0]));
                    }
                }
                // TODO: remember to set limited to false in production
                loop_counter++;
                if (limited && (loop_counter >= counter))
                    break;
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

    private static String getFallbackString(String fallbackString, String str, String calcTime) {
        if (calcTime == null) {
            fallbackString = str;
        }
        return fallbackString;
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
