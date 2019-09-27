package dk.kb.ds.cumulus.export;

import dk.kb.cumulus.CumulusQuery;
import dk.kb.cumulus.CumulusRecord;
import dk.kb.cumulus.CumulusRecordCollection;
import dk.kb.cumulus.CumulusServer;

import java.io.File;
import java.io.IOException;

public class CumulusExport {
    public static void main(String[] args) throws IOException {
//        System.out.println("Cumulus exporter");

        boolean writeAccess = false;
        try (CumulusServer server = new CumulusServer(Configuration.getCumulusConf())) {
            String myCatalog = String.valueOf(Configuration.getCumulusConf().getCatalogs());
            CumulusQuery query = CumulusQuery.getQueryForAllInCatalog(myCatalog);
            CumulusRecordCollection recordCollection = server.getItems(myCatalog, query);

            for (CumulusRecord record : recordCollection) {
                String name = record.getFieldValue("Record Name");
                File f = record.getFile();
                record.setStringValueInField("status", "We found record named '" + name +
                    "' with file at location: " + f.getAbsolutePath());
            }
        }
    }
}
