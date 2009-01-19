/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.ImportBreakpointsOperation;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;

import com.ibm.icu.text.MessageFormat;

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
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event) {
		Widget source = event.widget;
		if(source == fBrowseForFileButton) {
			handleBrowseForFileButtonPressed();
		}
		setPageComplete(detectPageComplete());
	}

	/**
	 * This method handles the fBrowseForFileButton being pressed.
	 */
	protected void handleBrowseForFileButtonPressed() {
		FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.OPEN);
		dialog.setFilterExtensions(new String[]{"*."+IImportExportConstants.EXTENSION});  //$NON-NLS-1$
		String file = dialog.open();
		if(file != null) {
			fFileNameField.setText(file);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		Composite composite = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_BOTH);
		createDestinationGroup(composite);
		createOptionsGroup(composite);
		setControl(composite);
		restoreWidgetState();
		setPageComplete(detectPageComplete());
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IDebugHelpContextIds.IMPORT_BREAKPOINTS_WIZARD_PAGE);
	}

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
	 * @return if the prerequisites of the wizard are met to allow the wizard to complete.
	 */
	private boolean detectPageComplete() {
		String fileName = fFileNameField.getText().trim();
		if (fileName.equals(IInternalDebugCoreConstants.EMPTY_STRING)) {
			setMessage(ImportExportMessages.WizardImportBreakpointsPage_6);
			return false;
		}
		File file = new File(fileName);
		if (!file.exists() || file.isDirectory()) {
			setMessage(MessageFormat.format(ImportExportMessages.WizardImportBreakpointsPage_1, new String[]{fileName}), ERROR);
			return false;
		}
		
		setMessage(ImportExportMessages.WizardImportBreakpointsPage_2); 
		return true;
	}

	/**
	 * Create the options specification widgets.
	 * 
	 * @param parent the parent composite to add this one to
	 */
	protected void createOptionsGroup(Composite parent) {
		fAutoRemoveDuplicates = SWTFactory.createCheckButton(parent, ImportExportMessages.WizardImportBreakpointsPage_3, null, false, 1);
		fAutoCreateWorkingSets = SWTFactory.createCheckButton(parent, ImportExportMessages.WizardImportBreakpointsPage_5, null, false, 1);
	}
	
	/**
	 * Create the export destination specification widgets
	 * 
	 * @param parent the parent composite to add this one to
	 */
	protected void createDestinationGroup(Composite parent) {
		Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 3, 1, GridData.FILL_HORIZONTAL, 0, 10);
		SWTFactory.createLabel(comp, ImportExportMessages.WizardImportBreakpointsPage_4, 1);

		// file name entry field
		fFileNameField = SWTFactory.createText(comp, SWT.BORDER | SWT.SINGLE, 1, GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		fFileNameField.addListener(SWT.Modify, this);
		
		// destination browse button
		fBrowseForFileButton = SWTFactory.createPushButton(comp, ImportExportMessages.WizardBreakpointsPage_8, null);
		fBrowseForFileButton.addListener(SWT.Selection, this);
	}
	
	/**
	 * Save the state of the widgets select, for successive invocations of the wizard
	 */
	private void saveWidgetState() {
		IDialogSettings settings = getDialogSettings();
		if(settings != null) {
			settings.put(REMOVE_DUPS, fAutoRemoveDuplicates.getSelection());
			settings.put(CREATE_WORKING_SETS, fAutoCreateWorkingSets.getSelection());
			settings.put(SOURCE_FILE_NAME, fFileNameField.getText().trim());
		}
	}
	
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
		}
	}
	
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
			getContainer().run(false, 
					true, 
					new ImportBreakpointsOperation(
							fFileNameField.getText().trim(), 
							fAutoRemoveDuplicates.getSelection(), 
							fAutoCreateWorkingSets.getSelection()));
		}
		catch (InterruptedException e) {
			DebugPlugin.log(e);
			return false;
		}
		catch (InvocationTargetException e) {
			DebugPlugin.log(e);
			return false;
		}
		return true;
	}
	
}
