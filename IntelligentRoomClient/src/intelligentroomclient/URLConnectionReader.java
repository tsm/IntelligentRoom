/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intelligentroomclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author tsm
 */
public class URLConnectionReader {
    public String sendGetRequest(String address, String params) {
        String response="";
        try {
            URL url = new URL(address+params);
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
                return "Error: incorrect server address";
            }
        catch (IOException ex) {
            return "Error: read or write error";
        }
        return response;
    }
}
