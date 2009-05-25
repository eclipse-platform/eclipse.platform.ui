/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import java.io.File;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

/**
 * A dialog that prompts for a directory to use as a workspace.
 */
public class ChooseWorkspaceDialog extends TitleAreaDialog {
	
	private static final String DIALOG_SETTINGS_SECTION = "ChooseWorkspaceDialogSettings"; //$NON-NLS-1$
	
	private ChooseWorkspaceData launchData;

    private Combo text;

    private boolean suppressAskAgain = false;

    private boolean centerOnMonitor = false;
    /**
     * Create a modal dialog on the arugment shell, using and updating the
     * argument data object.
     * @param parentShell the parent shell for this dialog
     * @param launchData the launch data from past launches
     * 
     * @param suppressAskAgain
     *            true means the dialog will not have a "don't ask again" button
     * @param centerOnMonitor indicates whether the dialog should be centered on 
     * the monitor or according to it's parent if there is one
     */
    public ChooseWorkspaceDialog(Shell parentShell,
            ChooseWorkspaceData launchData, boolean suppressAskAgain, boolean centerOnMonitor) {
        super(parentShell);
        this.launchData = launchData;
        this.suppressAskAgain = suppressAskAgain;
        this.centerOnMonitor = centerOnMonitor;
    }

    /**
     * Show the dialog to the user (if needed). When this method finishes,
     * #getSelection will return the workspace that should be used (whether it
     * was just selected by the user or some previous default has been used.
     * The parameter can be used to override the users preference.  For example,
     * this is important in cases where the default selection is already in use
     * and the user is forced to choose a different one.
     * 
     * @param force
     *            true if the dialog should be opened regardless of the value of
     *            the show dialog checkbox
     */
    public void prompt(boolean force) {
        if (force || launchData.getShowDialog()) {
            open();

            // 70576: make sure dialog gets dismissed on ESC too
            if (getReturnCode() == CANCEL) {
				launchData.workspaceSelected(null);
			}

            return;
        }

        String[] recent = launchData.getRecentWorkspaces();

        // If the selection dialog was not used then the workspace to use is either the
        // most recent selection or the initialDefault (if there is no history).
        String workspace = null;
        if (recent != null && recent.length > 0) {
			workspace = recent[0];
		}
        if (workspace == null || workspace.length() == 0) {
			workspace = launchData.getInitialDefault();
		}
        launchData.workspaceSelected(TextProcessor.deprocess(workspace));
    }

    /**
     * Creates and returns the contents of the upper part of this dialog (above
     * the button bar).
     * <p>
     * The <code>Dialog</code> implementation of this framework method creates
     * and returns a new <code>Composite</code> with no margins and spacing.
     * </p>
     *
     * @param parent the parent composite to contain the dialog area
     * @return the dialog area control
     */
    protected Control createDialogArea(Composite parent) {
        String productName = getWindowTitle();

        Composite composite = (Composite) super.createDialogArea(parent);
        setTitle(IDEWorkbenchMessages.ChooseWorkspaceDialog_dialogTitle);
        setMessage(NLS.bind(IDEWorkbenchMessages.ChooseWorkspaceDialog_dialogMessage, productName));

        // bug 59934: load title image for sizing, but set it non-visible so the
        //            white background is displayed
        if (getTitleImageLabel() != null) {
			getTitleImageLabel().setVisible(false);
		}

        createWorkspaceBrowseRow(composite);
        if (!suppressAskAgain) {
			createShowDialogButton(composite);
		}
        
        // look for the eclipse.gcj property.  
        // If true, then we dont need any warning messages.
        // someone is asserting that we're okay on GCJ
        boolean gcj = Boolean.getBoolean("eclipse.gcj"); //$NON-NLS-1$
		String vmName = System.getProperty("java.vm.name");//$NON-NLS-1$
		if (!gcj && vmName != null && vmName.indexOf("libgcj") != -1) { //$NON-NLS-1$
			composite.getDisplay().asyncExec(new Runnable() {
				public void run() {
					// set this via an async - if we set it directly the dialog
					// will
					// be huge. See bug 223532
					setMessage(IDEWorkbenchMessages.UnsupportedVM_message,
							IMessageProvider.WARNING);
				}
			});
		}
        
        Dialog.applyDialogFont(composite);
        return composite;
    }

	/**
	 * Returns the title that the dialog (or splash) should have.
	 * 
	 * @return the window title
	 * @since 3.4
	 */
	public static String getWindowTitle() {
		String productName = null;
		IProduct product = Platform.getProduct();
		if (product != null) {
			productName = product.getName();
		}
		if (productName == null) {
			productName = IDEWorkbenchMessages.ChooseWorkspaceDialog_defaultProductName;
		}
		return productName;
	}

    /**
     * Configures the given shell in preparation for opening this window
     * in it.
     * <p>
     * The default implementation of this framework method
     * sets the shell's image and gives it a grid layout. 
     * Subclasses may extend or reimplement.
     * </p>
     * 
     * @param shell the shell
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(IDEWorkbenchMessages.ChooseWorkspaceDialog_dialogName);
    }

    /**
     * Notifies that the ok button of this dialog has been pressed.
     * <p>
     * The <code>Dialog</code> implementation of this framework method sets
     * this dialog's return code to <code>Window.OK</code>
     * and closes the dialog. Subclasses may override.
     * </p>
     */
    protected void okPressed() {
        launchData.workspaceSelected(TextProcessor.deprocess(getWorkspaceLocation()));
        super.okPressed();
    }

	/**
	 * Get the workspace location from the widget.
	 * @return String
	 */
	protected String getWorkspaceLocation() {
		return text.getText();
	}

