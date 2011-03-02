/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.utils;

import java.util.Collection;

import org.eclipse.help.HelpSystem;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.search.SearchHit;
import org.eclipse.help.internal.webapp.data.UrlUtil;
import org.eclipse.help.internal.webapp.servlet.XMLGenerator;

public class SearchXMLGenerator  {

	public static String serialize(Collection results) {
		return serialize((results != null) ? results.toArray() : null, false);
	}
	
	public static String serialize(Object[] hits, boolean boolIsCategory) {
		StringBuffer buf = new StringBuffer();
		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
		buf.append("<searchHits>\n"); //$NON-NLS-1$
		
		if (hits != null) {
			for (int i = 0; i < hits.length; i++) {
				SearchHit hit = (SearchHit) hits[i];
				serialize(hit, buf, "   ", boolIsCategory); //$NON-NLS-1$
			}
		}
		
		buf.append("</searchHits>\n"); //$NON-NLS-1$
		
		return buf.toString();
	}
	
	private static void serialize(SearchHit hit, StringBuffer buf, 
			String indent, boolean boolIsCategory) {
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
		
		// get Category
		if (boolIsCategory) {
			IHelpResource categoryResource = hit.getCategory();
			if (categoryResource != null) {
				serializeCategory(categoryResource, buf, indent + "  "); //$NON-NLS-1$
			}
		}
		
		// get Summary/Description
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
	
	private static void serializeCategory(IHelpResource categoryResource, 
			StringBuffer buf, String indent) {
		String category = categoryResource.getLabel();
		if (category == null) return;
		
		buf.append(indent + "<category"); //$NON-NLS-1$
		
		String catHref = getCategoryHref(categoryResource);
		if (catHref != null) {
			buf.append('\n' + indent	+ "      href=\""  //$NON-NLS-1$
					+ XMLGenerator.xmlEscape(catHref) + '"');
		}
		
		buf.append(">\n"); //$NON-NLS-1$
		buf.append(XMLGenerator.xmlEscape(category));
		
		buf.append("</category>\n"); //$NON-NLS-1$
	}
	
	private static String getCategoryHref(IHelpResource categoryResource) {
		String tocHref = categoryResource.getHref();
		IToc[] tocs = HelpSystem.getTocs();
		for (int j=0;j<tocs.length;++j) {
			if (tocHref.equals(tocs[j].getHref())) {
				ITopic topic = tocs[j].getTopic(null);
				String topicHref = topic.getHref();
				if (topicHref != null) {
					return UrlUtil.getHelpURL(topicHref);
				}
				return "../nav/" + j; //$NON-NLS-1$
			}
		}
		return null;
	}
}
