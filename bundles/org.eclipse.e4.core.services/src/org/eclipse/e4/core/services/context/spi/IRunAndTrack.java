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
 * {@link IEclipseContext#runAndTrack(Runnable, String)}. This version gets more
 * detailed information on the change, such as the service name and the event
 * type.
 */
public interface IRunAndTrack {

	/**
	 * Initial direct call to @
	 * {@link IEclipseContext#runAndTrack(Runnable, String)}.
	 */
	public int INITIAL = 0;

	/**
	 * Element has been added to the context.
	 */
	public int ADDED = 1;

	/**
	 * Element has been removed from the context.
	 */
	public int REMOVED = 2;

	/**
	 * Context is being disposed.
	 */
	public int DISPOSE = 3;

	/**
	 * The trackable operation is notified of a context change.
	 * 
	 * @param name
	 *            name of the service changed
	 * @param eventType
	 *            describes type of the change, see {@link #ADDED},
	 *            {@link #REMOVED}
	 */
	public boolean notify(IEclipseContext context, String name, int eventType,
			Object[] args);
}
