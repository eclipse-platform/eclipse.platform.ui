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
	Object[] getChildren();
	/**
	 * Returns the debug target associated with this launch, or <code>null</code>
	 * if no debug target is associated with this launch.
	 *
	 * @return the debug target associated with this launch, or <code>null</code>
	 */
	IDebugTarget getDebugTarget();
	/**
	 * Returns the object that was launched. Cannot return <code>null</code>.
	 * 
	 * @return the launched object
	 */
	Object getElement();
	/**
	 * Returns the launcher that was used to launch. Cannot return <code>null</code>.
	 *
	 * @return the launcher
	 */
	ILauncher getLauncher();
	/**
	 * Returns the processes that were launched,
	 * or an empty collection if no processes were launched.
	 *
	 * @return array of processes
	 */
	IProcess[] getProcesses();
	/**
	 * Returns the source locator to use for locating source elements for
	 * the debug target associated with this launch, or <code>null</code>
	 * if source mapping is not supported.
	 *
	 * @return the source locator
	 */
	ISourceLocator getSourceLocator();
	/**
	 * Returns the mode of this launch - one of the mode constants defined by
	 * the launch manager.
	 *
	 * @return the launch mode
	 * @see ILaunchManager
	 */
	public String getLaunchMode();

}
