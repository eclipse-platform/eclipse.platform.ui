package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

/**
 * Describes and creates instances of a specific type of
 * launch configuration. Launch configuration types are
 * defined by extensions.
 * <p>
 * A launch configuration type extension is defined in <code>plugin.xml</code>.
 * Following is an example definition of a launch configuration
 * type extension.
 * <pre>
 * &lt;extension point="org.eclipse.debug.core.launchConfigurationTypes"&gt;
 *   &lt;launchConfigurationType 
 *      id="com.example.ExampleIdentifier"
 *      delegate="com.example.ExampleLaunchConfigurationDelegate"
 *      modes="run, debug"
 *      name="Example Application"
 *   &lt;/launchConfigurationType&gt;
 * &lt;/extension&gt;
 * </pre>
 * The attributes are specified as follows:
 * <ul>
 * <li><code>id</code> specifies a unique identifier for this launch configuration
 *  type.</li>
 * <li><code>delegate</code> specifies the fully qualified name of the java class
 *   that implements <code>ILaunchConfigurationDelegate</code>. Launch configuration
 *   instances of this type will delegate to instances of this class
 *   to perform launching.</li>
 * <li><code>modes</code> specifies a comma separated list of the modes this
 *    type of launch configuration suports - <code>"run"</code> and/or <code>"debug</code>.</li>
 * <li><code>name</code> specifies a human readable name for this type
 *    of launch configuration.</li>
 * </ul>
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients. Clients
 * that define a launch configuration delegate extension implement the
 * <code>ILaunchConfigurationDelegate</code> interface.
 * </p>
 * @see ILaunchConfiguration
 * @since 2.0
 */
public interface ILaunchConfigurationType {
		
	/**
	 * Returns whether this type of launch configuration supports
	 * the specified mode.
	 * 
	 * @param mode a mode in which a configuration can be launched, one of
	 *  the mode constants defined by this interface - <code>RUN</code> or
	 *  <code>DEBUG</code>.
	 * @return whether this kind of launch configuration supports the
	 *  specified mode
	 */
	public boolean supportsMode(String mode);
	
	/**
	 * Returns the name of this type of launch configuration.
	 * 
	 * @return the name of this type of launch configuration
	 */
	public String getName();
	
	/**
	 * Returns the unique identifier for this type of launch configuration
	 * 
	 * @return the unique identifier for this type of launch configuration
	 */
	public String getIdentifier();
	
	/**
	 * Returns a new launch configuration working copy of this type,
	 * that resides in the specified container, with the given name.
	 * When <code>container</code> is </code>null</code>, the configuration
	 * will reside locally in the metadata area.
	 * Note: a working copy is not actually created until saved.
	 * 
	 * @param container the container in which the new configuration will
	 *  reside, or <code>null</code> if the configuration should reside
	 *  locally with the metadata.
	 * @param name name for the launch configuration
	 * @return a new launch configuration instance of this type
	 * @exception CoreException if an instance of this type
	 *  of launch configuration could not be created for any
	 *  reason
	 */
	public ILaunchConfigurationWorkingCopy newInstance(IContainer container, String name) throws CoreException;
	
	/**
	 * Returns the launch configuration delegate for launch
	 * configurations of this type. The first time this method
	 * is called, the delegate is instantiated.
	 * 
	 * @return launch configuration delegate
	 * @exception CoreException if unable to instantiate the
	 *  delegate
	 */	
	public ILaunchConfigurationDelegate getDelegate() throws CoreException;
		
}
