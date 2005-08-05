/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - Mikhail Khodjaiants - Bug 89748
 *******************************************************************************/
package org.eclipse.debug.internal.ui.sourcelookup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.DefaultSourceContainer;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.WorkingSetSourceContainer;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;

/**
 * The panel that contains the list of source containers.
 * 
 * @since 3.0
 */
public class SourceLookupPanel extends AbstractLaunchConfigurationTab implements IPropertyChangeListener {
	//the configuration being edited
	protected ILaunchConfiguration fConfig;
	//the viewer displaying the containers
	protected SourceContainerViewer fPathViewer;
	//the duplicates checkbox
	protected Button fDuplicatesButton;
	//the source actions - up, down, add, remove, restore
	protected List fActions = new ArrayList(6);
	//the director that will be used by the tab to manage/store the containers
	protected ISourceLookupDirector fLocator;
	
	protected AddContainerAction fAddAction; 
	protected EditContainerAction fEditAction;
	protected RestoreDefaultAction fRestoreDefaultAction;
	
	/**
	 * Creates and returns the source lookup control.
	 * 
	 * @param parent the parent widget of this control
	 */
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		comp.setLayout(topLayout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);
		
		Label viewerLabel = new Label(comp, SWT.LEFT);
		viewerLabel.setText(
				SourceLookupUIMessages.sourceTab_lookupLabel); 
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		viewerLabel.setLayoutData(gd);
		viewerLabel.setFont(font);
		
		fPathViewer = new SourceContainerViewer(comp, this);
		
		gd = new GridData(GridData.FILL_BOTH);
		fPathViewer.getControl().setLayoutData(gd);
		fPathViewer.getControl().setFont(font);
		
		IWorkingSetManager workingSetMgr =DebugUIPlugin.getDefault().getWorkbench().getWorkingSetManager();
		//listen to changes user made to the working sets, if a working set is being removed
		//check current list to validate working sets  
		workingSetMgr.addPropertyChangeListener(this);
		
		Composite pathButtonComp = new Composite(comp, SWT.NONE);
		GridLayout pathButtonLayout = new GridLayout();
		pathButtonLayout.marginHeight = 0;
		pathButtonLayout.marginWidth = 0;
		pathButtonComp.setLayout(pathButtonLayout);
		gd =
			new GridData(
					GridData.VERTICAL_ALIGN_BEGINNING
					| GridData.HORIZONTAL_ALIGN_FILL);
		pathButtonComp.setLayoutData(gd);
		pathButtonComp.setFont(font);
		
		createVerticalSpacer(comp, 2);
		
