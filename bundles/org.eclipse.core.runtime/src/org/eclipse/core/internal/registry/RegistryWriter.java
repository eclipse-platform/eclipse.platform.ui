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
package org.eclipse.core.internal.registry;

import java.io.PrintWriter;
import org.eclipse.core.runtime.*;
// TODO dead code? Does not seem to be used.
public class RegistryWriter {
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
		for (int i = 0; i < IModel.INDENT; i++)
			gap2 += " "; //$NON-NLS-1$

		w.print(gap1 + "<" + element); //$NON-NLS-1$
		ConfigurationProperty[] propList = configElement.getProperties();
		int propSize = (propList == null) ? 0 : propList.length;
		for (int i = 0; i < propSize; i++)
			writeConfigurationProperty(propList[i], w, indent + IModel.INDENT);

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
			writeConfigurationElement((ConfigurationElement) subElementList[i], w, indent + IModel.INDENT);

		w.println(gap1 + "</" + element + ">"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void writeConfigurationProperty(ConfigurationProperty configProp, PrintWriter w, int indent) {
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

		w.print(gap1 + "<" + IModel.EXTENSION); //$NON-NLS-1$
		if (extension.getExtensionPointIdentifier() != null)
			w.print(" " + IModel.EXTENSION_TARGET + "=\"" + xmlSafe(extension.getExtensionPointIdentifier()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (extension.getUniqueIdentifier() != null)
			w.print(" " + IModel.EXTENSION_ID + "=\"" + xmlSafe(extension.getUniqueIdentifier()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (extension.getName() != null)
			w.print(" " + IModel.EXTENSION_NAME + "=\"" + xmlSafe(extension.getName()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		IConfigurationElement[] subElements = extension.getConfigurationElements();
		int size = (subElements == null) ? 0 : subElements.length;
		if (size == 0) {
			w.println("/>"); //$NON-NLS-1$
			return;
		}
		w.println(">"); //$NON-NLS-1$
		for (int i = 0; i < size; i++)
			writeConfigurationElement((ConfigurationElement) subElements[i], w, indent + IModel.INDENT);

		w.println(gap1 + "</" + IModel.EXTENSION + ">"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void writeExtensionPoint(ExtensionPoint extPt, PrintWriter w, int indent) {
		String gap1 = ""; //$NON-NLS-1$
		for (int i = 0; i < indent; i++)
			gap1 += " "; //$NON-NLS-1$

		w.print(gap1 + "<" + IModel.EXTENSION_POINT); //$NON-NLS-1$
		if (extPt.getUniqueIdentifier() != null)
			w.print(" " + IModel.EXTENSION_POINT_ID + "=\"" + xmlSafe(extPt.getUniqueIdentifier()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (extPt.getName() != null)
			w.print(" " + IModel.EXTENSION_POINT_NAME + "=\"" + xmlSafe(extPt.getName()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		w.println("/>"); //$NON-NLS-1$
	}
	public void writeBundleModel(BundleModel plugin, PrintWriter w, int indent) {

		String gap1 = ""; //$NON-NLS-1$
		for (int i = 0; i < indent; i++)
			gap1 += " "; //$NON-NLS-1$
		String gap2 = gap1;
		for (int i = 0; i < IModel.INDENT; i++)
			gap2 += " "; //$NON-NLS-1$

		w.println(""); //$NON-NLS-1$
		w.print(gap1 + "<" + IModel.PLUGIN); //$NON-NLS-1$
		if (plugin.getUniqueIdentifier() != null)
			w.print(" " + IModel.PLUGIN_ID + "=\"" + xmlSafe(plugin.getUniqueIdentifier()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (plugin.getName() != null)
			w.print(" " + IModel.PLUGIN_NAME + "=\"" + xmlSafe(plugin.getName()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		w.println(">"); //$NON-NLS-1$

		IExtensionPoint[] extensionPoints = plugin.getExtensionPoints();
		int extPointsSize = (extensionPoints == null) ? 0 : extensionPoints.length;
		if (extPointsSize != 0) {
			w.println(""); //$NON-NLS-1$
			for (int i = 0; i < extPointsSize; i++)
				writeExtensionPoint((ExtensionPoint) extensionPoints[i], w, indent + IModel.INDENT);
		}

		IExtension[] extensions = plugin.getExtensions();
		int extSize = (extensions == null) ? 0 : extensions.length;
		if (extSize != 0) {
			for (int i = 0; i < extSize; i++) {
				w.println(""); //$NON-NLS-1$
				writeExtension((Extension) extensions[i], w, indent + IModel.INDENT);
			}
		}

		// Don't write fragments here.  If we do, XML won't be
		// able to parse what we write out.  Fragments must be
		// entities separate from plugins.
		w.println(gap1 + "</" + IModel.PLUGIN + ">"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void writeRegistry(ExtensionRegistry registry, PrintWriter w, int indent) {
		String gap1 = ""; //$NON-NLS-1$
		for (int i = 0; i < indent; i++)
			gap1 += " "; //$NON-NLS-1$
		w.println(gap1 + "<" + IModel.REGISTRY + ">"); //$NON-NLS-1$ //$NON-NLS-2$
		String[] list = registry.getElementIdentifiers();
		for (int i = 0; i < list.length; i++)
			writeBundleModel((BundleModel) registry.getElement(list[i]), w, indent + IModel.INDENT);

		w.println(gap1 + "</" + IModel.REGISTRY + ">"); //$NON-NLS-1$ //$NON-NLS-2$
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
