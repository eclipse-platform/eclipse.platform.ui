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
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.*;

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
			tempFile = File.createTempFile("MoreInfo", ".htm");
		}
		return tempFile;
	}
	
	public String createURL(PendingOperation job) {
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
	
	private void createMoreInfoContent(PendingOperation job, PrintWriter out) {
		IFeature feature = job.getFeature();
		out.println(
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">");
		out.print("<HEAD>");
		out.println(
			"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
		out.println("<TITLE>"+feature.getLabel()+"</TITLE>");
		out.println("</HEAD>");
		out.println("<HTML>");
		out.println("<BODY>");
		out.println("<H1><CENTER>" + feature.getLabel() + "</CENTER></H1>");
		createProfileTable(job, out);
		createDescription(feature, out);
		out.println("<hr>");
		createEnvironmentTable(job, out);
		out.println("<hr>");
		if (feature.getLicense()!=null) {
			IURLEntry license = feature.getLicense();
			out.println("<h2>License</h2>");
			if (license.getURL()!=null) {
				out.println("<p><a href=\""+license.getURL()+"\">HTML version of the license agreement</a> is also available.</p>");
			}
			if (license.getAnnotation()!=null) {
				out.println("<p><pre>");
				out.println(license.getAnnotation().trim());
				out.println("</pre></p>");
			}
		}
		if (feature.getCopyright()!=null) {
			IURLEntry copyright = feature.getCopyright();
			out.println("<h2>Copyright</h2>");
			if (copyright.getURL()!=null) {
				out.println("<p><a href=\""+copyright.getURL()+"\">HTML version of the copyright</a> is also available.</p>");
			}
			if (copyright.getAnnotation()!=null) {
				out.println("<p>");
				out.println(copyright.getAnnotation().trim());
				out.println("</p>");
			}
		}
		out.println("</BODY>");
		out.println("</HTML>");
	}
	
	private void createProfileTable(PendingOperation job, PrintWriter out) {
		IFeature feature =job.getFeature();
		PluginVersionIdentifier version = feature.getVersionedIdentifier().getVersion();
		IFeature oldFeature = job.getOldFeature();
		PluginVersionIdentifier oldVersion=null;
		String oldVersionLabel = "not installed";
		if (oldFeature!=null) {
			oldVersion = oldFeature.getVersionedIdentifier().getVersion();
			oldVersionLabel = oldVersion.toString();
		}
		out.println("<table border=\"0\" width=\"100%\">");
		out.println("<tr>");
		out.println("<td width=\"50%\" valign=\"top\">Version: <b>"+version.toString()+"</b>");
		out.println("<p>Installed Version: <b>"+oldVersionLabel+"</b>");
		out.println("<p>Provider: <b>"+feature.getProvider()+"</b>");
		out.println("<td width=\"50%\" valign=\"top\"><img border=\"0\" src=\""+getImageURL(feature)+"\"></td>");
		out.println("</tr>");
		out.println("</table>");
	}
	private void createEnvironmentTable(PendingOperation job, PrintWriter out) {
		IFeature feature = job.getFeature();
		out.println("<table border=\"0\" width=\"100%\">");
		out.println("<tr>");
		out.println("<td width=\"50%\" valign=\"top\">");
		out.println("<h2>Supported Platforms</h2>");
		out.println("<p>Operating System: <b>"+getEnv(feature.getOS())+"</b></p>");
		out.println("<p>Windowing System: <b>"+getEnv(feature.getWS())+"</b></p>");
		out.println("<p>Architecture: <b>"+getEnv(feature.getOSArch())+"</b></p>");
		out.println("<p>Languages: <b>"+getEnv(feature.getNL())+"</b></td>");
		out.println("<td width=\"50%\" valign=\"top\">");
		out.println("<h2>Footprint</h2>");
		out.println("<p>Download size: <b>0KB</b></p>");
		out.println("<p>Install size: <b>0KB</b></p>");
		out.println("<p>Estimated download time: <b>less than one minute</b></td>");
		out.println("</tr>");
		out.println("</table>");
	}
	private String getEnv(String value) {
		if (value==null) return "all";
		return value;
	}
	
	private void createDescription(IFeature feature, PrintWriter out) {
		out.println("<h2>Description</h2>");
		IURLEntry desc = feature.getDescription();
		out.println("<p>");
		if (desc.getAnnotation()!=null)
			out.println(desc.getAnnotation().trim());
		if (desc.getURL()!=null)
			out.println(" <a href=\""+desc.getURL().toString()+"\">More info...</a>");
		out.println("</p>");
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
