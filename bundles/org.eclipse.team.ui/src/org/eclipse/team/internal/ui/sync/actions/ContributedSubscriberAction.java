/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.sync.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.sync.views.SubscriberInput;
import org.eclipse.team.ui.sync.SubscriberAction;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * This action delegates to an action delegate contributed through the plugin.xml
 * 
 *       <action
 *             label="%CVSWorkspaceSubscriber.commit.label"
 *             tooltip="%CVSWorkspaceSubscriber.commit.tooltip"
 *             class="org.eclipse.team.internal.ccvs.ui.subscriber.CommitAction"
 *             helpContextId="org.eclipse.team.cvs.ui.workspace_subscriber_commit_action"
 *             id="org.eclipse.team.ccvs.ui.CVSWorkspaceSubscriber.commit">
 *       </action>
 */
public class ContributedSubscriberAction extends SyncViewerAction {

	private IActionDelegate delegate;
	private IConfigurationElement element;
	private IWorkbenchPart activePart;
	private ISelection selection;
	private SubscriberInput context;

	/*
	 * NOTE: Code copied from WorkbenchPlugin.
	 * 
	 * Creates an extension.  If the extension plugin has not
	 * been loaded a busy cursor will be activated during the duration of
	 * the load.
	 *
	 * @param element the config element defining the extension
	 * @param classAttribute the name of the attribute carrying the class
	 * @returns the extension object
	 */
	private static Object createExtension(final IConfigurationElement element, final String classAttribute) throws TeamException {
		// If plugin has been loaded create extension.
		// Otherwise, show busy cursor then create extension.
		IPluginDescriptor plugin = element.getDeclaringExtension().getDeclaringPluginDescriptor();
		if (plugin.isPluginActivated()) {
			try {
				return element.createExecutableExtension(classAttribute);
			} catch (CoreException e) {
				throw TeamException.asTeamException(e);
			}
		} else {
			final Object[] ret = new Object[1];
			final TeamException[] exc = new TeamException[1];
			BusyIndicator.showWhile(null, new Runnable() {
				public void run() {
					try {
						ret[0] = element.createExecutableExtension(classAttribute);
					} catch (CoreException e) {
						exc[0] = TeamException.asTeamException(e);
					}
				}
			});
			if (exc[0] != null)
				throw exc[0];
			else
				return ret[0];
		}
	}

	private static void log(String message, TeamException e) {
		if (message == null) {
			message = e.getMessage();
			System.err.println(message);
		} else { 
			System.err.println(message + "\nReason:");
			System.err.println(e.getStatus().getMessage());
		}
		TeamUIPlugin.log(new Status(IStatus.ERROR, TeamUIPlugin.ID, 0, message, e));
	}
	
	/**
	 * @param viewer
	 * @param element
	 */
	public ContributedSubscriberAction(IViewPart viewPart, IConfigurationElement element) {
		super(viewPart, element.getAttribute("label"));
		this.element = element;
		String tooltip = element.getAttribute("tooltip");
		String helpContextId = element.getAttribute("helpContextId");
		String id = element.getAttribute("id");
		setToolTipText(tooltip);
		setId(id);
		if (helpContextId != null) {
			WorkbenchHelp.setHelp(this, helpContextId);
		}
	}

	/**
	 * Return the delegate action or null if not created yet
	 */
	private IActionDelegate getDelegate() {
		if (delegate == null) {
			createDelegate();
		}
		return delegate;
	}
	
	private void createDelegate() {
		if (delegate == null) {
			try {
				Object obj = createExtension(element, "class");
				delegate = validateDelegate(obj);
				initDelegate();
				refreshEnablement();
			} catch (TeamException e) {
				String id = element.getAttribute("id");
				log("Could not create action delegate for id: " + id, e); //$NON-NLS-1$
				return;
			}
		}
	}

	/**
	 * 
	 */
	private void refreshEnablement() {
		if (delegate != null) {
			delegate.selectionChanged(this, selection);
		}
	}

	private void initDelegate() {
		if (delegate instanceof IActionDelegate2)
			((IActionDelegate2)delegate).init(this);
		if (delegate instanceof IObjectActionDelegate && activePart != null)
			((IObjectActionDelegate)delegate).setActivePart(this, activePart);
	}
	

