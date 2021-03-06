package intelligentroomclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Intelligent room: bright control by Aruino
 * 
 * @author tsm & Smilasek
 * tomszom.com
 */
public class URLConnectionReader {
    public String sendGetRequest(String address, String port, String params) {
        String response="";
        try {
            URL url = new URL("http://"+address+":"+port+"/"+params);
            URLConnection con = url.openConnection();
            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                    con.getInputStream()));
            String inputLine;            
            while ((inputLine = in.readLine()) != null){ 
                response+=inputLine;
            }
            in.close();
            
            
        } 
        catch (MalformedURLException ex) {
                return "Error: Can't connect to server - incorrect server address";
            }
        catch (IOException ex) {
            return "Error: Can't connect to server - IO error";
        }
        return response;
    }
}
