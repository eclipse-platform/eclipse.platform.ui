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
package org.eclipse.update.internal.ui.servlets;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.forms.DetailsForm;
import org.eclipse.update.internal.ui.model.PendingChange;
import org.eclipse.update.internal.ui.parts.SWTUtil;

/**
 * @author dejan
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
		PrintWriter writer,
		URL siteURL,
		VersionedIdentifier[] vids,
		boolean needLicensePage) {
		if (vids.length == 1) {
			return executeInstall(writer, siteURL, vids[0], needLicensePage);
		} else {
			ServletsUtil.createError(writer, UpdateUI.getString("InstallServlet.multipleInstall"), null); //$NON-NLS-1$
			return false;
		}
	}

	private boolean executeInstall(
		final PrintWriter writer,
		final URL siteURL,
		final VersionedIdentifier vid,
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
								vid,
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
		VersionedIdentifier vid,
		final boolean needLicensePage) {
		shell.forceActive();
		IFeature feature =
			DetailsForm.fetchFeatureFromServer(shell, siteURL, vid);
		if (feature == null)
			return false;
		IFeature latestOldFeature = findLatestOldFeature(feature);

		if (latestOldFeature != null) {
			if (latestOldFeature
				.getVersionedIdentifier()
				.equals(feature.getVersionedIdentifier())) {
				// Already installed.
				ServletsUtil.createError(writer, UpdateUI.getString("InstallServlet.alreadyInstalled"), //$NON-NLS-1$
				UpdateUI.getString("InstallServlet.alreadyHaveIt")); //$NON-NLS-1$
				return false;
			}
			if (latestOldFeature
				.getVersionedIdentifier()
				.getVersion()
				.isGreaterThan(feature.getVersionedIdentifier().getVersion())) {
				ServletsUtil.createError(writer, UpdateUI.getString("InstallServlet.olderFeature"), //$NON-NLS-1$
				UpdateUI.getString("InstallServlet.nothing2")); //$NON-NLS-1$
				return false;
			}
		}
		final PendingChange job;

		if (latestOldFeature != null)
			job = new PendingChange(latestOldFeature, feature);
		else
			job = new PendingChange(feature, PendingChange.INSTALL);
		shell.getDisplay().asyncExec(new Runnable() {
			public void run() {
				DetailsForm.executeJob(shell, job, needLicensePage);
			}
		});
		return true;
	}

	private IFeature findLatestOldFeature(IFeature feature) {
		IFeature[] oldFeatures = UpdateUI.getInstalledFeatures(feature);
		if (oldFeatures.length == 0)
			return null;

		IFeature latest = null;
		for (int i = 0; i < oldFeatures.length; i++) {
			IFeature curr = oldFeatures[i];
			if (latest == null
				|| curr.getVersionedIdentifier().getVersion().isGreaterThan(
					latest.getVersionedIdentifier().getVersion())) {
				latest = curr;
			}
		}
		return latest;
	}
}
