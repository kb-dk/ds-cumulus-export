package dk.kb.ds.cumulus.export;

import dk.kb.cumulus.CumulusQuery;
import dk.kb.cumulus.CumulusRecord;
import dk.kb.cumulus.CumulusRecordCollection;
import dk.kb.cumulus.CumulusServer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public class CumulusExport {
    public static void main(String[] args) throws IOException, TransformerException, ParserConfigurationException {
//        System.out.println("Cumulus exporter");

        boolean writeAccess = false;
        try (CumulusServer server = new CumulusServer(Configuration.getCumulusConf())) {
            String myCatalog = Configuration.getCumulusConf().getCatalogs().get(0);
            CumulusQuery query = CumulusQuery.getQueryForAllInCatalog(myCatalog);
            CumulusRecordCollection recordCollection = server.getItems(myCatalog, query);

            System.out.println("<add>");
            for (CumulusRecord record : recordCollection) {
                System.out.println("<doc>");

                String name = record.getFieldValue("Record Name");
                String guid = record.getFieldValue("guid");
//                System.out.println("Record: " + name + " with GUID " + guid);
                System.out.println("<field name=\"id\">kb_image_" + myCatalog + "_" + guid + "</field>");

                CumulusRecord fullRecord = server.findCumulusRecordByName(myCatalog, name);
//                System.out.println(fullRecord);
//                record.writeFieldMetadata(System.out);
                System.out.println("<field name=\"title\">" + record.getFieldValue("Titel") + "</field>");

                System.out.println("</doc>");
            }
            System.out.println("</add>");
        }
    }
}
