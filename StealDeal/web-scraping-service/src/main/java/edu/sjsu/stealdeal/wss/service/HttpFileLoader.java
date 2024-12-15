package edu.sjsu.stealdeal.wss.service;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HttpFileLoader {

  public static Document loadFileToJsoupDocument(String fileUrl) throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpGet httpGet = new HttpGet(fileUrl);
      CloseableHttpResponse response = httpClient.execute(httpGet);
      HttpEntity entity = response.getEntity();

      Document document = null;
      // Check if the entity is not null
      if (entity != null) {
        // Obtain the content as an InputStream
        try (InputStream inputStream = entity.getContent()) {
          document = Jsoup.parse(inputStream, "UTF-8", fileUrl);
        }
      }
      httpClient.close();
      return document;
    }
  }
}
