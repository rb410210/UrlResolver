package com.rohitbalan.core;

import java.io.File;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rohitbalan.modal.Channel;

public class XMLWriter {
	public void createXML(String category, List<Channel> channels) throws Exception {
		File outputFolder = new File("output");
		outputFolder.mkdir();

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("feed");
		doc.appendChild(rootElement);

		Element resultLength = doc.createElement("resultLength");
		resultLength.appendChild(doc.createTextNode("" + channels.size()));
		rootElement.appendChild(resultLength);

		Element endIndex = doc.createElement("endIndex");
		endIndex.appendChild(doc.createTextNode("1"));
		rootElement.appendChild(endIndex);

		for (Channel channel : channels) {
			Element item = doc.createElement("item");
			Attr imageAttr = doc.createAttribute("hdImg");
			imageAttr.setValue(channel.getThumbnail());
			item.setAttributeNode(imageAttr);
			rootElement.appendChild(item);

			Element title = doc.createElement("title");
			title.appendChild(doc.createTextNode(channel.getName()));
			item.appendChild(title);

			Element contentId = doc.createElement("contentId");
			contentId.appendChild(doc.createTextNode("100"));
			item.appendChild(contentId);

			Element contentQuality = doc.createElement("contentQuality");
			contentQuality.appendChild(doc.createTextNode("HD"));
			item.appendChild(contentQuality);

			Element streamFormat = doc.createElement("streamFormat");
			streamFormat.appendChild(doc.createTextNode("hls"));
			item.appendChild(streamFormat);

			Element media = doc.createElement("media");
			item.appendChild(media);

			Element streamQuality = doc.createElement("streamQuality");
			streamQuality.appendChild(doc.createTextNode("HD"));
			media.appendChild(streamQuality);

			Element streamUrl = doc.createElement("streamUrl");
			streamUrl.appendChild(doc.createTextNode(channel.getVideo()));
			media.appendChild(streamUrl);

		}

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		DOMSource source = new DOMSource(doc);
		String filename = category.replace(" ", "_") + ".xml";
		System.out.println(filename + "\t" + category);
		StreamResult result = new StreamResult(new File(outputFolder, filename));
		try {
			transformer.transform(source, result);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
