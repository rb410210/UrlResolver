package com.rohitbalan;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.io.Files;
import com.rohitbalan.core.OktvService;
import com.rohitbalan.core.XMLWriter;
import com.rohitbalan.modal.Category;
import com.rohitbalan.modal.Channel;

public class RokuConfigCreator
{
	public static void main(String[] args)
	{
		new RokuConfigCreator().start();
	}

	private void start()
	{
		try
		{
			String homePageContent = new OktvService().getHtmlContentFromUrl("http://oklivetv.com/");
			Document html = Jsoup.parse(homePageContent);
			List<Category> categories = traverseCategories(html.body().getElementsByTag("li"));
			
			LinkedHashSet<Category> setCategories = new LinkedHashSet<Category>();
			setCategories.addAll(categories);
			categories.clear();
			categories.addAll(setCategories);
			
			//System.out.println(categories);
			int count = 0;
			for (Category category : categories) {
				System.out.println(category + ": " + count++ + "/" + categories.size());
				try
				{
					browseCategory(category);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			//browseCategory(categories.get(0));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void browseCategory(Category category) throws Exception
	{
		String categoryContent = new OktvService().getHtmlContentFromUrl(category.getUrl());
		Document html = Jsoup.parse(categoryContent);
		List<Channel> channels = traverseChannels(html);
		//System.out.println(category.getName() + " (" + channels.size() + "): " + channels);
		
		List<Channel> directChannels = new ArrayList<Channel>();
		List<Channel> routedChannels = new ArrayList<Channel>();
		
		for(Channel channel: channels) {
			if(channel.isRoutedVideo())
				routedChannels.add(channel);
			else
				directChannels.add(channel);
		}
		
		splitXMLs(routedChannels, category, "R");
		splitXMLs(directChannels, category, "D");
	}
	
	private void splitXMLs(List<Channel> routedChannels, Category category, String routedOrDirect) throws Exception {
		if(routedChannels.size()>0) {
			System.out.println(routedChannels.size());
			for (int channelIndex = 0; channelIndex < routedChannels.size(); channelIndex = channelIndex + 8) {
				List<Channel> sublist = routedChannels.subList(channelIndex, (channelIndex + 8) > routedChannels.size() ? routedChannels.size() :(channelIndex + 8));
				new XMLWriter().createXML(category.getName()
						+ " " + routedOrDirect
						+ (channelIndex == 0 ? "" : channelIndex/8), sublist);
			}
		}
		
	}

	private List<Channel> traverseChannels(Document html) throws IOException
	{
		List<Channel> channels = new ArrayList<Channel>();
		
		Element main = html.body().getElementById("main");
		Elements anchorTags = main.getElementsByTag("a");
		for (Element anchorTag: anchorTags) {
			if("clip-link".equals(anchorTag.attr("class")) 
					&& anchorTag.getElementsByTag("span").size() >0 
					&& "clip".equals(anchorTag.getElementsByTag("span").first().attr("class"))
					&& anchorTag.getElementsByTag("span").first().getElementsByTag("img").size() >0) {
				Element image = anchorTag.getElementsByTag("span").first().getElementsByTag("img").first();
				String icon = image.attr("src").replace("-320x180", "");
				icon = downloadIcon(icon);
				Channel channel = new Channel(image.attr("alt"), anchorTag.attr("href"), icon);
				
				String channelContent = new OktvService().getHtmlContentFromUrl(channel.getUrl());
				Document channelHtml = Jsoup.parse(channelContent);
				Elements iframes = channelHtml.body().getElementsByTag("iframe");
				for(Element iframe: iframes) {
					String originalVideoUrl = iframe.attr("src");
					if(iframe.hasAttr("src") && originalVideoUrl.startsWith("http://oklivetv.com/xplay/xplay.php?idds=")) {
						String parsedVideoUrl = new OktvService().getHtmlContentFromUrl("http://192.168.0.102:9888/urlresolver/?id=" + originalVideoUrl.replace("http://oklivetv.com/xplay/xplay.php?idds=", ""));
						if(parsedVideoUrl.contains("porndig.com") || parsedVideoUrl.trim().isEmpty()) {
							channel.setVideo(originalVideoUrl);
							channel.setRoutedVideo(true);
						} else if(parsedVideoUrl.contains("filmon.com")) {
							channel.setVideo(parsedVideoUrl.replace("low.stream", "high.stream"));
						} else {
							channel.setVideo(parsedVideoUrl);
						}
						break;
					}
				}
				channels.add(channel);
			}
		}
		
		Elements nextElements = html.body().getElementsByAttributeValue("rel", "next");
		if(nextElements.size()>0) {
			String nextPageUrl = nextElements.first().attr("href");
			String nextPageContent = new OktvService().getHtmlContentFromUrl(nextPageUrl);
			Document nextHtml = Jsoup.parse(nextPageContent);
			channels.addAll(traverseChannels(nextHtml));
		}
		
		return channels;
	}

	private String downloadIcon(String icon)
	{
		try
		{
			byte[] bytes = new OktvService().getBinaryContentFromUrl(icon);
			Path path = Paths.get(icon);
			String filename = path.getFileName().toString();
			File outputFolder = new File("images");
			outputFolder.mkdir();
			File image = new File(outputFolder, filename);
			Files.write(bytes, image);
			return "pkg:/images/" + filename;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return icon;
		}
	}

	private List<Category> traverseCategories(List<Element> elements) {
		List<Category> categories = new ArrayList<Category>();
		for(Element listItem: elements) {
			//System.out.println("#########");
			if(listItem.hasAttr("class") && listItem.attr("class").contains("cat-item") ) {
				//System.out.println(listItem.getElementsByTag("ul").size() + listItem.html());
				if(listItem.getElementsByTag("ul").size()>0) {
					categories.addAll(traverseCategories(listItem.getElementsByTag("ul").first().getElementsByTag("li")));
				} else {
					if(listItem.getElementsByTag("a").size()>0) {
						Element anchor = listItem.getElementsByTag("a").first();
						categories.add(new Category(anchor.text(), anchor.attr("href")));
					}
					//System.out.println(listItem.html());
				}
			}
		};
		return categories;
	}
}
