package org.eclipse.ui.externaltools.internal.registry;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

/**
 * The registry of available external tool types.
 */
public class ExternalToolTypeRegistry {
	// Format of the tool type extension point
	// <extension point="org.eclipse.ui.externalTools.toolTypes>
	//		<toolType
	//			id={string}
	//			name={string}
	//			description={string}
	//			icon={string:path}
	//			runnerClass={string:IExternalToolRunner}>
	//		</toolType>
	// </extension>
	//
	/**
	 * Element and attribute tags a tool type extension point.
	 */
	/*package*/ static final String TAG_TOOL_TYPE = "toolType"; //$NON-NLS-1$
	/*package*/ static final String TAG_ID = "id"; //$NON-NLS-1$
	/*package*/ static final String TAG_NAME = "name"; //$NON-NLS-1$
	/*package*/ static final String TAG_ICON = "icon"; //$NON-NLS-1$
	/*package*/ static final String TAG_RUN_CLASS = "runnerClass"; //$NON-NLS-1$
	/*package*/ static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	
	private ArrayList types;
	
	/**
	 * Creates a new registry and loads the available type
	 * of external tools.
	 */
	public ExternalToolTypeRegistry() {
		super();
		loadTypes();
	}

	/**
	 * Returns the tool type for the specified id, or
	 * <code>null</code> if none found.
	 */
	public ExternalToolType getToolType(String toolTypeId) {
		for (int i = 0; i < types.size(); i++) {
			ExternalToolType type = (ExternalToolType) types.get(i);
			if (type.getId().equals(toolTypeId))
				return type;
		}
		return null;
	}
	
	/**
	 * Returns the tool type image for the specified id.
	 */
	public ImageDescriptor getToolTypeImageDescriptor(String toolTypeId) {
		ExternalToolType type = getToolType(toolTypeId);
		if (type == null)
			return ImageDescriptor.getMissingImageDescriptor();
		else
			return type.getImageDescriptor();
	}
	
	/**
	 * Returns the types in the registry
	 */
	public ExternalToolType[] getToolTypes() {
		ExternalToolType[] results = new ExternalToolType[types.size()];
		types.toArray(results);
		return results;
	}

	/**
	 * Returns the number of tool types	in the registry.
	 */
	public int getTypeCount() {
		return types.size();
	}
	
	/**
	 * Returns whether the tool type is known to
	 * the registry.
	 */
	public boolean hasType(String toolTypeId) {
		for (int i = 0; i < types.size(); i++) {
			ExternalToolType type = (ExternalToolType) types.get(i);
			if (type.getId().equals(toolTypeId))
				return true;
		}
		return false;
	}

	/**
	 * Load the available types of external tools.
	 */
	private void loadTypes() {
		types = new ArrayList(10);
		IPluginRegistry registry = Platform.getPluginRegistry();
		IExtensionPoint point = registry.getExtensionPoint(IExternalToolConstants.PLUGIN_ID, IExternalToolConstants.PL_TOOL_TYPES);
		if (point != null) {
			IExtension[] extensions = point.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement[] elements = extensions[i].getConfigurationElements();
				for (int j = 0; j < elements.length; j++) {
					IConfigurationElement element = elements[j];
					if (element.getName().equals(TAG_TOOL_TYPE)) {
						String id = element.getAttribute(TAG_ID);
						String name = element.getAttribute(TAG_NAME);
						String iconPath = element.getAttribute(TAG_ICON);
						String runClassName = element.getAttribute(TAG_RUN_CLASS);
						
						boolean valid = true;
						if (id == null || id.length() == 0) {
							valid = false;
							ExternalToolsPlugin.getDefault().log("Missing id attribute value for toolType element.", null); //$NON-NLS-1$
						}
						if (name == null || name.length() == 0) {
							valid = false;
							ExternalToolsPlugin.getDefault().log("Missing name attribute value for toolType element.", null); //$NON-NLS-1$
						}
						if (iconPath == null || iconPath.length() == 0) {
							valid = false;
							ExternalToolsPlugin.getDefault().log("Missing icon attribute value for toolType element.", null); //$NON-NLS-1$
						}
						if (runClassName == null || runClassName.length() == 0) {
							valid = false;
							ExternalToolsPlugin.getDefault().log("Missing class attribute value for toolType element.", null); //$NON-NLS-1$
						}
						
						if (valid)
							types.add(new ExternalToolType(id, name, element));
					}
				}
			}
		}
	}
}
