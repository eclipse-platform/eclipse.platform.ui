/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.index;

import org.eclipse.help.internal.dynamic.DocumentReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class IndexDocumentReader extends DocumentReader {
	
	protected void prepareDocument(Document document) {
		prune(document.getDocumentElement());
	}

	private void prune(Node element) {
		NodeList nodes = element.getChildNodes();
		Node node = nodes.item(0); 
		while (node != null) {
			short nodeType = node.getNodeType();
			if (nodeType == Node.TEXT_NODE || nodeType == Node.COMMENT_NODE) {
				Node nodeToDelete = node;
				node = node.getNextSibling();
				element.removeChild(nodeToDelete);
			} else  if (nodeType == Node.ELEMENT_NODE) {
				String kind = node.getNodeName();
				if ("topic".equalsIgnoreCase(kind)) { //$NON-NLS-1$
					fixTopicAttributes((Element)node);
				}
				prune(node);
			    node = node.getNextSibling();
			} else {
				node = node.getNextSibling();
			}
		}
	}

	private void fixTopicAttributes(Element topic) {
		String title = topic.getAttribute("name"); //$NON-NLS-1$
		if (title != null && title.length() > 0) {
			topic.setAttribute("label", title);			 //$NON-NLS-1$
			topic.removeAttribute("name"); //$NON-NLS-1$
		}
	}

}
