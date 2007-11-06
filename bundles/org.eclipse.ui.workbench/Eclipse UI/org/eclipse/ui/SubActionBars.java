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
package org.eclipse.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.SubMenuManager;
import org.eclipse.jface.action.SubStatusLineManager;
import org.eclipse.jface.action.SubToolBarManager;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.EditorActionBars;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.actions.CommandAction;
import org.eclipse.ui.internal.handlers.CommandLegacyActionWrapper;
import org.eclipse.ui.internal.handlers.IActionCommandMappingService;
import org.eclipse.ui.internal.services.SourcePriorityNameMapping;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Generic implementation of the <code>IActionBars</code> interface.
 */
public class SubActionBars extends EventManager implements IActionBars {

	/**
	 * The expression to use when contributing handlers through
	 * {@link #setGlobalActionHandler(String, IAction)}}. This ensures that
	 * handlers contributed through {@link SubActionBars} are given priority
	 * over handlers contributed to the {@link IHandlerService}.
	 */
	private static final Expression EXPRESSION = new Expression() {
		public final EvaluationResult evaluate(final IEvaluationContext context) {
			return EvaluationResult.TRUE;
		}

		public final void collectExpressionInfo(final ExpressionInfo info) {
			info
					.addVariableNameAccess(SourcePriorityNameMapping.LEGACY_LEGACY_NAME);
		}
	};

	/**
	 * Property constant for changes to action handlers.
	 */
	public static final String P_ACTION_HANDLERS = "org.eclipse.ui.internal.actionHandlers"; //$NON-NLS-1$

	private Map actionHandlers;

	private boolean actionHandlersChanged;

	/**
	 * A map of handler activations ({@link IHandlerActivation} indexed by
	 * action id ({@link String}) indexed by service locator ({@link IServiceLocator}).
	 * This value is <code>null</code> if there are no activations.
	 */
	private Map activationsByActionIdByServiceLocator;

	private boolean active = false;

	private SubMenuManager menuMgr;

	private IActionBars parent;

	/**
	 * A service locator appropriate for this action bar. This value is never
	 * <code>null</code>. It must be capable of providing a
	 * {@link IHandlerService}.
	 */
	private IServiceLocator serviceLocator;

	private SubStatusLineManager statusLineMgr;

	private SubToolBarManager toolBarMgr;

	private Map actionIdByCommandId = new HashMap();

	/**
	 * Construct a new <code>SubActionBars</code> object. The service locator
	 * will simply be the service locator of the parent.
	 * 
	 * @param parent
	 *            The parent of this action bar; must not be <code>null</code>.
	 */
	public SubActionBars(final IActionBars parent) {
		this(parent, null);
	}

	/**
	 * Constructs a new instance of <code>SubActionBars</code>.
	 * 
	 * @param parent
	 *            The parent of this action bar; must not be <code>null</code>.
	 * @param serviceLocator
	 *            The service locator for this action bar; should not be
	 *            <code>null</code>.
	 *            
	 * @since 3.2
	 */
	public SubActionBars(final IActionBars parent,
			final IServiceLocator serviceLocator) {
		if (parent == null) {
			throw new NullPointerException("The parent cannot be null"); //$NON-NLS-1$
		}

		this.parent = parent;
		this.serviceLocator = serviceLocator;
	}

	/**
	 * Activate the contributions.
	 */
	public void activate() {
		activate(true);
	}

	/**
	 * Activate the contributions.
	 * <p>
	 * Workaround for toolbar layout flashing when editors contribute large
	 * amounts of items. In this case we want to force the items to be
	 * visible/hidden only when required, otherwise just change the enablement
	 * state.
	 * </p>
	 */
	public void activate(boolean forceVisibility) {
		setActive(true);
	}

	/**
	 * Adds a property change listener. Has no effect if an identical listener
	 * is already registered.
	 * 
	 * @param listener
	 *            a property change listener
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		addListenerObject(listener);
	}

	/**
	 * Sets the active flag. Clients should not call this method directly unless
	 * they are overriding the setActive() method.
	 */
	protected final void basicSetActive(boolean active) {
		this.active = active;
	}

