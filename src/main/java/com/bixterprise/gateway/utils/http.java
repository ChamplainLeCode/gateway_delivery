package com.bixterprise.gateway.utils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.bixterprise.gateway.domain.AutomateAgents;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;


import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import java.util.HashMap;


/**
 *  Utilitaire de connection aux services distants sur http
 * @author haranov
 */
public class http {

	@Value("${http.firebase.url}")
	public static String FIREBASE_SERVER;
	
    @Value("${http.firebase.token}")
    public static String FIREBASE_TOKEN;

    /**
     *  This static function is used to send push notification to Mobile Agent
     * @param params HashMap<String, Object> formatted as firebase push notification params
     * @return A JSON Object is returned, if contains field success: 1 then notification have been successfully sent otherwise failure: 1
     */
    public static Object firebaseNotification(HashMap<String, Object> params) {
    	try {
		    URL http = new URL(FIREBASE_SERVER);
		    HttpURLConnection connection = (HttpURLConnection) http.openConnection();
		    
		    connection.setRequestProperty("Content-Type", "application/json; utf-8");
		    connection.setRequestProperty("Accept", "application/json");
		    
		    connection.setRequestMethod("POST");
		    connection.setDoOutput(true);
		    connection.setRequestProperty("Authorization", "Bearer " + FIREBASE_TOKEN);    
		    
		    if(params != null){
				try(OutputStream os = connection.getOutputStream()) {
					HashMap obj = new HashMap(params);
				    byte[] input = obj.toString().getBytes("utf-8");
				    os.write(input, 0, input.length);   
				}
		    }
		    try(BufferedReader br = new BufferedReader(
				new InputStreamReader(connection.getInputStream(), "utf-8"))) {
				  StringBuilder response = new StringBuilder();
				  String responseLine;
				  while ((responseLine = br.readLine()) != null) {
				      response.append(responseLine.trim());
				  }
				  return response.toString();
		    }
		    
		}catch(IOException  e){
			return new HashMap<>();
		}
    }


    
    
    /**
     *
     * @param url This url where data will be submitted througth GET's method
     * @param params Data to send
     * @return HashMap 
     * @throws IOException 
     */
    public static Object get(String url, HashMap params) throws IOException {
		try {
		    URL http = new URL(url);
		    HttpURLConnection connection = (HttpURLConnection) http.openConnection();
		    
		    connection.setRequestProperty("Content-Type", "application/json; utf-8");
		    connection.setRequestProperty("Accept", "application/json");
		    
		    connection.setRequestMethod("GET");
		    connection.setDoOutput(true);
		    
		    if(params != null){
                        try(OutputStream os = connection.getOutputStream()) {
                            byte[] input = params.toString().getBytes("utf-8");
                            os.write(input, 0, input.length);   
                        }
		    }
		    try(BufferedReader br = new BufferedReader(
				new InputStreamReader(connection.getInputStream(), "utf-8"))) {
				  StringBuilder response = new StringBuilder();
				  String responseLine;
				  while ((responseLine = br.readLine()) != null) {
				      response.append(responseLine.trim());
				  }
				  try {
					  return new JSONParser(response.toString());
				  }catch(Exception e) {
					  return new HashMap();
				  }
		    }
		    
		}catch(IOException  e){
		    throw new IOException(e.fillInStackTrace());
		}

    }

    /**
     *
     * @param url This url where data will be submitted througth POST's method
     * @param params Data to send
     * @return HashMap 
     */
    public static Object post(String url, HashMap params) {
    	try {
            URL http = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) http.openConnection();

            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            if(params != null){
                try(OutputStream os = connection.getOutputStream()) {
                    byte[] input = params.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);   
                }
            }

            try {    
                FileOutputStream logFirebase = new FileOutputStream("gateway_firebase_notification.txt", true);
                logFirebase.write(("\n\nPost Data  >> "+(new Date())+" >>> URL = "+url+" params = "+params.toString()+"").getBytes());
                logFirebase.close();
            }catch(Exception e){} 
            
