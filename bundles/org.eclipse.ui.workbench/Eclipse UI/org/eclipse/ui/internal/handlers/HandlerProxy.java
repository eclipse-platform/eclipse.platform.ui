/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.handlers;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.services.IEvaluationReference;
import org.eclipse.ui.internal.services.IEvaluationService;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.menus.UIElement;

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
		IElementUpdater {

	/**
	 * 
	 */
	private static final String PROP_ENABLED = "enabled"; //$NON-NLS-1$

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

	private IHandlerListener handlerListener;

	/**
	 * The evaluation service to use when evaluating
	 * <code>enabledWhenExpression</code>. This value may be
	 * <code>null</code> only if the <code>enabledWhenExpression</code> is
	 * <code>null</code>.
	 */
	private IEvaluationService evaluationService;

	private IPropertyChangeListener enablementListener;

	private IEvaluationReference enablementRef;

	private boolean proxyEnabled;

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
	 * @param evaluationService
	 *            The evaluation service to manage enabledWhen expressions
	 *            trying to evaluate the <code>enabledWhenExpression</code>.
	 *            This value may be <code>null</code> only if the
	 *            <code>enabledWhenExpression</code> is <code>null</code>.
	 */
	public HandlerProxy(final IConfigurationElement configurationElement,
			final String handlerAttributeName,
			final Expression enabledWhenExpression,
			final IEvaluationService evaluationService) {
		if (configurationElement == null) {
			throw new NullPointerException(
					"The configuration element backing a handler proxy cannot be null"); //$NON-NLS-1$
		}

		if (handlerAttributeName == null) {
			throw new NullPointerException(
					"The attribute containing the handler class must be known"); //$NON-NLS-1$
		}

		if ((enabledWhenExpression != null) && (evaluationService == null)) {
			throw new NullPointerException(
					"We must have a handler service and evaluation service to support the enabledWhen expression"); //$NON-NLS-1$
		}

		this.configurationElement = configurationElement;
		this.handlerAttributeName = handlerAttributeName;
		this.enabledWhenExpression = enabledWhenExpression;
		this.evaluationService = evaluationService;
		if (enabledWhenExpression != null) {
			setProxyEnabled(false);
			registerEnablement();
		} else {
			setProxyEnabled(true);
		}
	}

	/**
	 * 
	 */
	private void registerEnablement() {
		enablementRef = evaluationService.addEvaluationListener(
				enabledWhenExpression, getEnablementListener(), PROP_ENABLED,
				null);
	}

	void setEnabledFor(IEvaluationContext context) throws ExecutionException {
		if (enabledWhenExpression != null) {
			try {
				setProxyEnabled(enabledWhenExpression.evaluate(context) == EvaluationResult.TRUE);
			} catch (CoreException e) {
				throw new ExecutionException(e.getMessage(), e);
			}
		}
	}

	void setProxyEnabled(boolean enabled) {
		proxyEnabled = enabled;
	}

	boolean getProxyEnabled() {
		return proxyEnabled;
	}

	/**
	 * @return
	 */
	private IPropertyChangeListener getEnablementListener() {
		if (enablementListener == null) {
			enablementListener = new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					if (event.getProperty() == PROP_ENABLED) {
						setProxyEnabled(event.getNewValue() == null ? false
								: ((Boolean) event.getNewValue())
										.booleanValue());
						fireHandlerChanged(new HandlerEvent(HandlerProxy.this,
								true, false));
					}
				}
			};
		}
		return enablementListener;
	}

	/**
	 * Passes the dipose on to the proxied handler, if it has been loaded.
	 */
	public final void dispose() {
		if (handler != null) {
			if (handlerListener != null) {
				handler.removeHandlerListener(handlerListener);
				handlerListener = null;
			}
			handler.dispose();
			handler = null;
		}
		if (enablementListener != null) {
			evaluationService.removeEvaluationListener(enablementRef);
			enablementRef = null;
			enablementListener = null;
		}
	}

	public final Object execute(final ExecutionEvent event)
			throws ExecutionException {
		if (loadHandler()) {
			return handler.execute(event);
		}

		return null;
	}

	public final boolean isEnabled() {
		if (enabledWhenExpression != null) {
			// proxyEnabled reflects the enabledWhen clause
			if (!getProxyEnabled()) {
				return false;
			}
			if (isOkToLoad() && loadHandler()) {
				return handler.isEnabled();
			}

			return true;
		}

		/*
		 * There is no enabled when expression, so we just need to consult the
		 * handler.
		 */
		if (isOkToLoad() && loadHandler()) {
			return handler.isEnabled();
		}
		return true;
	}

	public final boolean isHandled() {
		if (configurationElement != null) {
			return true;
		}

		if (isOkToLoad() && loadHandler()) {
			return handler.isHandled();
		}

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
				if (configurationElement != null) {
					handler = (IHandler) configurationElement
							.createExecutableExtension(handlerAttributeName);
					configurationElement = null;
					handler.addHandlerListener(getHandlerListener());
					return true;
				}

			} catch (final ClassCastException e) {
				final String message = "The proxied handler was the wrong class"; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.ERROR,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
				WorkbenchPlugin.log(message, status);
				configurationElement = null;

			} catch (final CoreException e) {
				final String message = "The proxied handler for '" + configurationElement.getAttribute(handlerAttributeName) //$NON-NLS-1$
						+ "' could not be loaded"; //$NON-NLS-1$
				IStatus status = new Status(IStatus.ERROR,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
				WorkbenchPlugin.log(message, status);
				configurationElement = null;
			}
			return false;
		}

		return true;
	}

	/**
	 * @return
	 */
	private IHandlerListener getHandlerListener() {
		if (handlerListener == null) {
			handlerListener = new IHandlerListener() {
				public void handlerChanged(HandlerEvent handlerEvent) {
					fireHandlerChanged(new HandlerEvent(HandlerProxy.this,
							handlerEvent.isEnabledChanged(), handlerEvent
									.isHandledChanged()));
				}
			};
		}
		return handlerListener;
	}

	public final String toString() {
		if (handler == null) {
			if (configurationElement != null) {
				return configurationElement.getAttribute(handlerAttributeName);
			}
			return "HandlerProxy()"; //$NON-NLS-1$
		}

		return handler.toString();
	}

	private boolean isOkToLoad() {
		if (configurationElement != null) {
			final String bundleId = configurationElement.getContributor()
					.getName();
			return BundleUtility.isActive(bundleId);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.IElementUpdater#updateElement(org.eclipse.ui.menus.UIElement,
	 *      java.util.Map)
	 */
	public void updateElement(UIElement element, Map parameters) {
		if (handler != null && handler instanceof IElementUpdater) {
			((IElementUpdater) handler).updateElement(element, parameters);
		}
	}
}
