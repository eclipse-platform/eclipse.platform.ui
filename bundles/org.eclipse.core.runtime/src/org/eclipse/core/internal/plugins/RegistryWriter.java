package org.eclipse.core.internal.plugins;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.model.*;
import java.io.PrintWriter;

public class RegistryWriter {
public RegistryWriter() {
	super();
}
public void writeConfigurationElement(ConfigurationElementModel configElement, PrintWriter w, int indent) {
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
	ConfigurationPropertyModel[] propList = configElement.getProperties();
	int propSize = (propList == null) ? 0 : propList.length;
	for (int i = 0; i < propSize; i++) 
		writeConfigurationProperty(propList[i], w, indent + IModel.INDENT);

	ConfigurationElementModel[] subElementList = configElement.getSubElements();
	int subElementSize = (subElementList == null) ? 0 : subElementList.length;
	if (configElement.getValue() == null && subElementSize == 0) {
		w.println("/>"); //$NON-NLS-1$
		return;
	}
	w.println(">"); //$NON-NLS-1$

	if (configElement.getValue() != null)
		w.println(gap2 + xmlSafe(configElement.getValue()));
	for (int i = 0; i < subElementSize; i++) 
		writeConfigurationElement(subElementList[i], w, indent + IModel.INDENT);

	w.println(gap1 + "</" + element + ">"); //$NON-NLS-1$ //$NON-NLS-2$
}
public void writeConfigurationProperty(ConfigurationPropertyModel configProp, PrintWriter w, int indent) {
	if (configProp.getName() == null)
		return;
	w.print(" " + xmlSafe(configProp.getName()) + "=\""); //$NON-NLS-1$ //$NON-NLS-2$
	if (configProp.getValue() != null)
		w.print(xmlSafe(configProp.getValue()));
	w.print("\""); //$NON-NLS-1$
}
public void writeExtension(ExtensionModel extension, PrintWriter w, int indent) {
	String gap1 = ""; //$NON-NLS-1$
	for (int i = 0; i < indent; i++)
		gap1 += " "; //$NON-NLS-1$

	w.print(gap1 + "<" + IModel.EXTENSION); //$NON-NLS-1$
	if (extension.getExtensionPoint() != null)
		w.print(" " + IModel.EXTENSION_TARGET + "=\"" + xmlSafe(extension.getExtensionPoint()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	if (extension.getId() != null)
		w.print(" " + IModel.EXTENSION_ID + "=\"" + xmlSafe(extension.getId()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	if (extension.getName() != null)
		w.print(" " + IModel.EXTENSION_NAME + "=\"" + xmlSafe(extension.getName()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	ConfigurationElementModel[] subElements = extension.getSubElements();
	int size = (subElements == null) ? 0 : subElements.length;
	if (size == 0) {
		w.println("/>"); //$NON-NLS-1$
		return;
	}
	w.println(">"); //$NON-NLS-1$

	for (int i = 0; i < size; i++) 
		writeConfigurationElement(subElements[i], w, indent + IModel.INDENT);

	w.println(gap1 + "</" + IModel.EXTENSION + ">"); //$NON-NLS-1$ //$NON-NLS-2$
}
public void writeExtensionPoint(ExtensionPointModel extPt, PrintWriter w, int indent) {
	String gap1 = ""; //$NON-NLS-1$
	for (int i = 0; i < indent; i++)
		gap1 += " "; //$NON-NLS-1$

	w.print(gap1 + "<" + IModel.EXTENSION_POINT); //$NON-NLS-1$
	if (extPt.getId() != null)
		w.print(" " + IModel.EXTENSION_POINT_ID + "=\"" + xmlSafe(extPt.getId()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	if (extPt.getName() != null)
		w.print(" " + IModel.EXTENSION_POINT_NAME + "=\"" + xmlSafe(extPt.getName()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	if (extPt.getSchema() != null)
		w.print(" " + IModel.EXTENSION_POINT_SCHEMA + "=\"" + xmlSafe(extPt.getSchema()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	w.println("/>"); //$NON-NLS-1$
}
public void writeLibrary(LibraryModel library, PrintWriter w, int indent) {
	String gap1 = ""; //$NON-NLS-1$
	for (int i = 0; i < indent; i++)
		gap1 += " "; //$NON-NLS-1$
	String gap2 = gap1;
	for (int i = 0; i < IModel.INDENT; i++)
		gap2 += " "; //$NON-NLS-1$

	w.print(gap1 + "<" + IModel.LIBRARY); //$NON-NLS-1$
	if (library.getName() != null)
		w.print(" " + IModel.LIBRARY_NAME + "=\"" + xmlSafe(library.getName()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	if (library.getType() != null)
		w.print(" " + IModel.LIBRARY_TYPE + "=\"" + xmlSafe(library.getType()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	if (!library.isExported())
		w.println("/>"); //$NON-NLS-1$
	else {
		w.println(">"); //$NON-NLS-1$
		String[] exports = library.getExports();
		int size = (exports == null) ? 0 : exports.length;
		for (int i = 0; i < size; i++)
			w.println(gap2 + "<" + IModel.LIBRARY_EXPORT + " " + IModel.LIBRARY_EXPORT_MASK + "=\"" + xmlSafe(exports[i]) + "\"/>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		w.println(gap1 + "</" + IModel.LIBRARY + ">"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
public void writePluginDescriptor(PluginDescriptorModel plugin, PrintWriter w, int indent) {

	String gap1 = ""; //$NON-NLS-1$
	for (int i = 0; i < indent; i++)
		gap1 += " "; //$NON-NLS-1$
	String gap2 = gap1;
	for (int i = 0; i < IModel.INDENT; i++)
		gap2 += " "; //$NON-NLS-1$

	w.println(""); //$NON-NLS-1$
	w.print(gap1 + "<" + IModel.PLUGIN); //$NON-NLS-1$
	if (plugin.getId() != null)
		w.print(" " + IModel.PLUGIN_ID + "=\"" + xmlSafe(plugin.getId()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	if (plugin.getName() != null)
		w.print(" " + IModel.PLUGIN_NAME + "=\"" + xmlSafe(plugin.getName()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	if (plugin.getProviderName() != null)
		w.print(" " + IModel.PLUGIN_PROVIDER + "=\"" + xmlSafe(plugin.getProviderName()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	if (plugin.getVersion() != null)
		w.print(" " + IModel.PLUGIN_VERSION + "=\"" + xmlSafe(plugin.getVersion()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	if (plugin.getPluginClass() != null)
		w.print(" " + IModel.PLUGIN_CLASS + "=\"" + xmlSafe(plugin.getPluginClass()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	w.println(">"); //$NON-NLS-1$

	PluginPrerequisiteModel[] requires = plugin.getRequires();
	int reqSize = (requires == null) ? 0 : requires.length;
	if (reqSize != 0) {
		w.print(gap2 + "<" + IModel.PLUGIN_REQUIRES); //$NON-NLS-1$
		w.println(">"); //$NON-NLS-1$
		for (int i = 0; i < reqSize; i++) 
			writePluginPrerequisite(requires[i], w, indent + 2 * IModel.INDENT);
		w.println(gap2 + "</" + IModel.PLUGIN_REQUIRES + ">"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	LibraryModel[] runtime = plugin.getRuntime();
	int runtimeSize = (runtime == null) ? 0 : runtime.length;
	if (runtimeSize != 0) {
		w.println(gap2 + "<" + IModel.RUNTIME + ">"); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < runtimeSize; i++) {
			writeLibrary(runtime[i], w, indent + 2 * IModel.INDENT);
		}
		w.println(gap2 + "</" + IModel.RUNTIME + ">"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	ExtensionPointModel[] extensionPoints = plugin.getDeclaredExtensionPoints();
	int extPointsSize = (extensionPoints == null) ? 0 : extensionPoints.length;
	if (extPointsSize != 0) {
		w.println(""); //$NON-NLS-1$
		for (int i = 0; i < extPointsSize; i++)
			writeExtensionPoint(extensionPoints[i], w, indent + IModel.INDENT);
	}

	ExtensionModel[] extensions = plugin.getDeclaredExtensions();
	int extSize = (extensions == null) ? 0 : extensions.length;
	if (extSize != 0) {
		for (int i = 0; i < extSize; i++) {
			w.println(""); //$NON-NLS-1$
			writeExtension(extensions[i], w, indent + IModel.INDENT);
		}
	}
	
	// Don't write fragments here.  If we do, XML won't be
	// able to parse what we write out.  Fragments must be
	// entities separate from plugins.
	w.println(gap1 + "</" + IModel.PLUGIN + ">"); //$NON-NLS-1$ //$NON-NLS-2$
}
public void writePluginFragment(PluginFragmentModel fragment, PrintWriter w, int indent) {

	String gap1 = ""; //$NON-NLS-1$
	for (int i = 0; i < indent; i++)
		gap1 += " "; //$NON-NLS-1$
	String gap2 = gap1;
	for (int i = 0; i < IModel.INDENT; i++)
		gap2 += " "; //$NON-NLS-1$

	w.println(""); //$NON-NLS-1$
	w.print(gap1 + "<" + IModel.FRAGMENT); //$NON-NLS-1$
	if (fragment.getId() != null)
		w.print(" " + IModel.FRAGMENT_ID + "=\"" + xmlSafe(fragment.getId()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	if (fragment.getName() != null)
		w.print(" " + IModel.FRAGMENT_NAME + "=\"" + xmlSafe(fragment.getName()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	if (fragment.getProviderName() != null)
		w.print(" " + IModel.FRAGMENT_PROVIDER + "=\"" + xmlSafe(fragment.getProviderName()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	if (fragment.getVersion() != null)
		w.print(" " + IModel.FRAGMENT_VERSION + "=\"" + xmlSafe(fragment.getVersion()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	if (fragment.getPluginId() != null)
		w.print(" " + IModel.FRAGMENT_PLUGIN_ID + "=\"" + xmlSafe(fragment.getPluginId()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	if (fragment.getPluginVersion() != null)
		w.print(" " + IModel.FRAGMENT_PLUGIN_VERSION + "=\"" + xmlSafe(fragment.getPluginVersion()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	if (fragment.getMatch() != PluginFragmentModel.FRAGMENT_MATCH_UNSPECIFIED) {
		switch (fragment.getMatch()) {
			case PluginFragmentModel.FRAGMENT_MATCH_PERFECT:
				w.print(" " + IModel.FRAGMENT_PLUGIN_MATCH + "=\"" + IModel.FRAGMENT_PLUGIN_MATCH_PERFECT + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				break;
			case PluginFragmentModel.FRAGMENT_MATCH_EQUIVALENT:
				w.print(" " + IModel.FRAGMENT_PLUGIN_MATCH + "=\"" + IModel.FRAGMENT_PLUGIN_MATCH_EQUIVALENT + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				break;
			case PluginFragmentModel.FRAGMENT_MATCH_COMPATIBLE:
				w.print(" " + IModel.FRAGMENT_PLUGIN_MATCH + "=\"" + IModel.FRAGMENT_PLUGIN_MATCH_COMPATIBLE + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				break;
			case PluginFragmentModel.FRAGMENT_MATCH_GREATER_OR_EQUAL:
				w.print(" " + IModel.FRAGMENT_PLUGIN_MATCH + "=\"" + IModel.FRAGMENT_PLUGIN_MATCH_GREATER_OR_EQUAL + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				break;
		}
	}
	
	w.println(">"); //$NON-NLS-1$

	PluginPrerequisiteModel[] requires = fragment.getRequires();
	int reqSize = (requires == null) ? 0 : requires.length;
	if (reqSize != 0) {
		w.print(gap2 + "<" + IModel.PLUGIN_REQUIRES); //$NON-NLS-1$
		w.println(">"); //$NON-NLS-1$
		for (int i = 0; i < reqSize; i++) 
			writePluginPrerequisite(requires[i], w, indent + 2 * IModel.INDENT);
		w.println(gap2 + "</" + IModel.PLUGIN_REQUIRES + ">"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	LibraryModel[] runtime = fragment.getRuntime();
	int runtimeSize = (runtime == null) ? 0 : runtime.length;
	if (runtimeSize != 0) {
		w.println(gap2 + "<" + IModel.RUNTIME + ">"); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < runtimeSize; i++) {
			writeLibrary(runtime[i], w, indent + 2 * IModel.INDENT);
		}
		w.println(gap2 + "</" + IModel.RUNTIME + ">"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	ExtensionPointModel[] extensionPoints = fragment.getDeclaredExtensionPoints();
	int extPointsSize = (extensionPoints == null) ? 0 : extensionPoints.length;
	if (extPointsSize != 0) {
		w.println(""); //$NON-NLS-1$
		for (int i = 0; i < extPointsSize; i++)
			writeExtensionPoint(extensionPoints[i], w, indent + IModel.INDENT);
	}

	ExtensionModel[] extensions = fragment.getDeclaredExtensions();
	int extSize = (extensions == null) ? 0 : extensions.length;
	if (extSize != 0) {
		for (int i = 0; i < extSize; i++) {
			w.println(""); //$NON-NLS-1$
			writeExtension(extensions[i], w, indent + IModel.INDENT);
		}
	}

	w.println(gap1 + "</" + IModel.FRAGMENT + ">"); //$NON-NLS-1$ //$NON-NLS-2$
}
public void writePluginPrerequisite(PluginPrerequisiteModel req, PrintWriter w, int indent) {
	String gap1 = ""; //$NON-NLS-1$
	for (int i = 0; i < indent; i++)
		gap1 += " "; //$NON-NLS-1$

	w.print(gap1 + "<" + IModel.PLUGIN_REQUIRES_IMPORT); //$NON-NLS-1$
	if (req.getPlugin() != null)
		w.print(" " + IModel.PLUGIN_REQUIRES_PLUGIN + "=\"" + xmlSafe(req.getPlugin()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	if (req.getVersion() != null)
		w.print(" " + IModel.PLUGIN_REQUIRES_PLUGIN_VERSION + "=\"" + xmlSafe(req.getVersion()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	if (req.getExport())
		w.print(" " + IModel.PLUGIN_REQUIRES_EXPORT + "=\"" + IModel.TRUE + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	if (req.getOptional())
		w.print(" " + IModel.PLUGIN_REQUIRES_OPTIONAL + "=\"" + IModel.TRUE + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	switch (req.getMatchByte()) {
		case PluginPrerequisiteModel.PREREQ_MATCH_PERFECT:
			w.print(" " + IModel.PLUGIN_REQUIRES_MATCH + "=\"" + IModel.PLUGIN_REQUIRES_MATCH_PERFECT + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			break;
		case PluginPrerequisiteModel.PREREQ_MATCH_EQUIVALENT:
			w.print(" " + IModel.PLUGIN_REQUIRES_MATCH + "=\"" + IModel.PLUGIN_REQUIRES_MATCH_EQUIVALENT + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			break;
		case PluginPrerequisiteModel.PREREQ_MATCH_COMPATIBLE:
			w.print(" " + IModel.PLUGIN_REQUIRES_MATCH + "=\"" + IModel.PLUGIN_REQUIRES_MATCH_COMPATIBLE + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			break;
		case PluginPrerequisiteModel.PREREQ_MATCH_GREATER_OR_EQUAL:
			w.print(" " + IModel.PLUGIN_REQUIRES_MATCH + "=\"" + IModel.PLUGIN_REQUIRES_MATCH_GREATER_OR_EQUAL + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			break;
	}
	w.println("/>"); //$NON-NLS-1$
}
public void writePluginRegistry(PluginRegistryModel registry, PrintWriter w, int indent) {
	String gap1 = ""; //$NON-NLS-1$
	for (int i = 0; i < indent; i++)
		gap1 += " "; //$NON-NLS-1$
	w.println(gap1 + "<" + IModel.REGISTRY + ">"); //$NON-NLS-1$ //$NON-NLS-2$
	PluginDescriptorModel[] pluginList = registry.getPlugins();
	for (int i = 0; i < pluginList.length; i++)
		writePluginDescriptor(pluginList[i], w, indent + IModel.INDENT);
	
	PluginFragmentModel[] fragmentList = registry.getFragments();
	for (int i = 0; i < fragmentList.length; i++)
		writePluginFragment(fragmentList[i], w, indent + IModel.INDENT);

	w.println(gap1 + "</" + IModel.REGISTRY + ">"); //$NON-NLS-1$ //$NON-NLS-2$

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
