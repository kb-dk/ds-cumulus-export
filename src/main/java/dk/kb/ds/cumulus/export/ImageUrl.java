package dk.kb.ds.cumulus.export;

import java.net.HttpURLConnection;
import java.net.URL;

public class ImageUrl {

    public static String makeUrl (String image_url) {
        String path1[] = image_url.split(".tif");
        String path2[] = path1[0].split(":/");
        String path = path2[1].replace("Depot/DAMX/Online_Master_Arkiv", "DAMJP2/online_master_arkiv");
        image_url = "https://kb-images.kb.dk/" + path + "/full/!345,2555/0/native.jpg";

        try {
            String http_url = image_url.replaceAll("https", "http");
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) new URL(http_url).openConnection();
            con.setRequestMethod("HEAD");
            boolean ok_image = (con.getResponseCode() == HttpURLConnection.HTTP_OK);

            String image_path = ok_image?  image_url:null;
            return image_path;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
