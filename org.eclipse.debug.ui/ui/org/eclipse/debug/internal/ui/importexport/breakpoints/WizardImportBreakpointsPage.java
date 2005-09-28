/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.importexport.breakpoints;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * The import breakpoints wizard page.
 * 
 * This class is used in <code>WizardImportBreakpoints</code>.
 * 
 * @since 3.2
 */
public class WizardImportBreakpointsPage extends WizardPage implements Listener {
	
	//widgets
	private Button fAutoRemoveDuplicates = null;
	private Button fAutoCreateWorkingSets = null;
	private Text fFileNameField = null;
	private Button fBrowseForFileButton = null;
	private File fImportFile = null;
	
//	state constants
	private static final String REMOVE_DUPS = "overwrite"; //$NON-NLS-1$
	private static final String CREATE_WORKING_SETS = "createws"; //$NON-NLS-1$
	private static final String SOURCE_FILE_NAME = "filename"; //$NON-NLS-1$
	
	/**
	 * This is the default constructor. It accepts the name for the tab as a
	 * parameter
	 * 
	 * @param pageName the name of the page
	 */
	public WizardImportBreakpointsPage(String pageName) {
		super(pageName, ImportExportMessages.WizardImportBreakpointsPage_0, null);
	}// end constructor

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event) {
		Widget source = event.widget;
		if(source == fBrowseForFileButton) {
			handleBrowseForFileButtonPressed();
		} else if (source == fFileNameField) {
			String fileName = fFileNameField.getText();
			fImportFile = new File(fileName);			
		}//end if
		setPageComplete(detectPageComplete());
	}// end handleEvent

	/**
	 * This method handles the fBrowseForFileButton being pressed.
	 */
	protected void handleBrowseForFileButtonPressed() {
		FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.OPEN);
		dialog.setFilterExtensions(new String[]{"*."+IImportExportConstants.EXTENSION});  //$NON-NLS-1$
		String file = dialog.open();
		if(file != null) {
			fFileNameField.setText(file);
		}// end if
	}//end handleBrowseForFileButtonPressed
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		composite.setFont(parent.getFont());
		createDestinationGroup(composite);
		createOptionsGroup(composite);
		setControl(composite);
		restoreWidgetState();
		setPageComplete(detectPageComplete());
	}// end createControl

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getImage()
	 */
	public Image getImage() {
		return DebugUITools.getImage(IInternalDebugUIConstants.IMG_WIZBAN_IMPORT_BREAKPOINTS);
	}
	
	/**
	 * This method is used to determine if the page can be "finished".
	 * To be determined "finishable" there must be an import path.
	 * 
	 * @return if the prerequesites of the wizard are met to allow the wizard to complete.
	 */
	private boolean detectPageComplete() {
		String fileName = fFileNameField.getText().trim();
		if (fileName.equals("")) {//$NON-NLS-1$
			setMessage(ImportExportMessages.WizardImportBreakpointsPage_6);
			return false;
		}
		fImportFile = new File(fileName);
		if (!fImportFile.exists()) {
			setMessage(MessageFormat.format(ImportExportMessages.WizardImportBreakpointsPage_1, new String[]{fileName}), ERROR);
			return false;
		}//end if
		
		setMessage(ImportExportMessages.WizardImportBreakpointsPage_2); 
		return true;
	}//end detectPageComplete

	/**
	 * Create the options specification widgets.
	 * 
	 * @param parent the parent composite to add this one to
	 */
	protected void createOptionsGroup(Composite parent) {
		Font font = parent.getFont();
		// options group
		Group optionsGroup = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		optionsGroup.setLayout(layout);
		optionsGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		optionsGroup.setText(ImportExportMessages.WizardBreakpointsPage_5);
		optionsGroup.setFont(parent.getFont());
		fAutoRemoveDuplicates = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		fAutoRemoveDuplicates.setText(ImportExportMessages.WizardImportBreakpointsPage_3);
		fAutoRemoveDuplicates.setFont(font);
		fAutoCreateWorkingSets = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		fAutoCreateWorkingSets.setText(ImportExportMessages.WizardImportBreakpointsPage_5);
		fAutoCreateWorkingSets.setFont(font);
	}// end createOptionsGroup
	
	/**
	 * Create the export destination specification widgets
	 * 
	 * @param parent the parent composite to add this one to
	 */
	protected void createDestinationGroup(Composite parent) {
		Font font = parent.getFont();
		Composite destinationSelectionGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		destinationSelectionGroup.setLayout(layout);
		destinationSelectionGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
		destinationSelectionGroup.setFont(font);
		Label destinationLabel = new Label(destinationSelectionGroup, SWT.NONE);
		destinationLabel.setText(ImportExportMessages.WizardImportBreakpointsPage_4);
		destinationLabel.setFont(font);

		// file name entry field
		fFileNameField = new Text(destinationSelectionGroup, SWT.BORDER);
		fFileNameField.addListener(SWT.Modify, this);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		fFileNameField.setLayoutData(data);
		fFileNameField.setFont(font);

		// destination browse button
		fBrowseForFileButton = SWTUtil.createPushButton(destinationSelectionGroup, ImportExportMessages.WizardBreakpointsPage_8, null);
		fBrowseForFileButton.addListener(SWT.Selection, this);
	}// end createDestinationGroup
	
	/**
	 * Save the state of the widgets select, for successive invocations of the wizard
	 */
	private void saveWidgetState() {
		IDialogSettings settings = getDialogSettings();
		if(settings != null) {
			settings.put(REMOVE_DUPS, fAutoRemoveDuplicates.getSelection());
			settings.put(CREATE_WORKING_SETS, fAutoCreateWorkingSets.getSelection());
			settings.put(SOURCE_FILE_NAME, fFileNameField.getText().trim());
		}//end if
	}//end save state
	
	/**
	 * Restores the state of the wizard from previous invocations
	 */
	private void restoreWidgetState() {
		IDialogSettings settings = getDialogSettings();
		if(settings != null) {
			fAutoRemoveDuplicates.setSelection(Boolean.valueOf(settings.get(REMOVE_DUPS)).booleanValue());
			fAutoCreateWorkingSets.setSelection(Boolean.valueOf(settings.get(CREATE_WORKING_SETS)).booleanValue());
			String fileName = settings.get(SOURCE_FILE_NAME);
			if (fileName != null) {
				fFileNameField.setText(fileName);
			}
		}//end if
	}//end restore state
	
	/**
	 * <p>
	 * This method is called when the Finish button is click on the main wizard
	 * dialog To import the breakpoints, we read then from the tree
	 * and add them into the BreakpointManager
	 * </p>
	 * @return if the import operation was successful or not
	 */
	public boolean finish() {	
		try {
			saveWidgetState();
			getContainer().run(true, true, new ImportOperation(fImportFile, fAutoRemoveDuplicates.getSelection(), fAutoCreateWorkingSets.getSelection()));
		}// end try
		catch (InterruptedException e) {
			DebugPlugin.log(e);
			return false;
		}// end catch
		catch (InvocationTargetException e) {
			DebugPlugin.log(e);
			return false;
		}//end catch
		return true;
	}// end finish
	
}//end class
