package org.eclipse.core.internal.plugins;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.core.runtime.model.*;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegistryCacheWriter {
	// Want this for the writer and the reader.  Where is a good place to
	// hold this?  On the PluginRegistryModel?  But it isn't the registry
	// version.
	public static final int REGISTRY_CACHE_VERSION = 1;
public RegistryCacheWriter() {
	super();
}
public void writeConfigurationElement(ConfigurationElementModel configElement, DataOutputStream out) {
	try {
		String outString;

		out.writeUTF("<configuration-element>");

		out.writeUTF("<readonly>");
		out.writeBoolean(configElement.isReadOnly());

		if ((outString = configElement.getName()) != null) {
			out.writeUTF("<name>");
			out.writeUTF(outString);
		}

		if ((outString = configElement.getValue()) != null) {
			out.writeUTF("<value>");
			out.writeUTF(outString);
		}

		ConfigurationPropertyModel[] properties = configElement.getProperties();
		if (properties != null) {
			out.writeUTF("<properties-length>");
			out.writeInt(properties.length);
			for (int i = 0; i < properties.length; i++) {
				writeConfigurationProperty(properties[i], out);
			}
		}

		ConfigurationElementModel[] subElements = configElement.getSubElements();
		if (subElements != null) {
			out.writeUTF("<subElements-length>");
			out.writeInt(subElements.length);
			for (int i = 0; i < subElements.length; i++) {
				writeConfigurationElement(subElements[i], out);
			}
		}

		out.writeUTF("<endconfiguration-element>");
	} catch (IOException ioe) {
	}
}
public void writeConfigurationProperty(ConfigurationPropertyModel configProperty, DataOutputStream out) {
	try {
		String outString;

		out.writeUTF("<configuration-element>");

		out.writeUTF("<readonly>");
		out.writeBoolean(configProperty.isReadOnly());

		if ((outString = configProperty.getName()) != null) {
			out.writeUTF("<name>");
			out.writeUTF(outString);
		}

		if ((outString = configProperty.getValue()) != null) {
			out.writeUTF("<value>");
			out.writeUTF(outString);
		}

		out.writeUTF("<endconfiguration-property>");
	} catch (IOException ioe) {
	}
}
public void writeExtension(ExtensionModel extension, DataOutputStream out) {
	try {
		String outString;

		out.writeUTF("<extension>");

		out.writeUTF("<readonly>");
		out.writeBoolean(extension.isReadOnly());

		if ((outString = extension.getName()) != null) {
			out.writeUTF("<name>");
			out.writeUTF(outString);
		}

		if ((outString = extension.getExtensionPoint()) != null) {
			out.writeUTF("<extension-extPt-name>");
			out.writeUTF(outString);
		}

		if ((outString = extension.getId()) != null) {
			out.writeUTF("<id>");
			out.writeUTF(outString);
		}

		ConfigurationElementModel[] subElements = extension.getSubElements();
		if (subElements != null) {
			out.writeUTF("<subElements-length>");
			out.writeInt(subElements.length);
			for (int i = 0; i < subElements.length; i++) {
				writeConfigurationElement(subElements[i], out);
			}
		}

		out.writeUTF("<endextension>");
	} catch (IOException ioe) {
	}
}
public void writeExtensionPoint(ExtensionPointModel extPoint, DataOutputStream out) {
	try {
		String outString;

		out.writeUTF("<extensionPoint>");

		out.writeUTF("<readonly>");
		out.writeBoolean(extPoint.isReadOnly());

		if ((outString = extPoint.getName()) != null) {
			out.writeUTF("<name>");
			out.writeUTF(outString);
		}

		if ((outString = extPoint.getId()) != null) {
			out.writeUTF("<id>");
			out.writeUTF(outString);
		}

		if ((outString = extPoint.getSchema()) != null) {
			out.writeUTF("<schema>");
			out.writeUTF(outString);
		}

		// Now do the extensions.
		ExtensionModel[] extensions = extPoint.getDeclaredExtensions();
		if (extensions != null) {
		}

		out.writeUTF("<endextensionPoint>");
	} catch (IOException ioe) {
	}
}
public void writeHeaderInformation(DataOutputStream out) {
	try {
		out.writeInt(REGISTRY_CACHE_VERSION);
		// output some stamps too
		// windows system stamp
		// OS stamp
		// install stamp
		// locale stamp
	} catch (IOException ioe) {
	}
}
public void writeLibrary(LibraryModel library, DataOutputStream out) {
	try {
		String outString;

		out.writeUTF("<library>");

		out.writeUTF("<readonly>");
		out.writeBoolean(library.isReadOnly());

		if ((outString = library.getName()) != null) {
			out.writeUTF("<name>");
			out.writeUTF(outString);
		}

		String[] exports = null;
		if ((exports = library.getExports()) != null) {
			out.writeUTF("<exports-length>");
			out.writeInt(exports.length);
			out.writeUTF("<exports>");
			for (int i = 0; i < exports.length; i++) {
				out.writeUTF(exports[i]);
			}
		}

		// Don't bother caching 'isExported' and 'isFullyExported'.  There
		// is no way of explicitly setting these fields.  They are computed
		// from the values in the 'exports' list.
		out.writeUTF("<endlibrary>");
	} catch (IOException ioe) {
	}
}
public void writePluginDescriptor(PluginDescriptorModel plugin, DataOutputStream out) {
	try {
		String outString;

		out.writeUTF("<plugin>");
		out.writeUTF("<readonly>");
		out.writeBoolean(plugin.isReadOnly());
		if ((outString = plugin.getName()) != null) {
			out.writeUTF("<name>");
			out.writeUTF(outString);
		}
		if ((outString = plugin.getId()) != null) {
			out.writeUTF("<id>");
			out.writeUTF(outString);
		}
		if ((outString = plugin.getProviderName()) != null) {
			out.writeUTF("<provider>");
			out.writeUTF(outString);
		}
		if ((outString = plugin.getVersion()) != null) {
			out.writeUTF("<version>");
			out.writeUTF(outString);
		}
		if ((outString = plugin.getPluginClass()) != null) {
			out.writeUTF("<class>");
			out.writeUTF(outString);
		}
		if ((outString = plugin.getLocation()) != null) {
			out.writeUTF("<location>");
			out.writeUTF(outString);
		}
		out.writeUTF("<enabled>");
		out.writeBoolean(plugin.getEnabled());

		// write out prerequisites
		PluginPrerequisiteModel[] requires = plugin.getRequires();
		int reqSize = (requires == null) ? 0 : requires.length;
		if (reqSize != 0) {
			for (int i = 0; i < reqSize; i++)
				writePluginPrerequisite(requires[i], out);
		}

		// write out library entries
		LibraryModel[] runtime = plugin.getRuntime();
		int runtimeSize = (runtime == null) ? 0 : runtime.length;
		if (runtimeSize != 0) {
			for (int i = 0; i < runtimeSize; i++) {
				writeLibrary(runtime[i], out);
			}
		}

		// need to worry about cross links here
		// now do extension points
		ExtensionPointModel[] extensionPoints = plugin.getDeclaredExtensionPoints();
		int extPointsSize = (extensionPoints == null) ? 0 : extensionPoints.length;
		if (extPointsSize != 0) {
			for (int i = 0; i < extPointsSize; i++)
				writeExtensionPoint(extensionPoints[i], out);
		}

		// and then extensions
		ExtensionModel[] extensions = plugin.getDeclaredExtensions();
		int extSize = (extensions == null) ? 0 : extensions.length;
		if (extSize != 0) {
			for (int i = 0; i < extSize; i++) {
				writeExtension(extensions[i], out);
			}
		}

		out.writeUTF("<endplugin>");
	} catch (IOException ioe) {
	}
}
public void writePluginPrerequisite(PluginPrerequisiteModel requires, DataOutputStream out) {
	try {
		String outString = null;

		out.writeUTF("<requires>");

		out.writeUTF("<readonly>");
		out.writeBoolean(requires.isReadOnly());

		if ((outString = requires.getName()) != null) {
			out.writeUTF("<name>");
			out.writeUTF(outString);
		}

		if ((outString = requires.getVersion()) != null) {
			out.writeUTF("<version>");
			out.writeUTF(outString);
		}

		out.writeUTF("<match>");
		out.writeBoolean(requires.getMatch());

		out.writeUTF("<export>");
		out.writeBoolean(requires.getExport());

		if ((outString = requires.getResolvedVersion()) != null) {
			out.writeUTF("<resolved_version>");
			out.writeUTF(outString);
		}

		if ((outString = requires.getPlugin()) != null) {
			out.writeUTF("<requires_plugin_name>");
			out.writeUTF(outString);
		}

		out.writeUTF("<endrequires>");
	} catch (IOException ioe) {
	}
}
public void writePluginRegistry(PluginRegistryModel registry, DataOutputStream out) {
	writeHeaderInformation(out);
	PluginDescriptorModel[] pluginList = registry.getPlugins();
	for (int i = 0; i < pluginList.length; i++)
		writePluginDescriptor(pluginList[i], out);
}
}
