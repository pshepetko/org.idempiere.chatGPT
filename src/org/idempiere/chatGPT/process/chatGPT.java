package org.idempiere.chatGPT.process;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.compiere.model.MSysConfig;
import org.compiere.util.Env;
import org.json.JSONArray;
import org.json.JSONObject;

public class chatGPT {
	
	public static String chatGPTconnect(String text) throws Exception {
		String		chatGPT_API_Key = MSysConfig.getValue("chatGPT_API_Key",Env.getAD_Client_ID(Env.getCtx()));
		
		String url = "https://api.openai.com/v1/completions";
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + chatGPT_API_Key);

        JSONObject data = new JSONObject();
        data.put("model", "text-davinci-003");
        data.put("prompt", text);
        data.put("max_tokens", 4000);
        data.put("temperature", 1.0);

        con.setDoOutput(true);
        con.getOutputStream().write(data.toString().getBytes());

        String output = new BufferedReader(new InputStreamReader(con.getInputStream())).lines()
                .reduce((a, b) -> a + b).get();

       return (new JSONObject(output).getJSONArray("choices").getJSONObject(0).getString("text"));
    } 	
	

}