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
package org.eclipse.debug.internal.ui.views.variables;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DefaultVariablesContentProvider;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IVariablesContentProvider;

/**
 * This class returns an instance of <code>IVariablesContentProvider</code>
 * via the <code>getContentProvider</code> method for a given debug model
 * id.  If no implementation is registered for a debug model id, a default
 * implementation is returned.
 */
public class VariablesContentProviderManager {

	/**
	 * Mapping of debug model ids to <code>IConfigurationElements</code>
	 * that describe variables content providers.
	 */
	private Map fConfigElementMap;
	
	/**
	 * Mapping of debug model ids to <code>IVariablesContentProvider</code>s.
	 */
	private Map fContentProviderMap;
	
	/**
	 * The default implementation of <code>IVariablesContentProvider</code>,
	 * used when no other implementation can be found for a particular
	 * debug model id.
	 */
	private IVariablesContentProvider fDefaultContentProvider;

	public VariablesContentProviderManager() {
		loadConfigElementMap();
	}

	/**
	 * Return an instance of <code>IVariablesContentProvider</code> corresponding
	 * to the specified debug model identifier.
	 */
	public IVariablesContentProvider getContentProvider(String debugModelId) {
		IVariablesContentProvider contentProvider = getContentProviderByModelId(debugModelId);
		if (contentProvider == null) {
			contentProvider = getDefaultContentProvider();
		}
		return contentProvider;		
	}
	
	protected IVariablesContentProvider getContentProviderByModelId(String debugModelId) {
		if (fContentProviderMap == null) {
			fContentProviderMap = new HashMap(fConfigElementMap.size());
		}
		IVariablesContentProvider contentProvider = (IVariablesContentProvider) fContentProviderMap.get(debugModelId);
		if (contentProvider == null) {
			IConfigurationElement configElement = (IConfigurationElement) fConfigElementMap.get(debugModelId);
			if (configElement == null) {
				return null;
			}
			Object executable = null;
			try {
				executable = DebugUIPlugin.createExtension(configElement, "class"); //$NON-NLS-1$
			} catch (CoreException ce) {
				DebugUIPlugin.log(ce);
				return null;
			}
			if (!(executable instanceof IVariablesContentProvider)) {
				DebugUIPlugin.logErrorMessage(MessageFormat.format(VariablesViewMessages.getString("VariablesContentProviderManager.2"), new String[]{configElement.getAttribute("class")}));			 //$NON-NLS-1$ //$NON-NLS-2$
				return null;
			}	
			contentProvider = (IVariablesContentProvider) executable;
			fContentProviderMap.put(debugModelId, contentProvider);		
		}
		return contentProvider;
	}
	
	/**
	 * Load the mapping of debug model ids to config elements.
	 */
	protected void loadConfigElementMap() {
		IPluginDescriptor descriptor = DebugUIPlugin.getDefault().getDescriptor();
		IExtensionPoint extensionPoint= descriptor.getExtensionPoint(IDebugUIConstants.EXTENSION_POINT_VARIABLES_CONTENT_PROVIDERS);
		IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
		
		fConfigElementMap = new HashMap(5);
		for (int i = 0; i < infos.length; i++) {
			IConfigurationElement configElement = infos[i];
			String debugModelId = configElement.getAttribute("debugModelId"); //$NON-NLS-1$
			fConfigElementMap.put(debugModelId, configElement);
		}				
	}

	/**
	 * Return the default content provider, creating it if necessary.
	 */
	public IVariablesContentProvider getDefaultContentProvider() {
		if (fDefaultContentProvider == null) {
			fDefaultContentProvider = new DefaultVariablesContentProvider();
		}
		return fDefaultContentProvider;
	}
	
}
