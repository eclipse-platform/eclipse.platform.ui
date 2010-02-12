/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.criteria;

import org.eclipse.help.internal.dynamic.DocumentReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CriteriaDefinitionDocumentReader extends DocumentReader {
	
	private static final String CRITERION_ELEMENT = "criterion"; //$NON-NLS-1$
	private static final String CRITERION_ID_ATTRIBUTE = "id"; //$NON-NLS-1$
	
	
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
				if (CRITERION_ELEMENT.equalsIgnoreCase(kind)) {
					fixCriterionId((Element)node);
				}
				prune(node);
			    node = node.getNextSibling();
			} else {
				node = node.getNextSibling();
			}
		}
	}
	
	/*
	 * Cause criterion ID is case insensitive, so just change it to lower case.
	 */
	private void fixCriterionId(Element criterion) {
		String id = criterion.getAttribute(CRITERION_ID_ATTRIBUTE);
		if (id != null && id.length() > 0) {
			criterion.setAttribute(CRITERION_ID_ATTRIBUTE, id.toLowerCase());
		}
	}
}
