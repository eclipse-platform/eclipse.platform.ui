package org.eclipse.help.internal.search;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.URLEncoder;
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
 * the search results as an XML Document and passes it to the client.
 * <pre>
 * 	<toc>
 * 		<topic label=".." score="..." toc=".." toclabel=".."/>
 *  .....
 */
public class SearchResult {
	private Document factory;
	private String urlEncodedQuery;
	private List scope;
	private int maxHits;
	private String locale;
	/**
	 * Constructor
	 * @param scope list of books to search
	 */
	public SearchResult(List scope, int maxHits, String locale) {
		this.scope = scope;
		this.maxHits = maxHits;
		this.locale = locale;
		// instantiate the xml factory and create the root element
		factory = new DocumentImpl();
		factory.appendChild(factory.createElement(IToc.TOC));
	}
	/**
	 * Adds hits to the result
	 * @param Hits hits
	 */
	public void addHits(Hits hits, String analyzedWords) {
		float scoreScale = 1.0f;
		String urlEncodedWords = URLCoder.encode(analyzedWords);
		for (int h = 0; h < hits.length() && h < maxHits; h++) {
			org.apache.lucene.document.Document doc;
			float score;
			try {
				doc = hits.doc(h);
				score = hits.score(h);
			} catch (IOException ioe) {
				return;
			}
			if (h == 0) {
				if (score > 0) {
					scoreScale = 1 / score;
					score = 1;
				}
			} else {
				score *= scoreScale;
			}
			String href = doc.get("name");
			IToc toc = findTocForTopic(href);
			ITopic topic = toc==null? null : toc.getTopic(href);

			// Create topic
			Element e = factory.createElement(ITopic.TOPIC);
			factory.getDocumentElement().appendChild(e);
			// Set document score
			e.setAttribute("score", Float.toString(score));
			// Set document href
			e.setAttribute(ITopic.HREF, href + "?resultof=" + urlEncodedWords);
			// Set the document label
			String label = doc.get("raw_title");
			if ("".equals(label) && topic!=null) {
				label = topic.getLabel();
			}
			if (label == null || "".equals(label))
				label = href;
			e.setAttribute(ITopic.LABEL, label);
			// Set the document toc
			if (toc != null)
			{
				e.setAttribute(IToc.TOC, toc.getHref());
				e.setAttribute(IToc.TOC+IToc.LABEL, toc.getLabel());
			}
		}
	}
	/**
	 * Finds a topic in a bookshelf
	 * or within a scope if specified
	 */
	protected IToc findTocForTopic(String href) {
		IToc[] tocs = HelpSystem.getTocManager().getTocs(locale);
		for (int i = 0; i < tocs.length; i++) {
			if (scope != null)
				if (!scope.contains(tocs[i].getHref()))
					continue;
			ITopic topic = tocs[i].getTopic(href);
			if (topic != null)
				return tocs[i];
		}
		return null;
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