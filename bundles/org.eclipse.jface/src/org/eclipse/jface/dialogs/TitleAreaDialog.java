/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Konstantin Scheglov <scheglov_ke@nlmk.ru > - Fix for bug 41172
 *     [Dialogs] Bug with Image in TitleAreaDialog
 *******************************************************************************/
package org.eclipse.jface.dialogs;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A dialog that has a title area for displaying a title and an image as well as
 * a common area for displaying a description, a message, or an error message.
 * <p>
 * This dialog class may be subclassed.
 */
public class TitleAreaDialog extends TrayDialog {
    /**
     * Image registry key for error message image.
     */
    public static final String DLG_IMG_TITLE_ERROR = DLG_IMG_MESSAGE_ERROR;

    /**
     * Image registry key for banner image (value
     * <code>"dialog_title_banner_image"</code>).
     */
    public static final String DLG_IMG_TITLE_BANNER = "dialog_title_banner_image";//$NON-NLS-1$

    /**
     * Message type constant used to display an info icon with the message.
     * 
     * @since 2.0
     * @deprecated
     */
    public final static String INFO_MESSAGE = "INFO_MESSAGE"; //$NON-NLS-1$

    /**
     * Message type constant used to display a warning icon with the message.
     * 
     * @since 2.0
     * @deprecated
     */
    public final static String WARNING_MESSAGE = "WARNING_MESSAGE"; //$NON-NLS-1$

    // Space between an image and a label
    private static final int H_GAP_IMAGE = 5;

    //Minimum dialog width (in dialog units)
    private static final int MIN_DIALOG_WIDTH = 350;

    //Minimum dialog height (in dialog units)
    private static final int MIN_DIALOG_HEIGHT = 150;
    static {
        ImageRegistry reg = JFaceResources.getImageRegistry();
        reg.put(DLG_IMG_TITLE_BANNER, ImageDescriptor.createFromFile(
                TitleAreaDialog.class, "images/title_banner.gif"));//$NON-NLS-1$
    }

    private Label titleLabel;

    private Label titleImage;

    private Label leftFillerLabel;

    private RGB titleAreaRGB;

    Color titleAreaColor;

    private String message = ""; //$NON-NLS-1$

    private String errorMessage;

    private Text messageLabel;

    private Composite workArea;
    
	private Composite titleArea;

    private Label messageImageLabel;

    private Image messageImage;

    private boolean showingError = false;

    private boolean titleImageLargest = true;
    
    private int messageLabelHeight;
    
    private MessageArea messageArea;

    /**
     * Instantiate a new title area dialog.
     * 
     * @param parentShell
     *            the parent SWT shell
     */
    public TitleAreaDialog(Shell parentShell) {
        super(parentShell);
    }

    /*
     * @see Dialog.createContents(Composite)
     */
    protected Control createContents(Composite parent) {
    	// create the overall composite
    	Composite contents = new Composite(parent, SWT.NONE);
        contents.setLayoutData(new GridData(GridData.FILL_BOTH));
        // initialize the dialog units
        initializeDialogUnits(contents);
        FormLayout layout = new FormLayout();
        contents.setLayout(layout);
        //Now create a work area for the rest of the dialog
        workArea = new Composite(contents, SWT.NONE);
        GridLayout childLayout = new GridLayout();
        childLayout.marginHeight = 0;
        childLayout.marginWidth = 0;
        childLayout.verticalSpacing = 0;
        workArea.setLayout(childLayout);
        Control top = createTitleArea(contents);
        resetWorkAreaAttachments(top);
        workArea.setFont(JFaceResources.getDialogFont());
        // initialize the dialog units
        initializeDialogUnits(workArea);
        // create the dialog area and button bar
        dialogArea = createDialogArea(workArea);
        buttonBar = createButtonBar(workArea);
        return contents;
    }

