package org.eclipse.ui.part;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.SWT;
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
	private Text text;
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
	text = new Text(parent, SWT.WRAP | SWT.READ_ONLY);
	text.setText(message);
	text.setMenu(new Menu(text));
}
/* (non-Javadoc)
 * Method declared on IPage.
 */
public org.eclipse.swt.widgets.Control getControl() {
	return text;
}
/**
 * Sets focus to a part in the page.
 */
public void setFocus() {
	text.setFocus();
}
/**
 * Sets the message to the given string.
 *
 * @param message the message text
 */
public void setMessage(String message) {
	this.message = message;
	if (text != null)
		text.setText(message);
}
}
