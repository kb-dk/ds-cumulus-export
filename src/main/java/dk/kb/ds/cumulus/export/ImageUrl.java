package dk.kb.ds.cumulus.export;

public class ImageUrl {

    public static String makeUrl (String url) {
        // cumulus-core-test-01:/Depot/DAM/test/Samlingsbilleder/0000/375/526/KE030219.tif
        String path1[] = url.split(".tif");
        String path2[] = path1[0].split(":/");
        String path = path2[1].replace("Depot/DAMX/Online_Master_Arkiv", "DAMJP2/online_master_arkiv");
        url = "https://kb-images.kb.dk/" + path + "/full/!345,2555/0/native.jpg";
        return url;
    }
}
