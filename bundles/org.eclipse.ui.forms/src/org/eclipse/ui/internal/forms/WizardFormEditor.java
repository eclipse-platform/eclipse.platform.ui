/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardContainer2;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ILayoutExtension;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.internal.forms.widgets.SWTUtil;
import org.eclipse.ui.internal.forms.widgets.WrappedPageBook;
import org.eclipse.ui.part.EditorPart;

/**
 * A dialog to show a wizard to the end user.
 * <p>
 * In typical usage, the client instantiates this class with a particular
 * wizard. The dialog serves as the wizard container and orchestrates the
 * presentation of its pages.
 * <p>
 * The standard layout is roughly as follows: it has an area at the top
 * containing both the wizard's title, description, and image; the actual wizard
 * page appears in the middle; below that is a progress indicator (which is made
 * visible if needed); and at the bottom of the page is message line and a
 * button bar containing Help, Next, Back, Finish, and Cancel buttons (or some
 * subset).
 * </p>
 * <p>
 * Clients may subclass <code>WizardDialog</code>, although this is rarely
 * required.
 * </p>
 */
public class WizardFormEditor extends EditorPart implements IWizardContainer2 {
	/**
	 * Image registry key for error message image (value
	 * <code>"dialog_title_error_image"</code>).
	 */
	public static final String WIZ_IMG_ERROR = "dialog_title_error_image"; //$NON-NLS-1$

	public static final int OK = 0;

	public static final int CANCEL = 1;

	// The wizard the dialog is currently showing.
	private IWizard wizard;

	// Wizards to dispose
	private ArrayList createdWizards = new ArrayList();

	// Current nested wizards
	private ArrayList nestedWizards = new ArrayList();

	private Cursor arrowCursor;

	// The currently displayed page.
	private IWizardPage currentPage = null;

	// The number of long running operation executed from the dialog.
	private long activeRunningOperations = 0;

	private String pageDescription;

	private MessageDialog windowClosingDialog;

	// Navigation buttons
	private Button backButton;
	private Button nextButton;
	private Button finishButton;
	private Button cancelButton;
	private Button helpButton;
	private Hashtable buttons;

	private SelectionAdapter cancelListener;

	private boolean isMovingToPreviousPage = false;

	private ScrolledForm form;
	private Label descriptionLabel;
	private Label separator;
	private WrappedPageBook pageContainer;
	private Composite buttonContainer;

	private static final String FOCUS_CONTROL = "focusControl"; //$NON-NLS-1$

	private boolean lockedUI = false;

	private int returnCode = OK;

	private FormToolkit toolkit;
	
	private class WizardFormLayout extends Layout implements ILayoutExtension {
		private int wMargin = 5;
		private int hMargin = 5;
		private int spacing = 5;
		protected Point computeSize(Composite composite, int wHint, int hHint,
				boolean flushCache) {
			int innerWHint = wHint;
			if (wHint!=SWT.DEFAULT)
				innerWHint -= wMargin + wMargin;
			
			int dWHint = innerWHint;
			Point dsize = descriptionLabel.computeSize(dWHint, SWT.DEFAULT, flushCache);
			Point psize = pageContainer.computeSize(innerWHint, hHint, flushCache);
			Point bsize = buttonContainer.computeSize(innerWHint, hHint, flushCache);
			Point size = new Point(0,0);
			size.x = dsize.x;
			size.x = Math.max(size.x, psize.x);
			size.x = Math.max(size.x, bsize.x);
			size.x += wMargin + wMargin;
			int dheight = dsize.y;
			Point ssize = separator.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
			size.y = dheight + spacing + psize.y + spacing + ssize.y + bsize.y;
			size.y += hMargin + hMargin;
			return size;
		}
		protected void layout(Composite composite, boolean flushCache) {
			Rectangle clientArea = composite.getClientArea();
			int x = wMargin;
			int y = hMargin;
			int innerWidth = clientArea.width - wMargin - wMargin;
			int dWidth = innerWidth;
			Point dsize = descriptionLabel.computeSize(dWidth, SWT.DEFAULT, flushCache);
			descriptionLabel.setBounds(x, y, dsize.x, dsize.y);
			
			Point psize = pageContainer.computeSize(innerWidth, SWT.DEFAULT, flushCache);
			y += dsize.y + spacing;
			pageContainer.setBounds(x, y, psize.x, psize.y);
			y += psize.y + spacing;
			Point ssize = separator.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
			separator.setBounds(x, y, innerWidth, ssize.y);
			y += ssize.y;
			buttonContainer.setBounds(x, y, innerWidth, clientArea.height-hMargin-hMargin-y);
		}
		
