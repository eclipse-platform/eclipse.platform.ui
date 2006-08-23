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

import org.eclipse.help.IContext;
import org.eclipse.help.IHelpResource;

/*
 * Serializes the a single complete IContext and id into XML, so that it
 * can be transmitted across the network and reconstructed on the other side
 * for remote help.
 */
public class ContextSerializer {

	public static String serialize(IContext context, String id) {
		StringBuffer buf = new StringBuffer();
		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
		buf.append("<context id=\"" + XMLGenerator.xmlEscape(id) + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
		serialize(context.getText(), buf, "   "); //$NON-NLS-1$
		IHelpResource[] topics = context.getRelatedTopics();
		for (int i=0;i<topics.length;++i) {
			serialize(topics[i], buf, "   "); //$NON-NLS-1$
		}
		buf.append("</context>\n"); //$NON-NLS-1$
		return buf.toString();
	}

	private static void serialize(String description, StringBuffer buf, String indent) {
		buf.append(indent + "<description>"); //$NON-NLS-1$
		buf.append(XMLGenerator.xmlEscape(description));
		buf.append(indent + "</description>\n"); //$NON-NLS-1$
	}

	private static void serialize(IHelpResource topic, StringBuffer buf, String indent) {
		buf.append(indent + "<topic"); //$NON-NLS-1$
		if (topic.getHref() != null) {
			buf.append('\n' + indent	+ "      href=\"" + XMLGenerator.xmlEscape(topic.getHref()) + '"'); //$NON-NLS-1$
		}
		if (topic.getLabel() != null) {
			buf.append('\n' + indent + "      label=\"" + XMLGenerator.xmlEscape(topic.getLabel()) + '"'); //$NON-NLS-1$
		}
		buf.append(">\n"); //$NON-NLS-1$
		buf.append(indent + "</topic>\n"); //$NON-NLS-1$
	}
}
