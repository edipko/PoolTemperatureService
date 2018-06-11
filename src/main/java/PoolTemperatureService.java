import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.*;


// The Java class will be hosted at the URI path "/pooltemp"
@Path("/pool")
public class PoolTemperatureService {
    private String BASE_DIR = "/sys/bus/w1/devices";

    @Context
    private ResourceInfo resourceInfo;


    private String getTemperature(File temperatureFile) {
        try {
            FileReader fileReader = new FileReader(temperatureFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            String temperature = "";
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("t=")) {
                    temperature = line.split("=")[1];
                }
            }
            fileReader.close();
            return temperature;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @GET
    @Path("/temps")
    // The Java method will produce content identified by the MIME Media type "text/plain"
    @Produces("text/plain")
    public Response getTemperature(@QueryParam("location") String location) {

        //  The pool temperature readings are accessed  by  reading the contents of
        //  a file located in /sys/bus/w1/devices/28-
        // This is needed

        String airTemp = getTemperature(new File(BASE_DIR + "/28-800000281f91/w1_slave"));
        Double airTemp_celsius = Double.valueOf(airTemp) / 1000;
        Double airTemp_farenheit = airTemp_celsius * 9 / 5 + 32;

        String poolTemp = getTemperature(new File(BASE_DIR + "/28-800000282739/w1_slave"));
        Double poolTemp_celsius = Double.valueOf(poolTemp) / 1000;
        Double poolTemp_farennheit = poolTemp_celsius * 9 / 5 + 32;


        if (location != null) {
            if (location.toLowerCase().equals("pool")) {
                JSONObject json = new JSONObject();
                try {
                    json.put("pool", poolTemp_farennheit);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return Response
                        .status(200)
                        .entity(json.toString()).build();
            } else {
                if (location.toLowerCase().equals("air")) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("air", airTemp_farenheit);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return Response
                            .status(200)
                            .entity(json.toString()).build();
                } else {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("pool", poolTemp_farennheit);
                        json.put("air", airTemp_farenheit);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {


                        return Response
                                .status(200)
                                .entity(json.toString()).build();
                    }
                }
            }
        }
        JSONObject json = new JSONObject();
        try {
            json.put("pool", poolTemp_farennheit);
            json.put("air", airTemp_farenheit);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            return Response
                    .status(200)
                    .entity(json.toString()).build();
        }
    }
}