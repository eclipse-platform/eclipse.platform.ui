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
/**
 * A dialog that has a title area for displaying a title and an image as well as
 * a common area for displaying a description, a message, or an error message.
 * <p>
 * This dialog class may be subclassed.
 */
public class TitleAreaDialog extends Dialog {
	/**
	 * Image registry key for error message image (value <code>"dialog_title_error_image"</code>).
	 */
	public static final String DLG_IMG_TITLE_ERROR = "dialog_title_error_image";//$NON-NLS-1$

	/**
	 * Image registry key for banner image (value <code>"dialog_title_banner_image"</code>).
	 */
	public static final String DLG_IMG_TITLE_BANNER = "dialog_title_banner_image";//$NON-NLS-1$

	// Space between the top of the title area and the title
	private static final int H_INDENT_TITLE = 7;
	// Space between the left of the title area and the title
	private static final int V_INDENT_TITLE = 8;
	// Space between an image and a label
	private static final int H_GAP_IMAGE = 5;
	// Space between the title bottom and message area top
	private static final int V_INDENT_MSG_AREA = 3;
	// Space between the left of the title area and the message
	private static final int H_INDENT_MSG = 11;
	// Space between the message area top and the top of the message label
	private static final int V_INDENT_MSG = 3;
	//Minimum height of the title image
	private static final int MIN_TITLE_IMAGE_HEIGHT = 64;
	//Minimum width of the title image
	private static final int MIN_TITLE_IMAGE_WIDTH = 64;

	static {
		ImageRegistry reg = JFaceResources.getImageRegistry();
		reg.put(DLG_IMG_TITLE_ERROR, ImageDescriptor.createFromFile(TitleAreaDialog.class, "images/title_error.gif"));//$NON-NLS-1$
		reg.put(DLG_IMG_TITLE_BANNER, ImageDescriptor.createFromFile(TitleAreaDialog.class, "images/title_banner.gif"));//$NON-NLS-1$
	}

	// Title area fields
	private static final RGB ERROR_BACKGROUND_RGB = new RGB(230, 226, 221);
	private static final RGB ERROR_BORDER_RGB = new RGB(192, 192, 192);

	private Composite titleArea;
	private Label titleLabel;
	private Label titleImage;
	private Color titleAreaColor;
	private RGB titleAreaRGB;

	private String message;
	private Composite messageArea;
	private Label messageLabel;
	private Label messageImage;
	private Color normalMsgAreaBackground;
	private Color errorMsgAreaBackground;
	private Color errorMsgAreaBorderColor;
	private Image errorMsgImage;

	/**
	 * Layout the contents of the title area.
	 */
	class TitleAreaLayout extends Layout {
		
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
			// get the title size
			Point titleSize = titleLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);

