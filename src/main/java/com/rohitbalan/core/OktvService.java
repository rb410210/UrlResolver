package com.rohitbalan.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptException;
import javax.ws.rs.WebApplicationException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class OktvService
{
	public String resolvedUrl(String id) {
		try {
			String htmlContent = getHtmlContent(id);
			List<String> scriptlets = extractScriptlets(htmlContent);
			return scriptlets.size()>0 ? scriptlets.get(0) : "";
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebApplicationException(400);
		}
	}
	
	private List<String> extractScriptlets(String htmlContent) throws ScriptException, IOException
	{
		List<String> scriplets = new ArrayList<String>();
		Document html = Jsoup.parse(htmlContent);
		for(Element element: html.body().getElementsByTag("script")) {
			String htmlScript = element.html();
			if(htmlScript!=null && htmlScript.trim().startsWith("eval")) {
				scriplets.add(evalJS(htmlScript));
			}
		};
		return scriplets ;
	}

	private String getHtmlContent(String id) throws IOException {
		return getHtmlContentFromUrl("http://oklivetv.com/xplay/xplay.php?idds=" + id);
	}
	
	public String getHtmlContentFromUrl(String url) throws IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(url);
		CloseableHttpResponse response1 = httpclient.execute(httpGet);
		
		try {
		    System.out.println(response1.getStatusLine() + " " + url);
		    HttpEntity entity1 = response1.getEntity();
		    String response = EntityUtils.toString(entity1, StandardCharsets.UTF_8);
		    EntityUtils.consume(entity1);
		    return response;
		} finally {
		    response1.close();
		}

	}
	
	public byte[] getBinaryContentFromUrl(String url) throws IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(url);
		CloseableHttpResponse response1 = httpclient.execute(httpGet);
		
		try {
		    //System.out.println(response1.getStatusLine() + " " + url);
		    HttpEntity entity1 = response1.getEntity();
		    byte[] response = EntityUtils.toByteArray(entity1);
		    EntityUtils.consume(entity1);
		    return response;
		} finally {
		    response1.close();
		}

	}
	
	
	private String evalJS(String scriptlet) throws IOException
	{
		String resolvedUrl = "";
		File tempFile = File.createTempFile("urlresolver", ".js");

		BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
		writer.write(scriptlet);
		writer.close();
		Process process = new ProcessBuilder("js-beautify", tempFile.getCanonicalPath()).start();
		InputStream is = process.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		
		Pattern p = Pattern.compile("streamURL = \"(.*)\";");
		
		while ((line = br.readLine()) != null)
		{
			Matcher m = p.matcher(line);
			while (m.find()) {
				resolvedUrl += m.group(1);
			}
		}
		System.out.println(resolvedUrl);
		tempFile.deleteOnExit();
		return resolvedUrl;

	}

}
