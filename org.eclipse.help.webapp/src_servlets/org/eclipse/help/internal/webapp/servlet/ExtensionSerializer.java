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

import org.eclipse.help.IContentExtension;

public class ExtensionSerializer {

	public static String serialize(IContentExtension[] extensions) {
		StringBuffer buf = new StringBuffer();
		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
		buf.append("<contentExtensions>\n"); //$NON-NLS-1$
		for (int i=0;i<extensions.length;++i) {
			serialize(extensions[i], buf, "   "); //$NON-NLS-1$
		}
		buf.append("</contentExtensions>\n"); //$NON-NLS-1$
		return buf.toString();
	}

	private static void serialize(IContentExtension ext, StringBuffer buf, String indent) {
		buf.append(indent + "<contentExtension"); //$NON-NLS-1$
		if (ext.getContent() != null) {
			buf.append('\n' + indent	+ "      content=\"" + XMLGenerator.xmlEscape(ext.getContent()) + '"'); //$NON-NLS-1$
		}
		if (ext.getPath() != null) {
			buf.append('\n' + indent	+ "      path=\"" + XMLGenerator.xmlEscape(ext.getPath()) + '"'); //$NON-NLS-1$
		}
		buf.append('\n' + indent	+ "      type=\"" + ext.getType() + '"'); //$NON-NLS-1$
		buf.append(">\n"); //$NON-NLS-1$
		buf.append(indent + "</contentExtension>\n"); //$NON-NLS-1$
	}
}
