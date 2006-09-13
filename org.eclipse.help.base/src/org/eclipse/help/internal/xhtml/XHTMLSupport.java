/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.xhtml;

import java.util.Collection;

import org.w3c.dom.Document;

/**
 * Central class for XHTML support in help.
 */
public class XHTMLSupport {

	private static UAContentFilterProcessor filterProcessor = new UAContentFilterProcessor();
	private static UAIncludeProcessor includeProcessor = new UAIncludeProcessor();
	private static UAExtensionProcessor extensionProcessor = new UAExtensionProcessor();
	
	private Document document;
	private String href;

	public XHTMLSupport(Document document, String href) {
		this.document = document;
		this.href = href;
	}

	/**
	 * Processes the DOM, with filtering turned on.
	 * 
	 * @return the resulting DOM
	 */
	public Document processDOM() {
		return processDOM(true);
	}

	/**
	 * Processes the DOM. Filtering will only be done if requested. Filtering
	 * may be skipped, for example, for indexing.
	 * 
	 * @param filter whether or not to filter
	 * @return the resulting DOM
	 */
	public Document processDOM(boolean filter) {

		if (filter) {
			// resolve filters
			filterProcessor.applyFilters(document);
		}

		// resolve includes
		includeProcessor.resolveIncludes(document);

		// resolve extensions
		extensionProcessor.resolveExtensions(document, href);

		return document;
	}

	public static Collection getTopicExtensions() {
		return extensionProcessor.getTopicExtensions();
	}
	
	public static Collection getTopicReplaces() {
		return extensionProcessor.getTopicReplaces();
	}
	
	public static UAContentFilterProcessor getFilterProcessor() {
		return filterProcessor;
	}

	/**
	 * Used by the UI plugin to override base functionality and add more filtering capabilities.
	 */
	public static void setFilterProcessor(UAContentFilterProcessor filterProcessor) {
		XHTMLSupport.filterProcessor = filterProcessor;
	}
}
