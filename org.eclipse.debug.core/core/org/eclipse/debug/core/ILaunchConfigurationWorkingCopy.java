package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

/**
 * An editable copy of a launch configuration. Attributes of a
 * launch configuration are modified by modifying the attributes
 * of a working copy, and then saving the working copy.
 * <p>
 * This interface is not intended to be implemented by clients. Clients
 * that define a launch configuration delegate extension implement the
 * <code>ILaunchConfigurationDelegate</code> interface.
 * </p>
 * @see ILaunchConfiguration
 * @see ILaunchConfigurationType
 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate
 * @since 2.0
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
	 * @exception CoreException if an exception occurs while 
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
	 * Sets the <code>java.util.List</code>-valued attribute with the given name.
	 * The specified List <em>must</em> contain only String-valued entries.
	 * If the value is <code>null</code>, the attribute is removed from
	 * this launch configuration.
	 *
	 * @param attributeName the name of the attribute
	 * @param value the value, or <code>null</code> if the attribute is to be undefined
	 */
	public void setAttribute(String attributeName, List value);
	
	/**
	 * Sets the <code>java.util.Map</code>-valued attribute with the given name.
	 * The specified Map <em>must</em> contain only String keys and String values.
	 * If the value is <code>null</code>, the attribute is removed from
	 * this launch configuration.
	 *
	 * @param attributeName the name of the attribute
	 * @param value the value, or <code>null</code> if the attribute is to be undefined
	 */
	public void setAttribute(String attributeName, Map value);
	
	/**
	 * Sets the boolean-valued attribute with the given name.  
	 *
	 * @param attributeName the name of the attribute
	 * @param value the value
	 */
	public void setAttribute(String attributeName, boolean value);	
	
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
}
