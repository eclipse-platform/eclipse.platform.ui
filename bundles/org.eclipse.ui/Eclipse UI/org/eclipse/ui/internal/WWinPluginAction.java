package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;

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
		window.getSelectionService().addSelectionListener(this);
		refreshSelection();
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
		return super.isOkToCreateDelegate() && window != null;
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