		public int computeMaximumWidth(Composite parent, boolean changed) {
			int dwidth = descriptionLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			int pwidth = ((ILayoutExtension)pageContainer.getLayout()).computeMaximumWidth(pageContainer, changed);
			int width = Math.max(dwidth, pwidth);
			int bwidth = buttonContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			return Math.max(bwidth, width);
		}
		public int computeMinimumWidth(Composite parent, boolean changed) {
			int dwidth = descriptionLabel.computeSize(10, SWT.DEFAULT).x;
			int pwidth = ((ILayoutExtension)pageContainer.getLayout()).computeMinimumWidth(pageContainer, changed);
			int width = Math.max(dwidth, pwidth);
			int bwidth = buttonContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			return Math.max(bwidth, width);
		}
	}

	/**
	 * Creates a new wizard dialog for the given wizard.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param newWizard
	 *            the wizard this dialog is working on
	 */
	public WizardFormEditor() {
		// since VAJava can't initialize an instance var with an anonymous
		// class outside a constructor we do it here:
		cancelListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				cancelPressed();
			}
		};
	}

	/**
	 * About to start a long running operation tiggered through the wizard.
	 * Shows the progress monitor and disables the wizard's buttons and
	 * controls.
	 * 
	 * @param enableCancelButton
	 *            <code>true</code> if the Cancel button should be enabled,
	 *            and <code>false</code> if it should be disabled
	 * @return the saved UI state
	 */
	private Object aboutToStart(boolean enableCancelButton) {
		Map savedState = null;
		if (getShell() != null) {
			// Save focus control
			Control focusControl = getShell().getDisplay().getFocusControl();
			if (focusControl != null && focusControl.getShell() != getShell())
				focusControl = null;
			cancelButton.removeSelectionListener(cancelListener);
			// Set the busy cursor to all shells.
			Display d = getShell().getDisplay();
			// Set the arrow cursor to the cancel component.
			arrowCursor = new Cursor(d, SWT.CURSOR_ARROW);
			cancelButton.setCursor(arrowCursor);
			// Deactivate shell
			savedState = saveUIState(enableCancelButton);
			if (focusControl != null)
				savedState.put(FOCUS_CONTROL, focusControl);
		}
		return savedState;
	}
	
    private void stopped(Object savedState) {
        if (getShell() != null) {
//            if (wizard.needsProgressMonitor()) {
//                progressMonitorPart.setVisible(false);
//                progressMonitorPart.removeFromCancelComponent(cancelButton);
//            }
            Map state = (Map) savedState;
            restoreUIState(state);
            cancelButton.addSelectionListener(cancelListener);
            setDisplayCursor(null);
            cancelButton.setCursor(null);
            arrowCursor.dispose();
            arrowCursor = null;
            Control focusControl = (Control) state.get(FOCUS_CONTROL);
            if (focusControl != null)
                focusControl.setFocus();
        }
    }	

	/**
	 * The Back button has been pressed.
	 */
	protected void backPressed() {
		IWizardPage page = currentPage.getPreviousPage();
		if (page == null)
			// should never happen since we have already visited the page
			return;
		// set flag to indicate that we are moving back
		isMovingToPreviousPage = true;
		// show the page
		showPage(page);
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case IDialogConstants.HELP_ID: {
			helpPressed();
			break;
		}
		case IDialogConstants.BACK_ID: {
			backPressed();
			break;
		}
		case IDialogConstants.NEXT_ID: {
			nextPressed();
			break;
		}
		case IDialogConstants.FINISH_ID: {
			finishPressed();
			break;
		}
		// The Cancel button has a listener which calls cancelPressed directly
		}
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected void cancelPressed() {
		if (activeRunningOperations <= 0) {
			// Close the dialog. The check whether the dialog can be
			// closed or not is done in <code>okToClose</code>.
			// This ensures that the check is also evaluated when the user
			// presses the window's close button.
			close();
		} else {
			cancelButton.setEnabled(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		if (okToClose()) {
			hardClose();
			return true;
		} else
			return false;
	}
	
	public void dispose() {
		if (toolkit!=null) {
			toolkit.dispose();
			toolkit=null;
		}
		super.dispose();
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		if (wizard.isHelpAvailable()) {
			helpButton = createButton(parent, IDialogConstants.HELP_ID,
					IDialogConstants.HELP_LABEL, false);
		}
		if (wizard.needsPreviousAndNextButtons())
			createPreviousAndNextButtons(parent);
		finishButton = createButton(parent, IDialogConstants.FINISH_ID,
				IDialogConstants.FINISH_LABEL, true);
		cancelButton = createCancelButton(parent);
	}

	/**
	 * Creates the Cancel button for this wizard dialog. Creates a standard (
	 * <code>SWT.PUSH</code>) button and registers for its selection events.
	 * Note that the number of columns in the button bar composite is
	 * incremented. The Cancel button is created specially to give it a
	 * removeable listener.
	 * 
	 * @param parent
	 *            the parent button bar
	 * @return the new Cancel button
	 */
	private Button createCancelButton(Composite parent) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		Button button = toolkit.createButton(parent, IDialogConstants.CANCEL_LABEL, SWT.PUSH);
		setButtonLayoutData(button);
		button.setFont(parent.getFont());
		button.setData(new Integer(IDialogConstants.CANCEL_ID));
		button.addSelectionListener(cancelListener);
		return button;
	}

	protected void setButtonLayoutData(Button button) {
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		button.setLayoutData(data);
		SWTUtil.setButtonDimensionHint(button);
	}

	/**
	 * Return the cancel button if the id is a the cancel id.
	 * 
	 * @param id
	 *            the button id
	 * @return the button corresponding to the button id
	 */
	protected Button getButton(int id) {
		if (buttons != null)
			return (Button) buttons.get(new Integer(id));
		return null;
	}

	/**
	 * The <code>WizardDialog</code> implementation of this
	 * <code>Window</code> method calls call <code>IWizard.addPages</code>
	 * to allow the current wizard to add extra pages, then
	 * <code>super.createContents</code> to create the controls. It then calls
	 * <code>IWizard.createPageControls</code> to allow the wizard to
	 * pre-create their page controls prior to opening, so that the wizard opens
	 * to the correct size. And finally it shows the first page.
	 */
	public void createPartControl(Composite parent) {
		// Allow the wizard to add pages to itself
		// Need to call this now so page count is correct
		// for determining if next/previous buttons are needed
		toolkit = new FormToolkit(parent.getDisplay());
		wizard.addPages();
		createForm(parent);
		// Allow the wizard pages to precreate their page controls
		createPageControls();
		// Show the first page
		showStartingPage();
	}

	private void createForm(Composite parent) {
		form = toolkit.createScrolledForm(parent);
		//form.getForm().setBackgroundImageAlignment(SWT.RIGHT);
		//form.getForm().setBackgroundImageClipped(false);
		form.getBody().setLayout(new WizardFormLayout());
		descriptionLabel = toolkit.createLabel(form.getBody(), "", SWT.WRAP);
		pageContainer = new WrappedPageBook(form.getBody(), SWT.NULL);
		separator = toolkit.createLabel(form.getBody(), null, SWT.SEPARATOR|SWT.HORIZONTAL);
		buttonContainer = toolkit.createComposite(form.getBody());
		buttonContainer.setLayout(new GridLayout());
		createButtonsForButtonBar(buttonContainer);
	}

	/**
	 * Allow the wizard's pages to pre-create their page controls. This allows
	 * the wizard dialog to open to the correct size.
	 */
	private void createPageControls() {
		// Allow the wizard pages to precreate their page controls
		// This allows the wizard to open to the correct size
		wizard.createPageControls(pageContainer);
		// Ensure that all of the created pages are initially not visible
		IWizardPage[] pages = wizard.getPages();
		for (int i = 0; i < pages.length; i++) {
			IWizardPage page = pages[i];
			if (page.getControl() != null)
				page.getControl().setVisible(false);
		}
	}

	/**
	 * Creates the Previous and Next buttons for this wizard dialog. Creates
	 * standard (<code>SWT.PUSH</code>) buttons and registers for their
	 * selection events. Note that the number of columns in the button bar
	 * composite is incremented. These buttons are created specially to prevent
	 * any space between them.
	 * 
	 * @param parent
	 *            the parent button bar
	 * @return a composite containing the new buttons
	 */
	private Composite createPreviousAndNextButtons(Composite parent) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		Composite composite = new Composite(parent, SWT.NONE);
		// create a layout with spacing and margins appropriate for the font
		// size.
		GridLayout layout = new GridLayout();
		layout.numColumns = 0; // will be incremented by createButton
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER
				| GridData.VERTICAL_ALIGN_CENTER);
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());
		backButton = createButton(composite, IDialogConstants.BACK_ID,
				IDialogConstants.BACK_LABEL, false);
		nextButton = createButton(composite, IDialogConstants.NEXT_ID,
				IDialogConstants.NEXT_LABEL, false);
		return composite;
	}

	/**
	 * Creates and return a new wizard closing dialog without openiong it.
	 * 
	 * @return MessageDalog
	 */
	private MessageDialog createWizardClosingDialog() {
		MessageDialog result = new MessageDialog(getShell(), JFaceResources
				.getString("WizardClosingDialog.title"), //$NON-NLS-1$
				null, JFaceResources.getString("WizardClosingDialog.message"), //$NON-NLS-1$
				MessageDialog.QUESTION,
				new String[] { IDialogConstants.OK_LABEL }, 0);
		return result;
	}

	/**
	 * The Finish button has been pressed.
	 */
	protected void finishPressed() {
		// Wizards are added to the nested wizards list in setWizard.
		// This means that the current wizard is always the last wizard in the
		// list.
		// Note that we first call the current wizard directly (to give it a
		// chance to
		// abort, do work, and save state) then call the remaining n-1 wizards
		// in the
		// list (to save state).
		if (wizard.performFinish()) {
			// Call perform finish on outer wizards in the nested chain
			// (to allow them to save state for example)
			for (int i = 0; i < nestedWizards.size() - 1; i++) {
				((IWizard) nestedWizards.get(i)).performFinish();
			}
			// Hard close the dialog.
			setReturnCode(OK);
			hardClose();
		}
	}

	/*
	 * (non-Javadoc) Method declared on IWizardContainer.
	 */
	public IWizardPage getCurrentPage() {
		return currentPage;
	}

	/**
	 * Returns the wizard this dialog is currently displaying.
	 * 
	 * @return the current wizard
	 */
	protected IWizard getWizard() {
		return wizard;
	}

	/**
	 * Closes this window.
	 * 
	 * @return <code>true</code> if the window is (or was already) closed, and
	 *         <code>false</code> if it is still open
	 */
	private void hardClose() {
		// inform wizards
		for (int i = 0; i < createdWizards.size(); i++) {
			IWizard createdWizard = (IWizard) createdWizards.get(i);
			createdWizard.dispose();
			// Remove this dialog as a parent from the managed wizard.
			// Note that we do this after calling dispose as the wizard or
			// its pages may need access to the container during
			// dispose code
			createdWizard.setContainer(null);
		}
		getEditorSite().getPage().closeEditor(this, false);
	}

	/**
	 * The Help button has been pressed.
	 */
	protected void helpPressed() {
		if (currentPage != null) {
			currentPage.performHelp();
		}
	}

	/**
	 * The Next button has been pressed.
	 */
	protected void nextPressed() {
		IWizardPage page = currentPage.getNextPage();
		if (page == null) {
			// something must have happend getting the next page
			return;
		}
		// show the next page
		showPage(page);
	}

	/**
	 * Checks whether it is alright to close this wizard dialog and performed
	 * standard cancel processing. If there is a long running operation in
	 * progress, this method posts an alert message saying that the wizard
	 * cannot be closed.
	 * 
	 * @return <code>true</code> if it is alright to close this dialog, and
	 *         <code>false</code> if it is not
	 */
	private boolean okToClose() {
		if (activeRunningOperations > 0) {
			synchronized (this) {
				windowClosingDialog = createWizardClosingDialog();
			}
			windowClosingDialog.open();
			synchronized (this) {
				windowClosingDialog = null;
			}
			return false;
		}
		return wizard.performCancel();
	}

	/**
	 * Restores the enabled/disabled state of the given control.
	 * 
	 * @param w
	 *            the control
	 * @param h
	 *            the map (key type: <code>String</code>, element type:
	 *            <code>Boolean</code>)
	 * @param key
	 *            the key
	 * @see #saveEnableStateAndSet
	 */
	private void restoreEnableState(Control w, Map h, String key) {
		if (w != null) {
			Boolean b = (Boolean) h.get(key);
			if (b != null)
				w.setEnabled(b.booleanValue());
		}
	}

	/**
	 * Restores the enabled/disabled state of the wizard dialog's buttons and
	 * the tree of controls for the currently showing page.
	 * 
	 * @param state
	 *            a map containing the saved state as returned by
	 *            <code>saveUIState</code>
	 * @see #saveUIState
	 */
	private void restoreUIState(Map state) {
		restoreEnableState(backButton, state, "back"); //$NON-NLS-1$
		restoreEnableState(nextButton, state, "next"); //$NON-NLS-1$
		restoreEnableState(finishButton, state, "finish"); //$NON-NLS-1$
		restoreEnableState(cancelButton, state, "cancel"); //$NON-NLS-1$
		restoreEnableState(helpButton, state, "help"); //$NON-NLS-1$
		Object pageValue = state.get("page"); //$NON-NLS-1$
		if (pageValue != null)//page may never have been created
			((ControlEnableState) pageValue).restore();
	}

	/*
	 * (non-Javadoc) Method declared on IRunnableContext.
	 */
	public void run(boolean fork, boolean cancelable,
			IRunnableWithProgress runnable) throws InvocationTargetException,
			InterruptedException {
        // The operation can only be canceled if it is executed in a separate thread.
        // Otherwise the UI is blocked anyway.
        Object state = null;
        if (activeRunningOperations == 0)
            state = aboutToStart(fork && cancelable);
        activeRunningOperations++;
        try {
            if (!fork)//If we are not forking do not open other dialogs
                lockedUI = true;
    		getSite().getWorkbenchWindow().run(fork, cancelable, runnable);
            lockedUI = false;
	    } finally {
	        activeRunningOperations--;
	        //Stop if this is the last one
	        if (state != null)
	            stopped(state);
	    }
	}

	/**
	 * Saves the enabled/disabled state of the given control in the given map,
	 * which must be modifiable.
	 * 
	 * @param w
	 *            the control, or <code>null</code> if none
	 * @param h
	 *            the map (key type: <code>String</code>, element type:
	 *            <code>Boolean</code>)
	 * @param key
	 *            the key
	 * @param enabled
	 *            <code>true</code> to enable the control, and
	 *            <code>false</code> to disable it
	 * @see #restoreEnableState(Control, Map, String)
	 */
	private void saveEnableStateAndSet(Control w, Map h, String key,
			boolean enabled) {
		if (w != null) {
			h.put(key, new Boolean(w.getEnabled()));
			w.setEnabled(enabled);
		}
	}

	/**
	 * Captures and returns the enabled/disabled state of the wizard dialog's
	 * buttons and the tree of controls for the currently showing page. All
	 * these controls are disabled in the process, with the possible excepton of
	 * the Cancel button.
	 * 
	 * @param keepCancelEnabled
	 *            <code>true</code> if the Cancel button should remain
	 *            enabled, and <code>false</code> if it should be disabled
	 * @return a map containing the saved state suitable for restoring later
	 *         with <code>restoreUIState</code>
	 * @see #restoreUIState
	 */
	private Map saveUIState(boolean keepCancelEnabled) {
		Map savedState = new HashMap(10);
		saveEnableStateAndSet(backButton, savedState, "back", false); //$NON-NLS-1$
		saveEnableStateAndSet(nextButton, savedState, "next", false); //$NON-NLS-1$
		saveEnableStateAndSet(finishButton, savedState, "finish", false); //$NON-NLS-1$
		saveEnableStateAndSet(cancelButton, savedState,
				"cancel", keepCancelEnabled); //$NON-NLS-1$
		saveEnableStateAndSet(helpButton, savedState, "help", false); //$NON-NLS-1$
		if (currentPage != null)
			savedState
					.put(
							"page", ControlEnableState.disable(currentPage.getControl())); //$NON-NLS-1$
		return savedState;
	}

	/**
	 * Sets the given cursor for all shells currently active for this window's
	 * display.
	 * 
	 * @param c
	 *            the cursor
	 */
	private void setDisplayCursor(Cursor c) {
		Shell[] shells = getShell().getDisplay().getShells();
		for (int i = 0; i < shells.length; i++)
			shells[i].setCursor(c);
	}

	/**
	 * Sets the wizard this dialog is currently displaying.
	 * 
	 * @param newWizard
	 *            the wizard
	 */
	protected void setWizard(IWizard newWizard) {
		wizard = newWizard;
		wizard.setContainer(this);
		if (!createdWizards.contains(wizard)) {
			createdWizards.add(wizard);
			// New wizard so just add it to the end of our nested list
			nestedWizards.add(wizard);
			if (form != null) {
				// Dialog is already open
				// Allow the wizard pages to precreate their page controls
				// This allows the wizard to open to the correct size
				createPageControls();
				// Ensure the dialog is large enough for the wizard
				form.reflow(true);
			}
		} else {
			// We have already seen this wizard, if it is the previous wizard
			// on the nested list then we assume we have gone back and remove
			// the last wizard from the list
			int size = nestedWizards.size();
			if (size >= 2 && nestedWizards.get(size - 2) == wizard)
				nestedWizards.remove(size - 1);
			else
				// Assume we are going forward to revisit a wizard
				nestedWizards.add(wizard);
		}
	}

	/*
	 * (non-Javadoc) Method declared on IWizardContainer.
	 */
	public void showPage(IWizardPage page) {
		if (page == null || page == currentPage) {
			return;
		}
		if (!isMovingToPreviousPage)
			// remember my previous page.
			page.setPreviousPage(currentPage);
		else
			isMovingToPreviousPage = false;
		//Update for the new page ina busy cursor if possible
		if (form == null)
			updateForPage(page);
		else {
			final IWizardPage finalPage = page;
			BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
				public void run() {
					updateForPage(finalPage);
				}
			});
		}
	}

	/**
	 * Update the receiver for the new page.
	 * 
	 * @param page
	 */
	private void updateForPage(IWizardPage page) {
		// ensure this page belongs to the current wizard
		if (wizard != page.getWizard())
			setWizard(page.getWizard());
		// ensure that page control has been created
		// (this allows lazy page control creation)
		if (page.getControl() == null) {
			page.createControl(pageContainer);
			// the page is responsible for ensuring the created control is
			// accessable
			// via getControl.
			Assert.isNotNull(page.getControl());
		}
		// make the new page visible
		IWizardPage oldPage = currentPage;
		currentPage = page;
		currentPage.setVisible(true);
		if (oldPage != null)
			oldPage.setVisible(false);
		putPageOnTop(currentPage);
		// update the dialog controls
		update();
	}
	
	private void putPageOnTop(IWizardPage page) {
		pageContainer.showPage(page.getControl());
	}

	/**
	 * Shows the starting page of the wizard.
	 */
	private void showStartingPage() {
		currentPage = wizard.getStartingPage();
		if (currentPage == null) {
			// something must have happend getting the page
			return;
		}
		// ensure the page control has been created
		if (currentPage.getControl() == null) {
			currentPage.createControl(pageContainer);
			// the page is responsible for ensuring the created control is
			// accessable
			// via getControl.
			Assert.isNotNull(currentPage.getControl());
			// we do not need to update the size since the call
			// to initialize bounds has not been made yet.
		}
		// make the new page visible
		currentPage.setVisible(true);
		putPageOnTop(currentPage);
		// update the dialog controls
		update();
	}

	/**
	 * Updates this dialog's controls to reflect the current page.
	 */
	protected void update() {
		// Update the window title
		updateWindowTitle();
		// Update the title bar
		updateTitleBar();
		// Update the buttons
		updateButtons();
		form.reflow(true);
	}

	/*
	 * (non-Javadoc) Method declared on IWizardContainer.
	 */
	public void updateButtons() {
		boolean canFlipToNextPage = false;
		boolean canFinish = wizard.canFinish();
		if (backButton != null)
			backButton.setEnabled(currentPage.getPreviousPage() != null);
		if (nextButton != null) {
			canFlipToNextPage = currentPage.canFlipToNextPage();
			nextButton.setEnabled(canFlipToNextPage);
		}
		finishButton.setEnabled(canFinish);
		// finish is default unless it is diabled and next is enabled
		if (canFlipToNextPage && !canFinish)
			getShell().setDefaultButton(nextButton);
		else
			getShell().setDefaultButton(finishButton);
	}

	/**
	 * Update the message line with the page's description.
	 * <p>
	 * A discription is shown only if there is no message or error message.
	 * </p>
	 */
	private void updateDescriptionMessage() {
		pageDescription = currentPage.getDescription();
		String text = pageDescription!=null?pageDescription:"";
		descriptionLabel.setText(text);
	}

	/*
	 * (non-Javadoc) Method declared on IWizardContainer.
	 */

	public void updateMessage() {
		if (currentPage == null)
			return;

		String pageMessage = currentPage.getMessage();
		int pageMessageType = IMessageProvider.NONE;
		if (pageMessage != null && currentPage instanceof IMessageProvider)
			pageMessageType = ((IMessageProvider) currentPage).getMessageType();
		else
			pageMessageType = IMessageProvider.NONE;
		if (pageMessage == null)
			setMessage(pageDescription);
		else
			setMessage(pageMessage, pageMessageType);
		setErrorMessage(currentPage.getErrorMessage());
	}

	private void setMessage(String newMessage) {
		setMessage(newMessage, IMessageProvider.NONE);
	}

	private void setMessage(String newMessage, int newType) {
		Image newImage = null;
		if (newMessage != null) {
			switch (newType) {
			case IMessageProvider.NONE:
				break;
			case IMessageProvider.INFORMATION:
				newImage = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO);
				break;
			case IMessageProvider.WARNING:
				newImage = JFaceResources
						.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
				break;
			case IMessageProvider.ERROR:
				newImage = JFaceResources
						.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
				break;
			}
		}
		showMessage(newMessage, newImage);
	}

	private void setErrorMessage(String errorMessage) {
		setMessage(errorMessage, IMessageProvider.ERROR);
	}

	private void showMessage(String newMessage, Image newImage) {
		IStatusLineManager mng = getEditorSite().getActionBars()
				.getStatusLineManager();
		mng.setMessage(newImage, newMessage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizardContainer2#updateSize()
	 */
	public void updateSize() {
	}

	/*
	 * (non-Javadoc) Method declared on IWizardContainer.
	 */
	public void updateTitleBar() {
		String s = null;
		if (currentPage != null)
			s = currentPage.getTitle();
		if (s == null)
			s = ""; //$NON-NLS-1$
		form.setText(s);
		//if (currentPage != null)
			//form.setBackgroundImage(currentPage.getImage());
		updateDescriptionMessage();
		updateMessage();
	}

	/*
	 * (non-Javadoc) Method declared on IWizardContainer.
	 */
	public void updateWindowTitle() {
		if (getShell() == null)
			// Not created yet
			return;
		String title = wizard.getWindowTitle();
		if (title == null)
			title = ""; //$NON-NLS-1$
		setPartName(title);
		firePropertyChange(PROP_TITLE);
	}

	/**
	 * @return Returns the returnCode.
	 */
	public int getReturnCode() {
		return returnCode;
	}

	/**
	 * @param returnCode
	 *            The returnCode to set.
	 */
	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

	protected Button createButton(Composite parent, int id, String label,
			boolean defaultButton) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		Button button = toolkit.createButton(parent, label, SWT.PUSH);
		button.setFont(JFaceResources.getDialogFont());
		button.setData(new Integer(id));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				buttonPressed(((Integer) event.widget.getData()).intValue());
			}
		});
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
		}
		setButtonLayoutData(button);
		if (buttons == null)
			buttons = new Hashtable();
		buttons.put(new Integer(id), button);
		return button;
	}

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		IWizard wizard = (IWizard)input.getAdapter(IWizard.class);
		if (wizard!=null)
			setWizard(wizard);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSaveAs()
	 */
	public void doSaveAs() {
	}	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isDirty()
	 */
	public boolean isDirty() {
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardContainer#getShell()
	 */
	public Shell getShell() {
		IEditorSite site = getEditorSite();
		if (site==null) return null;
		return site.getShell();
	}
	/**
	 * @return Returns the toolkit.
	 */
	public FormToolkit getToolkit() {
		return toolkit;
	}
}