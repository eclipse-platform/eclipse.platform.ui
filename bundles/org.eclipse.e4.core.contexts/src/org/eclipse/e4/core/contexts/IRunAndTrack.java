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
package org.eclipse.e4.core.contexts;

/**
 * Extended version of a runnable that can be used with the
 * {@link IEclipseContext#runAndTrack(IRunAndTrack, Object[])} version gets more detailed
 * information on the change, such as the service name and the event type.
 */
public interface IRunAndTrack {

	/**
	 * Executes this runnable. The reason for the execution is provided in the
	 * <code>eventType</code> argument.
	 * @param event The event that occurred
	 * @return <code>true</code> to continue receiving notifications; <code>false</code> otherwise
	 */
	public boolean notify(ContextChangeEvent event);
}
