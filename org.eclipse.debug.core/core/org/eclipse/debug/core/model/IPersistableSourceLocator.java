package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * A source locator that can be persisted and restored,
 * to be used with a specfic launch configuration.
 * The debug plug-in defines source locator extension
 * point for persistable source locators.
 * <p>
 * A source locator extension is defined in <code>plugin.xml</code>.
 * Following is an example definition of a source locator extension.
 * <pre>
 * &lt;extension point="org.eclipse.debug.core.sourceLocators"&gt;
 *   &lt;sourceLocator 
 *      id="com.example.ExampleIdentifier"
 *      class="com.example.ExampleSourceLocator"
 *      name="Example Source Locator"
 *   &lt;/sourceLocator&gt;
 * &lt;/extension&gt;
 * </pre>
 * The attributes are specified as follows:
 * <ul>
 * <li><code>id</code> specifies a unique identifier for this source locator.</li>
 * <li><code>class</code> specifies the fully qualified name of the Java class
 *   that implements <code>IPersistableSourceLocator</code>.</li>
 * <li><code>name</code> a human readable name, describing the type of
 *   this source locator.</li>
 * </ul>
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see org.eclipse.debug.core.ILaunch
 * @see IStackFrame 
 * @see org.eclipse.debug.ui.IDebugModelPresentation
 * @since 2.0
 */
public interface IPersistableSourceLocator extends ISourceLocator {
	
	/**
	 * Returns a memento that can be used to reconstruct
	 * this source locator
	 * 
	 * @return a memento that can be used to reconstruct
	 *  this source locator
	 * @exception CoreException if unable to construct a memento
	 */
	public String getMemento() throws CoreException;
	
	/**
	 * Initializes this source locator based on the given
	 * memento.
	 * 
	 * @param memento a memento to initialize this source locator
	 * @exception CoreException on failure to initialize 
	 */
	public void initiatlizeFromMemento(String memento) throws CoreException;
	
	/**
	 * Initializes this source locator to perform default
	 * source lookup for the given launch configuration.
	 * 
	 * @param configuration launch configuration this source locator
	 *  will be performing souce lookup for
	 * @exception CoreException on failure to initialize
	 */
	public void initializeDefaults(ILaunchConfiguration configuration) throws CoreException;

}


