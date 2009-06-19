/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.core.services.context.spi;

import org.eclipse.e4.core.services.context.IEclipseContext;

/**
 * Extended version of a runnable that can be used with the
 * {@link IEclipseContext#runAndTrack(IRunAndTrack, Object[])} version gets more
 * detailed information on the change, such as the service name and the event
 * type.
 */
public interface IRunAndTrack {

	/**
	 * A change event type (value "0"), indicating that this runnable has just
	 * been registered with a context.
	 * 
	 * @see IEclipseContext#runAndTrack(IRunAndTrack, Object[])
	 */
	public int INITIAL = 0;

	/**
	 * A change event type (value "1"), indicating that a context value has been
	 * added.
	 */
	public int ADDED = 1;

	/**
	 * A change event type (value "2"), indicating that a context value has been
	 * removed.
	 */
	public int REMOVED = 2;

	/**
	 * A change event type (value "3") indicating that the context is being
	 * disposed.
	 */
	public int DISPOSE = 3;

	/**
	 * Executes this runnable. The reason for the execution is provided in the
	 * <code>eventType</code> argument.
	 * 
	 * @param context
	 *            The context that triggered this notification
	 * @param name
	 *            name of the context value that changed
	 * @param eventType
	 *            describes the type of change, one of {@link #INITIAL},
	 *            {@link #ADDED}, {@link #REMOVED}, or {@link #DISPOSE}
	 * @param args
	 *            The arguments that were supplied when this runnable was
	 *            registered with the context
	 */
	public boolean notify(IEclipseContext context, String name, int eventType, Object[] args);
}
