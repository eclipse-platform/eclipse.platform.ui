/***************************************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/

package org.eclipse.help.internal.xhtml;

import org.w3c.dom.Document;


/**
 * Central class for XHTML support in help.
 */
public class XHTMLSupport {

	// singleton for performance.
	private static UAContentFilterProcessor filterProcessor = new UAContentFilterProcessor();
	// singleton for performance.
	private static UAContentMergeProcessor mergeProcessor = new UAContentMergeProcessor();

	public static Document processDOM(Document document, String locale) {

		// resolve filters.
		filterProcessor.applyFilters(document);

		// resolve includes.
		mergeProcessor.resolveIncludes(document, locale);

		return document;
	}


	/**
	 * Used by the UI plugin to override base functionality and add more filtering capabilities.
	 */
	public static void setFilterProcessor(UAContentFilterProcessor filterProcessor) {
		XHTMLSupport.filterProcessor = filterProcessor;
	}

}
