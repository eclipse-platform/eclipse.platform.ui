package org.eclipse.jface.preference;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.util.Assert; 
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import java.util.*;

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
public abstract class PreferencePage extends DialogPage implements IPreferencePage {

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
 * Creates a new preference page with an empty title and no image.
 */
protected PreferencePage() {
	this("");//$NON-NLS-1$
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
public void createControl(Composite parent) {

	GridData gd;
	Composite content= new Composite(parent, SWT.NULL);
	setControl(content);
	Font font = parent.getFont();
	GridLayout layout = new GridLayout();
	layout.marginWidth = 0;
	layout.marginHeight = 0;
	content.setLayout(layout);

	// initialize the dialog units
	initializeDialogUnits(content);
	
	descriptionLabel= createDescriptionLabel(content);
	if (descriptionLabel != null) {
		descriptionLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}
		
	body = createContents(content);
	if (body != null) // null is not a valid return value but support graceful failure
		body.setLayoutData(new GridData(GridData.FILL_BOTH));

	Composite buttonBar= new Composite(content, SWT.NULL);
	layout= new GridLayout();
	layout.numColumns= 0; 
	layout.marginHeight= 0; 
	layout.marginWidth= 0;
	buttonBar.setLayout(layout);
	gd= new GridData(); gd.horizontalAlignment= gd.END;
	buttonBar.setLayoutData(gd);

	contributeButtons(buttonBar);

	if (createDefaultAndApplyButton) {	
		layout.numColumns= layout.numColumns + 2;
		String[] labels= JFaceResources.getStrings(new String[] {"defaults", "apply"});//$NON-NLS-2$//$NON-NLS-1$
		int heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
	    int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		defaultsButton= new Button(buttonBar, SWT.PUSH);
		defaultsButton.setFont(font);
		defaultsButton.setText(labels[0]);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.heightHint = heightHint;
		data.widthHint = Math.max(widthHint, defaultsButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		defaultsButton.setLayoutData(data);
		defaultsButton.addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					performDefaults();
				}
			}
		);

	
		applyButton= new Button(buttonBar, SWT.PUSH);
		applyButton.setFont(font);
		applyButton.setText(labels[1]);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.heightHint = heightHint;
		data.widthHint = Math.max(widthHint, applyButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		applyButton.setLayoutData(data);
		applyButton.addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					performApply();
				}
			}
		);
		applyButton.setEnabled(isValid());
	}	
	else {
		/* Check if there are any other buttons on the button bar.
		 * If not, throw away the button bar composite.  Otherwise
		 * there is an unusually large button bar.
		 */
		if (buttonBar.getChildren().length < 1)
			buttonBar.dispose();
	}
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
		Point size = body.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		GridData gd = (GridData) descriptionLabel.getLayoutData();
		gd.widthHint = size.x;
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
	return (IPreferencePageContainer)container;
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
	else
		if (container != null)
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
 * This is a framework hook method for sublcasses to do special things when
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
 * Sets or clears the error message for this page.
 *
 * @param newMessage the message, or <code>null</code> to clear
 *   the error message
 */
public void setErrorMessage(String newMessage) {
	super.setErrorMessage(newMessage);
	if (getContainer() != null) {
		getContainer().updateMessage();
	}
}
/**
 * Sets or clears the message for this page.
 *
 * @param newMessage the message, or <code>null</code> to clear
 *   the message
 */
public void setMessage(String newMessage) {
	super.setMessage(newMessage);
	if (getContainer() != null) {
		getContainer().updateMessage();
	}
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
}
