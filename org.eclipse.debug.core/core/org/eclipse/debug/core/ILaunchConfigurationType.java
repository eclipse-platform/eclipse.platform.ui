/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core;

 
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;

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
 *      name="Example Application"&gt;
 *      sourceLocatorId="com.example.SourceLocator"&gt;
 *      sourcePathComputerId="com.example.SourcePathComputer"&gt;
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
 *    type of launch configuration suports - <code>"run"</code> and/or <code>"debug"</code>.</li>
 * <li><code>name</code> specifies a human readable name for this type
 *    of launch configuration.</li>
 * <li><code>category</code> is an optional attribute that specifies a category
 * for this launch configuration type. Categories are client defined. This
 * attribute was added in the 2.1 release.</li>
 * <li><code>sourceLocatorId</code> an optional unique identifier of a sourceLocator extension that
 * is used to create the source locator for sessions launched using launch configurations
 * of this type. This atttribute was added in the 3.0 release.</li>
 * <li><code>sourcePathComputerId</code> an optional unique identifier of a sourcePathComputer extension
 * that is used to compute a default source lookup path for launch configurations of this type.
 * This atttribute was added in the 3.0 release.</li>
 * </ul>
 * </p>
 * <p>
 * The <code>category</code> attribute has been added in release 2.1, such that other
 * tools may re-use the launch configuration framework for purposes other than
 * the standard running and debugging of programs under developement. Such that
 * clients may access arbitrary attribtes specified in launch configuration type
 * extension definitions, the method <code>getAttribute</code> has also been
 * added. Launch configurations that are to be recognized as standard run/debug
 * launch configurations should not specify the <code>category</code> attribute.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients. Clients
 * that define a launch configuration delegate extension implement the
 * <code>ILaunchConfigurationDelegate</code> interface.
 * </p>
 * @see ILaunchConfiguration
 * @since 2.0
 */
public interface ILaunchConfigurationType extends IAdaptable {
		
	/**
	 * Returns whether this type of launch configuration supports
	 * the specified mode.
	 * 
	 * @param mode a mode in which a configuration can be launched, one of
	 *  the mode constants defined by <code>ILaunchManager</code> - <code>RUN_MODE</code> or
	 *  <code>DEBUG_MODE</code>.
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
	 * Returns whether this launch configuration type is public.  Public configuration
	 * types are available for use by the user, for example, the user can create new
	 * configurations based on public types through the UI.  Private types are not
	 * accessbile in this way, but are still available through the methods on 
	 * <code>ILaunchManager</code>.
	 * 
	 * @return whether this launch configuration type is public.
	 */
	public boolean isPublic();
	
	/**
	 * Returns a new launch configuration working copy of this type,
	 * that resides in the specified container, with the given name.
	 * When <code>container</code> is </code>null</code>, the configuration
	 * will reside locally in the metadata area.
	 * Note: a launch configuration is not actually created until the working copy is saved.
	 * 
	 * @param container the container in which the new configuration will
	 *  reside, or <code>null</code> if the configuration should reside
	 *  locally with the metadata.
	 * @param name name for the launch configuration
	 * @return a new launch configuration working copy instance of this type
	 * @exception CoreException if an instance of this type
	 *  of launch configuration could not be created for any
	 *  reason
	 */
	public ILaunchConfigurationWorkingCopy newInstance(IContainer container, String name) throws CoreException;
	
	/**
	 * Returns the launch configuration delegate for launch
	 * configurations of this type, for <code>run</code> mode.
	 * The first time this method is called, the delegate is instantiated.
	 * 
	 * @return launch configuration delegate
	 * @exception CoreException if unable to instantiate the
	 *  delegate
	 * @deprecated use <code>getDelegate(String)</code> to specify mode
	 */	
	public ILaunchConfigurationDelegate getDelegate() throws CoreException;
	
	/**
	 * Returns the launch configuration delegate for launch
	 * configurations of this type, for the specified mode. The first time
	 * this method is called for a mode, the delegate is instantiated.
	 * Launch delegates may be contributed to a launch configuration type
	 * via the extension point <code>org.eclipse.debug.core.launchDelegates</code>
	 * 
	 * @param mode launch mode
	 * @return launch configuration delegate
	 * @exception CoreException if unable to instantiate the
	 *  delegate
	 * @since 3.0
	 */	
	public ILaunchConfigurationDelegate getDelegate(String mode) throws CoreException;
	
	/**
	 * Returns this launch configuration type's category, or <code>null</code>
	 * if unspecified. This corresponds to the category attribute specified in
	 * the extension definition.
	 * 
	 * @return this launch configuration type's category, or <code>null</code>
	 * @since 2.1
	 */
	public String getCategory();
	
	/**
	 * Returns the attribute with the given name, as specified by this launch
	 * configuration type's extension definition, or <code>null</code> if
	 * unspecified.
	 * 
	 * @param attributeName attribute name
	 * @return the specified extension attribute, or <code>null</code>
	 * @since 2.1
	 */
	public String getAttribute(String attributeName);	
	
	/**
	 * Returns the source path computer registered with this launch configuration
	 * type or <code>null</code> if unspecified. A source path computer can be
	 * registered with a launch configuration type in plug-in XML via the
	 * <code>sourcePathComputerId</code> attribute.
	 * 
	 * @return the source path computer registered with this launch configuration
	 * type or <code>null</code> if unspecified
	 * @since 3.0
	 */
	public ISourcePathComputer getSourcePathComputer();
	
	/**
	 * Returns the identifier of the persistable source locator registered with
	 * this launch configurations type, or <code>null</code> if unspecified.
	 * Launch configuration types optionally specify this attribue
	 * in their plug-in XML via the <code>sourceLocatorId</code> attribute.  
	 *  
	 * @return the identifier of the persistable source locator registered with
	 *  this launch configurations type, or <code>null</code> if unspecified
	 * @since 3.0
	 */
	public String getSourceLocatorId();
	
	/**
	 * Returns the identifier of the plug-in that contributes this launch configuration type.
	 * 
	 * @return the identifier of the plug-in that contributes this launch configuration type
	 * @since 3.0
	 */
	public String getPluginIdentifier();
	
	/**
	 * Returns all of the registered supported modes for this launch configuration type.
	 * This method does not return null.
	 * 
	 * @return the set of all supported modes
	 * @since 3.2
	 */
	public Set getSupportedModes();
}
