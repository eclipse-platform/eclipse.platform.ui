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
package org.eclipse.ant.internal.ui.launchConfigurations;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsBuilderTab;
import org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsLaunchConfigurationMessages;

public class AntBuilderTab extends ExternalToolsBuilderTab {
	
	private Button fAfterCleanTarget;
	private Button fManualBuildTarget;
	private Button fAutoBuildTarget;
	private Button fDuringBuildTarget;
	
	protected SelectionListener fSelectionListener= new SelectionAdapter() {
		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			String attribute= null;
			if (source == fAfterCleanTarget) {
				setTargets();
			} else if (source == fManualBuildTarget) {
				setTargets();
			} else if (source == fAutoBuildTarget) {
				setTargets();
			} else if (source == fDuringBuildTarget) {
				setTargets();
			}
			setTargets();
		}
	};
	
	public AntBuilderTab() {
		super();
	}
	
	protected void createBuildScheduleComponent(Composite parent) {
		createVerticalSpacer(parent, 2);
		Label label= new Label(parent, SWT.NONE);
		label.setText(ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.Run_this_builder_for__1")); //$NON-NLS-1$
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		label.setFont(parent.getFont());
		afterClean= createButton(parent, selectionListener, ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.&Full_builds_2"), ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.Full"), 1); //$NON-NLS-1$ //$NON-NLS-2$
		
		fAfterCleanTarget = createPushButton(parent, "Set Target(s)...", null);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		fAfterCleanTarget.setLayoutData(gd);
		fAfterCleanTarget.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setTargets();
			}
		});
		
		manualBuild= createButton(parent, selectionListener, ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.&Incremental_builds_4"), ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.Inc"), 1); //$NON-NLS-1$ //$NON-NLS-2$
		fManualBuildTarget = createPushButton(parent, "Set Target(s)...", null);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		fManualBuildTarget.setLayoutData(gd);
		fManualBuildTarget.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setTargets();
			}
		});
		autoBuildButton= createButton(parent, selectionListener, ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.&Auto_builds_(Not_recommended)_6"), ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.Auto"), 1); //$NON-NLS-1$ //$NON-NLS-2$
		fAutoBuildTarget = createPushButton(parent, "Set Target(s)...", null);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		fAutoBuildTarget.setLayoutData(gd);
		fAutoBuildTarget.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setTargets();
			}
		});
		fDuringClean= createButton(parent, selectionListener, "During a Clean", "Runs when a clean has been initiated", 1);
		fDuringBuildTarget = createPushButton(parent, "Set Target(s)...", null);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		fDuringBuildTarget.setLayoutData(gd);
		fDuringBuildTarget.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setTargets();
			}
		});
		createVerticalSpacer(parent, 2);
		
		workingSetButton= createButton(parent, selectionListener, ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.workingSet_label"), ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.workingSet_tooltip"), 1); //$NON-NLS-1$ //$NON-NLS-2$
		specifyResources= createPushButton(parent, ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.13"), null); //$NON-NLS-1$
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		specifyResources.setLayoutData(gd);
		specifyResources.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
		//		selectResources();
			}
		});
	}

	/**
	 * 
	 */
	protected void setTargets() {
		SetTargetsDialog dialog= new SetTargetsDialog(getShell(), (ILaunchConfigurationWorkingCopy) fConfiguration);
		dialog.open();
		
	}
}
