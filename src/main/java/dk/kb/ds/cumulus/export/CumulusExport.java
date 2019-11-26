package dk.kb.ds.cumulus.export;

import dk.kb.cumulus.CumulusQuery;
import dk.kb.cumulus.CumulusRecordCollection;
import dk.kb.cumulus.CumulusServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

public class CumulusExport {
    // List of valid types
    private static List<String> listOfType = Arrays.asList("image", "moving_image", "sound", "text", "other");

    private static final Logger log = LoggerFactory.getLogger(CumulusExport.class);
    private static final XMLOutputFactory xmlOutput = XMLOutputFactory.newFactory();

    static final String INDENTATION = "    ";
    static final String NEWLINE = "\n";

    public static void main(String[] args) throws Exception {

        try (CumulusServer server = new CumulusServer(Configuration.getCumulusConf())) {
            boolean limited = Boolean.parseBoolean(Configuration.getLimited());
            int counter = Integer.parseInt(Configuration.getCounter());

            //Select the first Cumulus catalog from the configuration
            String myCatalog = Configuration.getCumulusConf().getCatalogs().get(0);
            CumulusQuery query = CumulusQuery.getQueryForAllInCatalog(myCatalog);
            log.info("Requesting catalog '{}' with query '{}' from server", myCatalog, query);
            CumulusRecordCollection recordCollection = server.getItems(myCatalog, query);

            // Create output file and stream for XML
            File outputFile = new File(Configuration.getOutputFile());
            try (FileOutputStream fos = new FileOutputStream(outputFile)){
                // Initializing XML
                XMLStreamWriter xmlWriter = xmlOutput.createXMLStreamWriter(fos, "utf-8");
                xmlWriter.writeStartDocument("UTF-8", "1.0");
                xmlWriter.writeCharacters(NEWLINE);
                xmlWriter.writeStartElement("add");
                xmlWriter.writeCharacters(NEWLINE);

                // collection and type are mandatory fields in the Digisam Solr setup
                final FieldMapper fieldMapper = new FieldMapper();
                fieldMapper.putStatic("collection", convertCollectionToSolrFormat(Configuration.getCollection()));
                fieldMapper.putStatic("type", getConfigurationType());

                StreamSupport.stream(recordCollection.spliterator(), false).
                    limit(limited ? counter : Long.MAX_VALUE). // For testing purposes
                    map(fieldMapper).                          // Cumulus record -> FieldValues object
                    filter(Objects::nonNull).                  // Records that failed conversion are propagated as null
                    forEach(fv -> fv.toXML(xmlWriter));        // Populating XML

                // Ending XML
                xmlWriter.writeEndDocument(); // add
                xmlWriter.writeCharacters(NEWLINE);
                xmlWriter.flush();
                log.debug("Created " + outputFile + " as input for solr.");
            }
        }
    }

    // Check for valid type
    static String getConfigurationType() {
        String retValue = Configuration.getType();
        if (listOfType.contains(retValue)){
            return retValue;
        }
        log.warn("The stated collection type '{}' was not on the approved list({}) and was substituted with 'other'",
                 Configuration.getType(), listOfType);
        return "other";
    }

    // Remove special chars and replace space with underscore
    static String convertCollectionToSolrFormat(String toSolr) {
        return toSolr
            .replaceAll("[^\\p{L}\\p{Z}]","")
            .replaceAll("\\s","_");
    }

}
