package org.eclipse.help.internal.search;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.help.internal.base.DynamicContentProducer;
import org.eclipse.help.search.XMLSearchParticipant;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class XHTMLSearchParticipant extends XMLSearchParticipant {

	class IncludedHandler extends DefaultHandler {

		private String id;
		private boolean active;
		private Stack stack = new Stack();
		private IParsedXMLContent data;

		public IncludedHandler(String id, IParsedXMLContent data) {
			this.id = id;
			this.data = data;
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			String eid = attributes.getValue("id"); //$NON-NLS-1$
			if (eid != null && eid.equals(id)) {
				stack.push(qName);
				active = true;
			}
		}

		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (active) {
				stack.pop();
				if (stack.size() == 0)
					active = false;
			}
		}

		public void characters(char[] characters, int start, int length) throws SAXException {
			if (!active)
				return;
			if (length == 0)
				return;
			StringBuffer buff = new StringBuffer();
			for (int i = 0; i < length; i++) {
				buff.append(characters[start + i]);
			}
			String text = buff.toString().trim();
			if (text.length() > 0) {
				data.addText(text);
				data.addToSummary(text);
			}
		}
	}


	public XHTMLSearchParticipant() {
	}

	protected void handleStartElement(String name, Attributes attributes, IParsedXMLContent data) {
		if (name.equalsIgnoreCase("include")) { //$NON-NLS-1$
			processIncludedContent(attributes.getValue("path"), data); //$NON-NLS-1$
		}
	}

	protected void handleEndElement(String name, IParsedXMLContent data) {
	}

	protected void handleText(String text, IParsedXMLContent data) {
		String stackPath = getElementStackPath();
		IPath path = new Path(stackPath);
		if (path.segment(1).equalsIgnoreCase("body")) { //$NON-NLS-1$
			data.addText(text);
			data.addToSummary(text);
		} else if (path.segment(1).equalsIgnoreCase("head")) { //$NON-NLS-1$
			data.setTitle(text);
		}
	}

	private void processIncludedContent(String path, IParsedXMLContent data) {
		int sep1 = path.indexOf('/');
		if (sep1 == -1)
			return;
		String pluginId = path.substring(0, sep1);
		int sep2 = path.lastIndexOf('/');
		if (sep2 == -1)
			return;
		String href = path.substring(sep1 + 1, sep2);
		String id = path.substring(sep2 + 1);
		InputStream stream = DynamicContentProducer.openStreamFromPlugin(pluginId, href, data.getLocale());
		if (stream == null)
			return;
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			IncludedHandler handler = new IncludedHandler(id, data);
			parser.parse(stream, handler);
		} catch (Exception e) {
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
			}
		}
	}
}