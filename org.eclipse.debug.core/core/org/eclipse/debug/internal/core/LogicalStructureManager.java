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
package org.eclipse.debug.internal.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILogicalStructureType;
import org.eclipse.debug.core.model.IValue;

/**
 * Manages logical structure extensions
 * 
 * @since 3.0
 */
public class LogicalStructureManager {

	private static LogicalStructureManager fgDefault;
	private List fTypes = null;
	private List fTypeProviders;
	
	public static LogicalStructureManager getDefault() {
		if (fgDefault == null) {
			fgDefault = new LogicalStructureManager();
		}
		return fgDefault;
	}
	
	public ILogicalStructureType[] getLogicalStructureTypes(IValue value) {
		initialize();
		// looks in the logical structure types
		Iterator iterator = fTypes.iterator();
		List select = new ArrayList();
		while (iterator.hasNext()) {
			ILogicalStructureType type = (ILogicalStructureType)iterator.next();
			if (type.providesLogicalStructure(value)) {
				select.add(type);
			}
		}
		// asks the logical structure providers
		for (Iterator iter= fTypeProviders.iterator(); iter.hasNext();) {
			ILogicalStructureType[] logicalStructures= ((LogicalStructureProvider) iter.next()).getLogicalStructures(value);
			for (int i= 0; i < logicalStructures.length; i++) {
				select.add(logicalStructures[i]);
			}
		}
		return (ILogicalStructureType[]) select.toArray(new ILogicalStructureType[select.size()]);
	}
	
	private void initialize() {
		if (fTypes == null) {
			//get the logical structure types from the extension points
			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(DebugPlugin.getUniqueIdentifier(), DebugPlugin.EXTENSION_POINT_LOGICAL_STRUCTURE_TYPES);
			IConfigurationElement[] extensions = point.getConfigurationElements();
			fTypes = new ArrayList(extensions.length);
			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement extension = extensions[i];
				LogicalStructureType type;
				try {
					type = new LogicalStructureType(extension);
					fTypes.add(type);
				} catch (CoreException e) {
					DebugPlugin.log(e);
				}
			}
			// get the logical structure providers from the extension point
			point= Platform.getExtensionRegistry().getExtensionPoint(DebugPlugin.getUniqueIdentifier(), DebugPlugin.EXTENSION_POINT_LOGICAL_STRUCTURE_PROVIDERS);
			extensions= point.getConfigurationElements();
			fTypeProviders= new ArrayList(extensions.length);
			for (int i= 0; i < extensions.length; i++) {
				try {
					fTypeProviders.add(new LogicalStructureProvider(extensions[i]));
				} catch (CoreException e) {
					DebugPlugin.log(e);
				}
			}
		}
	}
}
