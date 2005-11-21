/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandlerWithState;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IState;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.CommandLegacyActionWrapper;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.commands.CommandImageManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * <p>
 * This proxies an {@link IActionDelegate} so that it can impersonate an
 * {@link IHandler}.
 * </p>
 * <p>
 * Clients may instantiate this class, but must not extend.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public final class ActionDelegateHandlerProxy extends AbstractHandlerWithState {

	/**
	 * A fake action that proxies all of the command-based services. This value
	 * is never <code>null</code>.
	 */
	private final CommandLegacyActionWrapper action;

	/**
	 * The real handler. This value is <code>null</code> until the proxy is
	 * forced to load the real handler. At this point, the configuration element
	 * is converted, nulled out, and this handler gains a reference.
	 */
	private IActionDelegate delegate = null;

	/**
	 * The name of the configuration element attribute which contains the
	 * information necessary to instantiate the real handler.
	 */
	private final String delegateAttributeName;

	/**
	 * The configuration element from which the handler can be created. This
	 * value will exist until the element is converted into a real class -- at
	 * which point this value will be set to <code>null</code>.
	 */
	private IConfigurationElement element;

	/**
	 * The <code>enabledWhen</code> expression for the handler. Only if this
	 * expression evaluates to <code>true</code> (or the value is
	 * <code>null</code>) should we consult the handler.
	 */
	private final Expression enabledWhenExpression;

	/**
	 * The handler service to use when evaluating
	 * <code>enabledWhenExpression</code>. This value may be
	 * <code>null</code> only if the <code>enabledWhenExpression</code> is
	 * <code>null</code>.
	 */
	private final IHandlerService handlerService;

	/**
	 * Constructs a new instance of <code>ActionDelegateHandlerProxy</code>
	 * with all the information it needs to try to avoid loading until it is
	 * needed.
	 * 
	 * @param element
	 *            The configuration element from which the real class can be
	 *            loaded at run-time; must not be <code>null</code>.
	 * @param delegateAttributeName
	 *            The name of the attibute or element containing the action
	 *            delegate; must not be <code>null</code>.
	 * @param actionId
	 *            The identifier of the underlying action; may be
	 *            <code>null</code>.
	 * @param command
	 *            The command with which the action delegate will be associated;
	 *            must not be <code>null</code>.
	 * @param commandManager
	 *            The manager providing commands for this action delegate; must
	 *            not be <code>null</code>.
	 * @param handlerService
	 *            The handler service from which to get the current context when
	 *            trying to evaluate the <code>enabledWhenExpression</code>.
	 *            This value may be <code>null</code> only if the
	 *            <code>enabledWhenExpression</code> is <code>null</code>.
	 * @param bindingManager
	 *            The binding manager providing accelerators for this action
	 *            delegate; must not be <code>null</code>.
	 * @param commandImageManager
	 *            The image manager providing icons for the action delegate;
	 *            must not be <code>null</code>.
	 * @param style
	 *            The image style with which the icons are associated; may be
	 *            <code>null</code>.
	 * @param enabledWhenExpression
	 *            The name of the element containing the enabledWhen expression.
	 *            This should be a child of the
	 *            <code>configurationElement</code>. If this value is
	 *            <code>null</code>, then there is no enablement expression
	 *            (i.e., enablement will be delegated to the handler when
	 *            possible).
	 */
	public ActionDelegateHandlerProxy(final IConfigurationElement element,
			final String delegateAttributeName, final String actionId,
			final ParameterizedCommand command,
			final CommandManager commandManager,
			final IHandlerService handlerService,
			final BindingManager bindingManager,
			final CommandImageManager commandImageManager, final String style,
			final Expression enabledWhenExpression) {
		if (element == null) {
			throw new NullPointerException(
					"The configuration element backing a handler proxy cannot be null"); //$NON-NLS-1$
		}

		if (delegateAttributeName == null) {
			throw new NullPointerException(
					"The attribute containing the action delegate must be known"); //$NON-NLS-1$
		}

		this.element = element;
		this.enabledWhenExpression = enabledWhenExpression;
		this.delegateAttributeName = delegateAttributeName;
		this.handlerService = handlerService;
		this.action = new CommandLegacyActionWrapper(actionId, command,
				commandManager, bindingManager, commandImageManager, style);
		this.action.addPropertyChangeListener(new IPropertyChangeListener() {
			public final void propertyChange(final PropertyChangeEvent event) {
				// TODO Update the state somehow.
			}

		});
	}

	public final Object execute(final ExecutionEvent event) {
		if (loadDelegate()) {
			final Object trigger = event.getTrigger();
			if ((delegate instanceof IActionDelegate2)
					&& (trigger instanceof Event)) {
				final IActionDelegate2 delegate2 = (IActionDelegate2) delegate;
				final Event triggeringEvent = (Event) trigger;
				delegate2.runWithEvent(action, triggeringEvent);
			} else {
				delegate.run(action);
			}
		}

		return null;
	}

	public final void handleStateChange(final IState state,
			final Object oldValue) {
		// TODO What should we do here?
	}

	public final boolean isEnabled() {
		if (enabledWhenExpression != null) {
			try {
				final EvaluationResult result = enabledWhenExpression
						.evaluate(handlerService.getCurrentState());
				if (result == EvaluationResult.TRUE) {
					return action.isEnabled();
				}
			} catch (final CoreException e) {
				// We will just fall through an let it return false.
				final String message = "An exception occurred while evaluating the enabledWhen expression for " //$NON-NLS-1$
						+ element.getAttribute(delegateAttributeName)
						+ "' could not be loaded"; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.WARNING,
						WorkbenchPlugin.PI_WORKBENCH, 0, e.getMessage(), e);
				WorkbenchPlugin.log(message, status);
			}

			return false;
		}

		/*
		 * There is no enabled when expression, so we just need to consult the
		 * action.
		 */
		return action.isEnabled();
	}

	/**
	 * Loads the delegate, if possible. If the delegate is loaded, then the
	 * member variables are updated accordingly.
	 * 
	 * @return <code>true</code> if the delegate is now non-null;
	 *         <code>false</code> otherwise.
	 */
	private final boolean loadDelegate() {
		if (delegate == null) {
			// Load the handler.
			try {
				delegate = (IActionDelegate) element
						.createExecutableExtension(delegateAttributeName);
				element = null;
				return true;

			} catch (final ClassCastException e) {
				final String message = "The proxied delegate was the wrong class"; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.ERROR,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
				WorkbenchPlugin.log(message, status);
				return false;

			} catch (final CoreException e) {
				final String message = "The proxied delegate for '" //$NON-NLS-1$
						+ element.getAttribute(delegateAttributeName)
						+ "' could not be loaded"; //$NON-NLS-1$
				IStatus status = new Status(IStatus.ERROR,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
				WorkbenchPlugin.log(message, status);
				return false;
			}
		}

		return true;
	}

	public final String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("ActionDelegateHandlerProxy("); //$NON-NLS-1$
		if (element == null) {
			buffer.append(delegate);
		} else {
			final String className = element
					.getAttribute(delegateAttributeName);
			buffer.append(className);
		}
		buffer.append(')');
		return buffer.toString();
	}
}
