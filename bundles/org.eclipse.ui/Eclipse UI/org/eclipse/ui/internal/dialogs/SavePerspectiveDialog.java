package org.eclipse.ui.internal.dialogs;

import org.eclipse.swt.events.*;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import java.util.*;

/**
 * The SavePerspectiveDialog can be used to get the name of a new
 * perspective or the descriptor of an old perspective.  The results
 * are returned by <code>getNewPerspName</code> and 
 * <code>getOldPersp</code>.
 */
public class SavePerspectiveDialog extends org.eclipse.jface.dialogs.Dialog
	implements ISelectionChangedListener, ModifyListener
{
	private Text text;
	private ListViewer list;
	private Button okButton;
	private PerspectiveRegistry perspReg;
	private String perspName;
	private IPerspectiveDescriptor persp;
	private IPerspectiveDescriptor initialSelection;
	private boolean ignoreSelection = false;

	final private static int LIST_WIDTH = 150;
	final private static int TEXT_WIDTH = 150;
	final private static int LIST_HEIGHT = 100;
/**
 * PerspectiveDialog constructor comment.
 */
public SavePerspectiveDialog(Shell parentShell, PerspectiveRegistry perspReg) {
	super(parentShell);
	this.perspReg = perspReg;
}
/* (non-Javadoc)
 * Method declared in Window.
 */
protected void configureShell(Shell shell) {
	super.configureShell(shell);
	shell.setText("Save Perspective As...");
}
/**
 * Add buttons to the dialog's button bar.
 *
 * @param parent the button bar composite
 */
protected void createButtonsForButtonBar(Composite parent) {
	okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	updateButtons();
	text.setFocus();
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

	// description
	Label descLabel = new Label(composite, SWT.WRAP);
	descLabel.setText("Enter or select a name to save the\ncurrent perspective as.");
	descLabel.setFont(parent.getFont());
	
	// Spacer.
	Label label = new Label(composite, SWT.NONE);
	GridData data = new GridData();
	data.heightHint = 8;
	label.setLayoutData(data);
	
	// Create name group.
	Composite nameGroup = new Composite(composite, SWT.NONE);
	nameGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL));
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	layout.marginWidth = layout.marginHeight = 0;
	nameGroup.setLayout(layout);

	// Create name label.
	label = new Label(nameGroup, SWT.NONE);
	label.setText("Name:");
	label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
	label.setFont(parent.getFont());

	// Add text field.
	text = new Text(nameGroup, SWT.BORDER);
	text.setFocus();
	data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
	data.widthHint = TEXT_WIDTH;
	text.setLayoutData(data);
	text.addModifyListener(this);

	// Spacer.
	label = new Label(composite, SWT.NONE);
	data = new GridData();
	data.heightHint = 5;
	label.setLayoutData(data);
		
	// Another label.
	label = new Label(composite, SWT.NONE);
	label.setText("Existing Perspectives:");
	label.setFont(parent.getFont());

	// Add perspective list.
	list = new ListViewer(new org.eclipse.swt.widgets.List(composite, SWT.H_SCROLL 
		| SWT.V_SCROLL | SWT.BORDER));
	list.setLabelProvider(new PerspLabelProvider());
	list.setContentProvider(new PerspContentProvider());
	list.setInput(perspReg);
	list.addSelectionChangedListener(this);

	// Set pespective list size.
	Control ctrl = list.getControl();
	GridData spec = new GridData(
		GridData.VERTICAL_ALIGN_FILL | 
		GridData.HORIZONTAL_ALIGN_FILL);
	spec.widthHint = LIST_WIDTH;
	spec.heightHint = LIST_HEIGHT;
	ctrl.setLayoutData(spec);

	// Set the initial selection
	if (initialSelection != null) {
		StructuredSelection sel = new StructuredSelection(initialSelection);
		list.setSelection(sel);
	}

	// Return results.
	return composite;
}
/**
 * Returns the target name.
 */
public IPerspectiveDescriptor getPersp() {
	return persp;
}
/**
 * Returns the target name.
 */
public String getPerspName() {
	return perspName;
}
/**
 * The user has typed some text.
 */
public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
	// Get text.
	perspName = text.getText();

	// Transfer text to persp list.
	ignoreSelection = true;
	persp = perspReg.findPerspectiveWithLabel(perspName);
	if (persp == null) {
		StructuredSelection sel = new StructuredSelection();
		list.setSelection(sel);
	} else {
		StructuredSelection sel = new StructuredSelection(persp);
		list.setSelection(sel);
	}
	ignoreSelection = false;
	
	updateButtons();
}
/**
 * Notifies that the ok button of this dialog has been pressed.
 * <p>
 * The default implementation of this framework method sets
 * this dialog's return code to <code>Window.OK</code>
 * and closes the dialog. Subclasses may override.
 * </p>
 */
protected void okPressed() {
	perspName = text.getText();
	persp = perspReg.findPerspectiveWithLabel(perspName);
	if (persp != null) {
		// Confirm ok to overwrite
		String message = "A perpective with the name '" + perspName +"' already exist. Do you want to overwrite?";
		String [] buttons= new String[] { 
			IDialogConstants.YES_LABEL,
			IDialogConstants.NO_LABEL,
			IDialogConstants.CANCEL_LABEL
		};
		MessageDialog d= new MessageDialog(
			this.getShell(),
			"Overwrite Perspective",
			null,
			message,
			MessageDialog.QUESTION,
			buttons,
			0
		);

		switch (d.open()) {
			case 0:   	//yes
				break;
			case 1:		//no
				return;
			case 2:		//cancel
				cancelPressed();
				return;
			default:
				return;
		}
	}
	
	super.okPressed();
}
/**
 * Notifies that the selection has changed.
 *
 * @param event event object describing the change
 */
public void selectionChanged(SelectionChangedEvent event) {
	// If a selection is caused by modifyText ignore it.
	if (ignoreSelection)
		return;
		
	// Get selection.
	IStructuredSelection sel = (IStructuredSelection)list.getSelection();
	persp = null;
	if (!sel.isEmpty())
		persp = (IPerspectiveDescriptor)sel.getFirstElement();

	// Transfer selection to text field.
	if (persp != null) {
		perspName = persp.getLabel();
		text.setText(perspName);		
	}

	updateButtons();
}
/**
 * Sets the initial selection in this dialog.
 *
 * @param selectedElement the perspective descriptor to select
 */
public void setInitialSelection(IPerspectiveDescriptor selectedElement) {
	initialSelection = selectedElement;
}
/**
 * Update the OK button.
 */
private void updateButtons() {
	if (okButton != null) {
		String label = text.getText();
		okButton.setEnabled(perspReg.validateLabel(label));
	}
}
}
