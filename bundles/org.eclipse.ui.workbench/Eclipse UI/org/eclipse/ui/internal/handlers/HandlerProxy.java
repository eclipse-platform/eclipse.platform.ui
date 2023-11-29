/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 *******************************************************************************/

package org.eclipse.ui.internal.handlers;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.commands.AbstractHandlerWithState;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.commands.IObjectWithState;
import org.eclipse.core.commands.State;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.RadioState;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IEvaluationReference;
import org.eclipse.ui.services.IEvaluationService;

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
public final class HandlerProxy extends AbstractHandlerWithState implements IElementUpdater {

	private static Map<IConfigurationElement, HandlerProxy> CEToProxyMap = new HashMap<>();

	private static final String PROP_ENABLED = "enabled"; //$NON-NLS-1$

	/**
	 * The configuration element from which the handler can be created. This value
	 * will exist until the element is converted into a real class -- at which point
	 * this value will be set to <code>null</code>.
	 */
	private IConfigurationElement configurationElement;

	/**
	 * The <code>enabledWhen</code> expression for the handler. Only if this
	 * expression evaluates to <code>true</code> (or the value is <code>null</code>)
	 * should we consult the handler.
	 */
	private final Expression enabledWhenExpression;

	/**
	 * The real handler. This value is <code>null</code> until the proxy is forced
	 * to load the real handler. At this point, the configuration element is
	 * converted, nulled out, and this handler gains a reference.
	 */
	private IHandler handler;

	/**
	 * The name of the configuration element attribute which contains the
	 * information necessary to instantiate the real handler.
	 */
	private final String handlerAttributeName;

	private IHandlerListener handlerListener;

	/**
	 * The evaluation service to use when evaluating
	 * <code>enabledWhenExpression</code>. This value may be <code>null</code> only
	 * if the <code>enabledWhenExpression</code> is <code>null</code>.
	 */
	private IEvaluationService evaluationService;

	private IPropertyChangeListener enablementListener;

	private IEvaluationReference enablementRef;

	private boolean proxyEnabled;

	private String commandId;

	//
	// state to support checked or radio commands.
	private State checkedState;

	private State radioState;

	// Exception that occurs while loading the proxied handler class
	private Exception loadException;

	/**
	 * Constructs a new instance of <code>HandlerProxy</code> with all the
	 * information it needs to try to avoid loading until it is needed.
	 *
	 * @param commandId            the id for this handler
	 * @param configurationElement The configuration element from which the real
	 *                             class can be loaded at run-time; must not be
	 *                             <code>null</code>.
	 * @param handlerAttributeName The name of the attibute or element containing
	 *                             the handler executable extension; must not be
	 *                             <code>null</code>.
	 */
	public HandlerProxy(final String commandId, final IConfigurationElement configurationElement,
			final String handlerAttributeName) {
		this(commandId, configurationElement, handlerAttributeName, null, null);
	}

	/**
	 * Constructs a new instance of <code>HandlerProxy</code> with all the
	 * information it needs to try to avoid loading until it is needed.
	 *
	 * @param commandId             the id for this handler
	 * @param configurationElement  The configuration element from which the real
	 *                              class can be loaded at run-time; must not be
	 *                              <code>null</code>.
	 * @param handlerAttributeName  The name of the attribute or element containing
	 *                              the handler executable extension; must not be
	 *                              <code>null</code>.
	 * @param enabledWhenExpression The name of the element containing the
	 *                              enabledWhen expression. This should be a child
	 *                              of the <code>configurationElement</code>. If
	 *                              this value is <code>null</code>, then there is
	 *                              no enablement expression (i.e., enablement will
	 *                              be delegated to the handler when possible).
	 * @param evaluationService     The evaluation service to manage enabledWhen
	 *                              expressions trying to evaluate the
	 *                              <code>enabledWhenExpression</code>. This value
	 *                              may be <code>null</code> only if the
	 *                              <code>enabledWhenExpression</code> is
	 *                              <code>null</code>.
	 */
	public HandlerProxy(final String commandId, final IConfigurationElement configurationElement,
			final String handlerAttributeName, final Expression enabledWhenExpression,
			final IEvaluationService evaluationService) {
		if (configurationElement == null) {
			throw new NullPointerException("The configuration element backing a handler proxy cannot be null"); //$NON-NLS-1$
		}

		if (handlerAttributeName == null) {
			throw new NullPointerException("The attribute containing the handler class must be known"); //$NON-NLS-1$
		}

		if ((enabledWhenExpression != null) && (evaluationService == null)) {
			throw new NullPointerException(
					"We must have a handler service and evaluation service to support the enabledWhen expression"); //$NON-NLS-1$
		}

		this.commandId = commandId;
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

		CEToProxyMap.put(configurationElement, this);
	}

