package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * A view that shows items that have been added to a inspector
 */
public class InspectorView extends AbstractDebugView {
	
	private InspectorContentProvider fContentProvider;
	
	/**
	 * @see AbstractDebugView#createViewer(Composite)
	 */
	protected StructuredViewer createViewer(Composite parent) {
		TreeViewer vv = new TreeViewer(parent, SWT.MULTI);
		setContentProvider(new InspectorContentProvider());
		vv.setContentProvider(getContentProvider());
		vv.setLabelProvider(new DelegatingModelPresentation());
		vv.setUseHashlookup(true);
		vv.setInput(getContentProvider().getInspectorList());		
		return vv;
	}
	
	/**
	 * @see AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.INSPECTOR_VIEW;
	}

	/**
	 * @see AbstractDebugView#createActions()
	 */
	protected void createActions() {
		IAction action;
		
		action = new ShowTypesAction(getViewer());
		action.setChecked(false);
		setAction("ShowTypes", action); //$NON-NLS-1$
		
		action = new ShowQualifiedAction(getViewer());
		action.setChecked(false);
		setAction("ShowQualifiedNames", action); //$NON-NLS-1$
				
		setAction("AddToInspector", new InspectorViewAddToInspectorAction(getViewer())); //$NON-NLS-1$
		setAction(REMOVE_ACTION, new RemoveFromInspectorAction(getViewer()));
		
		action = new RemoveAllFromInspectorAction(getViewer());
		setAction("RemoveAll", action); //$NON-NLS-1$
		getContentProvider().setRemoveAllAction((RemoveAllFromInspectorAction)action);
		
		action = new ChangeVariableValueAction(getViewer());
		action.setEnabled(false);
		setAction("ChangeVariableValue", action); //$NON-NLS-1$
		setAction(DOUBLE_CLICK_ACTION, action);
		
		setAction("CopyToClipboard", new ControlAction(getViewer(), new CopyVariablesToClipboardActionDelegate())); //$NON-NLS-1$
	}

	/**
	 * Configures the toolBar
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(new Separator(this.getClass().getName()));
		tbm.add(getAction("ShowTypes")); //$NON-NLS-1$
		tbm.add(getAction("ShowQualifiedNames")); //$NON-NLS-1$
		tbm.add(new Separator(this.getClass().getName()));
		tbm.add(getAction(REMOVE_ACTION));
		tbm.add(getAction("RemoveAll")); //$NON-NLS-1$
	}

	/**
	 * Adds items to the context menu including any extension defined actions.
	 */
	protected void fillContextMenu(IMenuManager menu) {
		menu.add(new Separator(IDebugUIConstants.EMPTY_EXPRESSION_GROUP));
		menu.add(new Separator(IDebugUIConstants.EXPRESSION_GROUP));
		menu.add(getAction("AddToInspector")); //$NON-NLS-1$
		menu.add(getAction("ChangeVariableValue")); //$NON-NLS-1$
		menu.add(getAction("CopyToClipboard")); //$NON-NLS-1$
		menu.add(getAction(REMOVE_ACTION));
		menu.add(getAction("RemoveAll")); //$NON-NLS-1$
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.RENDER_GROUP));
		menu.add(getAction("ShowTypes")); //$NON-NLS-1$
		menu.add(getAction("ShowQualifiedNames")); //$NON-NLS-1$
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * Adds a inspect item to the list, and sets the selection to either
	 * the first child or to the item if it has no children.
	 */
	public void addToInspector(InspectItem item) {
		getContentProvider().addToInspector(item);
	}

	/**
	 * Removes a items from the list
	 */
	public void removeFromInspector(Object object) {
		// first we have to get the root item to remove
		while (! (object instanceof InspectItem && object != null)) {
			object = getContentProvider().getParent(object);
		}
		if (object != null) {
			getContentProvider().removeFromInspector((InspectItem)object);
		}
	}

	/**
	 * Removes all items from the list
	 */
	public void removeAllFromInspector() {
		getContentProvider().removeAll();
	}
	
	protected void setContentProvider(InspectorContentProvider contentProvider) {
		fContentProvider = contentProvider;
	}
	
	protected InspectorContentProvider getContentProvider() {
		return fContentProvider;
	}	
	

}