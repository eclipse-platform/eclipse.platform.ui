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
 * The TitleAndImageHeader is the header for wizards that shows
 * a title message, error messages and has a main title image/
 */
public class TitleAndImageHeader extends TitleAreaDialogHeader {

	static {
		ImageRegistry reg = JFaceResources.getImageRegistry();
		reg.put(DLG_IMG_TITLE_BANNER, ImageDescriptor.createFromFile(TitleAreaDialog.class, "images/title_banner.gif")); //$NON-NLS-1$
	}

	private Label titleLabel;
	private Label titleImage;

	private boolean titleImageLargest = true;

	/**
	 * Create a new instance of the receiver.	 * 
	 */

	public TitleAndImageHeader(TitleAreaDialog parentDialog) {
		super(parentDialog);
	}

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

		// Title label @ top, left
		titleLabel = new Label(parent, SWT.LEFT);
		JFaceColors.setColors(titleLabel, foreground, background);
		titleLabel.setFont(JFaceResources.getBannerFont());
		titleLabel.setText(" "); //$NON-NLS-1$

		FormData titleData = new FormData();
		titleData.top = new FormAttachment(0, verticalSpacing);
		titleData.right = new FormAttachment(titleImage);
		titleData.left = new FormAttachment(0, horizontalSpacing);
		titleLabel.setLayoutData(titleData);

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
	 * 
	 * @see org.eclipse.jface.dialogs.TitleAreaDialogHeader#getControlDisposeListener()
	 */

	protected DisposeListener getControlDisposeListener() {
		return new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				freeResources();
			}

		};
	}

	/**
	 * Determine if the title image is larger than the title message
	 * and message area. This is used for layout decisions.
	 */
	private void determineTitleImageLargest() {

		int titleY = titleImage.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

		int labelY = titleLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		labelY += messageLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

		titleImageLargest = titleY > labelY;
	}

	/*
	 * @see org.eclipse.jface.dialogs.TitleAreaDialogHeader#setLayoutsForNormalMessage(int, int)	 */

	protected void setLayoutsForNormalMessage(
		int verticalSpacing,
		int horizontalSpacing) {
		FormData messageImageData = new FormData();
		messageImageData.top = new FormAttachment(titleLabel, verticalSpacing);
		messageImageData.left = new FormAttachment(0, H_GAP_IMAGE);
		messageImageLabel.setLayoutData(messageImageData);

		FormData messageLabelData = new FormData();
		messageLabelData.top = new FormAttachment(titleLabel, verticalSpacing);
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

	/*
	 * @see org.eclipse.jface.dialogs.TitleAreaDialogHeader#setLayoutsForErrorMessage(int, int)	 */
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
		data.top = new FormAttachment(titleLabel, verticalSpacing);
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
		messageLabelData.top = new FormAttachment(titleLabel, verticalSpacing);
		messageLabelData.right = new FormAttachment(titleImage);
		messageLabelData.left = new FormAttachment(messageImageLabel, 0);

		if (titleImageLargest)
			messageLabelData.bottom =
				new FormAttachment(titleImage, 0, SWT.BOTTOM);

		messageLabel.setLayoutData(messageLabelData);

	}

	/**
	* Sets the title image to be shown in the title area of this dialog.
	*
	* @param newTitle the title image show 
	*/
	public void setTitleImage(Image newTitleImage) {
		titleImage.setImage(newTitleImage);
		titleImage.setVisible(newTitleImage != null);
	}

	/**
	* Sets the title to be shown in the title area of this dialog.
	*
	* @param newTitle the title show 
	*/
	public void setTitle(String newTitle) {
		if (titleLabel == null)
			return;
		String title = newTitle;
		if (title == null)
			title = ""; //$NON-NLS-1$
		titleLabel.setText(title);
	}

	/**
	 * Returns the title image label.
	 * 
	 * @return the title image label
	 */
	protected Label getTitleImageLabel() {
		return titleImage;
	}

}
