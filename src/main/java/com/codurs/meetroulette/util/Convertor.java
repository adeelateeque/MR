package com.codurs.meetroulette.util;

import com.codurs.meetroulette.ui.StaticObject;

/**
 * Created by MoistyBurger on 6/7/2014.
 */
public class Convertor {

    public static void midPoint(double lat1,double lon1,double lat2,double lon2){

        //convert to google cord





        double dLon = Math.toRadians(lon2 - lon1);

        //convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lon1 = Math.toRadians(lon1);

        double Bx = Math.cos(lat2) * Math.cos(dLon);
        double By = Math.cos(lat2) * Math.sin(dLon);
        double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
        double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

        //print out in degrees
        System.out.println(Math.toDegrees(lat3) + " " + Math.toDegrees(lon3));

        //convert to onemao cord


    }
}
