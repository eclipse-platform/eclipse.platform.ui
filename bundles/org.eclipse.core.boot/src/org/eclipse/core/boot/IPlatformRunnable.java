package org.eclipse.core.boot;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
 * Runs this runnable with the given args and returns a result.
 * The content of the args is unchecked and should conform to the expectations of
 * the runnable being invoked.  Typically this is a <code>String<code> array.
 * 
 * @exception Exception if there is a problem running this runnable.
 */
public Object run(Object args) throws Exception;
}
