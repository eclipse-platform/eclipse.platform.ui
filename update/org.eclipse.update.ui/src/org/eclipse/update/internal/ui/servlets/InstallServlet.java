package org.eclipse.update.internal.ui.servlets;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
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
	public static final String SERVLET_NAME = "/InstallServlet";

	public void init(ServletConfig config) throws ServletException {
		this.servletConfig = config;
	}

	public void destroy() {
	}

	public ServletConfig getServletConfig() {
		return servletConfig;
	}

	public String getServletInfo() {
		return "Eclipse Install servlet";
	}

	public void service(
		HttpServletRequest servletRequest,
		HttpServletResponse servletResponse)
		throws ServletException, IOException {
		PrintWriter writer =
			ServletsUtil.createResponsePrologue(servletResponse);
		execute(writer, servletRequest);
		ServletsUtil.createResponseEpilogue(servletResponse, writer);
	}

	/**
	 * Method execute.
	 * @param servletRequest
	 */
	private void execute(
		PrintWriter writer,
		HttpServletRequest servletRequest) {
		String serverURL = servletRequest.getParameter("server");
		String license = servletRequest.getParameter("license");
		boolean needLicensePage=true;
		if (license!=null && license.equalsIgnoreCase("false"))
			needLicensePage=false;
		
		String [] versionedIds = servletRequest.getParameterValues("feature");
		
		if (serverURL == null) {
			createError(writer, "Update server URL is unknown.");
			return;
		}
		if (versionedIds == null) {
			createError(writer, "No features to install.");
			return;
		}
		try {
			URL url = new URL(serverURL);
			VersionedIdentifier [] vids = computeVersionedIdentifiers(versionedIds);
			boolean success = executeInstall(writer, url, vids, needLicensePage);
			if (success) createInfo(writer);
		} catch (MalformedURLException e) {
			createError(
				writer,
				"Update server URL has incorrect format: "
					+ serverURL.toString());
		}
	}
	
	private VersionedIdentifier[] computeVersionedIdentifiers(String [] array) {
		ArrayList result = new ArrayList();
		for (int i=0; i<array.length; i++) {
			String id_version = array[i];
			int sep = id_version.lastIndexOf('_');
			if (sep== -1) continue;
			String id = id_version.substring(0, sep);
			String version = id_version.substring(sep+1);
			VersionedIdentifier vid = new VersionedIdentifier(id, version);
			result.add(vid);
		}
		return (VersionedIdentifier[])result.toArray(new VersionedIdentifier[result.size()]);
	}

	private void createError(PrintWriter writer, String message) {
		writer.println(
			"<H3>Error received from Eclipse application server.</H3>");
		writer.println("<b>Message:</b>" + message);
	}

	private void createInfo(PrintWriter writer) {
		writer.println("Request succesfully executed.");
	}
	private boolean executeInstall(PrintWriter writer,
								URL siteURL,
								VersionedIdentifier [] vids,
								boolean needLicensePage) {
		if (vids.length==1) {
			return executeInstall(writer, siteURL, vids[0], needLicensePage);
		}
		else {
			createError(writer, "Multiple feature install not supported");
			return false;
		}
	}

	private boolean executeInstall(
		final PrintWriter writer,
		final URL siteURL,
		final VersionedIdentifier vid,
		final boolean needLicensePage) {
		Display display = SWTUtil.getStandardDisplay();
		final boolean [] result = new boolean[] { false };
		
		display.syncExec(new Runnable() {
			public void run() {
				final Shell shell = UpdateUIPlugin.getActiveWorkbenchShell();
				BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
					public void run() {
						result[0] = doExecuteInstall(writer, shell, siteURL, vid, needLicensePage);
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
				createError(writer, "The feature is already installed.");
				return false;
			}
			if (latestOldFeature
				.getVersionedIdentifier()
				.getVersion()
				.isGreaterThan(feature.getVersionedIdentifier().getVersion())) {
				createError(
					writer,
					"The feature is older than the one already installed");
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
		IFeature[] oldFeatures = UpdateUIPlugin.getInstalledFeatures(feature);
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
