/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IVariableValueEditor;

/**
 * Manager which provides the variable value editors contributed
 * via the org.eclipse.debug.ui.variableValueEditors extension
 * point.
 * 
 * @see org.eclipse.debug.ui.actions.IVariableValueEditor
 * @since 3.1
 */
public class VariableValueEditorManager {
    
    /**
     * Mapping of debug model identifiers to variable value editors.
     * The keys in this map are always Strings (model ids).
     * The values in the map are IConfigurationElements at startup,
     * which are replaced by IVariableValueEditors as the editors
     * are instantiated (editors are loaded lazily, then cached).
     */
    private Map fEditorMap= new HashMap();
    
    /**
     * The singleton instance of this manager.
     */
    private static VariableValueEditorManager fgManager;

    /**
     * Creates a new variable value editor manager. Clients
     * should access the singleton instance of this manager
     * by calling getDefault()
     */
    private VariableValueEditorManager() {
        loadVariableEditors();
    }

    /**
     * Returns the singleton instance of this manager.
     * @return the singleton instance of this manager
     */
    public static VariableValueEditorManager getDefault() {
        if (fgManager == null) {
            fgManager= new VariableValueEditorManager();
        }
        return fgManager;
    }
    
    /**
     * Returns the variable value editor associated with the given debug
     * model identifier or <code>null</code> if no editor has been supplied
     * for the given debug model.
     * @param modelIdentifier the debug model identifier
     * @return the variable value editor associated with the given debug model
     *  identifier or <code>null</code>
     */
    public IVariableValueEditor getVariableValueEditor(String modelIdentifier) {
        Object object = fEditorMap.get(modelIdentifier);
        IVariableValueEditor editor= null;
        if (object instanceof IVariableValueEditor) {
            editor= (IVariableValueEditor) object;
        } else if (object instanceof IConfigurationElement) {
            try {
                editor = (IVariableValueEditor) ((IConfigurationElement) object).createExecutableExtension("class"); //$NON-NLS-1$
                fEditorMap.put(modelIdentifier, editor);
            } catch (CoreException e) {
                // If an exception occurs, loading the extension, just log it and
                // return null.
                DebugUIPlugin.log(e);
            }
        }
        return editor;
    }
	
	/**
	 * Loads contributors to the org.eclipse.debug.ui.variableValueEditors extension point,
	 * for use when the user runs this action.
	 */
	private void loadVariableEditors() {
		IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.EXTENSION_POINT_VARIABLE_VALUE_EDITORS);
		IConfigurationElement[] elements = ep.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
            IConfigurationElement element = elements[i];
            String modelId = element.getAttribute("modelId"); //$NON-NLS-1$
            if (modelId != null) {
                fEditorMap.put(modelId, element);
            }
        }
	}
}
