/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

/**
 * Added a Details button to the MessageDialog to show the exception
 * stack trace.
 */
public class InternalErrorDialog extends MessageDialog {

	private Throwable detail;
	private int detailButtonID = -1;
	private Text text;
	
	//Workaround. SWT does not seem to set the default button if 
	//there is not control with focus. Bug: 14668
	private int defaultButtonIndex = 0;
	
	/**
	 * Size of the text in lines.
	 */
	private static final int TEXT_LINE_COUNT = 15;

public InternalErrorDialog(
	Shell parentShell,
	String dialogTitle,
	Image dialogTitleImage,
	String dialogMessage,
	Throwable detail,
	int dialogImageType,
	String[] dialogButtonLabels,
	int defaultIndex) {
	super(
		parentShell,
		dialogTitle,
		dialogTitleImage,
		dialogMessage,
		dialogImageType,
		dialogButtonLabels,
		defaultIndex);
	defaultButtonIndex = defaultIndex;
	this.detail = detail;
	setShellStyle(getShellStyle() | SWT.APPLICATION_MODAL);
}

//Workaround. SWT does not seem to set rigth the default button if 
//there is not control with focus. Bug: 14668
public int open() {
	create();
	Button b = getButton(defaultButtonIndex);
	b.setFocus();
	b.getShell().setDefaultButton(b);
	return super.open();
}
	
/**
 * Set the detail button;
 */
public void setDetailButton(int index) {
	detailButtonID = index;
}
/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected void buttonPressed(int buttonId) {
	if(buttonId == detailButtonID) {
		toggleDetailsArea();
	} else {
		setReturnCode(buttonId);
		close();
	}
}
/**
 * Toggles the unfolding of the details area.  This is triggered by
 * the user pressing the details button.
 */
private void toggleDetailsArea() {
	Point windowSize = getShell().getSize();
	Point oldSize = getContents().computeSize(SWT.DEFAULT, SWT.DEFAULT);
	
	if (text != null) {
		text.dispose();
		text = null;
		getButton(detailButtonID).setText(IDialogConstants.SHOW_DETAILS_LABEL);
	} else {
		createDropDownText((Composite)getContents());
		getButton(detailButtonID).setText(IDialogConstants.HIDE_DETAILS_LABEL);
	}

	Point newSize = getContents().computeSize(SWT.DEFAULT, SWT.DEFAULT);
	getShell().setSize(new Point(windowSize.x, windowSize.y + (newSize.y - oldSize.y)));
}
/**
 * Create this dialog's drop-down list component.
 *
 * @param parent the parent composite
 * @return the drop-down list component
 */
protected void createDropDownText(Composite parent) {
	// create the list
	text = new Text(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
	text.setFont(parent.getFont());

	// print the stacktrace in the text field
	try {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		detail.printStackTrace(ps);
		if((detail instanceof SWTError) && (((SWTError)detail).throwable != null)) {
			ps.println("\n*** Stack trace of contained exception ***"); //$NON-NLS-1$
			((SWTError)detail).throwable.printStackTrace(ps);
		} else if((detail instanceof SWTException) && (((SWTException)detail).throwable != null)) {
			ps.println("\n*** Stack trace of contained exception ***"); //$NON-NLS-1$
			((SWTException)detail).throwable.printStackTrace(ps);
		}
		ps.flush();
		baos.flush();
		text.setText(baos.toString());
	} catch (IOException e) {
	}
		
	GridData data = new GridData(
		GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL |
		GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL);
	data.heightHint = text.getLineHeight() * TEXT_LINE_COUNT;
	text.setLayoutData(data);
}
/** 
 * Convenience method to open a simple Yes/No question dialog.
 *
 * @param parent the parent shell of the dialog, or <code>null</code> if none
 * @param title the dialog's title, or <code>null</code> if none
 * @param message the message
 * @return <code>true</code> if the user presses the OK button,
 *    <code>false</code> otherwise
 */
public static boolean openQuestion(Shell parent, String title, String message, Throwable detail,int defaultIndex) {
	String[] labels;
	if(detail == null)
		labels = new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL};
    else
		labels = new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL,IDialogConstants.SHOW_DETAILS_LABEL};

	InternalErrorDialog dialog = new InternalErrorDialog(
		parent,
		title, 
		null,	// accept the default window icon
		message,
		detail,
		QUESTION, 
		labels, 
		defaultIndex);
	if(detail != null)
	    dialog.setDetailButton(2);
	return dialog.open() == 0;
}

}
