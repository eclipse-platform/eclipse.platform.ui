package org.eclipse.help.ui.internal.util;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.jface.dialogs.*;
/**
 * This is the Dialog box that displays all the errors the occur during the
 * initial load of the Help System. It's data (model) is taken from the
 * RuntimeHelpStatus object.
 */
public class RuntimeErrorDialog extends MessageDialog {
	private static String errorStringToDisplay = null;
	/**
	 * RuntimeErrorDialog constructor comment.
	 * @param dialogTitle java.lang.String
	 * @param dialogTitleImage org.eclipse.swt.graphics.Image
	 * @param dialogMessage java.lang.String
	 * @param dialogImageType int
	 * @param dialogButtonLabels java.lang.String[]
	 * @param defaultIndex int
	 */
	public RuntimeErrorDialog(
		Shell parentShell,
		String dialogTitle,
		org.eclipse.swt.graphics.Image dialogTitleImage,
		String dialogMessage,
		int dialogImageType,
		java.lang.String[] dialogButtonLabels,
		int defaultIndex) {
		super(
			parentShell,
			dialogTitle,
			dialogTitleImage,
			dialogMessage,
			dialogImageType,
			dialogButtonLabels,
			defaultIndex);
	}
	protected Control createCustomArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.RESIZE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight =
			convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth =
			convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.horizontalSpacing =
			convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		// set error message
		if (errorStringToDisplay != null) {
			Text text =
				new Text(
					composite,
					SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY | SWT.MULTI);
			text.setText(errorStringToDisplay);
			GridData data =
				new GridData(
					GridData.GRAB_HORIZONTAL
						| GridData.GRAB_VERTICAL
						| GridData.HORIZONTAL_ALIGN_FILL
						| GridData.VERTICAL_ALIGN_CENTER);
			data.widthHint = getMinimumMessageWidth();
			// set the default height on linux. 
			// Note: on Windows, the default height is fine.
			if (System.getProperty("os.name").startsWith("Linux"))
				data.heightHint = convertVerticalDLUsToPixels(100);
			text.setLayoutData(data);
			text.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		}
		return composite;
	}
	public static void open(
		Shell parentShell,
		String title,
		String message,
		String errorString) {
		errorStringToDisplay = errorString;
		RuntimeErrorDialog dialog = new RuntimeErrorDialog(parentShell, title, null,			// accept the default window icon
	message, ERROR, new String[] { IDialogConstants.OK_LABEL }, 0);
		// ok is the default
		dialog.open();
		return;
	}
}