/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandlerAttributes;

/**
 * This class is a partial implementation of <code>IHandler</code>. This
 * abstract implementation provides support for handler listeners. You should
 * subclass from this method unless you want to implement your own listener
 * support. Subclasses should call
 * {@link AbstractHandler#fireHandlerChanged(HandlerEvent)}when the handler
 * changes. Subclasses should also override
 * {@link AbstractHandler#getAttributeValuesByName()}if they have any
 * attributes.
 *
 * @since 3.0
 * @deprecated Please use the "org.eclipse.core.commands" plug-in instead. This
 *             API is scheduled for deletion, see Bug 431177 for details
 * @see org.eclipse.core.commands.AbstractHandler
 * @noreference This class is scheduled for deletion.
 * @noextend This class is not intended to be extended by clients.
 */
@Deprecated(forRemoval = true, since = "2024-03")
@SuppressWarnings({ "unchecked" })
public abstract class AbstractHandler extends org.eclipse.core.commands.AbstractHandler implements IHandler {

	/**
	 * Those interested in hearing about changes to this instance of
	 * <code>IHandler</code>. This member is null iff there are no listeners
	 * attached to this handler. (Most handlers don't have any listeners, and this
	 * optimization saves some memory.)
	 */
	private List<IHandlerListener> handlerListeners;

	/**
	 * @see IHandler#addHandlerListener(IHandlerListener)
	 */
	@Override
	@Deprecated
	public void addHandlerListener(IHandlerListener handlerListener) {
		if (handlerListener == null) {
			throw new NullPointerException();
		}
		if (handlerListeners == null) {
			handlerListeners = new ArrayList<>();
		}
		if (!handlerListeners.contains(handlerListener)) {
			handlerListeners.add(handlerListener);
		}
	}

	/**
	 * The default implementation does nothing. Subclasses who attach listeners to
	 * other objects are encouraged to detach them in this method.
	 *
	 * @see org.eclipse.ui.commands.IHandler#dispose()
	 */
	@Override
	@Deprecated
	public void dispose() {
		// Do nothing.
	}

	@Override
	@Deprecated
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		try {
			return execute(event.getParameters());
		} catch (final org.eclipse.ui.commands.ExecutionException e) {
			throw new ExecutionException(e.getMessage(), e.getCause());
		}
	}

	/**
	 * Fires an event to all registered listeners describing changes to this
	 * instance.
	 *
	 * @param handlerEvent the event describing changes to this instance. Must not
	 *                     be <code>null</code>.
	 */
	@Override
	@Deprecated
	protected void fireHandlerChanged(HandlerEvent handlerEvent) {
		super.fireHandlerChanged(handlerEvent);

		if (handlerListeners != null) {
			final boolean attributesChanged = handlerEvent.isEnabledChanged() || handlerEvent.isHandledChanged();
			final Map previousAttributes;
			if (attributesChanged) {
				previousAttributes = new HashMap();
				previousAttributes.putAll(getAttributeValuesByName());
				if (handlerEvent.isEnabledChanged()) {
					Boolean disabled = !isEnabled() ? Boolean.TRUE : Boolean.FALSE;
					previousAttributes.put("enabled", disabled); //$NON-NLS-1$
				}
				if (handlerEvent.isHandledChanged()) {
					Boolean notHandled = !isHandled() ? Boolean.TRUE : Boolean.FALSE;
					previousAttributes.put(IHandlerAttributes.ATTRIBUTE_HANDLED, notHandled);
				}
			} else {
				previousAttributes = null;
			}
			final org.eclipse.ui.commands.HandlerEvent legacyEvent = new org.eclipse.ui.commands.HandlerEvent(this,
					attributesChanged, previousAttributes);

			for (IHandlerListener handlerListener : handlerListeners) {
				handlerListener.handlerChanged(legacyEvent);
			}
		}
	}

	/**
	 * @see org.eclipse.core.commands.AbstractHandler
	 */
	@Deprecated
	protected void fireHandlerChanged(final org.eclipse.ui.commands.HandlerEvent handlerEvent) {
		if (handlerEvent == null) {
			throw new NullPointerException();
		}

		if (handlerListeners != null) {
			for (IHandlerListener handlerListener : handlerListeners) {
				handlerListener.handlerChanged(handlerEvent);
			}
		}

		if (super.hasListeners()) {
			final boolean enabledChanged;
			final boolean handledChanged;
			if (handlerEvent.haveAttributeValuesByNameChanged()) {
				Map previousAttributes = handlerEvent.getPreviousAttributeValuesByName();

				Object attribute = previousAttributes.get("enabled"); //$NON-NLS-1$
				if (attribute instanceof Boolean) {
					enabledChanged = ((Boolean) attribute).booleanValue();
				} else {
					enabledChanged = false;
				}

				attribute = previousAttributes.get(IHandlerAttributes.ATTRIBUTE_HANDLED);
				if (attribute instanceof Boolean) {
					handledChanged = ((Boolean) attribute).booleanValue();
				} else {
					handledChanged = false;
				}
			} else {
				enabledChanged = false;
				handledChanged = true;
			}
			final HandlerEvent newEvent = new HandlerEvent(this, enabledChanged, handledChanged);
			super.fireHandlerChanged(newEvent);
		}
	}

	/**
	 * This simply return an empty map. The default implementation has no
	 * attributes.
	 *
	 * @see IHandler#getAttributeValuesByName()
	 */
	@Override
	@Deprecated
	public Map getAttributeValuesByName() {
		return Collections.EMPTY_MAP;
	}

	/**
	 * Returns true iff there is one or more IHandlerListeners attached to this
	 * AbstractHandler.
	 *
	 * @return true iff there is one or more IHandlerListeners attached to this
	 *         AbstractHandler
	 * @since 3.1
	 */
	@Override
	@Deprecated
	protected final boolean hasListeners() {
		return super.hasListeners() || handlerListeners != null;
	}

	@Override
	@Deprecated
	public boolean isEnabled() {
		final Object handled = getAttributeValuesByName().get("enabled"); //$NON-NLS-1$
		if (handled instanceof Boolean) {
			return ((Boolean) handled).booleanValue();
		}

		return false;
	}

	@Override
	@Deprecated
	public boolean isHandled() {
		final Object handled = getAttributeValuesByName().get(IHandlerAttributes.ATTRIBUTE_HANDLED);
		if (handled instanceof Boolean) {
			return ((Boolean) handled).booleanValue();
		}

		return false;
	}

	/**
	 * @see IHandler#removeHandlerListener(IHandlerListener)
	 */
	@Override
	@Deprecated
	public void removeHandlerListener(IHandlerListener handlerListener) {
		if (handlerListener == null) {
			throw new NullPointerException();
		}
		if (handlerListeners == null) {
			return;
		}

		if (handlerListeners != null) {
			handlerListeners.remove(handlerListener);
		}
		if (handlerListeners.isEmpty()) {
			handlerListeners = null;
		}
	}
}
