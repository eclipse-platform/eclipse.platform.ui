/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The PreferencesExportDialog is the dialog that handles preference output
 * selection.
 */
public class PreferencesExportDialog extends Dialog {

	CheckboxTreeViewer viewer;

	Combo outputLocationCombo;

	//Minimum dialog width (in dialog units)
	private static final int MIN_DIALOG_WIDTH = 350;

	//Minimum dialog height (in dialog units)
	private static final int MIN_DIALOG_HEIGHT = 150;

	private static String EXPORT_LOCATION_KEY = "PREFERENCES_EXPORT_LOCATION"; //$NON-NLS-1$

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param parentShell
	 */
	public PreferencesExportDialog(Shell parentShell) {
		super(parentShell);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(WorkbenchMessages.getString("PreferencesExportDialog.SelectMessage")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#getShellStyle()
	 */
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
protected Control createDialogArea(Composite parent) {
		Control outerArea = super.createDialogArea(parent);

		createOutputSelectionArea((Composite) outerArea);

		viewer = new CheckboxTreeViewer((Composite) outerArea);

		viewer.setContentProvider(new PreferencesContentProvider());

		viewer.setLabelProvider(new LabelProvider() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object obj) {
				IEclipsePreferences node = (IEclipsePreferences) obj;

				// we are at the scope level
				if (node.parent().parent() == null) {
					if (node.name().equals(InstanceScope.SCOPE))
						return WorkbenchMessages.getString("PreferencesExportDialog.WorkspaceScope"); //$NON-NLS-1$
					else if (node.name().equals(ConfigurationScope.SCOPE))
						return WorkbenchMessages.getString("PreferencesExportDialog.ConfigurationScope"); //$NON-NLS-1$
					return node.name();
				}

				// we are at the plug-in level, try and look up a reasonable name
				String result = node.name();
				Plugin plugin = Platform.getPlugin(result);
				if (plugin != null)
					return WorkbenchMessages.format("PreferencesExportDialog.PluginLabel", //$NON-NLS-1$
							new String [] {plugin.getDescriptor().getLabel(), node.name()});
				return node.name();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
			 */
			public Image getImage(Object obj) {
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
			}
		});

		viewer.setSorter(new ViewerSorter());
		viewer.setInput(Platform.getPreferencesService().getRootNode());

		GridData data = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);

		viewer.getControl().setLayoutData(data);
		return outerArea;

	}
	/**
	 * Create the output file location.
	 * 
	 * @param outerArea
	 */
	private void createOutputSelectionArea(Composite outerArea) {

		Composite outputArea = new Composite(outerArea, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = false;

		outputArea.setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		outputArea.setLayoutData(data);

		Label titleLabel = new Label(outputArea, SWT.NONE);
		titleLabel.setText(WorkbenchMessages.getString("ExportWizard.selectDestination"));//$NON-NLS-1$

		IDialogSettings settings = WorkbenchPlugin.getDefault().getDialogSettings();
		String[] locations = settings.getArray(EXPORT_LOCATION_KEY);

		outputLocationCombo = new Combo(outputArea, SWT.NONE);

		if (locations != null) {
			outputLocationCombo.setItems(locations);
			outputLocationCombo.select(0);
		}

		outputLocationCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// destination browse button
		Button destinationBrowseButton = new Button(outputArea, SWT.PUSH);
		destinationBrowseButton.setText(WorkbenchMessages.getString("EditorSelection.browse")); //$NON-NLS-1$
		destinationBrowseButton.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
				dialog.setText(WorkbenchMessages
						.getString("PreferencesExportDialog.ExportDialogTitle")); //$NON-NLS-1$
				dialog
						.setFilterExtensions(PreferenceImportExportFileSelectionPage.DIALOG_PREFERENCE_EXTENSIONS);
				String selectedDirectoryName = dialog.open();

				if (selectedDirectoryName != null) {
					outputLocationCombo.setText(selectedDirectoryName);
				}
			}
		});
		setButtonLayoutData(destinationBrowseButton);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
	protected Point getInitialSize() {
		Point shellSize = super.getInitialSize();
		return new Point(Math.max(convertHorizontalDLUsToPixels(MIN_DIALOG_WIDTH), shellSize.x),
				Math.max(convertVerticalDLUsToPixels(MIN_DIALOG_HEIGHT), shellSize.y));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {

		Object[] selections = viewer.getCheckedElements();
		IEclipsePreferences[] preferences = new IEclipsePreferences[selections.length];

		System.arraycopy(selections, 0, preferences, 0, selections.length);

		String outputPath = getOutputFileName();
		exportPreferences(new Path(outputPath), preferences);

		String[] items = outputLocationCombo.getItems();
		boolean needToAddOutput = true;

		for (int i = 0; i < items.length; i++) {
			if (items[i].equals(outputPath)) {
				needToAddOutput = false;
				break;
			}
		}

		if (needToAddOutput) {
			String[] newValues = new String[items.length + 1];
			newValues[0] = outputPath;
			System.arraycopy(items, 0, newValues, 1, items.length);
			items = newValues;
		}

		WorkbenchPlugin.getDefault().getDialogSettings().put(EXPORT_LOCATION_KEY, items);

		super.okPressed();
	}

	/**
	 * Get the file name to use. Add the .epf extension if it is not there.
	 * 
	 * @return String
	 */
	private String getOutputFileName() {

		String fileName = new File(outputLocationCombo.getText()).getPath();
		if (fileName.lastIndexOf(".") == -1) { //$NON-NLS-1$
			fileName += AbstractPreferenceImportExportPage.PREFERENCE_EXT;
		}
		return fileName;
	}

	/**
	 * Export all of the selected preferences to path.
	 * 
	 * @param path
	 * @param preferences
	 */
	private void exportPreferences(IPath path, IEclipsePreferences[] preferences) {
		File file = path.toFile();

		if (file.exists())
			file.delete();

		File parent = file.getParentFile();
		if (parent != null)
			file.getParentFile().mkdirs();
		IPreferencesService service = Platform.getPreferencesService();
		OutputStream output = null;
		try {
			output = new BufferedOutputStream(new FileOutputStream(file));
			for (int i = 0; i < preferences.length; i++) {
				IEclipsePreferences node = preferences[i];
				service.exportPreferences(node, output, null);
			}

		} catch (FileNotFoundException exception) {
			openErrorDialog(new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR,
					exception.getMessage(), exception));
		} catch (CoreException exception) {
			openErrorDialog(exception.getStatus());
		} finally {
			if (output != null)
				try {
					output.close();
				} catch (IOException e) {
					// ignore
				}
		}
	}

	/**
	 * Open an error dialog on the supplied status.
	 * 
	 * @param status
	 */
	private void openErrorDialog(IStatus status) {
		ErrorDialog.openError(getShell(), WorkbenchMessages
				.getString("PreferencesExportDialog.ErrorDialogTitle"), //$NON-NLS-1$
				WorkbenchMessages.getString("PreferencesExportDialog.ErrorDialogMessage"), status); //$NON-NLS-1$
	}

}