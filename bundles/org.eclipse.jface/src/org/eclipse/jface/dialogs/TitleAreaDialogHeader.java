package org.eclipse.jface.dialogs;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import sun.security.action.GetBooleanAction;

/**
 * The WizardHeader is the class that defines the look of the area
 * at the top of a wizard.
 */
public abstract class TitleAreaDialogHeader {

	protected TitleAreaDialog dialog;
	protected FontMetrics metrics;
	protected Label messageImageLabel;
	protected Image messageImage;
	protected Label bottomFillerLabel;
	protected Label leftFillerLabel;
	protected Label messageLabel;
	protected Label titleImage;
	protected Color normalMsgAreaBackground;
	protected Color errorMsgAreaBackground;
	protected Color titleAreaColor;
	protected RGB titleAreaRGB;
	protected Image errorMsgImage;
	protected boolean showingError = false;
	protected String message = ""; //$NON-NLS-1$
	protected String errorMessage;

	private boolean titleImageLargest = true;

	/**
	 * Image registry key for error message image.
	 */
	public static final String DLG_IMG_TITLE_ERROR = Dialog.DLG_IMG_MESSAGE_ERROR; //$NON-NLS-1$

	/**
	 * Image registry key for banner image (value <code>"dialog_title_banner_image"</code>).
	 */
	public static final String DLG_IMG_TITLE_BANNER = "dialog_title_banner_image"; //$NON-NLS-1$

	/**
	 * Message type constant used to display an info icon with the message.
	 * @since 2.0
	 * @deprecated
	 */
	public final static String INFO_MESSAGE = "INFO_MESSAGE"; //$NON-NLS-1$

	/**
	 * Message type constant used to display a warning icon with the message.
	 * @since 2.0
	 * @deprecated
	 */
	public final static String WARNING_MESSAGE = "WARNING_MESSAGE"; //$NON-NLS-1$

	// Space between an image and a label
	protected static final int H_GAP_IMAGE = 5;

	/**
	* Create a new instance of the receiver.
	*/

	public TitleAreaDialogHeader(TitleAreaDialog parentDialog) {
		super();
		this.dialog = parentDialog;
	}

	/**
	 * Creates the control that displays the title information.
	 * @param Composite. The parent Composite.
	 * @param int The vertical spacing for the parent dialog.	
	 * @param int The horizontal spacing for the parent dialog.
	 */

	protected abstract void createTitleControl(
		Composite parent,
		int verticalSpacing,
		int horizontalSpacing);

	/**
	 * Get a dispose listener to attach to the parent so that resource
	 * can be freed on a close
	 * @return DisposeListener
	 */