			// get the message image size
			Point messageImageSize = new Point(0,0);
			if (messageImage.getVisible()) 
				messageImageSize = messageImage.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);

			// get the message size
			String temp = messageLabel.getText();
			messageLabel.setText(" \n ");//$NON-NLS-1$
			Point messageSize = messageLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			messageLabel.setText(temp);
	
			// get the title image size
			Point titleImageSize = new Point(0,0);	
			if (titleImage.getVisible()) 
				titleImageSize = titleImage.computeSize(SWT.DEFAULT, SWT.DEFAULT, true); 

			int width = Math.max(
				H_INDENT_TITLE + titleSize.x, 
				H_INDENT_MSG + messageImageSize.x + (messageImageSize.x > 0 ? H_GAP_IMAGE : 0) + messageSize.x) 
					+ (titleImageSize.x > 0 ? H_GAP_IMAGE : 0) + titleImageSize.x;
			int height = Math.max(
				V_INDENT_TITLE + titleSize.y + V_INDENT_MSG_AREA + V_INDENT_MSG +
					Math.max(messageImageSize.y, messageSize.y) + V_INDENT_MSG, 
				titleImageSize.y);
			return new Point(width, height);
		}
		
		protected void layout(Composite composite, boolean flushCache) {
			Rectangle bounds = composite.getClientArea();
			int currentXEnd = bounds.width;

			// layout the title image
			if (titleImage.getVisible()) {
				Point imageSize = titleImage.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
				int imageWidth = Math.max(imageSize.x, MIN_TITLE_IMAGE_WIDTH);
				currentXEnd -= imageWidth;
				titleImage.setBounds(
					currentXEnd, 
					0, 
					imageWidth, 
					Math.max(imageSize.y, MIN_TITLE_IMAGE_HEIGHT));
			}

			// layout the title
			Point titleSize = titleLabel.computeSize(currentXEnd - H_INDENT_TITLE, SWT.DEFAULT, true);
			titleLabel.setBounds(H_INDENT_TITLE, V_INDENT_TITLE, titleSize.x, titleSize.y);

			// layout the message area composite
			int currentY = V_INDENT_TITLE + titleSize.y + V_INDENT_MSG_AREA;
			int messageAreaHeight = bounds.height - currentY;
			messageArea.setBounds(0, currentY, currentXEnd, messageAreaHeight);

			if (titleImage.getVisible())
				currentXEnd -= H_GAP_IMAGE;

			// layout the message image
			int currentXBegin = H_INDENT_MSG;
			if (messageImage.getVisible()) {
				Point imageSize = messageImage.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
				messageImage.setBounds(currentXBegin, V_INDENT_MSG, imageSize.x, imageSize.y);
				currentXBegin += imageSize.x + H_GAP_IMAGE;
			}

			// layout the message
			String temp = messageLabel.getText();
			messageLabel.setText(" \n ");  // 2 line limit//$NON-NLS-1$
			int labelHeight = messageLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y;
			messageLabel.setText(temp);
			messageLabel.setBounds(currentXBegin, V_INDENT_MSG, currentXEnd - currentXBegin, 
				labelHeight);
		}
	}
/**
 * Instantiate a new title area dialog.
 *
 * @param parentShell the parent SWT shell
 */
public TitleAreaDialog(Shell parentShell) {
	super(parentShell);
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

	// create the title area
	createTitleArea(composite);

	// Build the separator line
	Label titleBarSeparator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
	titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	return composite;
}
/**
 * Creates the dialog's title area.
 *
 * @param parent the SWT parent for the title area composite
 * @return the created title area composite
 */