	/**
	 * Clear the global action handlers.
	 */
	public void clearGlobalActionHandlers() {
		if (actionHandlers != null) {
			actionHandlers.clear();
			actionHandlersChanged = true;
		}

		if (activationsByActionIdByServiceLocator != null) {
			// Clean up the activations.
			final Iterator activationItr = activationsByActionIdByServiceLocator
					.entrySet().iterator();
			while (activationItr.hasNext()) {
				final Map.Entry value = (Map.Entry) activationItr.next();
				final IServiceLocator locator = (IServiceLocator) value
						.getKey();
				final IHandlerService service = (IHandlerService) locator
						.getService(IHandlerService.class);
				final Map activationsByActionId = (Map) value.getValue();
				final Iterator iterator = activationsByActionId.values()
						.iterator();
				while (iterator.hasNext()) {
					final IHandlerActivation activation = (IHandlerActivation) iterator
							.next();
					service.deactivateHandler(activation);
					activation.getHandler().dispose();
				}
			}
			activationsByActionIdByServiceLocator.clear();
		}
	}

	/**
	 * Returns a new sub menu manager.
	 * 
	 * @param parent
	 *            the parent menu manager
	 * @return the menu manager
	 */
	protected SubMenuManager createSubMenuManager(IMenuManager parent) {
		return new SubMenuManager(parent);
	}

	/**
	 * Returns a new sub toolbar manager.
	 * 
	 * @param parent
	 *            the parent toolbar manager
	 * @return the tool bar manager
	 */
	protected SubToolBarManager createSubToolBarManager(IToolBarManager parent) {
		return new SubToolBarManager(parent);
	}

	/**
	 * Deactivate the contributions.
	 */
	public void deactivate() {
		deactivate(true);
	}

	/**
	 * Deactivate the contributions.
	 * <p>
	 * Workaround for menubar/toolbar layout flashing when editors have many
	 * contributions. In this case we want to force the contributions to be
	 * visible/hidden only when required, otherwise just change the enablement
	 * state.
	 * </p>
	 */
	public void deactivate(boolean forceHide) {
		setActive(false);
	}

	/**
	 * Dispose the contributions.
	 */
	public void dispose() {
		clearGlobalActionHandlers();
		if (menuMgr != null) {
			menuMgr.dispose();
			menuMgr.disposeManager();
		}
		if (statusLineMgr != null) {
			statusLineMgr.disposeManager();
		}
		if (toolBarMgr != null) {
			toolBarMgr.disposeManager();
		}
		clearListeners();
	}

	/**
	 * Notifies any property change listeners if the global action handlers have
	 * changed
	 */
	protected void fireActionHandlersChanged() {
		if (actionHandlersChanged) {
			// Doesn't actually pass the old and new values
			firePropertyChange(new PropertyChangeEvent(this, P_ACTION_HANDLERS,
					null, null));
			actionHandlersChanged = false;
		}
	}

	/**
	 * Notifies any property change listeners that a property has changed. Only
	 * listeners registered at the time this method is called are notified.
	 * 
	 * @param event
	 *            the property change event
	 * 
	 * @see IPropertyChangeListener#propertyChange
	 */
	protected void firePropertyChange(PropertyChangeEvent event) {
		Object[] listeners = getListeners();
		for (int i = 0; i < listeners.length; ++i) {
			((IPropertyChangeListener) listeners[i]).propertyChange(event);
		}
	}

	/**
	 * Return whether the manager is currently active or not.
	 */
	protected final boolean getActive() {
		return active;
	}

	/**
	 * Get the handler for a window action.
	 * 
	 * @param actionID
	 *            an action ID declared in the registry
	 * @return an action handler which implements the action ID, or
	 *         <code>null</code> if none is registered.
	 */
	public IAction getGlobalActionHandler(String actionID) {
		if (actionHandlers == null) {
			return null;
		}
		return (IAction) actionHandlers.get(actionID);
	}

	/**
	 * Returns the complete list of active global action handlers. If there are
	 * no global action handlers registered return null.
	 */
	public Map getGlobalActionHandlers() {
		return actionHandlers;
	}

