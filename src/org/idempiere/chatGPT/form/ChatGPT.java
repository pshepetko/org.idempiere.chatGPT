package org.idempiere.chatGPT.form;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class ChatGPT {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChatGPT.class);


  public static String sendQuery(String input, String endpoint, String apiKey, String sysMessage) {
    // Build input and API key params
    JSONObject payload = new JSONObject();
    JSONObject sys_message = new JSONObject();
    JSONObject message = new JSONObject();
    JSONArray messageList = new JSONArray();

    sys_message.put("role", "system");
    sys_message.put("content",  sysMessage);
    message.put("role", "user");
    message.put("content", input);

    messageList.put(sys_message);
    messageList.put(message);
    
    System.out.print(messageList.toString());

    payload.put("model", "gpt-4o"); // model is important
    payload.put("messages", messageList);
    payload.put("temperature", 0.7);

    StringEntity inputEntity = new StringEntity(payload.toString(), ContentType.APPLICATION_JSON);

    // Build POST request
    HttpPost post = new HttpPost(endpoint);
    post.setEntity(inputEntity);
    post.setHeader("Authorization", "Bearer " + apiKey);
    post.setHeader("Content-Type", "application/json");

    // Send POST request and parse response
    try (CloseableHttpClient httpClient = HttpClients.createDefault();
      CloseableHttpResponse response = httpClient.execute(post)) {
        HttpEntity resEntity = response.getEntity();
        String resJsonString = new String(resEntity.getContent().readAllBytes(), StandardCharsets.UTF_8);
        JSONObject resJson = new JSONObject(resJsonString);
        
        if (resJson.has("error")) {
          String errorMsg = resJson.getString("error");
          LOGGER.error("Chatbot API error: {}", errorMsg);
          return "Error: " + errorMsg;
        }

        // Parse JSON response
        JSONArray responseArray = resJson.getJSONArray("choices");
        List<String> responseList = new ArrayList<>();
        
        for (int i = 0; i < responseArray.length(); i++) {
          JSONObject responseObj = responseArray.getJSONObject(i);
          String responseString = responseObj.getJSONObject("message").getString("content");
          responseList.add(responseString);
        }
       
        return responseList.toString().substring(1, (responseList.toString().length()-1));

      } catch (IOException | JSONException e) {
        LOGGER.error("Error sending request: {}", e.getMessage());
        return "Error: " + e.getMessage();
      }
  }
}