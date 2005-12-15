/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.commands;

import java.util.Collections;
import java.util.Map;

/**
 * <p>
 * The data object to pass to the command (and its handler) as it executes. This
 * carries information about the current state of the application, and the
 * application context in which the command was executed.
 * </p>
 * <p>
 * An execution event carries three blocks of data: the parameters, the trigger,
 * and the application context. How these blocks are used is application
 * dependent. In the Eclipse workbench, the trigger is an SWT event, and the
 * application context contains information about the selection and active part.
 * </p>
 * 
 * @since 3.1
 */
public final class ExecutionEvent {

	/**
	 * The state of the application at the time the execution was triggered. In
	 * the Eclipse workbench, this might contain information about the active
	 * part of the active selection (for example). This value may be
	 * <code>null</code>.
	 */
	private final Object applicationContext;

	/**
	 * The parameters to qualify the execution. For handlers that normally
	 * prompt for additional information, these can be used to avoid prompting.
	 * This value may be empty, but it is never <code>null</code>.
	 */
	private final Map parameters;

	/**
	 * The object that triggered the execution. In an event-driven architecture,
	 * this is typically just another event. In the Eclipse workbench, this is
	 * typically an SWT event. This value may be <code>null</code>.
	 */
	private final Object trigger;
	
	/**
	 * Constructs a new instance of <code>ExecutionEvent</code> with no
	 * parameters, no trigger and no application context.  This is just a
	 * convenience method.
	 *
	 * @since 3.2
	 */
	public ExecutionEvent() {
		this(Collections.EMPTY_MAP, null, null);
	}

	/**
     * Constructs a new instance of <code>ExecutionEvent</code>.
     * 
     * @param parameters
     *            The parameters to qualify the execution; must not be
     *            <code>null</code>. This must be a map of parameter ids (<code>String</code>)
     *            to parameter values (<code>String</code>).
     * @param trigger
     *            The object that triggered the execution; may be
     *            <code>null</code>.
     * @param applicationContext
     *            The state of the application at the time the execution was
     *            triggered; may be <code>null</code>.
     */
	public ExecutionEvent(final Map parameters, final Object trigger,
			final Object applicationContext) {
		if (parameters == null) {
			throw new NullPointerException(
					"An execution event must have a non-null map of parameters"); //$NON-NLS-1$
		}

		this.parameters = parameters;
		this.trigger = trigger;
		this.applicationContext = applicationContext;
	}

	/**
	 * Returns the state of the application at the time the execution was
	 * triggered.
	 * 
	 * @return The application context; may be <code>null</code>.
	 */
	public final Object getApplicationContext() {
		return applicationContext;
	}

	/**
	 * Returns the value of the parameter with the given id.
	 * 
	 * @param parameterId
	 *            The id of the parameter to retrieve; may be
	 *            <code>null</code>.
	 * @return The parameter value; <code>null</code> if the parameter cannot
	 *         be found.
	 */
	public final String getParameter(final String parameterId) {
		return (String) parameters.get(parameterId);
	}

	/**
	 * Returns all of the parameters.
	 * 
	 * @return The parameters; never <code>null</code>, but may be empty.
	 */
	public final Map getParameters() {
		return parameters;
	}

	/**
	 * Returns the object that triggered the execution
	 * 
	 * @return The trigger; <code>null</code> if there was no trigger.
	 */
	public final Object getTrigger() {
		return trigger;
	}
}
