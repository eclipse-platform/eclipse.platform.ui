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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.commands.IObjectWithState;
import org.eclipse.core.commands.IState;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IActionDelegateWithEvent;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.handlers.CommandLegacyActionWrapper;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;

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
public final class ActionDelegateHandlerProxy implements ISelectionListener,
		ISelectionChangedListener, INullSelectionListener, IHandler,
		IObjectWithState {

	/**
	 * The identifier of the actions to create as a wrapper to the command
	 * architecture. This value may be <code>null</code>.
	 */
	private String actionId;

	/**
	 * The fake action that proxies all of the command-based services. This
	 * value is never <code>null</code>.
	 */
	private CommandLegacyActionWrapper action;

	/**
	 * The command that will back the dummy actions exposed to this delegate.
	 * This value is never <code>null</code>.
	 */
	private ParameterizedCommand command;

	/**
	 * This is the current selection, as seen by this proxy.
	 */
	private ISelection currentSelection;

	/**
	 * The name of the configuration element attribute which contains the
	 * information necessary to instantiate the real handler.
	 */
	private String delegateAttributeName;

	/**
	 * The delegate, if it has been created yet.
	 */
	private IActionDelegate delegate;

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
	 * A collection of objects listening to changes to this manager. This
	 * collection is <code>null</code> if there are no listeners.
	 */
	private transient ListenerList listenerList = null;

	/**
	 * The image style to use when selecting the images to display for this
	 * delegate. This value may be <code>null</code>, if the default style
	 * should be used.
	 */
	private final String style;

	/**
	 * The identifier of the view with which this delegate must be associated.
	 * This value is not <code>null</code> iff the delegate is an
	 * {@link IViewActionDelegate}.
	 */
	private final String viewId;

	/**
	 * The workbench window in which this delegate is active. This value is
	 * never <code>null</code>.
	 */
	private final WorkbenchWindow window;

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
	 * @param window
	 *            The workbench window in which this delegate will be active;
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
	 * @param viewId
	 *            The identifier of the view to which this proxy is bound; may
	 *            be <code>null</code> if this proxy is not for an
	 *            {@link IViewActionDelegate}.
	 */
	public ActionDelegateHandlerProxy(final IConfigurationElement element,
			final String delegateAttributeName, final String actionId,
			final ParameterizedCommand command, final WorkbenchWindow window,
			final String style, final Expression enabledWhenExpression,
			final String viewId) {
		if (element == null) {
			throw new NullPointerException(
					"The configuration element backing a handler proxy cannot be null"); //$NON-NLS-1$
		}

		if (delegateAttributeName == null) {
			throw new NullPointerException(
					"The attribute containing the action delegate must be known"); //$NON-NLS-1$
		}

		if (window == null) {
			throw new NullPointerException(
					"The workbench window for a delegate must not be null"); //$NON-NLS-1$
		}

		this.element = element;
		this.enabledWhenExpression = enabledWhenExpression;
		this.delegateAttributeName = delegateAttributeName;
		this.window = window;
		this.command = command;
		this.actionId = actionId;
		this.style = style;
		this.viewId = viewId;
	}

	public final void addHandlerListener(final IHandlerListener handlerListener) {
		if (listenerList == null) {
			listenerList = new ListenerList(ListenerList.IDENTITY);
		}

		listenerList.add(handlerListener);
	}

	public void addState(String id, IState state) {
		// TODO Auto-generated method stub

	}

	public final void dispose() {
		final IActionDelegate delegate = getDelegate();
		if (delegate instanceof IWorkbenchWindowActionDelegate) {
			final IWorkbenchWindowActionDelegate workbenchWindowDelegate = (IWorkbenchWindowActionDelegate) delegate;
			workbenchWindowDelegate.dispose();
		} else if (delegate instanceof IActionDelegate2) {
			final IActionDelegate2 delegate2 = (IActionDelegate2) delegate;
			delegate2.dispose();
		}
	}

	public final Object execute(final ExecutionEvent event) {
		final IAction action = getAction();		
		if (loadDelegate() && (action != null)) {
			final Object trigger = event.getTrigger();
			final IActionDelegate delegate = getDelegate();
			
			// Attempt to update the selection.
			final Object applicationContext = event.getApplicationContext();
			if (applicationContext instanceof IEvaluationContext) {
				final IEvaluationContext context = (IEvaluationContext) applicationContext;
				final Object selectionObject = context.getDefaultVariable();
				if (selectionObject instanceof ISelection) {
					currentSelection = (ISelection) selectionObject;
					delegate.selectionChanged(action, currentSelection);
				}
			}
			
			// Decide what type of delegate we have.
			if ((delegate instanceof IActionDelegate2)
					&& (trigger instanceof Event)) {
				// This supports Eclipse 2.1 to Eclipse 3.1.
				final IActionDelegate2 delegate2 = (IActionDelegate2) delegate;
				final Event triggeringEvent = (Event) trigger;
				delegate2.runWithEvent(action, triggeringEvent);
			} else if ((delegate instanceof IActionDelegateWithEvent)
					&& (trigger instanceof Event)) {
				// This supports Eclipse 2.0
				final IActionDelegateWithEvent delegateWithEvent = (IActionDelegateWithEvent) delegate;
				final Event triggeringEvent = (Event) trigger;
				delegateWithEvent.runWithEvent(action, triggeringEvent);
			} else {
				delegate.run(action);
			}
		}

		return null;
	}

	/**
	 * Retrieves the action corresponding to the currently active workbench
	 * window, if any.
	 * 
	 * @return The current action; <code>null</code> if there is no currently
	 *         active workbench window.
	 */
	private final CommandLegacyActionWrapper getAction() {
		if (action == null) {
			action = new CommandLegacyActionWrapper(actionId, command, style,
					window);
			action.addPropertyChangeListener(new IPropertyChangeListener() {
				public final void propertyChange(final PropertyChangeEvent event) {
					// TODO Update the state somehow.
				}
			});
		}
		return action;
	}

	/**
	 * Retrieves the delegate corresponding to the currently active workbench
	 * window, if any. This does not trigger loading of the delegate.
	 * 
	 * @return The current delegate; or <code>null</code> if none.
	 */
	private final IActionDelegate getDelegate() {
		return delegate;
	}

	public IState getState(String stateId) {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getStateIds() {
		// TODO Auto-generated method stub
		return null;
	}

	public final void handleStateChange(final IState state,
			final Object oldValue) {
		// TODO What should we do here?
	}

	/**
	 * Initialize the action delegate by calling its lifecycle method.
	 */
	private void initDelegate() {
		final IWorkbenchPage page = window.getActivePage();
		final IWorkbenchPart activePart;
		final IEditorPart activeEditor;
		if (page == null) {
			activePart = null;
			activeEditor = null;
		} else {
			activePart = page.getActivePart();
			activeEditor = page.getActiveEditor();
		}
		final IActionDelegate delegate = getDelegate();
		final IAction action = getAction();

		final ISafeRunnable runnable = new ISafeRunnable() {
			public final void handleException(final Throwable exception) {
				// Do nothing.
			}

			public final void run() {
				// Handle IActionDelegate2
				if (delegate instanceof IActionDelegate2) {
					final IActionDelegate2 delegate2 = (IActionDelegate2) delegate;
					delegate2.init(action);
				}

				// Handle IObjectActionDelegates
				if ((delegate instanceof IObjectActionDelegate)
						&& (activePart != null)) {
					final IObjectActionDelegate objectActionDelegate = (IObjectActionDelegate) delegate;
					objectActionDelegate.setActivePart(action, activePart);
				}

				// Handle IEditorActionDelegates
				if (delegate instanceof IEditorActionDelegate) {
					final IEditorActionDelegate editorActionDelegate = (IEditorActionDelegate) delegate;
					editorActionDelegate.setActiveEditor(action, activeEditor);
				}

				// Handle IViewActionDelegates
				if (viewId != null) {
					if (page != null) {
						final IViewPart viewPart = page.findView(viewId);
						if (delegate instanceof IViewActionDelegate) {
							final IViewActionDelegate viewActionDelegate = (IViewActionDelegate) delegate;
							viewActionDelegate.init(viewPart);
						}
					}
				}

				// Handle IWorkbenchWindowActionDelegate
				if (delegate instanceof IWorkbenchWindowActionDelegate) {
					final IWorkbenchWindowActionDelegate workbenchWindowActionDelegate = (IWorkbenchWindowActionDelegate) delegate;
					workbenchWindowActionDelegate.init(window);
				}
			}
		};
		Platform.run(runnable);
	}

	public final boolean isEnabled() {
		final CommandLegacyActionWrapper action = getAction();
		if (enabledWhenExpression != null) {
			try {
				final IHandlerService service = (IHandlerService) window
						.getService(IHandlerService.class);
				final EvaluationResult result = enabledWhenExpression
						.evaluate(service.getCurrentState());
				if (result == EvaluationResult.TRUE) {
					return (action == null)
							|| action.isEnabledDisregardingCommand();
				}
			} catch (final CoreException e) {
				// We will just fall through an let it return false.
				final StringBuffer message = new StringBuffer(
						"An exception occurred while evaluating the enabledWhen expression for "); //$NON-NLS-1$
				if (element == null) {
					message.append(delegate);
				} else {
					message.append(element.getAttribute(delegateAttributeName));
				}
				message.append("' could not be loaded"); //$NON-NLS-1$
				final IStatus status = new Status(IStatus.WARNING,
						WorkbenchPlugin.PI_WORKBENCH, 0, e.getMessage(), e);
				WorkbenchPlugin.log(message.toString(), status);
			}

			return false;
		}

		return (action == null) || action.isEnabledDisregardingCommand();
	}

	public final boolean isHandled() {
		return true;
	}

	/**
	 * Checks if the declaring plugin has been loaded. This means that there
	 * will be no need to delay creating the delegate.
	 * 
	 * @return <code>true</code> if the bundle containing the delegate is
	 *         already loaded -- making it safe to load the delegate.
	 */
	private final boolean isSafeToLoadDelegate() {
		return false;
		// TODO This causes problem because some people expect their selections
		// to be a particular class.
		// final String bundleId = element.getNamespace();
		// return BundleUtility.isActive(bundleId);
	}

	/**
	 * Loads the delegate, if possible. If the delegate is loaded, then the
	 * member variables are updated accordingly.
	 * 
	 * @return <code>true</code> if the delegate is now non-null;
	 *         <code>false</code> otherwise.
	 */
	private final boolean loadDelegate() {
		// Try to load the delegate, if it hasn't been loaded already.
		if (delegate == null) {
			/*
			 * If this is an IViewActionDelegate, then check to see if we have a
			 * view ready yet. If not, then we'll have to wait.
			 */
			if (viewId != null) {
				final IWorkbenchPage activePage = window.getActivePage();
				if (activePage != null) {
					final IViewPart part = activePage.findView(viewId);
					if (part == null) {
						return false;
					}
				} else {
					return false;
				}
			}

			// Load the delegate.
			try {
				delegate = (IActionDelegate) element
						.createExecutableExtension(delegateAttributeName);
				initDelegate();
				element = null;
				delegateAttributeName = null;
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

	/**
	 * Refresh the action enablement.
	 */
	private final void refreshEnablement() {
		final IActionDelegate delegate = getDelegate();
		final IAction action = getAction();
		if ((delegate != null) && (action != null)) {
			delegate.selectionChanged(action, currentSelection);
		}
	}

	public void removeHandlerListener(IHandlerListener handlerListener) {
		if (listenerList != null) {
			listenerList.remove(handlerListener);

			if (listenerList.isEmpty()) {
				listenerList = null;
			}
		}
	}

	public void removeState(String stateId) {
		// TODO Auto-generated method stub

	}

	private final void selectionChanged(final ISelection selection) {
		// Update selection.
		currentSelection = selection;
		if (currentSelection == null)
			currentSelection = StructuredSelection.EMPTY;

		// The selection is passed to the delegate as-is without
		// modification. If the selection needs to be modified
		// the action contributors should do so.

		// If the delegate can be loaded, do so.
		// Otherwise, just update the enablement.
		final IActionDelegate delegate = getDelegate();
		if (delegate == null && isSafeToLoadDelegate()) {
			loadDelegate();
		}
		refreshEnablement();
	}

	public final void selectionChanged(final IWorkbenchPart part,
			final ISelection selection) {
		selectionChanged(selection);

	}

	public final void selectionChanged(final SelectionChangedEvent event) {
		final ISelection selection = event.getSelection();
		selectionChanged(selection);
	}

	public final String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("ActionDelegateHandlerProxy("); //$NON-NLS-1$
		buffer.append(getDelegate());
		if (element != null) {
			buffer.append(',');
			final String className = element
					.getAttribute(delegateAttributeName);
			buffer.append(className);
		}
		buffer.append(')');
		return buffer.toString();
	}
}
