package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.help.ViewContextComputer;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * A view that shows items that have been added to a inspector
 */
public class InspectorView extends AbstractDebugView  implements IDoubleClickListener{

	private InspectorContentProvider fContentProvider;
	private ShowQualifiedAction fShowQualifiedAction;
	private ShowTypesAction fShowTypesAction;
	private InspectorViewAddToInspectorAction fAddToInspectorAction;
	private RemoveFromInspectorAction fRemoveFromInspectorAction;
	private RemoveAllFromInspectorAction fRemoveAllFromInspectorAction;
	private ChangeVariableValueAction fChangeVariableAction;
	private ControlAction fCopyToClipboardAction;
	/**
	 * @see IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		TreeViewer vv = new TreeViewer(parent, SWT.MULTI);
		setViewer(vv);
		initializeActions();
		initializeToolBar();
		setContentProvider(new InspectorContentProvider(getRemoveAllFromInspectorAction()));
		getViewer().setContentProvider(getContentProvider());
		getViewer().setLabelProvider(new DelegatingModelPresentation());
		getViewer().setUseHashlookup(true);

		createContextMenu(vv.getTree());
		
		getViewer().setInput(getContentProvider().getInspectorList());
		getViewer().getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});
		
		getViewer().addDoubleClickListener(this);

		setTitleToolTip(DebugUIMessages.getString("InspectorView.Evaluated_Expression_1")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(
			parent,
			new ViewContextComputer(this, IDebugHelpContextIds.INSPECTOR_VIEW ));
	}

	/**
	 * Initializes the actions of this view.
	 */
	protected void initializeActions() {
		setShowTypesAction(new ShowTypesAction(getViewer()));
		getShowTypesAction().setChecked(false);
		
		setShowQualifiedAction(new ShowQualifiedAction(getViewer()));
		getShowQualifiedAction().setChecked(false);
				
		setAddToInspectorAction(new InspectorViewAddToInspectorAction(getViewer()));

		setRemoveFromInspectorAction(new RemoveFromInspectorAction(getViewer()));

		setRemoveAllFromInspectorAction(new RemoveAllFromInspectorAction(getViewer()));
		
		setChangeVariableAction(new ChangeVariableValueAction(getViewer()));
		getChangeVariableAction().setEnabled(false);
		
		setCopyToClipboardAction(new ControlAction(getViewer(), new CopyVariablesToClipboardActionDelegate()));
	}

	/**
	 * Configures the toolBar
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(new Separator(this.getClass().getName()));
		tbm.add(getShowTypesAction());
		tbm.add(getShowQualifiedAction());
		tbm.add(new Separator(this.getClass().getName()));
		tbm.add(getRemoveFromInspectorAction());
		tbm.add(getRemoveAllFromInspectorAction());
	}

	/**
	 * Adds items to the context menu including any extension defined actions.
	 */
	protected void fillContextMenu(IMenuManager menu) {
		menu.add(new Separator(IDebugUIConstants.EMPTY_EXPRESSION_GROUP));
		menu.add(new Separator(IDebugUIConstants.EXPRESSION_GROUP));
		menu.add(getAddToInspectorAction());
		menu.add(getChangeVariableAction());
		menu.add(getCopyToClipboardAction());
		menu.add(getRemoveFromInspectorAction());
		menu.add(getRemoveAllFromInspectorAction());
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.RENDER_GROUP));
		menu.add(getShowTypesAction());
		menu.add(getShowQualifiedAction());
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
	
	/**
	 * Handles key events in viewer.  Specifically interested in
	 * the Delete key.
	 */
	protected void handleKeyPressed(KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0 
			&& getRemoveFromInspectorAction().isEnabled()) {
				getRemoveFromInspectorAction().run();
		}
	}
	
	/**
	 * @see IDoubleClickListener#doubleClick(DoubleClickEvent)
	 */
	public void doubleClick(DoubleClickEvent event) {
		if (getChangeVariableAction().isEnabled()) {
			getChangeVariableAction().run();
		}
	}
	
	protected InspectorContentProvider getContentProvider() {
		return fContentProvider;
	}

	protected void setContentProvider(InspectorContentProvider contentProvider) {
		fContentProvider = contentProvider;
	}

	protected InspectorViewAddToInspectorAction getAddToInspectorAction() {
		return fAddToInspectorAction;
	}

	protected void setAddToInspectorAction(InspectorViewAddToInspectorAction addToInspectorAction) {
		fAddToInspectorAction = addToInspectorAction;
	}

	protected ChangeVariableValueAction getChangeVariableAction() {
		return fChangeVariableAction;
	}

	protected void setChangeVariableAction(ChangeVariableValueAction changeVariableAction) {
		fChangeVariableAction = changeVariableAction;
	}

	protected ControlAction getCopyToClipboardAction() {
		return fCopyToClipboardAction;
	}

	protected void setCopyToClipboardAction(ControlAction copyToClipboardAction) {
		fCopyToClipboardAction = copyToClipboardAction;
	}

	protected RemoveAllFromInspectorAction getRemoveAllFromInspectorAction() {
		return fRemoveAllFromInspectorAction;
	}

	protected void setRemoveAllFromInspectorAction(RemoveAllFromInspectorAction removeAllFromInspectorAction) {
		fRemoveAllFromInspectorAction = removeAllFromInspectorAction;
	}

	protected RemoveFromInspectorAction getRemoveFromInspectorAction() {
		return fRemoveFromInspectorAction;
	}

	protected void setRemoveFromInspectorAction(RemoveFromInspectorAction removeFromInspectorAction) {
		fRemoveFromInspectorAction = removeFromInspectorAction;
	}

	protected ShowQualifiedAction getShowQualifiedAction() {
		return fShowQualifiedAction;
	}

	protected void setShowQualifiedAction(ShowQualifiedAction showQualifiedAction) {
		fShowQualifiedAction = showQualifiedAction;
	}

	protected ShowTypesAction getShowTypesAction() {
		return fShowTypesAction;
	}

	protected void setShowTypesAction(ShowTypesAction showTypesAction) {
		fShowTypesAction = showTypesAction;
	}
}