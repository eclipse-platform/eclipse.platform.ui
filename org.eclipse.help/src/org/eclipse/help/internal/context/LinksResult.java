/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.context;
import java.io.*;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.HelpSystem;
import org.w3c.dom.*;
/**
 * An XML based links result. This is needed in order to decouple
 * the links on the client from the search server. The server encodes
 * the links as an XML Document and passes it to the client
 */
public class LinksResult {
	private Document factory;
	/**
	 * Constructor
	 */
	public LinksResult(String contextId) {
		// instantiate the xml factory and create the root element
		factory = new DocumentImpl();
		factory.appendChild(factory.createElement(IToc.TOC));
		IContext context = HelpSystem.getContextManager().getContext(contextId);
		if (context == null)
			return;
		IHelpResource[] links = context.getRelatedTopics();
		if (links == null || links.length <= 0)
			return;
		for (int i = 0; i < links.length; i++)
			addDocument(links[i]);
	}
	/**
	 * Adds a new link document to the result
	 * @param href the document location (url)
	 */
	private void addDocument(IHelpResource res) {
		// NOTE: some of the data may not be needed. Clean this later.
		Element e = factory.createElement(ITopic.TOPIC);
		factory.getDocumentElement().appendChild(e);
		// the document name is the actual plugin url
		e.setAttribute(ITopic.HREF, res.getHref());
		e.setAttribute(ITopic.LABEL, res.getLabel());
	}
	public int getSize() {
		if (factory == null || factory.getDocumentElement() == null)
			return 0;
		else
			return factory.getDocumentElement().getElementsByTagName("topic").getLength();
	}
	private Document getXML() {
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