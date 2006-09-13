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

import org.eclipse.help.internal.xhtml.UATopicExtension;
import org.eclipse.help.internal.xhtml.UATopicReplace;

public class ExtensionSerializer {

	public static String serialize(Collection topicExtensions, Collection topicReplaces, String locale) {
		StringBuffer buf = new StringBuffer();
		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
		buf.append("<extensions>\n"); //$NON-NLS-1$
		Iterator iter = topicExtensions.iterator();
		while (iter.hasNext()) {
			UATopicExtension ext = (UATopicExtension)iter.next();
			serialize(ext, buf, "   "); //$NON-NLS-1$
		}
		iter = topicReplaces.iterator();
		while (iter.hasNext()) {
			UATopicReplace replace = (UATopicReplace)iter.next();
			serialize(replace, buf, "   "); //$NON-NLS-1$
		}
		buf.append("</extensions>\n"); //$NON-NLS-1$
		return buf.toString();
	}

	private static void serialize(UATopicExtension ext, StringBuffer buf, String indent) {
		buf.append(indent + "<topicExtension"); //$NON-NLS-1$
		if (ext.getTargetHref() != null) {
			buf.append('\n' + indent	+ "      targetHref=\"" + XMLGenerator.xmlEscape(ext.getTargetHref()) + '"'); //$NON-NLS-1$
		}
		if (ext.getTargetAnchorId() != null) {
			buf.append('\n' + indent	+ "      targetAnchorId=\"" + XMLGenerator.xmlEscape(ext.getTargetAnchorId()) + '"'); //$NON-NLS-1$
		}
		if (ext.getContentHref() != null) {
			buf.append('\n' + indent	+ "      contentHref=\"" + XMLGenerator.xmlEscape(ext.getContentHref()) + '"'); //$NON-NLS-1$
		}
		if (ext.getContentElementId() != null) {
			buf.append('\n' + indent + "      contentElementId=\"" + XMLGenerator.xmlEscape(ext.getContentElementId()) + '"'); //$NON-NLS-1$
		}
		buf.append(">\n"); //$NON-NLS-1$
		buf.append(indent + "</topicExtension>\n"); //$NON-NLS-1$
	}

	private static void serialize(UATopicReplace replace, StringBuffer buf, String indent) {
		buf.append(indent + "<topicReplace"); //$NON-NLS-1$
		if (replace.getTargetHref() != null) {
			buf.append('\n' + indent	+ "      targetHref=\"" + XMLGenerator.xmlEscape(replace.getTargetHref()) + '"'); //$NON-NLS-1$
		}
		if (replace.getTargetElementId() != null) {
			buf.append('\n' + indent	+ "      targetElementId=\"" + XMLGenerator.xmlEscape(replace.getTargetElementId()) + '"'); //$NON-NLS-1$
		}
		if (replace.getContentHref() != null) {
			buf.append('\n' + indent	+ "      contentHref=\"" + XMLGenerator.xmlEscape(replace.getContentHref()) + '"'); //$NON-NLS-1$
		}
		if (replace.getContentElementId() != null) {
			buf.append('\n' + indent + "      contentElementId=\"" + XMLGenerator.xmlEscape(replace.getContentElementId()) + '"'); //$NON-NLS-1$
		}
		buf.append(">\n"); //$NON-NLS-1$
		buf.append(indent + "</topicReplace>\n"); //$NON-NLS-1$
	}
}
