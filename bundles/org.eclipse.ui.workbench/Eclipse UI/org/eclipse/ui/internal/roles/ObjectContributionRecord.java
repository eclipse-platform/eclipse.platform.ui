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
package org.eclipse.ui.internal.roles;

/**
 * Class that represents an extension contribution from a given plugin with a 
 * given localized id value.  Used in ObjectActivityManager and elsewhere for
 * pattern matching.  The toString() value of this Object is used to match
 * against activityPatternBindings and is of the form {pluginId}/{localId}.
 * 
 * This could potentially be extended to include the extension point id from 
 * where the contribution has came from as well, although such bindings would
 * be so specific (and limited in quantity) that it'd make more sense to specify
 * the contribution-&gt;activity binding directly rather than add yet another 
 * variable for pattern matching.  Pattern matching is meant as a shortcut for
 * establishing direct bindings, not the binding mechanism itself.
 * 
 * This class is immutible for efficiency.  
 * 
 * TBD: Is this class entirely necessary?  Could there not be a static key 
 * construction method on ObjectActivityManager that would construct suitable
 * keys for use within the manager?
 */
public class ObjectContributionRecord {
    
    private String fPluginId, fLocalId, fToString;
    
    /**
     * Create an ObjectContributionRecord with the given plugin and local IDs.
     * 
     * @param pluginId
     * @param localId
     */
    public ObjectContributionRecord(String pluginId, String localId) {
        setPluginId(pluginId);
        setLocalId(localId);    
        fToString = getPluginId() + '/' + getLocalId();
    }

	/**
	 * @return the local id of this object contribution
	 */
	public String getLocalId() {
		return fLocalId;
	}

	/**
	 * @param localId the local id of this object contribution
	 */
	private void setLocalId(String localId) {
        if (localId == null) {
            throw new IllegalArgumentException();
        }
		fLocalId = localId;
	}

	/**
	 * @return the plugin id of this object contribution
	 */
	public String getPluginId() {
		return fPluginId;
	}

	/**
	 * @param pluginId the plugin id of this object contribution
	 */
	private void setPluginId(String pluginId) {
        if (pluginId == null) {
            throw new IllegalArgumentException();
        }
        
		fPluginId = pluginId;
	}    

	/**
     * True if arg0 is an ObjectContributionRecord and its toString() value 
     * matches the toString() value of the reciever.
     * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg0) {
        if (arg0 instanceof ObjectContributionRecord) {
            return toString().equals(arg0);    
        }
        else {
            return false;
        }		
	}

	/**
     * The hash of the toString() value.
     * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return toString().hashCode();
	}

	/** 
     * A composite String of the form {pluginId}/{localId}
     * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
        return fToString;
	}
}
