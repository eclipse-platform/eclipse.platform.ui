package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.model.*;
import org.eclipse.core.runtime.IAdaptable;

/**
 * A launch is the result of launching a debug session
 * and/or one or more system processes.
 * <p>
 * This interface is not intended to be implemented by clients. Clients
 * should create instances of this interface by using the implementation
 * provided by the class <code>Launch</code>.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see Launch
 */
public interface ILaunch extends ITerminate, IAdaptable {
	/**
	 * Returns the children of this launch - at least one of a debug target
	 * and/or one or more processes.
	 *
	 * @return an array (element type:<code>IDebugTarget</code> or <code>IProcess</code>)
	 */
	public Object[] getChildren();
	/**
	 * Returns the primary (first) debug target associated with this launch, or <code>null</code>
	 * if no debug target is associated with this launch. All debug targets 
	 * associated with this launch may be retrieved by
	 * <code>getDebugTargets()</code>.
	 *
	 * @return the primary debug target associated with this launch, or <code>null</code>
	 */
	public IDebugTarget getDebugTarget();
	/**
	 * Returns the object that was launched. Cannot return <code>null</code>.
	 * 
	 * @return the launched object
	 */
	public Object getElement();
	/**
	 * Returns the launcher that was used to launch.
	 * Returns <code>null</code> if this launch was 
	 * the result of a launch configuration being
	 * launched.
	 *
	 * @return the launcher, or <code>null</code>
	 * @deprecated to be removed
	 */
	public ILauncher getLauncher();
	/**
	 * Returns the processes that were launched,
	 * or an empty collection if no processes were launched.
	 *
	 * @return array of processes
	 */
	public IProcess[] getProcesses();
	
	/**
	 * Returns all the debug targets associatd with this launch,
	 * or an empty collection if no debug targets are associated
	 * with this launch. The primary debug target is the first
	 * in the collection (if any).
	 *
	 * @return array of debug targets
	 * @since 2.0
	 */
	public IDebugTarget[] getDebugTargets();
	
	/**
	 * Adds the given debug target to this launch. Has no effect
	 * if the given debug target is already associated with this
	 * launch.
	 *
	 * @param target debug target to add to this launch
	 * @since 2.0
	 */
	public void addDebugTarget(IDebugTarget target);	
	
	/**
	 * Adds the given process to this launch. Has no effect
	 * if the given process is already associated with this
	 * launch.
	 *
	 * @param process the process to add to this launch
	 * @since 2.0
	 */
	public void addProcess(IProcess process);		
		
	/**
	 * Returns the source locator to use for locating source elements for
	 * the debug target associated with this launch, or <code>null</code>
	 * if source lookup is not supported.
	 *
	 * @return the source locator
	 */
	public ISourceLocator getSourceLocator();
	
	/**
	 * Sets the source locator to use for locating source elements for
	 * the debug target associated with this launch, or <code>null</code>
	 * if source lookup is not supported.
	 *
	 * @param sourceLocator source locator or <code>null</code>
	 * @since 2.0
	 */
	public void setSourceLocator(ISourceLocator sourceLocator);
		
	/**
	 * Returns the mode of this launch - one of the mode constants defined by
	 * the launch manager.
	 *
	 * @return the launch mode
	 * @see ILaunchManager
	 */
	public String getLaunchMode();
	
	/**
	 * Returns the configuration that was launched, or <code>null</code>
	 * if no configration was launched.
	 * 
	 * @return the launched configuration or <code>null</code>
	 * @since 2.0
	 */
	public ILaunchConfiguration getLaunchConfiguration();
	
	/**
	 * Sets the value of a client defined attribute.
	 *
	 * @param key the attribute key
	 * @param value the attribute value
	 * @since 2.0
	 */
	public void setAttribute(String key, String value);
	
	/**
	 * Returns the value of a client defined attribute.
	 *
	 * @param key the attribute key
	 * @return value the attribute value, or <code>null</code> if undefined
	 * @since 2.0
	 */
	public String getAttribute(String key);	
	
	/**
	 * Returns whether this launch contains at least one process
	 * or debug target.
	 * 
	 * @return whether this launch contains at least one process
	 * or debug target
	 * @since 2.0
	 */
	public boolean hasChildren();

}
