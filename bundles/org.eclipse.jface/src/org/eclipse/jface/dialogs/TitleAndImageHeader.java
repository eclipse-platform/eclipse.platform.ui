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

	/**
	 * Create a new instance of the receiver.
	 */

	public TitleAndImageHeader(TitleAreaDialog parentDialog) {
		super(parentDialog);
	}

	/*
	 * @see TitleAreaDialogHeader.createTitleControl(Composite ,int,int);
	*/

	protected void createTitleControl(
		Composite parent,
		int verticalSpacing,
		int horizontalSpacing) {

		// Title label @ top, left
		titleLabel = new Label(parent, SWT.LEFT);
		Display display = parent.getDisplay();
		JFaceColors.setColors(
			titleLabel,
			getTitleForeground(display),
			getTitleBackground(display));
		titleLabel.setFont(JFaceResources.getBannerFont());
		titleLabel.setText(" "); //$NON-NLS-1$

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

	/**
	 * @see org.eclipse.jface.dialogs.TitleAreaDialogHeader#getTitleControl()
	 */
	protected Control getTitleControl() {
		return titleLabel;
	}

}
