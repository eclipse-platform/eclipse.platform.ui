package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.*;
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
public class WWinPluginAction extends PluginAction
	implements IActionSetContributionItem 
{
	private IWorkbenchWindow window;
	private String actionSetId;
	private RetargetAction retargetAction;
	
	private static ArrayList staticActionList = new ArrayList(50);

	/**
	 * Constructs a new WWinPluginAction object..
	 */
	public WWinPluginAction(IConfigurationElement actionElement,
		String runAttribute, IWorkbenchWindow window,String definitionId) 
	{
		super(actionElement, runAttribute);
		setActionDefinitionId(definitionId);
		this.window = window;

		// If config specifies a retarget action, create it now
		String retarget = actionElement.getAttribute(ActionDescriptor.ATT_RETARGET);
		if (retarget != null && retarget.equals("true")) {
			// create a retarget action
			String allowLabelUpdate = actionElement.getAttribute(ActionDescriptor.ATT_ALLOW_LABEL_UPDATE);
			String id = actionElement.getAttribute(ActionDescriptor.ATT_ID);
			String label = actionElement.getAttribute(ActionDescriptor.ATT_LABEL);
			
			if (allowLabelUpdate != null && allowLabelUpdate.equals("true")) 
				retargetAction = new LabelRetargetAction(id, label);
			else
				retargetAction = new RetargetAction(id, label);
			retargetAction.addPropertyChangeListener(new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					if (event.getProperty().equals(Action.ENABLED)) {
						Boolean bool = (Boolean) event.getNewValue();
						setEnabled(bool.booleanValue());
					} else if (event.getProperty().equals(Action.TEXT)) {
						String str = (String)event.getNewValue();
						setText(str);
					} else if (event.getProperty().equals(Action.TOOL_TIP_TEXT)) {
						String str = (String)event.getNewValue();
						setToolTipText(str);
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
			WWinPluginAction action = (WWinPluginAction)iter.next();
			if ((action.getDelegate() == null) && action.isOkToCreateDelegate()) {
				action.createDelegate();
				// creating the delegate also refreshes its enablement
			}
		}
	} 
	
	/** 
	 * Initialize an action delegate.
	 * Subclasses may override this.
	 */
	protected IActionDelegate initDelegate(Object obj) 
		throws WorkbenchException
	{
		if (obj instanceof IWorkbenchWindowActionDelegate) {
			IWorkbenchWindowActionDelegate winDelegate =
				(IWorkbenchWindowActionDelegate) obj;
			winDelegate.init(window);
			return winDelegate;
		} else
			throw new WorkbenchException("Action must implement IWorkbenchWindowActionDelegate"); //$NON-NLS-1$
	}

	/**
	 * Disposes of the action and any resources held.
	 */
	public void dispose() {
		removeFromActionList(this);
		if (retargetAction != null)
			window.getPartService().removePartListener(retargetAction);
		window.getSelectionService().removeSelectionListener(this);
		if (getDelegate() instanceof IWorkbenchWindowActionDelegate) {
			IWorkbenchWindowActionDelegate winDelegate =
				(IWorkbenchWindowActionDelegate) getDelegate();
			winDelegate.dispose();
		}
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
	 * Refresh the selection for the action.
	 */
	protected void refreshSelection() {
		ISelection selection = window.getSelectionService().getSelection();
		selectionChanged(selection);
	}
}