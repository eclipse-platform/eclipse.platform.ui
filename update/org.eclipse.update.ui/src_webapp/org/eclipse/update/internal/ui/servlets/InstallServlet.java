/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.servlets;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.update.core.VersionedIdentifier;
import org.eclipse.update.internal.search.SiteSearchCategory;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.parts.SWTUtil;
import org.eclipse.update.internal.ui.servlets.ServletsUtil;
import org.eclipse.update.internal.ui.wizards.InstallWizard;
import org.eclipse.update.operations.OperationsManager;
import org.eclipse.update.search.BackLevelFilter;
import org.eclipse.update.search.EnvironmentFilter;
import org.eclipse.update.search.UpdateSearchRequest;
import org.eclipse.update.search.UpdateSearchScope;
import org.eclipse.update.search.VersionedIdentifiersFilter;

/**
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class InstallServlet extends HttpServlet {
	private ServletConfig servletConfig;
	public static final String SERVLET_NAME = "/InstallServlet"; //$NON-NLS-1$

	public void init(ServletConfig config) throws ServletException {
		this.servletConfig = config;
	}

	public void destroy() {
	}

	public ServletConfig getServletConfig() {
		return servletConfig;
	}

	public String getServletInfo() {
		return "Eclipse Install servlet"; //$NON-NLS-1$
	}

	public void service(
		HttpServletRequest servletRequest,
		HttpServletResponse servletResponse)
		throws ServletException, IOException {
		PrintWriter writer =
			ServletsUtil.createResponsePrologue(servletResponse);
		execute(writer, servletRequest);
		ServletsUtil.createResponseEpilogue(
			servletRequest,
			servletResponse,
			writer);
	}

	/**
	 * Method execute.
	 * @param servletRequest
	 */
	private void execute(
		PrintWriter writer,
		HttpServletRequest servletRequest) {
		String serverURL = servletRequest.getParameter("server"); //$NON-NLS-1$
		String license = servletRequest.getParameter("license"); //$NON-NLS-1$
		boolean needLicensePage = true;
		if (license != null && license.equalsIgnoreCase("false")) //$NON-NLS-1$
			needLicensePage = false;

		String[] versionedIds = servletRequest.getParameterValues("feature"); //$NON-NLS-1$

		if (serverURL == null) {
			createPageError(writer, UpdateUI.getString("InstallServlet.unknownServerURL")); //$NON-NLS-1$
			return;
		}
		if (versionedIds == null) {
			createPageError(writer, UpdateUI.getString("InstallServlet.noFeatures")); //$NON-NLS-1$
			return;
		}
		if (OperationsManager.isInProgress()) {
			ServletsUtil.createError(writer, UpdateUI.getString("InstallServlet.inProgress"), null); //$NON-NLS-1$
			return;
		}
//		if (DetailsForm.isInProgress()) {
//			ServletsUtil.createError(writer, UpdateUI.getString("InstallServlet.inProgress"), null);
//			return;
//		}
		try {
			URL url = new URL(serverURL);
			VersionedIdentifier[] vids =
				computeVersionedIdentifiers(versionedIds);
			boolean success =
				executeInstall(writer, url, vids, needLicensePage);
			if (success)
				ServletsUtil.createInfo(writer);
		} catch (MalformedURLException e) {
			createPageError(writer, UpdateUI.getFormattedMessage("InstallServlet.incorrectURLFormat", //$NON-NLS-1$
			serverURL.toString()));
		}
	}

	private void createPageError(PrintWriter writer, String problem) {
		ServletsUtil.createError(writer, problem, UpdateUI.getString("InstallServlet.contactWebmaster")); //$NON-NLS-1$
	}

	private VersionedIdentifier[] computeVersionedIdentifiers(String[] array) {
		ArrayList result = new ArrayList();
		for (int i = 0; i < array.length; i++) {
			String id_version = array[i];
			int sep = id_version.lastIndexOf('_');
			if (sep == -1)
				continue;
			String id = id_version.substring(0, sep);
			String version = id_version.substring(sep + 1);
			VersionedIdentifier vid = new VersionedIdentifier(id, version);
			result.add(vid);
		}
		return (VersionedIdentifier[]) result.toArray(
			new VersionedIdentifier[result.size()]);
	}

	private boolean executeInstall(
		final PrintWriter writer,
		final URL siteURL,
		final VersionedIdentifier[] vids,
		final boolean needLicensePage) {

		Display display = SWTUtil.getStandardDisplay();
		final boolean[] result = new boolean[] { false };

		display.syncExec(new Runnable() {
			public void run() {
				final Shell shell = UpdateUI.getActiveWorkbenchShell();
				BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
					public void run() {
						result[0] =
							doExecuteInstall(
								writer,
								shell,
								siteURL,
								vids,
								needLicensePage);
					}
				});
			}
		});
		return result[0];
	}

	private boolean doExecuteInstall(
		PrintWriter writer,
		final Shell shell,
		URL siteURL,
		VersionedIdentifier[] vids,
		final boolean needLicensePage) {
			
		shell.forceActive();

		UpdateSearchScope searchScope = new UpdateSearchScope();
		searchScope.addSearchSite(
			siteURL.toString(),
			siteURL,
			new String[0]);

		final UpdateSearchRequest searchRequest =
			new UpdateSearchRequest(
				new SiteSearchCategory(),
				searchScope);
	
		searchRequest.addFilter(new VersionedIdentifiersFilter(vids));
		searchRequest.addFilter(new EnvironmentFilter());
		searchRequest.addFilter(new BackLevelFilter());

		shell.getDisplay().asyncExec(new Runnable() {
			public void run() {
				openWizard(shell, searchRequest);
			}
		});	
		return true;
	}


	private void openWizard(Shell shell, UpdateSearchRequest searchRequest) {
		InstallWizard wizard = new InstallWizard(searchRequest);
		WizardDialog dialog = new ResizableWizardDialog(shell, wizard);
		dialog.create();
//		dialog.getShell().setText(
//			UpdateUI.getString(KEY_OPTIONAL_INSTALL_TITLE));
		dialog.getShell().setSize(600, 500);
		dialog.open();
		if (wizard.isSuccessfulInstall())
			UpdateUI.requestRestart();
	}
}
