package org.eclipse.ui.externaltools.internal.ant.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ant.core.TargetInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.ui.AntTargetContentProvider;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.model.ToolUtil;
import org.eclipse.ui.externaltools.variable.ExpandVariableContext;

public class AntTargetsTab extends AbstractLaunchConfigurationTab {

	private Button runDefaultTargetButton;
	private Button upButton;
	private Button downButton;
	
	private Label descriptionLabel;
	private Label executeLabel;
	private ListViewer executeTargetsList;
	private Button showSubTargetsButton;
	
	private TargetInfo defaultTarget = null;
	private Text descriptionField;
	
	private String location= null;
	private Button addButton;
	
	private Map targetNamesToTargetInfos = new HashMap();
	private Button removeButton;
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		GridLayout layout = new GridLayout();
		mainComposite.setLayout(layout);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayoutData(gridData);
		createVerticalSpacer(mainComposite, 1);
										
		Composite upperComposite = new Composite(mainComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		upperComposite.setLayout(layout);
		upperComposite.setLayoutData(gridData);		
		
		createRunDefaultTargetButton(upperComposite);
		
		Composite middleComposite = new Composite(mainComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 4;
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		middleComposite.setLayout(layout);
		middleComposite.setLayoutData(gridData);
		
		createExecuteTargetsList(middleComposite);
		createButtonComposite(middleComposite);
		
		Composite lowerComposite = new Composite(mainComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		lowerComposite.setLayout(layout);
		lowerComposite.setLayoutData(gridData);		
		
		createDescriptionField(lowerComposite);
		//createShowSubTargetsButton(lowerComposite);
		
		allowSelectTargets(!runDefaultTargetButton.getSelection());
		
	}
	
	/*
	 * Enables all the appropriate controls.
	 */
	private void allowSelectTargets(boolean enabled) {
		if (! enabled) {
			//deselectAll();
			if (defaultTarget != null && defaultTarget.getDescription() != null) {
				descriptionField.setText(defaultTarget.getDescription());	
			}
		} else {
			descriptionField.setText(""); //$NON-NLS-1$
		}
		
	 	executeLabel.setEnabled(enabled);
	 	executeTargetsList.getControl().setEnabled(enabled);
	 	if (enabled && executeTargetsList.getElementAt(0) != null) {
	 		TargetInfo[] infos= getTargets();
	 		/*String[] names= new String[infos.length];
	 		for (int i = 0; i < infos.length; i++) {
				TargetInfo info = infos[i];
				names[i]= info.getName();
			}*/
	 		executeTargetsList.setInput(infos);
	 	}
	 	descriptionLabel.setEnabled(enabled);
	 	descriptionField.setEnabled(enabled);
	 	//showSubTargetsButton.setEnabled(enabled);
	 	updateButtonEnablement();
	}
	
	/*
	 * Creates a button bank containing the buttons for moving
	 * targets in the active list upButton and downButton.
	 */
	private void createButtonComposite(Composite parent) {
		Composite buttonComposite = new Composite(parent, SWT.NONE);

		GridData gridData = new GridData();
		buttonComposite.setLayoutData(gridData);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		buttonComposite.setLayout(layout);

		new Label(buttonComposite, SWT.NONE);
		
		addButton = createPushButton(buttonComposite, "Add", null);
		addButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					addTargets();
					updateLaunchConfigurationDialog();
				}
			});
			