    /**
     * Creates and returns the contents of the upper part of this dialog (above
     * the button bar).
     * <p>
     * The <code>Dialog</code> implementation of this framework method creates
     * and returns a new <code>Composite</code> with no margins and spacing.
     * Subclasses should override.
     * </p>
     * 
     * @param parent
     *            The parent composite to contain the dialog area
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
        Label titleBarSeparator = new Label(composite, SWT.HORIZONTAL
                | SWT.SEPARATOR);
        titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return composite;
    }

    /**
     * Creates the dialog's title area.
     * 
     * @param parent
     *            the SWT parent for the title area widgets
     * @return Control with the highest x axis value.
     */
    private Control createTitleArea(Composite parent) {
    	titleArea = new Composite(parent, SWT.NONE);
        initializeDialogUnits(titleArea);
    	
    	FormData titleAreaData = new FormData();
    	titleAreaData.top = new FormAttachment(0,0);
    	titleAreaData.left = new FormAttachment(0,0);
    	titleAreaData.right = new FormAttachment(100,0);
		titleArea.setLayoutData(titleAreaData);
		
        FormLayout layout = new FormLayout();
        titleArea.setLayout(layout);
    	
        // add a dispose listener
        titleArea.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (titleAreaColor != null) {
					titleAreaColor.dispose();
				}
            }
        });
        // Determine the background color of the title bar
        Display display = titleArea.getDisplay();
        Color background;
        Color foreground;
        if (titleAreaRGB != null) {
            titleAreaColor = new Color(display, titleAreaRGB);
            background = titleAreaColor;
            foreground = null;
        } else {
            background = JFaceColors.getBannerBackground(display);
            foreground = JFaceColors.getBannerForeground(display);
        }
        int verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        int horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        titleArea.setBackground(background);
        // Dialog image @ right
        titleImage = new Label(titleArea, SWT.CENTER);
        titleImage.setBackground(background);
        titleImage.setImage(JFaceResources.getImage(DLG_IMG_TITLE_BANNER));
        FormData imageData = new FormData();
        imageData.top = new FormAttachment(0, 0);
        // Note: do not use horizontalSpacing on the right as that would be a
        // regression from
        // the R2.x style where there was no margin on the right and images are
        // flush to the right
        // hand side. see reopened comments in 41172
        imageData.right = new FormAttachment(100, 0); // horizontalSpacing
        titleImage.setLayoutData(imageData);
        // Title label @ top, left
        titleLabel = new Label(titleArea, SWT.LEFT);
        JFaceColors.setColors(titleLabel, foreground, background);
        titleLabel.setFont(JFaceResources.getBannerFont());
        titleLabel.setText(" ");//$NON-NLS-1$
        FormData titleData = new FormData();
        titleData.top = new FormAttachment(0, verticalSpacing);
        titleData.right = new FormAttachment(titleImage);
        titleData.left = new FormAttachment(0, horizontalSpacing);
        titleLabel.setLayoutData(titleData);
        // Message image @ bottom, left
        messageImageLabel = new Label(titleArea, SWT.CENTER);
        messageImageLabel.setBackground(background);
        // Message label @ bottom, center
        messageLabel = new Text(titleArea, SWT.WRAP | SWT.READ_ONLY);
        JFaceColors.setColors(messageLabel, foreground, background);
        messageLabel.setText(" \n "); // two lines//$NON-NLS-1$
        messageLabel.setFont(JFaceResources.getDialogFont());
        messageLabelHeight = messageLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        // Filler label
        leftFillerLabel = new Label(titleArea, SWT.CENTER);
        leftFillerLabel.setBackground(background);
        setLayoutsForNormalMessage(verticalSpacing, horizontalSpacing);
        determineTitleImageLargest();
        
        return titleArea;
    }

    /**
     * Determine if the title image is larger than the title message and message
     * area. This is used for layout decisions.
     */
    private void determineTitleImageLargest() {
        int titleY = titleImage.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        int verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        int labelY = titleLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        labelY += verticalSpacing;
        labelY += messageLabelHeight;
        labelY += verticalSpacing;
        titleImageLargest = titleY > labelY;
    }

    /**
     * Set the layout values for the messageLabel, messageImageLabel and
     * fillerLabel for the case where there is a normal message.
     * 
     * @param verticalSpacing
     *            int The spacing between widgets on the vertical axis.
     * @param horizontalSpacing
     *            int The spacing between widgets on the horizontal axis.
     */
    private void setLayoutsForNormalMessage(int verticalSpacing,
            int horizontalSpacing) {
        FormData messageLabelData = new FormData();
        messageLabelData.top = new FormAttachment(titleLabel, verticalSpacing);
        messageLabelData.right = new FormAttachment(titleImage);
        messageLabelData.left = new FormAttachment(messageImageLabel,horizontalSpacing);
        messageLabelData.height = messageLabelHeight;
        if (titleImageLargest) {
			messageLabelData.bottom = new FormAttachment(titleImage, 0,
                    SWT.BOTTOM);
		}
        messageLabel.setLayoutData(messageLabelData);
        FormData imageLabelData = new FormData();
        imageLabelData.top = new FormAttachment(titleLabel, verticalSpacing);
        imageLabelData.left = new FormAttachment(leftFillerLabel);
        imageLabelData.right = new FormAttachment(messageLabel);
        messageImageLabel.setLayoutData(imageLabelData);
        
        FormData data = new FormData();
        data.top = new FormAttachment(titleLabel, 0, SWT.TOP);
        data.left = new FormAttachment(0,H_GAP_IMAGE);
        data.bottom = new FormAttachment(messageLabel, 0, SWT.BOTTOM);
        leftFillerLabel.setLayoutData(data);       
               	
     }

    /**
     * The <code>TitleAreaDialog</code> implementation of this
     * <code>Window</code> methods returns an initial size which is at least
     * some reasonable minimum.
     * 
     * @return the initial size of the dialog
     */
    protected Point getInitialSize() {
        Point shellSize = super.getInitialSize();
        return new Point(Math.max(
                convertHorizontalDLUsToPixels(MIN_DIALOG_WIDTH), shellSize.x),
                Math.max(convertVerticalDLUsToPixels(MIN_DIALOG_HEIGHT),
                        shellSize.y));
    }

    /**
     * Retained for backward compatibility.
     * 
     * Returns the title area composite. There is no composite in this
     * implementation so the shell is returned.
     * 
     * @return Composite
     * @deprecated
     */
    protected Composite getTitleArea() {
        return getShell();
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
     * Display the given error message. The currently displayed message is saved
     * and will be redisplayed when the error message is set to
     * <code>null</code>.
     * 
     * @param newErrorMessage
     *            the newErrorMessage to display or <code>null</code>
     */
    public void setErrorMessage(String newErrorMessage) {
        // Any change?
        if (errorMessage == null ? newErrorMessage == null : errorMessage
                .equals(newErrorMessage)) {
			return;
		}
        errorMessage = newErrorMessage;
         
        //Clear or set error message.
        if (errorMessage == null) {
        	if(messageArea != null){
           		setMessageAreaVisible(false);
         	}
            if (showingError) {
                // we were previously showing an error
                showingError = false;
            }
            // show the message
            // avoid calling setMessage in case it is overridden to call
            // setErrorMessage,
            // which would result in a recursive infinite loop
            if (message == null) {
                // setMessage does this conversion....
                message = ""; //$NON-NLS-1$
			}
            updateMessage(message);
            messageImageLabel.setImage(messageImage);
            setImageLabelVisible(messageImage != null);
            
            if(messageImage != null)
            {
                // set the bounds of the messageLabel to account for a  message 
                // image and avoid resetting the layout
                Rectangle messageBounds = messageLabel.getBounds();
                Rectangle imageBounds = messageImageLabel.getBounds();

                messageImageLabel.setBounds(imageBounds.x,
                							imageBounds.y,
                							messageImage.getBounds().width,
                							messageImage.getBounds().height);
                messageLabel.setBounds(imageBounds.x + messageImage.getBounds().width,
                					   messageBounds.y, 
                					   messageBounds.width, 
                					   messageBounds.height);
            } 
         } else {
            if (!showingError) {
                // we were not previously showing an error
                showingError = true;
            }            
            if(messageArea == null){
            	// create a message area to display the error
                messageArea = new MessageArea(titleArea, SWT.NULL);
        		messageArea.setBackground(messageLabel.getBackground());
       		
        		Policy.getAnimator().setAnimationState(ControlAnimator.CLOSED);
              }
            // show the error
            messageArea.setText(errorMessage);
        	messageArea.setImage(JFaceResources.getImage(DLG_IMG_TITLE_ERROR));
            setMessageAreaVisible(true);
         }
        int verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        int horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        setLayoutsForNormalMessage(verticalSpacing, horizontalSpacing);
      }
    
	/**
	 * Sets whether the message area should appear or dissapear
	 * 
	 * @param visible
	 * 			<code>true</code> if the message area should be 
	 * 			displayed, and <code>false</code> otherwise.
	 */
	private void setMessageAreaVisible(boolean visible) {
		// return immediately if already OPENING/OPEN and
		// visible is true or if CLOSING/CLOSED and visible
		// is false. 
		switch (Policy.getAnimator().getAnimationState()) {
		case ControlAnimator.OPENING:
		case ControlAnimator.OPEN:
			if (visible)
				return;
			break;
		case ControlAnimator.CLOSING:
		case ControlAnimator.CLOSED:
			if (!visible){
				return;
			}
			break;
		}
		
        FormData messageAreaData = new FormData();
        messageAreaData.right = new FormAttachment(titleImage);
        messageAreaData.left = new FormAttachment(leftFillerLabel);
        messageAreaData.bottom = new FormAttachment(100,0);
        messageArea.setLayoutData(messageAreaData);
		messageArea.moveAbove(null);
		
		// assumes that bottom of the message area should match
		// the bottom of te parent composite.
		int bottom = titleArea.getBounds().y + titleArea.getBounds().height;
		
		// Only set bounds if the message area is CLOSED. The bounds 
		// are dependent on whether a message image is being shown.
		Rectangle msgLabelBounds = messageLabel.getBounds();
		if(Policy.getAnimator().getAnimationState() == ControlAnimator.CLOSED) {
			messageArea.setBounds(
					(messageImageLabel == null) ? msgLabelBounds.x: 
						messageImageLabel.getBounds().x,
					bottom,
					(messageImageLabel == null) ? msgLabelBounds.width: 
						msgLabelBounds.width + messageImageLabel.getBounds().width,
					messageArea.computeSize(SWT.DEFAULT,SWT.DEFAULT).y);
		}		
		Policy.getAnimator().setAnimationState(visible ? 
				ControlAnimator.OPENING: ControlAnimator.CLOSING);
		Policy.getAnimator().setVisible(visible, messageArea);
	}

    /**
     * Re-layout the labels for the new message.
     */
    private void layoutForNewMessage() {
        int verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        int horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        setLayoutsForNormalMessage(verticalSpacing, horizontalSpacing);
        //Do not layout before the dialog area has been created
        //to avoid incomplete calculations.
        if (dialogArea != null) {
			workArea.getParent().layout(true);
        	
			// force re-layout of controls on titleArea since the 
			// above call will not perform this operation if the 
			// titleArea has resized.
        	titleArea.layout(true);
		}
    }

    /**
     * Set the message text. If the message line currently displays an error,
     * the message is saved and will be redisplayed when the error message is
     * set to <code>null</code>.
     * <p>
     * Shortcut for <code>setMessage(newMessage, IMessageProvider.NONE)</code>
     * </p>
     * This method should be called after the dialog has been opened as it
     * updates the message label immediately.
     * 
     * @param newMessage
     *            the message, or <code>null</code> to clear the message
     */
    public void setMessage(String newMessage) {
        setMessage(newMessage, IMessageProvider.NONE);
    }

    /**
     * Sets the message for this dialog with an indication of what type of
     * message it is.
     * <p>
     * The valid message types are one of <code>NONE</code>,
     * <code>INFORMATION</code>,<code>WARNING</code>, or
     * <code>ERROR</code>.
     * </p>
     * <p>
     * Note that for backward compatibility, a message of type
     * <code>ERROR</code> is different than an error message (set using
     * <code>setErrorMessage</code>). An error message overrides the current
     * message until the error message is cleared. This method replaces the
     * current message and does not affect the error message.
     * </p>
     * 
     * @param newMessage
     *            the message, or <code>null</code> to clear the message
     * @param newType
     *            the message type
     * @since 2.0
     */
    public void setMessage(String newMessage, int newType) {
        Image newImage = null;
        if (newMessage != null) {
            switch (newType) {
            case IMessageProvider.NONE:
                break;
            case IMessageProvider.INFORMATION:
                newImage = JFaceResources.getImage(DLG_IMG_MESSAGE_INFO);
                break;
            case IMessageProvider.WARNING:
                newImage = JFaceResources.getImage(DLG_IMG_MESSAGE_WARNING);
                break;
            case IMessageProvider.ERROR:
                newImage = JFaceResources.getImage(DLG_IMG_MESSAGE_ERROR);
                break;
            }
        }
        showMessage(newMessage, newImage);
    }

    /**
     * Show the new message and image.
     * @param newMessage 
     * @param newImage
     */
    private void showMessage(String newMessage, Image newImage) {
        // Any change?
        if (message.equals(newMessage) && messageImage == newImage) {
			return;
		}
        message = newMessage;
        if (message == null) {
			message = "";//$NON-NLS-1$
		}
        // Message string to be shown - if there is an image then add in
        // a space to the message for layout purposes
        String shownMessage = (newImage == null) ? message : " " + message; //$NON-NLS-1$  
        messageImage = newImage;
        if (!showingError) {
            // we are not showing an error
            updateMessage(shownMessage);
            messageImageLabel.setImage(messageImage);
            setImageLabelVisible(messageImage != null);
            layoutForNewMessage();
        }
    }

    /**
     * Update the contents of the messageLabel.
     * 
     * @param newMessage
     *            the message to use
     */
    private void updateMessage(String newMessage) {
        messageLabel.setText(newMessage);
    }

    /**
     * Sets the title to be shown in the title area of this dialog.
     * 
     * @param newTitle
     *            the title show
     */
    public void setTitle(String newTitle) {
        if (titleLabel == null) {
			return;
		}
        String title = newTitle;
        if (title == null) {
			title = "";//$NON-NLS-1$
		}
        titleLabel.setText(title);
    }

    /**
     * Sets the title bar color for this dialog.
     * 
     * @param color
     *            the title bar color
     */
    public void setTitleAreaColor(RGB color) {
        titleAreaRGB = color;
    }

    /**
     * Sets the title image to be shown in the title area of this dialog.
     * 
     * @param newTitleImage
     *            the title image show
     */
    public void setTitleImage(Image newTitleImage) {
        titleImage.setImage(newTitleImage);
        titleImage.setVisible(newTitleImage != null);
        if (newTitleImage != null) {
            resetWorkAreaAttachments(titleArea);
        }
    }

    /**
     * Make the label used for displaying error images visible depending on
     * boolean.
     * @param visible If <code>true</code> make the image visible, if
     * not then make it not visible.
     */
    private void setImageLabelVisible(boolean visible) {
        messageImageLabel.setVisible(visible);
        leftFillerLabel.setVisible(visible);
    }


    /**
     * Reset the attachment of the workArea to now attach to top as the top
     * control.
     * 
     * @param top
     */
    private void resetWorkAreaAttachments(Control top) {
        FormData childData = new FormData();
        childData.top = new FormAttachment(top);
        childData.right = new FormAttachment(100, 0);
        childData.left = new FormAttachment(0, 0);
        childData.bottom = new FormAttachment(100, 0);
        workArea.setLayoutData(childData);
    }
}
