package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.ViewContextComputer;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * This view shows variables and their values for a particular stack frame
 */
public class VariablesView extends AbstractDebugView implements ISelectionListener, IDoubleClickListener {

	protected ShowQualifiedAction fShowQualifiedAction;
	protected ShowTypesAction fShowTypesAction;
	protected ChangeVariableValueAction fChangeVariableAction;
	protected AddToInspectorAction fAddToInspectorAction;
	protected ControlAction fCopyToClipboardAction;

	/**
	 * Remove myself as a selection listener to the <code>LaunchesView</code> in this perspective.
	 *
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose() {
		DebugUIPlugin.getDefault().removeSelectionListener(this);
		super.dispose();
	}

	/** 
	 * The <code>VariablesView</code> listens for selection changes in the <code>LaunchesView</code>
	 *
	 * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection sel) {
		if (part instanceof LaunchesView) {
			if (sel instanceof IStructuredSelection) {
				setViewerInput((IStructuredSelection)sel);
			}
		}
		if (!(part instanceof DebugView)) {
			return;
		}
		if (!(sel instanceof IStructuredSelection)) {
			return;
		}

		setViewerInput((IStructuredSelection)sel);
	}

	protected void setViewerInput(IStructuredSelection ssel) {
		IStackFrame frame= null;
		if (ssel.size() == 1) {
			Object input= ssel.getFirstElement();
			if (input != null && input instanceof IStackFrame) {
				frame= (IStackFrame)input;
			}
		}

		Object current= getViewer().getInput();
		if (current == null && frame == null) {
			return;
		}

		if (current != null && current.equals(frame)) {
			return;
		}

		((VariablesContentProvider)getViewer().getContentProvider()).clearCache();
		getViewer().setInput(frame);
	}
	
	/**
	 * @see IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		DebugUIPlugin.getDefault().addSelectionListener(this);
		TreeViewer vv = new TreeViewer(parent, SWT.MULTI);
		setViewer(vv);
		getViewer().setContentProvider(new VariablesContentProvider());
		getViewer().setLabelProvider(new DelegatingModelPresentation());
		getViewer().setUseHashlookup(true);
		getViewer().addDoubleClickListener(this);
		
		// add a context menu
		createContextMenu(vv.getTree());

		initializeActions();
		initializeToolBar();
	
		setInitialContent();
		setTitleToolTip(DebugUIMessages.getString("VariablesView.Variables_and_their_Values_for_a_Selected_Stack_Frame_1")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(parent,
			new ViewContextComputer(this, IDebugHelpContextIds.VARIABLE_VIEW ));
	}

	protected void setInitialContent() {
		IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
		if (window == null) {
			return;
		}
		IWorkbenchPage p= window.getActivePage();
		if (p == null) {
			return;
		}
		DebugView view= (DebugView) p.findView(IDebugUIConstants.ID_DEBUG_VIEW);
		if (view != null) {
			ISelectionProvider provider= view.getSite().getSelectionProvider();
			if (provider != null) {
				provider.getSelection();
				ISelection selection= provider.getSelection();
				if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
					setViewerInput((IStructuredSelection) selection);
				}
			}
		}
	}
	
	/**
	 * Initializes the actions of this view.
	 */
	protected void initializeActions() {
		setShowTypesAction(new ShowTypesAction(getViewer()));
		getShowTypesAction().setChecked(false);
		
		setShowQualifiedAction(new ShowQualifiedAction(getViewer()));
		getShowQualifiedAction().setChecked(false);
		
		setAddToInspectorAction(new AddToInspectorAction(getViewer()));
		
		setChangeVariableAction(new ChangeVariableValueAction(getViewer()));
		getChangeVariableAction().setEnabled(false);
		
		setCopyToClipboardAction(new ControlAction(getViewer(), new CopyVariablesToClipboardActionDelegate()));
	} 

	/**
	 * Configures the toolBar.
	 * 
	 * @param tbm The toolbar that will be configured
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(new Separator(this.getClass().getName()));
		tbm.add(getShowTypesAction());
		tbm.add(getShowQualifiedAction());
	}

   /**
	* Adds items to the context menu including any extension defined
	* actions.
	* 
	* @param menu The menu to add the item to.
	*/
	protected void fillContextMenu(IMenuManager menu) {

		menu.add(new Separator(IDebugUIConstants.EMPTY_VARIABLE_GROUP));
		menu.add(new Separator(IDebugUIConstants.VARIABLE_GROUP));
		menu.add(getAddToInspectorAction());
		menu.add(getChangeVariableAction());
		menu.add(getCopyToClipboardAction());
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.RENDER_GROUP));
		menu.add(getShowTypesAction());
		menu.add(getShowQualifiedAction());
		
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	/**
	 * @see IDoubleClickListener#doubleClick(DoubleClickEvent)
	 */
	public void doubleClick(DoubleClickEvent event) {
		if (getChangeVariableAction().isEnabled()) {
			getChangeVariableAction().run();
		}
	}
	
	protected AddToInspectorAction getAddToInspectorAction() {
		return fAddToInspectorAction;
	}

	protected void setAddToInspectorAction(AddToInspectorAction addToInspectorAction) {
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

