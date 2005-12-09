package org.eclipse.help.internal.search;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.help.search.XMLSearchParticipant;
import org.xml.sax.Attributes;


public class XHTMLSearchParticipant extends XMLSearchParticipant {
	public XHTMLSearchParticipant() {
	}

	protected void handleStartElement(String name, Attributes attributes, IParsedXMLContent data) {
	}

	protected void handleEndElement(String name, IParsedXMLContent data) {
	}

	protected void handleText(String text, IParsedXMLContent data) {
		String stackPath = getElementStackPath();
		IPath path = new Path(stackPath);
		if (path.segment(1).equalsIgnoreCase("body")) { //$NON-NLS-1$
			data.addText(text);
			data.addToSummary(text);
		}
		else if (path.segment(1).equalsIgnoreCase("head")) { //$NON-NLS-1$
			data.setTitle(text);
		}
	}
}