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
	protected Color normalMsgAreaBackground;
	protected Color errorMsgAreaBackground;
	protected Color titleAreaColor;
	protected RGB titleAreaRGB;	
	protected Image errorMsgImage;
	protected boolean showingError = false;
	protected String message = ""; //$NON-NLS-1$
	protected String errorMessage;

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
	 * Creates the  title area.
	 *
	 * @param parent the SWT parent for the title area widgets
	 * @return Control with the highest x axis value.
	 */
	public abstract Control createTitleArea(
		Composite parent,
		FontMetrics parentMetrics);

	/**
	 * Set the layout values for the messageLabel, messageImageLabel and 
	 * fillerLabel for the case where there is a normal message.
	 * @param verticalSpacing int The spacing between widgets on the vertical axis.
	 * @param horizontalSpacing int The spacing between widgets on the horizontal axis.
	 */

	protected abstract void setLayoutsForNormalMessage(
		int verticalSpacing,
		int horizontalSpacing);

	/**
	 * Set the layout values for the messageLabel, messageImageLabel and 
	 * fillerLabel for the case where there is a error message.
	 * @param verticalSpacing int The spacing between widgets on the vertical axis.
	 * @param horizontalSpacing int The spacing between widgets on the horizontal axis.
	 */

	protected abstract void setLayoutsForErrorMessage(
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
	public Color getTitleBackground(Display display){
		
		if (titleAreaRGB != null) {
			if(titleAreaColor == null)
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
	public Color getTitleForeground(Display display){
		if (titleAreaRGB != null) {
			return null;
		} else {
			return JFaceColors.getBannerForeground(display);
		}
	}

}
