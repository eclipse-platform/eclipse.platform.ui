package org.eclipse.jface.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.window.Window;
import org.eclipse.jface.resource.*;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import java.util.*;

/**
 * A dialog is a specialized window used for narrow-focused communication
 * with the user.
 * <p>
 * Dialogs are usually modal. Consequently, it is generally bad practice
 * to open a dialog without a parent. A model dialog without a parent 
 * is not prevented from disappearing behind the application's other windows,
 * making it very confusing for the user.
 * </p>
 */
public abstract class Dialog extends Window {

	/**
	 * Image registry key for error image (value <code>"dialog_error_image"</code>).
	 */
	public static final String DLG_IMG_ERROR = "dialog_error_image";//$NON-NLS-1$

	/**
	 * Image registry key for info image (value <code>"dialog_info_image"</code>).
	 */
	public static final String DLG_IMG_INFO = "dialog_info_image";//$NON-NLS-1$

	/**
	 * Image registry key for question image (value <code>"dialog_question_image"</code>).
	 */
	public static final String DLG_IMG_QUESTION = "dialog_question_image";//$NON-NLS-1$

	/**
	 * Image registry key for warning image (value <code>"dialog_warning_image"</code>).
	 */
	public static final String DLG_IMG_WARNING = "dialog_warning_image";//$NON-NLS-1$

	static {
		ImageRegistry reg = JFaceResources.getImageRegistry();
		reg.put(DLG_IMG_ERROR, ImageDescriptor.createFromFile(Dialog.class, "images/error.gif"));//$NON-NLS-1$
		reg.put(DLG_IMG_INFO, ImageDescriptor.createFromFile(Dialog.class, "images/inform.gif"));//$NON-NLS-1$
		reg.put(DLG_IMG_QUESTION, ImageDescriptor.createFromFile(Dialog.class, "images/question.gif"));//$NON-NLS-1$
		reg.put(DLG_IMG_WARNING, ImageDescriptor.createFromFile(Dialog.class, "images/warning.gif"));//$NON-NLS-1$
	}

	/**
	 * The dialog area; <code>null</code> until dialog is layed out.
	 */
	private Control dialogArea;

	/**
	 * The button bar; <code>null</code> until dialog is layed out.
	 */
	private Control buttonBar;

	/**
	 * Horizontal dialog units.
	 */
	private double horizontalDialogUnitSize;