	/**
	 * Returns the abstract menu manager. If items are added or removed from the
	 * manager be sure to call <code>updateActionBars</code>.
	 * 
	 * @return the menu manager
	 */
	public IMenuManager getMenuManager() {
		if (menuMgr == null) {
			menuMgr = createSubMenuManager(parent.getMenuManager());
			menuMgr.setVisible(active);
		}
		return menuMgr;
	}

	/**
	 * Return the parent action bar manager.
	 */
	protected final IActionBars getParent() {
		return parent;
	}

	/**
	 * Answer the service locator for this action bar.
	 * 
	 * @return an <code>IServiceLocater</code> or the parents if 
	 *     the receiver does not have one
	 *     
	 * @since 3.2
	 */
	public final IServiceLocator getServiceLocator() {
		if (serviceLocator != null) {
			return serviceLocator;
		}

		return parent.getServiceLocator();
	}

	/**
	 * Returns the status line manager. If items are added or removed from the
	 * manager be sure to call <code>updateActionBars</code>.
	 * 
	 * @return the status line manager
	 */
	public IStatusLineManager getStatusLineManager() {
		if (statusLineMgr == null) {
			statusLineMgr = new SubStatusLineManager(parent
					.getStatusLineManager());
			statusLineMgr.setVisible(active);
		}
		return statusLineMgr;
	}

	/**
	 * Returns the tool bar manager. If items are added or removed from the
	 * manager be sure to call <code>updateActionBars</code>.
	 * 
	 * @return the tool bar manager
	 */
	public IToolBarManager getToolBarManager() {
		if (toolBarMgr == null) {
			toolBarMgr = createSubToolBarManager(parent.getToolBarManager());
			toolBarMgr.setVisible(active);
		}
		return toolBarMgr;
	}

	/**
	 * Return whether the sub menu manager has been created yet.
	 */
	protected final boolean isSubMenuManagerCreated() {
		return menuMgr != null;
	}

	/**
	 * Return whether the sub status line manager has been created yet.
	 */
	protected final boolean isSubStatusLineManagerCreated() {
		return statusLineMgr != null;
	}

	/**
	 * Return whether the sub toolbar manager has been created yet.
	 */
	protected final boolean isSubToolBarManagerCreated() {
		return toolBarMgr != null;
	}

	/**
	 * Notification that the target part for the action bars has changed.
	 */
	public void partChanged(IWorkbenchPart part) {
	}

	/**
	 * Removes the given property change listener. Has no effect if an identical
	 * listener is not registered.
	 * 
	 * @param listener
	 *            a property change listener
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		removeListenerObject(listener);
	}

	/**
	 * Activate / deactivate the contributions.
	 */
	protected void setActive(boolean set) {
		active = set;
		if (menuMgr != null) {
			menuMgr.setVisible(set);
		}

		if (statusLineMgr != null) {
			statusLineMgr.setVisible(set);
		}

		if (toolBarMgr != null) {
			toolBarMgr.setVisible(set);
		}
	}

