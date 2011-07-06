/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import java.util.StringTokenizer;
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

    /**
     * Map containing the user's selection for each combination of logical
     * structure types.
     * key: String - Comma-separated list of ints representing a combination of structure types.
     *               These integers are indeces into the fStructureTypeIds array.
     * value: Integer - One of the ints from the combo key (the one chosen by the user) or -1 if
     *                  the user has chosen not to display any structures for this combination
     */
    private Map fStructureTypeSelections= null;
    /**
     * List of known type identifiers. An identifier's index in this list is used as
     * its ID number. This list is maintained as a space-saving measure so that the various
     * combinations of structure types can be persisted using indeces instead of storing the
     * full index strings.
     */
    private List fStructureTypeIds= null;
    
    /**
     * Preference key used for storing the user's selected structure for each combination
     * or structures. The preference value is stored in the form:
     * int,int,...,int|int,int,...int|...
     * Where int is an integer index of a structure in the array of known structures.
     */
    public static final String PREF_STRUCTURE_SELECTIONS= "selectedStructures"; //$NON-NLS-1$
    /**
     * Preference key used for storing the array of known structures. The preference
     * value is in the form:
     * string,string,string,...,string,
     * Where string is an identifier of a logical structure.
     */
    public static final String PREF_STRUCTURE_IDS= "allStructures"; //$NON-NLS-1$
	
	public static LogicalStructureManager getDefault() {
		if (fgDefault == null) {
			fgDefault = new LogicalStructureManager();
		}
		return fgDefault;
	}
	
    /**
     * Returns the logical structure types that are applicable to the given value.
     * @param value the value
     * @return the logical structure types that are applicable to the given value
     */
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
    
    /**
     * Loads the map of structure selections from the preference store.
     */
    private void loadStructureTypeSelections() {
        fStructureTypeSelections= new HashMap();
        String selections= Platform.getPreferencesService().getString(DebugPlugin.getUniqueIdentifier(), PREF_STRUCTURE_SELECTIONS, IInternalDebugCoreConstants.EMPTY_STRING, null);
    	// selections are stored in the form:
    	// selection|selection|...selection|
        StringTokenizer tokenizer= new StringTokenizer(selections, "|"); //$NON-NLS-1$
        while (tokenizer.hasMoreTokens()) {
            String selection = tokenizer.nextToken();
            // selection string is of the form:
            // id,id,...,selectedid
            int i = selection.lastIndexOf(',');
            if (i > 0 && i < selection.length() - 1) {
                String comboKey= selection.substring(0, i + 1);
                String selected= selection.substring(i + 1, selection.length());
                fStructureTypeSelections.put(comboKey, new Integer(Integer.parseInt(selected)));
            }
        }
    }
    
    /**
     * Stores the map of structure selections to the preference store
     */
    private void storeStructureTypeSelections() {
        StringBuffer buffer= new StringBuffer();
        Iterator iter = fStructureTypeSelections.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Entry) iter.next();
            buffer.append(entry.getKey());
            buffer.append(entry.getValue());
            buffer.append('|');
        }
        Preferences.setString(DebugPlugin.getUniqueIdentifier(), PREF_STRUCTURE_SELECTIONS, buffer.toString(), null);
    }
    
    /**
     * Loads the collection of known structures identifiers from the preference store
     */
    private void loadStructureTypeIds() {
        fStructureTypeIds= new ArrayList();
    	// Types are stored as a comma-separated, ordered list.
        String types= Platform.getPreferencesService().getString(DebugPlugin.getUniqueIdentifier(), PREF_STRUCTURE_IDS, IInternalDebugCoreConstants.EMPTY_STRING, null);
        StringTokenizer tokenizer= new StringTokenizer(types, ","); //$NON-NLS-1$
        while (tokenizer.hasMoreTokens()) {
            String id= tokenizer.nextToken();
            if (id.length() > 0) {
                fStructureTypeIds.add(id);
            }
        }
    }
    
    /**
     * Stores the collection of known structure identifiers to the preference store
     */
    private void storeStructureTypeIds() {
        StringBuffer buffer= new StringBuffer();
        Iterator iter = fStructureTypeIds.iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next()).append(',');
        }
        Preferences.setString(DebugPlugin.getUniqueIdentifier(), PREF_STRUCTURE_IDS, buffer.toString(), null);
    }
    
    /**
     * Returns the structure that the user has chosen from among the given
     * collection of structures or <code>null</code> if the user has chosen
     * to display none.
     * @param structureTypes the collection of structures available
     * @return the structure that the user has chosen from among the given collection
     *  or <code>null</code> if the user has chosen to display none
     */
    public ILogicalStructureType getSelectedStructureType(ILogicalStructureType[] structureTypes) {
        if (structureTypes.length == 0) {
            return null;
        }
        String combo= getComboString(structureTypes);
        // Lookup the combo
        Integer index = (Integer) fStructureTypeSelections.get(combo);
        if (index == null) {
            // If the user hasn't explicitly chosen anything for this
            // combo yet, just return the first type.
            return structureTypes[0];
        } else if (index.intValue() == -1) {
            // An index of -1 means the user has deselected all structures for this combo
            return null;
        }
        // If an index is stored for this combo, retrieve the id at the index
        String id= (String) fStructureTypeIds.get(index.intValue());
        for (int i = 0; i < structureTypes.length; i++) {
            // Return the type with the retrieved id
            ILogicalStructureType type = structureTypes[i];
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return structureTypes[0];
    }
    
    /**
     * 
     * @param types the array of types
     * @param selected the type that is selected for the given combo or <code>null</code>
     *  if the user has de-selected any structure for the given combo
     */
    public void setEnabledType(ILogicalStructureType[] types, ILogicalStructureType selected) {
        String combo= getComboString(types);
        int index= -1; // Initialize to "none selected"
        if (selected != null) {
            index= fStructureTypeIds.indexOf(selected.getId());
        }
        Integer integer= new Integer(index);
        fStructureTypeSelections.put(combo, integer);
        storeStructureTypeSelections();
        storeStructureTypeIds();
    }
    
    /**
     * Returns the string representing the given combination of logical
     * structure types. This string will be a series of comma-separated
     * indices representing the various types. If any of the given types
     * don't have indices associated with them, this method will create
     * the appropriate index.
     * @param types the logical structure types
     * @return the string representing the given combination of logical
     *  structure types
     */
    protected String getComboString(ILogicalStructureType[] types) {
        StringBuffer comboKey= new StringBuffer();
        for (int i = 0; i < types.length; i++) {
            ILogicalStructureType type = types[i];
            int typeIndex = fStructureTypeIds.indexOf(type.getId());
            if (typeIndex == -1) {
                typeIndex= fStructureTypeIds.size();
                fStructureTypeIds.add(type.getId());
            }
            comboKey.append(typeIndex).append(',');
        }
        return comboKey.toString();
    }
	
	private synchronized void initialize() {
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
        if (fStructureTypeSelections == null) {
            loadStructureTypeSelections();
        }
        if (fStructureTypeIds == null) {
            loadStructureTypeIds();
        }
	}
}
