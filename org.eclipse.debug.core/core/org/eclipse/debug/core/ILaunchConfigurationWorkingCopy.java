package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

/**
 * Note: This interface is yet experimental.
 * <p>
 * An editable copy of a launch configuration. Attributes of a
 * launch configuration are modified by modifying the attributes
 * of a working copy, and then saving the working copy.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients. Clients
 * that define a launch configuration delegate extension implement the
 * <code>ILaunchConfigurationDelegate</code> interface.
 * </p>
 * <p>
 * <b>NOTE:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see ILaunchConfiguration
 * @see ILaunchConfigurationType
 * @see ILaunchConfigurationDelegate
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
	 * 
	 * @exception CoreException if an exception occurrs while 
	 *  writing this configuration to its underlying file.
	 */
	public ILaunchConfiguration doSave() throws CoreException;
			
	/**
	 * Sets the integer-valued attribute with the given name.  
	 *
	 * @param attributeName the name of the attribute
	 * @param value the value
	 */
	public void setAttribute(String attributeName, int value);
	
	/**
	 * Sets the String-valued attribute with the given name.
	 * If the value is <code>null</code>, the attribute is removed from
	 * this launch configuration.
	 *
	 * @param attributeName the name of the attribute
	 * @param value the value, or <code>null</code> if the attribute is to be undefined
	 */
	public void setAttribute(String attributeName, String value);
	
	/**
	 * Sets the boolean-valued attribute with the given name.  
	 *
	 * @param attributeName the name of the attribute
	 * @param value the value
	 */
	public void setAttribute(String attributeName, boolean value);	
	
	/**
	 * Initializes this configuration's attributes to default settings
	 * for the specified object. This responsibility is handled
	 * by the lanuch configuration delegate.
	 * 
	 * @param object a context from which to initialize settings in
	 *  this launch configuration
	 * @exception CoreException if unable to instantiate the launch
	 *  configuration delegate for this configuration
	 */
	public void initializeDefaults(Object object) throws CoreException;
	
	/**
	 * Returns the original launch configuration this working copy
	 * was created from, or <code>null</code> if this is a new
	 * working copy created from a launch configuration type.
	 * 
	 * @return launch configuration, or <code>null</code>
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
	 * Sets whether this launch configuration will be stored
	 * locally in a project's working location, or as a resource
	 * in the workspace. Has no effect if the specified value reflects
	 * this launch configuration's current storage location.
	 * <p>
	 * If this configuration is changed from local to non-local,
	 * a file will be created in this launch configuration's project's
	 * '.launches' folder, with the contents of this launch configuration.
	 * The original file associated with this configuration in the project's
	 * working location will be deleted.
	 * </p>
	 * <p>
	 * If this configuration is changed from non-local to local,
	 * a file will be created in this launch configuration's project's
	 * working location, with the contents of this launch configuration.
	 * The original file associated with this configuration in the project's
	 * resource structure will be deleted.
	 * </p>
	 * 
	 * @param local whether this launch configuration is to be stored
	 *  locally in a project's working location
	 */
	public void setLocal(boolean local) throws CoreException;	
}
