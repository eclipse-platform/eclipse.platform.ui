/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.boot;

/**
 * Bootstrap type for the platform. Platform runnables represent executable 
 * entry points into plug-ins.  Runnables can be configured into the Platform's
 * <code>org.eclipse.core.runtime.applications</code> extension-point 
 * or be made available through code or extensions on other plug-in's extension-points.
 *
 * <p>
 * Clients may implement this interface.
 * </p>
 */
public interface IPlatformRunnable {
	
/**
 * Exit object indicating normal termination
 */
public static final Integer EXIT_OK = new Integer(0);

/**
 * Exit object requesting platform restart
 */
public static final Integer EXIT_RESTART = new Integer(23);
	
/**
 * Runs this runnable with the given args and returns a result.
 * The content of the args is unchecked and should conform to the expectations of
 * the runnable being invoked.  Typically this is a <code>String<code> array.
 * 
 * @exception Exception if there is a problem running this runnable.
 */
public Object run(Object args) throws Exception;
}
