package org.eclipse.update.internal.ui.servlets;

import java.io.*;
import java.net.*;

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
		String serverInfo = ServletsUtil.getServerInfo(servletRequest);
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
		String id = servletRequest.getParameter("id");
		String version = servletRequest.getParameter("version");
		if (serverURL == null) {
			createError(writer, "Update server URL is unknown.");
			return;
		}
		if (id == null) {
			createError(writer, "Feature identifier is unknown.");
			return;
		}
		if (version == null) {
			createError(writer, "Feature version is unknown.");
			return;
		}
		try {
			URL url = new URL(serverURL);
			VersionedIdentifier vid = new VersionedIdentifier(id, version);
			executeInstall(writer, url, vid);
			createInfo(writer);
		} catch (MalformedURLException e) {
			createError(
				writer,
				"Update server URL has incorrect format: "
					+ serverURL.toString());
		}
	}

	private void createError(PrintWriter writer, String message) {
		writer.println(
			"<H3>Error received from Eclipse application server.</H3>");
		writer.println("<b>Message:</b>" + message);
	}

	private void createInfo(PrintWriter writer) {
		writer.println("Request succesfully executed.");
	}

	private void createResponseBody(PrintWriter writer, String serverInfo) {
		writer.println("<H2>Web-triggered Updates</H2>");
	}
	private void executeInstall(
		final PrintWriter writer,
		final URL siteURL,
		final VersionedIdentifier vid) {
		Display display = SWTUtil.getStandardDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				final Shell shell = UpdateUIPlugin.getActiveWorkbenchShell();
				BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
					public void run() {
						doExecuteInstall(writer, shell, siteURL, vid);
					}
				});
			}
		});
	}
	private void doExecuteInstall(
		PrintWriter writer,
		Shell shell,
		URL siteURL,
		VersionedIdentifier vid) {
		IFeature feature =
			DetailsForm.fetchFeatureFromServer(shell, siteURL, vid);
		if (feature == null)
			return;
		IFeature latestOldFeature = findLatestOldFeature(feature);

		if (latestOldFeature != null) {
			if (latestOldFeature
				.getVersionedIdentifier()
				.equals(feature.getVersionedIdentifier())) {
				// Already installed.
				createError(writer, "The feature is already installed.");
				return;
			}
			if (latestOldFeature
				.getVersionedIdentifier()
				.getVersion()
				.isGreaterThan(feature.getVersionedIdentifier().getVersion())) {
				createError(
					writer,
					"The feature is older than the one already installed");
				return;
			}
		}
		PendingChange job;

		if (latestOldFeature != null)
			job = new PendingChange(latestOldFeature, feature);
		else
			job = new PendingChange(feature, PendingChange.INSTALL);
		DetailsForm.executeJob(shell, job);
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
