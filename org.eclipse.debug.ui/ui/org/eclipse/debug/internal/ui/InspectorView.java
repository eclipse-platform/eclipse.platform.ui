package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.ui.IDebugUIConstants;import org.eclipse.jface.action.*;import org.eclipse.jface.viewers.TreeViewer;import org.eclipse.swt.SWT;import org.eclipse.swt.events.KeyAdapter;import org.eclipse.swt.events.KeyEvent;import org.eclipse.swt.widgets.Composite;import org.eclipse.ui.IWorkbenchActionConstants;import org.eclipse.ui.help.ViewContextComputer;import org.eclipse.ui.help.WorkbenchHelp;

/**
 * A view that shows items that have been added to a inspector
 */
public class InspectorView extends AbstractDebugView {
	
	protected final static String PREFIX= "inspector_view.";

	protected InspectorContentProvider fContentProvider= null;
	protected ShowQualifiedAction fShowQualifiedAction;
	protected ShowTypesAction fShowTypesAction;
	protected InspectorViewAddToInspectorAction fAddToInspectorAction;
	protected RemoveFromInspectorAction fRemoveFromInspectorAction;
	protected RemoveAllFromInspectorAction fRemoveAllFromInspectorAction;
	protected ChangeVariableValueAction fChangeVariableAction;
	
	/**
	 * @see IWorkbenchPart
	 */
	public void createPartControl(Composite parent) {
		TreeViewer vv = new TreeViewer(parent, SWT.MULTI);
		fViewer= vv;
		initializeActions();
		initializeToolBar();
		fContentProvider= new InspectorContentProvider(fRemoveAllFromInspectorAction);
		fViewer.setContentProvider(fContentProvider);
		fViewer.setLabelProvider(new DelegatingModelPresentation());
		fViewer.setUseHashlookup(true);

		createContextMenu(vv.getTree());
		
		fViewer.setInput(fContentProvider.getInspectorList());
		fViewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});

		setTitleToolTip(getTitleToolTipText(PREFIX));
		WorkbenchHelp.setHelp(
			parent,
			new ViewContextComputer(this, IDebugHelpContextIds.INSPECTOR_VIEW ));
	}

	/**
	 * Initializes the actions of this view.
	 */
	protected void initializeActions() {
		fShowTypesAction= new ShowTypesAction(fViewer);
		fShowTypesAction.setChecked(false);
		
		fShowQualifiedAction= new ShowQualifiedAction(fViewer);
		fShowQualifiedAction.setChecked(false);
				
		fAddToInspectorAction = new InspectorViewAddToInspectorAction(fViewer);

		fRemoveFromInspectorAction= new RemoveFromInspectorAction(fViewer);

		fRemoveAllFromInspectorAction= new RemoveAllFromInspectorAction(fViewer);
		
		fChangeVariableAction= new ChangeVariableValueAction(fViewer);
		fChangeVariableAction.setEnabled(false);
	}

	/**
	 * Configures the toolBar
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(new Separator(this.getClass().getName()));
		tbm.add(fShowTypesAction);
		tbm.add(fShowQualifiedAction);
		tbm.add(new Separator(this.getClass().getName()));
		tbm.add(fRemoveFromInspectorAction);
		tbm.add(fRemoveAllFromInspectorAction);
	}

	/**
	 * Adds items to the context menu including any extension defined actions.
	 */
	protected void fillContextMenu(IMenuManager menu) {

		// Add the actions defined in this view
		menu.add(new Separator(IDebugUIConstants.EMPTY_EXPRESSION_GROUP));
		menu.add(new Separator(IDebugUIConstants.EXPRESSION_GROUP));
		menu.add(fAddToInspectorAction);
		menu.add(fChangeVariableAction);
		menu.add(fRemoveFromInspectorAction);
		menu.add(fRemoveAllFromInspectorAction);
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.RENDER_GROUP));
		menu.add(fShowTypesAction);
		menu.add(fShowQualifiedAction);
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * Adds a inspect item to the list, and sets the selection to either
	 * the first child or to the item if it has no children.
	 */
	public void addToInspector(InspectItem item) {
		fContentProvider.addToInspector(item);
	}

	/**
	 * Removes a items from the list
	 */
	public void removeFromInspector(Object object) {
		// first we have to get the root item to remove
		while (! (object instanceof InspectItem && object != null)) {
			object = fContentProvider.getParent(object);
		}
		if (object != null) {
			fContentProvider.removeFromInspector((InspectItem)object);
		}
	}

	/**
	 * Removes all items from the list
	 */
	public void removeAllFromInspector() {
		fContentProvider.removeAll();
	}
	
	/**
	 * Handles key events in viewer.  Specifically interested in
	 * the Delete key.
	 */
	protected void handleKeyPressed(KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0 
			&& fRemoveFromInspectorAction.isEnabled()) {
				fRemoveFromInspectorAction.run();
		}
	}
}

