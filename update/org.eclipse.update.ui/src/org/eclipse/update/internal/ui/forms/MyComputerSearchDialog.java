/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.internal.ui.forms;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.SWT;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.update.internal.ui.parts.DefaultContentProvider;
import org.eclipse.update.internal.ui.search.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.*;
import java.io.File;
import java.util.Vector;
import org.eclipse.jface.viewers.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
import org.eclipse.update.configuration.LocalSystemInfo;

/**
 * @version 	1.0
 * @author
 */
public class MyComputerSearchDialog extends Dialog {
	MyComputerSearchSettings settings;
	Button okButton;
	Image driveImage;
	CheckboxTableViewer viewer;
	boolean loaded=false;
	SearchObject search;
	
	class DriveContentProvider extends DefaultContentProvider implements IStructuredContentProvider {
		public Object [] getElements(Object input) {
			if (!loaded) initializeDrives();
			return settings.getDriveSettings();
		}
	}
	
	class DriveLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			return ((DriveSearchSettings)obj).getName();
		}
		public Image getImage(Object obj) {
			return driveImage;
		}
	}
	
	/**
	 * Constructor for MyComputerSearchDialog.
	 * @param parentShell
	 */
	public MyComputerSearchDialog(Shell parentShell, SearchObject search) {
		super(parentShell);
		driveImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		settings = new MyComputerSearchSettings(search);
	}
	
	public void buttonPressed(int id) {
		if (id==IDialogConstants.OK_ID) {
			storeSettings();
		}
		super.buttonPressed(id);
	}

	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		okButton =
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(
			parent,
			IDialogConstants.CANCEL_ID,
			IDialogConstants.CANCEL_LABEL,
			false);
	}

	public Control createDialogArea(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		container.setLayout(layout);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label label = new Label(container, SWT.NULL);
		label.setText("Select the root folders that should be searched:");
		
		viewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER);
		viewer.setContentProvider(new DriveContentProvider());
		viewer.setLabelProvider(new DriveLabelProvider());
		viewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				handleDriveChecked((DriveSearchSettings)event.getElement(), event.getChecked());
			}
		});
		viewer.setInput(UpdateUIPlugin.getDefault());
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 200;
		gd.heightHint = 300;
		viewer.getTable().setLayoutData(gd);
		loadSettings();
		return container;
	}
	
	private void loadSettings() {
		Vector selected = new Vector();
		DriveSearchSettings [] drives = settings.getDriveSettings();
		for (int i=0; i<drives.length; i++) {
			if (drives[i].isChecked())
			   selected.add(drives[i]);
		}
		viewer.setCheckedElements(selected.toArray());
	}
	
	private void initializeDrives() {
		File [] drives = MyComputer.getRoots();
		for (int i=0; i<drives.length; i++) {
			// Ensure settings exists
			String label = LocalSystemInfo.getLabel(drives[i]);
			if (label==null)
				label = drives[i].getPath();
			settings.getDriveSettings(label);
		}
	}
	
	private void handleDriveChecked(DriveSearchSettings drive, boolean checked) {
		drive.setChecked(checked);
	}

	private void storeSettings() {
		settings.store();
	}
}