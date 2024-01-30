/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 475689
 *******************************************************************************/
package org.eclipse.jface.commands;

import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.widgets.Event;

/**
 * <p>
 * This class adapts instances of <code>IAction</code> to <code>IHandler</code>.
 * </p>
 *
 * @since 3.1
 */
public final class ActionHandler extends AbstractHandler {

	/**
	 * The wrapped action. This value is never <code>null</code>.
	 */
	private final IAction action;

	/**
	 * The property change listener hooked on to the action. This is initialized
	 * when the first listener is attached to this handler, and is removed when
	 * the handler is disposed or the last listener is removed.
	 */
	private IPropertyChangeListener propertyChangeListener;

	/**
	 * Creates a new instance of this class given an instance of
	 * <code>IAction</code>.
	 *
	 * @param action
	 *            the action. Must not be <code>null</code>.
	 */
	public ActionHandler(final IAction action) {
		if (action == null) {
			throw new NullPointerException();
		}

		this.action = action;
	}

	@Override
	public final void addHandlerListener(final IHandlerListener handlerListener) {
		if (!hasListeners()) {
			attachListener();
		}

		super.addHandlerListener(handlerListener);
	}

	/**
	 * When a listener is attached to this handler, then this registers a
	 * listener with the underlying action.
	 *
	 * @since 3.1
	 */
	private final void attachListener() {
		if (propertyChangeListener == null) {
			propertyChangeListener = propertyChangeEvent -> {
				final String property = propertyChangeEvent.getProperty();
				fireHandlerChanged(new HandlerEvent(ActionHandler.this,
						IAction.ENABLED.equals(property),
						IAction.HANDLED.equals(property)));
			};
		}

		this.action.addPropertyChangeListener(propertyChangeListener);
	}

	/**
	 * When no more listeners are registered, then this is used to removed the
	 * property change listener from the underlying action.
	 */
	private final void detachListener() {
		this.action.removePropertyChangeListener(propertyChangeListener);
		propertyChangeListener = null;
	}

	/**
	 * Removes the property change listener from the action.
	 *
	 * @see org.eclipse.core.commands.IHandler#dispose()
	 */
	@Override
	public final void dispose() {
		if (hasListeners()) {
			action.removePropertyChangeListener(propertyChangeListener);
		}
	}

	@Override
	public final Object execute(final ExecutionEvent event)
			throws ExecutionException {
		if ((action.getStyle() == IAction.AS_CHECK_BOX)
				|| (action.getStyle() == IAction.AS_RADIO_BUTTON)) {
			action.setChecked(!action.isChecked());
		}
		final Object trigger = event.getTrigger();
		try {
			if (trigger instanceof Event) {
				action.runWithEvent((Event) trigger);
			} else {
				action.runWithEvent(new Event());
			}
		} catch (Exception e) {
			throw new ExecutionException(
					"While executing the action, an exception occurred", e); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Returns the action associated with this handler
	 *
	 * @return the action associated with this handler (not null)
	 * @since 3.1
	 */
	public final IAction getAction() {
		return action;
	}

	@Override
	public final boolean isEnabled() {
		return action.isEnabled();
	}

	@Override
	public final boolean isHandled() {
		return action.isHandled();
	}

	@Override
	public final void removeHandlerListener(final IHandlerListener handlerListener) {
		super.removeHandlerListener(handlerListener);

		if (!hasListeners()) {
			detachListener();
		}
	}

	@Override
	public final String toString() {
		final StringBuilder buffer = new StringBuilder();

		buffer.append("ActionHandler("); //$NON-NLS-1$
		buffer.append(action);
		buffer.append(')');

		return buffer.toString();
	}

	@Override
	public String getHandlerLabel() {
		return Optional.of(action).map(IAction::getText).map(LegacyActionTools::removeAcceleratorText).orElse(null);
	}
}
