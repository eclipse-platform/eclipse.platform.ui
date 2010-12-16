/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.servlet;

import java.io.*;
import java.lang.reflect.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.internal.webapp.data.*;
import org.osgi.framework.*;

/**
 * Servlet to control Eclipse helpApplication from standalone application.
 * Accepts the following parameters: command=displayHelp | shutdown
 * | install | update | enable | disable | uninstall | search | listFeatures
 * | addSite | removeSite | apply.  
 * href - may be provided if comand==displayHelp.
 * featureId, version, from, to, verifyOnly may be provided for update commands
 */
public class ControlServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String UPDATE_PLUGIN_ID = "org.eclipse.update.core"; //$NON-NLS-1$

	public static final String CMD_DISPLAYHELP = "displayHelp"; //$NON-NLS-1$
	
	public static final String CMD_DISPLAYHELPWINDOW = "displayHelpWindow"; //$NON-NLS-1$

	public static final String CMD_SHUTDOWN = "shutdown"; //$NON-NLS-1$

	public static final String CMD_INSTALL = "install"; //$NON-NLS-1$

	public static final String CMD_UPDATE = "update"; //$NON-NLS-1$

	public static final String CMD_ENABLE = "enable"; //$NON-NLS-1$

	public static final String CMD_DISABLE = "disable"; //$NON-NLS-1$

	public static final String CMD_UNINSTALL = "uninstall"; //$NON-NLS-1$

	public static final String CMD_SEARCH = "search"; //$NON-NLS-1$

	public static final String CMD_LIST = "listFeatures"; //$NON-NLS-1$

	public static final String CMD_ADDSITE = "addSite"; //$NON-NLS-1$

	public static final String CMD_APPLY = "apply"; //$NON-NLS-1$

	public static final String CMD_REMOVESITE = "removeSite"; //$NON-NLS-1$

	public static final String PACKAGE_PREFIX = "org.eclipse.update.standalone."; //$NON-NLS-1$

	public static final String CLASS_INSTALL = PACKAGE_PREFIX
			+ "InstallCommand"; //$NON-NLS-1$

	public static final String CLASS_UPDATE = PACKAGE_PREFIX + "UpdateCommand"; //$NON-NLS-1$

	public static final String CLASS_ENABLE = PACKAGE_PREFIX + "EnableCommand"; //$NON-NLS-1$

	public static final String CLASS_DISABLE = PACKAGE_PREFIX
			+ "DisableCommand"; //$NON-NLS-1$

	public static final String CLASS_UNINSTALL = PACKAGE_PREFIX
			+ "UninstallCommand"; //$NON-NLS-1$

	public static final String CLASS_SEARCH = PACKAGE_PREFIX + "SearchCommand"; //$NON-NLS-1$

	public static final String CLASS_LIST = PACKAGE_PREFIX
			+ "ListFeaturesCommand"; //$NON-NLS-1$

	public static final String CLASS_ADDSITE = PACKAGE_PREFIX
			+ "AddSiteCommand"; //$NON-NLS-1$

	public static final String CLASS_REMOVESITE = PACKAGE_PREFIX
			+ "RemoveSiteCommand"; //$NON-NLS-1$

	public static final String PARAM_FEATUREID = "featureId"; //$NON-NLS-1$

	public static final String PARAM_VERSION = "version"; //$NON-NLS-1$

	public static final String PARAM_FROM = "from"; //$NON-NLS-1$

	public static final String PARAM_TO = "to"; //$NON-NLS-1$

	public static final String PARAM_VERIFYONLY = "verifyOnly"; //$NON-NLS-1$

	private HelpDisplay helpDisplay = null;

	private boolean shuttingDown = false;

	/**
	 * Called by the servlet container to indicate to a servlet that the servlet
	 * is being placed into service.
	 */
	public void init() throws ServletException {
		super.init();
		if (BaseHelpSystem.getMode() == BaseHelpSystem.MODE_STANDALONE) {
			helpDisplay = BaseHelpSystem.getHelpDisplay();
		}
	}

	/**
	 * Called by the server (via the <code>service</code> method) to allow a
	 * servlet to handle a GET request.
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest(req, resp);
	}

	/**
	 * Called by the server (via the <code>service</code> method) to allow a
	 * servlet to handle a POST request.
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest(req, resp);
	}

	private void processRequest(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		req.setCharacterEncoding("UTF-8"); //$NON-NLS-1$

		// for HTTP 1.1
		resp.setHeader("Cache-Control", "no-cache"); //$NON-NLS-1$ //$NON-NLS-2$
		// for HTTP 1.0
		resp.setHeader("Pragma", "no-cache"); //$NON-NLS-1$ //$NON-NLS-2$
		resp.setDateHeader("Expires", 0); //$NON-NLS-1$
		//prevents caching at the proxy server

		if (!UrlUtil.isLocalRequest(req)) {
			// do not allow remote clients to execute this servlet
			return;
		}
		if (!"/help".equals(req.getContextPath()) //$NON-NLS-1$
				|| !"/control".equals(req.getServletPath())) { //$NON-NLS-1$
			// do not allow arbitrary URLs to execute this servlet
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, ""); //$NON-NLS-1$
			return;
		}

		if (shuttingDown) {
			return;
		}

		String command = req.getParameter("command"); //$NON-NLS-1$
		if (command == null) {
			// this should never happen and is invisible to the user
			resp.getWriter().print("No command."); //$NON-NLS-1$
			return;
		}

		if (CMD_SHUTDOWN.equalsIgnoreCase(command)) {
			shutdown();
		} else if (CMD_DISPLAYHELP.equalsIgnoreCase(command)) {
			if (BaseHelpSystem.getMode() == BaseHelpSystem.MODE_STANDALONE) {
				displayHelp(req);
			}
		} else if (CMD_DISPLAYHELPWINDOW.equalsIgnoreCase(command)) {
			if (BaseHelpSystem.getMode() == BaseHelpSystem.MODE_STANDALONE) {
				displayHelp(req);
				HelpApplication.setShutdownOnClose(true);
			}
		} else if (CMD_INSTALL.equalsIgnoreCase(command)
				|| CMD_ENABLE.equalsIgnoreCase(command)
				|| CMD_UPDATE.equalsIgnoreCase(command)
				|| CMD_DISABLE.equalsIgnoreCase(command)
				|| CMD_UNINSTALL.equalsIgnoreCase(command)
				|| CMD_SEARCH.equalsIgnoreCase(command)
				|| CMD_LIST.equalsIgnoreCase(command)
				|| CMD_ADDSITE.equalsIgnoreCase(command)
				|| CMD_REMOVESITE.equalsIgnoreCase(command)
				|| CMD_APPLY.equalsIgnoreCase(command)) {
			updateDocs(command, req);
		} else {
			// this should never happen and is invisible to the user
			resp.getWriter().print("Unrecognized command."); //$NON-NLS-1$
		}
	}

	private void updateDocs(String command, HttpServletRequest req) {
		Bundle bundle = Platform.getBundle(UPDATE_PLUGIN_ID);
		if (bundle == null) {
			// no update plugin present
			return;
		}
		try {
			String className = getStandaloneClassName(command);
			if (className == null) {
				System.out.println("No class name for command " + command); //$NON-NLS-1$
				return;
			}
			Class c = bundle.loadClass(className);
			if (c == null) {
				System.out.println("No class for command " + command); //$NON-NLS-1$
				return;
			}
			Class[] parameterTypes = getParameterTypes(className);
			Constructor constr = c.getConstructor(parameterTypes);
			if (constr == null) {
				System.out.println("No expected constructor for command " //$NON-NLS-1$
						+ command);
				return;
			}
			Method m;
			if (!CMD_APPLY.equalsIgnoreCase(command)) {
				m = c.getMethod("run", new Class[]{}); //$NON-NLS-1$
			} else {
				m = c.getMethod("applyChangesNow", new Class[]{}); //$NON-NLS-1$
			}
			Object[] initargs = getInitArgs(className, req);
			Object o = constr.newInstance(initargs);
			Object ret = m.invoke(o, new Object[]{});
			if (!CMD_APPLY.equalsIgnoreCase(command) &&((Boolean)ret).equals(Boolean.FALSE)){
				System.out.println("Command not executed."); //$NON-NLS-1$
			} else {
				System.out.println("Command executed."); //$NON-NLS-1$
			}
		} catch (Exception e) {
			Throwable t = e;
			if (e instanceof InvocationTargetException) {
				t = ((InvocationTargetException) e).getTargetException();
			}
			System.out.println(t.getLocalizedMessage());
			//t.printStackTrace();
		}
	}

	private String getStandaloneClassName(String command) {
		if (CMD_INSTALL.equalsIgnoreCase(command))
			return CLASS_INSTALL;
		else if (CMD_UPDATE.equalsIgnoreCase(command))
			return CLASS_UPDATE;
		else if (CMD_ENABLE.equalsIgnoreCase(command))
			return CLASS_ENABLE;
		else if (CMD_DISABLE.equalsIgnoreCase(command))
			return CLASS_DISABLE;
		else if (CMD_UNINSTALL.equalsIgnoreCase(command))
			return CLASS_UNINSTALL;
		else if (CMD_SEARCH.equalsIgnoreCase(command))
			return CLASS_SEARCH;
		else if (CMD_LIST.equalsIgnoreCase(command)
				|| CMD_APPLY.equalsIgnoreCase(command))
			return CLASS_LIST;
		else if (CMD_ADDSITE.equalsIgnoreCase(command))
			return CLASS_ADDSITE;
		else if (CMD_REMOVESITE.equalsIgnoreCase(command))
			return CLASS_REMOVESITE;
		else
			return null;
	}

	private Class[] getParameterTypes(String className) {
		if (CLASS_INSTALL.equals(className))
			return new Class[]{String.class, String.class, String.class,
					String.class, String.class};
		else if (CLASS_UPDATE.equals(className))
			return new Class[]{String.class, String.class, String.class};
		else if (CLASS_ENABLE.equals(className)
				|| CLASS_DISABLE.equals(className)
				|| CLASS_UNINSTALL.equals(className))
			return new Class[]{String.class, String.class, String.class,
					String.class};
		else
			return new Class[]{String.class};
	}

	private Object[] getInitArgs(String className, HttpServletRequest req) {
		String featureId = req.getParameter(PARAM_FEATUREID); 
		String version = req.getParameter(PARAM_VERSION); 
		String fromSite = req.getParameter(PARAM_FROM); 
		String toSite = req.getParameter(PARAM_TO); 
		String verifyOnly = req.getParameter(PARAM_VERIFYONLY); 
		if (CLASS_INSTALL.equals(className))
			return new Object[]{featureId, version, fromSite, toSite,
					verifyOnly};
		else if (CLASS_UPDATE.equals(className))
			return new Object[]{featureId, version, verifyOnly};
		else if (CLASS_ENABLE.equals(className)
				|| CLASS_DISABLE.equals(className)
				|| CLASS_UNINSTALL.equals(className))
			return new Object[]{featureId, version, toSite, verifyOnly};
		else if (CLASS_REMOVESITE.equals(className))
			return new Object[]{toSite};
		else
			return new Object[]{fromSite};
	}

	/**
	 * Shuts-down Eclipse helpApplication.
	 */
	private void shutdown() {
		shuttingDown = true;
		HelpApplication.stopHelp();
	}

	/**
	 * Displays help.
	 * 
	 * @param req
	 *            HttpServletRequest that might contain href parameter, which is
	 *            the resource to display
	 */
	private void displayHelp(HttpServletRequest req) {
		String href = req.getParameter("href"); //$NON-NLS-1$
		if (href != null) {
			helpDisplay.displayHelpResource(href, false);
		} else {
			helpDisplay.displayHelp(false);
		}
	}
}
