package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2002. All Rights Reserved.
 * Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font should be
 * activated and used by other components.
 */
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.registry.IViewDescriptor;

/**
 * A dialog for perspective creation
 */
public class SelectPerspectiveDialog extends org.eclipse.jface.dialogs.Dialog
	implements ISelectionChangedListener
{
	private TableViewer list;
	private IPerspectiveRegistry perspReg;
	private IPerspectiveDescriptor perspDesc;
	private Button okButton;
	private Button cancelButton;

	final private static int LIST_WIDTH = 200;
	final private static int LIST_HEIGHT = 200;
/**
 * PerspectiveDialog constructor comment.
 */
public SelectPerspectiveDialog(Shell parentShell, IPerspectiveRegistry perspReg) {
	super(parentShell);
	this.perspReg = perspReg;
}
/**
 * Notifies that the cancel button of this dialog has been pressed.
 */
protected void cancelPressed() {
	perspDesc = null;
	super.cancelPressed();
}
/* (non-Javadoc)
 * Method declared in Window.
 */
protected void configureShell(Shell shell) {
	super.configureShell(shell);
	shell.setText(WorkbenchMessages.getString("SelectPerspective.shellTitle")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(shell, IHelpContextIds.SELECT_PERSPECTIVE_DIALOG);
}
/**
 * Adds buttons to this dialog's button bar.
 * <p>
 * The default implementation of this framework method adds 
 * standard ok and cancel buttons using the <code>createButton</code>
 * framework method. Subclasses may override.
 * </p>
 *
 * @param parent the button bar composite
 */
protected void createButtonsForButtonBar(Composite parent) {
	okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	cancelButton = createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
}
/**
 * Creates and returns the contents of the upper part 
 * of this dialog (above the button bar).
 *
 * @param the parent composite to contain the dialog area
 * @return the dialog area control
 */
protected Control createDialogArea(Composite parent) {
	// Run super.
	Composite composite = (Composite)super.createDialogArea(parent);
	
	// Add perspective list.
	list = new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
	list.getTable().setFont(parent.getFont());
	list.setLabelProvider(new PerspLabelProvider());
	list.setContentProvider(new PerspContentProvider());
	list.setSorter(new ViewerSorter() {});
	list.setInput(perspReg);
	list.addSelectionChangedListener(this);
	list.addDoubleClickListener(new IDoubleClickListener() {
		public void doubleClick(DoubleClickEvent event) {
			handleDoubleClickEvent();
		}
	});

	// Set list layout.
	Control ctrl = list.getControl();
	GridData spec = new GridData(GridData.FILL_BOTH);
	spec.widthHint = LIST_WIDTH;
	spec.heightHint = LIST_HEIGHT;
	ctrl.setLayoutData(spec);
	ctrl.setFont(parent.getFont());

	// Return results.
	return composite;
}
/**
 * Returns the current selection.
 */
public IPerspectiveDescriptor getSelection() {
	return perspDesc;
}
/**
 * Handle a double click event on the list
 */
protected void handleDoubleClickEvent() {
	okPressed();
}
/**
 * Notifies that the selection has changed.
 *
 * @param event event object describing the change
 */
public void selectionChanged(SelectionChangedEvent event) {
	updateSelection();
	updateButtons();
}
/**
 * Update the button enablement state.
 */
protected void updateButtons() {
	okButton.setEnabled(getSelection() != null);	
}
/**
 * Update the selection object.
 */
protected void updateSelection() {
	perspDesc = null;
	IStructuredSelection sel = (IStructuredSelection)list.getSelection();
	if (!sel.isEmpty()) {
		Object obj = sel.getFirstElement();
		if (obj instanceof IPerspectiveDescriptor)
			perspDesc = (IPerspectiveDescriptor)obj;
	}
}
}
