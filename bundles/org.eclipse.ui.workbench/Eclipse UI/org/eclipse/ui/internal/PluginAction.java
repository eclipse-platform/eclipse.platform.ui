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
import java.util.List;

import org.eclipse.core.internal.plugins.ConfigurationElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IActionDelegateWithEvent;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.WorkbenchException;

/**
 * A PluginAction is a proxy for an action extension.
 *
 * At startup we read the registry and create a PluginAction for each action extension.
 * This plugin action looks like the real action ( label, icon, etc ) and acts as
 * a proxy for the action until invoked.  At that point the proxy will instantiate 
 * the real action and delegate the run method to the real action.
 * This makes it possible to load the action extension lazily.
 *
 * Occasionally the class will ask if it is OK to 
 * load the delegate (on selection changes).  If the plugin containing
 * the action extension has been loaded then the action extension itself
 * will be instantiated.
 */

public abstract class PluginAction extends Action 
	implements ISelectionListener, ISelectionChangedListener, INullSelectionListener 
{
	private IActionDelegate delegate;
	private SelectionEnabler enabler;
	private ISelection selection;
	private IConfigurationElement configElement;
	private String runAttribute;
	private static int actionCount = 0;

	//a boolean that returns whether or not this action
	//is Adaptable - i.e. is defined on a resource type
	boolean isAdaptableAction = false;
	boolean adaptableNotChecked = true;

	/**
	 * PluginAction constructor.
	 */
	public PluginAction(IConfigurationElement actionElement, String runAttribute, String definitionId, int style) {
		super(null, style);

		// Create unique action id.
		setId("PluginAction." + Integer.toString(actionCount)); //$NON-NLS-1$
		++actionCount;
		setActionDefinitionId(definitionId);
				
		this.configElement = actionElement;
		this.runAttribute = runAttribute;
		
		// Read enablement declaration.
		if (configElement.getAttribute(PluginActionBuilder.ATT_ENABLES_FOR) != null) {
			enabler = new SelectionEnabler(configElement);
		} else {
			IConfigurationElement[] kids = configElement.getChildren(PluginActionBuilder.TAG_ENABLEMENT);
			if (kids.length > 0)
				enabler = new SelectionEnabler(configElement);
		}

		// Give enabler or delegate a chance to adjust enable state
		selectionChanged(new StructuredSelection());
	}

	/**
	 * Creates the delegate and refreshes its enablement.
	 */
	protected final void createDelegate() {
		if (delegate == null) {
			try {
				Object obj = WorkbenchPlugin.createExtension(configElement, runAttribute);
				delegate = validateDelegate(obj);
				initDelegate();
				refreshEnablement();
			} catch (CoreException e) {
				WorkbenchPlugin.log("Could not create action delegate.", e.getStatus()); //$NON-NLS-1$
				return;
			}
		}
	}

	/**
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
	protected IActionDelegate validateDelegate(Object obj) throws WorkbenchException {
		if (obj instanceof IActionDelegate)
			return (IActionDelegate)obj;
		else
			throw new WorkbenchException("Action must implement IActionDelegate"); //$NON-NLS-1$
	}

	/** 
	 * Initialize the action delegate by calling its lifecycle method.
	 * Subclasses may override but must call this implementation first.
	 */
	protected void initDelegate() {
		if (delegate instanceof IActionDelegate2)
			((IActionDelegate2)delegate).init(this);
	}
	
	/**
	 * Return the delegate action or null if not created yet
	 */
	protected IActionDelegate getDelegate() {
		return delegate;
	}

	/**
	 * Returns true if the declaring plugin has been loaded
	 * and there is no need to delay creating the delegate
	 * any more.
	 */
	protected boolean isOkToCreateDelegate() {
		// test if the plugin has loaded
		IPluginDescriptor plugin =
			configElement.getDeclaringExtension().getDeclaringPluginDescriptor();
		return plugin.isPluginActivated();
	}

	/**
	 * Return whether or not this action could have been registered
	 * due to an adaptable - i.e. it is a resource type.
	 */
	private boolean hasAdaptableType() {
		if (adaptableNotChecked) {
			Object parentConfig = ((ConfigurationElement) configElement).getParent();
			String typeName = null;
			if(parentConfig != null && parentConfig instanceof IConfigurationElement) 
				typeName = ((IConfigurationElement) parentConfig).getAttribute("objectClass"); //$NON-NLS-1$
			
			//See if this is typed at all first
			if(typeName == null){
				adaptableNotChecked = false;
				return false;
			}
			Class resourceClass = IResource.class;

			if (typeName.equals(resourceClass.getName())) {
				isAdaptableAction = true;
				adaptableNotChecked = false;
				return isAdaptableAction;
			}
			Class[] children = resourceClass.getDeclaredClasses();
			for (int i = 0; i < children.length; i++) {
				if (children[i].getName().equals(typeName)) {
					isAdaptableAction = true;
					adaptableNotChecked = false;
					return isAdaptableAction;
				}				
			}
			adaptableNotChecked = false;
		}
		return isAdaptableAction;
	}

	/**
	 * Refresh the action enablement.
	 */
	protected void refreshEnablement() {
		if (enabler != null) {
			setEnabled(enabler.isEnabledForSelection(selection));
		}
		if (delegate != null) {
			delegate.selectionChanged(this, selection);
		}
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
			createDelegate();
			if (delegate == null) {
				MessageDialog.openInformation(
					Display.getDefault().getActiveShell(),
					WorkbenchMessages.getString("Information"), //$NON-NLS-1$
					WorkbenchMessages.getString("PluginAction.operationNotAvailableMessage")); //$NON-NLS-1$
				return;
			}
			if (!isEnabled()) {
				MessageDialog.openInformation(
					Display.getDefault().getActiveShell(),
					WorkbenchMessages.getString("Information"), //$NON-NLS-1$
					WorkbenchMessages.getString("PluginAction.disabledMessage")); //$NON-NLS-1$
				return;
			}
		}

		if (event != null) {
			if (delegate instanceof IActionDelegate2) {
				((IActionDelegate2)delegate).runWithEvent(this, event);
				return;
			}
			// Keep for backward compatibility with R2.0
			if (delegate instanceof IActionDelegateWithEvent) {
				((IActionDelegateWithEvent) delegate).runWithEvent(this, event);
				return;
			}
		}

		delegate.run(this);
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
		if (hasAdaptableType())
			selection = getResourceAdapters(selection);
			
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
	 * Get a new selection with the resource adaptable version 
	 * of this selection
	 */
	private ISelection getResourceAdapters(ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			List adaptables = new ArrayList();
			Object[] elements = ((IStructuredSelection)sel).toArray();
			for (int i = 0; i < elements.length; i++) {
				Object originalValue = elements[i];
				if (originalValue instanceof IAdaptable) {
					Object adaptedValue = ((IAdaptable)originalValue).getAdapter(IResource.class);
					if (adaptedValue != null)
						adaptables.add(adaptedValue);
				}
			}
			return new StructuredSelection(adaptables);
		} else {
			return sel;
		}
	}

	/**
	 * Returns the action identifier this action overrides.
	 * Default implementation returns <code>null</code>.
	 * 
	 * @return the action identifier to override or <code>null</code>
	 */
	public String getOverrideActionId() {
		return null;
	}
}