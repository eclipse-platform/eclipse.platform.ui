package org.eclipse.core.internal.plugins;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.core.runtime.model.*;
import org.eclipse.core.internal.plugins.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.EOFException;

public class RegistryCacheReader {

	Factory cacheFactory;

	public static final int REGISTRY_CACHE_VERSION = 1;

	public static final int NONLABEL = 0;
	public static final int READONLY_LABEL = 1;
	public static final int NAME_LABEL = 2;

	public static final int ID_LABEL = 10;
	public static final int PLUGIN_PROVIDER_NAME_LABEL = 11;
	public static final int VERSION_LABEL = 12;
	public static final int PLUGIN_CLASS_LABEL = 13;
	public static final int PLUGIN_LOCATION_LABEL = 14;
	public static final int PLUGIN_ENABLED_LABEL = 15;
	public static final int PLUGIN_REQUIRES_LABEL = 20;
	public static final int PLUGIN_LIBRARY_LABEL = 30;
	public static final int PLUGIN_EXTENSION_LABEL = 40;
	public static final int PLUGIN_EXTENSION_POINT_LABEL = 50;
	public static final int PLUGIN_END_LABEL = 19;

	public static final int REQUIRES_MATCH_LABEL = 23;
	public static final int REQUIRES_EXPORT_LABEL = 24;
	public static final int REQUIRES_RESOLVED_VERSION_LABEL = 25;
	public static final int REQUIRES_PLUGIN_NAME_LABEL = 26;
	public static final int REQUIRES_END_LABEL = 29;

	public static final int LIBRARY_EXPORTS_LENGTH_LABEL = 31;
	public static final int LIBRARY_EXPORTS_LABEL = 32;
	public static final int LIBRARY_END_LABEL = 39;

	public static final int EXTENSION_EXT_POINT_NAME_LABEL = 41;
	public static final int SUBELEMENTS_LENGTH_LABEL = 42;
	public static final int EXTENSION_END_LABEL = 49;

	public static final int EXTENSION_POINT_SCHEMA_LABEL = 51;
	public static final int EXTENSION_POINT_EXTENSIONS_LABEL = 52;
	public static final int EXTENSION_POINT_END_LABEL = 59;

	public static final int CONFIGURATION_ELEMENT_LABEL = 60;
	public static final int VALUE_LABEL = 61;
	public static final int PROPERTIES_LENGTH_LABEL = 62;
	public static final int CONFIGURATION_ELEMENT_END_LABEL = 69;

