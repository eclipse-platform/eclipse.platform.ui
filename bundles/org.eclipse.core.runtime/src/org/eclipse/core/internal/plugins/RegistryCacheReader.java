package org.eclipse.core.internal.plugins;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;
import org.eclipse.core.internal.plugins.*;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.internal.boot.LaunchInfo;
import java.io.*;
import java.util.ArrayList;

public class RegistryCacheReader {

	Factory cacheFactory;
	// objectTable will be an array list of objects.  The objects will be things 
	// like a plugin descriptor, extension, extension point, etc.  The integer 
	// index value will be used in the cache to allow cross-references in the 
	// cached registry.
	ArrayList objectTable = null;

	public MultiStatus cacheReadProblems = null;

	public static final byte REGISTRY_CACHE_VERSION = 2;

	public static final byte NONLABEL = 0;

	public static final byte CONFIGURATION_ELEMENT_END_LABEL = 1;
	public static final byte CONFIGURATION_ELEMENT_INDEX_LABEL = 45;
	public static final byte CONFIGURATION_ELEMENT_LABEL = 2;
	public static final byte CONFIGURATION_ELEMENT_PARENT_LABEL = 3;
	public static final byte CONFIGURATION_PROPERTY_END_LABEL = 4;
	public static final byte CONFIGURATION_PROPERTY_LABEL = 5;

	public static final byte EXTENSION_END_LABEL = 6;
	public static final byte EXTENSION_EXT_POINT_NAME_LABEL = 7;
	public static final byte EXTENSION_INDEX_LABEL = 8;
	public static final byte EXTENSION_PARENT_LABEL = 9;

	public static final byte EXTENSION_POINT_END_LABEL = 10;
	public static final byte EXTENSION_POINT_EXTENSIONS_LENGTH_LABEL = 11;
	public static final byte EXTENSION_POINT_EXTENSIONS_LABEL = 12;
	public static final byte EXTENSION_POINT_PARENT_LABEL = 13;
	public static final byte EXTENSION_POINT_SCHEMA_LABEL = 14;
	
	public static final byte FRAGMENT_INDEX_LABEL = 47;
	public static final byte FRAGMENT_LABEL = 48;
	public static final byte FRAGMENT_END_LABEL = 49;
	public static final byte FRAGMENT_PLUGIN_LABEL = 50;
	public static final byte FRAGMENT_PLUGIN_VERSION_LABEL = 51;

	public static final byte ID_LABEL = 15;
	public static final byte LIBRARY_END_LABEL = 16;
	public static final byte LIBRARY_EXPORTS_LABEL = 17;
	public static final byte LIBRARY_EXPORTS_LENGTH_LABEL = 18;
	public static final byte NAME_LABEL = 19;

	public static final byte PLUGIN_CLASS_LABEL = 20;
	public static final byte PLUGIN_ENABLED_LABEL = 21;
	public static final byte PLUGIN_END_LABEL = 22;
	public static final byte PLUGIN_EXTENSION_LABEL = 23;
	public static final byte PLUGIN_EXTENSION_POINT_LABEL = 24;
	public static final byte PLUGIN_INDEX_LABEL = 25;
	public static final byte PLUGIN_LABEL = 26;
	public static final byte PLUGIN_LOCATION_LABEL = 27;
	public static final byte PLUGIN_LIBRARY_LABEL = 28;
	public static final byte PLUGIN_PARENT_LABEL = 29;
	public static final byte PLUGIN_PROVIDER_NAME_LABEL = 30;
	public static final byte PLUGIN_REQUIRES_LABEL = 31;

