package org.eclipse.jface.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.graphics.Point;
/**
 * A dialog that has a title area for displaying a title and an image as well as
 * a common area for displaying a description, a message, or an error message.
 * <p>
 * This dialog class may be subclassed.
 */
public class TitleAreaDialog extends Dialog {

	//Minimum dialog width (in dialog units)
	private static final int MIN_DIALOG_WIDTH = 350;
	//Minimum dialog height (in dialog units)
	private static final int MIN_DIALOG_HEIGHT = 150;

	protected TitleAreaDialogHeader header;

	/**
	 * Instantiate a new title area dialog.
	 *
	 * @param parentShell the parent SWT shell
	 */
	public TitleAreaDialog(Shell parentShell) {
		super(parentShell);
	}

	/*
	 * @see Dialog.createContents(Composite)
	 */
	protected Control createContents(Composite parent) {

		// initialize the dialog units
		initializeDialogUnits(parent);
		initializeHeader();

		FormLayout layout = new FormLayout();
		parent.setLayout(layout);
		FormData data = new FormData();
		data.top = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(100, 100);
		parent.setLayoutData(data);

		Control top = header.createTitleArea(parent,fontMetrics);

		//Now create a work area for the rest of the dialog
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout childLayout = new GridLayout();
		childLayout.marginHeight = 0;
		childLayout.marginWidth = 0;
		childLayout.verticalSpacing = 0;
		composite.setLayout(childLayout);

		FormData childData = new FormData();
		childData.top = new FormAttachment(top);
		childData.right = new FormAttachment(100, 0);
		childData.left = new FormAttachment(0, 0);
		childData.bottom = new FormAttachment(100, 0);
		composite.setLayoutData(childData);

		composite.setFont(JFaceResources.getDialogFont());

		// initialize the dialog units
		initializeDialogUnits(composite);

		// create the dialog area and button bar
		dialogArea = createDialogArea(composite);
		buttonBar = createButtonBar(composite);

		return parent;
	}
	protected void initializeHeader() {
		this.header = new TitleAndImageHeader(this);
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
		// create the top level composite for the dialog area
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(parent.getFont());

		// Build the separator line
		Label titleBarSeparator =
			new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return composite;
	}
	/**
	 * The <code>TitleAreaDialog</code> implementation of this 
	 * <code>Window</code> methods returns an initial size which
	 * is at least some reasonable minimum.
	 *
	 * @return the initial size of the dialog
	 */
	protected Point getInitialSize() {
		Point shellSize = super.getInitialSize();
		return new Point(
			Math.max(
				convertHorizontalDLUsToPixels(MIN_DIALOG_WIDTH),
				shellSize.x),
			Math.max(
				convertVerticalDLUsToPixels(MIN_DIALOG_HEIGHT),
				shellSize.y));
	}

	/**
	 * Retained for backward compatibility.
	 * 
	 * Returns the title area composite. There is no composite in this
	 * implementation so the shell is returned.
	 * @deprecated
	 */
	protected Composite getTitleArea() {
		return getShell();
	} 	/**
	 * Display the given error message. The currently displayed message
	 * is saved and will be redisplayed when the error message is set
	 * to <code>null</code>.
	 *
	 * @param newErrorMessage the newErrorMessage to display or <code>null</code>
	 */
	public void setErrorMessage(String newErrorMessage) {
		this.header.setErrorMessage(newErrorMessage);
	}

	/**
	 * Set the message text. If the message line currently displays an error,
	 * the message is saved and will be redisplayed when the error message is set
	 * to <code>null</code>.
	 * <p>
	 * Shortcut for <code>setMessage(newMessage, IMessageProvider.NONE)</code>
	 * </p> 
	 * 
	 * @param newMessage the message, or <code>null</code> to clear
	 *   the message
	 */
	public void setMessage(String newMessage) {
		setMessage(newMessage, IMessageProvider.NONE);
	}
	/**
	 * Sets the message for this dialog with an indication of what type
	 * of message it is.
	 * <p>
	 * The valid message types are one of <code>NONE</code>, 
	 * <code>INFORMATION</code>, <code>WARNING</code>, or <code>ERROR</code>.
	 * </p>
	 * <p>
	 * Note that for backward compatibility, a message of type <code>ERROR</code> 
	 * is different than an error message (set using <code>setErrorMessage</code>). 
	 * An error message overrides the current message until the error message is 
	 * cleared. This method replaces the current message and does not affect the 
	 * error message.
	 * </p>
	 *
	 * @param newMessage the message, or <code>null</code> to clear
	 *   the message
	 * @param newType the message type
	 * @since 2.0
	 */
	public void setMessage(String newMessage, int newType) {
		Image newImage = null;

		if (newMessage != null) {
			switch (newType) {
				case IMessageProvider.NONE :
					break;
				case IMessageProvider.INFORMATION :
					newImage = JFaceResources.getImage(DLG_IMG_MESSAGE_INFO);
					break;
				case IMessageProvider.WARNING :
					newImage = JFaceResources.getImage(DLG_IMG_MESSAGE_WARNING);
					break;
				case IMessageProvider.ERROR :
					newImage = JFaceResources.getImage(DLG_IMG_MESSAGE_ERROR);
					break;
			}
		}

		showMessage(newMessage, newImage);
	}
	/**
	 * Show the new message
	 */
	private void showMessage(String newMessage, Image newImage) {
		this.header.showMessage(newMessage,newImage);
	}

	/**
	 * Sets the title to be shown in the title area of this dialog.
	 *
	 * @param newTitle the title show 
	 */
	public void setTitle(String newTitle) {
		this.header.setTitle(newTitle);
	}
	
	/**
	 * Sets the title bar color for this dialog.
	 *
	 * @param color the title bar color
	 */
	public void setTitleAreaColor(RGB color) {
		this.header.setTitleAreaColor(color);
	}
	

	/**
		* Sets the title image to be shown in the title area of this dialog.
		*
		* @param newTitle the title image show 
		*/
	public void setTitleImage(Image newTitleImage) {
		header.setTitleImage(newTitleImage);
	}

	/**
	 * Refresh the layout of the TitleAreaDialog
	 */
	public void refreshLayout() {
		//Do not layout before the dialog area has been created
		//to avoid incomplete calculations.
		if (dialogArea != null)
			getShell().layout(true);
	}
}
