/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.context;

/**
 * An event describing a change to an {@link IEclipseContext}. The following types of events are
 * currently defined:
 * <ul>
 * <li>An event indicating that the listener has just been registered with a context.</li>
 * <li>An after-the-fact report that a context value has been changed (either a new value or a
 * change to an existing value.</li>
 * <li>An after-the-fact report that a context value has been removed.</li>
 * <li>A report that a context is about to be disposed.</li>
 * </ul>
 * <p>
 * In order to handle additional event types that may be introduced in future releases, clients
 * should not write code that presumes the set of event types is closed.
 * </p>
 * 
 * @see IRunAndTrack
 */
public final class ContextChangeEvent {
	/**
	 * A change event type (value "0"), indicating that the listener receiving this event has just
	 * been registered with a context.
	 * 
	 * @see IEclipseContext#runAndTrack(IRunAndTrack, Object[])
	 */
	public static final int INITIAL = 0;

	/**
	 * A change event type (value "1"), indicating that a context value has been added.
	 */
	public static final int ADDED = 1;

	/**
	 * A change event type (value "2"), indicating that a context value has been removed.
	 */
	public static final int REMOVED = 2;

	/**
	 * A change event type (value "3") indicating that the context is being disposed. The context is
	 * still valid at the time of this event.
	 */
	public static final int DISPOSE = 3;

	private Object[] args;
	private IEclipseContext context;
	private int eventType;
	private String key;

	private Object oldValue;

	/**
	 * Creates a new context event.
	 * 
	 * @param context
	 * @param eventType
	 * @param args
	 * @param name
	 */
	ContextChangeEvent(IEclipseContext context, int eventType, Object[] args, String name,
			Object oldValue) {
		this.context = context;
		this.key = name;
		this.eventType = eventType;
		this.args = args;
		this.oldValue = oldValue;
	}

	/**
	 * Returns the arguments that were supplied when the listener was registered.
	 * 
	 * @see IEclipseContext#runAndTrack(IRunAndTrack, Object[])
	 * @return the arguments that were supplied when the listener was registered.
	 */
	public Object[] getArguments() {
		return args;
	}

	/**
	 * Returns the context where the change occurred.
	 * 
	 * @return the context where the change occurred
	 */
	public IEclipseContext getContext() {
		return context;
	}

	/**
	 * Returns the type of content change that occurred.
	 * 
	 * @return the type of content change that occurred.
	 */
	public int getEventType() {
		return eventType;
	}

	/**
	 * Returns the name of the context value that changed, or <code>null</code> if not applicable if
	 * this event type.
	 * 
	 * @return The name of the changed context value, or <code>null</code>
	 */
	public String getName() {
		return key;
	}

	public Object getOldValue() {
		return oldValue;
	}
}