	public static final byte PROPERTIES_LENGTH_LABEL = 32;
	public static final byte READONLY_LABEL = 33;
	public static final byte REGISTRY_END_LABEL = 34;
	public static final byte REGISTRY_INDEX_LABEL = 46;
	public static final byte REGISTRY_LABEL = 35;
	public static final byte REGISTRY_RESOLVED_LABEL = 36;
	public static final byte REQUIRES_END_LABEL = 37;
	public static final byte REQUIRES_EXPORT_LABEL = 38;
	public static final byte REQUIRES_MATCH_LABEL = 39;
	public static final byte REQUIRES_OPTIONAL_LABEL = 52;
	public static final byte REQUIRES_PLUGIN_NAME_LABEL = 40;
	public static final byte REQUIRES_RESOLVED_VERSION_LABEL = 41;
	public static final byte SOURCE_LABEL = 53;
	public static final byte SUBELEMENTS_LENGTH_LABEL = 42;
	public static final byte TYPE_LABEL = 54;
	public static final byte VALUE_LABEL = 43;
	public static final byte VERSION_LABEL = 44;
	
public RegistryCacheReader(Factory factory) {
	super();
	cacheFactory = factory;
	objectTable = null;
}
public int addToObjectTable(Object object) {
	if (objectTable == null) {
		objectTable = new ArrayList();
	}
	objectTable.add(object);
	// return the index of the object just added (i.e. size - 1)
	return (objectTable.size() - 1);

}
public static String decipherLabel(byte labelValue) {
	switch (labelValue) {
		case REGISTRY_LABEL:
			return "<registry>";
		case REGISTRY_RESOLVED_LABEL:
			return "<resolved>";
		case PLUGIN_LABEL:
			return "<plugin>";
		case REGISTRY_END_LABEL:
			return "<endregistry>";
		case READONLY_LABEL:
			return "<readonly>";
		case NAME_LABEL:
			return "<name>";
		case ID_LABEL:
			return "<id>";
		case PLUGIN_PROVIDER_NAME_LABEL:
			return "<provider>";
		case VERSION_LABEL:
			return "<version>";
		case PLUGIN_CLASS_LABEL:
			return "<class>";
		case PLUGIN_LOCATION_LABEL:
			return "<location>";
		case PLUGIN_ENABLED_LABEL:
			return "<enabled>";
		case PLUGIN_REQUIRES_LABEL:
			return "<requires>";
		case PLUGIN_LIBRARY_LABEL:
			return "<library>";
		case PLUGIN_EXTENSION_LABEL:
			return "<extension>";
		case PLUGIN_EXTENSION_POINT_LABEL:
			return "<extensionPoint>";
		case PLUGIN_END_LABEL:
			return "<endplugin>";
		case REQUIRES_MATCH_LABEL:
			return "<match>";
		case REQUIRES_EXPORT_LABEL:
			return "<export>";
		case REQUIRES_RESOLVED_VERSION_LABEL:
			return "<resolved_version>";
		case REQUIRES_PLUGIN_NAME_LABEL:
			return "<requires_plugin_name>";
		case REQUIRES_END_LABEL:
			return "<endrequires>";
		case LIBRARY_EXPORTS_LENGTH_LABEL:
			return "<exports-length>";
		case LIBRARY_EXPORTS_LABEL:
			return "<exports>";
		case LIBRARY_END_LABEL:
			return "<endlibrary>";
		case EXTENSION_POINT_SCHEMA_LABEL:
			return "<schema>";
		case EXTENSION_POINT_END_LABEL:
			return "<endextensionPoint>";
		case EXTENSION_EXT_POINT_NAME_LABEL:
			return "<extension-extPt-name>";
		case SUBELEMENTS_LENGTH_LABEL:
			return "<subElements-length>";
		case EXTENSION_END_LABEL:
			return "<endextension>";
		case CONFIGURATION_ELEMENT_LABEL:
			return "<configuration-element>";
		case VALUE_LABEL:
			return "<value>";
		case PROPERTIES_LENGTH_LABEL:
			return "<properties-length>";
		case CONFIGURATION_ELEMENT_END_LABEL:
			return "<endconfiguration-element>";
		case CONFIGURATION_PROPERTY_LABEL:
			return "<configuration-property>";
		case CONFIGURATION_PROPERTY_END_LABEL:
			return "<endconfiguration-property>";
		case PLUGIN_PARENT_LABEL:
			return "<parentRegistry>";
		case CONFIGURATION_ELEMENT_PARENT_LABEL:
			return "<ConfigurationElementParent>";
		case PLUGIN_INDEX_LABEL:
			return "<pluginIndex>";
		case EXTENSION_INDEX_LABEL:
			return "<extensionIndex>";
		case EXTENSION_POINT_PARENT_LABEL:
			return "<ExtensionPointParent>";
		case EXTENSION_POINT_EXTENSIONS_LENGTH_LABEL:
			return "<extensionPointExtensionsLength>";
		case EXTENSION_POINT_EXTENSIONS_LABEL:
			return "<extensionPointExtensions>";
		case EXTENSION_PARENT_LABEL:
			return "<extensionParent>";
		case CONFIGURATION_ELEMENT_INDEX_LABEL:
			return "<configElementIndex>";
		case REGISTRY_INDEX_LABEL:
			return "<registryIndex>";
	}

	return "<unknown label>";
}
public boolean interpretHeaderInformation(DataInputStream in) {
	try {
		int version = in.readInt();
		// install stamp
		String installStamp = in.readUTF();
		// OS stamp
		String osStamp = in.readUTF();
		// windows system stamp
		String windowsStamp = in.readUTF();
		// locale stamp
		String localeStamp = in.readUTF();

		return ((version == REGISTRY_CACHE_VERSION) &&
			(installStamp.equals(LaunchInfo.getCurrent().getIdentifier())) &&
			(osStamp.equals(BootLoader.getOS())) &&
			(windowsStamp.equals(BootLoader.getWS())) &&
			(localeStamp.equals(BootLoader.getNL())) );
	} catch (IOException ioe) {
		cacheReadProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", "HeaderInformation"), ioe));
		return false;
	}
}
public ConfigurationElementModel readConfigurationElement(DataInputStream in) {
	ConfigurationElementModel configurationElement = cacheFactory.createConfigurationElement();
	// Use this flag to determine if the read-only flag should be set.  You
	// can't set it now or you won't be able to add anything more to this
	// configuration element.
	addToObjectTable(configurationElement);
	boolean setReadOnlyFlag = false;
	try {
		byte inByte = 0;
		boolean done = false;
		while (!done) {
			try {
				inByte = in.readByte();
			} catch (EOFException eofe) {
				done = true;
				break;
			}
			switch (inByte) {
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
						// Do we have an index or a real configuration element?
						switch (in.readByte()) {
							case CONFIGURATION_ELEMENT_LABEL :
								subElements[i] = readConfigurationElement(in);
								break;
							case CONFIGURATION_ELEMENT_INDEX_LABEL :
								subElements[i] = (ConfigurationElementModel) objectTable.get(in.readInt());
								break;
						}
					}
					configurationElement.setSubElements(subElements);
					subElements = null;
					break;
				case CONFIGURATION_ELEMENT_PARENT_LABEL :
					// We know the parent already exists, just grab it.
					configurationElement.setParent(objectTable.get(in.readInt()));
					break;
				case CONFIGURATION_ELEMENT_END_LABEL :
					done = true;
			}
		}
	} catch (IOException ioe) {
		cacheReadProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", decipherLabel(CONFIGURATION_ELEMENT_LABEL)), ioe));
		return null;
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
		byte inByte = 0;
		boolean done = false;
		while (!done) {
			try {
				inByte = in.readByte();
			} catch (EOFException eofe) {
				done = true;
				break;
			}
			switch (inByte) {
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
		cacheReadProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", decipherLabel(CONFIGURATION_PROPERTY_LABEL)), ioe));
		return null;
	}
	return configurationProperty;
}
public ExtensionModel readExtension(DataInputStream in) {
	ExtensionModel extension = cacheFactory.createExtension();
	addToObjectTable(extension);
	// Use this flag to determine if the read-only flag should be set.  You
	// can't set it now or you won't be able to add anything more to this
	// extension.
	boolean setReadOnlyFlag = false;
	try {
		byte inByte = 0;
		boolean done = false;
		while (!done) {
			try {
				inByte = in.readByte();
			} catch (EOFException eofe) {
				done = true;
				break;
			}
			switch (inByte) {
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
						// Do we have a configuration element or an index into
						// objectTable?
						switch (in.readByte()) {
							case CONFIGURATION_ELEMENT_LABEL :
								subElements[i] = readConfigurationElement(in);
								break;
							case CONFIGURATION_ELEMENT_INDEX_LABEL :
								subElements[i] = (ConfigurationElementModel) objectTable.get(in.readInt());
								break;
						}
					}
					extension.setSubElements(subElements);
					subElements = null;
					break;
				case EXTENSION_PARENT_LABEL :
					// Either there is a plugin or there is an index into the
					// objectTable
					switch (in.readByte()) {
						case PLUGIN_LABEL :
							extension.setParent((PluginModel)readPluginDescriptor(in));
							break;
						case PLUGIN_INDEX_LABEL :
							extension.setParent((PluginModel)objectTable.get(in.readInt()));
							break;
						case FRAGMENT_LABEL :
							extension.setParent((PluginModel)readPluginFragment(in));
							break;
						case FRAGMENT_INDEX_LABEL :
							extension.setParent((PluginModel)objectTable.get(in.readInt()));
							break;
					}
					break;
				case EXTENSION_END_LABEL :
					done = true;
			}
		}
	} catch (IOException ioe) {
		cacheReadProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", decipherLabel(PLUGIN_EXTENSION_LABEL)), ioe));
		return null;
	}
	return extension;
}
public ExtensionPointModel readExtensionPoint(DataInputStream in) {
	ExtensionPointModel extPoint = cacheFactory.createExtensionPoint();
	addToObjectTable(extPoint);

	// Use this flag to determine if the read-only flag should be set.  You
	// can't set it now or you won't be able to add anything more to this
	// extension point.
	boolean setReadOnlyFlag = false;
	int extensionLength = 0;
	try {
		byte inByte = 0;
		boolean done = false;
		while (!done) {
			try {
				inByte = in.readByte();
			} catch (EOFException eofe) {
				done = true;
				break;
			}
			switch (inByte) {
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
				case EXTENSION_POINT_EXTENSIONS_LENGTH_LABEL :
					extensionLength = in.readInt();
					break;
				case EXTENSION_POINT_EXTENSIONS_LABEL :
					ExtensionModel[] extensions = new ExtensionModel[extensionLength];
					for (int i = 0; i < extensionLength; i++) {
						switch (in.readByte()) {
							// Either this is an extension or an index into
							// the objectTable
							case PLUGIN_EXTENSION_LABEL :
								extensions[i] = readExtension(in);
								break;
							case EXTENSION_INDEX_LABEL :
								extensions[i] = (ExtensionModel) objectTable.get(in.readInt());
								break;
						}
					}
					extPoint.setDeclaredExtensions(extensions);
					break;
				case EXTENSION_POINT_PARENT_LABEL :
					// We know this plugin or fragment is already in the objectTable
					extPoint.setParent((PluginModel) objectTable.get(in.readInt()));
					break;
				case EXTENSION_POINT_END_LABEL :
					done = true;
			}
		}
	} catch (IOException ioe) {
		cacheReadProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", decipherLabel(PLUGIN_EXTENSION_POINT_LABEL)), ioe));
		return null;
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
		byte inByte = 0;
		boolean done = false;
		while (!done) {
			try {
				inByte = in.readByte();
			} catch (EOFException eofe) {
				done = true;
				break;
			}
			switch (inByte) {
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
				case TYPE_LABEL :
					library.setType(in.readUTF());
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
		cacheReadProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", decipherLabel(PLUGIN_LIBRARY_LABEL)), ioe));
		return null;
	}
	return library;
}
public PluginDescriptorModel readPluginDescriptor(DataInputStream in) {
	PluginDescriptorModel plugin = cacheFactory.createPluginDescriptor();
	addToObjectTable(plugin);
	// Use this flag to determine if the read-only flag should be set.  You
	// can't set it now or you won't be able to add anything more to this
	// plugin.
	boolean setReadOnlyFlag = false;
	try {
		byte inByte = 0;
		boolean done = false;
		while (!done) {
			try {
				inByte = in.readByte();
			} catch (EOFException eofe) {
				done = true;
				break;
			}
			switch (inByte) {
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
				case EXTENSION_INDEX_LABEL :
					extension = (ExtensionModel) objectTable.get(in.readInt());
					extList = plugin.getDeclaredExtensions();
					newExtValues = null;
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
				case FRAGMENT_LABEL :
					PluginFragmentModel fragment = readPluginFragment(in);
					// Add this fragment to the end of the fragment list
					PluginFragmentModel[] fragmentList = plugin.getFragments();
					PluginFragmentModel[] newFragmentValues = null;
					if (fragmentList == null) {
						newFragmentValues = new PluginFragmentModel[1];
						newFragmentValues[0] = fragment;
					} else {
						newFragmentValues = new PluginFragmentModel[fragmentList.length + 1];
						System.arraycopy(fragmentList, 0, newFragmentValues, 0, fragmentList.length);
						newFragmentValues[fragmentList.length] = fragment;
					}
					plugin.setFragments(newFragmentValues);
					fragment = null;
					fragmentList = newFragmentValues = null;
					break;
				case FRAGMENT_INDEX_LABEL :
					fragment = (PluginFragmentModel) objectTable.get(in.readInt());
					fragmentList = plugin.getFragments();
					newFragmentValues = null;
					if (fragmentList == null) {
						newFragmentValues = new PluginFragmentModel[1];
						newFragmentValues[0] = fragment;
					} else {
						newFragmentValues = new PluginFragmentModel[fragmentList.length + 1];
						System.arraycopy(fragmentList, 0, newFragmentValues, 0, fragmentList.length);
						newFragmentValues[fragmentList.length] = fragment;
					}
					plugin.setFragments(newFragmentValues);
					fragment = null;
					fragmentList = newFragmentValues = null;
					break;
				case PLUGIN_PARENT_LABEL :
					plugin.setRegistry((PluginRegistryModel) objectTable.get(in.readInt()));
					break;
				case PLUGIN_END_LABEL :
					done = true;
			}
		}
	} catch (IOException ioe) {
		cacheReadProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", decipherLabel(PLUGIN_LABEL)), ioe));
		return null;
	}
	return plugin;
}
public PluginFragmentModel readPluginFragment(DataInputStream in) {
	PluginFragmentModel fragment = cacheFactory.createPluginFragment();
	addToObjectTable(fragment);
	// Use this flag to determine if the read-only flag should be set.  You
	// can't set it now or you won't be able to add anything more to this
	// plugin.
	boolean setReadOnlyFlag = false;
	try {
		byte inByte = 0;
		boolean done = false;
		while (!done) {
			try {
				inByte = in.readByte();
			} catch (EOFException eofe) {
				done = true;
				break;
			}
			switch (inByte) {
				case READONLY_LABEL :
					if (in.readBoolean()) {
						setReadOnlyFlag = true;
					}
					break;
				case NAME_LABEL :
					fragment.setName(in.readUTF());
					break;
				case ID_LABEL :
					fragment.setId(in.readUTF());
					break;
				case PLUGIN_PROVIDER_NAME_LABEL :
					fragment.setProviderName(in.readUTF());
					break;
				case VERSION_LABEL :
					fragment.setVersion(in.readUTF());
					break;
				case PLUGIN_LOCATION_LABEL :
					fragment.setLocation(in.readUTF());
					break;
				case FRAGMENT_PLUGIN_LABEL :
					fragment.setPlugin(in.readUTF());
					break;
				case FRAGMENT_PLUGIN_VERSION_LABEL :
					fragment.setPluginVersion(in.readUTF());
					break;
				case PLUGIN_REQUIRES_LABEL :
					PluginPrerequisiteModel requires = readPluginPrerequisite(in);
					// Add this prerequisite to the end of the requires list
					PluginPrerequisiteModel[] requiresList = fragment.getRequires();
					PluginPrerequisiteModel[] newRequiresValues = null;
					if (requiresList == null) {
						newRequiresValues = new PluginPrerequisiteModel[1];
						newRequiresValues[0] = requires;
					} else {
						newRequiresValues = new PluginPrerequisiteModel[requiresList.length + 1];
						System.arraycopy(requiresList, 0, newRequiresValues, 0, requiresList.length);
						newRequiresValues[requiresList.length] = requires;
					}
					fragment.setRequires(newRequiresValues);
					requires = null;
					requiresList = newRequiresValues = null;
					break;
				case PLUGIN_LIBRARY_LABEL :
					LibraryModel library = readLibrary(in);
					// Add this library to the end of the runtime list
					LibraryModel[] libraryList = fragment.getRuntime();
					LibraryModel[] newLibraryValues = null;
					if (libraryList == null) {
						newLibraryValues = new LibraryModel[1];
						newLibraryValues[0] = library;
					} else {
						newLibraryValues = new LibraryModel[libraryList.length + 1];
						System.arraycopy(libraryList, 0, newLibraryValues, 0, libraryList.length);
						newLibraryValues[libraryList.length] = library;
					}
					fragment.setRuntime(newLibraryValues);
					library = null;
					libraryList = newLibraryValues = null;
					break;
				case PLUGIN_EXTENSION_LABEL :
					ExtensionModel extension = readExtension(in);
					ExtensionModel[] extList = fragment.getDeclaredExtensions();
					ExtensionModel[] newExtValues = null;
					if (extList == null) {
						newExtValues = new ExtensionModel[1];
						newExtValues[0] = extension;
					} else {
						newExtValues = new ExtensionModel[extList.length + 1];
						System.arraycopy(extList, 0, newExtValues, 0, extList.length);
						newExtValues[extList.length] = extension;
					}
					fragment.setDeclaredExtensions(newExtValues);
					extension = null;
					extList = newExtValues = null;
					break;
				case EXTENSION_INDEX_LABEL :
					extension = (ExtensionModel) objectTable.get(in.readInt());
					extList = fragment.getDeclaredExtensions();
					newExtValues = null;
					if (extList == null) {
						newExtValues = new ExtensionModel[1];
						newExtValues[0] = extension;
					} else {
						newExtValues = new ExtensionModel[extList.length + 1];
						System.arraycopy(extList, 0, newExtValues, 0, extList.length);
						newExtValues[extList.length] = extension;
					}
					fragment.setDeclaredExtensions(newExtValues);
					extension = null;
					extList = newExtValues = null;
					break;
				case PLUGIN_EXTENSION_POINT_LABEL :
					ExtensionPointModel extensionPoint = readExtensionPoint(in);
					// Add this extension point to the end of the extension point list
					ExtensionPointModel[] extPointList = fragment.getDeclaredExtensionPoints();
					ExtensionPointModel[] newExtPointValues = null;
					if (extPointList == null) {
						newExtPointValues = new ExtensionPointModel[1];
						newExtPointValues[0] = extensionPoint;
					} else {
						newExtPointValues = new ExtensionPointModel[extPointList.length + 1];
						System.arraycopy(extPointList, 0, newExtPointValues, 0, extPointList.length);
						newExtPointValues[extPointList.length] = extensionPoint;
					}
					fragment.setDeclaredExtensionPoints(newExtPointValues);
					extensionPoint = null;
					extPointList = newExtPointValues = null;
					break;
				case PLUGIN_PARENT_LABEL :
					fragment.setRegistry((PluginRegistryModel) objectTable.get(in.readInt()));
					break;
				case FRAGMENT_END_LABEL :
					done = true;
			}
		}
	} catch (IOException ioe) {
		cacheReadProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", decipherLabel(FRAGMENT_LABEL)), ioe));
		return null;
	}
	return fragment;
}
public PluginPrerequisiteModel readPluginPrerequisite(DataInputStream in) {
	PluginPrerequisiteModel requires = cacheFactory.createPluginPrerequisite();
	// Use this flag to determine if the read-only flag should be set.  You
	// can't set it now or you won't be able to add anything more to this
	// prerequisite.
	boolean setReadOnlyFlag = false;
	try {
		byte inByte = 0;
		boolean done = false;
		while (!done) {
			try {
				inByte = in.readByte();
			} catch (EOFException eofe) {
				done = true;
				break;
			}
			switch (inByte) {
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
				case REQUIRES_OPTIONAL_LABEL :
					requires.setOptional(in.readBoolean());
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
		cacheReadProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", decipherLabel(PLUGIN_REQUIRES_LABEL)), ioe));
		return null;
	}
	return requires;
}
public PluginRegistryModel readPluginRegistry(DataInputStream in) {
	if (cacheReadProblems == null) {
		cacheReadProblems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("meta.registryCacheReadProblems"), null);
	}

	if (!interpretHeaderInformation(in)) {
		return null;
	}
	PluginRegistryModel cachedRegistry = cacheFactory.createPluginRegistry();
	addToObjectTable(cachedRegistry);

	boolean setReadOnlyFlag = false;
	try {
		byte inByte = 0;
		boolean done = false;
		while (!done) {
			try {
				inByte = in.readByte();
			} catch (EOFException eofe) {
				done = true;
				break;
			}
			switch (inByte) {
				case READONLY_LABEL :
					if (in.readBoolean()) {
						setReadOnlyFlag = true;
					}
					break;
				case REGISTRY_RESOLVED_LABEL :
					if (in.readBoolean()) {
						cachedRegistry.markResolved();
					}
					break;
				case PLUGIN_LABEL :
					PluginDescriptorModel plugin = null;
					if ((plugin = readPluginDescriptor(in)) != null) {
						cachedRegistry.addPlugin(plugin);
					}
					break;
				case PLUGIN_INDEX_LABEL :
					plugin = (PluginDescriptorModel) objectTable.get(in.readInt());
					cachedRegistry.addPlugin(plugin);
					break;
				case FRAGMENT_LABEL :
					PluginFragmentModel fragment = null;
					if ((fragment = readPluginFragment(in)) != null) {
						cachedRegistry.addFragment(fragment);
					}
					break;
				case FRAGMENT_INDEX_LABEL :
					fragment = (PluginFragmentModel) objectTable.get(in.readInt());
					cachedRegistry.addFragment(fragment);
					break;
				case REGISTRY_END_LABEL :
					done = true;
			}
		}
	} catch (IOException ioe) {
		cacheReadProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", decipherLabel(REGISTRY_LABEL)), ioe));
		return null;
	}
	if (setReadOnlyFlag) {
		// If we are finished reading this registry, we don't need to worry
		// about setting the read-only flag on other objects we might wish
		// to write to.  So, just to be safe, mark the whole thing.
		cachedRegistry.markReadOnly();
	}
	// if there are no plugins in the registry, return null instead of
	// an empty registry?
	PluginDescriptorModel[] pluginList = cachedRegistry.getPlugins();
	if ((pluginList == null) || (pluginList.length == 0)) {
		return null;
	} else {
		return cachedRegistry;
	}
}
}
