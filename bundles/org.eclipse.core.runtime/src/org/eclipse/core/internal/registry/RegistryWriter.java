/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

import java.io.PrintWriter;
import org.eclipse.core.runtime.*;

/**
 * The RegistryWriter is a helper/debugging class that dumps a loaded registry 
 * in a reasonably human readable form (i.e., XML).
 */
public class RegistryWriter {
	public static final int INDENT = 2;
	public static final String REGISTRY = "plugin-registry"; //$NON-NLS-1$

	public RegistryWriter() {
		super();
	}

	public void writeConfigurationElement(ConfigurationElement configElement, PrintWriter w, int indent) {
		String element = configElement.getName();
		if (element == null)
			return;

		String gap1 = ""; //$NON-NLS-1$
		for (int i = 0; i < indent; i++)
			gap1 += " "; //$NON-NLS-1$
		String gap2 = gap1;
		for (int i = 0; i < INDENT; i++)
			gap2 += " "; //$NON-NLS-1$

		w.print(gap1 + "<" + element); //$NON-NLS-1$
		ConfigurationProperty[] propList = configElement.getProperties();
		int propSize = (propList == null) ? 0 : propList.length;
		for (int i = 0; i < propSize; i++)
			writeConfigurationProperty(propList[i], w);

		IConfigurationElement[] subElementList = configElement.getChildren();
		int subElementSize = (subElementList == null) ? 0 : subElementList.length;
		if (configElement.getValue() == null && subElementSize == 0) {
			w.println("/>"); //$NON-NLS-1$
			return;
		}
		w.println(">"); //$NON-NLS-1$

		if (configElement.getValue() != null)
			w.println(gap2 + xmlSafe(configElement.getValue()));
		for (int i = 0; i < subElementSize; i++)
			writeConfigurationElement((ConfigurationElement) subElementList[i], w, indent + INDENT);

		w.println(gap1 + "</" + element + ">"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void writeConfigurationProperty(ConfigurationProperty configProp, PrintWriter w) {
		if (configProp.getName() == null)
			return;
		w.print(" " + xmlSafe(configProp.getName()) + "=\""); //$NON-NLS-1$ //$NON-NLS-2$
		if (configProp.getValue() != null)
			w.print(xmlSafe(configProp.getValue()));
		w.print("\""); //$NON-NLS-1$
	}

	public void writeExtension(Extension extension, PrintWriter w, int indent) {
		String gap1 = ""; //$NON-NLS-1$
		for (int i = 0; i < indent; i++)
			gap1 += " "; //$NON-NLS-1$

		w.print(gap1 + "<" + ExtensionsParser.EXTENSION); //$NON-NLS-1$
		if (extension.getExtensionPointIdentifier() != null)
			w.print(" " + ExtensionsParser.EXTENSION_TARGET + "=\"" + xmlSafe(extension.getExtensionPointIdentifier()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (extension.getUniqueIdentifier() != null)
			w.print(" " + ExtensionsParser.EXTENSION_ID + "=\"" + xmlSafe(extension.getUniqueIdentifier()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (extension.getName() != null)
			w.print(" " + ExtensionsParser.EXTENSION_NAME + "=\"" + xmlSafe(extension.getName()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		IConfigurationElement[] subElements = extension.getConfigurationElements();
		int size = (subElements == null) ? 0 : subElements.length;
		if (size == 0) {
			w.println("/>"); //$NON-NLS-1$
			return;
		}
		w.println(">"); //$NON-NLS-1$
		for (int i = 0; i < size; i++)
			writeConfigurationElement((ConfigurationElement) subElements[i], w, indent + INDENT);

		w.println(gap1 + "</" + ExtensionsParser.EXTENSION + ">"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void writeExtensionPoint(ExtensionPoint extPt, PrintWriter w, int indent) {
		String gap1 = ""; //$NON-NLS-1$
		for (int i = 0; i < indent; i++)
			gap1 += " "; //$NON-NLS-1$

		w.print(gap1 + "<" + ExtensionsParser.EXTENSION_POINT); //$NON-NLS-1$
		if (extPt.getUniqueIdentifier() != null)
			w.print(" " + ExtensionsParser.EXTENSION_POINT_ID + "=\"" + xmlSafe(extPt.getUniqueIdentifier()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (extPt.getName() != null)
			w.print(" " + ExtensionsParser.EXTENSION_POINT_NAME + "=\"" + xmlSafe(extPt.getName()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		w.println("/>"); //$NON-NLS-1$
	}

	public void writeBundleModel(Namespace plugin, PrintWriter w, int indent) {

		String gap1 = ""; //$NON-NLS-1$
		for (int i = 0; i < indent; i++)
			gap1 += " "; //$NON-NLS-1$
		String gap2 = gap1;
		for (int i = 0; i < INDENT; i++)
			gap2 += " "; //$NON-NLS-1$

		w.println(""); //$NON-NLS-1$
		w.print(gap1 + "<" + ExtensionsParser.PLUGIN); //$NON-NLS-1$
		if (plugin.getUniqueIdentifier() != null)
			w.print(" " + ExtensionsParser.PLUGIN_ID + "=\"" + xmlSafe(plugin.getUniqueIdentifier()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (plugin.getName() != null)
			w.print(" " + ExtensionsParser.PLUGIN_NAME + "=\"" + xmlSafe(plugin.getName()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		w.println(">"); //$NON-NLS-1$

		IExtensionPoint[] extensionPoints = plugin.getExtensionPoints();
		int extPointsSize = (extensionPoints == null) ? 0 : extensionPoints.length;
		if (extPointsSize != 0) {
			w.println(""); //$NON-NLS-1$
			for (int i = 0; i < extPointsSize; i++)
				writeExtensionPoint((ExtensionPoint) extensionPoints[i], w, indent + INDENT);
		}

		IExtension[] extensions = plugin.getExtensions();
		int extSize = (extensions == null) ? 0 : extensions.length;
		if (extSize != 0) {
			for (int i = 0; i < extSize; i++) {
				w.println(""); //$NON-NLS-1$
				writeExtension((Extension) extensions[i], w, indent + INDENT);
			}
		}

		// Don't write fragments here.  If we do, XML won't be
		// able to parse what we write out.  Fragments must be
		// entities separate from plugins.
		w.println(gap1 + "</" + ExtensionsParser.PLUGIN + ">"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void writeRegistry(ExtensionRegistry registry, PrintWriter w, int indent) {
		String gap1 = ""; //$NON-NLS-1$
		for (int i = 0; i < indent; i++)
			gap1 += " "; //$NON-NLS-1$
		w.println(gap1 + "<" + REGISTRY + ">"); //$NON-NLS-1$ //$NON-NLS-2$
		String[] list = registry.getNamespaces();
		for (int i = 0; i < list.length; i++)
			writeBundleModel(registry.getNamespace(list[i]), w, indent + INDENT);

		w.println(gap1 + "</" + REGISTRY + ">"); //$NON-NLS-1$ //$NON-NLS-2$
		w.flush();
	}

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
				buffer.append("&#"); //$NON-NLS-1$
				buffer.append(Integer.toString(c));
				buffer.append(';');
			}
		}
	}

	public static String xmlSafe(String s) {
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
				return "lt"; //$NON-NLS-1$
			case '>' :
				return "gt"; //$NON-NLS-1$
			case '"' :
				return "quot"; //$NON-NLS-1$
			case '\'' :
				return "apos"; //$NON-NLS-1$
			case '&' :
				return "amp"; //$NON-NLS-1$
		}
		return null;
	}
}