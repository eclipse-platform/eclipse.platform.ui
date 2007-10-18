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
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IActionDelegateWithEvent;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.SelectionEnabler;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.internal.util.Util;

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

public abstract class PluginAction extends Action implements
        ISelectionListener, ISelectionChangedListener, INullSelectionListener,
        IPluginContribution {
    private IActionDelegate delegate;

    private SelectionEnabler enabler;

    private ISelection selection;

    private IConfigurationElement configElement;

    private String pluginId;

    private String runAttribute = IWorkbenchRegistryConstants.ATT_CLASS;

    private static int actionCount = 0;

    /**
     * PluginAction constructor.
     * 
     * @param actionElement the element
     * @param id the identifier
     * @param style the style bits
     */
    public PluginAction(IConfigurationElement actionElement, String id,
            int style) {
        super(null, style);

        this.configElement = actionElement;

        if (id != null) {
            setId(id);
        } else {
            // Create unique action id.
            setId("PluginAction." + Integer.toString(actionCount)); //$NON-NLS-1$
            ++actionCount;
        }

        String defId = actionElement
                .getAttribute(IWorkbenchRegistryConstants.ATT_DEFINITION_ID);
        setActionDefinitionId(defId);

        pluginId = configElement.getNamespace();

        // Read enablement declaration.
        if (configElement.getAttribute(IWorkbenchRegistryConstants.ATT_ENABLES_FOR) != null) {
            enabler = new SelectionEnabler(configElement);
        } else {
			IConfigurationElement[] kids = configElement
					.getChildren(IWorkbenchRegistryConstants.TAG_ENABLEMENT);
			IConfigurationElement[] kids2 = configElement
					.getChildren(IWorkbenchRegistryConstants.TAG_SELECTION);
			if (kids.length > 0 || kids2.length>0) {
				enabler = new SelectionEnabler(configElement);
			}
		}

        // Give enabler or delegate a chance to adjust enable state
        selectionChanged(StructuredSelection.EMPTY);
    }

    /**
     * Creates the delegate and refreshes its enablement.
     */
    protected final void createDelegate() {
        // The runAttribute is null if delegate creation failed previously...
        if (delegate == null && runAttribute != null) {
            try {
                Object obj = WorkbenchPlugin.createExtension(configElement,
                        runAttribute);
                delegate = validateDelegate(obj);
                initDelegate();
                refreshEnablement();
            } catch (Throwable e) {
                runAttribute = null;
                IStatus status = null;
                if (e instanceof CoreException) {
                    status = ((CoreException) e).getStatus();
                } else {
                    status = StatusUtil
                            .newStatus(
                                    IStatus.ERROR,
                                    "Internal plug-in action delegate error on creation.", e); //$NON-NLS-1$
                }
                String id = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
                WorkbenchPlugin
                        .log(
                                "Could not create action delegate for id: " + id, status); //$NON-NLS-1$
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
     * @throws WorkbenchException if not of the expected delegate type
     */
    protected IActionDelegate validateDelegate(Object obj)
            throws WorkbenchException {
        if (obj instanceof IActionDelegate) {
			return (IActionDelegate) obj;
		}
        
        throw new WorkbenchException(
                "Action must implement IActionDelegate"); //$NON-NLS-1$
    }

    /** 
     * Initialize the action delegate by calling its lifecycle method.
     * Subclasses may override but must call this implementation first.
     */
    protected void initDelegate() {
        if (delegate instanceof IActionDelegate2) {
			((IActionDelegate2) delegate).init(this);
		}
    }

    /**
     * Returns the action delegate if created. Can be <code>null</code>
     * if the delegate is not created yet or if previous delegate
     * creation failed.
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
        String bundleId = configElement.getContributor().getName();
        return BundleUtility.isActive(bundleId);
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
                MessageDialog
                        .openInformation(
                                Util.getShellToParentOn(),
                                WorkbenchMessages.Information, 
                                WorkbenchMessages.PluginAction_operationNotAvailableMessage); 
                return;
            }
            if (!isEnabled()) {
                MessageDialog.openInformation(Util.getShellToParentOn(), WorkbenchMessages.Information, 
                        WorkbenchMessages.PluginAction_disabledMessage); 
                return;
            }
        }

        if (event != null) {
            if (delegate instanceof IActionDelegate2) {
                ((IActionDelegate2) delegate).runWithEvent(this, event);
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
     * 
     * @param newSelection the new selection
     */
    public void selectionChanged(ISelection newSelection) {
        // Update selection.
        selection = newSelection;
        if (selection == null) {
			selection = StructuredSelection.EMPTY;
		}

        // The selection is passed to the delegate as-is without
        // modification. If the selection needs to be modified
        // the action contributors should do so.
        
        // If the delegate can be loaded, do so.
        // Otherwise, just update the enablement.
        if (delegate == null && isOkToCreateDelegate()) {
			createDelegate();
		} else {
			refreshEnablement();
		}
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
     * For testing purposes only.
     * 
     * @return the selection 
     * @since 3.1
     */
    public ISelection getSelection() {
    	return selection;
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

    /**
     * @return the IConfigurationElement used to create this PluginAction.
     * 
     * @since 3.0
     */
    protected IConfigurationElement getConfigElement() {
        return configElement;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getLocalId()
     */
    public String getLocalId() {
        return getId();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getPluginId()
     */
    public String getPluginId() {
        return pluginId;
    }

    /**
     * Disposes the delegate, if created.
     * 
     * @since 3.1
     */
    public void disposeDelegate() {
        // avoid calling dispose() twice if the delegate implements
        // both IActionDelegate2 and IWorkbenchWindowActionDelegate
        if (getDelegate() instanceof IActionDelegate2) {
            ((IActionDelegate2) getDelegate()).dispose();
        }
        else if (getDelegate() instanceof IWorkbenchWindowActionDelegate) {
            ((IWorkbenchWindowActionDelegate) getDelegate()).dispose();
        }
        delegate = null;
    }

    /**
     * Disposes this plugin action.
     * 
     * @since 3.1
     */
    public void dispose() {
        disposeDelegate();
        selection = null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#getMenuCreator()
     */
    public IMenuCreator getMenuCreator() {
    	// now that action contribution item defers asking for the menu
    	// creator until its ready o show the menu, asking for the menu
    	// creator is time to instantiate the delegate
    	if (getDelegate()==null) {
    		createDelegate();
    	}
    	return super.getMenuCreator();
    }
}
