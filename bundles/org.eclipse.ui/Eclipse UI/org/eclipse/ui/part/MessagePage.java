package org.eclipse.ui.part;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;		

/**
 * A message page display a message in a pagebook view.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @see PageBookView
 */
public class MessagePage extends Page {
	private Composite pgComp;
	private Label msgLabel;
	private String message = "";//$NON-NLS-1$
/**
 * Creates a new page. The message is the empty string.
 */
public MessagePage() {
}
/* (non-Javadoc)
 * Method declared on IPage.
 */
public void createControl(Composite parent) {
	// Message in default page of Outline should have margins
	pgComp = new Composite(parent, SWT.NULL);
	pgComp.setLayout(new GridLayout());
	
	msgLabel = new Label(pgComp, SWT.LEFT | SWT.WRAP);
	msgLabel.setText(message);
	
	GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
	msgLabel.setLayoutData(gridData);
}
/* (non-Javadoc)
 * Method declared on IPage.
 */
public Control getControl() {
	return pgComp;
}
/**
 * Sets focus to a part in the page.
 */
public void setFocus() {
	msgLabel.setFocus();
}
/**
 * Sets the message to the given string.
 *
 * @param message the message text
 */
public void setMessage(String message) {
	this.message = message;
	if (msgLabel != null)
		msgLabel.setText(message);
}
}
