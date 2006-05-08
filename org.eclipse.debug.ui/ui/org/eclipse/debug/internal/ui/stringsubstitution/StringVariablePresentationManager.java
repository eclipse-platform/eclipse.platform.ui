/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.stringsubstitution;

import com.ibm.icu.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.variables.IStringVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;

/**
 * Manages argument selectors (choosers) for string variables.
 * 
 * @since 3.0
 */
public class StringVariablePresentationManager {
	
	/**
	 * String variable presentation extension point identifier
	 * (value <code>"stringVariablePresentations"</code>).
	 * 
	 * @since 3.0
	 */
	public static final String EXTENSION_POINT_STRING_VARIABLE_PRESENTATIONS = "stringVariablePresentations"; //$NON-NLS-1$
	
	// default manager
	private static StringVariablePresentationManager fgManager;
	
	// extension point attributes
	public static final String ATTR_NAME = "variableName"; //$NON-NLS-1$
	public static final String ATTR_ARGUMENT_SELECTOR = "argumentSelector"; //$NON-NLS-1$
	
	/**
	 * Table of configuration elements for variable presentations,
	 * keyed by variable name.
	 */
	private Map fConfigurations;
	
	/**
	 * Returns the singleton string variable presentation manager.
	 * 
	 * @return the singleton string variable presentation manager
	 */
	public static StringVariablePresentationManager getDefault() {
		if (fgManager == null) {
			fgManager = new StringVariablePresentationManager();
		}
		return fgManager;
	}
	
	/**
	 * Returns an argument selector contributed for the given
	 * variable, or <code>null</code> if none.
	 * 
	 * @param variable string substitution variable
	 * @return argument selector or <code>null</code>
	 */
	public IArgumentSelector getArgumentSelector(IStringVariable variable) {
		IConfigurationElement element = (IConfigurationElement) fConfigurations.get(variable.getName());
		if (element != null) {
			try {
				return (IArgumentSelector)element.createExecutableExtension(ATTR_ARGUMENT_SELECTOR);
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}
		return null;
	}
	
	/**
	 * Constructs the manager, loading extensions.
	 */
	private StringVariablePresentationManager() {
		initialize();
	}

	/**
	 * Load extensions 
	 */
	private void initialize() {
		fConfigurations = new HashMap();
		IExtensionPoint point= Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), EXTENSION_POINT_STRING_VARIABLE_PRESENTATIONS);
		IConfigurationElement elements[]= point.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			String name= element.getAttribute(ATTR_NAME);
			if (name == null) {
				DebugUIPlugin.logErrorMessage(MessageFormat.format("String variable presentation extension missing required 'variableName' attribute: {0}", new String[] {element.getDeclaringExtension().getLabel()})); //$NON-NLS-1$
				continue;
			}
			fConfigurations.put(name, element);
		}		
	}
	

}
