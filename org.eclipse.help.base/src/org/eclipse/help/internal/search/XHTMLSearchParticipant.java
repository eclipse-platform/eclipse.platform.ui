/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.help.search.XMLSearchParticipant;
import org.xml.sax.Attributes;

public class XHTMLSearchParticipant extends XMLSearchParticipant {
	
	protected void handleStartElement(String name, Attributes attributes, IParsedXMLContent data) {
		// do nothing
	}

	protected void handleEndElement(String name, IParsedXMLContent data) {
		// do nothing
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
}