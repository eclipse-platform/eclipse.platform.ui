package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;

/**
 * A debug element represents an artifact in a program being
 * debugged.
 * <p>
 * Some methods on debug elements require communication
 * with the target program. Such methods may throw a <code>DebugException</code>
 * with a status code of <code>TARGET_REQUEST_FAILED</code>
 * when unable to complete a request due to a failure on the target.
 * Methods that require communication with the target program or require
 * the target to be in a specific state (for example, suspended), are declared
 * as such.
 * </p>
 * <p>
 * Debug elements are language independent. However, language specific
 * features can be made available via the adapter mechanism provided by
 * <code>IAdaptable</code>, or by extending the debug element interfaces.
 * A debug model is responsible for declaring any special adapters 
 * its debug elements implement.
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 */
public interface IDebugElement extends IAdaptable {
	
	/**
	 * Returns the unique identifier of the plug-in
	 * this debug element originated from.
	 *
	 * @return plug-in identifier
	 */
	public String getModelIdentifier();
	/**
	 * Returns the debug target this element is contained in.
	 * 
	 * @return debug target
	 */
	public IDebugTarget getDebugTarget();
	/**
	 * Returns the launch this target is contained in,
	 * or <code>null</code> if not yet registered with
	 * a launch.
	 * <p>
	 * [Issue: Specification will be changed to not allow null
	 *  to be returned.]
	 * </p>
	 * @return launch or <code>null</code>
	 */
	public ILaunch getLaunch();
}


