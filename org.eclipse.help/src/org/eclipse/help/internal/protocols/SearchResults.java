/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.protocols;

import java.io.*;
import java.util.Collection;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.search.SearchResultCollector;
import org.w3c.dom.*;

/**
 * An XML based search results. This is needed in order to decouple
 * the search client from the search server. The server encodes
 * the search results as an XML Document and passes it to the client.
 * &lt;pre&gt;
 * 	&lt;toc&gt;
 * 		&lt;topic label=".." score="..." toc=".." toclabel=".."/&gt;
 *  .....
 */
public class SearchResults extends SearchResultCollector {
	private Document dom;
	/**
	 * Constructor
	 * @param scope collection of book names to search in, null means entire world
	 */
	public SearchResults(Collection scope, int maxHits, String locale) {
		super(scope, maxHits, locale);
	}
	/**
	 * Adds hits to the result
	 * @param Hits hits
	 */
	private void createDOM() {
		// instantiate the xml factory and create the root element
		dom = new DocumentImpl();
		dom.appendChild(dom.createElement(IToc.TOC));
		for (int s = 0; s < searchHits.length; s++) {
			// Create topic
			Element e = dom.createElement(ITopic.TOPIC);
			dom.getDocumentElement().appendChild(e);
			// Set document score
			e.setAttribute(ITopic.HREF, searchHits[s].getHref());
			e.setAttribute("score", Float.toString(searchHits[s].getScore()));
			e.setAttribute(ITopic.LABEL, searchHits[s].getLabel());
			// Set the document toc
			IToc toc = findTocForTopic(searchHits[s].getHref());
			if (toc != null) {
				e.setAttribute(IToc.TOC, toc.getHref());
				e.setAttribute(IToc.TOC + IToc.LABEL, toc.getLabel());
			}
		}
	}
	public InputStream getInputStream() {
		if (dom == null)
			createDOM();
		try {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			OutputFormat format = new OutputFormat();
			//format.setVersion("1.0");
			//format.setPreserveSpace(true);
			format.setEncoding("UTF-8");
			Serializer serializer =
				SerializerFactory.getSerializerFactory("xml").makeSerializer(outStream, format);
			serializer.asDOMSerializer().serialize(dom);
			return new ByteArrayInputStream(outStream.toByteArray());
		} catch (IOException e) {
			return null;
		}
	}
}