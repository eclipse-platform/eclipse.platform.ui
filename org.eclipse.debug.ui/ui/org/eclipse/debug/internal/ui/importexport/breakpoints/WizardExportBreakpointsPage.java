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

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsViewer;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
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
 * <p>
 * This class provides an internal implementation of a WizardPage, which is used
 * in the Export Breakpoints wizard.
 * </p>
 * <p>
 * The implementation presents the breakpoints to the user as they are shown in
 * their current breakpoint view.
 * </p>
 * <p>
 * Possible extensions would include:
 * <ul>
 * <li> Able to change the views as in the breakpoints view itself
 * <li> Able to reorder groups from within the wizard - easier in the viewer itself though
 * </ul>
 * </p>
 * This class is used by <code>WizardExportBreakpoints</code>
 * 
 * @since 3.2
 */
public class WizardExportBreakpointsPage extends WizardPage implements Listener {

	// widgets
	private Button fOverwriteExistingFilesCheckbox = null;
	private Text fDestinationNameField = null;
	private Button fDestinationBrowseButton = null;
	private IPath fPath = null;
	private EmbeddedBreakpointsViewer fTView = null;
	private IStructuredSelection fSelection = null;
	private Button fSelectAll = null;
	private Button fDeselectAll = null;

	//state constants
	private static final String OVERWRITE_ALL_STATE = "overwrite"; //$NON-NLS-1$
	private static final String DESTINATION_FILE_NAME = "filename"; //$NON-NLS-1$
	
