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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.help.internal.base.remote.RemoteHelp;
import org.eclipse.help.internal.webapp.data.UrlUtil;
import org.eclipse.help.internal.xhtml.DynamicXHTMLProcessor;

/*
 * A filter that performs any required XHTML processing, if the content is
 * XHTML. Using a servlet path of /rtopic (raw topic) will turn off this
 * filter.
 */
public class DynamicXHTMLFilter implements IFilter {

	/* (non-Javadoc)
	 * @see org.eclipse.help.internal.webapp.servlet.IFilter#filter(javax.servlet.http.HttpServletRequest, java.io.OutputStream)
	 */
	public OutputStream filter(final HttpServletRequest req, final OutputStream out) {
		final String uri = req.getRequestURI();
		if (uri == null || !uri.endsWith("html") && !uri.endsWith("htm")) { //$NON-NLS-1$ //$NON-NLS-2$
			return out;
		}
		
		/*
		 * Remote help does all XHTML processing on the client side, so we
		 * want raw XHTML topics from the remote help server.
		 */
		if ("/rtopic".equals(req.getServletPath())) { //$NON-NLS-1$
			return out;
		}

		/*
		 * Buffers the contents of the document until stream is closed, where
		 * the document is processed then pushed through the filter.
		 */
		ByteArrayOutputStream out2 = new ByteArrayOutputStream() {
			public void close() throws IOException {
				super.close();
				byte[] buf = toByteArray();
				ByteArrayInputStream in = new ByteArrayInputStream(buf);
				
				String locale = UrlUtil.getLocale(req, null);
				String contextPath = req.getContextPath();
				String servletPath = req.getServletPath();
				String path = uri;
				if (path.startsWith(contextPath)) {
					path = path.substring(contextPath.length());
				}
				if (path.startsWith(servletPath)) {
					path = path.substring(servletPath.length());
				}
				int index = path.indexOf('/', 1);
				String pluginId = path.substring(1, index);
				String file = path.substring(index + 1);
				
				InputStream in2 = DynamicXHTMLProcessor.process(pluginId, file, in, locale, RemoteHelp.isAllowed());
				transferContent(in2, out);
			}
		};
		return out2;
	}

	/*
	 * Forward the contents of the input stream to the output stream.
	 */
	private void transferContent(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[4096];
		int len = 0;
		while (true) {
			len = in.read(buffer);
			if (len == -1)
				break;
			out.write(buffer, 0, len);
		}
	}
}
