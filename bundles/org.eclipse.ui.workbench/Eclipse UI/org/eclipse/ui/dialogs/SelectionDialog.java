package org.eclipse.ui.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.internal.WorkbenchMessages;
import java.util.List;
import java.util.ArrayList;

/**
 * The abstract implementation of a selection dialog. It can be primed with
 * initial selections (<code>setInitialSelections</code>), and returns
 * the final selection (via <code>getResult</code>) after completion.
 * <p>
 * Clients may subclass this dialog to inherit its selection facilities.
 * </p>
 */
public abstract class SelectionDialog extends Dialog {
	// the final collection of selected elements, or null if this dialog was canceled
	private Object[] result;

	// a collection of the initially-selected elements
	private List initialSelections = new ArrayList();

	// title of dialog
	private String title;
	
	// message to show user
	private String message;

	static String SELECT_ALL_TITLE = WorkbenchMessages.getString("SelectionDialog.selectLabel"); //$NON-NLS-1$
	static String DESELECT_ALL_TITLE = WorkbenchMessages.getString("SelectionDialog.deselectLabel"); //$NON-NLS-1$
/**
 * Creates a dialog instance.
 * Note that the dialog will have no visual representation (no widgets)
 * until it is told to open.
 *
 * @param parentShell the parent shell
 */
protected SelectionDialog(Shell parentShell) {
	super(parentShell);
}
/* (non-Javadoc)
 * Method declared in Window.
 */
protected void configureShell(Shell shell) {
	super.configureShell(shell);
	if (title != null)
		shell.setText(title);
}
/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected void createButtonsForButtonBar(Composite parent) {
	createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
}
/**
 * Creates the message area for this dialog.
 * <p>
 * This method is provided to allow subclasses to decide where the message
 * will appear on the screen.
 * </p>
 *
 * @param parent the parent composite
 * @return the message label
 */
protected Label createMessageArea(Composite composite) {
	Label label = new Label(composite,SWT.NONE);
	if (message != null) {
		label.setText(message);
	} 
	label.setFont(composite.getFont());
	return label;
}
/**
 * Returns the initial selection in this selection dialog.
 *
 * @deprecated use getInitialElementSelections() instead
 * @return the list of initial selected elements or null 
 */
protected List getInitialSelections() {
	if(initialSelections.isEmpty())
		return null;
	else
		return getInitialElementSelections();
}

/**
 * Returns the list of initial element selections. * @return List */
protected List getInitialElementSelections(){
	return initialSelections;
}

/**
 * Returns the message for this dialog.
 *
 * @return the message for this dialog
 */
protected String getMessage() {
	return message;
}
/**
 * Returns the ok button.
 *
 * @return the ok button or <code>null</code> if the button is not created
 *  yet.
 */
public Button getOkButton() {
	return getButton(IDialogConstants.OK_ID);
}
/**
 * Returns the list of selections made by the user, or <code>null</code> if
 * the selection was canceled.
 *
 * @return the array of selected elements, or <code>null</code> if Cancel was
 *   pressed
 */
public Object[] getResult() {
	return result;
}
/**
 * Sets the initial selection in this selection dialog to the given elements.
 *
 * @param selectedElements the array of elements to select
 */
public void setInitialSelections(Object[] selectedElements) {
	initialSelections = new ArrayList(selectedElements.length);
	for (int i = 0; i < selectedElements.length; i++) 
		initialSelections.add(selectedElements[i]);
}

/**
 * Sets the initial selection in this selection dialog to the given elements.
 *
 * @param selectedElements the List of elements to select
 */
public void setInitialElementSelections(List selectedElements) {
	initialSelections = selectedElements;
}

/**
 * Sets the message for this dialog.
 *
 * @param message the message
 */
public void setMessage(String message) {
	this.message = message;
}
/**
 * Set the selections made by the user, or <code>null</code> if
 * the selection was canceled.
 *
 * @param the list of selected elements, or <code>null</code> if Cancel was
 *   pressed
 */
protected void setResult(List newResult) {
	if (newResult == null) {
		result = null;
	} else {
		result = new Object[newResult.size()];
		newResult.toArray(result);
	}
}

/**
 * Set the selections made by the user, or <code>null</code> if
 * the selection was canceled.
 * <p>
 * The selections may accessed using <code>getResult</code>.
 * </p>
 *
 * @param Object[] newResult - the new values
 * @since 2.0
 */
protected void setSelectionResult(Object[] newResult) {
	result = newResult;
}

/**
 * Sets the title for this dialog.
 *
 * @param title the title
 */
public void setTitle(String title) {
	this.title = title;
}
}
