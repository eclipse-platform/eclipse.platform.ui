package org.eclipse.ui.internal.dialogs;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.util.ArrayList;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class PerspectivesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private IWorkbench workbench;
	private PerspectiveRegistry perspectiveRegistry;
	private ArrayList perspectives;
	private String defaultPerspectiveId;
	private ArrayList perspToDelete = new ArrayList();
	private ArrayList perspToRevert = new ArrayList();
	private List list;
	private Button revertButton;
	private Button deleteButton;
	private Button setDefaultButton;

	// widgets for open perspective mode;
	private Button openSameWindowButton;
	private Button openNewWindowButton;
	private int openPerspMode;
	
	// widgets for open view mode
	private int openViewMode;
	private Button openEmbedButton;
	private Button openFastButton;

	// widgets for perspective switching when creating new projects
	private Button switchOnNewProjectButton;
	private boolean switchOnNewProject;

//	private static final int LIST_WIDTH = 200;
//	private static final int LIST_HEIGHT = 200;

	// labels
	private static final String NEW_PROJECT_PERSPECTIVE_TITLE = WorkbenchMessages.getString("WorkbenchPreference.projectOptionsTitle"); //$NON-NLS-1$

	private static final String SWITCH_PERSPECTIVES_LABEL = WorkbenchMessages.getString("WorkbenchPreference.switch"); //$NON-NLS-1$

	private static final String OVM_TITLE = WorkbenchMessages.getString("OpenViewMode.title"); //$NON-NLS-1$
	private static final String OVM_EMBED = WorkbenchMessages.getString("OpenViewMode.embed"); //$NON-NLS-1$
	private static final String OVM_FAST = WorkbenchMessages.getString("OpenViewMode.fast"); //$NON-NLS-1$

	private static final String OPM_TITLE = WorkbenchMessages.getString("OpenPerspectiveMode.optionsTitle"); //$NON-NLS-1$
	private static final String OPM_SAME_WINDOW = WorkbenchMessages.getString("OpenPerspectiveMode.sameWindow"); //$NON-NLS-1$
	private static final String OPM_NEW_WINDOW = WorkbenchMessages.getString("OpenPerspectiveMode.newWindow"); //$NON-NLS-1$

	/**
	 * Creates the page's UI content.
	 */
	protected Control createContents(Composite parent) {
		WorkbenchHelp.setHelp(parent, IHelpContextIds.PERSPECTIVES_PREFERENCE_PAGE);

		Composite pageComponent = new Composite(parent, SWT.NULL);
		GridData data = new GridData(GridData.FILL_BOTH);
		pageComponent.setLayoutData(data);
		pageComponent.setFont(parent.getFont());

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 10;
		pageComponent.setLayout(layout);

		createOpenPerspButtonGroup(pageComponent);
		createOpenViewButtonGroup(pageComponent);
		createProjectPerspectiveGroup(pageComponent);
		createCustomizePerspective(pageComponent);

		return pageComponent;
	}
	
	/**
	 * Create a composite that contains buttons for selecting
	 * the open perspective mode.
	 * 
	 * @param composite Composite
	 */
	private void createOpenPerspButtonGroup(Composite composite) {
		
		Font font = composite.getFont();

		Group buttonComposite = new Group(composite, SWT.LEFT);
		buttonComposite.setText(OPM_TITLE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		buttonComposite.setLayout(layout);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		buttonComposite.setLayoutData(data);
		buttonComposite.setFont(font);

		openSameWindowButton = new Button(buttonComposite, SWT.RADIO);
		openSameWindowButton.setText(OPM_SAME_WINDOW);
		openSameWindowButton.setSelection(IPreferenceConstants.OPM_ACTIVE_PAGE == openPerspMode);
		openSameWindowButton.setFont(font);
		openSameWindowButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openPerspMode = IPreferenceConstants.OPM_ACTIVE_PAGE;
			}
		});

		openNewWindowButton = new Button(buttonComposite, SWT.RADIO);
		openNewWindowButton.setText(OPM_NEW_WINDOW);
		openNewWindowButton.setSelection(IPreferenceConstants.OPM_NEW_WINDOW == openPerspMode);
		openNewWindowButton.setFont(font);
		openNewWindowButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openPerspMode = IPreferenceConstants.OPM_NEW_WINDOW;
			}
		});
	}
	
	/**
	 * Create a composite that contains buttons for selecting open view mode.
	 * @param composite Composite
	 */
	private void createOpenViewButtonGroup(Composite composite) {
		
		Font font = composite.getFont();

		Group buttonComposite = new Group(composite, SWT.LEFT);
		buttonComposite.setText(OVM_TITLE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		buttonComposite.setLayout(layout);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		buttonComposite.setLayoutData(data);
		buttonComposite.setFont(font);

		openEmbedButton = new Button(buttonComposite, SWT.RADIO);
		openEmbedButton.setText(OVM_EMBED);
		openEmbedButton.setSelection(
			openViewMode == IPreferenceConstants.OVM_EMBED);
		openEmbedButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openViewMode = IPreferenceConstants.OVM_EMBED;
			}
		});
		openEmbedButton.setFont(font);

		// Open view as float no longer supported
		if (openViewMode == IPreferenceConstants.OVM_FLOAT)
			openViewMode = IPreferenceConstants.OVM_FAST;

		openFastButton = new Button(buttonComposite, SWT.RADIO);
		openFastButton.setText(OVM_FAST);
		openFastButton.setSelection(
			openViewMode == IPreferenceConstants.OVM_FAST);
		openFastButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openViewMode = IPreferenceConstants.OVM_FAST;
			}
		});
		openFastButton.setFont(font);
	}
	
	/**
	 * Create a composite that contains buttons for selecting the 
	 * preference opening new project selections. 
	 */
	private void createProjectPerspectiveGroup(Composite composite) {
		
		Font font = composite.getFont();

		Group buttonComposite = new Group(composite, SWT.LEFT | SWT.NULL);
		GridLayout layout = new GridLayout();
		buttonComposite.setLayout(layout);
		GridData data =
			new GridData(
				GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		buttonComposite.setLayoutData(data);
		buttonComposite.setText(NEW_PROJECT_PERSPECTIVE_TITLE);
		buttonComposite.setFont(font);

		// No switch button
		switchOnNewProjectButton = new Button(buttonComposite, SWT.CHECK | SWT.LEFT);
		switchOnNewProjectButton.setText(SWITCH_PERSPECTIVES_LABEL);
		switchOnNewProjectButton.setFont(buttonComposite.getFont());
		switchOnNewProjectButton.setSelection(switchOnNewProject);
		switchOnNewProjectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				switchOnNewProject = switchOnNewProjectButton.getSelection();
			}
		});
	}
	/**
	 * Create a table a 3 buttons to enable the user to manage customized
	 * perspectives.
	 */
	protected Composite createCustomizePerspective(Composite parent) {
		
		Font font = parent.getFont();

		// define container & its gridding
		Composite perspectivesComponent = new Composite(parent, SWT.NULL);
		perspectivesComponent.setLayoutData(new GridData(GridData.FILL_BOTH));
		perspectivesComponent.setFont(parent.getFont());

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		perspectivesComponent.setLayout(layout);

		// Add the label
		Label label = new Label(perspectivesComponent, SWT.LEFT);
		label.setText(WorkbenchMessages.getString("PerspectivesPreference.available")); //$NON-NLS-1$
		GridData data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		label.setFont(font);

		// Add perspective list.
		list = new List(perspectivesComponent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		list.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
		});
		
		list.setFont(font);

		data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		list.setLayoutData(data);

		// Populate the perspective list
		IPerspectiveDescriptor[] persps = perspectiveRegistry.getPerspectives();
		perspectives = new ArrayList(persps.length);
		for (int i = 0; i < persps.length; i++)
			perspectives.add(i, persps[i]);
		defaultPerspectiveId = perspectiveRegistry.getDefaultPerspective();
		updateList();

		// Create vertical button bar.
		Composite buttonBar =
			(Composite) createVerticalButtonBar(perspectivesComponent);
		data = new GridData(GridData.FILL_VERTICAL);
		buttonBar.setLayoutData(data);
		return perspectivesComponent;
	}

	/**
	 * Creates a new vertical button with the given id.
	 * <p>
	 * The default implementation of this framework method
	 * creates a standard push button, registers for selection events
	 * including button presses and help requests, and registers
	 * default buttons with its shell.
	 * The button id is stored as the buttons client data.
	 * </p>
	 *
	 * @param parent the parent composite
	 * @param buttonId the id of the button (see
	 *  <code>IDialogConstants.*_ID</code> constants 
	 *  for standard dialog button ids)
	 * @param label the label from the button
	 * @param defaultButton <code>true</code> if the button is to be the
	 *   default button, and <code>false</code> otherwise
	 */
	protected Button createVerticalButton(
		Composite parent,
		String label,
		boolean defaultButton) {
		Button button = new Button(parent, SWT.PUSH);

		button.setText(label);
		
		GridData data = setButtonLayoutData(button);
		data.horizontalAlignment = GridData.FILL;

		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				verticalButtonPressed(event.widget);
			}
		});
		button.setToolTipText(label);
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
		}
		button.setFont(parent.getFont());
		return button;
	}
	
	/**
	 * Creates and returns the vertical button bar.
	 *
	 * @param parent the parent composite to contain the button bar
	 * @return the button bar control
	 */
	protected Control createVerticalButtonBar(Composite parent) {
		// Create composite.
		Composite composite = new Composite(parent, SWT.NULL);

		// create a layout with spacing and margins appropriate for the font size.
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 5;
		layout.marginHeight = 0;
		layout.horizontalSpacing =
			convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing =
			convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		composite.setLayout(layout);
		composite.setFont(parent.getFont());

		// Add the buttons to the button bar.
		setDefaultButton = createVerticalButton(composite, WorkbenchMessages.getString("PerspectivesPreference.MakeDefault"), false); //$NON-NLS-1$
		setDefaultButton.setToolTipText(WorkbenchMessages.getString("PerspectivesPreference.MakeDefaultTip")); //$NON-NLS-1$

		revertButton = createVerticalButton(composite, WorkbenchMessages.getString("PerspectivesPreference.Reset"), false); //$NON-NLS-1$
		revertButton.setToolTipText(WorkbenchMessages.getString("PerspectivesPreference.ResetTip")); //$NON-NLS-1$

		deleteButton = createVerticalButton(composite, WorkbenchMessages.getString("PerspectivesPreference.Delete"), false); //$NON-NLS-1$
		deleteButton.setToolTipText(WorkbenchMessages.getString("PerspectivesPreference.DeleteTip")); //$NON-NLS-1$
		updateButtons();

		return composite;
	}
	
	/**
	 * @see IWorkbenchPreferencePage
	 */
	public void init(IWorkbench aWorkbench) {
		this.workbench = aWorkbench;
		this.perspectiveRegistry = (PerspectiveRegistry) workbench.getPerspectiveRegistry();
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		
		switchOnNewProject = 
			!IWorkbenchPreferenceConstants.NO_NEW_PERSPECTIVE.equals(
			getUIPublicPreferenceStore().getString(IWorkbenchPreferenceConstants.PROJECT_OPEN_NEW_PERSPECTIVE));
		openViewMode = store.getInt(IPreferenceConstants.OPEN_VIEW_MODE);
		openPerspMode = store.getInt(IPreferenceConstants.OPEN_PERSP_MODE);
	}
	
	/**
	 * Get the preference store for the API constants (those in
	 * IWorkbenchPreferenceConstants).
	 * @return IPreferenceStore
	 */

	private IPreferenceStore getUIPublicPreferenceStore() {
		AbstractUIPlugin uiPlugin =
			(AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
		return uiPlugin.getPreferenceStore();
	}
	
	/**
	 * The default button has been pressed. 
	 */
	protected void performDefaults() {
		//Project perspective preferences
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
				
		switchOnNewProject = !IWorkbenchPreferenceConstants.NO_NEW_PERSPECTIVE.equals(
			getUIPublicPreferenceStore().getDefaultString(IWorkbenchPreferenceConstants.PROJECT_OPEN_NEW_PERSPECTIVE));
		switchOnNewProjectButton.setSelection(switchOnNewProject);

		openViewMode = store.getDefaultInt(IPreferenceConstants.OPEN_VIEW_MODE);
		// Open view as float no longer supported
		if (openViewMode == IPreferenceConstants.OVM_FLOAT)
			openViewMode = IPreferenceConstants.OVM_FAST;
		openEmbedButton.setSelection(openViewMode == IPreferenceConstants.OVM_EMBED);
		openFastButton.setSelection(openViewMode == IPreferenceConstants.OVM_FAST);

		openPerspMode = store.getDefaultInt(IPreferenceConstants.OPEN_PERSP_MODE);
		openSameWindowButton.setSelection(IPreferenceConstants.OPM_ACTIVE_PAGE == openPerspMode);
		openNewWindowButton.setSelection(IPreferenceConstants.OPM_NEW_WINDOW == openPerspMode);
	}
	/*
	 * Delete the perspectives selected by the user if there is not
	 * opened instance of that perspective.
	 */

	private boolean deletePerspectives() {
		IWorkbenchWindow windows[] = workbench.getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			IWorkbenchPage pages[] = windows[i].getPages();
			for (int j = 0; j < pages.length; j++) {
				WorkbenchPage page = (WorkbenchPage)pages[j];
				for (int k = 0; k < perspToDelete.size(); k++) {
					IPerspectiveDescriptor desc = (IPerspectiveDescriptor)perspToDelete.get(k);				
					if(page.findPerspective(desc) != null) {
						MessageDialog.openInformation(
							getShell(),
							WorkbenchMessages.getString("PerspectivesPreference.cannotdelete.title"), //$NON-NLS-1$
							WorkbenchMessages.format("PerspectivesPreference.cannotdelete.message", new String[]{desc.getLabel()})); //$NON-NLS-1$
						return false;
					}
				}
			}
		}
		
		//Delete the perspectives from the registry
		perspectiveRegistry.deletePerspectives(perspToDelete);
		return true;
	}
	/**
	 * Apply the user's changes if any
	 */
	public boolean performOk() {
		// Set the default perspective
		if (!defaultPerspectiveId
			.equals(perspectiveRegistry.getDefaultPerspective()))
			perspectiveRegistry.setDefaultPerspective(defaultPerspectiveId);

		if(!deletePerspectives())
			return false;

		// Revert the perspectives
		perspectiveRegistry.revertPerspectives(perspToRevert);

		// Update perspective history.
		 ((Workbench) workbench).getPerspectiveHistory().refreshFromRegistry();

		// store the open new project perspective settings
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		String newProjectPerspectiveSetting;
		if(!switchOnNewProject)
			newProjectPerspectiveSetting = IWorkbenchPreferenceConstants.NO_NEW_PERSPECTIVE;
		else if(openPerspMode == IPreferenceConstants.OPM_NEW_WINDOW)
			newProjectPerspectiveSetting = IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW;
		else
			newProjectPerspectiveSetting = IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE;
		
		getUIPublicPreferenceStore().setValue(
			IWorkbenchPreferenceConstants.PROJECT_OPEN_NEW_PERSPECTIVE,
			newProjectPerspectiveSetting);

		// store the open view mode setting
		store.setValue(IPreferenceConstants.OPEN_VIEW_MODE, openViewMode);

		// store the open perspective mode setting
		store.setValue(IPreferenceConstants.OPEN_PERSP_MODE, openPerspMode);
		
		WorkbenchPlugin.getDefault().savePluginPreferences();
		return true;
	}
	
	/**
	 * Update the button enablement state.
	 */
	protected void updateButtons() {
		// Get selection.
		int index = list.getSelectionIndex();

		// Map it to the perspective descriptor	
		PerspectiveDescriptor desc = null;
		if (index > -1)
			desc = (PerspectiveDescriptor) perspectives.get(index);

		// Do enable.
		if (desc != null) {
			revertButton.setEnabled(
				desc.isPredefined()
					&& desc.hasCustomDefinition()
					&& !perspToRevert.contains(desc));
			deleteButton.setEnabled(!desc.isPredefined());
			setDefaultButton.setEnabled(true);
		} else {
			revertButton.setEnabled(false);
			deleteButton.setEnabled(false);
			setDefaultButton.setEnabled(false);
		}
	}
	
	/**
	 * Update the list items.
	 */
	protected void updateList() {
		list.removeAll();
		for (int i = 0; i < perspectives.size(); i++) {
			IPerspectiveDescriptor desc =
				(IPerspectiveDescriptor) perspectives.get(i);
			String label = desc.getLabel();
			if (desc.getId().equals(defaultPerspectiveId))
				label = WorkbenchMessages.format("PerspectivesPreference.defaultLabel", new Object[] { label }); //$NON-NLS-1$
			list.add(label, i);
		}
	}
	
	/**
	 * Notifies that this page's button with the given id has been pressed.
	 *
	 * @param buttonId the id of the button that was pressed (see
	 *  <code>IDialogConstants.*_ID</code> constants)
	 */
	protected void verticalButtonPressed(Widget button) {
		// Get selection.
		int index = list.getSelectionIndex();

		// Map it to the perspective descriptor	
		PerspectiveDescriptor desc = null;
		if (index > -1)
			desc = (PerspectiveDescriptor) perspectives.get(index);
		else
			return;

		// Take action.
		if (button == revertButton) {
			if (desc.isPredefined() && !perspToRevert.contains(desc)) {
				perspToRevert.add(desc);
			}
		} else if (button == deleteButton) {
			if (!desc.isPredefined() && !perspToDelete.contains(desc)) {
				perspToDelete.add(desc);
				perspToRevert.remove(desc);
				perspectives.remove(desc);
				updateList();
			}
		} else if (button == setDefaultButton) {
			defaultPerspectiveId = desc.getId();
			updateList();
			list.setSelection(index);
		}

		updateButtons();
	}
}