	protected DisposeListener getControlDisposeListener() {
		return new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				freeResources();
			}

		};
	}

	/**
	 * Free any resources created by the receiver.
	 */
	protected void freeResources() {

		if (errorMsgAreaBackground != null)
			errorMsgAreaBackground.dispose();
		if (titleAreaColor != null)
			titleAreaColor.dispose();

	}

	/**
	 * Display the given error message. The currently displayed message
	 * is saved and will be redisplayed when the error message is set
	 * to <code>null</code>.
	 *
	 * @param newErrorMessage the newErrorMessage to display or <code>null</code>
	 */
	public void setErrorMessage(String newErrorMessage) {
		// Any change?
		if (errorMessage == null
			? newErrorMessage == null
			: errorMessage.equals(newErrorMessage))
			return;

		errorMessage = newErrorMessage;
		if (errorMessage == null) {
			if (showingError) {
				// we were previously showing an error
				showingError = false;
				setMessageBackgrounds(false);
			}

			// show the message
			// avoid calling setMessage in case it is overridden to call setErrorMessage, 
			// which would result in a recursive infinite loop
			if (message == null)
				//this should probably never happen since setMessage does this conversion....
				message = ""; //$NON-NLS-1$
			updateMessage(message);
			messageImageLabel.setImage(messageImage);
			setImageLabelVisible(messageImage != null);
			messageLabel.setToolTipText(message);

		} else {

			//Add in a space for layout purposes
			errorMessage = " " + errorMessage;
			updateMessage(errorMessage);
			messageLabel.setToolTipText(errorMessage);
			if (!showingError) {
				// we were not previously showing an error
				showingError = true;

				// lazy initialize the error background color and image
				if (errorMsgAreaBackground == null) {
					errorMsgAreaBackground =
						JFaceColors.getErrorBackground(
							messageLabel.getDisplay());
					errorMsgImage =
						JFaceResources.getImage(DLG_IMG_TITLE_ERROR);
				}

				// show the error	
				normalMsgAreaBackground = messageLabel.getBackground();
				setMessageBackgrounds(true);
				messageImageLabel.setImage(errorMsgImage);
				setImageLabelVisible(true);
			}
		}
		layoutForNewMessage();
	}

	/**
	 * Re-layout the labels for the new message.
	 */
	protected void layoutForNewMessage() {

		int verticalSpacing =
			this.dialog.convertVerticalDLUsToPixels(
				metrics,
				IDialogConstants.VERTICAL_SPACING);
		int horizontalSpacing =
			this.dialog.convertHorizontalDLUsToPixels(
				metrics,
				IDialogConstants.HORIZONTAL_SPACING);

		//If there are no images then layout as normal
		if (errorMessage == null && messageImage == null) {
			setImageLabelVisible(false);

			setLayoutsForNormalMessage(verticalSpacing, horizontalSpacing);

		} else {
			setLayoutsForErrorMessage(verticalSpacing, horizontalSpacing);
		}
		dialog.refreshLayout();

	}

	/**
	 * Set the message backgrounds to be the error or normal color
	 * depending on whether or not showingError is true.
	 */
	protected void setMessageBackgrounds(boolean showingError) {

		Color color;
		if (showingError)
			color = errorMsgAreaBackground;
		else
			color = normalMsgAreaBackground;

		messageLabel.setBackground(color);
		messageImageLabel.setBackground(color);
		bottomFillerLabel.setBackground(color);
		leftFillerLabel.setBackground(color);
	}

	/**
	 * Update the contents of the messageLabel.
	 * @param String the message to use
	 */
	protected void updateMessage(String newMessage) {

		//Be sure there are always 2 lines for layout purposes
		if (newMessage != null && newMessage.indexOf('\n') == -1)
			newMessage = newMessage + "\n ";

		messageLabel.setText(newMessage);
	}

	/**
	 * Make the label used for displaying error images visible
	 * depending on boolean.
	 */
	protected void setImageLabelVisible(boolean visible) {
		messageImageLabel.setVisible(visible);
		bottomFillerLabel.setVisible(visible);
		leftFillerLabel.setVisible(visible);

	}

	/**
	* Show the new message
	*/
	public void showMessage(String newMessage, Image newImage) {
		// Any change?
		if (message.equals(newMessage) && messageImage == newImage)
			return;

		message = newMessage;
		if (message == null)
			message = ""; //$NON-NLS-1$

		//If there is an image then add in a space to the message
		//for layout purposes
		if (newImage != null)
			message = " " + message; //$NON-NLS-1$

		messageImage = newImage;

		if (!showingError) {
			// we are not showing an error
			updateMessage(message);
			messageImageLabel.setImage(messageImage);
			setImageLabelVisible(messageImage != null);
			messageLabel.setToolTipText(message);
			layoutForNewMessage();
		}
	}

	/**
	* Sets the title image to be shown in the title area of this dialog.
	* By default do nothing.
	*
	* @param newTitle the title image show 
	*/
	public void setTitleImage(Image newTitleImage) {
	}

	/**
	* Sets the title to be shown in the title area of this dialog.
	* By default do nothing.
	*
	* @param newTitle the title show 
	*/
	public void setTitle(String newTitle) {
	}

	/**
	 * Sets the title bar color for this dialog.
	 *
	 * @param color the title bar color
	 */
	public void setTitleAreaColor(RGB color) {
		titleAreaRGB = color;
	}

	/**
	 * Get the title background Color.
	 * @param Display
	 * @return Color or <code>null</code> if there if no Color should
	 * be specified.
	 */
	public Color getTitleBackground(Display display) {

		if (titleAreaRGB != null) {
			if (titleAreaColor == null)
				titleAreaColor = new Color(display, titleAreaRGB);
			return titleAreaColor;
		} else {
			return JFaceColors.getBannerBackground(display);
		}
	}

	/**
	 * Get the title foreground Color.
	 * @param Display
	 * @return Color or <code>null</code> if there if no Color should
	 * be specified.
	 */
	public Color getTitleForeground(Display display) {
		if (titleAreaRGB != null) {
			return null;
		} else {
			return JFaceColors.getBannerForeground(display);
		}
	}

	/**
	 * Get the control that is displaying the title information.
	 * This is used for layout purposes.
	 * @return Control
	 */
	protected abstract Control getTitleControl();

	/**
	 * Creates the  title area.
	 *
	 * @param parent the SWT parent for the title area widgets
	 * @return Control with the highest x axis value.
	 */
	public Control createTitleArea(
		Composite parent,
		FontMetrics parentMetrics) {

		this.metrics = parentMetrics;

		// add a dispose listener
		parent.addDisposeListener(getControlDisposeListener());

		// Determine the background color of the title bar
		Display display = parent.getDisplay();
		Color background = getTitleBackground(display);
		Color foreground = getTitleForeground(display);

		int verticalSpacing =
			Dialog.convertVerticalDLUsToPixels(
				metrics,
				IDialogConstants.VERTICAL_SPACING);
		int horizontalSpacing =
			Dialog.convertHorizontalDLUsToPixels(
				metrics,
				IDialogConstants.HORIZONTAL_SPACING);
		parent.setBackground(background);

		// Dialog image @ right
		titleImage = new Label(parent, SWT.CENTER);
		titleImage.setBackground(background);
		titleImage.setImage(JFaceResources.getImage(DLG_IMG_TITLE_BANNER));

		FormData imageData = new FormData();
		imageData.top = new FormAttachment(0, verticalSpacing);
		imageData.right = new FormAttachment(100, horizontalSpacing);
		titleImage.setLayoutData(imageData);

		createTitleControl(parent, verticalSpacing, horizontalSpacing);
		
		FormData titleData = new FormData();
		titleData.top = new FormAttachment(0, verticalSpacing);
		titleData.right = new FormAttachment(titleImage);
		titleData.left = new FormAttachment(0, horizontalSpacing);
		getTitleControl().setLayoutData(titleData);


		// Message image @ bottom, left
		messageImageLabel = new Label(parent, SWT.CENTER);
		messageImageLabel.setBackground(background);

		// Message label @ bottom, center
		messageLabel = new Label(parent, SWT.WRAP);
		JFaceColors.setColors(messageLabel, foreground, background);
		messageLabel.setText(" \n "); // two lines//$NON-NLS-1$
		messageLabel.setFont(JFaceResources.getDialogFont());

		// Filler labels
		leftFillerLabel = new Label(parent, SWT.CENTER);
		leftFillerLabel.setBackground(background);

		bottomFillerLabel = new Label(parent, SWT.CENTER);
		bottomFillerLabel.setBackground(background);

		setLayoutsForNormalMessage(verticalSpacing, horizontalSpacing);

		determineTitleImageLargest();
		if (titleImageLargest)
			return titleImage;
		else
			return messageLabel;

	}

	/**
	* Determine if the title image is larger than the title message
	* and message area. This is used for layout decisions.
	*/
	private void determineTitleImageLargest() {

		int titleY = titleImage.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

		int labelY = getTitleControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		labelY += messageLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

		titleImageLargest = titleY > labelY;
	}

	/**
	 * Set the layouts for when there is no error message.
	 * @param int The vertical spacing.
	 * @param int The horizontal spacing.
	 */

	protected void setLayoutsForNormalMessage(
		int verticalSpacing,
		int horizontalSpacing) {
		FormData messageImageData = new FormData();
		messageImageData.top =
			new FormAttachment(getTitleControl(), verticalSpacing);
		messageImageData.left = new FormAttachment(0, H_GAP_IMAGE);
		messageImageLabel.setLayoutData(messageImageData);

		FormData messageLabelData = new FormData();
		messageLabelData.top =
			new FormAttachment(getTitleControl(), verticalSpacing);
		messageLabelData.right = new FormAttachment(titleImage);
		messageLabelData.left =
			new FormAttachment(messageImageLabel, horizontalSpacing);

		if (titleImageLargest)
			messageLabelData.bottom =
				new FormAttachment(titleImage, 0, SWT.BOTTOM);

		messageLabel.setLayoutData(messageLabelData);

		FormData fillerData = new FormData();
		fillerData.left = new FormAttachment(0, horizontalSpacing);
		fillerData.top = new FormAttachment(messageImageLabel, 0);
		fillerData.bottom = new FormAttachment(messageLabel, 0, SWT.BOTTOM);
		bottomFillerLabel.setLayoutData(fillerData);

		FormData data = new FormData();
		data.top = new FormAttachment(messageImageLabel, 0, SWT.TOP);
		data.left = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(messageImageLabel, 0, SWT.BOTTOM);
		data.right = new FormAttachment(messageImageLabel, 0);
		leftFillerLabel.setLayoutData(data);
	}

	/**
	 * Set the layouts for when there is an error message.
	 * @param int The vertical spacing.
	 * @param int The horizontal spacing.
	 */
	protected void setLayoutsForErrorMessage(
		int verticalSpacing,
		int horizontalSpacing) {

		messageImageLabel.setVisible(true);
		bottomFillerLabel.setVisible(true);
		leftFillerLabel.setVisible(true);

		/**
		 * Note that we do not use horizontalSpacing here 
		 * as when the background of the messages changes
		 * there will be gaps between the icon label and the
		 * message that are the background color of the shell.
		 * We add a leading space elsewhere to compendate for this.
		 */

		FormData data = new FormData();
		data.left = new FormAttachment(0, H_GAP_IMAGE);
		data.top = new FormAttachment(getTitleControl(), verticalSpacing);
		messageImageLabel.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(messageImageLabel, 0);
		data.left = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(messageLabel, 0, SWT.BOTTOM);
		data.right = new FormAttachment(messageImageLabel, 0, SWT.RIGHT);
		bottomFillerLabel.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(messageImageLabel, 0, SWT.TOP);
		data.left = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(messageImageLabel, 0, SWT.BOTTOM);
		data.right = new FormAttachment(messageImageLabel, 0);
		leftFillerLabel.setLayoutData(data);

		FormData messageLabelData = new FormData();
		messageLabelData.top =
			new FormAttachment(getTitleControl(), verticalSpacing);
		messageLabelData.right = new FormAttachment(titleImage);
		messageLabelData.left = new FormAttachment(messageImageLabel, 0);

		if (titleImageLargest)
			messageLabelData.bottom =
				new FormAttachment(titleImage, 0, SWT.BOTTOM);

		messageLabel.setLayoutData(messageLabelData);

	}

}
