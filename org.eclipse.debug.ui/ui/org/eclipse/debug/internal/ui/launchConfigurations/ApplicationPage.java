/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A page that allows a user to select a launch configuration from a launch group.
 */
public class ApplicationPage extends AbstractLaunchPage {
	
	private StructuredViewer fTypesViewer;
	private StructuredViewer fConfigsViewer;
	
	/**
	 * Creates a page for the given launch group
	 * 
	 * @param group launch group
	 */
	public ApplicationPage(String label, ImageDescriptor image) {
		super(label, null, image);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		setControl(composite);
		GridLayout layout = new GridLayout(3, false);
		composite.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gd);
		composite.setFont(parent.getFont());
		
		// config type area
		Composite typesComp = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		typesComp.setLayout(layout);
		gd = new GridData(GridData.FILL_VERTICAL);
		typesComp.setLayoutData(gd);
		typesComp.setFont(parent.getFont());
		
		// config area
		Composite configsComp = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		configsComp.setLayout(layout);
		gd = new GridData(GridData.FILL_BOTH);
		configsComp.setLayoutData(gd);
		configsComp.setFont(parent.getFont());
		
		// button area
		Composite buttonsComp = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonsComp.setLayout(layout);
		gd = new GridData(GridData.FILL_VERTICAL);
		buttonsComp.setLayoutData(gd);
		buttonsComp.setFont(parent.getFont());
		
		// create config type area
		Label label = new Label(typesComp, SWT.NONE);
		label.setFont(parent.getFont());
		label.setText(LaunchConfigurationsMessages.getString("ApplicationPage.0")); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gd);
		
		fTypesViewer = new LaunchConfigurationTypesViewer(typesComp, getLaunchGroup());
		gd = new GridData(GridData.FILL_BOTH);
		fTypesViewer.getControl().setLayoutData(gd);
		
		// create configs area
		label = new Label(configsComp, SWT.NONE);
		label.setFont(parent.getFont());
		label.setText(LaunchConfigurationsMessages.getString("ApplicationPage.1")); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gd);
		
		fConfigsViewer = new LaunchConfigurationsViewer(configsComp, getLaunchGroup());
		gd = new GridData(GridData.FILL_BOTH);
		fConfigsViewer.getControl().setLayoutData(gd);
		
		// button area
		new Label(buttonsComp, SWT.NONE); // spacer
		final Button newButton = SWTUtil.createPushButton(buttonsComp, LaunchConfigurationsMessages.getString("ApplicationPage.2"), null); //$NON-NLS-1$
		final Button copyButton = SWTUtil.createPushButton(buttonsComp, LaunchConfigurationsMessages.getString("ApplicationPage.3"), null); //$NON-NLS-1$
		final Button deleteButton = SWTUtil.createPushButton(buttonsComp, LaunchConfigurationsMessages.getString("ApplicationPage.4"), null); //$NON-NLS-1$
		final Action deleteAction = new DeleteLaunchConfigurationAction(fConfigsViewer, getMode());
		final DuplicateLaunchConfigurationAction copyAction = new DuplicateLaunchConfigurationAction(fConfigsViewer, getMode());
		
		newButton.setEnabled(false);
		copyButton.setEnabled(false);
		deleteButton.setEnabled(false);
		setPageComplete(false);
		
		fTypesViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
				    ILaunchConfigurationType type = (ILaunchConfigurationType) ((IStructuredSelection)selection).getFirstElement();
				    setLaunchConfigurationType(type);
					fConfigsViewer.setInput(type);
					setDescription(getDescription(type));
					newButton.setEnabled(type != null);
				}
			}
		});		
		
		fConfigsViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
				    IStructuredSelection ss = (IStructuredSelection) selection;
				    setPageComplete(ss.size() == 1);
				    setLaunchConfiguration((ILaunchConfiguration) ss.getFirstElement());
				    deleteButton.setEnabled(ss.size() > 0);
				    copyButton.setEnabled(ss.size() == 1);
				}
            }
        });
		
		newButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    ILaunchConfigurationType type = getLaunchConfigurationType();
        			ILaunchConfigurationWorkingCopy wc = type.newInstance(null, DebugPlugin.getDefault().getLaunchManager().generateUniqueLaunchConfigurationNameFrom(LaunchConfigurationsMessages.getString("CreateLaunchConfigurationAction.New_configuration_2"))); //$NON-NLS-1$
        			ILaunchConfigurationTabGroup tabGroup = LaunchConfigurationPresentationManager.getDefault().getTabGroup(wc.getType(), getMode());
        			ConfigurationPage page = getConfigurationPage();
        			tabGroup.createTabs(page, getMode());
        			ILaunchConfigurationTab[] tabs = tabGroup.getTabs();
        			for (int i = 0; i < tabs.length; i++) {
        				ILaunchConfigurationTab tab = tabs[i];
        				tab.setLaunchConfigurationDialog(page);
        			}
        			tabGroup.setDefaults(wc);
        			tabGroup.dispose();
        			setLaunchConfiguration(wc.doSave());
        			// proceed to next page first, to avoid flash
        			getWizard().getContainer().showPage(page);
        			// then mark viewer as dirty and set selection
        			fConfigsViewer.refresh();
        			fConfigsViewer.setSelection(new StructuredSelection(getLaunchConfiguration()));
                } catch (CoreException ex) {
                    setErrorMessage(ex.getMessage());
                }
            }
        });
		
		deleteButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                deleteAction.run();
                fConfigsViewer.refresh();
            }
        });
		
		copyButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                copyAction.run();
                setLaunchConfiguration(copyAction.getDuplicate());
                // proceed to next page first, to avoid flash
    			getWizard().getContainer().showPage(getConfigurationPage());
    			// then mark viewer as dirty and set selection
    			fConfigsViewer.refresh();
    			fConfigsViewer.setSelection(new StructuredSelection(getLaunchConfiguration()));
            }
        });
		
		String text = getLaunchGroup().getTitle();
		if (text == null) {
		    text = LaunchConfigurationsMessages.getString("ApplicationPage.5"); //$NON-NLS-1$
		}
        setTitle(text);
		
		// initialize selection
		IStructuredSelection selection = getLaunchWizard().getInitialSelection();
		if (selection != null && !selection.isEmpty()) {
		    ILaunchConfigurationType type = null;
		    ILaunchConfiguration config = null;
		    Object first = selection.getFirstElement();
		    try {
			    if (first instanceof ILaunchConfiguration) {
			        config = (ILaunchConfiguration) first;
			        type = config.getType();
			    } else if (first instanceof ILaunchConfigurationType) {
			        type = (ILaunchConfigurationType) first;
			    }
			    if (type != null) {
			        fTypesViewer.setSelection(new StructuredSelection(type));
			        if (config == null) {
			            fTypesViewer.getControl().setFocus();
			        }
			    }
			    if (config != null) {
			        fConfigsViewer.setSelection(new StructuredSelection(config));
			        fConfigsViewer.getControl().setFocus();
			    }
		    } catch (CoreException ex) {
		        setErrorMessage(ex.getMessage());
		    }
		}
	}

}