	public static void updateStaleCEs(IConfigurationElement[] replacements) {
		for (IConfigurationElement replacement : replacements) {
			HandlerProxy proxy = CEToProxyMap.get(replacement);
			if (proxy != null)
				proxy.configurationElement = replacement;
		}
	}

	private void registerEnablement() {
		enablementRef = evaluationService.addEvaluationListener(enabledWhenExpression, getEnablementListener(),
				PROP_ENABLED);
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		if (!(evaluationContext instanceof IEvaluationContext)) {
			return;
		}
		IEvaluationContext context = (IEvaluationContext) evaluationContext;
		if (enabledWhenExpression != null) {
			try {
				setProxyEnabled(enabledWhenExpression.evaluate(context) == EvaluationResult.TRUE);
			} catch (CoreException e) {
				// TODO should we log this exception, or just treat it as
				// a failure
			}
		}
		if (isOkToLoad() && loadHandler()) {
			if (handler instanceof IHandler2) {
				((IHandler2) handler).setEnabled(evaluationContext);
			}
		}
	}

	void setProxyEnabled(boolean enabled) {
		proxyEnabled = enabled;
	}

	boolean getProxyEnabled() {
		return proxyEnabled;
	}

	private IPropertyChangeListener getEnablementListener() {
		if (enablementListener == null) {
			enablementListener = event -> {
				if (event.getProperty() == PROP_ENABLED) {
					setProxyEnabled(
							event.getNewValue() == null ? false : ((Boolean) event.getNewValue()).booleanValue());
					fireHandlerChanged(new HandlerEvent(HandlerProxy.this, true, false));
				}
			};
		}
		return enablementListener;
	}

	/**
	 * Passes the dipose on to the proxied handler, if it has been loaded.
	 */
	@Override
	public void dispose() {
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

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		if (loadHandler()) {
			if (!isEnabled()) {
				MessageDialog.openInformation(Util.getShellToParentOn(), WorkbenchMessages.Information,
						WorkbenchMessages.PluginAction_disabledMessage);
				return null;
			}
			return handler.execute(event);
		}

		if (loadException != null)
			throw new ExecutionException("Exception occured when loading the handler", loadException); //$NON-NLS-1$

		return null;
	}

	@Override
	public boolean isEnabled() {
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
		 * There is no enabled when expression, so we just need to consult the handler.
		 */
		if (isOkToLoad() && loadHandler()) {
			return handler.isEnabled();
		}
		return true;
	}

	@Override
	public boolean isHandled() {
		boolean okToLoad = isOkToLoad();
		if (okToLoad && loadHandler()) {
			return handler.isHandled();
		}
		if (!okToLoad) {
			// That is crazy, but we should answer "true" for contributions from not loaded
			// plugins, otherwise pure declarative commands defined in not active bundles
			// will not run for the first time
			return true;
		}
		return false;
	}