    /**
     * Notifies that the cancel button of this dialog has been pressed.
     * <p>
     * The <code>Dialog</code> implementation of this framework method sets
     * this dialog's return code to <code>Window.CANCEL</code>
     * and closes the dialog. Subclasses may override if desired.
     * </p>
     */
    protected void cancelPressed() {
        launchData.workspaceSelected(null);
        super.cancelPressed();
    }

    /**
     * The main area of the dialog is just a row with the current selection
     * information and a drop-down of the most recently used workspaces.
     */
    private void createWorkspaceBrowseRow(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        panel.setLayout(layout);
        panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        panel.setFont(parent.getFont());

        Label label = new Label(panel, SWT.NONE);
        label.setText(IDEWorkbenchMessages.ChooseWorkspaceDialog_workspaceEntryLabel);

        text = new Combo(panel, SWT.BORDER | SWT.LEAD | SWT.DROP_DOWN);
        text.setFocus();        
        text.setLayoutData(new GridData(400, SWT.DEFAULT));
        text.addModifyListener(new ModifyListener(){
        	public void modifyText(ModifyEvent e) {
        		Button okButton = getButton(Window.OK);
        		if(okButton != null && !okButton.isDisposed()) {
        			boolean nonWhitespaceFound = false;
					String characters = getWorkspaceLocation();
					for (int i = 0; !nonWhitespaceFound
							&& i < characters.length(); i++) {
						if (!Character.isWhitespace(characters.charAt(i))) {
							nonWhitespaceFound = true;
						}
					}
        			okButton.setEnabled(nonWhitespaceFound);
        		}
        	}
        });
        setInitialTextValues(text);

        Button browseButton = new Button(panel, SWT.PUSH);
        browseButton.setText(IDEWorkbenchMessages.ChooseWorkspaceDialog_browseLabel);
        setButtonLayoutData(browseButton);
        GridData data = (GridData) browseButton.getLayoutData();
        data.horizontalAlignment = GridData.HORIZONTAL_ALIGN_END;
        browseButton.setLayoutData(data);
        browseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SHEET);
                dialog.setText(IDEWorkbenchMessages.ChooseWorkspaceDialog_directoryBrowserTitle);
                dialog.setMessage(IDEWorkbenchMessages.ChooseWorkspaceDialog_directoryBrowserMessage);
                dialog.setFilterPath(getInitialBrowsePath());
                String dir = dialog.open();
                if (dir != null) {
					text.setText(TextProcessor.process(dir));
				}
            }
        });
    }

    /**
     * Return a string containing the path that is closest to the current
     * selection in the text widget. This starts with the current value and
     * works toward the root until there is a directory for which File.exists
     * returns true. Return the current working dir if the text box does not
     * contain a valid path.
     * 
     * @return closest parent that exists or an empty string
     */
    private String getInitialBrowsePath() {
        File dir = new File(getWorkspaceLocation());
        while (dir != null && !dir.exists()) {
			dir = dir.getParentFile();
		}

        return dir != null ? dir.getAbsolutePath() : System
                .getProperty("user.dir"); //$NON-NLS-1$
    }

	/*
	 * see org.eclipse.jface.Window.getInitialLocation() 
	 */
	protected Point getInitialLocation(Point initialSize) {
		Composite parent = getShell().getParent();
		
		if (!centerOnMonitor || parent == null) {
			return super.getInitialLocation(initialSize);
		}

		Monitor monitor = parent.getMonitor();
		Rectangle monitorBounds = monitor.getClientArea();
		Point centerPoint = Geometry.centerPoint(monitorBounds);

		return new Point(centerPoint.x - (initialSize.x / 2), Math.max(
				monitorBounds.y, Math.min(centerPoint.y
						- (initialSize.y * 2 / 3), monitorBounds.y
						+ monitorBounds.height - initialSize.y)));
	}

    /**
     * The show dialog button allows the user to choose to neven be nagged again.
     */
    private void createShowDialogButton(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setFont(parent.getFont());

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        panel.setLayout(layout);

        GridData data = new GridData(GridData.FILL_BOTH);
        data.verticalAlignment = GridData.END;
        panel.setLayoutData(data);

        Button button = new Button(panel, SWT.CHECK);
        button.setText(IDEWorkbenchMessages.ChooseWorkspaceDialog_useDefaultMessage);
        button.setSelection(!launchData.getShowDialog());
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                launchData.toggleShowDialog();
            }
        });
    }

    private void setInitialTextValues(Combo text) {
        String[] recentWorkspaces = launchData.getRecentWorkspaces();
        for (int i = 0; i < recentWorkspaces.length; ++i) {
			if (recentWorkspaces[i] != null) {
				text.add(recentWorkspaces[i]);
			}
		}

        text.setText(TextProcessor.process((text.getItemCount() > 0 ? text
				.getItem(0) : launchData.getInitialDefault())));
    }
    
	/* (non-Javadoc)
     * @see org.eclipse.jface.window.Dialog#getDialogBoundsSettings()
     * 
     * @since 3.2
     */
	protected IDialogSettings getDialogBoundsSettings() {
		// If we were explicitly instructed to center on the monitor, then
		// do not provide any settings for retrieving a different location or, worse,
		// saving the centered location.
		if (centerOnMonitor) {
			return null;
		}
		
        IDialogSettings settings = IDEWorkbenchPlugin.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(DIALOG_SETTINGS_SECTION);
        if (section == null) {
            section = settings.addNewSection(DIALOG_SETTINGS_SECTION);
        } 
        return section;
	}

}
