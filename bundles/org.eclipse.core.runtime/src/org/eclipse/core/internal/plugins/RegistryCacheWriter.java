package org.eclipse.core.internal.plugins;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.model.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class RegistryCacheWriter {
	// See RegistryCacheReader for constants commonly used here too.

	// objectTable will be an array list of objects.  The objects will be things 
	// like a plugin descriptor, extension, extension point, etc.  The integer 
	// index value will be used in the cache to allow cross-references in the 
	// cached registry.
	ArrayList objectTable = null;
	
	public MultiStatus cacheWriteProblems = null;
	
	public static final boolean DEBUG_REGISTRY_CACHE = false;
	
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
public void writeLabel(byte labelValue, DataOutputStream out) {
	try {
		if (DEBUG_REGISTRY_CACHE) {
			out.writeUTF (RegistryCacheReader.decipherLabel(labelValue));
		} else {
			out.writeByte(labelValue);
		}
	} catch (IOException ioe) {
		cacheWriteProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", RegistryCacheReader.decipherLabel(labelValue)), ioe));
	}
}
public void writeConfigurationElement(ConfigurationElementModel configElement, DataOutputStream out) {
	try {
		// Check to see if this configuration element already exists in the
		// objectTable.  If it is there, it has already been written to the 
		// cache so just write out the index.
		int configElementIndex = objectTable.indexOf(configElement);
		if (configElementIndex != -1) {
			// this extension is already there
			writeLabel(RegistryCacheReader.CONFIGURATION_ELEMENT_INDEX_LABEL, out);
			out.writeInt(configElementIndex);
			return;
		}

		String outString;
		// add this object to the object table first
		addToObjectTable(configElement);

		writeLabel(RegistryCacheReader.CONFIGURATION_ELEMENT_LABEL, out);

		writeLabel(RegistryCacheReader.READONLY_LABEL, out);
		out.writeBoolean(configElement.isReadOnly());

		if ((outString = configElement.getName()) != null) {
			writeLabel(RegistryCacheReader.NAME_LABEL, out);
			out.writeUTF(outString);
		}

		if ((outString = configElement.getValue()) != null) {
			writeLabel(RegistryCacheReader.VALUE_LABEL, out);
			out.writeUTF(outString);
		}

		ConfigurationPropertyModel[] properties = configElement.getProperties();
		if (properties != null) {
			writeLabel(RegistryCacheReader.PROPERTIES_LENGTH_LABEL, out);
			out.writeInt(properties.length);
			for (int i = 0; i < properties.length; i++) {
				writeConfigurationProperty(properties[i], out);
			}
		}

		ConfigurationElementModel[] subElements = configElement.getSubElements();
		if (subElements != null) {
			writeLabel(RegistryCacheReader.SUBELEMENTS_LENGTH_LABEL, out);
			out.writeInt(subElements.length);
			for (int i = 0; i < subElements.length; i++) {
				writeConfigurationElement(subElements[i], out);
			}
		}

		// Write out the parent information.  We can assume that the parent has
		// already been written out.
		// Add the index to the registry object for this plugin
		Object parent = configElement.getParent();
		writeLabel(RegistryCacheReader.CONFIGURATION_ELEMENT_PARENT_LABEL, out);
		out.writeInt(objectTable.indexOf(parent));

		writeLabel(RegistryCacheReader.CONFIGURATION_ELEMENT_END_LABEL, out);
	} catch (IOException ioe) {
		cacheWriteProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", RegistryCacheReader.decipherLabel(RegistryCacheReader.CONFIGURATION_ELEMENT_LABEL)), ioe));
	}
}
public void writeConfigurationProperty(ConfigurationPropertyModel configProperty, DataOutputStream out) {
	try {
		String outString;

		writeLabel(RegistryCacheReader.CONFIGURATION_PROPERTY_LABEL, out);

		writeLabel(RegistryCacheReader.READONLY_LABEL, out);
		out.writeBoolean(configProperty.isReadOnly());

		if ((outString = configProperty.getName()) != null) {
			writeLabel(RegistryCacheReader.NAME_LABEL, out);
			out.writeUTF(outString);
		}

		if ((outString = configProperty.getValue()) != null) {
			writeLabel(RegistryCacheReader.VALUE_LABEL, out);
			out.writeUTF(outString);
		}

		writeLabel(RegistryCacheReader.CONFIGURATION_PROPERTY_END_LABEL, out);
	} catch (IOException ioe) {
		cacheWriteProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", RegistryCacheReader.decipherLabel(RegistryCacheReader.CONFIGURATION_PROPERTY_LABEL)), ioe));
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
			writeLabel(RegistryCacheReader.EXTENSION_INDEX_LABEL, out);
			out.writeInt(extensionIndex);
			return;
		}
		// add this object to the object table first
		addToObjectTable(extension);

		String outString;

		writeLabel(RegistryCacheReader.PLUGIN_EXTENSION_LABEL, out);

		writeLabel(RegistryCacheReader.READONLY_LABEL, out);
		out.writeBoolean(extension.isReadOnly());

		if ((outString = extension.getName()) != null) {
			writeLabel(RegistryCacheReader.NAME_LABEL, out);
			out.writeUTF(outString);
		}

		if ((outString = extension.getExtensionPoint()) != null) {
			writeLabel(RegistryCacheReader.EXTENSION_EXT_POINT_NAME_LABEL, out);
			out.writeUTF(outString);
		}

		if ((outString = extension.getId()) != null) {
			writeLabel(RegistryCacheReader.ID_LABEL, out);
			out.writeUTF(outString);
		}

		ConfigurationElementModel[] subElements = extension.getSubElements();
		if (subElements != null) {
			writeLabel(RegistryCacheReader.SUBELEMENTS_LENGTH_LABEL, out);
			out.writeInt(subElements.length);
			for (int i = 0; i < subElements.length; i++) {
				writeConfigurationElement(subElements[i], out);
			}
		}

		// Now worry about the parent plugin descriptor or plugin fragment
		PluginModel parent = extension.getParent();
		int parentIndex = objectTable.indexOf(parent);
		writeLabel(RegistryCacheReader.EXTENSION_PARENT_LABEL, out);
		if (parentIndex != -1) {
			// We have already written this plugin or fragment.  Just use the index.
			if (parent instanceof PluginDescriptorModel) {
				writeLabel(RegistryCacheReader.PLUGIN_INDEX_LABEL, out);
			} else /* must be a fragment */ {
				writeLabel(RegistryCacheReader.FRAGMENT_INDEX_LABEL, out);
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

		writeLabel(RegistryCacheReader.EXTENSION_END_LABEL, out);
	} catch (IOException ioe) {
		cacheWriteProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", RegistryCacheReader.decipherLabel(RegistryCacheReader.PLUGIN_EXTENSION_LABEL)), ioe));
	}
}
public void writeExtensionPoint(ExtensionPointModel extPoint, DataOutputStream out) {
	// add this object to the object table first
	addToObjectTable(extPoint);
	try {
		String outString;

		writeLabel(RegistryCacheReader.PLUGIN_EXTENSION_POINT_LABEL, out);

		writeLabel(RegistryCacheReader.READONLY_LABEL, out);
		out.writeBoolean(extPoint.isReadOnly());

		if ((outString = extPoint.getName()) != null) {
			writeLabel(RegistryCacheReader.NAME_LABEL, out);
			out.writeUTF(outString);
		}

		if ((outString = extPoint.getId()) != null) {
			writeLabel(RegistryCacheReader.ID_LABEL, out);
			out.writeUTF(outString);
		}

		if ((outString = extPoint.getSchema()) != null) {
			writeLabel(RegistryCacheReader.EXTENSION_POINT_SCHEMA_LABEL, out);
			out.writeUTF(outString);
		}

		// Write out the parent's index.  We know we have
		// already written this plugin or fragment to the cache
		PluginModel parent = extPoint.getParent();
		if (parent != null) {
			int parentIndex = objectTable.indexOf(parent);
			if (parentIndex != -1) {
				writeLabel(RegistryCacheReader.EXTENSION_POINT_PARENT_LABEL, out);
				out.writeInt(parentIndex);
			}
		}

		// Now do the extensions.
		ExtensionModel[] extensions = extPoint.getDeclaredExtensions();
		int extLength = extensions == null ? 0 : extensions.length;
		if (extLength != 0) {
			writeLabel(RegistryCacheReader.EXTENSION_POINT_EXTENSIONS_LENGTH_LABEL, out);
			out.writeInt(extLength);
			writeLabel(RegistryCacheReader.EXTENSION_POINT_EXTENSIONS_LABEL, out);
			for (int i = 0; i < extLength; i++) {
				// Check to see if the extension exists in the objectTable first
				int extensionIndex = objectTable.indexOf(extensions[i]);
				if (extensionIndex != -1) {
					// Already in the objectTable and written to the cache
					writeLabel(RegistryCacheReader.EXTENSION_INDEX_LABEL, out);
					out.writeInt(extensionIndex);
				} else {
					writeExtension(extensions[i], out);
				}
			}
		}

		writeLabel(RegistryCacheReader.EXTENSION_POINT_END_LABEL, out);
	} catch (IOException ioe) {
		cacheWriteProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", RegistryCacheReader.decipherLabel(RegistryCacheReader.PLUGIN_EXTENSION_POINT_LABEL)), ioe));
	}
}
public void writeHeaderInformation(DataOutputStream out) {
	try {
		out.writeInt(RegistryCacheReader.REGISTRY_CACHE_VERSION);
		// install stamp
		out.writeLong(InternalPlatform.getRegistryCacheTimeStamp());
		// OS stamp
		out.writeUTF(BootLoader.getOS());
		// windows system stamp
		out.writeUTF(BootLoader.getWS());
		// locale stamp
		out.writeUTF(BootLoader.getNL());
	} catch (IOException ioe) {
		cacheWriteProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", "HeaderInformation"), ioe));
	}
}
public void writeLibrary(LibraryModel library, DataOutputStream out) {
	try {
		String outString;

		writeLabel(RegistryCacheReader.PLUGIN_LIBRARY_LABEL, out);

		writeLabel(RegistryCacheReader.READONLY_LABEL, out);
		out.writeBoolean(library.isReadOnly());

		if ((outString = library.getName()) != null) {
			writeLabel(RegistryCacheReader.NAME_LABEL, out);
			out.writeUTF(outString);
		}
		if ((outString = library.getType()) != null) {
			writeLabel(RegistryCacheReader.TYPE_LABEL, out);
			out.writeUTF(outString);
		}

		String[] exports = null;
		if ((exports = library.getExports()) != null) {
			writeLabel(RegistryCacheReader.LIBRARY_EXPORTS_LENGTH_LABEL, out);
			out.writeInt(exports.length);
			writeLabel(RegistryCacheReader.LIBRARY_EXPORTS_LABEL, out);
			for (int i = 0; i < exports.length; i++) {
				out.writeUTF(exports[i]);
			}
		}

		// Don't bother caching 'isExported' and 'isFullyExported'.  There
		// is no way of explicitly setting these fields.  They are computed
		// from the values in the 'exports' list.
		writeLabel(RegistryCacheReader.LIBRARY_END_LABEL, out);
	} catch (IOException ioe) {
		cacheWriteProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", RegistryCacheReader.decipherLabel(RegistryCacheReader.PLUGIN_LIBRARY_LABEL)), ioe));
	}
}
public void writePluginDescriptor(PluginDescriptorModel plugin, DataOutputStream out) {

	try {
		// Check to see if this plugin already exists in the objectTable.  If it is there,
		// it has already been written to the cache so just write out the index.
		int pluginIndex = objectTable.indexOf(plugin);
		if (pluginIndex != -1) {
			// this plugin is already there
			writeLabel(RegistryCacheReader.PLUGIN_INDEX_LABEL, out);
			out.writeInt(pluginIndex);
			return;
		}

		// add this object to the object table first
		addToObjectTable(plugin);
		String outString;

		writeLabel(RegistryCacheReader.PLUGIN_LABEL, out);
		writeLabel(RegistryCacheReader.READONLY_LABEL, out);
		out.writeBoolean(plugin.isReadOnly());
		if ((outString = plugin.getName()) != null) {
			writeLabel(RegistryCacheReader.NAME_LABEL, out);
			out.writeUTF(outString);
		}
		if ((outString = plugin.getId()) != null) {
			writeLabel(RegistryCacheReader.ID_LABEL, out);
			out.writeUTF(outString);
		}
		if ((outString = plugin.getProviderName()) != null) {
			writeLabel(RegistryCacheReader.PLUGIN_PROVIDER_NAME_LABEL, out);
			out.writeUTF(outString);
		}
		if ((outString = plugin.getVersion()) != null) {
			writeLabel(RegistryCacheReader.VERSION_LABEL, out);
			out.writeUTF(outString);
		}
		if ((outString = plugin.getPluginClass()) != null) {
			writeLabel(RegistryCacheReader.PLUGIN_CLASS_LABEL, out);
			out.writeUTF(outString);
		}
		if ((outString = plugin.getLocation()) != null) {
			writeLabel(RegistryCacheReader.PLUGIN_LOCATION_LABEL, out);
			out.writeUTF(outString);
		}
		writeLabel(RegistryCacheReader.PLUGIN_ENABLED_LABEL, out);
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
		writeLabel(RegistryCacheReader.PLUGIN_PARENT_LABEL, out);
		// We can assume that the parent registry is already written out.
		out.writeInt(objectTable.indexOf(parentRegistry));

		writeLabel(RegistryCacheReader.PLUGIN_END_LABEL, out);
	} catch (IOException ioe) {
		cacheWriteProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", RegistryCacheReader.decipherLabel(RegistryCacheReader.PLUGIN_LABEL)), ioe));
	}
}
public void writePluginFragment(PluginFragmentModel fragment, DataOutputStream out) {

	try {
		// Check to see if this fragment already exists in the objectTable.  If it is there,
		// it has already been written to the cache so just write out the index.
		int fragmentIndex = objectTable.indexOf(fragment);
		if (fragmentIndex != -1) {
			// this fragment is already there
			writeLabel(RegistryCacheReader.FRAGMENT_INDEX_LABEL, out);
			out.writeInt(fragmentIndex);
			return;
		}

		// add this object to the object table first
		addToObjectTable(fragment);
		String outString;
		byte outByte;

		writeLabel(RegistryCacheReader.FRAGMENT_LABEL, out);
		writeLabel(RegistryCacheReader.READONLY_LABEL, out);
		out.writeBoolean(fragment.isReadOnly());
		if ((outString = fragment.getName()) != null) {
			writeLabel(RegistryCacheReader.NAME_LABEL, out);
			out.writeUTF(outString);
		}
		if ((outString = fragment.getId()) != null) {
			writeLabel(RegistryCacheReader.ID_LABEL, out);
			out.writeUTF(outString);
		}
		if ((outString = fragment.getProviderName()) != null) {
			writeLabel(RegistryCacheReader.PLUGIN_PROVIDER_NAME_LABEL, out);
			out.writeUTF(outString);
		}
		if ((outString = fragment.getVersion()) != null) {
			writeLabel(RegistryCacheReader.VERSION_LABEL, out);
			out.writeUTF(outString);
		}
		if ((outString = fragment.getLocation()) != null) {
			writeLabel(RegistryCacheReader.PLUGIN_LOCATION_LABEL, out);
			out.writeUTF(outString);
		}
		if ((outString = fragment.getPlugin()) != null) {
			writeLabel(RegistryCacheReader.FRAGMENT_PLUGIN_LABEL, out);
			out.writeUTF(outString);
		}
		if ((outString = fragment.getPluginVersion()) != null) {
			writeLabel(RegistryCacheReader.FRAGMENT_PLUGIN_VERSION_LABEL, out);
			out.writeUTF(outString);
		}
		if ((outByte = fragment.getMatch()) != PluginFragmentModel.FRAGMENT_MATCH_UNSPECIFIED) {
			writeLabel(RegistryCacheReader.FRAGMENT_PLUGIN_MATCH_LABEL, out);
			out.writeByte(outByte);
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
		writeLabel(RegistryCacheReader.PLUGIN_PARENT_LABEL, out);
		// We can assume that the parent registry is already written out.
		out.writeInt(objectTable.indexOf(parentRegistry));

		writeLabel(RegistryCacheReader.FRAGMENT_END_LABEL, out);
	} catch (IOException ioe) {
		cacheWriteProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", RegistryCacheReader.decipherLabel(RegistryCacheReader.FRAGMENT_LABEL)), ioe));
	}
}
public void writePluginPrerequisite(PluginPrerequisiteModel requires, DataOutputStream out) {
	try {
		String outString = null;

		writeLabel(RegistryCacheReader.PLUGIN_REQUIRES_LABEL, out);

		writeLabel(RegistryCacheReader.READONLY_LABEL, out);
		out.writeBoolean(requires.isReadOnly());

		if ((outString = requires.getName()) != null) {
			writeLabel(RegistryCacheReader.NAME_LABEL, out);
			out.writeUTF(outString);
		}

		if ((outString = requires.getVersion()) != null) {
			writeLabel(RegistryCacheReader.VERSION_LABEL, out);
			out.writeUTF(outString);
		}
		
		byte outMatch = requires.getMatchByte();
		if (outMatch != PluginPrerequisiteModel.PREREQ_MATCH_UNSPECIFIED) {
			writeLabel(RegistryCacheReader.REQUIRES_MATCH_LABEL, out);
			out.writeByte(requires.getMatchByte());
		}

		writeLabel(RegistryCacheReader.REQUIRES_EXPORT_LABEL, out);
		out.writeBoolean(requires.getExport());

		writeLabel(RegistryCacheReader.REQUIRES_OPTIONAL_LABEL, out);
		out.writeBoolean(requires.getOptional());

		if ((outString = requires.getResolvedVersion()) != null) {
			writeLabel(RegistryCacheReader.REQUIRES_RESOLVED_VERSION_LABEL, out);
			out.writeUTF(outString);
		}

		if ((outString = requires.getPlugin()) != null) {
			writeLabel(RegistryCacheReader.REQUIRES_PLUGIN_NAME_LABEL, out);
			out.writeUTF(outString);
		}

		writeLabel(RegistryCacheReader.REQUIRES_END_LABEL, out);
	} catch (IOException ioe) {
		cacheWriteProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", RegistryCacheReader.decipherLabel(RegistryCacheReader.PLUGIN_REQUIRES_LABEL)), ioe));
	}
}
public void writePluginRegistry(PluginRegistryModel registry, DataOutputStream out) {
	if (cacheWriteProblems == null) {
		cacheWriteProblems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("meta.registryCacheWriteProblems"), null);
	}

	try {
		// Check to see if this registry already exists in the objectTable.  If it is there,
		// it has already been written to the cache so just write out the index.
		if (objectTable != null) {
			int registryIndex = objectTable.indexOf(registry);
			if (registryIndex != -1) {
				// this plugin is already there
				writeLabel(RegistryCacheReader.REGISTRY_INDEX_LABEL, out);
				out.writeInt(registryIndex);
				return;
			}
		}

		// add this object to the object table first
		addToObjectTable(registry);
		writeHeaderInformation(out);
	
		writeLabel(RegistryCacheReader.REGISTRY_LABEL, out);

		writeLabel(RegistryCacheReader.READONLY_LABEL, out);
		out.writeBoolean(registry.isReadOnly());

		writeLabel(RegistryCacheReader.REGISTRY_RESOLVED_LABEL, out);
		out.writeBoolean(registry.isResolved());
		PluginDescriptorModel[] pluginList = registry.getPlugins();
		for (int i = 0; i < pluginList.length; i++)
			writePluginDescriptor(pluginList[i], out);
		PluginFragmentModel[] fragmentList = registry.getFragments();
		int fragmentLength = (fragmentList == null) ? 0 : fragmentList.length;
		for (int i = 0; i < fragmentLength; i++)
			writePluginFragment(fragmentList[i], out);
		writeLabel(RegistryCacheReader.REGISTRY_END_LABEL, out);
	} catch (IOException ioe) {
		cacheWriteProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", RegistryCacheReader.decipherLabel(RegistryCacheReader.REGISTRY_LABEL)), ioe));
	}
}
}
