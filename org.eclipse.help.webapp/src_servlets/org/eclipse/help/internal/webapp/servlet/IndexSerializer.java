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

import org.eclipse.help.IIndex;
import org.eclipse.help.IIndexContribution;
import org.eclipse.help.IIndexEntry;
import org.eclipse.help.INode;
import org.eclipse.help.ITopic;

/*
 * Serializes the complete contents of IIndexContributions into XML, so that it
 * can be transmitted across the network and reconstructed on the other side
 * for remote help.
 */
public class IndexSerializer {

	public static String serialize(IIndexContribution[] contributions, String locale) {
		StringBuffer buf = new StringBuffer();
		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
		buf.append("<indexContributions>\n"); //$NON-NLS-1$
		for (int i = 0; i < contributions.length; ++i) {
			serialize(contributions[i], buf, "   "); //$NON-NLS-1$
		}
		buf.append("</indexContributions>\n"); //$NON-NLS-1$
		return buf.toString();
	}

	private static void serialize(IIndexContribution contribution, StringBuffer buf, String indent) {
		buf.append(indent + "<indexContribution"); //$NON-NLS-1$
		if (contribution.getId() != null) {
			buf.append('\n' + indent	+ "      id=\"" + XMLGenerator.xmlEscape(contribution.getId()) + '"'); //$NON-NLS-1$
		}
		if (contribution.getLocale() != null) {
			buf.append('\n' + indent + "      locale=\"" + XMLGenerator.xmlEscape(contribution.getLocale()) + '"'); //$NON-NLS-1$
		}
		buf.append(">\n"); //$NON-NLS-1$
		serialize(contribution.getIndex(), buf, indent + "   "); //$NON-NLS-1$
		buf.append(indent + "</indexContribution>\n"); //$NON-NLS-1$
	}

	private static void serialize(IIndex index, StringBuffer buf, String indent) {
		buf.append(indent + "<index>\n"); //$NON-NLS-1$
		serialize(index.getChildren(), buf, indent + "   "); //$NON-NLS-1$
		buf.append(indent + "</index>\n"); //$NON-NLS-1$
	}

	private static void serialize(INode[] nodes, StringBuffer buf, String indent) {
		for (int i = 0; i < nodes.length; ++i) {
			INode node = nodes[i];
			if (node instanceof IIndexEntry) {
				serialize((IIndexEntry) node, buf, indent);
			} else if (node instanceof ITopic) {
				serialize((ITopic) node, buf, indent);
			} else if (node instanceof IIndex) {
				serialize((IIndex) node, buf, indent);
			}
		}
	}

	private static void serialize(IIndexEntry entry, StringBuffer buf, String indent) {
		buf.append(indent + "<entry"); //$NON-NLS-1$
		if (entry.getKeyword() != null) {
			buf.append('\n' + indent	+ "      keyword=\"" + XMLGenerator.xmlEscape(entry.getKeyword()) + '"'); //$NON-NLS-1$
		}
		buf.append(">\n"); //$NON-NLS-1$
		serialize(entry.getChildren(), buf, indent + "   "); //$NON-NLS-1$
		buf.append(indent + "</entry>\n"); //$NON-NLS-1$
	}
	
	private static void serialize(ITopic topic, StringBuffer buf, String indent) {
		buf.append(indent + "<topic"); //$NON-NLS-1$
		if (topic.getHref() != null) {
			buf.append('\n' + indent	+ "      href=\"" + XMLGenerator.xmlEscape(topic.getHref()) + '"'); //$NON-NLS-1$
		}
		if (topic.getLabel() != null) {
			buf.append('\n' + indent + "      label=\"" + XMLGenerator.xmlEscape(topic.getLabel()) + '"'); //$NON-NLS-1$
		}
		buf.append(">\n"); //$NON-NLS-1$
		serialize(topic.getChildren(), buf, indent + "   "); //$NON-NLS-1$
		buf.append(indent + "</topic>\n"); //$NON-NLS-1$
	}
}
