/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.IAction;
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
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * This class extends regular plugin action with the additional requirement that
 * the delegate has to implement interface
 * {@link org.eclipse.ui.IWorkbenchWindowActionDelegate}. This interface has
 * one additional method (init) whose purpose is to initialize the delegate with
 * the window in which the action is intended to run.
 */
public class WWinPluginAction extends PluginAction implements
        IActionSetContributionItem {
    /**
     * The help listener assigned to this action, or <code>null</code> if none.
     */
    private HelpListener localHelpListener;

    private IWorkbenchWindow window;

    private String actionSetId;

    private RetargetAction retargetAction;

    private static ArrayList staticActionList = new ArrayList(50);

    /**
     * Constructs a new <code>WWinPluginAction</code> object.
     * 
     * @param actionElement the configuration element
     * @param window the window to contribute to
     * @param id the identifier
     * @param style the style
     */
    public WWinPluginAction(IConfigurationElement actionElement,
            IWorkbenchWindow window, String id, int style) {
        super(actionElement, id, style);
        this.window = window;

        // If config specifies a retarget action, create it now
        String retarget = actionElement
                .getAttribute(IWorkbenchRegistryConstants.ATT_RETARGET);
        if (retarget != null && Boolean.valueOf(retarget).booleanValue()) {
            // create a retarget action
            String allowLabelUpdate = actionElement
                    .getAttribute(IWorkbenchRegistryConstants.ATT_ALLOW_LABEL_UPDATE);
            String label = actionElement
                    .getAttribute(IWorkbenchRegistryConstants.ATT_LABEL);

            if (allowLabelUpdate != null && Boolean.valueOf(allowLabelUpdate).booleanValue()) {
				retargetAction = new LabelRetargetAction(id, label, style);
			} else {
				retargetAction = new RetargetAction(id, label, style);
			}
            retargetAction
                    .addPropertyChangeListener(new IPropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent event) {
                            if (event.getProperty().equals(IAction.ENABLED)) {
                                Object val = event.getNewValue();
                                if (val instanceof Boolean) {
                                    setEnabled(((Boolean) val).booleanValue());
                                }
                            } else if (event.getProperty().equals(
                                    IAction.CHECKED)) {
                                Object val = event.getNewValue();
                                if (val instanceof Boolean) {
                                    setChecked(((Boolean) val).booleanValue());
                                }
                            } else if (event.getProperty().equals(IAction.TEXT)) {
                                Object val = event.getNewValue();
                                if (val instanceof String) {
                                    setText((String) val);
                                }
                            } else if (event.getProperty().equals(
                                    IAction.TOOL_TIP_TEXT)) {
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
            if (activePart != null) {
				retargetAction.partActivated(activePart);
			}
        } else {
            // if we retarget the handler will look after selection changes
            window.getSelectionService().addSelectionListener(this);
            refreshSelection();
        }
        addToActionList(this);

        super.setHelpListener(new HelpListener() {
            public void helpRequested(HelpEvent e) {
                HelpListener listener = null;
                if (retargetAction != null) {
					listener = retargetAction.getHelpListener();
				}
                if (listener == null) {
					// use our own help listener
                    listener = localHelpListener;
				}
                if (listener != null) {
					// pass on the event
                    listener.helpRequested(e);
				}
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
    protected IActionDelegate validateDelegate(Object obj)
            throws WorkbenchException {
        if (obj instanceof IWorkbenchWindowActionDelegate) {
			return (IWorkbenchWindowActionDelegate) obj;
		}
        
        throw new WorkbenchException(
                "Action must implement IWorkbenchWindowActionDelegate"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * Method declared on PluginAction.
     */
    protected void initDelegate() {
        super.initDelegate();
        ((IWorkbenchWindowActionDelegate) getDelegate()).init(window);
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
        super.dispose();
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
        return super.isOkToCreateDelegate() && window != null
                && retargetAction == null;
    }

    /* (non-Javadoc)
     * Method declared on IActionDelegate2.
     */
    public void runWithEvent(Event event) {
        if (retargetAction == null) {
            super.runWithEvent(event);
            return;
        }

        if (event != null) {
			retargetAction.runWithEvent(event);
		} else {
			retargetAction.run();
		}
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

    /* (non-Javadoc)
     * Method declared on IAction.
     */
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        // This call may come from the SWT control event handler
        // itself, so notify the retarget action to keep things
        // in sync.
        if (retargetAction != null) {
			retargetAction.setChecked(checked);
		}
    }

    /**
     * Refresh the selection for the action.
     */
    protected void refreshSelection() {
        ISelection selection = window.getSelectionService().getSelection();
        selectionChanged(selection);
    }
}
