/*******************************************************************************
 *  Copyright (c) 2003, 2014 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

import org.eclipse.equinox.app.IApplication;

/**
 * Bootstrap type for the platform. Platform runnables represent executable
 * entry points into plug-ins. Runnables can be configured into the Platform's
 * <code>org.eclipse.core.runtime.applications</code> extension-point or be made
 * available through code or extensions on other plug-in's extension-points.
 *
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @since 3.0
 * @deprecated use {@link IApplication}
 * @noimplement This interface is not intended to be implemented by clients.
 * @noreference This interface is not intended to be referenced by clients.
 *
 *              This API is planned to be deleted see
 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=457134 for details
 */
@Deprecated
public interface IPlatformRunnable {

	/**
	 * Exit object indicating normal termination
	 */
	@Deprecated
	public static final Integer EXIT_OK = Integer.valueOf(0);

	/**
	 * Exit object requesting platform restart
	 */
	@Deprecated
	public static final Integer EXIT_RESTART = Integer.valueOf(23);

	/**
	 * Exit object requesting that the command passed back be executed.  Typically
	 * this is used to relaunch Eclipse with different command line arguments. When the executable is
	 * relaunched the command line will be retrieved from the <code>eclipse.exitdata</code> system property.
	 */
	@Deprecated
	public static final Integer EXIT_RELAUNCH = Integer.valueOf(24);

	/**
	 * Runs this runnable with the given args and returns a result.
	 * The content of the args is unchecked and should conform to the expectations of
	 * the runnable being invoked.  Typically this is a <code>String</code> array.
	 * Applications can return any object they like.  If an <code>Integer</code> is returned
	 * it is treated as the program exit code if Eclipse is exiting.
	 *
	 * @param args the argument(s) to pass to the application
	 * @return the return value of the application
	 * @exception Exception if there is a problem running this runnable.
	 * @see #EXIT_OK
	 * @see #EXIT_RESTART
	 * @see #EXIT_RELAUNCH
	 */
	@Deprecated
	public Object run(Object args) throws Exception;
}
