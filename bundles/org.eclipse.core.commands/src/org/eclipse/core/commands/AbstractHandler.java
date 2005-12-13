/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.commands;

import org.eclipse.core.commands.common.EventManager;

/**
 * <p>
 * This class is a partial implementation of <code>IHandler</code>. This
 * abstract implementation provides support for handler listeners. You should
 * subclass from this method unless you want to implement your own listener
 * support. Subclasses should call
 * {@link AbstractHandler#fireHandlerChanged(HandlerEvent)}when the handler
 * changes. Subclasses can also override {@link AbstractHandler#isEnabled()} and
 * {@link AbstractHandler#isHandled()}.
 * </p>
 * 
 * @since 3.1
 */
public abstract class AbstractHandler extends EventManager implements IHandler {

	/**
	 * @see IHandler#addHandlerListener(IHandlerListener)
	 */
	public void addHandlerListener(final IHandlerListener handlerListener) {
		addListenerObject(handlerListener);
	}

	/**
	 * The default implementation does nothing. Subclasses who attach listeners
	 * to other objects are encouraged to detach them in this method.
	 * 
	 * @see org.eclipse.core.commands.IHandler#dispose()
	 */
	public void dispose() {
		// Do nothing.
	}

	/**
	 * Fires an event to all registered listeners describing changes to this
	 * instance.
	 * <p>
	 * Subclasses may extend the definition of this method (i.e., if a different
	 * type of listener can be attached to a subclass). This is used primarily
	 * for support of <code>AbstractHandler</code> in
	 * <code>org.eclipse.ui.workbench</code>, and clients should be wary of
	 * overriding this behaviour. If this method is overridden, then the first
	 * line of the method should be "<code>super.fireHandlerChanged(handlerEvent);</code>".
	 * </p>
	 * 
	 * @param handlerEvent
	 *            the event describing changes to this instance. Must not be
	 *            <code>null</code>.
	 */
	protected void fireHandlerChanged(final HandlerEvent handlerEvent) {
		if (handlerEvent == null)
			throw new NullPointerException();

		final Object[] listeners = getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IHandlerListener listener = (IHandlerListener) listeners[i];
			listener.handlerChanged(handlerEvent);
		}
	}

	/**
	 * Whether this handler is capable of executing at this time. Subclasses may
	 * override this method.
	 * 
	 * @return <code>true</code>
	 */
	public boolean isEnabled() {
		return true;
	}

	/**
	 * Whether this handler is capable of handling delegated responsibilities at
	 * this time. Subclasses may override this method.
	 * 
	 * @return <code>true</code>
	 */
	public boolean isHandled() {
		return true;
	}

	/**
	 * <p>
	 * Returns true iff there is one or more IHandlerListeners attached to this
	 * AbstractHandler.
	 * </p>
	 * <p>
	 * Subclasses may extend the definition of this method (i.e., if a different
	 * type of listener can be attached to a subclass). This is used primarily
	 * for support of <code>AbstractHandler</code> in
	 * <code>org.eclipse.ui.workbench</code>, and clients should be wary of
	 * overriding this behaviour. If this method is overridden, then the return
	 * value should include "<code>super.hasListeners() ||</code>".
	 * </p>
	 * 
	 * @return true iff there is one or more IHandlerListeners attached to this
	 *         AbstractHandler
	 */
	protected boolean hasListeners() {
		return isListenerAttached();
	}

	/**
	 * @see IHandler#removeHandlerListener(IHandlerListener)
	 */
	public void removeHandlerListener(final IHandlerListener handlerListener) {
		removeListenerObject(handlerListener);
	}
}
