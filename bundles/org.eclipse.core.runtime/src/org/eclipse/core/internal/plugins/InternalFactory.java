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
import org.eclipse.core.internal.runtime.InternalPlatform;

public class InternalFactory extends Factory {
public InternalFactory(MultiStatus status) {
	super(status);
}
public ConfigurationElementModel createConfigurationElement() {
	return new ConfigurationElement();
}
public ConfigurationPropertyModel createConfigurationProperty() {
	return new ConfigurationProperty();
}
public ExtensionModel createExtension() {
	return new Extension();
}
public ExtensionPointModel createExtensionPoint() {
	return new ExtensionPoint();
}



public LibraryModel createLibrary() {
	return new Library();
}
public PluginDescriptorModel createPluginDescriptor() {
	return new PluginDescriptor();
}

public PluginFragmentModel createPluginFragment() {
	return new FragmentDescriptor();
}

public PluginPrerequisiteModel createPluginPrerequisite() {
	return new PluginPrerequisite();
}
public PluginRegistryModel createPluginRegistry() {
	return new PluginRegistry();
}
}
