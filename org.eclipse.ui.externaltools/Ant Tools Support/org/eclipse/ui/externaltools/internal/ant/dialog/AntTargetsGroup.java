package org.eclipse.ui.externaltools.internal.ant.dialog;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ant.core.TargetInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.externaltools.group.ExternalToolGroup;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.model.ToolUtil;
import org.eclipse.ui.externaltools.variable.ExpandVariableContext;

/**
 * Group for selecting the targets of an Ant build tool.
 */
public class AntTargetsGroup extends ExternalToolGroup {
	private static final int DESCRIPTION_FIELD_HEIGHT = 3;

	private String fileLocation = null;
	private TargetInfo defaultTarget = null;
	private Map mapTargetNamesToTargetInfos = new HashMap();
	private ArrayList subTargets = new ArrayList();

	private Button runDefaultTargetButton;
	private Button showSubTargetsButton;
	private List availableTargetsList;
	private List activeTargetsList;
	private Button addButton;
	private Button removeButton;
	private Button addAll;
	private Button removeAll;
	private Button upButton;
	private Button downButton;
	private Text descriptionField;
	private Label descriptionLabel;
	private Label availableLabel;
	private Label activeLabel;

	/**
	 * Creates the group
	 */
	public AntTargetsGroup() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared on ExternalToolGroup.
	 */
	protected Control createGroupContents(Composite parent, ExternalTool tool) {
		// main composite
		Composite mainComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		mainComposite.setLayout(layout);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayoutData(gridData);
										
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
		
		createAvailableTargetsList(middleComposite);
		createAddRemoveComposite(middleComposite);
		createActiveTargetsList(middleComposite);
		createUpDownComposite(middleComposite);
		
		Composite lowerComposite = new Composite(mainComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		lowerComposite.setLayout(layout);
		lowerComposite.setLayoutData(gridData);		
		
		createDescriptionField(lowerComposite);
		createShowSubTargetsButton(lowerComposite);
		
		if (tool != null) 
			restoreValues(tool);
		allowSelectTargets(!runDefaultTargetButton.getSelection());
		
		return mainComposite;
	}
	
	/*
	 * Creates the checkbox button for the
	 * "Run default target" preference.
	 */
	private void createRunDefaultTargetButton(Composite parent) {
		runDefaultTargetButton = new Button(parent, SWT.CHECK);
		// The label that is applied if the default target is unknown
		runDefaultTargetButton.setText(ToolMessages.getString("AntTargetsGroup.runDefaultTargetUnknownLabel")); //$NON-NLS-1$
		runDefaultTargetButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				runDefaultTargetSelected();
			}			
		});
		runDefaultTargetButton.setSelection(true);
	}
	
	/*
	 * Creates the checkbox button for the
	 * "Show sub-targets" preference.
	 */
	private void createShowSubTargetsButton(Composite parent) {
		showSubTargetsButton = new Button(parent, SWT.CHECK);
		showSubTargetsButton.setText(ToolMessages.getString("AntTargetsGroup.showSubTargetsLabel")); //$NON-NLS-1$
		showSubTargetsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (showSubTargetsButton.getSelection()) {
					showSubTargets();
				} else {
					hideSubTargets();
				}
			}			
		});
		showSubTargetsButton.setSelection(false);			
	}
	
	/*
	 * Creates the list of targets provided by the ant build tool.
	 */
	private void createAvailableTargetsList(Composite parent) {
		Composite listComposite = new Composite(parent, SWT.NONE);
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		listComposite.setLayoutData(gridData);
		
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		listComposite.setLayout(layout);
				
		availableLabel = new Label(listComposite, SWT.LEFT);
		availableLabel.setText(ToolMessages.getString("AntTargetsGroup.availableTargetsLabel")); //$NON-NLS-1$
		
		availableTargetsList = new List(listComposite, SWT.BORDER | SWT.MULTI);	
		gridData = new GridData(GridData.FILL_BOTH);
		availableTargetsList.setLayoutData(gridData);
		
		availableTargetsList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = availableTargetsList.getSelectionIndex();
				if (index >= 0)
					targetsSelected(availableTargetsList.getItem(index), availableTargetsList);
				else
					deselectAll();
			}
		});	
	}
	
	/*
	 * Creates the list of targets that will be used when the tool is run.
	 */
	private void createActiveTargetsList(Composite parent) {
		Composite listComposite = new Composite(parent, SWT.NONE);
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		listComposite.setLayoutData(gridData);
		
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		listComposite.setLayout(layout);
				
		activeLabel = new Label(listComposite, SWT.LEFT);
		activeLabel.setText(ToolMessages.getString("AntTargetsGroup.activeTargetsLabel")); //$NON-NLS-1$
		
		activeTargetsList = new List(listComposite, SWT.BORDER | SWT.MULTI);	
		gridData = new GridData(GridData.FILL_BOTH);
		activeTargetsList.setLayoutData(gridData);
		
		activeTargetsList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = activeTargetsList.getSelectionIndex();
				if (index >= 0)
					targetsSelected(activeTargetsList.getItem(index), activeTargetsList);
				else
					deselectAll();
			}
		});		
	}
	
	/*
	 * Creates the bank of buttons that allow the user to
	 * addButton and removeButton targets from the active list.
	 */
	private void createAddRemoveComposite(Composite parent) {
		Composite addRemoveComposite = new Composite(parent, SWT.NONE);

		GridData gridData = new GridData();
		addRemoveComposite.setLayoutData(gridData);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		addRemoveComposite.setLayout(layout);

		new Label(addRemoveComposite, SWT.NONE);
		
		addButton = createButton(
			addRemoveComposite, 
			ToolMessages.getString("AntTargetsGroup.addLabel"), //$NON-NLS-1$	
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					addTargets();
				}
			},
			false);
		
		removeButton = createButton(
			addRemoveComposite, 
			ToolMessages.getString("AntTargetsGroup.removeLabel"), //$NON-NLS-1$	
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					removeTargets();
				}
			},
			false);
		
		new Label(addRemoveComposite, SWT.NONE);
		
		addAll = createButton(
			addRemoveComposite, 
			ToolMessages.getString("AntTargetsGroup.addAllLabel"), //$NON-NLS-1$	
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					addAllTargets();
				}
			},
			false);
		
		removeAll = createButton(
			addRemoveComposite, 
			ToolMessages.getString("AntTargetsGroup.removeAllLabel"), //$NON-NLS-1$	
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					removeAllTargets();
				}
			},
			false);
	}
	
	/*
	 * Creates a button bank containing the buttons for moving
	 * targets in the active list upButton and downButton.
	 */
	private void createUpDownComposite(Composite parent) {
		Composite upDownComposite = new Composite(parent, SWT.NONE);

		GridData gridData = new GridData();
		upDownComposite.setLayoutData(gridData);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		upDownComposite.setLayout(layout);

		new Label(upDownComposite, SWT.NONE);
				
		upButton = createButton(
			upDownComposite, 
			ToolMessages.getString("AntTargetsGroup.upLabel"), //$NON-NLS-1$	
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					moveTargetUp();
				}
			},
			true);
				
		downButton = createButton(
			upDownComposite, 
			ToolMessages.getString("AntTargetsGroup.downLabel"), //$NON-NLS-1$	
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					moveTargetDown();
				}
			},
			true);
	}
	
	/*
	 * Creates a button in the given composite with the given label and selection adapter.
	 * minWidth specifies whether the button should be at minimum IDialogConstants.BUTTON_WIDTH
	 * wide.
	 */
	 private Button createButton(Composite parent, String label, SelectionAdapter adapter, boolean minWidth) {
		Button button = new Button(parent, SWT.PUSH);		
		button.setText(label);
		GridData data = getPage().setButtonGridData(button);
		if (!minWidth)
			data.widthHint = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
		button.addSelectionListener(adapter);
		
		return button;
	 }
	
	/*
	 * Creates the text field which displays the descriptionField of the selected target.
	 */
	private void createDescriptionField(Composite parent) {
		descriptionLabel = new Label(parent, SWT.NONE);
		descriptionLabel.setText(ToolMessages.getString("AntTargetsGroup.descriptionLabel")); //$NON-NLS-1$
		
		descriptionField = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = getPage().convertHeightHint(DESCRIPTION_FIELD_HEIGHT);
		descriptionField.setLayoutData(data);
	}

	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public void restoreValues(ExternalTool tool) {
		if (activeTargetsList != null) {
			activeTargetsList.setItems(toArray(tool.getExtraAttribute(AntUtil.RUN_TARGETS_ATTRIBUTE)));

			if (activeTargetsList.getItemCount() == 0) {
				runDefaultTargetButton.setSelection(true);
				runDefaultTargetSelected();
			}
		}
	}

	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public void updateTool(ExternalTool tool) {
		if (runDefaultTargetButton == null)
			return;
		if (runDefaultTargetButton.getSelection()) {
			tool.setExtraAttribute(AntUtil.RUN_TARGETS_ATTRIBUTE, null);
		} else {
			if (activeTargetsList != null)
				tool.setExtraAttribute(AntUtil.RUN_TARGETS_ATTRIBUTE, toString(activeTargetsList.getItems()));
		}
	}

	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public void validate() {
	}
	
	/**
	 * Informs the group of the current external tool
	 * file location.
	 */
	public void setFileLocation(String newLocation) {
		if (newLocation == null) {
			if (fileLocation != null) {
				fileLocation = newLocation;
				updateAvailableTargets();
			}
		} else if (!newLocation.equals(fileLocation)) {
			fileLocation = newLocation;
			updateAvailableTargets();
		}
	}

	/*
	 * Translates an array of target names into a 
	 * single string for storage.
	 */
	private String toString(String[] targetArray) {
		return AntUtil.combineRunTargets(targetArray);
	}
	
	/*
	 * Translates a single string of target names into
	 * an array of target names.
	 */
	private String[] toArray(String targetString) {
		return AntUtil.parseRunTargets(targetString);
	}
	
	/*
	 * The "run default target" preference has been selected.
	 */
	private void runDefaultTargetSelected() {
		allowSelectTargets(! runDefaultTargetButton.getSelection());
	}
	
	/*
	 * Adds the current selection in the available list
	 * to the active list.
	 */
	private void addTargets() {
		String[] targets = availableTargetsList.getSelection();
		for (int i=0; i < targets.length; i++) {
			activeTargetsList.add(targets[i]);
		}
		updateButtonEnablement();
	}
	
	/*
	 * Removes the current selection in the active list.
	 */
	private void removeTargets() {
		String[] targets = activeTargetsList.getSelection();
		for (int i=0; i < targets.length; i++) {
			activeTargetsList.remove(targets[i]);
		}
		deselectAll();
	}
	
	/*
	 * Adds all the available targets to the active list.
	 */
	private void addAllTargets() {
		String[] targets = availableTargetsList.getItems();
		for (int i=0; i < targets.length; i++) {
			activeTargetsList.add(targets[i]);	
		}
		updateButtonEnablement();
	}
	
	/*
	 * Removes all the active targets.
	 */
	private void removeAllTargets() {
		activeTargetsList.removeAll();
		deselectAll();
	}
	
	/*
	 * Moves the current selection in the active list upButton.
	 */
	private void moveTargetUp() {
		int index = activeTargetsList.getSelectionIndex();
		// Action only works if selected element is not first element.
		if (index > 0) {
			String target = activeTargetsList.getItem(index);
			activeTargetsList.remove(index);
			activeTargetsList.add(target, index - 1);
			activeTargetsList.setSelection(index - 1);
		}
		
		updateUpDownButtonEnablement();
	}
	
	/*
	 * Moves the current selection in the active list downButton.
	 */
	private void moveTargetDown() {
		int index = activeTargetsList.getSelectionIndex();
		if (index < 0)
			return;
		// Action only works if selected element is not last element.
		if (index < activeTargetsList.getItemCount() - 1) {
			String target = activeTargetsList.getItem(index);
			activeTargetsList.remove(index);
			activeTargetsList.add(target, index + 1);
			activeTargetsList.setSelection(index + 1);
		}
		
		updateUpDownButtonEnablement();
	}
	
	/*
	 * Updates the available targets list based on the tool location
	 * for this tool.
	 */
	private void updateAvailableTargets() {
		// Clear the map of target names to target infos.
		mapTargetNamesToTargetInfos.clear();
		subTargets.clear();
		availableTargetsList.removeAll();
		activeTargetsList.removeAll();
		
		if (fileLocation == null)
			return;
			
		try {
			MultiStatus status = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
			String expandedLocation = ToolUtil.expandFileLocation(fileLocation, ExpandVariableContext.EMPTY_CONTEXT, status);
			if (expandedLocation != null && status.isOK()) {
				TargetInfo[] targets = AntUtil.getTargets(expandedLocation);
				ArrayList targetNameList = new ArrayList();
				for (int i=0; i < targets.length; i++) {
					if (! AntUtil.isInternalTarget(targets[i])) {
						// Add the target to the map of target names to target infos.
						mapTargetNamesToTargetInfos.put(targets[i].getName(), targets[i]);

						if (targets[i].isDefault()) {
							defaultTarget = targets[i];
							runDefaultTargetButton.setText(ToolMessages.format("AntTargetsGroup.runDefaultTargetLabel", new Object[] {targets[i].getName()})); //NON-NLS-1$
						}
						
						if (AntUtil.isSubTarget(targets[i])) {
							subTargets.add(targets[i].getName());
						} else {
							targetNameList.add(targets[i].getName());
						}
					}
				}
				if (showSubTargetsButton.getSelection())
					targetNameList.addAll(subTargets);
					
				String[] targetNames = (String[]) targetNameList.toArray(new String[targetNameList.size()]);
				availableTargetsList.setItems(targetNames);
			} else {
				displayErrorStatus(status);
			}
		} catch (CoreException e) {
			displayErrorStatus(e.getStatus());
		}
	}
	
	/*
	 * Displays an error dialog with the given status.
	 */
	private void displayErrorStatus(IStatus status) {
		ErrorDialog.openError(
			null, 
			ToolMessages.getString("AntTargetsGroup.getTargetsTitle"), //$NON-NLS-1$;
			ToolMessages.getString("AntTargetsGroup.getTargetsProblem"), //$NON-NLS-1$;
			status);
	}
	
	/*
	 * A target was selected in one of the lists.
	 */
	private void targetsSelected(String targetName, List list) {
		updateButtonEnablement();

		if (targetName == null)	
			return;		
		if (list == availableTargetsList) {
			activeTargetsList.deselectAll();
		} else {
			availableTargetsList.deselectAll();
		}
		
		showDescription(targetName);
	}
	
	/*
	 * Updates the enablement of all the buttons in the group.
	 */
	private void updateButtonEnablement() {
		updateUpDownButtonEnablement();
		updateAddRemoveButtonEnablement();
	}
	
	/*
	 * Updates the enabled state of the upButton and downButton buttons based
	 * on the current list selection.
	 */
	private void updateUpDownButtonEnablement() {
		if (activeTargetsList.getEnabled() == false) {
			disableUpDownButtons();
			return;
		}
		// Disable upButton and downButton buttons if there is not one
		// target selected in the active list.
		if (activeTargetsList.getSelectionCount() != 1) {
			disableUpDownButtons();
			return;	
		}
			
		int index = activeTargetsList.getSelectionIndex();
		if (index > 0)
			upButton.setEnabled(true);
		else
			upButton.setEnabled(false);
	
		if (index >= 0 && index < activeTargetsList.getItemCount() - 1)
			downButton.setEnabled(true);		
		else
			downButton.setEnabled(false);		
	}
	
	/*
	 * Updates the enabled state of the addButton, removeButton, addAll, and
	 * removeAll buttons based on the current list selection.
	 */
	private void updateAddRemoveButtonEnablement() {
		if (runDefaultTargetButton.getSelection()) {
			disableAddRemoveButtons();
			return;
		}
		int availableIndex = availableTargetsList.getSelectionIndex();
		int activeIndex = activeTargetsList.getSelectionIndex();
		addButton.setEnabled(availableIndex >= 0);
		removeButton.setEnabled(activeIndex >= 0);
		addAll.setEnabled(availableTargetsList.getItemCount() > 0);
		removeAll.setEnabled(activeTargetsList.getItemCount() > 0);
	}
	
	/*
	 * Deselects all targets in both lists.
	 */
	private void deselectAll() {
		availableTargetsList.deselectAll();
		activeTargetsList.deselectAll();
		updateButtonEnablement();
		clearDescriptionField();
	}

	/*
	 * Enables all the appropriate controls.
	 */
	private void allowSelectTargets(boolean enabled) {
		if (! enabled) {
			deselectAll();
			if (defaultTarget != null && defaultTarget.getDescription() != null)
				descriptionField.setText(defaultTarget.getDescription());	
		} else {
			descriptionField.setText(""); //$NON-NLS-1$
		}
		
		availableLabel.setEnabled(enabled);
	 	availableTargetsList.setEnabled(enabled);
	 	activeLabel.setEnabled(enabled);
	 	activeTargetsList.setEnabled(enabled);
	 	descriptionLabel.setEnabled(enabled);
	 	descriptionField.setEnabled(enabled);
	 	showSubTargetsButton.setEnabled(enabled);
	 	updateButtonEnablement();	
	}

	/*
	 * Disables all buttons in the group.
	 */
	private void disableAllButtons() {
		disableAddRemoveButtons();
		disableUpDownButtons();
	}
	
	/*
	 * Disables the addButton, removeButton, addAll, and
	 * removeAll buttons.
	 */
	private void disableAddRemoveButtons() {
		addButton.setEnabled(false);
		removeButton.setEnabled(false);
		addAll.setEnabled(false);
		removeAll.setEnabled(false);		
	}
	
	/*
	 * Disables the upButton and downButton buttons.
	 */
	private void disableUpDownButtons() {
		upButton.setEnabled(false);
		downButton.setEnabled(false);		
	}
	
	/*
	 * Shows the descriptionField of the given target in the
	 * descriptionField field.
	 */
	private void showDescription(String targetName) {
		clearDescriptionField();
		if (targetName == null)
			return;
		TargetInfo targetInfo = (TargetInfo) mapTargetNamesToTargetInfos.get(targetName);
		if (targetInfo != null && targetInfo.getDescription() != null)
			descriptionField.setText(targetInfo.getDescription());
	}
	
	/*
	 * Clears the descriptionField field.
	 */
	 private void clearDescriptionField() {
	 	descriptionField.setText(""); //$NON-NLS-1$
	 }
	 
	 /*
	  * Shows sub-targets in the available targets list.
	  */
	 private void showSubTargets() {
	 	Iterator i = subTargets.iterator();
	 	while (i.hasNext()) {
	 		String target = (String) i.next();
	 		availableTargetsList.add(target);
	 	}
	 }
	 
	 /*
	  * Hides sub-targets in the available targets list.
	  */
	 private void hideSubTargets() {
	 	int startOfSubTargets = availableTargetsList.getItemCount() - subTargets.size();
	 	int endOfSubTargets = availableTargetsList.getItemCount() - 1;
	 	availableTargetsList.remove(startOfSubTargets, endOfSubTargets);
	 }
}
