package org.eclipse.core.internal.plugins;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.core.runtime.model.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class RegistryCacheWriter {
	// See RegistryCacheReader for constants commonly used here too.

	// objectTable will be an array list of objects.  The objects will be things 
	// like a plugin descriptor, extension, extension point, etc.  The integer 
	// index value will be used in the cache to allow cross-references in the 
	// cached registry.
	ArrayList objectTable = null;
public RegistryCacheWriter() {
	super();
}
public int addToObjectTable(Object object) {
	if (objectTable == null) {
		objectTable = new ArrayList();
	}
	objectTable.add(object);
	// return the index of the object just added (i.e. size - 1)
	return (objectTable.size() - 1);

}
public void writeConfigurationElement(ConfigurationElementModel configElement, DataOutputStream out) {
	try {
		// Check to see if this configuration element already exists in the
		// objectTable.  If it is there, it has already been written to the 
		// cache so just write out the index.
		int configElementIndex = objectTable.indexOf(configElement);
		if (configElementIndex != -1) {
			// this extension is already there
			out.writeByte(RegistryCacheReader.CONFIGURATION_ELEMENT_INDEX_LABEL);
			out.writeInt(configElementIndex);
			return;
		}

		String outString;
		// add this object to the object table first
		addToObjectTable(configElement);

		out.writeByte(RegistryCacheReader.CONFIGURATION_ELEMENT_LABEL);

		out.writeByte(RegistryCacheReader.READONLY_LABEL);
		out.writeBoolean(configElement.isReadOnly());

		if ((outString = configElement.getName()) != null) {
			out.writeByte(RegistryCacheReader.NAME_LABEL);
			out.writeUTF(outString);
		}

		if ((outString = configElement.getValue()) != null) {
			out.writeByte(RegistryCacheReader.VALUE_LABEL);
			out.writeUTF(outString);
		}

		ConfigurationPropertyModel[] properties = configElement.getProperties();
		if (properties != null) {
			out.writeByte(RegistryCacheReader.PROPERTIES_LENGTH_LABEL);
			out.writeInt(properties.length);
			for (int i = 0; i < properties.length; i++) {
				writeConfigurationProperty(properties[i], out);
			}
		}

		ConfigurationElementModel[] subElements = configElement.getSubElements();
		if (subElements != null) {
			out.writeByte(RegistryCacheReader.SUBELEMENTS_LENGTH_LABEL);
			out.writeInt(subElements.length);
			for (int i = 0; i < subElements.length; i++) {
				writeConfigurationElement(subElements[i], out);
			}
		}

		// Write out the parent information.  We can assume that the parent has
		// already been written out.
		// Add the index to the registry object for this plugin
		Object parent = configElement.getParent();
		out.writeByte(RegistryCacheReader.CONFIGURATION_ELEMENT_PARENT_LABEL);
		out.writeInt(objectTable.indexOf(parent));

		out.writeByte(RegistryCacheReader.CONFIGURATION_ELEMENT_END_LABEL);
	} catch (IOException ioe) {
	}
}
public void writeConfigurationProperty(ConfigurationPropertyModel configProperty, DataOutputStream out) {
	try {
		String outString;

		out.writeByte(RegistryCacheReader.CONFIGURATION_PROPERTY_LABEL);

		out.writeByte(RegistryCacheReader.READONLY_LABEL);
		out.writeBoolean(configProperty.isReadOnly());

		if ((outString = configProperty.getName()) != null) {
			out.writeByte(RegistryCacheReader.NAME_LABEL);
			out.writeUTF(outString);
		}

		if ((outString = configProperty.getValue()) != null) {
			out.writeByte(RegistryCacheReader.VALUE_LABEL);
			out.writeUTF(outString);
		}

		out.writeByte(RegistryCacheReader.CONFIGURATION_PROPERTY_END_LABEL);
	} catch (IOException ioe) {
	}
}
public void writeExtension(ExtensionModel extension, DataOutputStream out) {
	try {
		// Check to see if this extension already exists in the objectTable.  If it
		// is there, it has already been written to the cache so just write out
		// the index.
		int extensionIndex = objectTable.indexOf(extension);
		if (extensionIndex != -1) {
			// this extension is already there
			out.writeByte(RegistryCacheReader.EXTENSION_INDEX_LABEL);
			out.writeInt(extensionIndex);
			return;
		}
		// add this object to the object table first
		addToObjectTable(extension);

		String outString;

		out.writeByte(RegistryCacheReader.PLUGIN_EXTENSION_LABEL);

		out.writeByte(RegistryCacheReader.READONLY_LABEL);
		out.writeBoolean(extension.isReadOnly());

		if ((outString = extension.getName()) != null) {
			out.writeByte(RegistryCacheReader.NAME_LABEL);
			out.writeUTF(outString);
		}

		if ((outString = extension.getExtensionPoint()) != null) {
			out.writeByte(RegistryCacheReader.EXTENSION_EXT_POINT_NAME_LABEL);
			out.writeUTF(outString);
		}

		if ((outString = extension.getId()) != null) {
			out.writeByte(RegistryCacheReader.ID_LABEL);
			out.writeUTF(outString);
		}

		ConfigurationElementModel[] subElements = extension.getSubElements();
		if (subElements != null) {
			out.writeByte(RegistryCacheReader.SUBELEMENTS_LENGTH_LABEL);
			out.writeInt(subElements.length);
			for (int i = 0; i < subElements.length; i++) {
				writeConfigurationElement(subElements[i], out);
			}
		}

		// Now worry about the parent plugin descriptor or plugin fragment
		PluginModel parent = extension.getParent();
		int parentIndex = objectTable.indexOf(parent);
		out.writeByte(RegistryCacheReader.EXTENSION_PARENT_LABEL);
		if (parentIndex != -1) {
			// We have already written this plugin or fragment.  Just use the index.
			if (parent instanceof PluginDescriptorModel) {
				out.writeByte(RegistryCacheReader.PLUGIN_INDEX_LABEL);
			} else /* must be a fragment */ {
				out.writeByte(RegistryCacheReader.FRAGMENT_INDEX_LABEL);
			}
			out.writeInt(parentIndex);
		} else {
			// We haven't visited this plugin or fragment yet, so write it explicitly
			if (parent instanceof PluginDescriptorModel) {
				writePluginDescriptor((PluginDescriptorModel)parent, out);
			} else /* must be a fragment */ {
				writePluginFragment((PluginFragmentModel)parent, out);
			}
		}

		out.writeByte(RegistryCacheReader.EXTENSION_END_LABEL);
	} catch (IOException ioe) {
	}
}
public void writeExtensionPoint(ExtensionPointModel extPoint, DataOutputStream out) {
	// add this object to the object table first
	addToObjectTable(extPoint);
	try {
		String outString;

		out.writeByte(RegistryCacheReader.PLUGIN_EXTENSION_POINT_LABEL);

		out.writeByte(RegistryCacheReader.READONLY_LABEL);
		out.writeBoolean(extPoint.isReadOnly());

		if ((outString = extPoint.getName()) != null) {
			out.writeByte(RegistryCacheReader.NAME_LABEL);
			out.writeUTF(outString);
		}

		if ((outString = extPoint.getId()) != null) {
			out.writeByte(RegistryCacheReader.ID_LABEL);
			out.writeUTF(outString);
		}

		if ((outString = extPoint.getSchema()) != null) {
			out.writeByte(RegistryCacheReader.EXTENSION_POINT_SCHEMA_LABEL);
			out.writeUTF(outString);
		}

		// Write out the parent's index.  We know we have
		// already written this plugin or fragment to the cache
		PluginModel parent = extPoint.getParent();
		if (parent != null) {
			int parentIndex = objectTable.indexOf(parent);
			if (parentIndex != -1) {
				out.writeByte(RegistryCacheReader.EXTENSION_POINT_PARENT_LABEL);
				out.writeInt(parentIndex);
			}
		}

		// Now do the extensions.
		ExtensionModel[] extensions = extPoint.getDeclaredExtensions();
		int extLength = extensions == null ? 0 : extensions.length;
		if (extLength != 0) {
			out.writeByte(RegistryCacheReader.EXTENSION_POINT_EXTENSIONS_LENGTH_LABEL);
			out.writeInt(extLength);
			out.writeByte(RegistryCacheReader.EXTENSION_POINT_EXTENSIONS_LABEL);
			for (int i = 0; i < extLength; i++) {
				// Check to see if the extension exists in the objectTable first
				int extensionIndex = objectTable.indexOf(extensions[i]);
				if (extensionIndex != -1) {
					// Already in the objectTable and written to the cache
					out.writeByte(RegistryCacheReader.EXTENSION_INDEX_LABEL);
					out.writeInt(extensionIndex);
				} else {
					writeExtension(extensions[i], out);
				}
			}
		}

		out.writeByte(RegistryCacheReader.EXTENSION_POINT_END_LABEL);
	} catch (IOException ioe) {
	}
}
public void writeHeaderInformation(DataOutputStream out) {
	try {
		out.writeInt(RegistryCacheReader.REGISTRY_CACHE_VERSION);
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

		out.writeByte(RegistryCacheReader.PLUGIN_LIBRARY_LABEL);

		out.writeByte(RegistryCacheReader.READONLY_LABEL);
		out.writeBoolean(library.isReadOnly());

		if ((outString = library.getName()) != null) {
			out.writeByte(RegistryCacheReader.NAME_LABEL);
			out.writeUTF(outString);
		}
		if ((outString = library.getType()) != null) {
			out.writeByte(RegistryCacheReader.TYPE_LABEL);
			out.writeUTF(outString);
		}
		if ((outString = library.getSource()) != null) {
			out.writeByte(RegistryCacheReader.SOURCE_LABEL);
			out.writeUTF(outString);
		}

		String[] exports = null;
		if ((exports = library.getExports()) != null) {
			out.writeByte(RegistryCacheReader.LIBRARY_EXPORTS_LENGTH_LABEL);
			out.writeInt(exports.length);
			out.writeByte(RegistryCacheReader.LIBRARY_EXPORTS_LABEL);
			for (int i = 0; i < exports.length; i++) {
				out.writeUTF(exports[i]);
			}
		}

		// Don't bother caching 'isExported' and 'isFullyExported'.  There
		// is no way of explicitly setting these fields.  They are computed
		// from the values in the 'exports' list.
		out.writeByte(RegistryCacheReader.LIBRARY_END_LABEL);
	} catch (IOException ioe) {
	}
}
public void writePluginDescriptor(PluginDescriptorModel plugin, DataOutputStream out) {

	try {
		// Check to see if this plugin already exists in the objectTable.  If it is there,
		// it has already been written to the cache so just write out the index.
		int pluginIndex = objectTable.indexOf(plugin);
		if (pluginIndex != -1) {
			// this plugin is already there
			out.writeByte(RegistryCacheReader.PLUGIN_INDEX_LABEL);
			out.writeInt(pluginIndex);
			return;
		}

		// add this object to the object table first
		addToObjectTable(plugin);
		String outString;

		out.writeByte(RegistryCacheReader.PLUGIN_LABEL);
		out.writeByte(RegistryCacheReader.READONLY_LABEL);
		out.writeBoolean(plugin.isReadOnly());
		if ((outString = plugin.getName()) != null) {
			out.writeByte(RegistryCacheReader.NAME_LABEL);
			out.writeUTF(outString);
		}
		if ((outString = plugin.getId()) != null) {
			out.writeByte(RegistryCacheReader.ID_LABEL);
			out.writeUTF(outString);
		}
		if ((outString = plugin.getProviderName()) != null) {
			out.writeByte(RegistryCacheReader.PLUGIN_PROVIDER_NAME_LABEL);
			out.writeUTF(outString);
		}
		if ((outString = plugin.getVersion()) != null) {
			out.writeByte(RegistryCacheReader.VERSION_LABEL);
			out.writeUTF(outString);
		}
		if ((outString = plugin.getPluginClass()) != null) {
			out.writeByte(RegistryCacheReader.PLUGIN_CLASS_LABEL);
			out.writeUTF(outString);
		}
		if ((outString = plugin.getLocation()) != null) {
			out.writeByte(RegistryCacheReader.PLUGIN_LOCATION_LABEL);
			out.writeUTF(outString);
		}
		out.writeByte(RegistryCacheReader.PLUGIN_ENABLED_LABEL);
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

		// and then fragments
		PluginFragmentModel[] fragments = plugin.getFragments();
		int fragmentSize = (fragments == null) ? 0 : fragments.length;
		if (fragmentSize != 0) {
			for (int i = 0; i < fragmentSize; i++) {
				writePluginFragment(fragments[i], out);
			}
		}

		// Add the index to the registry object for this plugin
		PluginRegistryModel parentRegistry = plugin.getRegistry();
		out.writeByte(RegistryCacheReader.PLUGIN_PARENT_LABEL);
		// We can assume that the parent registry is already written out.
		out.writeInt(objectTable.indexOf(parentRegistry));

		out.writeByte(RegistryCacheReader.PLUGIN_END_LABEL);
	} catch (IOException ioe) {
	}
}
public void writePluginFragment(PluginFragmentModel fragment, DataOutputStream out) {

	try {
		// Check to see if this fragment already exists in the objectTable.  If it is there,
		// it has already been written to the cache so just write out the index.
		int fragmentIndex = objectTable.indexOf(fragment);
		if (fragmentIndex != -1) {
			// this fragment is already there
			out.writeByte(RegistryCacheReader.FRAGMENT_INDEX_LABEL);
			out.writeInt(fragmentIndex);
			return;
		}

		// add this object to the object table first
		addToObjectTable(fragment);
		String outString;

		out.writeByte(RegistryCacheReader.FRAGMENT_LABEL);
		out.writeByte(RegistryCacheReader.READONLY_LABEL);
		out.writeBoolean(fragment.isReadOnly());
		if ((outString = fragment.getName()) != null) {
			out.writeByte(RegistryCacheReader.NAME_LABEL);
			out.writeUTF(outString);
		}
		if ((outString = fragment.getId()) != null) {
			out.writeByte(RegistryCacheReader.ID_LABEL);
			out.writeUTF(outString);
		}
		if ((outString = fragment.getProviderName()) != null) {
			out.writeByte(RegistryCacheReader.PLUGIN_PROVIDER_NAME_LABEL);
			out.writeUTF(outString);
		}
		if ((outString = fragment.getVersion()) != null) {
			out.writeByte(RegistryCacheReader.VERSION_LABEL);
			out.writeUTF(outString);
		}
		if ((outString = fragment.getLocation()) != null) {
			out.writeByte(RegistryCacheReader.PLUGIN_LOCATION_LABEL);
			out.writeUTF(outString);
		}
		if ((outString = fragment.getPlugin()) != null) {
			out.writeByte(RegistryCacheReader.FRAGMENT_PLUGIN_LABEL);
			out.writeUTF(outString);
		}
		if ((outString = fragment.getPluginVersion()) != null) {
			out.writeByte(RegistryCacheReader.FRAGMENT_PLUGIN_VERSION_LABEL);
			out.writeUTF(outString);
		}

		// write out prerequisites
		PluginPrerequisiteModel[] requires = fragment.getRequires();
		int reqSize = (requires == null) ? 0 : requires.length;
		if (reqSize != 0) {
			for (int i = 0; i < reqSize; i++)
				writePluginPrerequisite(requires[i], out);
		}

		// write out library entries
		LibraryModel[] runtime = fragment.getRuntime();
		int runtimeSize = (runtime == null) ? 0 : runtime.length;
		if (runtimeSize != 0) {
			for (int i = 0; i < runtimeSize; i++) {
				writeLibrary(runtime[i], out);
			}
		}

		// need to worry about cross links here
		// now do extension points
		ExtensionPointModel[] extensionPoints = fragment.getDeclaredExtensionPoints();
		int extPointsSize = (extensionPoints == null) ? 0 : extensionPoints.length;
		if (extPointsSize != 0) {
			for (int i = 0; i < extPointsSize; i++)
				writeExtensionPoint(extensionPoints[i], out);
		}

		// and then extensions
		ExtensionModel[] extensions = fragment.getDeclaredExtensions();
		int extSize = (extensions == null) ? 0 : extensions.length;
		if (extSize != 0) {
			for (int i = 0; i < extSize; i++) {
				writeExtension(extensions[i], out);
			}
		}

		// Add the index to the registry object for this plugin
		PluginRegistryModel parentRegistry = fragment.getRegistry();
		out.writeByte(RegistryCacheReader.PLUGIN_PARENT_LABEL);
		// We can assume that the parent registry is already written out.
		out.writeInt(objectTable.indexOf(parentRegistry));

		out.writeByte(RegistryCacheReader.FRAGMENT_END_LABEL);
	} catch (IOException ioe) {
	}
}
public void writePluginPrerequisite(PluginPrerequisiteModel requires, DataOutputStream out) {
	try {
		String outString = null;

		out.writeByte(RegistryCacheReader.PLUGIN_REQUIRES_LABEL);

		out.writeByte(RegistryCacheReader.READONLY_LABEL);
		out.writeBoolean(requires.isReadOnly());

		if ((outString = requires.getName()) != null) {
			out.writeByte(RegistryCacheReader.NAME_LABEL);
			out.writeUTF(outString);
		}

		if ((outString = requires.getVersion()) != null) {
			out.writeByte(RegistryCacheReader.VERSION_LABEL);
			out.writeUTF(outString);
		}

		out.writeByte(RegistryCacheReader.REQUIRES_MATCH_LABEL);
		out.writeBoolean(requires.getMatch());

		out.writeByte(RegistryCacheReader.REQUIRES_EXPORT_LABEL);
		out.writeBoolean(requires.getExport());

		out.writeByte(RegistryCacheReader.REQUIRES_OPTIONAL_LABEL);
		out.writeBoolean(requires.getOptional());

		if ((outString = requires.getResolvedVersion()) != null) {
			out.writeByte(RegistryCacheReader.REQUIRES_RESOLVED_VERSION_LABEL);
			out.writeUTF(outString);
		}

		if ((outString = requires.getPlugin()) != null) {
			out.writeByte(RegistryCacheReader.REQUIRES_PLUGIN_NAME_LABEL);
			out.writeUTF(outString);
		}

		out.writeByte(RegistryCacheReader.REQUIRES_END_LABEL);
	} catch (IOException ioe) {
	}
}
public void writePluginRegistry(PluginRegistryModel registry, DataOutputStream out) {
	try {
		// Check to see if this registry already exists in the objectTable.  If it is there,
		// it has already been written to the cache so just write out the index.
		if (objectTable != null) {
			int registryIndex = objectTable.indexOf(registry);
			if (registryIndex != -1) {
				// this plugin is already there
				out.writeByte(RegistryCacheReader.REGISTRY_INDEX_LABEL);
				out.writeInt(registryIndex);
				return;
			}
		}

		// add this object to the object table first
		addToObjectTable(registry);
		writeHeaderInformation(out);
		String outString = null;

		out.writeByte(RegistryCacheReader.REGISTRY_LABEL);

		out.writeByte(RegistryCacheReader.READONLY_LABEL);
		out.writeBoolean(registry.isReadOnly());

		out.writeByte(RegistryCacheReader.REGISTRY_RESOLVED_LABEL);
		out.writeBoolean(registry.isResolved());
		PluginDescriptorModel[] pluginList = registry.getPlugins();
		for (int i = 0; i < pluginList.length; i++)
			writePluginDescriptor(pluginList[i], out);
		PluginFragmentModel[] fragmentList = registry.getFragments();
		int fragmentLength = (fragmentList == null) ? 0 : fragmentList.length;
		for (int i = 0; i < fragmentLength; i++)
			writePluginFragment(fragmentList[i], out);
		out.writeByte(RegistryCacheReader.REGISTRY_END_LABEL);
	} catch (IOException ioe) {
	}
}
}
