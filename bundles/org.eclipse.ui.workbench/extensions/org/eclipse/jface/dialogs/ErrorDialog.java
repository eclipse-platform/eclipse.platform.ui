package org.eclipse.jface.dialogs;

/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font should
	be activated and used by other components.
 */
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * A dialog to display one or more errors to the user, as contained in an
 * <code>IStatus</code> object.  If an error contains additional detailed
 * information then a Details button is automatically supplied, which shows
 * or hides an error details viewer when pressed by the user.
 *
 * @see org.eclipse.core.runtime.IStatus
 */
public class ErrorDialog extends IconAndMessageDialog {

	/**
	 * Reserve room for this many list items.
	 */
	private static final int LIST_ITEM_COUNT = 7;

	/**
	 * The Details button.
	 */
	private Button detailsButton;

	/**
	 * The title of the dialog.
	 */
	private String title;

	/**
	 * The SWT list control that displays the error details.
	 */
	private List list;

	/**
	 * Indicates whether the error details viewer is currently created.
	 */
	private boolean listCreated = false;

	/**
	 * Filter mask for determining which status items to display.
	 */
	private int displayMask = 0xFFFF;

	/** 
	 * The main status object.
	 */
	private IStatus status;

	/**
	 * List of the main error object's detailed errors
	 * (element type: <code>IStatus</code>).
	 */
	private java.util.List statusList;
	/**
	 * Creates an error dialog.
	 * Note that the dialog will have no visual representation (no widgets)
	 * until it is told to open.
	 * <p>
	 * Normally one should use <code>openError</code> to create and open one of these.
	 * This constructor is useful only if the error object being displayed contains child
	 * items <it>and</it> you need to specify a mask which will be used to filter the
	 * displaying of these children.
	 * </p>
	 *
	 * @param parentShell the shell under which to create this dialog
	 * @param dialogTitle the title to use for this dialog,
	 *   or <code>null</code> to indicate that the default title should be used
	 * @param message the message to show in this dialog, 
	 *   or <code>null</code> to indicate that the error's message should be shown
	 *   as the primary message
	 * @param status the error to show to the user
	 * @param displayMask the mask to use to filter the displaying of child items,
	 *   as per <code>IStatus.matches</code>
	 * @see org.eclipse.core.runtime.IStatus#matches
	 */
	public ErrorDialog(
		Shell parentShell,
		String dialogTitle,
		String message,
		IStatus status,
		int displayMask) {
		super(parentShell);
			this.title = dialogTitle == null ? JFaceResources.getString("Problem_Occurred") : //$NON-NLS-1$
	dialogTitle;
		this.message = message == null ? status.getMessage() : JFaceResources.format("Reason", new Object[] { message, status.getMessage()}); //$NON-NLS-1$
		this.status = status;
		statusList = Arrays.asList(status.getChildren());
		this.displayMask = displayMask;
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 * Handles the pressing of the Ok or Details button in this dialog.
	 * If the Ok button was pressed then close this dialog.  If the Details
	 * button was pressed then toggle the displaying of the error details area.
	 * Note that the Details button will only be visible if the error being
	 * displayed specifies child details.
	 */
	protected void buttonPressed(int id) {
		if (id == IDialogConstants.DETAILS_ID) {
			// was the details button pressed?
			toggleDetailsArea();
		} else {
			super.buttonPressed(id);
		}
	}
	/* (non-Javadoc)
	 * Method declared in Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Details buttons
		createButton(
			parent,
			IDialogConstants.OK_ID,
			IDialogConstants.OK_LABEL,
			true);
		if (status.isMultiStatus()) {
			detailsButton =
				createButton(
					parent,
					IDialogConstants.DETAILS_ID,
					IDialogConstants.SHOW_DETAILS_LABEL,
					false);
		}
	}

	/**
	 * This implementation of the <code>Dialog</code> framework 
	 * method creates and lays out a composite and calls 
	 * <code>createMessageArea</code> and <code>createCustomArea</code>
	 * to populate it. Subclasses should override <code>createCustomArea</code>
	 * to add contents below the message.
	 */
	protected Control createDialogArea(Composite parent) {

		createMessageArea(parent);

		// create a composite with standard margins and spacing
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight =
			convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth =
			convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing =
			convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing =
			convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData childData = new GridData(GridData.FILL_BOTH);
		childData.horizontalSpan = 2;
		composite.setLayoutData(childData);
		composite.setFont(parent.getFont());
		return composite;

	}

	/*
	 * @see IconAndMessageDialog#createDialogAndButtonArea(Composite)
	 */

	protected void createDialogAndButtonArea(Composite parent) {

		super.createDialogAndButtonArea(parent);
		if (this.dialogArea instanceof Composite) {
			//Create a label if there are no children to force a smaller layout
			Composite dialogComposite = (Composite) dialogArea;
			if (dialogComposite.getChildren().length == 0)
				new Label(dialogComposite, SWT.NULL);
		}
	}

	/*
	 * @see IconAndMessageDialog#getImage()
	 */
	protected Image getImage() {

		if (status != null) {
			if (status.getSeverity() == IStatus.WARNING)
				return JFaceResources.getImageRegistry().get(DLG_IMG_WARNING);
			if (status.getSeverity() == IStatus.INFO)
				return JFaceResources.getImageRegistry().get(DLG_IMG_INFO);
		}

		//If it was not a warning or an error then return the error image
		return JFaceResources.getImageRegistry().get(DLG_IMG_ERROR);
	}

	/**
	 * Create this dialog's drop-down list component.
	 *
	 * @param parent the parent composite
	 * @return the drop-down list component
	 */
	protected List createDropDownList(Composite parent) {
		// create the list
		list =
			new List(
				parent,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);

		// fill the list
		populateList(list);

		GridData data =
			new GridData(
				GridData.HORIZONTAL_ALIGN_FILL
					| GridData.GRAB_HORIZONTAL
					| GridData.VERTICAL_ALIGN_FILL
					| GridData.GRAB_VERTICAL);
		data.heightHint = list.getItemHeight() * LIST_ITEM_COUNT;
		data.horizontalSpan = 2;
		list.setLayoutData(data);
		list.setFont(parent.getFont());
		listCreated = true;
		return list;
	}
	/* (non-Javadoc)
	 * Method declared on Window.
	 */
	/**
	 * Extends <code>Window.open()</code>.
	 * Opens an error dialog to display the error.
	 * If you specified a mask to filter the displaying of these
	 * children, the error dialog will only be displayed if there is at
	 * least one child status matching the mask.
	 */
	public int open() {
		if (shouldDisplay(status, displayMask)) {
			return super.open();
		}
		return 0;
	}
	/**
	 * Opens an error dialog to display the given error.  Use this method if the
	 * error object being displayed does not contain child items, or if you
	 * wish to display all such items without filtering.
	 *
	 * @param parent the parent shell of the dialog, or <code>null</code> if none
	 * @param dialogTitle the title to use for this dialog,
	 *   or <code>null</code> to indicate that the default title should be used
	 * @param message the message to show in this dialog, 
	 *   or <code>null</code> to indicate that the error's message should be shown
	 *   as the primary message
	 * @param status the error to show to the user
	 * @return the code of the button that was pressed that resulted in this dialog
	 *     closing.  This will be <code>Dialog.OK</code> if the OK button was 
	 * 	   pressed, or <code>Dialog.CANCEL</code> if this dialog's close window 
	 *     decoration or the ESC key was used.
	 */
	public static int openError(
		Shell parent,
		String dialogTitle,
		String message,
		IStatus status) {
		return openError(
			parent,
			dialogTitle,
			message,
			status,
			IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR);
	}
	/**
	 * Opens an error dialog to display the given error.  Use this method if the
	 * error object being displayed contains child items <it>and</it> you wish to
	 * specify a mask which will be used to filter the displaying of these
	 * children.  The error dialog will only be displayed if there is at
	 * least one child status matching the mask.
	 *
	 * @param parentShell the parent shell of the dialog, or <code>null</code> if none
	 * @param dialogTitle the title to use for this dialog,
	 *   or <code>null</code> to indicate that the default title should be used
	 * @param message the message to show in this dialog, 
	 *   or <code>null</code> to indicate that the error's message should be shown
	 *   as the primary message
	 * @param status the error to show to the user
	 * @param displayMask the mask to use to filter the displaying of child items,
	 *   as per <code>IStatus.matches</code>
	 * @return the code of the button that was pressed that resulted in this dialog
	 *     closing.  This will be <code>Dialog.OK</code> if the OK button was 
	 * 	   pressed, or <code>Dialog.CANCEL</code> if this dialog's close window 
	 *     decoration or the ESC key was used.
	 * @see org.eclipse.core.runtime.IStatus#matches
	 */
	public static int openError(
		Shell parentShell,
		String title,
		String message,
		IStatus status,
		int displayMask) {
		ErrorDialog dialog =
			new ErrorDialog(parentShell, title, message, status, displayMask);
		return dialog.open();
	}
	/**
	 * Populates the list using this error dialog's status object.
	 * This walks the child stati of the status object and
	 * displays them in a list. The format for each entry is
	 *		status_path : status_message
	 * If the status's path was null then it (and the colon)
	 * are omitted.
	 */
	private void populateList(List list) {
		Iterator enum = statusList.iterator();
		while (enum.hasNext()) {
			IStatus childStatus = (IStatus) enum.next();
			populateList(list, childStatus, 0);
		}
	}
	private void populateList(List list, IStatus status, int nesting) {
		if (!status.matches(displayMask)) {
			return;
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < nesting; i++) {
			sb.append("  "); //$NON-NLS-1$
		}
		sb.append(status.getMessage());
		list.add(sb.toString());
		IStatus[] children = status.getChildren();
		for (int i = 0; i < children.length; i++) {
			populateList(list, children[i], nesting + 1);
		}
	}
	/**
	 * Returns whether the given status object should be displayed.
	 *
	 * @param status a status object
	 * @param mask a mask as per <code>IStatus.matches</code>
	 * @return <code>true</code> if the given status should be displayed, 
	 *   and <code>false</code> otherwise
	 * @see org.eclipse.core.runtime.IStatus#matches
	 */
	protected static boolean shouldDisplay(IStatus status, int mask) {
		IStatus[] children = status.getChildren();
		if (children == null || children.length == 0) {
			return status.matches(mask);
		}
		for (int i = 0; i < children.length; i++) {
			if (children[i].matches(mask))
				return true;
		}
		return false;
	}
	/**
	 * Toggles the unfolding of the details area.  This is triggered by
	 * the user pressing the details button.
	 */
	private void toggleDetailsArea() {
		Point windowSize = getShell().getSize();
		Point oldSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);

		if (listCreated) {
			list.dispose();
			listCreated = false;
			detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
		} else {
			list = createDropDownList((Composite) getContents());
			detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
		}

		Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);

		getShell().setSize(
			new Point(windowSize.x, windowSize.y + (newSize.y - oldSize.y)));

	}
}
