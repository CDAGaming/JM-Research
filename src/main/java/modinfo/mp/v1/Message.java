package modinfo.mp.v1;

import java.util.concurrent.*;
import modinfo.*;
import org.apache.logging.log4j.*;
import java.net.*;
import java.io.*;

public class Message implements Callable<Object>
{
    private final String endpoint;
    private final Payload payload;
    private final String userAgent;
    private final int retries;
    private final int connectionTimeout;
    private final int readTimeout;
    
    Message(final String endpoint, final Payload payload, final String userAgent, final int retries, final int connectionTimeout, final int readTimeout) {
        this.endpoint = endpoint;
        this.payload = payload;
        this.userAgent = userAgent;
        this.retries = retries;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }
    
    @Override
    public Object call() {
        final String defaultAgent = System.setProperty("http.agent", "");
        int remainingRetries = Math.max(1, this.retries);
        Exception exception = null;
        Integer responseCode = null;
        while (responseCode == null && remainingRetries > 0) {
            try {
                final String payloadString = this.payload.toUrlEncodedString();
                final URL url = new URL(this.endpoint);
                final HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("User-Agent", this.userAgent);
                con.setConnectTimeout(this.connectionTimeout);
                con.setReadTimeout(this.readTimeout);
                con.setUseCaches(false);
                con.setDoOutput(true);
                final DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(payloadString);
                wr.flush();
                wr.close();
                responseCode = con.getResponseCode();
            }
            catch (MalformedURLException ex) {
                exception = ex;
                ModInfo.LOGGER.log(Level.ERROR, "ModInfo got a bad URL: " + this.endpoint);
            }
            catch (IOException ex2) {
                exception = ex2;
                ModInfo.LOGGER.log(Level.ERROR, "ModInfo can't send message", (Throwable)ex2);
            }
            finally {
                --remainingRetries;
            }
        }
        if (defaultAgent != null && defaultAgent.length() > 0) {
            System.setProperty("http.agent", defaultAgent);
        }
        if (responseCode != null) {
            return Boolean.TRUE;
        }
        if (exception == null) {
            exception = new Exception("ModInfo got a null response from endpoint");
        }
        return exception;
    }
    
    public interface Callback
    {
        void onResult(final Object p0);
    }
}