	/**
	 * This is the default constructor. It accepts the name for the tab as a
	 * parameter and an existing selection
	 * 
	 * @param pageName the name of the page
	 */
	public WizardExportBreakpointsPage(String pageName, IStructuredSelection selection) {
		super(pageName, ImportExportMessages.WizardExportBreakpoints_0, null);
		fSelection = selection;
	}// end constructor
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event) {
		Widget source = event.widget;
		if (source == fDestinationBrowseButton) {
			handleDestinationBrowseButtonPressed();
		}// end if
		else if (source == fDestinationNameField) {
			handlePathTextModifiedEvent();
		}// end if
		else if(source == fSelectAll) {
			handleSelectAllPressed();
		}//end if
		else if(source == fDeselectAll) {
			handleDeselectAllPressed();
		}//end if
	}// end handleEvent
	
	/**
	 * Handles the select all button pressed
	 *
	 */
	private void handleSelectAllPressed() {
		BreakpointsViewer viewer = fTView.getViewer();
		viewer.getTree().selectAll();
		viewer.setCheckedElements(((IStructuredSelection)viewer.getSelection()).toArray());
		viewer.setGrayedElements(new Object[] {});
		viewer.getTree().deselectAll();
		setPageComplete(detectPageComplete());
	}//end handleSelectAllPressed
	
	/**
	 * Handles the deselect all button pressed
	 *
	 */
	private void handleDeselectAllPressed() {
		BreakpointsViewer viewer = fTView.getViewer();
		viewer.setCheckedElements(new Object[] {});
		viewer.setGrayedElements(new Object[] {});
		setPageComplete(detectPageComplete());
	}//end handleDeselectAllPressed
	
	/**
	 * This method handles the modified event fomr the path combobox.
	 */
	protected void handlePathTextModifiedEvent() {
		setPageComplete(detectPageComplete());
	}// end handlePathComboModifiedEvent

	/**
	 * Open the SaveAsDialog so the user can save the listing of selected breakpoints
	 */
	protected void handleDestinationBrowseButtonPressed() {
		FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.SAVE);
		dialog.setFilterExtensions(new String[]{"*."+IImportExportConstants.EXTENSION});  //$NON-NLS-1$
		dialog.setText(ImportExportMessages.WizardExportBreakpoints_0);
		String file = dialog.open();
		if(file != null) {
			fPath = new Path(file);
			if (fPath != null) {
				setErrorMessage(null);
				if(fPath.getFileExtension() == null) {
					fPath = fPath.addFileExtension(IImportExportConstants.EXTENSION);  
				}//end if
				else if(!fPath.getFileExtension().equals(IImportExportConstants.EXTENSION)) { 
					fPath = fPath.addFileExtension(IImportExportConstants.EXTENSION); 
				}//end elseif
				fDestinationNameField.setText(fPath.toString());
			}// end if
		}//end if
	}// end handleDestinationBrowseButtonPressed
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		initializeDialogUnits(parent);
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		composite.setFont(font);
		fTView = new EmbeddedBreakpointsViewer(composite, DebugPlugin.getDefault().getBreakpointManager(), fSelection);
		fTView.getViewer().addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				setPageComplete(detectPageComplete());
			}
		});
		fTView.getViewer().setSelection(fSelection);
		createButtonsGroup(composite);
		createDestinationGroup(composite);
		createOptionsGroup(composite);
		setControl(composite); 
		setPageComplete(detectPageComplete());
		restoreWidgetState();
	}// end createControl

	/**
     * Creates the buttons for selecting all or none of the elements.
     *
     * @param parent the parent control
     */
    private void createButtonsGroup(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.makeColumnsEqualWidth = true;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
        fSelectAll = SWTUtil.createPushButton(composite, ImportExportMessages.WizardBreakpointsPage_1, null); 
        fSelectAll.addListener(SWT.Selection, this);
		fDeselectAll = SWTUtil.createPushButton(composite, ImportExportMessages.WizardBreakpointsPage_2, null);
		fDeselectAll.addListener(SWT.Selection, this);
    }
	
	/**
	 * This method is used to determine if the page can be "finished".
	 * 
	 * To be determined "finishable" there must be a save path and there must be
	 * a selection in the tree.
	 * 
	 * @return if the prerequesites of the wizard are met to allow the wizard to complete.
	 */
	private boolean detectPageComplete() {
		boolean emptyFile = fDestinationNameField.getText().trim().equals(""); //$NON-NLS-1$
		if (emptyFile) {
			setMessage(ImportExportMessages.WizardExportBreakpointsPage_0, IMessageProvider.NONE);
			return false;
		}
		int size = fTView.getCheckedElements().size();
		if (size == 0) {
			setMessage(ImportExportMessages.WizardExportBreakpointsPage_1, IMessageProvider.ERROR);
			return false;
		}
		setMessage(ImportExportMessages.WizardBreakpointsPage_4);
		return true;
	}//end detectPageComplete

	/**
	 * Create the Options specification widgets.
	 * 
	 * @param parent the parent to add this 
	 */
	protected void createOptionsGroup(Composite parent) {
		Font font = parent.getFont();
		// Options group
		Group OptionsGroup = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		OptionsGroup.setLayout(layout);
		OptionsGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		OptionsGroup.setText(ImportExportMessages.WizardBreakpointsPage_5);
		OptionsGroup.setFont(parent.getFont());
		fOverwriteExistingFilesCheckbox = new Button(OptionsGroup, SWT.CHECK | SWT.LEFT);
		fOverwriteExistingFilesCheckbox.setText(ImportExportMessages.WizardBreakpointsPage_6);
		fOverwriteExistingFilesCheckbox.setFont(font);
	}// end createOptionsGroup

	/**
	 * Create the export destination specification widgets
	 * 
	 * @param parent org.eclipse.swt.widgets.Composite
	 */
	protected void createDestinationGroup(Composite parent) {
		Font font = parent.getFont();
		// destination specification group
		Composite destinationSelectionGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		destinationSelectionGroup.setLayout(layout);
		destinationSelectionGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
		destinationSelectionGroup.setFont(font);
		Label destinationLabel = new Label(destinationSelectionGroup, SWT.NONE);
		destinationLabel.setText(ImportExportMessages.WizardBreakpointsPage_7);
		destinationLabel.setFont(font);
		fDestinationNameField = new Text(destinationSelectionGroup, SWT.BORDER);
		fDestinationNameField.addListener(SWT.Modify, this);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		fDestinationNameField.setLayoutData(data);
		fDestinationNameField.setFont(font);
		fDestinationBrowseButton = new Button(destinationSelectionGroup, SWT.PUSH);
		fDestinationBrowseButton.setText(ImportExportMessages.WizardBreakpointsPage_8);
		fDestinationBrowseButton.addListener(SWT.Selection, this);
		fDestinationBrowseButton.setFont(font);
		setButtonLayoutData(fDestinationBrowseButton);
	}// end createDestinationGroup

	/**
	 * Save the state of the widgets select, for successive invocations of the wizard
	 */
	private void saveWidgetState() {
		IDialogSettings settings = getDialogSettings();
		if(settings != null) {
			settings.put(OVERWRITE_ALL_STATE, fOverwriteExistingFilesCheckbox.getSelection());
			settings.put(DESTINATION_FILE_NAME, fDestinationNameField.getText().trim());
		}//end if
	}//end save state
	
	/**
	 * Restores the state of the wizard from previous invocations
	 */
	private void restoreWidgetState() {
		IDialogSettings settings = getDialogSettings();
		if(settings != null) {
			fOverwriteExistingFilesCheckbox.setSelection(Boolean.valueOf(settings.get(OVERWRITE_ALL_STATE)).booleanValue());
			String filename = settings.get(DESTINATION_FILE_NAME);
			if (filename != null) {
				fDestinationNameField.setText(filename);
			}
		}//end if
	}//end restore state
	
	/**
	 * The Finish button is clicked on the main wizard
	 * dialog to export the breakpoints, we write them out with all persistnat
	 * information to a simple XML file via the use of XMLMemento.
	 * 
	 * @return if the save operation was successful or not
	 */
	public boolean finish() {
		try {
			//name typed in without using selection box
			if(fPath == null) {
				fPath = new Path(fDestinationNameField.getText().trim());
				if(fPath.getFileExtension() == null) {
					fPath = fPath.addFileExtension(IImportExportConstants.EXTENSION);  
				}//end if
				else if(!fPath.getFileExtension().equals(IImportExportConstants.EXTENSION)) { 
					fPath = fPath.addFileExtension(IImportExportConstants.EXTENSION); 
				}//end elseif
			}//end if
			saveWidgetState();
			if(fPath.toFile().exists()) {
				if(fOverwriteExistingFilesCheckbox.getSelection()) {
					getContainer().run(true, true, new ExportOperation(fTView.getCheckedElements().toArray(), fPath, true));
				}//end if
				else {
					if(MessageDialog.openQuestion(null, ImportExportMessages.WizardBreakpointsPage_12, MessageFormat.format(ImportExportMessages.ImportExportOperations_0, new String[] {fPath.toPortableString()}))) {
						getContainer().run(true, true, new ExportOperation(fTView.getCheckedElements().toArray(), fPath, true));
					}//end if
				}//end else
			}//end if
			else {
				getContainer().run(true, true, new ExportOperation(fTView.getCheckedElements().toArray(), fPath, false));
			}//end else
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
}// end class
