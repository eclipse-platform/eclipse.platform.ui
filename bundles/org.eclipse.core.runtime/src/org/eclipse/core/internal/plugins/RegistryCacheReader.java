/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

package org.eclipse.core.internal.plugins;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;
import org.eclipse.core.internal.plugins.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.boot.BootLoader;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RegistryCacheReader {

	Factory cacheFactory;
	// objectTable will be an array list of objects.  The objects will be things 
	// like a plugin descriptor, extension, extension point, etc.  The integer 
	// index value will be used in the cache to allow cross-references in the 
	// cached registry.
	ArrayList objectTable = null;

	public MultiStatus cacheReadProblems = null;

	public static final byte REGISTRY_CACHE_VERSION = 6;

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
	public static final byte FRAGMENT_PLUGIN_MATCH_LABEL = 55;
	
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
	
	// So it's easier to add a new label ...
	public static final byte LARGEST_LABEL = 55;
	
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
private void debug(String msg) {
	System.out.println("RegistryCacheReader: " + msg); //$NON-NLS-1$
}
public static String decipherLabel(byte labelValue) {
	// This method should use the constants in IModel
	// e.g. return "<" + IModel.REGISTRY + ">"
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
		case FRAGMENT_END_LABEL:
			return "<fragmentEnd>";
		case FRAGMENT_INDEX_LABEL:
			return "<fragmentIndex>";
		case FRAGMENT_LABEL:
			return "<fragment>";
		case FRAGMENT_PLUGIN_LABEL:
			return "<fragmentPlugin>";
		case FRAGMENT_PLUGIN_MATCH_LABEL:
			return "<fragmentPluginMatch>";
		case FRAGMENT_PLUGIN_VERSION_LABEL:
			return "<fragmentPluginVersion>";
		case REQUIRES_OPTIONAL_LABEL:
			return "<requiresOptional>";
		case SOURCE_LABEL:
			return "<source>";
		case TYPE_LABEL:
			return "<type>";
	}

	return "<unknown label>";
}
public boolean interpretHeaderInformation(DataInputStream in) {
	try {
		if (in.readInt() != REGISTRY_CACHE_VERSION)
			return false;
			
		// install stamp
		long installStamp = in.readLong();
		// OS stamp
		String osStamp = in.readUTF();
		// windows system stamp
		String windowsStamp = in.readUTF();
		// locale stamp
		String localeStamp = in.readUTF();
		// Save the current plugin timestamp for writing to the
		// cache when we shutdown
		InternalPlatform.setRegistryCacheTimeStamp(BootLoader.getCurrentPlatformConfiguration().getPluginsChangeStamp());

		return ((installStamp == InternalPlatform.getRegistryCacheTimeStamp()) &&
			(osStamp.equals(BootLoader.getOS())) &&
			(windowsStamp.equals(BootLoader.getWS())) &&
			(localeStamp.equals(BootLoader.getNL())) );
	} catch (IOException ioe) {
		cacheReadProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", "HeaderInformation"), ioe)); //$NON-NLS-1$
		return false;
	}
}
public ConfigurationElementModel readConfigurationElement(DataInputStream in, boolean debugFlag) {
	ConfigurationElementModel configurationElement = cacheFactory.createConfigurationElement();
	// Use this flag to determine if the read-only flag should be set.  You
	// can't set it now or you won't be able to add anything more to this
	// configuration element.
	addToObjectTable(configurationElement);
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
					in.readBoolean();
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
					for (int i = 0; i < propertiesLength && !done; i++) {
						properties[i] = readConfigurationProperty(in, debugFlag);
						if (properties[i] == null) {
							// Something went wrong reading this configuration
							// property
							if (debugFlag) {
								String name = configurationElement.getName();
								if (name == null)
									name = new String ("<unknown name>"); //$NON-NLS-1$
								debug ("Trouble reading configuration property #" + i + " for configuration element " + name); //$NON-NLS-1$ //$NON-NLS-2$
							}
							configurationElement = null;
							done = true;
						}
					}
					if (configurationElement != null)
						configurationElement.setProperties(properties);
					properties = null;
					break;
				case SUBELEMENTS_LENGTH_LABEL :
					int subElementsLength = in.readInt();
					ConfigurationElementModel[] subElements = new ConfigurationElementModel[subElementsLength];
					for (int i = 0; i < subElementsLength && !done; i++) {
						// Do we have an index or a real configuration element?
						byte subInByte = in.readByte();
						switch (subInByte) {
							case CONFIGURATION_ELEMENT_LABEL :
								subElements[i] = readConfigurationElement(in, debugFlag);
								if (subElements[i] == null) {
									if (debugFlag) {
										String name = configurationElement.getName();
										if (name == null)
											name = new String ("<unknown name>"); //$NON-NLS-1$
										debug ("Unable to read subelement #" + i + " for configuration element " + name); //$NON-NLS-1$ //$NON-NLS-2$
									}
									configurationElement = null;
									done = true;
								}
								break;
							case CONFIGURATION_ELEMENT_INDEX_LABEL :
								subElements[i] = (ConfigurationElementModel) objectTable.get(in.readInt());
								break;
							default:
								// We found something we weren't expecting
								if (debugFlag) {
									String name = configurationElement.getName();
									if (name == null)
										name = new String ("<unknown name>"); //$NON-NLS-1$
									debug ("Unexpected byte code " + decipherLabel(subInByte) + "reading subelements of configuration element" + name); //$NON-NLS-1$ //$NON-NLS-2$
								}
								done = true;
								configurationElement = null;
								break;
						}
					}
					if (configurationElement != null)
						configurationElement.setSubElements(subElements);
					subElements = null;
					break;
				case CONFIGURATION_ELEMENT_PARENT_LABEL :
					// We know the parent already exists, just grab it.
					configurationElement.setParent(objectTable.get(in.readInt()));
					break;
				case CONFIGURATION_ELEMENT_END_LABEL :
					done = true;
					break;
				default:
					// We got something unexpected
					if (debugFlag) {
						String name = configurationElement.getName();
						if (name == null)
							name = new String ("<unknown name>"); //$NON-NLS-1$
						debug ("Unexpected byte code " + decipherLabel(inByte) + "reading configuration element" + name); //$NON-NLS-1$ //$NON-NLS-2$
					}
					done = true;
					configurationElement = null;
					break;
			}
		}
	} catch (IOException ioe) {
		cacheReadProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", decipherLabel(CONFIGURATION_ELEMENT_LABEL)), ioe)); //$NON-NLS-1$
		return null;
	}
	return configurationElement;
}
public ConfigurationPropertyModel readConfigurationProperty(DataInputStream in, boolean debugFlag) {
	ConfigurationPropertyModel configurationProperty = cacheFactory.createConfigurationProperty();
	// Use this flag to determine if the read-only flag should be set.  You
	// can't set it now or you won't be able to add anything more to this
	// configuration property.
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
				case CONFIGURATION_PROPERTY_LABEL:
					break;
				case READONLY_LABEL :
					in.readBoolean();
					break;
				case NAME_LABEL :
					configurationProperty.setName(in.readUTF());
					break;
				case VALUE_LABEL :
					configurationProperty.setValue(in.readUTF());
					break;
				case CONFIGURATION_PROPERTY_END_LABEL :
					done = true;
					break;
				default:
					// We got something unexpected
					if (debugFlag) {
						String name = configurationProperty.getName();
						if (name == null)
							name = new String ("<unknown name>"); //$NON-NLS-1$
						debug ("Unexpected byte code " + decipherLabel(inByte) + " reading configuration property " + name); //$NON-NLS-1$ //$NON-NLS-2$
					}
					configurationProperty = null;
					done = true;
					break;
			}
		}
	} catch (IOException ioe) {
		cacheReadProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", decipherLabel(CONFIGURATION_PROPERTY_LABEL)), ioe)); //$NON-NLS-1$
		return null;
	}
	return configurationProperty;
}
public ExtensionModel readExtension(DataInputStream in, boolean debugFlag) {
	ExtensionModel extension = cacheFactory.createExtension();
	addToObjectTable(extension);
	// Use this flag to determine if the read-only flag should be set.  You
	// can't set it now or you won't be able to add anything more to this
	// extension.
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
					in.readBoolean();
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
					for (int i = 0; i < subElementsLength && !done; i++) {
						// Do we have a configuration element or an index into
						// objectTable?
						byte subInByte = in.readByte();
						switch (subInByte) {
							case CONFIGURATION_ELEMENT_LABEL :
								subElements[i] = readConfigurationElement(in, debugFlag);
								if (subElements[i] == null) {
									if (debugFlag) {
										String name = extension.getName();
										if (name == null)
											name = new String("<unknown name>"); //$NON-NLS-1$
										debug ("Unable to read subelement #" + i + " for extension " + name); //$NON-NLS-1$ //$NON-NLS-2$
									}
									extension = null;
									done = true;
								}
								break;
							case CONFIGURATION_ELEMENT_INDEX_LABEL :
								subElements[i] = (ConfigurationElementModel) objectTable.get(in.readInt());
								break;
							default:
								// We got something unexpected
								if (debugFlag) {
									String name = extension.getName();
									if (name == null)
										name = new String("<unknown name>"); //$NON-NLS-1$
									debug ("Unexpected byte code " + decipherLabel(subInByte) + " reading subelements for extension " + name); //$NON-NLS-1$ //$NON-NLS-2$
								}
								extension = null;
								done = true;
								break;
						}
					}
					if (extension != null)
						extension.setSubElements(subElements);
					subElements = null;
					break;
				case EXTENSION_PARENT_LABEL :
					// Either there is a plugin or there is an index into the
					// objectTable
					byte subByte = in.readByte();
					switch (subByte) {
						case PLUGIN_LABEL :
							PluginModel parent = (PluginModel)readPluginDescriptor(in, debugFlag);
							if (parent == null) {
								if (debugFlag) {
									String name = extension.getName();
									if (name == null)
										name = new String("<unknown name>"); //$NON-NLS-1$
									debug ("Trouble reading parent plugin for extension " + name); //$NON-NLS-1$
								}
								done = true;
								extension = null;
							} else {
								extension.setParent(parent);
							}
							break;
						case PLUGIN_INDEX_LABEL :
							extension.setParent((PluginModel)objectTable.get(in.readInt()));
							break;
						case FRAGMENT_LABEL :
							PluginModel fragmentParent = (PluginModel)readPluginFragment(in, debugFlag);
							if (fragmentParent == null) {
								if (debugFlag) {
									String name = extension.getName();
									if (name == null)
										name = new String("<unknown name>"); //$NON-NLS-1$
									debug ("Trouble reading parent fragment for extension " + name); //$NON-NLS-1$
								}
								done = true;
								extension = null;
							} else {
								extension.setParent(fragmentParent);
							}
							break;
						case FRAGMENT_INDEX_LABEL :
							extension.setParent((PluginModel)objectTable.get(in.readInt()));
							break;
						default: 
							// We got something unexpected
							if (debugFlag) {
								String name = extension.getName();
								if (name == null)
									name = new String("<unknown name>"); //$NON-NLS-1$
								debug ("Unexpected byte code " + decipherLabel(subByte) + "reading parent of extension " + name); //$NON-NLS-1$ //$NON-NLS-2$
							}
							done = true;
							extension = null;
							break;
					}
					break;
				case EXTENSION_END_LABEL :
					done = true;
					break;
				default:
					// We got something unexpected
					if (debugFlag) {
						String name = extension.getName();
						if (name == null)
							name = new String ("<unknown name>"); //$NON-NLS-1$
						debug ("Unexpected byte code " + decipherLabel(inByte) + "reading extension" + name); //$NON-NLS-1$ //$NON-NLS-2$
					}
					done = true;
					extension = null;
					break;
			}
		}
	} catch (IOException ioe) {
		cacheReadProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", decipherLabel(PLUGIN_EXTENSION_LABEL)), ioe)); //$NON-NLS-1$
		return null;
	}
	return extension;
}
public ExtensionPointModel readExtensionPoint(DataInputStream in, boolean debugFlag) {
	ExtensionPointModel extPoint = cacheFactory.createExtensionPoint();
	addToObjectTable(extPoint);

	// Use this flag to determine if the read-only flag should be set.  You
	// can't set it now or you won't be able to add anything more to this
	// extension point.
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
					in.readBoolean();
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
					for (int i = 0; i < extensionLength && !done; i++) {
						byte subByte = in.readByte();
						switch (subByte) {
							// Either this is an extension or an index into
							// the objectTable
							case PLUGIN_EXTENSION_LABEL :
								extensions[i] = readExtension(in, debugFlag);
								if (extensions[i] == null) {
									if (debugFlag) {
										String name = extPoint.getName();
										if (name == null)
											name = new String ("<unknown name>"); //$NON-NLS-1$
										debug ("Unable to read extension #" + i + " for extension point " + name); //$NON-NLS-1$ //$NON-NLS-2$
									}
									done = true;
									extPoint = null;
								}
								break;
							case EXTENSION_INDEX_LABEL :
								extensions[i] = (ExtensionModel) objectTable.get(in.readInt());
								break;
							default:
								// We got something unexpected
								if (debugFlag) {
									String name = extPoint.getName();
									if (name == null)
										name = new String ("<unknown name>"); //$NON-NLS-1$
									debug ("Unexpected byte code " + decipherLabel(subByte) + "reading extension #" + i + " for extension point " + name); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								}
								extPoint = null;
								done = true;
								break;
						}
					}
					if (extPoint != null)
						extPoint.setDeclaredExtensions(extensions);
					break;
				case EXTENSION_POINT_PARENT_LABEL :
					// We know this plugin or fragment is already in the objectTable
					extPoint.setParent((PluginModel) objectTable.get(in.readInt()));
					break;
				case EXTENSION_POINT_END_LABEL :
					done = true;
					break;
				default:
					// We got something unexpected
					if (debugFlag) {
						String name = extPoint.getName();
						if (name == null)
							name = new String ("<unknown name>"); //$NON-NLS-1$
						debug ("Unexpected byte code " + decipherLabel(inByte) + " reading extension point " + name); //$NON-NLS-1$ //$NON-NLS-2$
					}
					extPoint = null;
					done = true;
					break;
			}
		}
	} catch (IOException ioe) {
		cacheReadProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", decipherLabel(PLUGIN_EXTENSION_POINT_LABEL)), ioe)); //$NON-NLS-1$
		return null;
	}
	return extPoint;
}
public LibraryModel readLibrary(DataInputStream in, boolean debugFlag) {
	LibraryModel library = cacheFactory.createLibrary();
	// Use this flag to determine if the read-only flag should be set.  You
	// can't set it now or you won't be able to add anything more to this
	// library.
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
					in.readBoolean();
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
					for (int i = 0; i < exportsLength && !done; i++) {
						exports[i] = in.readUTF();
						if (exports[i] == null) {
							if (debugFlag) {
								String name = library.getName();
								if (name == null)
									name = new String ("<unknown name>"); //$NON-NLS-1$
								debug ("Empty export string for export #" + i + " reading library " + name); //$NON-NLS-1$ //$NON-NLS-2$
							}
							done = true;
							library = null;
						}
					}
					if (!done)
						library.setExports(exports);
					exports = null;
					break;
				case LIBRARY_END_LABEL :
					done = true;
					break;
				default:
					// We got something unexpected
					if (debugFlag) {
						String name = library.getName();
						if (name == null)
							name = new String ("<unknown name>"); //$NON-NLS-1$
						debug ("Unexpected byte code " + decipherLabel(inByte) + " reading library " + name); //$NON-NLS-1$ //$NON-NLS-2$
					}
					library = null;
					done = true;
					break;
			}
		}
	} catch (IOException ioe) {
		cacheReadProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", decipherLabel(PLUGIN_LIBRARY_LABEL)), ioe)); //$NON-NLS-1$
		return null;
	}
	return library;
}
public PluginDescriptorModel readPluginDescriptor(DataInputStream in, boolean debugFlag) {
	PluginDescriptorModel plugin = cacheFactory.createPluginDescriptor();
	addToObjectTable(plugin);
	// Use this flag to determine if the read-only flag should be set.  You
	// can't set it now or you won't be able to add anything more to this
	// plugin.
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
					in.readBoolean();
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
					PluginPrerequisiteModel requires = readPluginPrerequisite(in, debugFlag);
					if (requires == null) {
						// Something went wrong
						if (debugFlag) {
							String name = plugin.getName();
							if (name == null)
								name = new String ("<unknown name>"); //$NON-NLS-1$
							debug ("Unable to read prerequisite for plugin " + name); //$NON-NLS-1$
						}
						plugin = null;
						done = true;
					} else {
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
						requiresList = newRequiresValues = null;
					}
					requires = null;
					break;
				case PLUGIN_LIBRARY_LABEL :
					LibraryModel library = readLibrary(in, debugFlag);
					if (library == null) {
						// Something went wrong reading this library
						if (debugFlag) {
							String name = plugin.getName();
							if (name == null)
								name = new String ("<unknown name>"); //$NON-NLS-1$
							debug ("Unable to read library for plugin " + name); //$NON-NLS-1$
						}
						plugin = null;
						done = true;
					} else {
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
						libraryList = newLibraryValues = null;
					}
					library = null;
					break;
				case PLUGIN_EXTENSION_LABEL :
					ExtensionModel extension = readExtension(in, debugFlag);
					if (extension == null) {
						// Something went wrong reading this extension
						if (debugFlag) {
							String name = plugin.getName();
							if (name == null)
								name = new String ("<unknown name>"); //$NON-NLS-1$
							debug ("Unable to read extension for plugin " + name); //$NON-NLS-1$
						}
						plugin = null;
						done = true;
					} else {
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
						extList = newExtValues = null;
					}
					extension = null;
					break;
				case EXTENSION_INDEX_LABEL :
					extension = (ExtensionModel) objectTable.get(in.readInt());
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
					ExtensionPointModel extensionPoint = readExtensionPoint(in, debugFlag);
					if (extensionPoint == null) {
						// Something went wrong reading this extension
						if (debugFlag) {
							String name = plugin.getName();
							if (name == null)
								name = new String ("<unknown name>"); //$NON-NLS-1$
							debug ("Unable to read extension point for plugin " + name); //$NON-NLS-1$
						}
						plugin = null;
						done = true;
					} else {
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
					}
					break;
				case FRAGMENT_LABEL :
					PluginFragmentModel fragment = readPluginFragment(in, debugFlag);
					if (fragment == null) {
						// Something went wrong reading this fragment
						if (debugFlag) {
							String name = plugin.getName();
							if (name == null)
								name = new String ("<unknown name>"); //$NON-NLS-1$
							debug ("Unable to read fragment for plugin " + name); //$NON-NLS-1$
						}
						plugin = null;
						done = true;
					} else {
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
					}
					break;
				case FRAGMENT_INDEX_LABEL :
					fragment = (PluginFragmentModel) objectTable.get(in.readInt());
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
				case PLUGIN_PARENT_LABEL :
					plugin.setRegistry((PluginRegistryModel) objectTable.get(in.readInt()));
					break;
				case PLUGIN_END_LABEL :
					done = true;
					break;
				default :
					// We got something unexpected
					if (debugFlag) {
						String name = plugin.getName();
						if (name == null)
							name = new String ("<unknown name>"); //$NON-NLS-1$
						debug ("Unexpected byte code " + decipherLabel(inByte) + " reading plugin " + name); //$NON-NLS-1$ //$NON-NLS-2$
					}
					plugin = null;
					done = true;
					break;
			}
		}
	} catch (IOException ioe) {
		cacheReadProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", decipherLabel(PLUGIN_LABEL)), ioe)); //$NON-NLS-1$
		return null;
	}
	return plugin;
}
public PluginFragmentModel readPluginFragment(DataInputStream in, boolean debugFlag) {
	PluginFragmentModel fragment = cacheFactory.createPluginFragment();
	addToObjectTable(fragment);
	// Use this flag to determine if the read-only flag should be set.  You
	// can't set it now or you won't be able to add anything more to this
	// plugin.
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
					in.readBoolean();
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
				case FRAGMENT_PLUGIN_MATCH_LABEL :
					fragment.setMatch(in.readByte());
					break;
				case PLUGIN_REQUIRES_LABEL :
					PluginPrerequisiteModel requires = readPluginPrerequisite(in, debugFlag);
					if (requires == null) {
						// Something went wrong reading the prerequisite
						if (debugFlag) {
							String name = fragment.getName();
							if (name == null)
								name = new String ("<unknown name>"); //$NON-NLS-1$
							debug ("Unable to read prerequisite for fragment " + name); //$NON-NLS-1$
						}
						done = true;
						fragment = null;
					} else {
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
					}
					break;
				case PLUGIN_LIBRARY_LABEL :
					LibraryModel library = readLibrary(in, debugFlag);
					if (library == null) {
						// Something went wrong reading this library
						if (debugFlag) {
							String name = fragment.getName();
							if (name == null)
								name = new String ("<unknown name>"); //$NON-NLS-1$
							debug ("Unable to read library for fragment " + name); //$NON-NLS-1$
						}
						fragment = null;
						done = true;
					} else {
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
					}
					break;
				case PLUGIN_EXTENSION_LABEL :
					ExtensionModel extension = readExtension(in, debugFlag);
					if (extension == null) {
						// Something went wrong reading this extension
						if (debugFlag) {
							String name = fragment.getName();
							if (name == null)
								name = new String ("<unknown name>"); //$NON-NLS-1$
							debug ("Unable to read extension for fragment " + name); //$NON-NLS-1$
						}
						fragment = null;
						done = true;
					} else {
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
					}
					break;
				case EXTENSION_INDEX_LABEL :
					extension = (ExtensionModel) objectTable.get(in.readInt());
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
				case PLUGIN_EXTENSION_POINT_LABEL :
					ExtensionPointModel extensionPoint = readExtensionPoint(in, debugFlag);
					if (extensionPoint == null) {
						// Something went wrong reading this extension point
						if (debugFlag) {
							String name = fragment.getName();
							if (name == null)
								name = new String ("<unknown name>"); //$NON-NLS-1$
							debug ("Unable to read extension point for fragment " + name); //$NON-NLS-1$
						}
						fragment = null;
						done = true;
					} else {
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
					}
					break;
				case PLUGIN_PARENT_LABEL :
					fragment.setRegistry((PluginRegistryModel) objectTable.get(in.readInt()));
					break;
				case FRAGMENT_END_LABEL :
					done = true;
					break;
				default:
					// We got something unexpected
					if (debugFlag) {
						String name = fragment.getName();
						if (name == null)
							name = new String ("<unknown name>"); //$NON-NLS-1$
						debug ("Unexpected byte code " + decipherLabel(inByte) + " reading fragment " + name); //$NON-NLS-1$
					}
					fragment = null;
					done = true;
					break;
			}
		}
	} catch (IOException ioe) {
		cacheReadProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", decipherLabel(FRAGMENT_LABEL)), ioe)); //$NON-NLS-1$
		return null;
	}
	return fragment;
}
public PluginPrerequisiteModel readPluginPrerequisite(DataInputStream in, boolean debugFlag) {
	PluginPrerequisiteModel requires = cacheFactory.createPluginPrerequisite();
	// Use this flag to determine if the read-only flag should be set.  You
	// can't set it now or you won't be able to add anything more to this
	// prerequisite.
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
					in.readBoolean();
					break;
				case NAME_LABEL :
					requires.setName(in.readUTF());
					break;
				case VERSION_LABEL :
					requires.setVersion(in.readUTF());
					break;
				case REQUIRES_MATCH_LABEL :
					requires.setMatchByte(in.readByte());
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
					break;
				default:
					// We got something we didn't expect
					// Make this an empty prerequisite
					if (debugFlag) {
						String name = requires.getName();
						if (name == null)
							name = new String ("<unknown name>"); //$NON-NLS-1$
						debug ("Unexpected byte code " + decipherLabel(inByte) + " reading prerequisite " + name); //$NON-NLS-1$ //$NON-NLS-2$
					}
					done = true;
					requires = null;
					break;
			}
		}
	} catch (IOException ioe) {
		cacheReadProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", decipherLabel(PLUGIN_REQUIRES_LABEL)), ioe)); //$NON-NLS-1$
		return null;
	}
	return requires;
}
public PluginRegistryModel readPluginRegistry(DataInputStream in, URL[] pluginPath, boolean debugFlag) {
	if (cacheReadProblems == null) {
		cacheReadProblems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("meta.registryCacheReadProblems"), null); //$NON-NLS-1$
	}

	if (!interpretHeaderInformation(in)) {
		if (debugFlag)
			debug ("Cache header information out of date - ignoring cache"); //$NON-NLS-1$
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
				case REGISTRY_LABEL:
					break;
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
					if ((plugin = readPluginDescriptor(in,debugFlag)) != null) {
						cachedRegistry.addPlugin(plugin);
					} else {
						// Something went wrong reading this plugin
						// Invalidate the cache
						if (debugFlag) {
							debug ("Unable to read plugin descriptor for plugin registry"); //$NON-NLS-1$
						}
						done = true;
						cachedRegistry = null;
					}
					break;
				case PLUGIN_INDEX_LABEL :
					plugin = (PluginDescriptorModel) objectTable.get(in.readInt());
					cachedRegistry.addPlugin(plugin);
					break;
				case FRAGMENT_LABEL :
					PluginFragmentModel fragment = null;
					if ((fragment = readPluginFragment(in, debugFlag)) != null) {
						cachedRegistry.addFragment(fragment);
					} else {
						// Something went wrong reading this fragment
						// Invalidate the cache
						if (debugFlag) {
							debug ("Unable to read fragment descriptor for plugin registry"); //$NON-NLS-1$
						}
						done = true;
						cachedRegistry = null;
					}
					break;
				case FRAGMENT_INDEX_LABEL :
					fragment = (PluginFragmentModel) objectTable.get(in.readInt());
					cachedRegistry.addFragment(fragment);
					break;
				case REGISTRY_END_LABEL :
					done = true;
					break;
				default:
					// We got something we weren't expecting
					// Invalidate this cached registry
					if (debugFlag) {
						debug ("Unexpected byte code " + decipherLabel(inByte) + " reading plugin registry"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					done = true;
					cachedRegistry = null;
					break;
			}
		}
	} catch (IOException ioe) {
		cacheReadProblems.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind ("meta.regCacheIOException", decipherLabel(REGISTRY_LABEL)), ioe)); //$NON-NLS-1$
		return null;
	}
	if (cachedRegistry == null)
		return null;
		
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
private String[] getPathMembers(URL path) {
	String[] list = null;
	String protocol = path.getProtocol();
	if (protocol.equals("file")) { //$NON-NLS-1$
		list = (new File(path.getFile())).list();
	} else {
		// XXX: attempt to read URL and see if we got html dir page
	}
	return list == null ? new String[0] : list;
}
}
