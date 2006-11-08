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
package org.eclipse.help.internal.xhtml;

import org.eclipse.help.Node;
import org.eclipse.help.internal.dynamic.NodeHandler;

/*
 * Converts the charset in XHTML meta tag to UTF-8. This is the encoding
 * output by the XMLProcessor, and we need the charset in the meta tags
 * to match, otherwise browsers will be confused.
 */
public class XHTMLCharsetHandler extends NodeHandler {

	private static final String ELEMENT_META = "meta"; //$NON-NLS-1$
	private static final String ATTRIBUTE_CONTENT = "content"; //$NON-NLS-1$
	private static final String PREFIX_CHARSET = "text/html; charset="; //$NON-NLS-1$
	private static final String ENCODING_UTF8 = "UTF-8"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.help.internal.dynamic.DocumentProcessorHandler#handle(org.eclipse.help.Node, java.lang.String)
	 */
	public short handle(Node node, String id) {
		if (ELEMENT_META.equals(node.getNodeName())) {
			String content = node.getAttribute(ATTRIBUTE_CONTENT);
			if (content != null && content.startsWith(PREFIX_CHARSET)) {
				node.setAttribute(ATTRIBUTE_CONTENT, PREFIX_CHARSET + ENCODING_UTF8);
				return HANDLED_CONTINUE;
			}
		}
		return UNHANDLED;
	}
}
