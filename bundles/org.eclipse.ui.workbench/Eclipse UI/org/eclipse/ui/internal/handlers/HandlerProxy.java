/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * <p>
 * A proxy for a handler that has been defined in XML. This delays the class
 * loading until the handler is really asked for information (besides the
 * priority or the command identifier). Asking a proxy for anything but the
 * attributes defined publicly in this class will cause the proxy to instantiate
 * the proxied handler.
 * </p>
 * 
 * @since 3.0
 */
public final class HandlerProxy extends AbstractHandler implements
		ILegacyHandlerWrapper {

	/**
	 * The configuration element from which the handler can be created. This
	 * value will exist until the element is converted into a real class -- at
	 * which point this value will be set to <code>null</code>.
	 */
	private IConfigurationElement configurationElement;

	/**
	 * The <code>enabledWhen</code> expression for the handler. Only if this
	 * expression evaluates to <code>true</code> (or the value is
	 * <code>null</code>) should we consult the handler.
	 */
	private final Expression enabledWhenExpression;

	/**
	 * The real handler. This value is <code>null</code> until the proxy is
	 * forced to load the real handler. At this point, the configuration element
	 * is converted, nulled out, and this handler gains a reference.
	 */
	private IHandler handler = null;

	/**
	 * The name of the configuration element attribute which contains the
	 * information necessary to instantiate the real handler.
	 */
	private final String handlerAttributeName;

	/**
	 * The handler service to use when evaluating
	 * <code>enabledWhenExpression</code>. This value may be
	 * <code>null</code> only if the <code>enabledWhenExpression</code> is
	 * <code>null</code>.
	 */
	private final IHandlerService handlerService;

	/**
	 * Constructs a new instance of <code>HandlerProxy</code> with all the
	 * information it needs to try to avoid loading until it is needed.
	 * 
	 * @param configurationElement
	 *            The configuration element from which the real class can be
	 *            loaded at run-time; must not be <code>null</code>.
	 * @param handlerAttributeName
	 *            The name of the attibute or element containing the handler
	 *            executable extension; must not be <code>null</code>.
	 */
	public HandlerProxy(final IConfigurationElement configurationElement,
			final String handlerAttributeName) {
		this(configurationElement, handlerAttributeName, null, null);
	}

	/**
	 * Constructs a new instance of <code>HandlerProxy</code> with all the
	 * information it needs to try to avoid loading until it is needed.
	 * 
	 * @param configurationElement
	 *            The configuration element from which the real class can be
	 *            loaded at run-time; must not be <code>null</code>.
	 * @param handlerAttributeName
	 *            The name of the attribute or element containing the handler
	 *            executable extension; must not be <code>null</code>.
	 * @param enabledWhenExpression
	 *            The name of the element containing the enabledWhen expression.
	 *            This should be a child of the
	 *            <code>configurationElement</code>. If this value is
	 *            <code>null</code>, then there is no enablement expression
	 *            (i.e., enablement will be delegated to the handler when
	 *            possible).
	 * @param handlerService
	 *            The handler service from which to get the current context when
	 *            trying to evaluate the <code>enabledWhenExpression</code>.
	 *            This value may be <code>null</code> only if the
	 *            <code>enabledWhenExpression</code> is <code>null</code>.
	 */
	public HandlerProxy(final IConfigurationElement configurationElement,
			final String handlerAttributeName,
			final Expression enabledWhenExpression,
			final IHandlerService handlerService) {
		if (configurationElement == null) {
			throw new NullPointerException(
					"The configuration element backing a handler proxy cannot be null"); //$NON-NLS-1$
		}

		if (handlerAttributeName == null) {
			throw new NullPointerException(
					"The attribute containing the handler class must be known"); //$NON-NLS-1$
		}

		if ((enabledWhenExpression != null) && (handlerService == null)) {
			throw new NullPointerException(
					"We must have a handler service to support the enabledWhen expression"); //$NON-NLS-1$
		}

		this.configurationElement = configurationElement;
		this.handlerAttributeName = handlerAttributeName;
		this.enabledWhenExpression = enabledWhenExpression;
		this.handlerService = handlerService;
	}

	/**
	 * Passes the dipose on to the proxied handler, if it has been loaded.
	 */
	public final void dispose() {
		if (handler != null) {
			handler.dispose();
		}
	}

	public final Object execute(final ExecutionEvent event)
			throws ExecutionException {
		if (loadHandler()) {
			return handler.execute(event);
		}

		return null;
	}

	public final String getClassName() {
		if (handler instanceof ILegacyHandlerWrapper) {
			final ILegacyHandlerWrapper wrapper = (ILegacyHandlerWrapper) handler;
			return wrapper.getClassName();
		}

		if (handler == null) {
			return configurationElement.getAttribute(handlerAttributeName);
		}

		return handler.getClass().getName();
	}

	public final boolean isEnabled() {
		if (enabledWhenExpression != null) {
			try {
				final EvaluationResult result = enabledWhenExpression
						.evaluate(handlerService.getCurrentState());
				if (result == EvaluationResult.TRUE) {
					if (loadHandler()) {
						return handler.isEnabled();
					}
				}
			} catch (final CoreException e) {
				// We will just fall through an let it return false.
				final String message = "An exception occurred while evaluating the enabledWhen expression for " //$NON-NLS-1$
						+ configurationElement
								.getAttribute(handlerAttributeName)
						+ "' could not be loaded"; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.WARNING,
						WorkbenchPlugin.PI_WORKBENCH, 0, e.getMessage(), e);
				WorkbenchPlugin.log(message, status);
			}

			return false;
		}

		/*
		 * There is no enabled when expression, so we just need to consult the
		 * handler.
		 */
		if (loadHandler()) {
			return handler.isEnabled();
		}
		return false;
	}

	public final boolean isHandled() {
		if (loadHandler())
			return handler.isHandled();

		return false;
	}

	/**
	 * Loads the handler, if possible. If the handler is loaded, then the member
	 * variables are updated accordingly.
	 * 
	 * @return <code>true</code> if the handler is now non-null;
	 *         <code>false</code> otherwise.
	 */
	private final boolean loadHandler() {
		if (handler == null) {
			// Load the handler.
			try {
				handler = (IHandler) configurationElement
						.createExecutableExtension(handlerAttributeName);
				configurationElement = null;
				return true;

			} catch (final ClassCastException e) {
				final String message = "The proxied handler was the wrong class"; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.ERROR,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
				WorkbenchPlugin.log(message, status);
				return false;

			} catch (final CoreException e) {
				final String message = "The proxied handler for '" + configurationElement.getAttribute(handlerAttributeName) //$NON-NLS-1$
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
		if (handler == null) {
			return configurationElement.getAttribute(handlerAttributeName);
		}

		return handler.toString();
	}
}