	/**
	 * Vertical dialog units.
	 */
	private double verticalDialogUnitSize;
/**
 * Creates a dialog instance.
 * Note that the window will have no visual representation (no widgets)
 * until it is told to open. 
 * By default, <code>open</code> blocks for dialogs.
 *
 * @param parentShell the parent shell, or <code>null</code> to create a top-level shell
 */
protected Dialog(Shell parentShell) {
	super(parentShell);
	setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	setBlockOnOpen(true);
}
/**
 * Notifies that this dialog's button with the given id has been pressed.
 * <p>
 * The <code>Dialog</code> implementation of this framework method calls
 * <code>okPressed</code> if the ok button is the pressed,
 * and <code>cancelPressed</code> if the cancel button is the pressed.
 * All other button presses are ignored. Subclasses may override
 * to handle other buttons, but should call <code>super.buttonPressed</code>
 * if the default handling of the ok and cancel buttons is desired.
 * </p>
 *
 * @param buttonId the id of the button that was pressed (see
 *  <code>IDialogConstants.*_ID</code> constants)
 */
protected void buttonPressed(int buttonId) {
	if (IDialogConstants.OK_ID == buttonId) 
		okPressed();
	else if (IDialogConstants.CANCEL_ID == buttonId) 
		cancelPressed();
}
/**
 * Notifies that the cancel button of this dialog has been pressed.
 * <p>
 * The <code>Dialog</code> implementation of this framework method sets
 * this dialog's return code to <code>Window.CANCEL</code>
 * and closes the dialog. Subclasses may override if desired.
 * </p>
 */
protected void cancelPressed() {
	setReturnCode(CANCEL);
	close();
}
/**
 * Returns the number of pixels corresponding to the
 * height of the given number of characters.
 * <p>
 * This method may only be called after <code>initializeDialogUnits</code>
 * has been called.
 * </p>
 * <p>
 * Clients may call this framework method, but should not override it.
 * </p>
 *
 * @param chars the number of characters
 * @return the number of pixels
 */
protected int convertHeightInCharsToPixels(int chars) {
	return convertVerticalDLUsToPixels(chars * 8);
}
/**
 * Returns the number of pixels corresponding to the
 * given number of horizontal dialog units.
 * <p>
 * This method may only be called after <code>initializeDialogUnits</code>
 * has been called.
 * </p>
 * <p>
 * Clients may call this framework method, but should not override it.
 * </p>
 *
 * @param dlus the number of horizontal dialog units
 * @return the number of pixels
 */
protected int convertHorizontalDLUsToPixels(int dlus) {
	return (int)Math.round(dlus * horizontalDialogUnitSize);
}
/**
 * Returns the number of pixels corresponding to the
 * given number of vertical dialog units.
 * <p>
 * This method may only be called after <code>initializeDialogUnits</code>
 * has been called.
 * </p>
 * <p>
 * Clients may call this framework method, but should not override it.
 * </p>
 *
 * @param dlus the number of vertical dialog units
 * @return the number of pixels
 */
protected int convertVerticalDLUsToPixels(int dlus) {
	return (int)Math.round(dlus * verticalDialogUnitSize);
}
/**
 * Returns the number of pixels corresponding to the
 * width of the given number of characters.
 * <p>
 * This method may only be called after <code>initializeDialogUnits</code>
 * has been called.
 * </p>
 * <p>
 * Clients may call this framework method, but should not override it.
 * </p>
 *
 * @param chars the number of characters
 * @return the number of pixels
 */
protected int convertWidthInCharsToPixels(int chars) {
	return convertHorizontalDLUsToPixels(chars * 4);
}
/**
 * Creates a new button with the given id.
 * <p>
 * The <code>Dialog</code> implementation of this framework method
 * creates a standard push button, registers for selection events
 * including button presses and registers
 * default buttons with its shell.
 * The button id is stored as the buttons client data.
 * Note that the parent's layout is assumed to be a GridLayout and 
 * the number of columns in this layout is incremented.
 * Subclasses may override.
 * </p>
 *
 * @param parent the parent composite
 * @param id the id of the button (see
 *  <code>IDialogConstants.*_ID</code> constants 
 *  for standard dialog button ids)
 * @param label the label from the button
 * @param defaultButton <code>true</code> if the button is to be the
 *   default button, and <code>false</code> otherwise
 */
protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
	// increment the number of columns in the button bar
	((GridLayout)parent.getLayout()).numColumns++;

	Button button = new Button(parent, SWT.PUSH);

	button.setText(label);
	GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
	data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
	int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
	data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	button.setLayoutData(data);
	
	button.setData(new Integer(id));
	button.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			buttonPressed(((Integer) event.widget.getData()).intValue());
		}
	});
	if (defaultButton) {
		Shell shell = parent.getShell();
		if (shell != null) {
			shell.setDefaultButton(button);
		}
	}
	button.setFont(parent.getFont());
	return button;
}
/**
 * Creates and returns the contents of this dialog's 
 * button bar.
 * <p>
 * The <code>Dialog</code> implementation of this framework method
 * lays out a button bar and calls the <code>createButtonsForButtonBar</code>
 * framework method to populate it. Subclasses may override.
 * </p>
 *
 * @param parent the parent composite to contain the button bar
 * @return the button bar control
 */
protected Control createButtonBar(Composite parent) {
	Composite composite = new Composite(parent, SWT.NONE);

	// create a layout with spacing and margins appropriate for the font size.
	GridLayout layout = new GridLayout();
	layout.numColumns = 0; // this is incremented by createButton
	layout.makeColumnsEqualWidth = true;
	layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
	layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
	layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
	layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);

	composite.setLayout(layout);

	GridData data = new GridData(
		GridData.HORIZONTAL_ALIGN_END |
		GridData.VERTICAL_ALIGN_CENTER);
	composite.setLayoutData(data);

	composite.setFont(parent.getFont());
	
	// Add the buttons to the button bar.
	createButtonsForButtonBar(composite);

	return composite;
}
/**
 * Adds buttons to this dialog's button bar.
 * <p>
 * The <code>Dialog</code> implementation of this framework method adds 
 * standard ok and cancel buttons using the <code>createButton</code>
 * framework method. Subclasses may override.
 * </p>
 *
 * @param parent the button bar composite
 */
