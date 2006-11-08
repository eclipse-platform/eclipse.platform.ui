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
package org.eclipse.help.internal.dynamic;

import org.eclipse.help.Node;
import org.eclipse.help.internal.extension.ContentExtension;

/*
 * The handler responsible for processing content extensions (contributions
 * into anchors and element replacements).
 */
public class ExtensionHandler extends NodeHandler {

	private static final String ELEMENT_ANCHOR = "anchor"; //$NON-NLS-1$
	private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

	private ExtensionResolver resolver;
	private NodeReader reader;
	private String locale;
	
	/*
	 * This handler must know the locale since it's pulling content
	 * in from other documents.
	 */
	public ExtensionHandler(NodeReader reader, String locale) {
		this.reader = reader;
		this.locale = locale;
	}
	
	public short handle(Node node, String path) {
		if (ELEMENT_ANCHOR.equals(node.getNodeName())) {
			return handleExtension(node, path, ContentExtension.CONTRIBUTION);
		}
		if (node.getAttribute(ATTRIBUTE_ID) != null) {
			return handleExtension(node, path, ContentExtension.REPLACE);
		}
		return UNHANDLED;
	}
	
	/*
	 * Handle the given extension-related node. It is either an anchor
	 * or an element with an id that could potentially be replaced.
	 */
	private short handleExtension(Node node, String path, int type) {
		String id = node.getAttribute(ATTRIBUTE_ID);
		if (id != null && id.length() > 0) {
			if (resolver == null) {
				resolver = new ExtensionResolver(getProcessor(), reader, locale);
			}
			// get the nodes to insert/replace with
			Node[] nodes = resolver.resolveExtension(path + '#' + id, type);
			if (nodes != null && nodes.length > 0) {
				Node parent = node.getParentNode();
				for (int i=0;i<nodes.length;++i) {
					parent.insertBefore(nodes[i], node);
				}
				parent.removeChild(node);
				return HANDLED_SKIP;
			}
		}
		// always remove anchors, even invalid ones
		if (type == ContentExtension.CONTRIBUTION) {
			node.getParentNode().removeChild(node);
			return HANDLED_SKIP;
		}
		// it was an element with id, but no replacements
		return UNHANDLED;
	}
}