	/**
	 * Loads the handler, if possible. If the handler is loaded, then the member
	 * variables are updated accordingly.
	 *
	 * @return <code>true</code> if the handler is now non-null; <code>false</code>
	 *         otherwise.
	 */
	private boolean loadHandler() {
		if (handler == null) {
			// Load the handler.
			try {
				if (configurationElement != null) {
					handler = (IHandler) configurationElement.createExecutableExtension(handlerAttributeName);
					handler.addHandlerListener(getHandlerListener());
					if (handler instanceof IObjectWithState) {
						for (String id : getStateIds()) {
							((IObjectWithState) handler).addState(id, getState(id));
						}
					}
					setEnabled(evaluationService == null ? null : evaluationService.getCurrentState());
					refreshElements();
					return true;
				}

			} catch (final ClassCastException e) {
				final String message = "The proxied handler was the wrong class"; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
				WorkbenchPlugin.log(message, status);
				configurationElement = null;
				loadException = e;

			} catch (final CoreException e) {
				final String message = "The proxied handler for '" //$NON-NLS-1$
						+ configurationElement.getAttribute(handlerAttributeName) + "' could not be loaded"; //$NON-NLS-1$
				IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
				WorkbenchPlugin.log(message, status);
				configurationElement = null;
				loadException = e;
			}
			return false;
		}

		return true;
	}

	private IHandlerListener getHandlerListener() {
		if (handlerListener == null) {
			handlerListener = handlerEvent -> fireHandlerChanged(new HandlerEvent(HandlerProxy.this,
					handlerEvent.isEnabledChanged(), handlerEvent.isHandledChanged()));
		}
		return handlerListener;
	}

	@Override
	public String toString() {
		if (handler == null) {
			if (configurationElement != null) {
				String configurationElementAttribute = getConfigurationElementAttribute();
				if (configurationElementAttribute != null) {
					return configurationElementAttribute;
				}
			}
			return "HandlerProxy()"; //$NON-NLS-1$
		}

		return handler.toString();
	}

	/**
	 * Retrives the ConfigurationElement attribute according to the
	 * <code>handlerAttributeName</code>.
	 *
	 * @return the handlerAttributeName value, may be <code>null</code>.
	 */
	private String getConfigurationElementAttribute() {
		String attribute = configurationElement.getAttribute(handlerAttributeName);
		if (attribute == null) {
			for (IConfigurationElement configElement : configurationElement.getChildren(handlerAttributeName)) {
				String childAttribute = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_CLASS);
				if (childAttribute != null) {
					return childAttribute;
				}
			}
		}
		return attribute;
	}

	private boolean isOkToLoad() {
		if (PlatformUI.getWorkbench().isClosing())
			return handler != null;

		if (configurationElement != null && handler == null) {
			final String bundleId = configurationElement.getContributor().getName();
			return BundleUtility.isActive(bundleId);
		}
		return true;
	}

	@Override
	public void updateElement(UIElement element, Map parameters) {
		if (checkedState != null) {
			Boolean value = (Boolean) checkedState.getValue();
			element.setChecked(value.booleanValue());
		} else if (radioState != null) {
			String value = (String) radioState.getValue();
			Object parameter = parameters.get(RadioState.PARAMETER_ID);
			element.setChecked(value != null && value.equals(parameter));
		}
		if (handler != null && handler instanceof IElementUpdater) {
			((IElementUpdater) handler).updateElement(element, parameters);
		}
	}

	private void refreshElements() {
		if (commandId == null
				|| !(handler instanceof IElementUpdater) && (checkedState == null && radioState == null)) {
			return;
		}
		ICommandService cs = PlatformUI.getWorkbench().getService(ICommandService.class);
		cs.refreshElements(commandId, null);
	}

	@Override
	public void handleStateChange(State state, Object oldValue) {
		if (state.getId().equals(RegistryToggleState.STATE_ID)) {
			checkedState = state;
			refreshElements();
		} else if (state.getId().equals(RadioState.STATE_ID)) {
			radioState = state;
			refreshElements();
		}
	}

	/**
	 * @return the config element for use with the PDE framework.
	 */
	public IConfigurationElement getConfigurationElement() {
		return configurationElement;
	}

	public String getAttributeName() {
		return handlerAttributeName;
	}

	/**
	 * @return Returns the handler.
	 */
	public IHandler getHandler() {
		return handler;
	}

	@Override
	public void addState(String stateId, State state) {
		super.addState(stateId, state);
		if (handler instanceof IObjectWithState) {
			((IObjectWithState) handler).addState(stateId, state);
		}
	}

	@Override
	public void removeState(String stateId) {
		if (handler instanceof IObjectWithState) {
			((IObjectWithState) handler).removeState(stateId);
		}
		super.removeState(stateId);
	}
}
