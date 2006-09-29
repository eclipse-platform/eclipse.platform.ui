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
package org.eclipse.help.internal.webapp.servlet;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.help.internal.search.SearchHit;

/*
 * Serializes a set of search hits into XML, so that it can be transmitted
 * across the network and reconstructed on the other side for remote help.
 */
public class SearchSerializer {

	public static String serialize(Collection results) {
		StringBuffer buf = new StringBuffer();
		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
		buf.append("<searchHits>\n"); //$NON-NLS-1$
		Iterator iter = results.iterator();
		while (iter.hasNext()) {
			SearchHit hit = (SearchHit)iter.next();
			serialize(hit, buf, "   "); //$NON-NLS-1$
		}
		buf.append("</searchHits>\n"); //$NON-NLS-1$
		return buf.toString();
	}
	
	private static void serialize(SearchHit hit, StringBuffer buf, String indent) {
		buf.append(indent + "<hit"); //$NON-NLS-1$
		if (hit.getHref() != null) {
			buf.append('\n' + indent	+ "      href=\"" + XMLGenerator.xmlEscape(hit.getHref()) + '"'); //$NON-NLS-1$
		}
		if (hit.getLabel() != null) {
			buf.append('\n' + indent	+ "      label=\"" + XMLGenerator.xmlEscape(hit.getLabel()) + '"'); //$NON-NLS-1$
		}
		if (hit.isPotentialHit()) {
			buf.append('\n' + indent	+ "      isPotentialHit=\"true\""); //$NON-NLS-1$
		}
		buf.append('\n' + indent + "      score=\"" + hit.getScore() + '"'); //$NON-NLS-1$
		buf.append(">\n"); //$NON-NLS-1$
		
		String summary = hit.getSummary();
		if (summary != null) {
			serialize(summary, buf, indent + "   "); //$NON-NLS-1$
		}
		buf.append(indent + "</hit>\n"); //$NON-NLS-1$
	}

	private static void serialize(String summary, StringBuffer buf, String indent) {
		buf.append(indent + "<summary>"); //$NON-NLS-1$
		buf.append(XMLGenerator.xmlEscape(summary));
		buf.append("</summary>\n"); //$NON-NLS-1$
	}
}
