/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.doc.internal.linkchecker;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.help.internal.webapp.servlet.FilterHTMLHeadOutputStream;
import org.eclipse.help.internal.webapp.servlet.FilterUtils;
import org.eclipse.help.webapp.IFilter;

/**
 * This class inserts a CSSs for narrow and disabled CSSs when called from the
 * dynamic help view.
 */
public class AddScriptFilter implements IFilter {
	/*
	 * @see IFilter#filter(HttpServletRequest, OutputStream)
	 */
	public OutputStream filter(HttpServletRequest req, OutputStream out) {
		String relativePath = FilterUtils.getRelativePathPrefix(req);
		StringBuffer script = new StringBuffer();	
		script.append("\n<script type=\"text/javascript\" src=\"");
		script.append(relativePath);
		script.append("content/org.eclipse.ua.tests.doc/checkdoc.js\"> </script>"); //$NON-NLS-1$

		try {
			return new FilterHTMLHeadOutputStream(
					out,
					script.toString().getBytes("ASCII")); //$NON-NLS-1$
		} catch (UnsupportedEncodingException uee) {
			return out;
		}
	}

}
