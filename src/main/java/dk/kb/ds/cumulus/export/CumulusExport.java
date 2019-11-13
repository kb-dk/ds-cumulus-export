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
import java.util.stream.StreamSupport;

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
            log.debug("Requesting catalog '{}' with query '{}' from server", myCatalog, query);
            CumulusRecordCollection recordCollection = server.getItems(myCatalog, query);

            File outputFile = new File(Configuration.getOutputFile());
            OutputStream out = new FileOutputStream(outputFile);
            ArgumentCheck.checkNotNull(out, "OutputStream out");
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document document = docBuilder.newDocument();

            Element rootElement = document.createElement("add");
            document.appendChild(rootElement);

            final FieldMapper fieldMapper = new FieldMapper();
            fieldMapper.putStatic("collection", convertCollectionToSolrFormat(Configuration.getCollection()));
            fieldMapper.putStatic("type", getConfigurationType());

            StreamSupport.stream(recordCollection.spliterator(), false).
                limit(limited ? counter : Long.MAX_VALUE).
                map(fieldMapper).
                forEach(fv -> fv.toDoc(rootElement));

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

    private static String getFallbackString(String fallbackString, String calcTime) {
        if (calcTime == null) {
            return fallbackString;
        }
        return null;
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