	/*
	 * Validates the object is a delegate of the expected type. Subclasses can
	 * override to check for specific delegate types.
	 * <p>
	 * <b>Note:</b> Calls to the object are not allowed during this method.
	 * </p>
	 *
	 * @param obj a possible action delegate implementation
	 * @return the <code>IActionDelegate</code> implementation for the object
	 * @throws a <code>WorkbenchException</code> if not expect delegate type
	 */
	private IActionDelegate validateDelegate(Object obj) throws TeamException {
		if (obj instanceof IActionDelegate)
			return (IActionDelegate)obj;
		else
			// TODO: Code in PluginAction was not NLSed. Should it be?
			throw new TeamException("Action must implement IActionDelegate"); //$NON-NLS-1$
	}
	
	/**
	 * Sets the active part for the delegate.
	 * <p>
	 * This method will be called every time the action appears in a popup menu.  The
	 * targetPart may change with each invocation.
	 * </p>
	 *
	 * @param action the action proxy that handles presentation portion of the action
	 * @param targetPart the new part target
	 */
	public void setActivePart(IWorkbenchPart targetPart) {
		activePart = targetPart;
		if (delegate != null && delegate instanceof IObjectActionDelegate)
			 ((IObjectActionDelegate) delegate).setActivePart(this, activePart);
	}
	
	/**
	 * Handles selection change. If rule-based enabled is
	 * defined, it will be first to call it. If the delegate
	 * is loaded, it will also be given a chance.
	 */
	public void selectionChanged(ISelection newSelection) {
		// Update selection.
		selection = newSelection;
		if (selection == null)
			selection = StructuredSelection.EMPTY;
			
		// If the delegate can be loaded, do so.
		// Otherwise, just update the enablement.
		if (delegate == null && isOkToCreateDelegate())
			createDelegate();
		else 
			refreshEnablement();
	}
	
	/**
	 * The <code>SelectionChangedEventAction</code> implementation of this 
	 * <code>ISelectionChangedListener</code> method calls 
	 * <code>selectionChanged(IStructuredSelection)</code> when the selection is
	 * a structured one.
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection sel = event.getSelection();
		selectionChanged(sel);
	}

	/**
	 * The <code>SelectionChangedEventAction</code> implementation of this 
	 * <code>ISelectionListener</code> method calls 
	 * <code>selectionChanged(IStructuredSelection)</code> when the selection is
	 * a structured one. Subclasses may extend this method to react to the change.
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection sel) {
		selectionChanged(sel);
	}
	
	/**
	 * Returns true if the declaring plugin has been loaded
	 * and there is no need to delay creating the delegate
	 * any more.
	 */
	private boolean isOkToCreateDelegate() {
		// test if the plugin has loaded
		IPluginDescriptor plugin =
			element.getDeclaringExtension().getDeclaringPluginDescriptor();
		return plugin.isPluginActivated();
	}
	
	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public void run() {
		runWithEvent(null);
	}
	
	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public void runWithEvent(Event event) {
		// this message dialog is problematic.
		if (delegate == null) {
			// TODO: We should create the delegate earlier since the subscriber 
			// is already loaded by the sync view.
			createDelegate();
			if (delegate == null) {
				MessageDialog.openInformation(
					Display.getDefault().getActiveShell(),
					"Information",
					"Operation Not Available");
				return;
			}
			if (!isEnabled()) {
				MessageDialog.openInformation(
					Display.getDefault().getActiveShell(),
					"Information",
					"Operation is disabled");
				return;
			}
		}

		if (event != null) {
			if (delegate instanceof IActionDelegate2) {
				((IActionDelegate2)delegate).runWithEvent(this, event);
				return;
			}
		}

		delegate.run(this);
	}

	/*
	 * Set the context 
	 * @param input
	 */
	protected void setContext(SubscriberInput input) {
		this.context = input;
		IActionDelegate delegate = getDelegate();
		if (delegate instanceof SubscriberAction) {
			((SubscriberAction)delegate).setSubscriber(context.getSubscriber());
		}
	}
	
}