private Composite createTitleArea(Composite parent) {
	// Create the title area which will contain
	// a title, message, and image.
	titleArea = new Composite(parent, SWT.NONE);
	titleArea.setLayout(new TitleAreaLayout());
	titleArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	// add a dispose listener
	titleArea.addDisposeListener(new DisposeListener() {
		public void widgetDisposed(DisposeEvent e) {
			if (titleAreaColor != null)
				titleAreaColor.dispose();
			if (errorMsgAreaBackground != null)
				errorMsgAreaBackground.dispose();
			if (errorMsgAreaBorderColor != null)
				errorMsgAreaBorderColor.dispose();
		}
	});
	

	// Determine the background color of the title bar
	Display display = parent.getDisplay();
	Color bg;
	if (titleAreaRGB != null) {
		titleAreaColor = new Color(display, titleAreaRGB);
		bg = titleAreaColor;
	} else {
		bg = display.getSystemColor(SWT.COLOR_WHITE);
	}	
	titleArea.setBackground(bg);

	// Title label @ top, left
	titleLabel = new Label(titleArea, SWT.LEFT);
	titleLabel.setBackground(bg);
	titleLabel.setFont(JFaceResources.getBannerFont());
	titleLabel.setText(" ");//$NON-NLS-1$

	// Composite to hold message label & icon
	// Need it to draw background color box when error msg
	messageArea = new Composite(titleArea, SWT.NONE); 
	messageArea.setBackground(bg);

	// Draw a border for the top and right side of the msg area
	messageArea.addPaintListener(new PaintListener() {
		public void paintControl(PaintEvent event) {
			if (messageImage.getVisible()) {
				Rectangle area = messageArea.getClientArea();
				GC gc = event.gc;
				if (errorMsgAreaBorderColor == null)
					errorMsgAreaBorderColor = new Color(messageArea.getDisplay(), ERROR_BORDER_RGB);
				gc.setForeground(errorMsgAreaBorderColor);
				gc.setLineWidth(1);
				gc.drawLine(area.x, area.y, area.x + area.width - 1, area.y);
				gc.drawLine(area.x + area.width  - 1, area.y, area.x + area.width - 1, area.y + area.height - 1);
			}
		}
	});
	
	// Message image @ bottom, left
	messageImage = new Label(messageArea, SWT.LEFT);
	messageImage.setBackground(bg);
	messageImage.setVisible(false);
	
	// Message label @ bottom, center
	messageLabel = new Label(messageArea, SWT.WRAP);
	messageLabel.setBackground(bg);
	messageLabel.setText(" \n "); // two lines//$NON-NLS-1$
	messageLabel.setFont(JFaceResources.getDialogFont());

	// Dialog image @ right
	titleImage = new Label(titleArea, SWT.CENTER);
	titleImage.setBackground(bg);
	titleImage.setImage(JFaceResources.getImage(DLG_IMG_TITLE_BANNER));
	GridData gd = new GridData(); 
	gd.horizontalAlignment = gd.END;
	titleImage.setLayoutData(gd);
	
	return titleArea;
}
/**
 * Returns the title area composite.
 * 
 * @return the title area composite
 */
protected Composite getTitleArea() {
	return titleArea;
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
 * Display the given error message. The currently displayed message
 * is saved and will be redisplayed when the error message is set
 * to <code>null</code>.
 *
 * @param errorMessage the errorMessage to display or <code>null</code>
 */
public void setErrorMessage(String errorMessage) {
	if (errorMessage == null) {
		if (messageImage.getVisible()) {
			// we were previously showing an error
			messageLabel.setBackground(normalMsgAreaBackground);
			messageImage.setBackground(normalMsgAreaBackground);
			messageArea.setBackground(normalMsgAreaBackground);
			messageImage.setVisible(false);
			messageImage.setImage(null);
			titleArea.layout(true);
		}

		// show the message
		setMessage(message);

	} else {
		messageLabel.setText(errorMessage);
		if (!messageImage.getVisible()) {
			// we were not previously showing an error

			// lazy initialize the error background color and image
			if (errorMsgAreaBackground == null) {
				errorMsgAreaBackground = new Color(messageLabel.getDisplay(), ERROR_BACKGROUND_RGB);
				errorMsgImage = JFaceResources.getImage(DLG_IMG_TITLE_ERROR);
			}

			// show the error	
			normalMsgAreaBackground = messageLabel.getBackground();
			messageLabel.setBackground(errorMsgAreaBackground);
			messageImage.setBackground(errorMsgAreaBackground);
			messageArea.setBackground(errorMsgAreaBackground);
			messageImage.setImage(errorMsgImage);
			messageImage.setVisible(true);
			titleArea.layout(true);
		}
	}
}
/**
 * Set the message text. If the message line currently displays an error,
 * the message is stored and will be shown after a call to clearErrorMessage
 */
public void setMessage(String newMessage) {
	message = newMessage;
	if (message == null)
		message = "";//$NON-NLS-1$
	if (!messageImage.getVisible()) 
		// we are not showing an error
		messageLabel.setText(message);
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
		title = "";//$NON-NLS-1$
	titleLabel.setText(title);
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
 * Sets the title image to be shown in the title area of this dialog.
 *
 * @param newTitle the title image show 
 */
public void setTitleImage(Image newTitleImage) {
	titleImage.setImage(newTitleImage);
	titleImage.setVisible(newTitleImage != null);
}
}
