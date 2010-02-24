/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core;


import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

/**
 * An editable copy of a launch configuration. Attributes of a
 * launch configuration are modified by modifying the attributes
 * of a working copy, and then saving the working copy.
 * <p>
 * Since 3.3, working copies can be nested. For example a working copy B can
 * be created from the original launch configuration A. Then a nested working
 * copy C can be created from working copy B. When the <code>doSave()</code> method
 * is called on C, changes are written back to its parent working copy B without
 * effecting the original launch configuration A. When <code>doSave()</code> is called
 * on B, the changes are persisted back to the original A.
 * </p>
 * <p>
 * Clients that define a launch configuration delegate extension implement the
 * <code>ILaunchConfigurationDelegate</code> interface.
 * </p>
 * @see ILaunchConfiguration
 * @see ILaunchConfigurationType
 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ILaunchConfigurationWorkingCopy extends ILaunchConfiguration, IAdaptable {
	
	/**
	 * Returns whether this configuration has been modified
	 * since it was last saved or created.
	 * 
	 * @return whether this configuration has been modified
	 *  since it was last saved or created
	 */
	public boolean isDirty();
	
	/**
	 * Saves this working copy to its underlying file and returns
	 * a handle to the resulting launch configuration.
	 * Has no effect if this configuration does not need saving.
	 * Creates the underlying file if not yet created.
	 * <p>
	 * Since 3.3, if this is a nested working copy, the contents of this working copy are
	 * saved to the parent working copy and the parent working copy is returned without
	 * effecting the original launch configuration.
	 * </p>
	 * @return handle to saved launch configuration
	 * @exception CoreException if an exception occurs while 
	 *  writing this configuration to its underlying file.
	 */
	public ILaunchConfiguration doSave() throws CoreException;
			
	/**
	 * Sets the integer-valued attribute with the given name.  
	 *
	 * @param attributeName the name of the attribute, cannot be <code>null</code>
	 * @param value the value
	 */
	public void setAttribute(String attributeName, int value);
	
	/**
	 * Sets the String-valued attribute with the given name.
	 * If the value is <code>null</code>, the attribute is removed from
	 * this launch configuration.
	 *
	 * @param attributeName the name of the attribute, cannot be <code>null</code>
	 * @param value the value, or <code>null</code> if the attribute is to be undefined
	 */
	public void setAttribute(String attributeName, String value);
	
	/**
	 * Sets the <code>java.util.List</code>-valued attribute with the given name.
	 * The specified List <em>must</em> contain only String-valued entries.
	 * If the value is <code>null</code>, the attribute is removed from
	 * this launch configuration.
	 *
	 * @param attributeName the name of the attribute, cannot be <code>null</code>
	 * @param value the value, or <code>null</code> if the attribute is to be undefined
	 */
	public void setAttribute(String attributeName, List value);
	
	/**
	 * Sets the <code>java.util.Map</code>-valued attribute with the given name.
	 * The specified Map <em>must</em> contain only String keys and String values.
	 * If the value is <code>null</code>, the attribute is removed from
	 * this launch configuration.
	 *
	 * @param attributeName the name of the attribute, cannot be <code>null</code>
	 * @param value the value, or <code>null</code> if the attribute is to be undefined
	 */
	public void setAttribute(String attributeName, Map value);
	
	/**
	 * Sets the <code>java.util.Set</code>-valued attribute with the given name.
	 * The specified Set <em>must</em> contain only String values.
	 * If the value is <code>null</code>, the attribute is removed from
	 * this launch configuration.
	 *
	 * @param attributeName the name of the attribute, cannot be <code>null</code>
	 * @param value the value, or <code>null</code> if the attribute is to be undefined
	 * @since 3.6
	 */
	public void setAttribute(String attributeName, Set value);
	
	/**
	 * Sets the boolean-valued attribute with the given name.  
	 *
	 * @param attributeName the name of the attribute, cannot be <code>null</code>
	 * @param value the value
	 */
	public void setAttribute(String attributeName, boolean value);	
	
	/**
	 * Returns the original launch configuration this working copy
	 * was created from or <code>null</code> if this is a new
	 * working copy created from a launch configuration type.
	 * 
	 * @return the original launch configuration, or <code>null</code>
	 */
	public ILaunchConfiguration getOriginal();
	
	/**
	 * Renames this launch configuration to the specified name.
	 * The new name cannot be <code>null</code>. Has no effect if the name
	 * is the same as the current name. If this working copy is based
	 * on an existing launch configuration, this will cause
	 * the underlying launch configuration file to be renamed when
	 * this working copy is saved.
	 * 
	 * @param name the new name for this configuration 
	 */
	public void rename(String name);	
	
	/**
	 * Sets the container this launch configuration will be stored
	 * in when saved. When set to <code>null</code>, this configuration
	 * will be stored locally with the workspace. The specified
	 * container must exist, if specified.
	 * <p>
	 * If this configuration is changed from local to non-local,
	 * a file will be created in the specified container when
	 * saved. The local file associated with this configuration
	 * will be deleted.
	 * </p>
	 * <p>
	 * If this configuration is changed from non-local to local,
	 * a file will be created locally when saved.
	 * The original file associated with this configuration in
	 * the workspace will be deleted.
	 * </p>
	 * 
	 * @param container the container in which to store this
	 *  launch configuration, or <code>null</code> if this
	 *  configuration is to be stored locally
	 */
	public void setContainer(IContainer container);	
	
	/**
	 * Sets the attributes of this launch configuration to be the ones contained
	 * in the given map. The values must be an instance of one of the following
	 * classes: <code>String</code>, <code>Integer</code>, or
	 * <code>Boolean</code>, <code>List</code>, <code>Map</code>. Attributes
	 * previously set on this launch configuration but not included in the given
	 * map are considered to be removals. Setting the given map to be
	 * <code>null</code> is equivalent to removing all attributes.
	 *
	 * @param attributes a map of attribute names to attribute values.
	 *  Attribute names are not allowed to be <code>null</code>
	 * @since 2.1
	 */
	public void setAttributes(Map attributes);
	
	/**
	 * Sets the resources associated with this launch configuration, possibly <code>null</code>.
	 * Clients contributing launch configuration types are responsible for maintaining
	 * resource mappings.
	 *  
	 * @param resources the resource to map to this launch configuration or <code>null</code>
	 * @since 3.2
	 */
	public void setMappedResources(IResource[] resources);
	
	/**
	 * Set the launch modes for this configuration.
	 * Over-writes existing launch modes.
	 * <p>
	 * Setting launch modes on a configuration allows the configuration to
	 * be launched in a mixed mode - for example, debug and profile.
	 * </p>
	 * @param modes launch mode identifiers to set on this configuration
	 *  or <code>null</code> to clear mode settings
	 *  
	 * @since 3.3
	 */
	public void setModes(Set modes);
	
	/**
	 * Set the preferred launch delegates' id for the given mode set. Passing in <code>null</code> as a delegate
	 * id will cause the mapping for the specified mode set (if any) to be removed.
	 * 
	 * @param modes the set of modes to set this delegate id for
	 * @param delegateId the id of the delegate to associate as preferred for the specified mode set
	 *  or <code>null</code> to clear the setting
	 *  
	 * @since 3.3
	 */
	public void setPreferredLaunchDelegate(Set modes, String delegateId);
	
	/**
	 * Adds the specified launch modes to this configuration's settings.
	 * <p>
	 * Setting launch modes on a configuration allows the configuration to
	 * be launched in a mixed mode - for example, debug and profile.
	 * </p>
	 * @param modes launch mode identifiers to append to the current set of
	 * 	launch modes set on this configuration
	 * 
	 * @since 3.3
	 */
	public void addModes(Set modes);
	
	/**
	 * Removes the specified launch modes from this configuration's settings.
	 * <p>
	 * Setting launch modes on a configuration allows the configuration to
	 * be launched in a mixed mode - for example, debug and profile.
	 * </p>
	 * @param modes launch mode identifiers to remove from the current set of
	 * 	launch modes set on this configuration
	 * 
	 * @since 3.3
	 */
	public void removeModes(Set modes);
	
	/**
	 * Removes the specified attribute from the this configuration and returns
	 * the previous value associated with the specified attribute name, or <tt>null</tt>
     * if there was no mapping for the attribute. Note that for int's and booleans,
     * corresponding Integer and Boolean objects are returned.
	 * <p>
	 * This method allows non-object attributes to be removed.
	 * </p>
	 * @param attributeName the name of the attribute to remove
	 * @return previous value of the attribute or <code>null</code>
	 * 
	 * @since 3.4
	 */
	public Object removeAttribute(String attributeName);
	
	/**
	 * Returns the parent of this working copy or <code>null</code> if this working
	 * copy is not a nested copy of another working copy.
	 * 
	 * @return parent or <code>null</code>
	 * @since 3.3
	 */
	public ILaunchConfigurationWorkingCopy getParent();
}
