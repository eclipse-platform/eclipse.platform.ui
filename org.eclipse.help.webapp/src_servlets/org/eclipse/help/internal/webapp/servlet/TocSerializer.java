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

import org.eclipse.help.IAnchor;
import org.eclipse.help.IInclude;
import org.eclipse.help.INode;
import org.eclipse.help.IToc;
import org.eclipse.help.ITocContribution;
import org.eclipse.help.ITopic;

/*
 * Serializes the complete contents of ITocContributions into XML, so that it
 * can be transmitted across the network and reconstructed on the other side
 * for remote help.
 */
public class TocSerializer {

	public static String serialize(ITocContribution[] contributions, String locale) {
		StringBuffer buf = new StringBuffer();
		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
		buf.append("<tocContributions>\n"); //$NON-NLS-1$
		for (int i = 0; i < contributions.length; ++i) {
			serialize(contributions[i], buf, "   "); //$NON-NLS-1$
		}
		buf.append("</tocContributions>\n"); //$NON-NLS-1$
		return buf.toString();
	}

	private static void serialize(ITocContribution contribution, StringBuffer buf, String indent) {
		buf.append(indent + "<tocContribution"); //$NON-NLS-1$
		if (contribution.getCategoryId() != null) {
			buf.append('\n' + indent	+ "      categoryId=\"" + XMLGenerator.xmlEscape(contribution.getCategoryId()) + '"'); //$NON-NLS-1$
		}
		if (contribution.getId() != null) {
			buf.append('\n' + indent	+ "      id=\"" + XMLGenerator.xmlEscape(contribution.getId()) + '"'); //$NON-NLS-1$
		}
		if (contribution.getLinkTo() != null) {
			buf.append('\n' + indent	+ "      linkTo=\"" + XMLGenerator.xmlEscape(contribution.getLinkTo()) + '"'); //$NON-NLS-1$
		}
		if (contribution.getLocale() != null) {
			buf.append('\n' + indent + "      locale=\"" + XMLGenerator.xmlEscape(contribution.getLocale()) + '"'); //$NON-NLS-1$
		}
		buf.append('\n' + indent + "      isPrimary=\"" + contribution.isPrimary() + '"'); //$NON-NLS-1$
		buf.append(">\n"); //$NON-NLS-1$
		serialize(contribution.getToc(), buf, indent + "   "); //$NON-NLS-1$
		serialize(contribution.getExtraDocuments(), buf, indent + "   "); //$NON-NLS-1$
		buf.append(indent + "</tocContribution>\n"); //$NON-NLS-1$
	}

	private static void serialize(IToc toc, StringBuffer buf, String indent) {
		buf.append(indent + "<toc"); //$NON-NLS-1$
		if (toc.getLabel() != null) {
			buf.append('\n' + indent	+ "      label=\"" + XMLGenerator.xmlEscape(toc.getLabel()) + '"'); //$NON-NLS-1$
		}
		ITopic topic = toc.getTopic(null);
		if (topic != null && topic.getHref() != null) {
			buf.append('\n' + indent + "      topic=\"" + XMLGenerator.xmlEscape(topic.getHref()) + '"'); //$NON-NLS-1$
		}
		if (toc.getHref() != null) {
			buf.append('\n' + indent + "      href=\"" + XMLGenerator.xmlEscape(toc.getHref()) + '"'); //$NON-NLS-1$
		}
		buf.append(">\n"); //$NON-NLS-1$
		serialize(toc.getChildren(), buf, indent + "   "); //$NON-NLS-1$
		buf.append(indent + "</toc>\n"); //$NON-NLS-1$
	}

	private static void serialize(INode[] nodes, StringBuffer buf, String indent) {
		for (int i = 0; i < nodes.length; ++i) {
			INode node = nodes[i];
			if (node instanceof ITopic) {
				serialize((ITopic) node, buf, indent);
			} else if (node instanceof org.eclipse.help.IFilter) {
				serialize((org.eclipse.help.IFilter) node, buf, indent);
			} else if (node instanceof IAnchor) {
				serialize((IAnchor) node, buf, indent);
			} else if (node instanceof IInclude) {
				serialize((IInclude) node, buf, indent);
			} else if (node instanceof IToc) {
				serialize((IToc) node, buf, indent);
			}
		}
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

	private static void serialize(org.eclipse.help.IFilter filter, StringBuffer buf, String indent) {
		if (filter.getExpression() != null) {
			buf.append(indent + "<filter expression=\"" + XMLGenerator.xmlEscape(filter.getExpression()) + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
			serialize(filter.getChildren(), buf, indent + "   "); //$NON-NLS-1$
			buf.append(indent + "</filter>\n"); //$NON-NLS-1$
		}
		else {
			serialize(filter.getChildren(), buf, indent);
		}
	}

	private static void serialize(IAnchor anchor, StringBuffer buf, String indent) {
		if (anchor.getId() != null) {
			buf.append(indent + "<anchor id=\"" + XMLGenerator.xmlEscape(anchor.getId()) + "\"/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private static void serialize(IInclude include, StringBuffer buf, String indent) {
		if (include.getTarget() != null) {
			buf.append(indent + "<include target=\"" + XMLGenerator.xmlEscape(include.getTarget()) + "\"/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private static void serialize(String[] extraDocs, StringBuffer buf, String indent) {
		for (int i = 0; i < extraDocs.length; ++i) {
			serialize(extraDocs[i], buf, indent);
		}
	}

	private static void serialize(String extraDoc, StringBuffer buf,	String indent) {
		if (extraDoc != null) {
			buf.append(indent + "<extraDoc href=\"" + XMLGenerator.xmlEscape(extraDoc) + "\"/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}