		fDuplicatesButton = new Button(comp, SWT.CHECK);
		fDuplicatesButton.setText(
				SourceLookupUIMessages.sourceTab_searchDuplicateLabel); 
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		fDuplicatesButton.setLayoutData(gd);
		fDuplicatesButton.setFont(font);
		fDuplicatesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				setDirty(true);
				updateLaunchConfigurationDialog();
			}
		});
		
		GC gc = new GC(parent);
		gc.setFont(parent.getFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();
		
		fAddAction = new AddContainerAction();
		Button button =
			createPushButton(pathButtonComp, fAddAction.getText(), fontMetrics);
		fAddAction.setButton(button);
		addAction(fAddAction);
		
		fEditAction = new EditContainerAction();
		button =
			createPushButton(pathButtonComp, fEditAction.getText(), fontMetrics);
		fEditAction.setButton(button);
		addAction(fEditAction);
		
		SourceContainerAction action = new RemoveAction();
		button =
			createPushButton(pathButtonComp, action.getText(), fontMetrics);
		action.setButton(button);
		addAction(action);
		
		action = new UpAction();
		button =
			createPushButton(pathButtonComp, action.getText(), fontMetrics);
		action.setButton(button);
		addAction(action);
		
		action = new DownAction();
		button =
			createPushButton(pathButtonComp, action.getText(), fontMetrics);
		action.setButton(button);
		addAction(action);		
		
		fRestoreDefaultAction = new RestoreDefaultAction();
		button = createPushButton(pathButtonComp, fRestoreDefaultAction.getText(), fontMetrics);
		fRestoreDefaultAction.setButton(button);
		addAction(fRestoreDefaultAction);
		
		retargetActions(fPathViewer);
		
		Dialog.applyDialogFont(comp);
		setControl(comp);
	}	
	
	/**
	 * Creates and returns a button 
	 * 
	 * @param parent parent widget
	 * @param label label
	 * @return Button
	 */
	protected Button createPushButton(
			Composite parent,
			String label,
			FontMetrics fontMetrics) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		button.setText(label);
		GridData gd = getButtonGridData(button, fontMetrics);
		button.setLayoutData(gd);
		return button;
	}
	
	private GridData getButtonGridData(
			Button button,
			FontMetrics fontMetrics) {
		GridData gd =
			new GridData(
					GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		
		int widthHint =
			Dialog.convertHorizontalDLUsToPixels(
					fontMetrics,
					IDialogConstants.BUTTON_WIDTH);
		gd.widthHint =
			Math.max(
					widthHint,
					button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		
		return gd;
	}
	
	/**
	 * Create some empty space 
	 */
	protected void createVerticalSpacer(Composite comp, int colSpan) {
		Label label = new Label(comp, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = colSpan;
		label.setLayoutData(gd);
	}
	
	/**
	 * Adds the given action to the action collection in this tab
	 */
	protected void addAction(SourceContainerAction action) {
		fActions.add(action);
	}
	
	/**
	 * Re-targets actions to the given viewer
	 */
	protected void retargetActions(SourceContainerViewer viewer) {
		Iterator actions = fActions.iterator();
		while (actions.hasNext()) {
			SourceContainerAction action = (SourceContainerAction) actions.next();
			action.setViewer(viewer);
		}
	}
	
	/**
	 * Initializes this control based on the settings in the given
	 * launch configuration.
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		if (fLocator != null) {
			fLocator.dispose();
			fLocator = null;
		}
		setErrorMessage(null);
		setMessage(null);
		String memento = null;	
		String type = null;
		try{
			memento = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, (String)null);
			type = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String)null);
			if (type == null) {
				type = configuration.getType().getSourceLocatorId();
			}
		}catch(CoreException e){
			setErrorMessage(e.getMessage());
			return;
		}	
		
		if(type == null) {
			setErrorMessage(SourceLookupUIMessages.sourceLookupPanel_2); 
			return;
		}
		
		boolean migration = false;
		try {
			ISourceLocator locator = getLaunchManager().newSourceLocator(type);
			if(!(locator instanceof AbstractSourceLookupDirector)) {
				// migrate to the new source lookup infrastructure
				memento = null; // don't use old memento
				type = configuration.getType().getSourceLocatorId();
				if(type == null) {
					setErrorMessage(SourceLookupUIMessages.sourceLookupPanel_2); 
					return;
				}
				locator = getLaunchManager().newSourceLocator(type);
				if (!(locator instanceof AbstractSourceLookupDirector)) {
					setErrorMessage(SourceLookupUIMessages.sourceLookupPanel_2); 
					return;
				}
				migration = true;
			}
			fLocator = (AbstractSourceLookupDirector)locator;			
			if (memento == null) {
				fLocator.initializeDefaults(configuration);
			} else {				
				fLocator.initializeFromMemento(memento, configuration);				 
			}			
		} catch (CoreException e) {
			setErrorMessage(e.getMessage());
			return;
		}	
		
		initializeFrom(fLocator);
		if (migration && configuration.isWorkingCopy()) {
			// ensure perform apply actual updates the config
			setDirty(true);
			performApply((ILaunchConfigurationWorkingCopy)configuration);
		}
	}
	
	/**
	 * Initializes this control based on the settings in the given
	 * AbstractSourceLookupDirector
	 */
	public void initializeFrom(ISourceLookupDirector locator) {
		if(fConfig == null)
			fConfig = locator.getLaunchConfiguration();
		fPathViewer.setEntries(locator.getSourceContainers());		
		fDuplicatesButton.setSelection(locator.isFindDuplicates());
		fLocator = locator;
		fAddAction.setSourceLookupDirector(locator);
		fEditAction.setSourceLookupDirector(locator);
		fRestoreDefaultAction.setSourceLookupDirector(locator);
		setDirty(false);
	}
	
	/**
	 * Saves the containers and duplicate policy into the given working copy of the configuration.  
	 * Saving the configuration will result in a change event, which will be picked up by the director 
	 * and used to refresh its internal list.
	 * 
	 * @param containers the list of containers entered by the user
	 * @param duplicates true if the user checked the duplicates check box, false otherwise
	 * @param workingCopy the working copy of the configuration that these values should be stored in, may be null.
	 * 	If null, will be written into a working copy of the configuration referenced by the director.
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {		
		if (isDirty()) {
			if (fLocator == null) {
				configuration.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, (String)null);
				configuration.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String)null);
				return;
			}
			ILaunchConfigurationWorkingCopy workingCopy = null;			
			if(configuration == null) {
				try{
					workingCopy = fLocator.getLaunchConfiguration().getWorkingCopy();
				}catch(CoreException e){ 
					DebugUIPlugin.log(e);
					setErrorMessage(SourceLookupUIMessages.sourceLookupPanel_1); 
					return;
				}
			}
			else workingCopy = configuration;	
			if(workingCopy == null) {
				DebugUIPlugin.logErrorMessage(SourceLookupUIMessages.sourceLookupPanel_1); 
				return;
			}
			//set new values in director so memento returned is correct
			fLocator.setSourceContainers(fPathViewer.getEntries());
			fLocator.setFindDuplicates(fDuplicatesButton.getSelection());
						
			//writing to the file will cause a change event and the listeners will be updated
			try{			
				if (isDefault(workingCopy)) {
					workingCopy.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, (String)null);
					workingCopy.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String)null);
				} else {
					workingCopy.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, fLocator.getMemento());
					workingCopy.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, fLocator.getId());
				}
				if(configuration == null) 
					workingCopy.doSave(); 
				setDirty(false);
			}catch(CoreException e){
				DebugUIPlugin.log(e);
				setErrorMessage(SourceLookupUIMessages.sourceLookupPanel_1); 
			}
			
		}			
	}
	
	protected boolean isDefault(ILaunchConfiguration configuration) {
		ISourceContainer[] current = getEntries();
		return !isFindDuplicates() && current.length == 1 && current[0] instanceof DefaultSourceContainer;
	}

	private boolean isFindDuplicates() {
		return fDuplicatesButton.getSelection();
	}

	/**
	 * Returns the entries visible in the viewer
	 */
	public ISourceContainer[] getEntries() {
		return fPathViewer.getEntries();
	}
	/**
	 * Marks the panel as dirty.
	 */
	public void setDirty(boolean dirty) {
		super.setDirty(dirty);
		
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return SourceLookupUIMessages.sourceTab_tabTitle; 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#updateLaunchConfigurationDialog()
	 */
	protected void updateLaunchConfigurationDialog() {
		if (getLaunchConfigurationDialog() != null) {
			super.updateLaunchConfigurationDialog();
		}
	}
		
	/**
	 * This is called whenever a working set has been removed. Loops through the original list
	 * of working sets the user stores in the configuration. If the one being removed is in the
	 * list, remove it from the list
	 * @param affectedWorkingSet - the working set being removed
	 */
	private void validateWorkingSetSourceContainers(IWorkingSet affectedWorkingSet) {
		List sourceContainers = (List) fPathViewer.getInput();
		
		if (sourceContainers != null) {
			for (int i = 0; i < sourceContainers.size(); i++) {
				if (sourceContainers.get(i)
						instanceof WorkingSetSourceContainer) {
					WorkingSetSourceContainer wsSrcContainer =
						(WorkingSetSourceContainer) sourceContainers.get(i);
					if (wsSrcContainer
							.getName()
							.equals(affectedWorkingSet.getName())) {
						sourceContainers.remove(i);
					}
				}
			}
		}
	}
	
	/**
	 * Listen to working set changes
	 * @param event
	 */
	public void propertyChange(PropertyChangeEvent event) {
		//if the PropertyChangeEvent has changeId CHANGE_WORKING_SET_REMOVE, 
		//validate the list to make sure all working sets are valid 
		if (event.getProperty().equals(IWorkingSetManager.CHANGE_WORKING_SET_REMOVE))
			validateWorkingSetSourceContainers((IWorkingSet) event.getOldValue());
		
		//if the PropertyChangeEvent has changeId CHANGE_WORKING_SET_NAME_CHANGE,
		//do nothing because the event only has newValue, since oldValue is not provided
		//there is no way to identify which working set does the newValue corresponds to									
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		initializeFrom(workingCopy);
	}
	
	/**
	 * Returns the source lookup director associated with this
	 * panel, or <code>null</code> if none.
	 * 
	 * @return the source lookup director associated with this
	 * panel, or <code>null</code> if none
	 */
	public ISourceLookupDirector getDirector() {
		return fLocator;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
		super.dispose();
		IWorkingSetManager workingSetMgr =DebugUIPlugin.getDefault().getWorkbench().getWorkingSetManager();
		//listen to changes user made to the working sets, if a working set is being removed
		//check current list to validate working sets  
		workingSetMgr.removePropertyChangeListener(this);
	}
}
