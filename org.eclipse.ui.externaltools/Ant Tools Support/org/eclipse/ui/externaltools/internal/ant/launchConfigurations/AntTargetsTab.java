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
import java.util.Iterator;
import java.util.List;

import org.eclipse.ant.core.TargetInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.ui.externaltools.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.model.ToolUtil;
import org.eclipse.ui.externaltools.variable.ExpandVariableContext;

public class AntTargetsTab extends AbstractLaunchConfigurationTab {

	private Button runDefaultTargetButton;
	private Button upButton;
	private Button downButton;
	
	private Label descriptionLabel;
	private Label executeLabel;
	private TableViewer executeTargetsTable;
	
	private TargetInfo defaultTarget = null;
	private TargetInfo[] allTargets= null;
	private Text descriptionField;
	
	private String location= null;
	private Button addButton;
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
	}
	
	/*
	 * Enables all the appropriate controls.
	 */
	private void allowSelectTargets(boolean enabled, boolean retrieveTargets) {
		if (! enabled) {
			if (defaultTarget != null && defaultTarget.getDescription() != null) {
				descriptionField.setText(defaultTarget.getDescription());	
			}
		} else {
			descriptionField.setText(""); //$NON-NLS-1$
		}
		
	 	executeLabel.setEnabled(enabled);
	 	executeTargetsTable.getControl().setEnabled(enabled);
	 	executeTargetsTable.refresh();
	 	if (enabled && executeTargetsTable.getTable().getItemCount() == 0 && retrieveTargets) {
	 		executeTargetsTable.setInput(getTargets());
	 	}
	 	descriptionLabel.setEnabled(enabled);
	 	descriptionField.setEnabled(enabled);
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
		
		addButton = createPushButton(buttonComposite, "Add...", null);
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
		if (allTargets == null) {
			setErrorMessage(null);
			MultiStatus status = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
			String expandedLocation = ToolUtil.expandFileLocation(location, ExpandVariableContext.EMPTY_CONTEXT, status);
			if (expandedLocation != null && status.isOK()) {
				try {
					allTargets = AntUtil.getTargets(expandedLocation);
				} catch (CoreException ce) {
					ExternalToolsPlugin.getDefault().log("Problems retrieving Ant Targets", ce);
					setErrorMessage(ce.getMessage());
					allTargets= null;
					return allTargets;
				}
				for (int i=0; i < allTargets.length; i++) {
					if (! AntUtil.isInternalTarget(allTargets[i])) {
						if (allTargets[i].isDefault()) {
							defaultTarget = allTargets[i];
							runDefaultTargetButton.setText(MessageFormat.format("Run default target ({0})", new Object[] {allTargets[i].getName()})); //NON-NLS-1$
							break;
						}
					}
				}
			}
				
			if (allTargets != null) {
				Arrays.sort(allTargets, new Comparator() {
					public int compare(Object o1, Object o2) {
						TargetInfo t1= (TargetInfo)o1;
						TargetInfo t2= (TargetInfo)o2;
						return t1.getName().compareTo(t2.getName());
					}
					public boolean equals(Object obj) {
						return false;
					}
				});
			}
		}
		return allTargets;
	}
				
	private void addTargets() {
		
		TargetInfo[] targets= getTargets();
		AntTargetLabelProvider labelProvider= new AntTargetLabelProvider();
		if (defaultTarget != null) {
			labelProvider.setDefaultTargetName(defaultTarget.getName());
		}					
		SelectionDialog dialog= new ListSelectionDialog(getShell(), targets, new AntTargetContentProvider(), labelProvider, "Select Ant Targets:");
		if (dialog.open() == SelectionDialog.OK) {
			getContentProvider().addAll(Arrays.asList(dialog.getResult()));
		}
	}
	
	private AntTargetContentProvider getContentProvider() {
		return (AntTargetContentProvider)executeTargetsTable.getContentProvider();
	}
	
	private void removeTargets() {
		IStructuredSelection selection = (IStructuredSelection)executeTargetsTable.getSelection();
		getContentProvider().remove(selection);
	}
	
	private void handleMove(int direction) {
		IStructuredSelection sel = (IStructuredSelection)executeTargetsTable.getSelection();
		List selList= sel.toList();
		Object[] elements = getContentProvider().getElements(null);
		List contents= new ArrayList(elements.length);
		for (int i = 0; i < elements.length; i++) {
			contents.add(elements[i]);
		}
		Object[] moved= new Object[contents.size()];
		int i;
		for (Iterator current = selList.iterator(); current.hasNext();) {
			Object config = current.next();
			i= contents.indexOf(config);
			moved[i + direction]= config;
		}
		
		contents.removeAll(selList);
			
		for (int j = 0; j < moved.length; j++) {
			Object config = moved[j];
			if (config != null) {
				contents.add(j, config);		
			}
		}
		executeTargetsTable.setInput(contents.toArray(new TargetInfo[contents.size()]));
		executeTargetsTable.setSelection(executeTargetsTable.getSelection());
	}


	/*
	 * Creates the list of targets that will be used when the tool is run.
	 */
	private void createExecuteTargetsList(Composite parent) {
		Composite tableComposite = new Composite(parent, SWT.NONE);
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		tableComposite.setLayoutData(gridData);
		
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		tableComposite.setLayout(layout);
				
		executeLabel = new Label(tableComposite, SWT.LEFT);
		executeLabel.setText("Targets to execute:"); 
		
		executeTargetsTable = new TableViewer(tableComposite,SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		executeTargetsTable.setContentProvider(new AntTargetContentProvider());
		AntTargetLabelProvider labelProvider= new AntTargetLabelProvider(executeTargetsTable);
		if (defaultTarget != null) {
			labelProvider.setDefaultTargetName(defaultTarget.getName());
		}
		executeTargetsTable.setLabelProvider(labelProvider);
		gridData = new GridData(GridData.FILL_BOTH);
		executeTargetsTable.getControl().setLayoutData(gridData);
		
		executeTargetsTable.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)executeTargetsTable.getSelection();
				targetsSelected((TargetInfo)selection.getFirstElement());
			}
		});
	}
	
	/*
	 * A target was selected.
	 */
	private void targetsSelected(TargetInfo target) {
		updateButtonEnablement();
		String description= "";
		if (target != null) {
			description =target.getDescription();
			if (description == null) {
				description = "";
			}
		}
		descriptionField.setText(description);
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
	
	/**
	 * Creates the checkbox button for the
	 * "Run default target" preference.
	 */
	private void createRunDefaultTargetButton(Composite parent) {
		runDefaultTargetButton = new Button(parent, SWT.CHECK);
		// The label that is applied if the default target is unknown
		runDefaultTargetButton.setText("Run default target");
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		runDefaultTargetButton.setLayoutData(gridData);
		runDefaultTargetButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				allowSelectTargets(!runDefaultTargetButton.getSelection(), true);
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
		setErrorMessage(null);
		setMessage(null);
		String configTargets= null;
		try {
			configTargets= configuration.getAttribute(IExternalToolConstants.ATTR_ANT_TARGETS, (String)null);
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log("Error reading configuration", ce);
		}
		
		String newLocation= null;
		try {
			newLocation= configuration.getAttribute(IExternalToolConstants.ATTR_LOCATION, (String)null);
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log("Error reading configuration", ce);
		}
		if (newLocation == null) {
			executeTargetsTable.setInput(new TargetInfo[0]);
			return; 
		}
		
		if (!newLocation.equals(location)) {
			allTargets= null;
			location= newLocation;
			runDefaultTargetButton.setText("Run default target");
		}
		
		runDefaultTargetButton.setSelection(configTargets == null);
		allowSelectTargets(configTargets != null, false);
		TargetInfo[] infos= getTargets();
		if (infos == null) {
			executeTargetsTable.setInput(new TargetInfo[0]);
			return; 
		}
		String[] targetNames= AntUtil.parseRunTargets(configTargets);
		if (targetNames.length == 0) {
			executeTargetsTable.setInput(new TargetInfo[0]);
			return;
		}
		
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

		if (targetInfos.length > 0) {
			executeTargetsTable.setInput(targetInfos);
		}
		if (defaultTarget != null) {
			((AntTargetLabelProvider)executeTargetsTable.getLabelProvider()).setDefaultTargetName(defaultTarget.getName());
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (!runDefaultTargetButton.getSelection()) {
			Object[] items= getContentProvider().getElements(null);
			StringBuffer buff= new StringBuffer();
			for (int i = 0; i < items.length; i++) {
				TargetInfo item = (TargetInfo)items[i];
				buff.append(item.getName());
				buff.append(',');
			}
			configuration.setAttribute(IExternalToolConstants.ATTR_ANT_TARGETS, buff.toString());
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
		if (executeTargetsTable.getTable().isEnabled() == false) {
			upButton.setEnabled(false);
			downButton.setEnabled(false);
			removeButton.setEnabled(false);
			addButton.setEnabled(false);	
			return;
		}
		
		addButton.setEnabled(true);
		// Disable upButton and downButton buttons if there is not one
		// target selected in the active list.
		if (executeTargetsTable.getTable().getSelectionCount() == 0) {
			upButton.setEnabled(false);
			downButton.setEnabled(false);
			removeButton.setEnabled(false);
			return;	
		}
		
		removeButton.setEnabled(true);
		int index = executeTargetsTable.getTable().getSelectionIndex();
		if (index > 0) {
			upButton.setEnabled(true);
		} else {
			upButton.setEnabled(false);
		}
	
		if (index >= 0 && 
			executeTargetsTable.getTable().getSelectionIndices()[executeTargetsTable.getTable().getSelectionCount() - 1] < executeTargetsTable.getTable().getItemCount() - 1) {
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