	/**
	 * Add a handler for a window action.
	 * 
	 * @param actionID
	 *            an action ID declared in the registry
	 * @param handler
	 *            an action which implements the action ID. <code>null</code>
	 *            may be passed to deregister a handler.
	 */
	public void setGlobalActionHandler(String actionID, IAction handler) {
		if (actionID == null) {
			/*
			 * Bug 124061. It used to be invalid to pass null as an action id,
			 * but some people still did it. Handle this case by trapping the
			 * exception and logging it.
			 */
			WorkbenchPlugin
					.log("Cannot set the global action handler for a null action id"); //$NON-NLS-1$
			return;
		}
		
		if (handler instanceof CommandLegacyActionWrapper) {
        	// this is a registration of a fake action for an already
			// registered handler
			WorkbenchPlugin
					.log("Cannot feed a CommandLegacyActionWrapper back into the system"); //$NON-NLS-1$
			return;
		}
		
		if (handler instanceof CommandAction) {
			// we unfortunately had to allow these out into the wild, but they
			// still must not feed back into the system
			return;
		}
		
		if (handler != null) {
			// Update the action handlers.
			if (actionHandlers == null) {
				actionHandlers = new HashMap(11);
			}
			actionHandlers.put(actionID, handler);

			// Add a mapping from this action id to the command id.
			if (serviceLocator != null) {
				String commandId = null;
				final IActionCommandMappingService mappingService = (IActionCommandMappingService) serviceLocator
						.getService(IActionCommandMappingService.class);
				if (mappingService != null) {
					commandId = mappingService.getCommandId(actionID);
				}
				if (commandId == null) {
					commandId = handler.getActionDefinitionId();
				}

				// Update the handler activations.
				final IHandlerService service = (IHandlerService) serviceLocator
						.getService(IHandlerService.class);
				Map activationsByActionId = null;
				if (activationsByActionIdByServiceLocator == null) {
					activationsByActionIdByServiceLocator = new WeakHashMap();
					activationsByActionId = new HashMap();
					activationsByActionIdByServiceLocator.put(serviceLocator,
							activationsByActionId);
				} else {
					activationsByActionId = (Map) activationsByActionIdByServiceLocator
							.get(serviceLocator);
					if (activationsByActionId == null) {
						activationsByActionId = new HashMap();
						activationsByActionIdByServiceLocator.put(
								serviceLocator, activationsByActionId);
					} else if (activationsByActionId.containsKey(actionID)) {
						final Object value = activationsByActionId
								.remove(actionID);
						if (value instanceof IHandlerActivation) {
							final IHandlerActivation activation = (IHandlerActivation) value;
							actionIdByCommandId.remove(activation.getCommandId());
							if (service != null) {
								service.deactivateHandler(activation);
							}
							activation.getHandler().dispose();
						}
					} else if (commandId != null
							&& actionIdByCommandId.containsKey(commandId)) {
						final Object value = activationsByActionId
								.remove(actionIdByCommandId.remove(commandId));
						if (value instanceof IHandlerActivation) {
							final IHandlerActivation activation = (IHandlerActivation) value;
							if (service != null) {
								service.deactivateHandler(activation);
							}
							activation.getHandler().dispose();
						}
					}
				}

				if (commandId != null) {
					actionIdByCommandId.put(commandId, actionID);
					// Register this as a handler with the given definition id.
					// the expression gives the setGlobalActionHandler() a
					// priority.
					final IHandler actionHandler = new ActionHandler(handler);
					Expression handlerExpression = EXPRESSION;
					//XXX add new API in next release to avoid down-casting (bug 137091)
					if (this instanceof EditorActionBars) {
						handlerExpression = ((EditorActionBars)this).getHandlerExpression();
					}
					if (service != null) {
						final IHandlerActivation activation = service
								.activateHandler(commandId, actionHandler,
										handlerExpression);
						activationsByActionId.put(actionID, activation);
					}
				}
			}

		} else {
			if (actionHandlers != null) {
				actionHandlers.remove(actionID);
			}

			// Remove the handler activation.
			if (serviceLocator != null) {
				final IHandlerService service = (IHandlerService) serviceLocator
						.getService(IHandlerService.class);
				if (activationsByActionIdByServiceLocator != null) {
					final Map activationsByActionId = (Map) activationsByActionIdByServiceLocator
							.get(serviceLocator);
					if ((activationsByActionId != null)
							&& (activationsByActionId.containsKey(actionID))) {
						final Object value = activationsByActionId
								.remove(actionID);
						if (value instanceof IHandlerActivation) {
							final IHandlerActivation activation = (IHandlerActivation) value;
							actionIdByCommandId.remove(activation.getCommandId());
							service.deactivateHandler(activation);
							activation.getHandler().dispose();
						}
					}
				}
			}
		}
		actionHandlersChanged = true;
	}

	/**
	 * Sets the service locator for this action bar.
	 * 
	 * @param locator
	 *            The new locator; must not be <code>null</code>.
	 *            
	 * @since 3.2
	 */
	protected final void setServiceLocator(final IServiceLocator locator) {
		if (locator == null) {
			throw new NullPointerException("The service locator cannot be null"); //$NON-NLS-1$
		}
		this.serviceLocator = locator;
	}

	/**
	 * Commits all UI changes. This should be called after additions or
	 * subtractions have been made to a menu, status line, or toolbar.
	 */
	public void updateActionBars() {
		parent.updateActionBars();
		fireActionHandlersChanged();
	}
}
