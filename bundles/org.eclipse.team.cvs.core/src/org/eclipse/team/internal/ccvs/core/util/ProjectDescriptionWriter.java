package org.eclipse.team.internal.ccvs.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IProjectDescription;
import java.io.*;

// NIK: Maybe we should make the Strings constants ?

public class ProjectDescriptionWriter {
	private static void appendEscapedChar(StringBuffer buffer, char c) {
		String replacement = getReplacement(c);
		if (replacement != null) {
			buffer.append('&');
			buffer.append(replacement);
			buffer.append(';');
		} else {
			if ((c >= ' ' && c <= 0x7E) || c == '\n' || c == '\r' || c == '\t') {
				buffer.append(c);
			} else {
				buffer.append("&#");
				buffer.append(Integer.toString(c));
				buffer.append(';');
			}
		}
	}
	public static String getEscaped(String s) {
		StringBuffer result = new StringBuffer(s.length() + 10);
		for (int i = 0; i < s.length(); ++i)
			appendEscapedChar(result, s.charAt(i));
		return result.toString();
	}
	private static String getReplacement(char c) {
		// Encode special XML characters into the equivalent character references.
		// These five are defined by default for all XML documents.
		switch (c) {
			case '<' :
				return "lt";
			case '>' :
				return "gt";
			case '"' :
				return "quot";
			case '\'' :
				return "apos";
			case '&' :
				return "amp";
		}
		return null;
	}
	public static void writeProjectDescription(
		IProjectDescription desc,
		OutputStream os)
		throws IOException {
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF8"));
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.println("<project-description>");

		String comment = desc.getComment();
		if (comment != null) {
			writer.print("\t<comment>");
			writer.print(getEscaped(desc.getComment()));
			writer.println("</comment>");
		}

		String[] natures = desc.getNatureIds();
		for (int i = 0; i < natures.length; i++) {
			writer.println("\t<nature id=\"" + getEscaped(natures[i]) + "\"/>");
		}

		IProject[] references = desc.getReferencedProjects();
		for (int i = 0; i < references.length; i++) {
			writer.println(
				"\t<reference project-name=\"" + getEscaped(references[i].getName()) + "\"/>");
		}

		ICommand[] commands = desc.getBuildSpec();
		for (int i = 0; i < commands.length; i++) {
			writer.println(
				"\t<builder name=\"" + getEscaped(commands[i].getBuilderName()) + "\">");
			Map args = commands[i].getArguments();
			for (Iterator it = args.keySet().iterator(); it.hasNext();) {
				String argName = (String) it.next();
				String argValue = (String) args.get(argName);
				writer.println(
					"\t\t<arg name=\""
						+ getEscaped(argName)
						+ "\" value=\""
						+ getEscaped(argValue)
						+ "\"/>");
			}
			writer.println("\t</builder>");
		}

		writer.println("</project-description>");
		writer.flush();
	}
}