package org.eclipse.toolscript.ui.internal;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.tools.ant.Project;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.toolscript.core.internal.ToolScriptPlugin;

/**
 * First page of the run Ant wizard. Allows the user to pick
 * the targets, supply extra arguments, and decide to show
 * output to the console.
 */
public class AntLaunchWizardPage extends WizardPage {
	private static final int SIZING_SELECTION_WIDGET_HEIGHT = 200;
	private static final int SIZING_SELECTION_WIDGET_WIDTH = 200;

	private Project project;
	private String initialTargets[];
	private String initialArguments;
	private boolean initialDisplayLog = true;
	private ArrayList selectedTargets = new ArrayList();
	
	private CheckboxTableViewer listViewer;
	private AntTargetLabelProvider labelProvider = new AntTargetLabelProvider();
	private Button showLog;
	private Text argumentsField;

	public AntLaunchWizardPage(Project project) {
		super("AntScriptPage"); //$NON-NLS-1$;
		this.project = project;
		setTitle(ToolScriptMessages.getString("AntLaunchWizard.dialogTitle")); //$NON-NLS-1$;
		setDescription(ToolScriptMessages.getString("AntLaunchWizard.dialogDescription")); //$NON-NLS-1$;
		setImageDescriptor(getImageDescriptor("icons/full/wizban/ant_wiz.gif")); //$NON-NLS-1$;
	}
	
	/**
	 * Returns the image descriptor for the banner
	 */
	private ImageDescriptor getImageDescriptor(String relativePath) {
		try {
			URL installURL = ToolScriptPlugin.getDefault().getDescriptor().getInstallURL();
			URL url = new URL(installURL, relativePath);
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
/*	public void checkStateChanged(CheckStateChangedEvent e) {
		Target checkedTarget = (Target) e.getElement();
		if (e.getChecked())
			selectedTargets.addElement(checkedTarget);
		else
			selectedTargets.removeElement(checkedTarget);

		labelProvider.setSelectedTargets(selectedTargets);
		listViewer.refresh();

		// need to tell the wizard container to refresh his buttons
		getWizard().getContainer().updateButtons();
	}
*/	
	/* (non-Javadoc)
	 * Method declared on IWizardPage.
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

/*		new Label(composite, SWT.NONE).setText(Policy.bind("wizard.availableTargetsLabel"));

		listViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		listViewer.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object o1, Object o2) {
				return ((Target) o1).getName().compareTo(((Target) o2).getName());
			}
		});

		listViewer.getTable().setLayoutData(data);
		if (project.getDefaultTarget() != null)
			labelProvider.setDefaultTargetName(project.getDefaultTarget());
		listViewer.setLabelProvider(labelProvider);
		listViewer.setContentProvider(TargetsListContentProvider.getInstance());
		listViewer.setInput(project);

		new Label(composite, SWT.NONE).setText(Policy.bind("wizard.argumentsLabel"));
		argumentsField = new Text(composite, SWT.BORDER);
		argumentsField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// adds a listener to tell the wizard when it can tell its container to refresh the buttons
		argumentsField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				AntLaunchWizardPage.this.getWizard().getContainer().updateButtons();
			}
		});

		showLog = new Button(composite, SWT.CHECK);
		showLog.setText(Policy.bind("wizard.displayLogLabel"));
		showLog.setSelection(initialDisplayLog);
		restorePreviousSelectedTargets();
		listViewer.addCheckStateListener(this);
		listViewer.refresh();

		if (initialArguments != null)
			argumentsField.setText(initialArguments);
		argumentsField.setFocus();
*/		setControl(composite);
	}
	
	/**
	 * Returns the arguments that the user has entered
	 * to run the ant file.
	 * 
	 * @return String the arguments
	 */
	public String getArguments() {
		return argumentsField.getText();
	}
	
	/**
	 * Returns the targets selected by the user
	 */
	public ArrayList getSelectedTargets() {
		return selectedTargets;
	}
	
	private void restorePreviousSelectedTargets() {
/*		if (initialTargetSelections == null)
			return;
		Vector result = new Vector();
		Object availableTargets[] = TargetsListContentProvider.getInstance().getElements(project);
		if (initialTargetSelections.length == 0) {
			boolean found = false;
			for (int j = 0; !found && (j < availableTargets.length); j++) {
				if (((Target) availableTargets[j]).getName().equals(project.getDefaultTarget())) {
					result.addElement(availableTargets[j]);
					listViewer.setChecked(availableTargets[j], true);
					found = true;
				}
			}
		} else {
			for (int i = 0; i < initialTargetSelections.length; i++) {
				String currentTargetName = initialTargetSelections[i];
				for (int j = 0; j < availableTargets.length; j++) {
					if (((Target) availableTargets[j]).getName().equals(currentTargetName)) {
						result.addElement(availableTargets[j]);
						listViewer.setChecked(availableTargets[j], true);
						continue;
					}
				}
			}
		}

		selectedTargets = result;
		labelProvider.setSelectedTargets(selectedTargets);
*/	}
	
	/**
	 * Sets the initial contents of the target list field.
	 * Ignored if controls already created.
	 */
	public void setInitialTargets(String value[]) {
		initialTargets = value;
	}
	
	/**
	 * Sets the initial contents of the arguments text field.
	 * Ignored if controls already created.
	 */
	public void setInitialArguments(String value) {
		initialArguments = value;
	}
	
	/**
	 * Sets the initial contents of the display to log option field.
	 * Ignored if controls already created.
	 */
	public void setInitialDisplayLog(boolean value) {
		initialDisplayLog = value;
	}
	
	/**
	 * Returns whether the users wants messages from running
	 * the script displayed in the console
	 */
	public boolean shouldLogMessages() {
		return showLog.getSelection();
	}
}