		removeButton = createPushButton(buttonComposite, "Remove", null);
		removeButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					removeTargets();
					updateLaunchConfigurationDialog();
				}
			});
		
		upButton = createPushButton(buttonComposite, "Up", null);
		upButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					handleMove(-1);
					updateLaunchConfigurationDialog();
				}
			});
				
		downButton = createPushButton(buttonComposite, "Down", null);
		downButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					handleMove(1);
					updateLaunchConfigurationDialog();
				}
			});
	}
	
	private TargetInfo[] getTargets() {
		TargetInfo[] targets= new TargetInfo[0];
		MultiStatus status = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
			String expandedLocation = ToolUtil.expandFileLocation(location, ExpandVariableContext.EMPTY_CONTEXT, status);
			if (expandedLocation != null && status.isOK()) {
				try {
					targets = AntUtil.getTargets(expandedLocation);
				} catch (CoreException ce) {
					ExternalToolsPlugin.getDefault().log("Problems retrieving Ant Targets", ce);
					return targets;
				}
				java.util.List targetNameList = new ArrayList();
				java.util.List subTargets = new ArrayList();
				for (int i=0; i < targets.length; i++) {
					if (! AntUtil.isInternalTarget(targets[i])) {
						// Add the target to the map of target names to target infos.
						targetNamesToTargetInfos.put(targets[i].getName(), targets[i]);

						if (targets[i].isDefault()) {
							defaultTarget = targets[i];
							runDefaultTargetButton.setText(MessageFormat.format("Run default target ({0})", new Object[] {targets[i].getName()})); //NON-NLS-1$
						}
						
						if (AntUtil.isSubTarget(targets[i])) {
							subTargets.add(targets[i].getName());
						} else {
							targetNameList.add(targets[i].getName());
						}
					}
				}
				//if (showSubTargetsButton.getSelection()) {
					targetNameList.addAll(subTargets);
				//}
			}
			
			Arrays.sort(targets, new Comparator() {
			public int compare(Object o1, Object o2) {
				TargetInfo t1= (TargetInfo)o1;
				TargetInfo t2= (TargetInfo)o2;
				return t1.getName().compareTo(t2.getName());
			}
			public boolean equals(Object obj) {
				return false;
			}
		});
			return targets;
	}
				
	private void addTargets() {
		
		TargetInfo[] targets= getTargets();
		AntTargetLabelProvider labelProvider= new AntTargetLabelProvider();
		labelProvider.setDefaultTargetName(defaultTarget.getName());					
		SelectionDialog dialog= new ListSelectionDialog(getShell(), targets, new AntTargetContentProvider(), labelProvider, "Select Ant Targets:");
		if (dialog.open() == SelectionDialog.OK) {
			Object[] results = dialog.getResult();
			for (int i = 0; i < results.length; i++) {
				TargetInfo info = (TargetInfo)results[i];
				executeTargetsList.add(info.getName());
			}
		}
	}
	
	private void removeTargets() {
		String[] targets = executeTargetsList.getList().getSelection();
		for (int i=0; i < targets.length; i++) {
			executeTargetsList.remove(targets[i]);
		}
	}
	
	private void handleMove(int direction) {
		int index = executeTargetsList.getList().getSelectionIndex();
		if (index < 0) {
			return;
		}
		
		String target = executeTargetsList.getList().getItem(index);
		executeTargetsList.getList().remove(index);
		executeTargetsList.getList().add(target, index + direction);
		executeTargetsList.getList().setSelection(index + direction);
		
		updateButtonEnablement();
	}


	/*
	 * Creates the list of targets that will be used when the tool is run.
	 */
	private void createExecuteTargetsList(Composite parent) {
		Composite listComposite = new Composite(parent, SWT.NONE);
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		listComposite.setLayoutData(gridData);
		
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		listComposite.setLayout(layout);
				
		executeLabel = new Label(listComposite, SWT.LEFT);
		executeLabel.setText("Targets to execute:"); 
		
		executeTargetsList = new ListViewer(listComposite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		executeTargetsList.setContentProvider(new AntTargetContentProvider());
		AntTargetLabelProvider labelProvider= new AntTargetLabelProvider();
		if (defaultTarget != null) {
			labelProvider.setDefaultTargetName(defaultTarget.getName());
		}
		executeTargetsList.setLabelProvider(labelProvider);
		gridData = new GridData(GridData.FILL_BOTH);
		executeTargetsList.getControl().setLayoutData(gridData);
		
		executeTargetsList.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)executeTargetsList.getSelection();
				if (selection.isEmpty()) {
					targetsSelected((TargetInfo)selection.getFirstElement());
				} else {
					//deselectAll();
				}
			}
		});
	}
	
	/*
	 * A target was selected.
	 */
	private void targetsSelected(TargetInfo target) {
		updateButtonEnablement();
		descriptionField.setText(target.getDescription());
	}
	
	/*
	 * Shows the descriptionField of the given target in the
	 * descriptionField field.
	 */
	private void showDescription(String targetName) {
		descriptionField.setText("");
		if (targetName == null) {
			return;
		}
		TargetInfo targetInfo = (TargetInfo) targetNamesToTargetInfos.get(targetName);
		if (targetInfo != null && targetInfo.getDescription() != null) {
			descriptionField.setText(targetInfo.getDescription());
		}
	}
	
	/*
	 * Creates the text field which displays the descriptionField of the selected target.
	 */
	private void createDescriptionField(Composite parent) {
		descriptionLabel = new Label(parent, SWT.NONE);
		descriptionLabel.setText("Target description:");
		
		descriptionField = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		descriptionField.setLayoutData(data);
	}
	
	/*
	 * Creates the checkbox button for the
	 * "Show sub-targets" preference.
	 */
	private void createShowSubTargetsButton(Composite parent) {
		showSubTargetsButton = new Button(parent, SWT.CHECK);
		showSubTargetsButton.setText("Show sub-targets");
		showSubTargetsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (showSubTargetsButton.getSelection()) {
					//showSubTargets();
				} else {
					//hideSubTargets();
				}
			}			
		});
		showSubTargetsButton.setSelection(false);			
	}

	/*
	 * Creates the checkbox button for the
	 * "Run default target" preference.
	 */
	private void createRunDefaultTargetButton(Composite parent) {
		runDefaultTargetButton = new Button(parent, SWT.CHECK);
		// The label that is applied if the default target is unknown
		runDefaultTargetButton.setText("Run default target");
		runDefaultTargetButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				allowSelectTargets(!runDefaultTargetButton.getSelection());
				updateLaunchConfigurationDialog();
			}			
		});
		runDefaultTargetButton.setSelection(true);
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		String targets= null;
		try {
			targets= configuration.getAttribute(IExternalToolConstants.ATTR_ANT_TARGETS, (String)null);
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log("Error reading configuration", ce);
		}
		
		location= null;
		try {
			location= configuration.getAttribute(IExternalToolConstants.ATTR_LOCATION, "");
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log("Error reading configuration", ce);
		}
		
		runDefaultTargetButton.setSelection(targets == null);
		allowSelectTargets(targets != null);
		String[] targetNames= AntUtil.parseRunTargets(targets);
		TargetInfo[] infos= getTargets();
		TargetInfo[] targetInfos= new TargetInfo[targetNames.length];
		int found= 0;
		for (int i = 0; i < infos.length; i++) {
			TargetInfo info = infos[i];
			for (int j = 0; j < targetNames.length; j++) {
				String name = targetNames[j];
				if (info.getName().equals(name)) {
					targetInfos[j]= info;
					found++;
				}
			}
			if (found == targetNames.length) {
				break;
			}
		}
		if (targets != null) {
			executeTargetsList.setInput(targetInfos);
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (!runDefaultTargetButton.getSelection()) {
			configuration.setAttribute(IExternalToolConstants.ATTR_ANT_TARGETS, AntUtil.combineRunTargets(executeTargetsList.getList().getItems()));
		} else {
			configuration.setAttribute(IExternalToolConstants.ATTR_ANT_TARGETS, (String)null);
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return "Targets";
	}
	
	/*
	 * Updates the enabled state of the upButton and downButton buttons based
	 * on the current list selection.
	 */
	private void updateButtonEnablement() {
		if (executeTargetsList.getList().isEnabled() == false) {
			upButton.setEnabled(false);
			downButton.setEnabled(false);
			removeButton.setEnabled(false);
			addButton.setEnabled(false);	
			return;
		}
		
		addButton.setEnabled(true);
		// Disable upButton and downButton buttons if there is not one
		// target selected in the active list.
		if (executeTargetsList.getSelection().isEmpty()) {
			upButton.setEnabled(false);
			downButton.setEnabled(false);
			removeButton.setEnabled(false);
			return;	
		}
		
		removeButton.setEnabled(true);
		int index = executeTargetsList.getList().getSelectionIndex();
		if (index > 0) {
			upButton.setEnabled(true);
		} else {
			upButton.setEnabled(false);
		}
	
		if (index >= 0 && index < executeTargetsList.getList().getItemCount() - 1) {
			downButton.setEnabled(true);
		} else {
			downButton.setEnabled(false);		
		}
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return ExternalToolsImages.getImage(IExternalToolConstants.IMG_TAB_ANT_TARGETS);
	}
}
