package org.eclipse.help.internal.search;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;
import java.util.List;
import org.apache.lucene.search.Hits;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.util.URLCoder;
import org.w3c.dom.*;
/**
 * An XML based search result. This is needed in order to decouple
 * the search client from the search server. The server encodes
 * the search results as an XML Document and passes it to the client
 */
public class SearchResult {
	private Document factory;
	private String urlEncodedQuery;
	/**
	 * Constructor
	 */
	public SearchResult(String query) {
		this.urlEncodedQuery = URLCoder.encode(query);
		// instantiate the xml factory and create the root element
		factory = new DocumentImpl();
		factory.appendChild(factory.createElement(IToc.TOC));
	}
	/**
	 * Adds hits to the result
	 * @param Hits hits
	 */
	public void addHits(Hits hits) {
		for (int h = 0; h < hits.length(); h++) {
			org.apache.lucene.document.Document doc;
			try {
				doc = hits.doc(h);
			} catch (IOException ioe) {
				return;
			}
			Element e = factory.createElement(ITopic.TOPIC);
			factory.getDocumentElement().appendChild(e);
			// Set the document href
			String href = doc.get("name");
			e.setAttribute(ITopic.HREF, href + "?resultof=" + urlEncodedQuery);
			// Set the document label
			String label = doc.get("title");
			if ("".equals(label)) {
				// Title does not exist, use label from the TOC
				ITopic topic = null;
				IToc[] tocs = HelpSystem.getTocManager().getTocs();
				for (int i = 0; topic == null && i < tocs.length; i++) {
					topic = tocs[i].getTopic(href);
				}
				if (topic != null) {
					label = topic.getLabel();
				}
			}
			if (label == null || "".equals(label))
				label = href;
			e.setAttribute(ITopic.LABEL, label);
		}
	}
	/**
	 * Filters search results according to the user selections if filtering is enabled
	 * @paream allResults array of Documents returned by search engine
	 * @return Document[] - subset of allResults
	 */
	protected void filterTopicsFromExcludedCategories(List excludedCategories) {
		/********
		if (getSize() == 0)
			return;
		if (excludedCategories == null)
			return;
		
		Element resultsRoot = factory.getDocumentElement();
		NodeList results =
			resultsRoot.getElementsByTagName(ITopic.TOPIC);
		for (int i = 0; i < results.getLength(); i++) {
			Element result = (Element) results.item(i);
			String url = result.getAttribute(ITopic.HREF);
		
			Topic[] topics =
				(Topic[]) HelpSystem
					.getNavigationManager()
					.getCurrentNavigationModel()
					.getTopicsWithURL(url);
			if (topics == null)
				continue; // should never happen
		
			// for all topics corresponding to the found document's URL
			boolean excludedFromAllViews=true;
			for (int j = 0; j < topics.length; j++) {
				// Find main level topic in the hierarchy
				Contribution parent = topics[j];
				while (parent.getParent() instanceof Topic) {
					parent = parent.getParent();
				}
				// if parent belongs to mainTopics, then this
				// search result may need to be filtered out
				if (!excludedCategories.contains(parent.getID())) {
					excludedFromAllViews=false;
					break;
				}
			}
			if(excludedFromAllViews){
				resultsRoot.removeChild(result);
				i--;
			}
		}
		*****************************/
	}
	public int getSize() {
		if (factory == null || factory.getDocumentElement() == null)
			return 0;
		else
			return factory.getDocumentElement().getElementsByTagName("topic").getLength();
	}
	public Document getXML() {
		return factory;
	}
	public String toString() {
		try {
			StringWriter writer = new StringWriter();
			OutputFormat format = new OutputFormat();
			//format.setVersion("1.0");
			//format.setPreserveSpace(true);
			// Should we set UTF-8 or leave default (which is UTF-8)
			format.setEncoding((String) null);
			Serializer serializer =
				SerializerFactory.getSerializerFactory("xml").makeSerializer(writer, format);
			serializer.asDOMSerializer().serialize(factory);
			return writer.getBuffer().toString();
		} catch (IOException e) {
			return null;
		}
	}
}