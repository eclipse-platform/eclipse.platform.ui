/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.internal.ui.search;

import java.util.*;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.jface.dialogs.Dialog;

/**
 * @version 	1.0
 * @author
 */
public class MyComputerSearchDialog extends Dialog {
	private static final String KEY_LABEL = "MyComputerSearchDialog.label";
	private MyComputerSearchSettings settings;
	private Button okButton;
	private VolumeLabelProvider volumeLabelProvider;
	private CheckboxTableViewer viewer;
	private boolean loaded = false;
	private SearchObject search;
	private Hashtable driveMap = new Hashtable();

	class DriveContentProvider
		extends DefaultContentProvider
		implements IStructuredContentProvider {
		public Object[] getElements(Object input) {
			if (!loaded)
				initializeDrives();
			return settings.getDriveSettings();
		}
	}

	class DriveLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			IVolume volume = (IVolume)driveMap.get(obj);
			if (volume!=null) return volumeLabelProvider.getText(volume);
			return ((DriveSearchSettings) obj).getName();
		}
		public Image getImage(Object obj) {
			IVolume volume = (IVolume)driveMap.get(obj);
			return volumeLabelProvider.getImage(volume);
		}
	}

	/**
	 * Constructor for MyComputerSearchDialog.
	 * @param parentShell
	 */
	public MyComputerSearchDialog(Shell parentShell, SearchObject search) {
		super(parentShell);
		settings = new MyComputerSearchSettings(search);
		volumeLabelProvider = new VolumeLabelProvider();
	}
	
	public boolean close() {
		boolean value = super.close();
		volumeLabelProvider.dispose();
		return value;
	}
	
	public void buttonPressed(int id) {
		if (id == IDialogConstants.OK_ID) {
			storeSettings();
		}
		super.buttonPressed(id);
	}

	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		okButton =
			createButton(
				parent,
				IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL,
				true);
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
		label.setText(UpdateUIPlugin.getResourceString(KEY_LABEL));

		viewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER);
		viewer.setContentProvider(new DriveContentProvider());
		viewer.setLabelProvider(new DriveLabelProvider());
		viewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				handleDriveChecked(
					(DriveSearchSettings) event.getElement(),
					event.getChecked());
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
		DriveSearchSettings[] drives = settings.getDriveSettings();
		for (int i = 0; i < drives.length; i++) {
			if (drives[i].isChecked())
				selected.add(drives[i]);
		}
		viewer.setCheckedElements(selected.toArray());
	}

	private void initializeDrives() {
		IVolume[] volumes = LocalSystemInfo.getVolumes();
		for (int i = 0; i < volumes.length; i++) {
			// Ensure settings exists
			String label = volumes[i].getLabel();
			if (label == null || "".equals(label))
				label = volumes[i].getFile().getPath();
			else
				label = label + " (" + volumes[i].getFile().getPath() + ")";
			DriveSearchSettings drive = settings.getDriveSettings(label);
			driveMap.put(drive, volumes[i]);
		}
	}

	private void handleDriveChecked(
		DriveSearchSettings drive,
		boolean checked) {
		drive.setChecked(checked);
	}

	private void storeSettings() {
		settings.store();
	}
}