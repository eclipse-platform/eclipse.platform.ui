/*
 * Created on Apr 18, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.update.internal.ui.wizards;

import java.io.*;
import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.operations.*;
import org.eclipse.update.internal.ui.UpdateUI;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MoreInfoGenerator {
	private File tempFile;
	
	private File getTempFile() throws IOException {
		if (tempFile==null) {
			tempFile = File.createTempFile("MoreInfo", ".htm"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return tempFile;
	}
	
	public String createURL(IInstallFeatureOperation job) {
		try {
			File file = getTempFile();
			OutputStream stream = new FileOutputStream(file);
			PrintWriter writer = new PrintWriter(stream);
			createMoreInfoContent(job, writer);
			writer.flush();
			stream.close();
			return file.getAbsolutePath();
		} catch (IOException e) {
			UpdateUI.logException(e);
			return null;
		}
	}
	
	private void createMoreInfoContent(IInstallFeatureOperation job, PrintWriter out) {
		IFeature feature = job.getFeature();
		out.println(
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">"); //$NON-NLS-1$
		out.print("<HEAD>"); //$NON-NLS-1$
		out.println(
			"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">"); //$NON-NLS-1$
		out.println("<TITLE>"+feature.getLabel()+"</TITLE>"); //$NON-NLS-1$ //$NON-NLS-2$
		out.println("</HEAD>"); //$NON-NLS-1$
		out.println("<HTML>"); //$NON-NLS-1$
		out.println("<BODY>"); //$NON-NLS-1$
		out.println("<H1><CENTER>" + feature.getLabel() + "</CENTER></H1>"); //$NON-NLS-1$ //$NON-NLS-2$
		createProfileTable(job, out);
		createDescription(feature, out);
		out.println("<hr>"); //$NON-NLS-1$
		createEnvironmentTable(job, out);
		out.println("<hr>"); //$NON-NLS-1$
		if (feature.getLicense()!=null) {
			IURLEntry license = feature.getLicense();
			out.println("<h2>" + UpdateUI.getString("MoreInfoGenerator.license") + "</h2>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (license.getURL()!=null) {
				out.println("<p><a href=\""+license.getURL()+"\">" + UpdateUI.getString("MoreInfoGenerator.HTMLlicense") + "</a> " + UpdateUI.getString("MoreInfoGenerator.licenseAvailable") + "</p>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			}
			if (license.getAnnotation()!=null) {
				out.println("<p><pre>"); //$NON-NLS-1$
				out.println(license.getAnnotation().trim());
				out.println("</pre></p>"); //$NON-NLS-1$
			}
		}
		if (feature.getCopyright()!=null) {
			IURLEntry copyright = feature.getCopyright();
			out.println("<h2>" + UpdateUI.getString("MoreInfoGenerator.copyright") + "</h2>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (copyright.getURL()!=null) {
				out.println("<p><a href=\""+copyright.getURL()+"\">" + UpdateUI.getString("MoreInfoGenerator.HTMLcopyright") + "</a> " + UpdateUI.getString("MoreInfoGenerator.copyrightAvailable") + "</p>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			}
			if (copyright.getAnnotation()!=null) {
				out.println("<p>"); //$NON-NLS-1$
				out.println(copyright.getAnnotation().trim());
				out.println("</p>"); //$NON-NLS-1$
			}
		}
		out.println("</BODY>"); //$NON-NLS-1$
		out.println("</HTML>"); //$NON-NLS-1$
	}
	
	private void createProfileTable(IInstallFeatureOperation job, PrintWriter out) {
		IFeature feature =job.getFeature();
		PluginVersionIdentifier version = feature.getVersionedIdentifier().getVersion();
		IFeature oldFeature = job.getOldFeature();
		PluginVersionIdentifier oldVersion=null;
		String oldVersionLabel = UpdateUI.getString("MoreInfoGenerator.notInstalled"); //$NON-NLS-1$
		if (oldFeature!=null) {
			oldVersion = oldFeature.getVersionedIdentifier().getVersion();
			oldVersionLabel = oldVersion.toString();
		}
		out.println("<table border=\"0\" width=\"100%\">"); //$NON-NLS-1$
		out.println("<tr>"); //$NON-NLS-1$
		out.println("<td width=\"50%\" valign=\"top\">Version: <b>"+version.toString()+"</b>"); //$NON-NLS-2$ //$NON-NLS-1$
		out.println("<p>" + "Installed Version:" + " <b>"+oldVersionLabel+"</b>"); //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$ //$NON-NLS-4$
		out.println("<p>" + "Provider:" + " <b>"+feature.getProvider()+"</b>"); //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$ //$NON-NLS-4$
		out.println("<td width=\"50%\" valign=\"top\"><img border=\"0\" src=\""+getImageURL(feature)+"\"></td>"); //$NON-NLS-1$ //$NON-NLS-2$
		out.println("</tr>"); //$NON-NLS-1$
		out.println("</table>"); //$NON-NLS-1$
	}
	private void createEnvironmentTable(IInstallFeatureOperation job, PrintWriter out) {
		IFeature feature = job.getFeature();
		out.println("<table border=\"0\" width=\"100%\">"); //$NON-NLS-1$
		out.println("<tr>"); //$NON-NLS-1$
		out.println("<td width=\"50%\" valign=\"top\">"); //$NON-NLS-1$
		out.println("<h2>" + UpdateUI.getString("MoreInfoGenerator.platforms") + "</h2>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		out.println("<p>" + "Operating System:" + " <b>"+getEnv(feature.getOS())+"</b></p>"); //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$ //$NON-NLS-4$
		out.println("<p>" + "Windowing System:" + " <b>"+getEnv(feature.getWS())+"</b></p>"); //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$ //$NON-NLS-4$
		out.println("<p>" + "Architecture:" + " <b>"+getEnv(feature.getOSArch())+"</b></p>"); //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$ //$NON-NLS-4$
		out.println("<p>" + "Languages:" +" <b>"+getEnv(feature.getNL())+"</b></td>"); //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$ //$NON-NLS-4$
		out.println("<td width=\"50%\" valign=\"top\">"); //$NON-NLS-1$
		out.println("<h2>" + UpdateUI.getString("MoreInfoGenerator.footprint") + "</h2>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		out.println("<p>" + UpdateUI.getString("MoreInfoGenerator.downloadSize") + " <b>0KB</b></p>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		out.println("<p>" + UpdateUI.getString("MoreInfoGenerator.installSize") + " <b>0KB</b></p>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		out.println("<p>" + UpdateUI.getString("MoreInfoGenerator.downloadTime") + " <b>" + UpdateUI.getString("MoreInfoGenerator.lessthanone") + "</b></td>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		out.println("</tr>"); //$NON-NLS-1$
		out.println(UpdateUI.getString("</table>")); //$NON-NLS-1$
	}
	private String getEnv(String value) {
		if (value==null) return UpdateUI.getString("MoreInfoGenerator.all"); //$NON-NLS-1$
		return value;
	}
	
	private void createDescription(IFeature feature, PrintWriter out) {
		out.println("<h2>" + UpdateUI.getString("MoreInfoGenerator.desc") + "</h2>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		IURLEntry desc = feature.getDescription();
		out.println("<p>"); //$NON-NLS-1$
		if (desc.getAnnotation()!=null)
			out.println(desc.getAnnotation().trim());
		if (desc.getURL()!=null)
			out.println(" <a href=\""+desc.getURL().toString()+"\">" + UpdateUI.getString("MoreInfoGenerator.moreInfo") + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		out.println("</p>"); //$NON-NLS-1$
	}
	
	private String getImageURL(IFeature feature) {
		URL imageURL = feature.getImage();
		if (imageURL==null)
			imageURL = UpdateUIImages.getDefaultProviderImageURL();
		return imageURL.toString();
	}

	public void dispose() {
		if (tempFile != null) {
			tempFile.delete();
			tempFile = null;
		}
	}
}
