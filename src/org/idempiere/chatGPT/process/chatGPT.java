package org.idempiere.chatGPT.process;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.compiere.model.MSysConfig;
import org.compiere.util.Env;
import org.json.JSONArray;
import org.json.JSONObject;

public class chatGPT {
	
	public static String chatGPTconnect(String text) throws Exception {
		String		chatGPT_API_Key = MSysConfig.getValue("chatGPT_API_Key",Env.getAD_Client_ID(Env.getCtx()));
		
		        String url = "https://api.openai.com/v1/chat/completions";
		        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

		        con.setRequestMethod("POST");
		        con.setRequestProperty("Content-Type", "application/json");
		        con.setRequestProperty("Authorization", "Bearer " + chatGPT_API_Key);

		        String model = "gpt-4o";
		        JSONArray messages = new JSONArray();
		        JSONObject userMessage = new JSONObject();
		        userMessage.put("role", "user");
		        userMessage.put("content", text);
		        messages.put(userMessage);

		        int maxTokens = 4096;

		        con.setDoOutput(true);

		        System.out.println(con);

		        JSONObject requestBody = new JSONObject();
		        requestBody.put("model", model);
		        requestBody.put("messages", messages);
		        requestBody.put("max_tokens", maxTokens);

		        OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
		        writer.write(requestBody.toString());
		        writer.flush();

		        // Read the answer
			    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			    String inputLine;
		        StringBuilder response = new StringBuilder();
		        	while ((inputLine = in.readLine()) != null) {
		            response.append(inputLine);
		        	}
		        	in.close();
		        	
		        return extractMessageFromJSONResponse(response.toString());

	
    } 	
	   public static String extractMessageFromJSONResponse(String response) {
	       int start = response.indexOf("content")+ 11;

	       int end = response.indexOf("\"", start);

	       return response.substring(start, end);

	   }	

}