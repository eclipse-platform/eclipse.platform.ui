package org.eclipse.ui.internal;

/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.actions.LabelRetargetAction;
import org.eclipse.ui.actions.RetargetAction;

/**
 * This class extends regular plugin action with the
 * additional requirement that the delegate has
 * to implement interface IWorkbenchWindowActionDeelgate.
 * This interface has one additional method (init)
 * whose purpose is to initialize the delegate with
 * the window in which the action is intended to run.
 */
public class WWinPluginAction extends PluginAction implements IActionSetContributionItem {
	/**
	 * The help listener assigned to this action, or <code>null</code> if none.
	 */
	private HelpListener localHelpListener;

	private IWorkbenchWindow window;
	private String actionSetId;
	private RetargetAction retargetAction;
	private static String TRUE_VALUE = "true"; //$NON-NLS-1$

	private static ArrayList staticActionList = new ArrayList(50);

	/**
	 * Constructs a new WWinPluginAction object..
	 */
	public WWinPluginAction(IConfigurationElement actionElement, String runAttribute, IWorkbenchWindow window, String definitionId, int style) {
		super(actionElement, runAttribute, definitionId, style);
		this.window = window;

		// If config specifies a retarget action, create it now
		String retarget = actionElement.getAttribute(ActionDescriptor.ATT_RETARGET);
		if (retarget != null && retarget.equals(TRUE_VALUE)) {
			// create a retarget action
			String allowLabelUpdate = actionElement.getAttribute(ActionDescriptor.ATT_ALLOW_LABEL_UPDATE);
			String id = actionElement.getAttribute(ActionDescriptor.ATT_ID);
			String label = actionElement.getAttribute(ActionDescriptor.ATT_LABEL);

			if (allowLabelUpdate != null && allowLabelUpdate.equals(TRUE_VALUE))
				retargetAction = new LabelRetargetAction(id, label);
			else
				retargetAction = new RetargetAction(id, label);
			retargetAction.addPropertyChangeListener(new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					if (event.getProperty().equals(Action.ENABLED)) {
						Object val = event.getNewValue();
						if (val instanceof Boolean) {
							setEnabled(((Boolean) val).booleanValue());
						}
					} else if (event.getProperty().equals(Action.TEXT)) {
						Object val = event.getNewValue();
						if (val instanceof String) {
							setText((String) val);
						}
					} else if (event.getProperty().equals(Action.TOOL_TIP_TEXT)) {
						Object val = event.getNewValue();
						if (val instanceof String) {
							setToolTipText((String) val);
						}
					}
				}
			});
			retargetAction.setEnabled(false);
			setEnabled(false);
			window.getPartService().addPartListener(retargetAction);
			IWorkbenchPart activePart = window.getPartService().getActivePart();
			if (activePart != null)
				retargetAction.partActivated(activePart);
		} else {
			// if we retarget the handler will look after selection changes
			window.getSelectionService().addSelectionListener(this);
			refreshSelection();
		}
		addToActionList(this);

		super.setHelpListener(new HelpListener() {
			public void helpRequested(HelpEvent e) {
				HelpListener listener = null;
				if (retargetAction != null)
					listener = retargetAction.getHelpListener();
				if (listener == null)
					// use our own help listener
					listener = localHelpListener;
				if (listener != null)
					// pass on the event
					listener.helpRequested(e);
			}
		});
	}

	/**
	 * Adds an item to the action list.
	 */
	private static void addToActionList(WWinPluginAction action) {
		staticActionList.add(action);
	}

	/**
	 * Removes an item from the action list.
	 */
	private static void removeFromActionList(WWinPluginAction action) {
		staticActionList.remove(action);
	}

	/**
	 * Creates any actions which belong to an activated plugin.
	 */
	public static void refreshActionList() {
		Iterator iter = staticActionList.iterator();
		while (iter.hasNext()) {
			WWinPluginAction action = (WWinPluginAction) iter.next();
			if ((action.getDelegate() == null) && action.isOkToCreateDelegate()) {
				action.createDelegate();
				// creating the delegate also refreshes its enablement
			}
		}
	}

	/* (non-Javadoc)
	 * Method declared on PluginAction.
	 */
	protected IActionDelegate validateDelegate(Object obj) throws WorkbenchException {
		if (obj instanceof IWorkbenchWindowActionDelegate)
			return (IWorkbenchWindowActionDelegate)obj;
		else
			throw new WorkbenchException("Action must implement IWorkbenchWindowActionDelegate"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * Method declared on PluginAction.
	 */
	protected void initDelegate() {
		super.initDelegate();
		((IWorkbenchWindowActionDelegate)getDelegate()).init(window);
	}

	/**
	 * Disposes of the action and any resources held.
	 */
	public void dispose() {
		removeFromActionList(this);
		if (retargetAction != null) {
			window.getPartService().removePartListener(retargetAction);
			retargetAction.dispose();
			retargetAction = null;
		}
		window.getSelectionService().removeSelectionListener(this);
		if (getDelegate() != null)
			((IWorkbenchWindowActionDelegate)getDelegate()).dispose();
	}
	/**
	 * Returns the action set id.
	 */
	public String getActionSetId() {
		return actionSetId;
	}
	/**
	 * Returns true if the window has been set.  
	 * The window may be null after the constructor is called and
	 * before the window is stored.  We cannot create the delegate
	 * at that time.
	 */
	public boolean isOkToCreateDelegate() {
		return super.isOkToCreateDelegate() && window != null && retargetAction == null;
	}

	/* (non-Javadoc)
	 * Method declared on IActionDelegate2.
	 */
	public void runWithEvent(Event event) {
		if (retargetAction == null) {
			super.runWithEvent(event);
			return;
		}

		if (event != null)
			retargetAction.runWithEvent(event);
		else
			retargetAction.run();
	}

	/**
	 * Sets the action set id.
	 */
	public void setActionSetId(String newActionSetId) {
		actionSetId = newActionSetId;
	}

	/** 
	 * The <code>WWinPluginAction</code> implementation of this method
	 * declared on <code>IAction</code> stores the help listener in
	 * a local field. The supplied listener is only used if there is
	 * no retarget action.
	 */
	public void setHelpListener(HelpListener listener) {
		localHelpListener = listener;
	}

	/**
	 * Refresh the selection for the action.
	 */
	protected void refreshSelection() {
		ISelection selection = window.getSelectionService().getSelection();
		selectionChanged(selection);
	}
}