            try(BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                          StringBuilder response = new StringBuilder();
                          String responseLine;
                          while ((responseLine = br.readLine()) != null) {
                              response.append(responseLine.trim());
                          }
                          //return response.toString();
                          try {
                        	  return new JSONParser(response.toString());
                          }catch(Exception e) {
                        	  return new HashMap();
                          }
            }

        }catch(Exception  e){
                HashMap<String, String> obj = new HashMap<>();
                obj.put("code", "400");
                obj.put("label", "Network connection error");
                obj.put("error", e.getMessage());
                return obj;
        }

    }

    /**
     *  This static method is used after registering new Agent to set up his mobile agent
     * that will be use for routing Gateway Request
     * @param a
     */
    public static synchronized void updateOperator(AutomateAgents a) {
        /**
         * On met à jour l'operateur téléphonique de l'agent
         */
        a.setPhoneOperator(
            /**
             * MTN Cameroon, Orange, NEXTTEL, UNKNOWN
             */
            getPhoneOperator(
                /**
                 * On retire les données superflues (237) (+237) (Blank Space)
                 */
                OperatorResolver.removeCountryCode(
                    a.getPhone()
                )
            )
        );
    }
    
    /**
     * Method used to check phone operator online for a specific phone number, 
     * Note that, actually we only check Cameroon phone.
     * @param phoneNumber
     * @return PhoneOperator
     * @see PhoneOperator
     */
    public static PhoneOperator getPhoneOperator (String phoneNumber) {
        return defaultPhoneOperatorScript(phoneNumber);
    }
    
//    {
//        try {
//            URL http = new URL("https://searchmobilenumber.com/info/"+phoneNumber+"-CM");
//            HttpURLConnection connection = (HttpURLConnection) http.openConnection();
//            connection.setRequestProperty("Content-Type", 
//                    "application/x-www-form-urlencoded");
//	         connection.setRequestProperty("Content-Language", "en-US"); 
//	         connection.setRequestProperty("User-Agent",
//                 "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11");
//
//            connection.setRequestMethod("GET");
//            connection.setDoOutput(true);
//
//            
//            try(BufferedReader br = new BufferedReader(
//                new InputStreamReader(connection.getInputStream(), "utf-8"))) {
//                  StringBuilder response = new StringBuilder();
//                  String responseLine;
//                  while ((responseLine = br.readLine()) != null) {
//                      response.append(responseLine.trim());
//                  }
//                  if(response.indexOf(PhoneOperator.MTN.toString())>=0){
//                      return PhoneOperator.MTN;
//                  }else if(response.indexOf(PhoneOperator.ORANGE.toString())>=0){
//                      return PhoneOperator.ORANGE;
//                  }else if(response.indexOf(PhoneOperator.NEXTTEL.toString())>=0){
//                      return PhoneOperator.NEXTTEL;
//                  }else
//                      return defaultPhoneOperatorScript(phoneNumber);
//            }catch(Exception e) {
//            	e.printStackTrace();
//            	return defaultPhoneOperatorScript(phoneNumber);
//            }
//        }catch(IOException  e){
//            e.printStackTrace();
//            return defaultPhoneOperatorScript(phoneNumber);
//        }
//    
//    }
    
    /**
     * In case where we could'n check phone operator through getPhoneOperator() method, 
     * we gonna try to use native way, Note that only Cameroon phone number is now supported
     * @param phone
     * @return return PhoneOperator
     * @see PhoneOperator
     */
    protected static PhoneOperator defaultPhoneOperatorScript(String phone) {
    	CharSequence start = phone.subSequence(0, 3);
    	if(start.charAt(0) != '6')
    		return PhoneOperator.UNKNOWN;
    	if(start.charAt(1) == '7' || start.charAt(1) == '8' || (start.charAt(1) == '5' && start.charAt(2)<'5') )
    		return PhoneOperator.MTN;
    	if(start.charAt(1) == '9' || (	start.charAt(1) == '5' && start.charAt(2)>= '5'))
			return PhoneOperator.ORANGE;
    	if(start.charAt(1) == '6')
    		return PhoneOperator.NEXTTEL;
    	return PhoneOperator.UNKNOWN;

    }


}
