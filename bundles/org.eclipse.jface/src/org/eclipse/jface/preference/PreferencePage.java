/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Sebastian Davids <sdavids@gmx.de> - Fix for bug 38729 - [Preferences]
 * 			 NPE PreferencePage isValid.
 *******************************************************************************/
package org.eclipse.jface.preference;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Abstract base implementation for all preference page implementations.
 * <p>
 * Subclasses must implement the <code>createControl</code> framework
 * method to supply the page's main control.
 * </p>
 * <p>
 * Subclasses should extend the <code>doComputeSize</code> framework
 * method to compute the size of the page's control.
 * </p>
 * <p>
 * Subclasses may override the <code>performOk</code>, <code>performApply</code>, 
 * <code>performDefaults</code>, <code>performCancel</code>, and <code>performHelp</code>
 * framework methods to react to the standard button events.
 * </p>
 * <p>
 * Subclasses may call the <code>noDefaultAndApplyButton</code> framework
 * method before the page's control has been created to suppress
 * the standard Apply and Defaults buttons.
 * </p>
 */
public abstract class PreferencePage extends DialogPage implements
        IPreferencePage {
	
	/**
	 * The MessageRegion is the optional area to
	 * show messages in the page.
	 */
	private class MessageRegion{

		private Text messageText;

		private Label messageImageLabel;

		private Composite messageComposite;

		private String lastMessageText = "";//$NON-NLS-1$

		private int lastMessageType;

		/**
		 * Create a new instance of the receiver.
		 */
		public MessageRegion() {
			//No initial behaviour
		}

		/**
		 * Create the contents for the receiver.
		 * 
		 * @param parent
		 *            the Composite that the children will be created in
		 */
		public void createContents(Composite parent) {
			messageComposite = new Composite(parent, SWT.NONE);
			GridLayout messageLayout = new GridLayout();
			messageLayout.numColumns = 2;
			messageLayout.marginWidth = 0;
			messageLayout.marginHeight = 0;
			messageLayout.makeColumnsEqualWidth = false;
			messageComposite.setLayout(messageLayout);
			messageImageLabel = new Label(messageComposite, SWT.NONE);

			GridData imageData = new GridData(GridData.VERTICAL_ALIGN_CENTER);
			Rectangle imageBounds = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR)
					.getBounds();
			imageData.heightHint = imageBounds.height + IDialogConstants.VERTICAL_SPACING;
			imageData.widthHint = imageBounds.width + IDialogConstants.HORIZONTAL_SPACING;
			messageImageLabel.setLayoutData(imageData);

			messageText = new Text(messageComposite, SWT.NONE);
			messageText.setEditable(false);
			messageText.setBackground(parent.getDisplay().getSystemColor(
					SWT.COLOR_WIDGET_BACKGROUND));

			GridData textData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL
					| GridData.VERTICAL_ALIGN_CENTER);
			messageText.setLayoutData(textData);

		}

		/**
		 * Set the layoutData for the messageArea. In most cases this will be a copy
		 * of the layoutData used in setTitleLayoutData.
		 * 
		 * @param layoutData
		 *            the layoutData for the message area composite.
		 */
		public void setMessageLayoutData(Object layoutData) {
			messageComposite.setLayoutData(layoutData);
		}

		/**
		 * Show the new message in the message text and update the image. Base the
		 * background color on whether or not there are errors.
		 * 
		 * @param newMessage
		 *            The new value for the message
		 * @param newType
		 *            One of the IMessageProvider constants. If newType is
		 *            IMessageProvider.NONE show the title.
		 * @see IMessageProvider
		 */
		public void updateText(String newMessage, int newType) {
			Image newImage = null;
			boolean showingError = false;
			switch (newType) {
			case IMessageProvider.NONE:
				hideRegion();
				return;
			case IMessageProvider.INFORMATION:
				newImage = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO);
				break;
			case IMessageProvider.WARNING:
				newImage = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
				break;
			case IMessageProvider.ERROR:
				newImage = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
				showingError = true;
				break;
			}
			
			if(newMessage == null){//No message so clear the area
				hideRegion();
				return;
			}
			messageComposite.setVisible(true);
			// Any more updates required
			if (newMessage.equals(messageText.getText())
					&& newImage == messageImageLabel.getImage())
				return;
			messageImageLabel.setImage(newImage);
			if (newMessage == null)
				messageText.setText("");//$NON-NLS-1$
			else
				messageText.setText(newMessage);
			if (showingError)
				setMessageColors(JFaceColors.getErrorBackground(messageComposite.getDisplay()));
			else {
				lastMessageText = newMessage;
				setMessageColors(JFaceColors.getBannerBackground(messageComposite.getDisplay()));
			}

		}

		/**
		 * Hide the message region and clear out the caches.
		 */
		private void hideRegion() {
			messageComposite.setVisible(false);
			lastMessageText = null;
			lastMessageType = IMessageProvider.NONE;
		}

		/**
		 * Set the colors of the message area.
		 * 
		 * @param color
		 *            The color to be use in the message area.
		 */
		private void setMessageColors(Color color) {
			messageText.setBackground(color);
			messageComposite.setBackground(color);
			messageImageLabel.setBackground(color);
		}

		/**
		 * Clear the error message. Restore the previously displayed message if
		 * there is one, if not restore the title label.
		 *  
		 */
		public void clearErrorMessage() {
			updateText(lastMessageText, lastMessageType);
		}

	}

    /**
     * Preference store, or <code>null</code>.
     */
    private IPreferenceStore preferenceStore;

    /**
     * Valid state for this page; <code>true</code> by default.
     *
     * @see #isValid
     */
    private boolean isValid = true;

    /**
     * Body of page.
     */
    private Control body;

    /**
     * Whether this page has the standard Apply and Defaults buttons; 
     * <code>true</code> by default.
     *
     * @see #noDefaultAndApplyButton
     */
    private boolean createDefaultAndApplyButton = true;

    /**
     * Standard Defaults button, or <code>null</code> if none.
     * This button has id <code>DEFAULTS_ID</code>.
     */
    private Button defaultsButton = null;

    /**
     * The container this preference page belongs to; <code>null</code>
     * if none.
     */
    private IPreferencePageContainer container = null;

    /**
     * Standard Apply button, or <code>null</code> if none.
     * This button has id <code>APPLY_ID</code>.
     */
    private Button applyButton = null;

    /**
     * Description label.
     * 
     * @see #createDescriptionLabel.
     */
    private Label descriptionLabel;

    /**
     * Caches size of page.
     */
    private Point size = null;

    /**
     * The message area for displaying the messages.
     * If this is <code>null</code> messages are forwarded
     * to the parent.
     */
	private MessageRegion messageArea;

    /**
     * Creates a new preference page with an empty title and no image.
     */
    protected PreferencePage() {
        this(""); //$NON-NLS-1$
    }

    /**
     * Creates a new preference page with the given title and no image.
     *
     * @param title the title of this preference page
     */
    protected PreferencePage(String title) {
        super(title);
    }

    /**
     * Creates a new abstract preference page with the given title and image.
     *
     * @param title the title of this preference page
     * @param image the image for this preference page,
     *  or <code>null</code> if none
     */
    protected PreferencePage(String title, ImageDescriptor image) {
        super(title, image);
    }

    /**
     * Computes the size for this page's UI control.
     * <p>
     * The default implementation of this <code>IPreferencePage</code>
     * method returns the size set by <code>setSize</code>; if no size
     * has been set, but the page has a UI control, the framework
     * method <code>doComputeSize</code> is called to compute the size.
     * </p>
     *
     * @return the size of the preference page encoded as
     *   <code>new Point(width,height)</code>, or 
     *   <code>(0,0)</code> if the page doesn't currently have any UI component
     */
    public Point computeSize() {
        if (size != null)
            return size;
        Control control = getControl();
        if (control != null) {
            size = doComputeSize();
            return size;
        }
        return new Point(0, 0);
    }

    /**
     * Contributes additional buttons to the given composite.
     * <p>
     * The default implementation of this framework hook method does
     * nothing. Subclasses should override this method to contribute buttons 
     * to this page's button bar. For each button a subclass contributes,
     * it must also increase the parent's grid layout number of columns
     * by one; that is,
     * <pre>
     * ((GridLayout) parent.getLayout()).numColumns++);
     * </pre>
     * </p>
     *
     * @param parent the button bar
     */
    protected void contributeButtons(Composite parent) {
    }

    /**
     * Creates and returns the SWT control for the customized body 
     * of this preference page under the given parent composite.
     * <p>
     * This framework method must be implemented by concrete subclasses. Any
     * subclass returning a <code>Composite</code> object whose <code>Layout</code>
     * has default margins (for example, a <code>GridLayout</code>) are expected to
     * set the margins of this <code>Layout</code> to 0 pixels. 
     * </p>
     *
     * @param parent the parent composite
     * @return the new control
     */
    protected abstract Control createContents(Composite parent);

    /**
     * The <code>PreferencePage</code> implementation of this 
     * <code>IDialogPage</code> method creates a description label
     * and button bar for the page. It calls <code>createContents</code>
     * to create the custom contents of the page.
     * <p>
     * If a subclass that overrides this method creates a <code>Composite</code>
     * that has a layout with default margins (for example, a <code>GridLayout</code>)
     * it is expected to set the margins of this <code>Layout</code> to 0 pixels.
     */
    public void createControl(Composite parent){
    	createControl(parent,true);
    }
    
    /**
     * Create the contents of the receiver with an
     * optional message area.
     * 
     * @param parent
     * @param createMessageArea Create a MessageRegion
     * for the receiver if this is true.
     * <strong>NOTE:</strong> This API is experimental
     * and subject to change before the 3.1 release.
     * @since 3.1
     */
    public void createControl(Composite parent, boolean createMessageArea) {

        GridData gd;
        Composite content = new Composite(parent, SWT.NONE);
        setControl(content);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        content.setLayout(layout);
        //Apply the font on creation for backward compatibility
        applyDialogFont(content);

        // initialize the dialog units
        initializeDialogUnits(content);

        descriptionLabel = createDescriptionLabel(content);
        if (descriptionLabel != null) {
            descriptionLabel.setLayoutData(new GridData(
                    GridData.FILL_HORIZONTAL));
        }

        body = createContents(content);
        if (body != null)
            // null is not a valid return value but support graceful failure
            body.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite buttonBar = new Composite(content, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.makeColumnsEqualWidth = false;
        buttonBar.setLayout(layout);
        
        if(createMessageArea){
        	layout.numColumns = layout.numColumns + 1;
        	createMessageArea(buttonBar);
        	gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        }    
        else
        	 gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
        
        buttonBar.setLayoutData(gd);

        contributeButtons(buttonBar);
        
        if (createDefaultAndApplyButton) {
            layout.numColumns = layout.numColumns + 2;
			String[] labels = JFaceResources.getStrings(new String[] {
					"defaults", "apply" }); //$NON-NLS-2$//$NON-NLS-1$
			int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
			defaultsButton = new Button(buttonBar, SWT.PUSH);
			defaultsButton.setText(labels[0]);
			Dialog.applyDialogFont(defaultsButton);
			GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			Point minButtonSize = defaultsButton.computeSize(SWT.DEFAULT,
					SWT.DEFAULT, true);
			data.widthHint = Math.max(widthHint, minButtonSize.x);
			defaultsButton.setLayoutData(data);
			defaultsButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					performDefaults();
				}
			});

            applyButton = new Button(buttonBar, SWT.PUSH);
			applyButton.setText(labels[1]);
			Dialog.applyDialogFont(applyButton);
			data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			minButtonSize = applyButton.computeSize(SWT.DEFAULT, SWT.DEFAULT,
					true);
			data.widthHint = Math.max(widthHint, minButtonSize.x);
			applyButton.setLayoutData(data);
			applyButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					performApply();
				}
			});
            applyButton.setEnabled(isValid());
            applyDialogFont(buttonBar);
        } else {
            /* Check if there are any other buttons on the button bar.
             * If not, throw away the button bar composite.  Otherwise
             * there is an unusually large button bar.
             */
            if (buttonBar.getChildren().length < 1)
                buttonBar.dispose();
        }
    }

	/**
	 * Create a message area for the receiver.
	 * @param content
	 */
	private void createMessageArea(Composite content) {
		
		messageArea = new MessageRegion();
		messageArea.createContents(content);
		GridData messageData = 
			new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		messageArea.setMessageLayoutData(messageData);
		
	}

	/**
     * Apply the dialog font to the composite and it's children
     * if it is set. Subclasses may override if they wish to
     * set the font themselves.
     * @param composite
     */
    protected void applyDialogFont(Composite composite) {
        Dialog.applyDialogFont(composite);
    }

    /**
     * Creates and returns an SWT label under the given composite.
     *
     * @param parent the parent composite
     * @return the new label
     */
    protected Label createDescriptionLabel(Composite parent) {
        Label result = null;
        String description = getDescription();
        if (description != null) {
            result = new Label(parent, SWT.WRAP);
            result.setFont(parent.getFont());
            result.setText(description);
        }
        return result;
    }

    /**
     * Computes the size needed by this page's UI control.
     * <p>
     * All pages should override this method and set the appropriate sizes
     * of their widgets, and then call <code>super.doComputeSize</code>.
     * </p>
     *
     * @return the size of the preference page encoded as
     *   <code>new Point(width,height)</code>
     */
    protected Point doComputeSize() {
        if (descriptionLabel != null && body != null) {
            Point bodySize = body.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
            GridData gd = (GridData) descriptionLabel.getLayoutData();
            gd.widthHint = bodySize.x;
            descriptionLabel.getParent().layout(true);
        }
        return getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
    }

    /**
     * Returns the preference store of this preference page.
     * <p>
     * This is a framework hook method for subclasses to return a
     * page-specific preference store. The default implementation
     * returns <code>null</code>.
     * </p>
     *
     * @return the preference store, or <code>null</code> if none
     */
    protected IPreferenceStore doGetPreferenceStore() {
        return null;
    }

    /**
     * Returns the container of this page.
     *
     * @return the preference page container, or <code>null</code> if this
     *   page has yet to be added to a container
     */
    public IPreferencePageContainer getContainer() {
        return container;
    }

    /**
     * Returns the preference store of this preference page.
     *
     * @return the preference store , or <code>null</code> if none
     */
    public IPreferenceStore getPreferenceStore() {
        if (preferenceStore == null)
            preferenceStore = doGetPreferenceStore();
        if (preferenceStore != null)
            return preferenceStore;
        else if (container != null)
            return container.getPreferenceStore();
        return null;
    }

    /**	
     * The preference page implementation of an <code>IPreferencePage</code>
     * method returns whether this preference page is valid. Preference
     * pages are considered valid by default; call <code>setValid(false)</code>
     * to make a page invalid.
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * Suppresses creation of the standard Default and Apply buttons
     * for this page.
     * <p>
     * Subclasses wishing a preference page wihthout these buttons
     * should call this framework method before the page's control
     * has been created.
     * </p>
     */
    protected void noDefaultAndApplyButton() {
        createDefaultAndApplyButton = false;
    }

    /**
     * The <code>PreferencePage</code> implementation of this 
     * <code>IPreferencePage</code> method returns <code>true</code>
     * if the page is valid.
     */
    public boolean okToLeave() {
        return isValid();
    }

    /**
     * Performs special processing when this page's Apply button has been pressed.
     * <p>
     * This is a framework hook method for sublcasses to do special things when
     * the Apply button has been pressed.
     * The default implementation of this framework method simply calls
     * <code>performOk</code> to simulate the pressing of the page's OK button.
     * </p>
     * 
     * @see #performOk
     */
    protected void performApply() {
        performOk();
    }

    /**	
     * The preference page implementation of an <code>IPreferencePage</code>
     * method performs special processing when this page's Cancel button has
     * been pressed.
     * <p>
     * This is a framework hook method for sublcasses to do special things when
     * the Cancel button has been pressed. The default implementation of this
     * framework method does nothing and returns <code>true</code>.
     */
    public boolean performCancel() {
        return true;
    }

    /**
     * Performs special processing when this page's Defaults button has been pressed.
     * <p>
     * This is a framework hook method for subclasses to do special things when
     * the Defaults button has been pressed.
     * Subclasses may override, but should call <code>super.performDefaults</code>.
     * </p>
     */
    protected void performDefaults() {
        updateApplyButton();
    }

    /** 
     * Method declared on IPreferencePage.
     * Subclasses should override
     */
    public boolean performOk() {
        return true;
    }

    /** (non-Javadoc)
     * Method declared on IPreferencePage.
     */
    public void setContainer(IPreferencePageContainer container) {
        this.container = container;
    }

    /**
     * The <code>PreferencePage</code> implementation of this method 
     * declared on <code>DialogPage</code> updates the container.
     */
    public void setErrorMessage(String newMessage) {
        super.setErrorMessage(newMessage);
        if(messageArea == null){
        	if (getContainer() != null) 
        		getContainer().updateMessage();
        }
        else 
        	messageArea.updateText(newMessage, IMessageProvider.ERROR);
        
    }

    /**
     * The <code>PreferencePage</code> implementation of this method 
     * declared on <code>DialogPage</code> updates the container.
     */
    public void setMessage(String newMessage, int newType) {
        super.setMessage(newMessage, newType);
        if(messageArea == null){
        	if (getContainer() != null) 
        		getContainer().updateMessage();
        }
        else 
        	messageArea.updateText(newMessage, IMessageProvider.NONE);
    }

    /**
     * Sets the preference store for this preference page.
     * <p>
     * If preferenceStore is set to null, getPreferenceStore
     * will invoke doGetPreferenceStore the next time it is called.
     * </p>
     *
     * @param store the preference store, or <code>null</code>
     * @see #getPreferenceStore
     */
    public void setPreferenceStore(IPreferenceStore store) {
        preferenceStore = store;
    }

    /* (non-Javadoc)
     * Method declared on IPreferencePage.
     */
    public void setSize(Point uiSize) {
        Control control = getControl();
        if (control != null) {
            control.setSize(uiSize);
            size = uiSize;
        }
    }

    /**
     * The <code>PreferencePage</code> implementation of this <code>IDialogPage</code>
     * method extends the <code>DialogPage</code> implementation to update
     * the preference page container title. Subclasses may extend.
     */
    public void setTitle(String title) {
        super.setTitle(title);
        if (getContainer() != null)
            getContainer().updateTitle();
    }

    /**
     * Sets whether this page is valid.
     * The enable state of the container buttons and the
     * apply button is updated when a page's valid state 
     * changes.
     * <p>
     *
     * @param b the new valid state
     */
    public void setValid(boolean b) {
        boolean oldValue = isValid;
        isValid = b;
        if (oldValue != isValid) {
            // update container state
            if (getContainer() != null)
                getContainer().updateButtons();
            // update page state
            updateApplyButton();
        }
    }

    /**
     * Returns a string suitable for debugging purpose only.
     */
    public String toString() {
        return getTitle();
    }

    /**
     * Updates the enabled state of the Apply button to reflect whether 
     * this page is valid.
     */
    protected void updateApplyButton() {
        if (applyButton != null)
            applyButton.setEnabled(isValid());
    }

    /**
     * Creates a composite with a highlighted Note entry and a message text.
     * This is designed to take up the full width of the page.
     * 
     * @param font the font to use
     * @param composite the parent composite
     * @param title the title of the note
     * @param message the message for the note
     * @return the composite for the note
     */
    protected Composite createNoteComposite(Font font, Composite composite,
            String title, String message) {
        Composite messageComposite = new Composite(composite, SWT.NONE);
        GridLayout messageLayout = new GridLayout();
        messageLayout.numColumns = 2;
        messageLayout.marginWidth = 0;
        messageLayout.marginHeight = 0;
        messageComposite.setLayout(messageLayout);
        messageComposite.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL));
        messageComposite.setFont(font);

        final Label noteLabel = new Label(messageComposite, SWT.BOLD);
        noteLabel.setText(title);
        noteLabel.setFont(JFaceResources.getBannerFont());
        noteLabel
                .setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

        final IPropertyChangeListener fontListener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (JFaceResources.BANNER_FONT.equals(event.getProperty())) {
                    noteLabel.setFont(JFaceResources
                            .getFont(JFaceResources.BANNER_FONT));
                }
            }
        };
        JFaceResources.getFontRegistry().addListener(fontListener);
        noteLabel.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent event) {
                JFaceResources.getFontRegistry().removeListener(fontListener);
            }
        });

        Label messageLabel = new Label(messageComposite, SWT.WRAP);
        messageLabel.setText(message);
        messageLabel.setFont(font);
        return messageComposite;
    }

    /**
     * Returns the Apply button.
     * 
     * @return the Apply button
     */
    protected Button getApplyButton() {
        return applyButton;
    }

    /**
     * Returns the Restore Defaults button.
     * 
     * @return the Restore Defaults button
     */
    protected Button getDefaultsButton() {
        return defaultsButton;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#performHelp()
     */
    public void performHelp() {
        getControl().notifyListeners(SWT.Help, new Event());
    }

}