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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

    
    private Map fgSelectedTypes= null;
    /**
     * List of known type identifiers. An identifiers index in this list is used as
     * its ID number.
     */
    private List fgTypeIds= null;
    
    public static final String PREF_SELECTED_TYPES= "selectedStructures"; //$NON-NLS-1$
    public static final String PREF_ALL_TYPES= "allStructures"; //$NON-NLS-1$
	
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
    
    public void loadSelectedTypes() {
        fgSelectedTypes= new HashMap();
        String selections= DebugPlugin.getDefault().getPluginPreferences().getString(PREF_SELECTED_TYPES);
        int start= 0;
        while (true) {
        	// selections are stored in the form:
        	// selection|selection|...selection|
            int index = selections.indexOf('|', start);
            if (index > 0) {
                String selection= selections.substring(start, index);
                // selection string is of the form:
                // id,id,...,selectedid
                int i = selection.lastIndexOf(',');
                if (i > 0 && i < selection.length() - 1) {
	                String comboKey= selection.substring(0, i + 1);
	                String selected= selection.substring(i + 1, selection.length());
	                fgSelectedTypes.put(comboKey, new Integer(Integer.parseInt(selected)));
                }
                start= index + 1;
            } else {
            	break;
            }
        }
    }
    
    public void storeSelectedTypes() {
        StringBuffer buffer= new StringBuffer();
        Iterator iter = fgSelectedTypes.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Entry) iter.next();
            buffer.append(entry.getKey());
            buffer.append(entry.getValue());
            buffer.append('|');
        }
        DebugPlugin.getDefault().getPluginPreferences().setValue(PREF_SELECTED_TYPES, buffer.toString());
    }
    
    public void loadTypes() {
        fgTypeIds= new ArrayList();
    	// Types are stored as a comma-separated, ordered list.
        String types= DebugPlugin.getDefault().getPluginPreferences().getString(PREF_ALL_TYPES);
        int start= 0;
        while (true) {
        	int end = types.indexOf(',', start);
        	if (end > start) {
        		fgTypeIds.add(types.substring(start, end));
        	} else {
        		break;
        	}
        	start= end + 1;
        }
    }
    
    public void storeTypes() {
        StringBuffer buffer= new StringBuffer();
        Iterator iter = fgTypeIds.iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next()).append(',');
        }
        DebugPlugin.getDefault().getPluginPreferences().setValue(PREF_ALL_TYPES, buffer.toString());
    }
    
    public ILogicalStructureType getSelectedType(ILogicalStructureType[] types) {
        if (types.length == 0) {
            return null;
        }
        StringBuffer comboKey= new StringBuffer();
        // First, build the "combo string" to use for lookup
        for (int i = 0; i < types.length; i++) {
            Integer integer= new Integer(fgTypeIds.indexOf(types[i].getId()));
            comboKey.append(integer).append(',');
        }
        // Lookup the combo
        Integer index = (Integer) fgSelectedTypes.get(comboKey.toString());
        if (index == null) {
            // If the user hasn't explicitly chosen anything for this
            // combo yet, just return the first type.
            return types[0];
        } else if (index.intValue() == -1) {
            // An index of -1 means the user has deselected all structures for this combo
            return null;
        }
        // If an index is stored for this combo, retrieve the id at the index
        String id= (String) fgTypeIds.get(index.intValue());
        for (int i = 0; i < types.length; i++) {
            // Return the type with the retrieved id
            ILogicalStructureType type = types[i];
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return types[0];
    }
    
    /**
     * 
     * @param types
     * @param selected the type that is selected for the given combo or <code>null</code>
     *  if the user has deselected any structure for the given combo
     */
    public void setEnabledType(ILogicalStructureType[] types, ILogicalStructureType selected) {
        StringBuffer comboKey= new StringBuffer();
        for (int i = 0; i < types.length; i++) {
            ILogicalStructureType type = types[i];
            int typeIndex = fgTypeIds.indexOf(type.getId());
            if (typeIndex == -1) {
                typeIndex= fgTypeIds.size();
                fgTypeIds.add(type.getId());
            }
            comboKey.append(typeIndex).append(',');
        }
        int index= -1; // Initialize to "none selected"
        if (selected != null) {
            index= fgTypeIds.indexOf(selected.getId());
        }
        Integer integer= new Integer(index);
        fgSelectedTypes.put(comboKey.toString(), integer);
        storeSelectedTypes();
        storeTypes();
        DebugPlugin.getDefault().savePluginPreferences();
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
        if (fgSelectedTypes == null) {
            loadSelectedTypes();
        }
        if (fgTypeIds == null) {
            loadTypes();
        }
	}
}
