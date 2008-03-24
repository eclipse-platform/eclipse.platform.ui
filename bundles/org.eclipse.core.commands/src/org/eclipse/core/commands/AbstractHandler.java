/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
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
public abstract class AbstractHandler extends EventManager implements IHandler2 {

	/**
	 * Track this base class enabled state.
	 * 
	 * @since 3.4
	 */
	private boolean baseEnabled = true;

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
		if (handlerEvent == null) {
			throw new NullPointerException();
		}

		final Object[] listeners = getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IHandlerListener listener = (IHandlerListener) listeners[i];
			listener.handlerChanged(handlerEvent);
		}
	}

	/**
	 * Whether this handler is capable of executing at this time. Subclasses may
	 * override this method. If clients override this method they should also
	 * consider overriding {@link #setEnabled(Object)} so they can be notified
	 * about framework execution contexts.
	 * 
	 * @return <code>true</code>
	 * @see #setEnabled(Object)
	 * @see #setBaseEnabled(boolean)
	 */
	public boolean isEnabled() {
		return baseEnabled;
	}

	/**
	 * Allow the default {@link #isEnabled()} to answer our enabled state. It
	 * will fire a HandlerEvent if necessary. If clients use this method they
	 * should also consider overriding {@link #setEnabled(Object)} so they can
	 * be notified about framework execution contexts.
	 * 
	 * @param state
	 *            the enabled state
	 * @since 3.4
	 */
	protected void setBaseEnabled(boolean state) {
		if (baseEnabled == state) {
			return;
		}
		baseEnabled = state;
		fireHandlerChanged(new HandlerEvent(this, true, false));
	}

	/**
	 * Called by the framework to allow the handler to update its enabled state
	 * by extracting the same information available at execution time. Clients
	 * may override if they need to extract information from the application
	 * context.
	 * 
	 * @param evaluationContext
	 *            the application context. May be <code>null</code>
	 * @since 3.4
	 * @see #setBaseEnabled(boolean)
	 */
	public void setEnabled(Object evaluationContext) {
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
