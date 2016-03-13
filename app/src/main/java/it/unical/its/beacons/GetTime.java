package it.unical.its.beacons;

/**
 * Created by Daniele on 13/03/2016.
 */
import java.io.IOException;
import org.apache.commons.net.time.TimeTCPClient;

public final class GetTime {

    public static final void main(String[] args) {
        try {
            TimeTCPClient client = new TimeTCPClient();
            try {
                // Set timeout of 60 seconds
                client.setDefaultTimeout(60000);
                // Connecting to time server
                // Other time servers can be found at : http://tf.nist.gov/tf-cgi/servers.cgi#
                // Make sure that your program NEVER queries a server more frequently than once every 4 seconds
                client.connect("nist.time.nosc.us");
                System.out.println(client.getDate());
            } finally {
                client.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
