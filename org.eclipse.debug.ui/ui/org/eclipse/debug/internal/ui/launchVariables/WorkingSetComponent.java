/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchVariables;

import java.io.IOException;
import java.io.StringWriter;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.launchVariables.AbstractVariableComponent;
import org.eclipse.debug.ui.launchVariables.IVariableComponentContainer;
import org.eclipse.debug.ui.launchVariables.IVariableConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.dialogs.IWorkingSetEditWizard;

/**
 * The working set component allows the user to choose a working set from the
 * workspace
 */
public class WorkingSetComponent extends AbstractVariableComponent {
	
	private ILabelProvider labelProvider;
	private Button chooseButton;
	private IWorkingSet currentWorkingSet;

	/**
	 * @see IVariableComponent#createContents(Composite, String, IVariableComponentContainer)
	 */
	public void createContents(Composite parent, String varTag, IVariableComponentContainer page) {
		super.createContents(parent, varTag, page); // Creates the main group and sets the page
		Font font= parent.getFont();
		GridLayout layout= (GridLayout)mainGroup.getLayout();
		layout.numColumns= 2;

		chooseButton = new Button(mainGroup, SWT.PUSH);
		GridData data= new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		chooseButton.setLayoutData(data);
		chooseButton.setFont(font);
		chooseButton.setText(LaunchVariableMessages.getString("WorkingSetComponent.1")); //$NON-NLS-1$
		chooseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleChooseButtonPressed();
			}
		});
	}

	/**
	 * Handles the working set choose button pressed. Returns the name of the
	 * chosen working set or <code>null</code> if none.
	 */
	private void handleChooseButtonPressed() {
		IWorkingSetManager workingSetManager= PlatformUI.getWorkbench().getWorkingSetManager();
		
		if (currentWorkingSet == null){
			currentWorkingSet= workingSetManager.createWorkingSet(LaunchVariableMessages.getString("WorkingSetComponent.2"), new IAdaptable[0]); //$NON-NLS-1$
		}
		IWorkingSetEditWizard wizard= workingSetManager.createWorkingSetEditWizard(currentWorkingSet);
		WizardDialog dialog = new WizardDialog(DebugUIPlugin.getStandardDisplay().getActiveShell(), wizard);
		dialog.create();		
		
		if (dialog.open() == Dialog.CANCEL) {
			return;
		}
		currentWorkingSet= wizard.getSelection();
		validate();
	}
	
	/**
	 * @see IVariableComponent#getVariableValue()
	 */
	public String getVariableValue() {
		XMLMemento workingSetMemento = XMLMemento.createWriteRoot(IVariableConstants.TAG_LAUNCH_CONFIGURATION_WORKING_SET);
	
		IPersistableElement persistable = null;

		if (currentWorkingSet instanceof IPersistableElement) {
			persistable = (IPersistableElement) currentWorkingSet;
		} else if (currentWorkingSet instanceof IAdaptable) {
			persistable = (IPersistableElement) ((IAdaptable) currentWorkingSet).getAdapter(IPersistableElement.class);
		}
		if (persistable != null) {
			workingSetMemento.putString(IVariableConstants.TAG_FACTORY_ID, persistable.getFactoryId());
			persistable.saveState(workingSetMemento);
			StringWriter writer= new StringWriter();
			try {
				workingSetMemento.save(writer);
			} catch (IOException e) {
				DebugUIPlugin.log(e);
			}
			return writer.toString();
		}
		getContainer().setErrorMessage(LaunchVariableMessages.getString("WorkingSetComponent.3")); //$NON-NLS-1$
		return null;
	}

	/**
	 * @see IVariableComponent#setVariableValue(String)
	 */
	public void setVariableValue(String varValue) {
		currentWorkingSet= WorkingSetExpander.restoreWorkingSet(varValue);
	}

	/**
	 * @see IVariableComponent#validate()
	 */
	public void validate() {
		boolean isValid= getVariableValue() != null;
		if (isValid) {
			getContainer().setErrorMessage(null);
		} else {
			getContainer().setErrorMessage(LaunchVariableMessages.getString("WorkingSetComponent.4")); //$NON-NLS-1$
		}
		setIsValid(isValid);
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.debug.ui.launchVariables.IVariableComponent#dispose()
	 */
	public void dispose() {
		if (labelProvider != null) {
			labelProvider.dispose();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.launchVariables.IVariableComponent#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		mainGroup.setEnabled(enabled);
		chooseButton.setEnabled(enabled);
	}
}