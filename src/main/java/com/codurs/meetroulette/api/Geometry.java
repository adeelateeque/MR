package com.codurs.meetroulette.api;

import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

/**
 * Created by MoistyBurger on 6/7/2014.
 */
public class Geometry implements Runnable {

    double x;
    double y;
    String out;
    String in;


    public Geometry(double x, double y, int i) {
        this.x=x;
        this.y=y;

        if(i==0) {
            out="4326";
            in="3414";
        }
        else
        {
            in="4326";
            out="3414";
        }
    }
    @Override
    public void run() {

        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = null;


        String url = "http://www.onemap.sg/omgeom/omgeom.svc/GetOutput?token=qo/s2TnSUmfLz+32CvLC4RMVkzEFYjxqyti1KhByvEacEdMWBpCuSSQ+IFRT84QjGPBCuz/cBom8PfSm3GjEsGc8PkdEEOEr&Param=project|inSR="+in+"|outSR="+out+"|geometries="+x+","+y+"|f=pjson";

        httpget = new HttpGet(url);

        Log.i("REQUEST IS :", httpget.getURI().toString());




        //EXCUTE REQUEST
        HttpResponse response;
        try {
            response = httpclient.execute(httpget);
            //PRINT OUT THE RESPONSE
            Log.i("RESPONSE :",response.getStatusLine().toString());
            //PASS THE RESPONSE TO THE EXTRACTOR
            //JSONExtractor paser= new JSONExtractor();
            //paser.ExtractLostRequest(response);

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }// catch (JSONException e) {e.printStackTrace();}
    }
}
