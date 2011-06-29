/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     dakshinamurthy.karra@gmail.com - bug 165371
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.launchConfigurations;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.externaltools.internal.launchConfigurations.ExternalToolsCoreUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IExternalToolsHelpContextIds;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * A launch configuration tab which allows the user to specify
 * which resources should be built before a build (a build scope)
 * <p>
 * This class may be instantiated; this class is not intended
 * to be sub-classed.
 * </p>
 * A generalized version of AntBuildTab which was removed after the work of bug 165371
 * @since 3.4
 */
public class ExternalToolsBuildTab extends AbstractLaunchConfigurationTab {
	// Check Buttons
	private Button fBuildButton;
	
	// Group box
	private Group fGroup;
	
	// Radio Buttons
	private Button fProjectButton;
	private Button fSpecificProjectsButton;
	private Button fWorkspaceButton;
	
	// Push Button
	private Button fSelectButton;
	
	// whether to include referenced projects
	private Button fReferencedProjects;
	
	// projects to build (empty if none)
	private List fProjects = new ArrayList();
	
	class ProjectsContentProvider implements IStructuredContentProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return ((IWorkspace)inputElement).getRoot().getProjects();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
	}
	
	/**
	 * Constructor
	 */
	public ExternalToolsBuildTab() {
		setHelpContextId(IExternalToolsHelpContextIds.EXTERNAL_TOOLS_LAUNCH_CONFIGURATION_DIALOG_BUILD_TAB);
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), getHelpContextId());
		GridLayout layout = new GridLayout();
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gd);
		mainComposite.setFont(parent.getFont());
		
		fBuildButton = createCheckButton(mainComposite, ExternalToolsLaunchConfigurationMessages.ExternalToolsBuildTab_1);
		fBuildButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateEnabledState();
				updateLaunchConfigurationDialog();
			}
		});
		
		fGroup = new Group(mainComposite, SWT.NONE);
		fGroup.setFont(mainComposite.getFont());
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		fGroup.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fGroup.setLayoutData(gd);

		SelectionAdapter adapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (((Button)e.getSource()).getSelection()) {
					updateEnabledState();
					updateLaunchConfigurationDialog();
				}
			}
		};
		
		fWorkspaceButton = createRadioButton(fGroup, ExternalToolsLaunchConfigurationMessages.ExternalToolsBuildTab_2);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fWorkspaceButton.setLayoutData(gd);
		fWorkspaceButton.addSelectionListener(adapter);
		
		fProjectButton = createRadioButton(fGroup, ExternalToolsLaunchConfigurationMessages.ExternalToolsBuildTab_3);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fProjectButton.setLayoutData(gd);		
		fProjectButton.addSelectionListener(adapter);
				
		fSpecificProjectsButton = createRadioButton(fGroup, ExternalToolsLaunchConfigurationMessages.ExternalToolsBuildTab_4);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		fSpecificProjectsButton.setLayoutData(gd);
		fSpecificProjectsButton.addSelectionListener(adapter);		
		
		fSelectButton = createPushButton(fGroup, ExternalToolsLaunchConfigurationMessages.ExternalToolsBuildTab_5, null);
		gd = (GridData)fSelectButton.getLayoutData();
		gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_END;
		fSelectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectResources();
			}
		});
		
		createVerticalSpacer(mainComposite, 1);
		fReferencedProjects = createCheckButton(mainComposite, ExternalToolsLaunchConfigurationMessages.ExternalToolsBuildTab_6);
	}

	/**
	 * Prompts the user to select the projects to build.
	 */
	private void selectResources() {
		ListSelectionDialog dialog = new ListSelectionDialog(getShell(), ResourcesPlugin.getWorkspace(), new ProjectsContentProvider(), new WorkbenchLabelProvider(), ExternalToolsLaunchConfigurationMessages.ExternalToolsBuildTab_7);
		dialog.setInitialElementSelections(fProjects);
		if (dialog.open() == Window.CANCEL) {
			return;
		}
		Object[] res = dialog.getResult();
		fProjects = new ArrayList(res.length);
		for (int i = 0; i < res.length; i++) {
			fProjects.add(res[i]);
		}
		updateLaunchConfigurationDialog();
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
		updateScope(configuration);
		updateReferencedProjects(configuration);
		updateEnabledState();		
	}
	
	private void updateReferencedProjects(ILaunchConfiguration configuration) {
		boolean ref = false;
		try {
			ref = configuration.getAttribute(IExternalToolConstants.ATTR_INCLUDE_REFERENCED_PROJECTS, true);
		} catch (CoreException e) {
			ExternalToolsPlugin.getDefault().log("Exception reading launch configuration", e); //$NON-NLS-1$
		}
		fReferencedProjects.setSelection(ref);
	}

	/**
	 * Updates the tab to display the build scope specified by the launch config
	 */
	private void updateScope(ILaunchConfiguration configuration) {
		String scope = null;
		try {
			scope= configuration.getAttribute(IExternalToolConstants.ATTR_BUILD_SCOPE, (String)null);
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log("Exception reading launch configuration", ce); //$NON-NLS-1$
		}
		fBuildButton.setSelection(scope != null);
		fWorkspaceButton.setSelection(false);
		fProjectButton.setSelection(false);
		fSpecificProjectsButton.setSelection(false);
		fProjects.clear();
		if (scope == null) {
			// select the workspace by default
			fBuildButton.setSelection(true);
			fWorkspaceButton.setSelection(true);
		} else {
			if (scope.equals("${none}")) { //$NON-NLS-1$
				fBuildButton.setSelection(false);
			} else if (scope.equals("${project}")) { //$NON-NLS-1$
				fProjectButton.setSelection(true);
			} else if (scope.startsWith("${projects:")) { //$NON-NLS-1$
				fSpecificProjectsButton.setSelection(true);
				IProject[] projects = getBuildProjects(configuration, IExternalToolConstants.ATTR_BUILD_SCOPE);
				fProjects = new ArrayList(projects.length);
				for (int i = 0; i < projects.length; i++) {
					fProjects.add(projects[i]);
				}
			}
		}
	}
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String scope = generateScopeMemento();
		configuration.setAttribute(IExternalToolConstants.ATTR_BUILD_SCOPE, scope);
		if (fReferencedProjects.getSelection()) {
			// default is true
			configuration.setAttribute(IExternalToolConstants.ATTR_INCLUDE_REFERENCED_PROJECTS, (String)null);
		} else {
			configuration.setAttribute(IExternalToolConstants.ATTR_INCLUDE_REFERENCED_PROJECTS, false);
		}
	}

	/**
	 * Generates a memento for the build scope.
	 */
	private String generateScopeMemento() {
		if (fBuildButton.getSelection()) {
			if (fWorkspaceButton.getSelection()) {
				return null;
			}
			if (fProjectButton.getSelection()) {
				return "${project}"; //$NON-NLS-1$
			}
			if (fSpecificProjectsButton.getSelection()) {
				return getBuildScopeAttribute(fProjects);
			}
			return null;
			
		}
		return "${none}"; //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return ExternalToolsLaunchConfigurationMessages.ExternalToolsBuildTab_8;
	}
	
	/**
	 * Updates the enablement state of the fields.
	 */
	private void updateEnabledState() {
		boolean enabled= fBuildButton.getSelection();
		fGroup.setEnabled(enabled);
		fWorkspaceButton.setEnabled(enabled);
		fProjectButton.setEnabled(enabled);
		fSpecificProjectsButton.setEnabled(enabled);
		fSelectButton.setEnabled(enabled && fSpecificProjectsButton.getSelection());
		if (!enabled) {
			super.setErrorMessage(null);
		}
		if (enabled) {
			if (!fWorkspaceButton.getSelection() && !fProjectButton.getSelection() &&
					!fSpecificProjectsButton.getSelection()) {
				fWorkspaceButton.setSelection(true);
			}
		}
		fReferencedProjects.setEnabled(fBuildButton.getSelection() && (fProjectButton.getSelection() || fSpecificProjectsButton.getSelection()));
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return ExternalToolsImages.getImage(org.eclipse.ui.externaltools.internal.model.IExternalToolConstants.IMG_TAB_BUILD);
	}

	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		setMessage(null);
		if (fBuildButton.getSelection() && fSpecificProjectsButton.getSelection() && fProjects.isEmpty()) {
			setErrorMessage(ExternalToolsLaunchConfigurationMessages.ExternalToolsBuildTab_9);
			return false;
		}
		return true;
	}
	
	/**
	 * Returns a collection of projects referenced by a build scope attribute.
	 * 
	 * @return collection of projects referred to by configuration
	 */
	public static IProject[] getBuildProjects(ILaunchConfiguration configuration, String buildScopeId) {
		return ExternalToolsCoreUtil.getBuildProjects(configuration,
				buildScopeId);

	}
	
	/**
	 * Whether referenced projects should be considered when building. Only valid
	 * when a set of projects is to be built.
	 * 
	 * @param configuration
	 * @return whether referenced projects should be considerd when building
	 * @throws CoreException if unable to access the associated attribute
	 */
	public static boolean isIncludeReferencedProjects(ILaunchConfiguration configuration, String includeReferencedProjectsId) throws CoreException {
		return ExternalToolsCoreUtil.isIncludeReferencedProjects(configuration,
				includeReferencedProjectsId);
	}
	
	/**
	 * Creates and returns a memento for the given project set, to be used as a
	 * build scope attribute.
	 * 
	 * @param projects list of projects
	 * @return an equivalent refresh attribute
	 */
	public static String getBuildScopeAttribute(List projects) {
		StringBuffer buf = new StringBuffer();
		buf.append("${projects:"); //$NON-NLS-1$
		Iterator iterator = projects.iterator();
		while (iterator.hasNext()) {
			IProject project = (IProject) iterator.next();
			buf.append(project.getName());
			if (iterator.hasNext()) {
				buf.append(","); //$NON-NLS-1$
			}
		}
		buf.append("}"); //$NON-NLS-1$
		return buf.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		// do nothing on activation
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#deactivated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
		// do nothing on deactivation
	}
}
