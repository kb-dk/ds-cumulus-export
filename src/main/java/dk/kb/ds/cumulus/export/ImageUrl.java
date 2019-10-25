package dk.kb.ds.cumulus.export;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;

public class ImageUrl {

    public static String makeUrl (String image_url) {
        // cumulus-core-test-01:/Depot/DAM/test/Samlingsbilleder/0000/375/526/KE030219.tif
        String path1[] = image_url.split(".tif");
        String path2[] = path1[0].split(":/");
        String path = path2[1].replace("Depot/DAMX/Online_Master_Arkiv", "DAMJP2/online_master_arkiv");
        image_url = "https://kb-images.kb.dk/" + path + "/full/!345,2555/0/native.jpg";
        /*
        try {
            HttpsURLConnection.setFollowRedirects(false);
            HttpsURLConnection con =
                (HttpsURLConnection) new URL(image_url).openConnection();
            con.setRequestMethod("HEAD");
            boolean ok_image = (con.getResponseCode() == HttpsURLConnection.HTTP_OK);

            String image_path = ok_image?  image_url:"null";
            System.out.println("Path is : " +image_path);
            return image_path;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

         */
        return image_url;
    }
}
