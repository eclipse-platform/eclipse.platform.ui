/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.ILiveHelpAction;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.webapp.data.*;
import org.osgi.framework.*;

/**
 * Servlet to handle live help action requests
 */
public class LiveHelpServlet extends HttpServlet {
	/**
	 */
	public void init() throws ServletException {
		if (BaseHelpSystem.getMode() == BaseHelpSystem.MODE_INFOCENTER) {
			throw new ServletException();
		}
	}

	/**
	 * Called by the server (via the <code>service</code> method) to
	 * allow a servlet to handle a GET request. 
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		if (BaseHelpSystem.getMode() == BaseHelpSystem.MODE_INFOCENTER) {
			return;
		}
		if (!new WebappPreferences().isActiveHelp()) {
			return;
		}
		req.setCharacterEncoding("UTF-8");
		String pluginID = req.getParameter("pluginID");
		if (pluginID == null)
			return;
		String className = req.getParameter("class");
		if (className == null)
			return;
		String arg = req.getParameter("arg");
		Bundle bundle = Platform.getBundle(pluginID);
		if(bundle==null){
			return;
		}
		
		try {
			Class c = bundle.loadClass(className);
			Object o = c.newInstance();
			if (o != null && o instanceof ILiveHelpAction) {
				ILiveHelpAction helpExt = (ILiveHelpAction) o;
				if (arg != null)
					helpExt.setInitializationString(arg);
				Thread runnableLiveHelp = new Thread(helpExt);
				runnableLiveHelp.setDaemon(true);
				runnableLiveHelp.start();
			}
		} catch (ThreadDeath td) {
			throw td;
		} catch (Exception e) {
		}
	}
	/**
	 *
	 * Called by the server (via the <code>service</code> method)
	 * to allow a servlet to handle a POST request.
	 *
	 * Handle the search requests,
	 *
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		doGet(req, resp);
	}
}
