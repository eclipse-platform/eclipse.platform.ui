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
package org.eclipse.debug.internal.core.variables;


import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.variables.IContextLaunchVariable;
import org.eclipse.debug.core.variables.IContextLaunchVariableRegistry;

/**
 * Registry for context launch variables.
 * @since 3.0
 */
public class ContextLaunchVariableRegistry implements IContextLaunchVariableRegistry {
	// Format of the variable extension points
	// <extension point="org.eclipse.debug.ui.launchConfigurationVariables>
	//		<variable
	//			tag={string}
	//			description={string}
	//			componentClass={string:IVariableComponent}
	//			expanderClass={string:IVariableExpander}>
	//		</variable>
	// </extension>
	//
	
	/**
	 * Element and attribute tags of a variable extension.
	 */
	protected static final String TAG_VARIABLE = "variable"; //$NON-NLS-1$
	protected static final String TAG_NAME = "name"; //$NON-NLS-1$
	protected static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	protected static final String TAG_EXPANDER_CLASS = "expanderClass"; //$NON-NLS-1$

	/**
	 * Sorted map of variables where the key is the variable tag
	 * and the value is the corresponding variable.
	 */
	private SortedMap variables;
	
	/**
	 * The extension point id to read the variables from
	 */
	protected String extensionPointId;
	
	public ContextLaunchVariableRegistry() {
		this(DebugPlugin.EXTENSION_POINT_CONTEXT_LAUNCH_VARIABLES);
	}
	
	/**
	 * Creates a new registry and loads the variables.
	 */
	protected ContextLaunchVariableRegistry(String extensionPointId) {
		this.extensionPointId = extensionPointId;
		loadVariables();
	}

	/**
	 * Returns the variable for the specified tag, or
	 * <code>null</code> if none found.
	 */
	private final IContextLaunchVariable findVariable(String tag) {
		return (IContextLaunchVariable) variables.get(tag);
	}

	/**
	 * Returns the number of variables in the registry.
	 */
	private final int getVariableCount() {
		return variables.size();
	}
	
	/**
	 * @see IContextLaunchVariableRegistry#getVariable(String)
	 */
	public IContextLaunchVariable getVariable(String tag) {
		return findVariable(tag);
	}
	
	/**
	 * @see IContextLaunchVariableRegistry#getVariables()
	 */
	public IContextLaunchVariable[] getVariables() {
		IContextLaunchVariable[] results = new ContextLaunchVariable[getVariableCount()];
		variables.values().toArray(results);
		return results;
	}
	
	/**
	 * Load the available variables
	 */
	private void loadVariables() {
		variables = new TreeMap();
		IPluginRegistry registry = Platform.getPluginRegistry();
		IExtensionPoint point = registry.getExtensionPoint(DebugPlugin.getUniqueIdentifier(), extensionPointId);
		if (point != null) {
			IExtension[] extensions = point.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement[] elements = extensions[i].getConfigurationElements();
				for (int j = 0; j < elements.length; j++) {
					IConfigurationElement element = elements[j];
					if (element.getName().equals(TAG_VARIABLE)) {
						String tag = element.getAttribute(TAG_NAME);
						String description = element.getAttribute(TAG_DESCRIPTION);
						String className = element.getAttribute(TAG_EXPANDER_CLASS);
						
						boolean valid = true;
						if (tag == null || tag.length() == 0) {
							valid = false;
							DebugPlugin.log(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), IStatus.ERROR, "Missing tag attribute value for variable element.", null)); //$NON-NLS-1$
						}
						if (description == null || description.length() == 0) {
							valid = false;
							DebugPlugin.log(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), IStatus.ERROR, "Missing description attribute value for variable element.", null)); //$NON-NLS-1$
						}
						if (className == null || className.length() == 0) {
							valid = false;
							DebugPlugin.log(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), IStatus.ERROR, "Missing expander class attribute value for variable element.", null)); //$NON-NLS-1$
						}

						if (valid)
							variables.put(tag, newVariable(tag, description, element));
					}
				}
			}
		}
	}
	
	/**
	 * Creates a new variable from the specified information.
	 */
	private IContextLaunchVariable newVariable(String tag, String description, IConfigurationElement element) {
		return new ContextLaunchVariable(tag, description, element);
	}
	
}
