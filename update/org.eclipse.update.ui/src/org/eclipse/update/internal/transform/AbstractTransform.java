package org.eclipse.update.internal.transform;
import org.eclipse.update.internal.ui.*;
import java.net.URL;
import org.eclipse.core.runtime.Platform;
import java.io.*;

public abstract class AbstractTransform implements ITransform {
	public static final String HTML_ROOT = "html";
	public static final char VAR_BEGIN = '$';
	public static final char VAR_END = '$';
	public static final String KEY_JAVA_SCRIPT = "JavaScript";
	public static final String KEY_HTML_BASE = "HTMLBase";

	public String getTemplateFileName(Object input) {
		String base = getHTMLBase();
		return base + "/" + getObjectTemplate(input);
	}
	
	public static String getHTMLBase() {
		URL installURL = UpdateUIPlugin.getDefault().getDescriptor().getInstallURL();
		try {
		   installURL = Platform.resolve(installURL);
		   String urlName = installURL.toString();
		   if (urlName.endsWith("/"))
		      urlName = urlName.substring(0, urlName.length()-1);
		   return urlName + "/"+HTML_ROOT;
		}
		catch (IOException e) {
			return HTML_ROOT;
		}
	}
	
	protected abstract String getObjectTemplate(Object input);
	
	protected abstract void writeJavaScriptSection(Object input, PrintWriter writer);
	
	protected String getValue(Object input, String key) {
		if (key.equals(KEY_JAVA_SCRIPT)) {
			StringWriter swriter = new StringWriter();
			PrintWriter writer = new PrintWriter(swriter);
			writer.println("<script language=\"JavaScript1.1\">");
			writeJavaScriptSection(input, writer);
			writer.println("</script>");
			return swriter.toString();
		}
		if (key.equals(KEY_HTML_BASE)) {
			return getHTMLBase();
		}
		return key;
	}
	
	public String transform(Object input, String template) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<template.length(); i++) {
			char c = template.charAt(i);
			if (c==VAR_BEGIN) {
				int j;
				for (j=i+1; j<template.length(); j++) {
					c = template.charAt(j);
					if (c==VAR_END) {
						String var = template.substring(i+1, j);
						String value = getValue(input, var);
						buf.append(value);
						break;
					}
				}
				i=j;
				continue;
			}
			else buf.append(c);
		}
		return buf.toString();
	}
}