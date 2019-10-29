package dk.kb.ds.cumulus.export;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class ImageUrl {

    public static String makeUrl (String image_url) {
        try {
            Properties imagePro = new Properties();

            imagePro.load(ImageUrl.class.getClassLoader().getResourceAsStream("cumulusExport.properties"));
            String serverUrl = imagePro.getProperty("serverUrl");
            String cumulusPath = imagePro.getProperty("cumulusPath");
            String imagePath = imagePro.getProperty("imagePath");
            String imageResolution = imagePro.getProperty("imageResolution");

            String path1[] = image_url.split(".tif");
            String path2[] = path1[0].split(":/");
            if (path2.length > 1) {
                String path = path2[1].replace(cumulusPath, imagePath);
                image_url = serverUrl + path + "/full/!" + imageResolution + "/0/native.jpg";

                String http_url = image_url.replaceAll("https", "http");
                HttpURLConnection.setFollowRedirects(false);
                HttpURLConnection con = (HttpURLConnection) new URL(http_url).openConnection();
                con.setRequestMethod("HEAD");
                boolean ok_image = (con.getResponseCode() == HttpURLConnection.HTTP_OK);

                String image_path = ok_image?  image_url:null;
                return image_path;
            }
            else
                return null;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