protected void createButtonsForButtonBar(Composite parent) {
	// create OK and Cancel buttons by default
	createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
}
/**
 * The <code>Dialog</code> implementation of this <code>Window</code> method 
 * creates and lays out the top level composite for the dialog, and
 * determines the appropriate horizontal and vertical dialog units
 * based on the font size. It then calls the <code>createDialogArea</code>
 * and <code>createButtonBar</code> methods to create the dialog area
 * and button bar, respectively. Overriding <code>createDialogArea</code> and
 * <code>createButtonBar</code> are recommended rather than overriding
 * this method.
 */
protected Control createContents(Composite parent) {
	// create the top level composite for the dialog
	Composite composite = new Composite(parent, 0);
	GridLayout layout = new GridLayout();
	layout.marginHeight = 0;
	layout.marginWidth = 0;
	layout.verticalSpacing = 0;
	composite.setLayout(layout);
	composite.setLayoutData(new GridData(GridData.FILL_BOTH));
	composite.setFont(JFaceResources.getDialogFont());

	// initialize the dialog units
	initializeDialogUnits(composite);
	
	// create the dialog area and button bar
	dialogArea = createDialogArea(composite);
	buttonBar = createButtonBar(composite);
	
	
	return composite;
}
/**
 * Creates and returns the contents of the upper part 
 * of this dialog (above the button bar).
 * <p>
 * The <code>Dialog</code> implementation of this framework method
 * creates and returns a new <code>Composite</code> with
 * standard margins and spacing. Subclasses should override.
 * </p>
 *
 * @param the parent composite to contain the dialog area
 * @return the dialog area control
 */
protected Control createDialogArea(Composite parent) {
	// create a composite with standard margins and spacing
	Composite composite = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
	layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
	layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
	layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
	composite.setLayout(layout);
	composite.setLayoutData(new GridData(GridData.FILL_BOTH));
	composite.setFont(parent.getFont());

	return composite;
}
/**
 * Returns the button bar control.
 * <p>
 * Clients may call this framework method, but should not override it.
 * </p>
 *
 * @return the button bar, or <code>null</code> if
 * the button bar has not been created yet
 */
protected Control getButtonBar() {
	return buttonBar;
}
/**
 * Returns the dialog area control.
 * <p>
 * Clients may call this framework method, but should not override it.
 * </p>
 *
 * @return the dialog area, or <code>null</code> if
 *   the dialog area has not been created yet
 */
protected Control getDialogArea() {
	return dialogArea;
}
/**
 * Returns the standard dialog image with the given key.
 * Note that these images are managed by the dialog framework, 
 * and must not be disposed by another party.
 *
 * @param key one of the <code>Dialog.DLG_IMG_* </code> constants
 * @return the standard dialog image
 */
public static Image getImage(String key) {
	return JFaceResources.getImageRegistry().get(key);
}
/**
 * Initializes the values of the horizontal and vertical dialog units
 * based on the size of current font.
 * <p>
 * This method must be called before any of the dialog unit based
 * conversion methods are called.
 * </p>
 *
 * @param control a control from which to obtain the current font
 */
protected void initializeDialogUnits(Control control) {
	GC gc= new GC(control);
	gc.setFont(control.getFont());
	int averageWidth= gc.getFontMetrics().getAverageCharWidth();
	int height = gc.getFontMetrics().getHeight();
	gc.dispose();

	horizontalDialogUnitSize = averageWidth * 0.25;
	verticalDialogUnitSize = height * 0.125;
}
/**
 * Notifies that the ok button of this dialog has been pressed.
 * <p>
 * The <code>Dialog</code> implementation of this framework method sets
 * this dialog's return code to <code>Window.OK</code>
 * and closes the dialog. Subclasses may override.
 * </p>
 */
protected void okPressed() {
	setReturnCode(OK);
	close();
}
}