	public static final int CONFIGURATION_PROPERTY_LABEL = 70;
	public static final int CONFIGURATION_PROPERTY_END_LABEL = 79;
public RegistryCacheReader(Factory factory) {
	super();
	cacheFactory = factory;
}
public int decipherLabel(String labelString) {
	if (labelString.equals("<readonly>")) {
		return READONLY_LABEL;
	}
	if (labelString.equals("<name>")) {
		return NAME_LABEL;
	}
	if (labelString.equals("<id>")) {
		return ID_LABEL;
	}
	if (labelString.equals("<provider>")) {
		return PLUGIN_PROVIDER_NAME_LABEL;
	}
	if (labelString.equals("<version>")) {
		return VERSION_LABEL;
	}
	if (labelString.equals("<class>")) {
		return PLUGIN_CLASS_LABEL;
	}
	if (labelString.equals("<location>")) {
		return PLUGIN_LOCATION_LABEL;
	}
	if (labelString.equals("<enabled>")) {
		return PLUGIN_ENABLED_LABEL;
	}
	if (labelString.equals("<requires>")) {
		return PLUGIN_REQUIRES_LABEL;
	}
	if (labelString.equals("<library>")) {
		return PLUGIN_LIBRARY_LABEL;
	}
	if (labelString.equals("<extension>")) {
		return PLUGIN_EXTENSION_LABEL;
	}
	if (labelString.equals("<extension_point>")) {
		return PLUGIN_EXTENSION_POINT_LABEL;
	}
	if (labelString.equals("<endplugin>")) {
		return PLUGIN_END_LABEL;
	}
	if (labelString.equals("<match>")) {
		return REQUIRES_MATCH_LABEL;
	}
	if (labelString.equals("<export>")) {
		return REQUIRES_EXPORT_LABEL;
	}
	if (labelString.equals("<resolved_version>")) {
		return REQUIRES_RESOLVED_VERSION_LABEL;
	}
	if (labelString.equals("<requires_plugin_name>")) {
		return REQUIRES_PLUGIN_NAME_LABEL;
	}
	if (labelString.equals("<endrequires>")) {
		return REQUIRES_END_LABEL;
	}
	if (labelString.equals("<exports-length>")) {
		return LIBRARY_EXPORTS_LENGTH_LABEL;
	}
	if (labelString.equals("<exports>")) {
		return LIBRARY_EXPORTS_LABEL;
	}
	if (labelString.equals("<endlibrary>")) {
		return LIBRARY_END_LABEL;
	}
	if (labelString.equals("<schema>")) {
		return EXTENSION_POINT_SCHEMA_LABEL;
	}
	if (labelString.equals("<endextensionPoint>")) {
		return EXTENSION_POINT_END_LABEL;
	}
	return NONLABEL;
}
public boolean interpretHeaderInformation(DataInputStream in) {
	int version = 0;
	try {
		version = in.readInt();
		// output some stamps too
		// windows system stamp
		// OS stamp
		// install stamp
		// locale stamp
	} catch (IOException ioe) {
		return false;
	}
	if (version != REGISTRY_CACHE_VERSION) {
		return false;
	}
	return true;
}
public ConfigurationElementModel readConfigurationElement(DataInputStream in) {
	ConfigurationElementModel configurationElement = cacheFactory.createConfigurationElement();
	// Use this flag to determine if the read-only flag should be set.  You
	// can't set it now or you won't be able to add anything more to this
	// configuration element.
	boolean setReadOnlyFlag = false;
	try {
		String inString = null;
		boolean done = false;
		while (!done) {
			try {
				inString = in.readUTF();
			} catch (EOFException eofe) {
				done = true;
				break;
			}
			switch (decipherLabel(inString)) {
				case READONLY_LABEL :
					if (in.readBoolean()) {
						setReadOnlyFlag = true;
					}
					break;
				case NAME_LABEL :
					configurationElement.setName(in.readUTF());
					break;
				case VALUE_LABEL :
					configurationElement.setValue(in.readUTF());
					break;
				case PROPERTIES_LENGTH_LABEL :
					int propertiesLength = in.readInt();
					ConfigurationPropertyModel[] properties = new ConfigurationPropertyModel[propertiesLength];
					for (int i = 0; i < propertiesLength; i++) {
						properties[i] = readConfigurationProperty(in);
					}
					configurationElement.setProperties(properties);
					properties = null;
					break;
				case SUBELEMENTS_LENGTH_LABEL :
					int subElementsLength = in.readInt();
					ConfigurationElementModel[] subElements = new ConfigurationElementModel[subElementsLength];
					for (int i = 0; i < subElementsLength; i++) {
						subElements[i] = readConfigurationElement(in);
					}
					configurationElement.setSubElements(subElements);
					subElements = null;
					break;
				case CONFIGURATION_ELEMENT_END_LABEL :
					done = true;
			}
		}
	} catch (IOException ioe) {
		return null;
	}
	if (setReadOnlyFlag) {
		configurationElement.markReadOnly();
	}
	return configurationElement;
}
public ConfigurationPropertyModel readConfigurationProperty(DataInputStream in) {
	ConfigurationPropertyModel configurationProperty = cacheFactory.createConfigurationProperty();
	// Use this flag to determine if the read-only flag should be set.  You
	// can't set it now or you won't be able to add anything more to this
	// configuration property.
	boolean setReadOnlyFlag = false;
	try {
		String inString = null;
		boolean done = false;
		while (!done) {
			try {
				inString = in.readUTF();
			} catch (EOFException eofe) {
				done = true;
				break;
			}
			switch (decipherLabel(inString)) {
				case READONLY_LABEL :
					if (in.readBoolean()) {
						setReadOnlyFlag = true;
					}
					break;
				case NAME_LABEL :
					configurationProperty.setName(in.readUTF());
					break;
				case VALUE_LABEL :
					configurationProperty.setValue(in.readUTF());
					break;
				case CONFIGURATION_PROPERTY_END_LABEL :
					done = true;
			}
		}
	} catch (IOException ioe) {
		return null;
	}
	if (setReadOnlyFlag) {
		configurationProperty.markReadOnly();
	}
	return configurationProperty;
}
public ExtensionModel readExtension(DataInputStream in) {
	ExtensionModel extension = cacheFactory.createExtension();
	// Use this flag to determine if the read-only flag should be set.  You
	// can't set it now or you won't be able to add anything more to this
	// extension.
	boolean setReadOnlyFlag = false;
	try {
		String inString = null;
		boolean done = false;
		while (!done) {
			try {
				inString = in.readUTF();
			} catch (EOFException eofe) {
				done = true;
				break;
			}
			switch (decipherLabel(inString)) {
				case READONLY_LABEL :
					if (in.readBoolean()) {
						setReadOnlyFlag = true;
					}
					break;
				case NAME_LABEL :
					extension.setName(in.readUTF());
					break;
				case ID_LABEL :
					extension.setId(in.readUTF());
					break;
				case EXTENSION_EXT_POINT_NAME_LABEL :
					extension.setExtensionPoint(in.readUTF());
					break;
				case SUBELEMENTS_LENGTH_LABEL :
					int subElementsLength = in.readInt();
					ConfigurationElementModel[] subElements = new ConfigurationElementModel[subElementsLength];
					for (int i = 0; i < subElementsLength; i++) {
						subElements[i] = readConfigurationElement(in);
					}
					extension.setSubElements(subElements);
					subElements = null;
					break;
				case EXTENSION_POINT_END_LABEL :
					done = true;
			}
		}
	} catch (IOException ioe) {
		return null;
	}
	if (setReadOnlyFlag) {
		extension.markReadOnly();
	}
	return extension;
}
public ExtensionPointModel readExtensionPoint(DataInputStream in) {
	ExtensionPointModel extPoint = cacheFactory.createExtensionPoint();
	// Use this flag to determine if the read-only flag should be set.  You
	// can't set it now or you won't be able to add anything more to this
	// extension point.
	boolean setReadOnlyFlag = false;
	try {
		String inString = null;
		boolean done = false;
		while (!done) {
			try {
				inString = in.readUTF();
			} catch (EOFException eofe) {
				done = true;
				break;
			}
			switch (decipherLabel(inString)) {
				case READONLY_LABEL :
					if (in.readBoolean()) {
						setReadOnlyFlag = true;
					}
					break;
				case NAME_LABEL :
					extPoint.setName(in.readUTF());
					break;
				case ID_LABEL :
					extPoint.setId(in.readUTF());
					break;
				case EXTENSION_POINT_SCHEMA_LABEL :
					extPoint.setSchema(in.readUTF());
					break;
				case EXTENSION_POINT_EXTENSIONS_LABEL :
					// Add stuff here
					break;
				case EXTENSION_POINT_END_LABEL :
					done = true;
			}
		}
	} catch (IOException ioe) {
		return null;
	}
	if (setReadOnlyFlag) {
		extPoint.markReadOnly();
	}
	return extPoint;
}
public LibraryModel readLibrary(DataInputStream in) {
	LibraryModel library = cacheFactory.createLibrary();
	// Use this flag to determine if the read-only flag should be set.  You
	// can't set it now or you won't be able to add anything more to this
	// library.
	boolean setReadOnlyFlag = false;
	int exportsLength = 0;
	try {
		String inString = null;
		boolean done = false;
		while (!done) {
			try {
				inString = in.readUTF();
			} catch (EOFException eofe) {
				done = true;
				break;
			}
			switch (decipherLabel(inString)) {
				case READONLY_LABEL :
					if (in.readBoolean()) {
						setReadOnlyFlag = true;
					}
					break;
				case NAME_LABEL :
					library.setName(in.readUTF());
					break;
				case LIBRARY_EXPORTS_LENGTH_LABEL :
					exportsLength = in.readInt();
					break;
				case LIBRARY_EXPORTS_LABEL :
					String[] exports = new String[exportsLength];
					for (int i = 0; i < exportsLength; i++) {
						exports[i] = in.readUTF();
					}
					library.setExports(exports);
					exports = null;
					break;
				case LIBRARY_END_LABEL :
					done = true;
			}
		}
	} catch (IOException ioe) {
		return null;
	}
	if (setReadOnlyFlag) {
		library.markReadOnly();
	}
	return library;
}
public PluginDescriptorModel readPluginDescriptor(DataInputStream in) {
	try {
		String inString = in.readUTF();
		if (!inString.equals("<plugin>")) {
			return null;
		}
	} catch (IOException ioe) {
		return null;
	}
	PluginDescriptorModel plugin = cacheFactory.createPluginDescriptor();
	// Use this flag to determine if the read-only flag should be set.  You
	// can't set it now or you won't be able to add anything more to this
	// plugin.
	boolean setReadOnlyFlag = false;
	try {
		String inString = null;
		boolean done = false;
		while (!done) {
			try {
				inString = in.readUTF();
			} catch (EOFException eofe) {
				done = true;
				break;
			}
			switch (decipherLabel(inString)) {
				case READONLY_LABEL :
					if (in.readBoolean()) {
						setReadOnlyFlag = true;
					}
					break;
				case NAME_LABEL :
					plugin.setName(in.readUTF());
					break;
				case ID_LABEL :
					plugin.setId(in.readUTF());
					break;
				case PLUGIN_PROVIDER_NAME_LABEL :
					plugin.setProviderName(in.readUTF());
					break;
				case VERSION_LABEL :
					plugin.setVersion(in.readUTF());
					break;
				case PLUGIN_CLASS_LABEL :
					plugin.setPluginClass(in.readUTF());
					break;
				case PLUGIN_LOCATION_LABEL :
					plugin.setLocation(in.readUTF());
					break;
				case PLUGIN_ENABLED_LABEL :
					plugin.setEnabled(in.readBoolean());
					break;
				case PLUGIN_REQUIRES_LABEL :
					PluginPrerequisiteModel requires = readPluginPrerequisite(in);
					// Add this prerequisite to the end of the requires list
					PluginPrerequisiteModel[] requiresList = plugin.getRequires();
					PluginPrerequisiteModel[] newRequiresValues = null;
					if (requiresList == null) {
						newRequiresValues = new PluginPrerequisiteModel[1];
						newRequiresValues[0] = requires;
					} else {
						newRequiresValues = new PluginPrerequisiteModel[requiresList.length + 1];
						System.arraycopy(requiresList, 0, newRequiresValues, 0, requiresList.length);
						newRequiresValues[requiresList.length] = requires;
					}
					plugin.setRequires(newRequiresValues);
					requires = null;
					requiresList = newRequiresValues = null;
					break;
				case PLUGIN_LIBRARY_LABEL :
					LibraryModel library = readLibrary(in);
					// Add this library to the end of the runtime list
					LibraryModel[] libraryList = plugin.getRuntime();
					LibraryModel[] newLibraryValues = null;
					if (libraryList == null) {
						newLibraryValues = new LibraryModel[1];
						newLibraryValues[0] = library;
					} else {
						newLibraryValues = new LibraryModel[libraryList.length + 1];
						System.arraycopy(libraryList, 0, newLibraryValues, 0, libraryList.length);
						newLibraryValues[libraryList.length] = library;
					}
					plugin.setRuntime(newLibraryValues);
					library = null;
					libraryList = newLibraryValues = null;
					break;
				case PLUGIN_EXTENSION_LABEL :
					ExtensionModel extension = readExtension(in);
					ExtensionModel[] extList = plugin.getDeclaredExtensions();
					ExtensionModel[] newExtValues = null;
					if (extList == null) {
						newExtValues = new ExtensionModel[1];
						newExtValues[0] = extension;
					} else {
						newExtValues = new ExtensionModel[extList.length + 1];
						System.arraycopy(extList, 0, newExtValues, 0, extList.length);
						newExtValues[extList.length] = extension;
					}
					plugin.setDeclaredExtensions(newExtValues);
					extension = null;
					extList = newExtValues = null;
					break;
				case PLUGIN_EXTENSION_POINT_LABEL :
					ExtensionPointModel extensionPoint = readExtensionPoint(in);
					// Add this extension point to the end of the extension point list
					ExtensionPointModel[] extPointList = plugin.getDeclaredExtensionPoints();
					ExtensionPointModel[] newExtPointValues = null;
					if (extPointList == null) {
						newExtPointValues = new ExtensionPointModel[1];
						newExtPointValues[0] = extensionPoint;
					} else {
						newExtPointValues = new ExtensionPointModel[extPointList.length + 1];
						System.arraycopy(extPointList, 0, newExtPointValues, 0, extPointList.length);
						newExtPointValues[extPointList.length] = extensionPoint;
					}
					plugin.setDeclaredExtensionPoints(newExtPointValues);
					extensionPoint = null;
					extPointList = newExtPointValues = null;
					break;
				case PLUGIN_END_LABEL :
					done = true;
			}
		}
	} catch (IOException ioe) {
		return null;
	}
	if (setReadOnlyFlag) {
		plugin.markReadOnly();
	}
	return plugin;
}
public PluginPrerequisiteModel readPluginPrerequisite(DataInputStream in) {
	PluginPrerequisiteModel requires = cacheFactory.createPluginPrerequisite();
	// Use this flag to determine if the read-only flag should be set.  You
	// can't set it now or you won't be able to add anything more to this
	// prerequisite.
	boolean setReadOnlyFlag = false;
	try {
		String inString = null;
		boolean done = false;
		while (!done) {
			try {
				inString = in.readUTF();
			} catch (EOFException eofe) {
				done = true;
				break;
			}
			switch (decipherLabel(inString)) {
				case READONLY_LABEL :
					if (in.readBoolean()) {
						setReadOnlyFlag = true;
					}
					break;
				case NAME_LABEL :
					requires.setName(in.readUTF());
					break;
				case VERSION_LABEL :
					requires.setVersion(in.readUTF());
					break;
				case REQUIRES_MATCH_LABEL :
					requires.setMatch(in.readBoolean());
					break;
				case REQUIRES_EXPORT_LABEL :
					requires.setExport(in.readBoolean());
					break;
				case REQUIRES_RESOLVED_VERSION_LABEL :
					requires.setResolvedVersion(in.readUTF());
					break;
				case REQUIRES_PLUGIN_NAME_LABEL :
					requires.setPlugin(in.readUTF());
					break;
				case REQUIRES_END_LABEL :
					done = true;
			}
		}
	} catch (IOException ioe) {
		return null;
	}
	if (setReadOnlyFlag) {
		requires.markReadOnly();
	}
	return requires;
}
public PluginRegistryModel readPluginRegistry(DataInputStream in) {
	if (!interpretHeaderInformation(in)) {
		return null;
	}
	PluginRegistryModel cachedRegistry = cacheFactory.createPluginRegistry();
	PluginDescriptorModel plugin = null;
	while ((plugin = readPluginDescriptor(in)) != null) {
		cachedRegistry.addPlugin(plugin);
	}
	// if there are no plugins in the registry, return null instead of
	// an empty registry?
	return cachedRegistry;
}
}
