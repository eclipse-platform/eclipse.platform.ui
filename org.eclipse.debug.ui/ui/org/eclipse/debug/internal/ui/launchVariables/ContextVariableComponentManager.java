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
package org.eclipse.debug.internal.ui.launchVariables;

import java.text.MessageFormat;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.launchVariables.IVariableComponent;
import org.eclipse.debug.ui.launchVariables.IContextVariableComponentManager;

public class ContextVariableComponentManager implements IContextVariableComponentManager {
	
	private HashMap fVariableMap= new HashMap();
	private static final IVariableComponent defaultComponent = new DefaultVariableComponent(false);
	
	private static final String ELEMENT_COMPONENT= "component"; //$NON-NLS-1$
	private static final String ATTR_VARIABLE_NAME= "variableName"; //$NON-NLS-1$
	private static final String ATTR_COMPONENT_CLASS= "componentClass"; //$NON-NLS-1$
	
	public ContextVariableComponentManager() {
		loadComponentExtensions();
	}
	
	public IVariableComponent getComponent(String variableName) {
		Object object= fVariableMap.get(variableName);
		IVariableComponent component= defaultComponent;
		if (object instanceof IVariableComponent) {
			component=  (IVariableComponent) object;
		} else if (object instanceof IConfigurationElement) {
			IConfigurationElement element= (IConfigurationElement) object;
			if (element.getAttribute(ATTR_COMPONENT_CLASS) != null) {
				try {
					component= (IVariableComponent) element.createExecutableExtension(ATTR_COMPONENT_CLASS);
				} catch (CoreException e) {
					component= new DefaultVariableComponent(true);
					DebugUIPlugin.log(DebugUIPlugin.newErrorStatus(MessageFormat.format("Failed to load variable component for {0}", new String[] {element.getAttribute(ATTR_VARIABLE_NAME)}), e)); //$NON-NLS-1$
				}
			}
			fVariableMap.put(variableName, component);
		}
		return component;
	}
	
	public void loadComponentExtensions() {
		IExtensionPoint point= DebugUIPlugin.getDefault().getDescriptor().getExtensionPoint(IDebugUIConstants.EXTENSION_POINT_LAUNCH_VARIABLE_COMPONENTS);
		IConfigurationElement elements[]= point.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (!element.getName().equals(ELEMENT_COMPONENT)) {
				DebugUIPlugin.logErrorMessage(MessageFormat.format("Invalid variable component extension found: {0}", new String[] {element.getDeclaringExtension().getLabel()})); //$NON-NLS-1$
				continue;
			}
			String variableName= element.getAttribute(ATTR_VARIABLE_NAME);
			if (variableName == null) {
				DebugUIPlugin.logErrorMessage(MessageFormat.format("Invalid variable component extension found: {0}", new String[] {element.getDeclaringExtension().getLabel()})); //$NON-NLS-1$
				continue;
			}
			if (element.getAttribute(ATTR_COMPONENT_CLASS) == null) {
				DebugUIPlugin.logErrorMessage(MessageFormat.format("Invalid variable component extension found. No component specified for {0}", new String[] {variableName})); //$NON-NLS-1$
				continue;
			}
			fVariableMap.put(variableName, element);
		}
	}

}
