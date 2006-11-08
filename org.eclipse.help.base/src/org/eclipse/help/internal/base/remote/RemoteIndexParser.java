/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.base.remote;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.help.IndexContribution;
import org.eclipse.help.Node;
import org.eclipse.help.internal.dynamic.NodeReader;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * Converts indexes serialized by the IndexServlet on remote help server back
 * into model objects. The XML is similar to index XML files but not identical
 * (it has all indexes in one, has indexContribution elements, etc.
 */
public class RemoteIndexParser extends DefaultHandler {

	private NodeReader reader;
	
	/*
	 * Parses the given serialized indexes and returns generated model objects.
	 */
	public IndexContribution[] parse(InputStream in) throws ParserConfigurationException, SAXException, IOException {
		if (reader == null) {
			reader = new NodeReader();
		}
		Node root = reader.read(in);
		Node[] children = root.getChildNodes();
		IndexContribution[] contributions = new IndexContribution[children.length];
		for (int i=0;i<children.length;++i) {
			IndexContribution contribution = new IndexContribution();
			contribution.setId(children[i].getAttribute("id")); //$NON-NLS-1$
			contribution.setLocale(children[i].getAttribute("locale")); //$NON-NLS-1$
			contribution.setIndex(children[i].getChildNodes()[0]);
			contributions[i] = contribution;
		}		
		return contributions;
	}
}
