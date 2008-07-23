/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.xhtml;

import org.eclipse.help.internal.UAElement;
import org.eclipse.help.internal.dynamic.ProcessorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/*
 * Converts the charset in XHTML meta tag to UTF-8. This is the encoding
 * output by the XMLProcessor, and we need the charset in the meta tags
 * to match, otherwise browsers will be confused.
 * Also ensure that all <script> and <div> elements have a child, 
 */
public class XHTMLCharsetHandler extends ProcessorHandler {

	private static final String ELEMENT_META = "meta"; //$NON-NLS-1$
	private static final String ATTRIBUTE_CONTENT = "content"; //$NON-NLS-1$
	private static final String PREFIX_CHARSET = "text/html; charset="; //$NON-NLS-1$
	private static final String ENCODING_UTF8 = "UTF-8"; //$NON-NLS-1$

	public short handle(UAElement element, String id) {
		if (ELEMENT_META.equals(element.getElementName())) {
			String content = element.getAttribute(ATTRIBUTE_CONTENT);
			if (content != null && content.startsWith(PREFIX_CHARSET)) {
				element.setAttribute(ATTRIBUTE_CONTENT, PREFIX_CHARSET + ENCODING_UTF8);
				return HANDLED_CONTINUE;
			}
		}
		if ("script".equalsIgnoreCase(element.getElementName())) { //$NON-NLS-1$ 
			Element domElement = element.getElement();
			if (domElement.getFirstChild() == null) {
				Document document = domElement.getOwnerDocument();
				Text child = document.createTextNode(" "); //$NON-NLS-1$
				domElement.appendChild(child);
			}
		}
		return UNHANDLED;
	}
}
