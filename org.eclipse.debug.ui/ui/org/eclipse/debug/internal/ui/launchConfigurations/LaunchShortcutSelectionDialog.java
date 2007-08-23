/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.internal.ui.AbstractDebugListSelectionDialog;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.ibm.icu.text.MessageFormat;

/**
 * Specialized dialog for showing/selecting a specific launch shortcut extension, and allowing it
 * to be marked to be set as the default
 * 
 * @see {@link org.eclipse.debug.internal.ui.actions.ContextLaunchingAction}
 * 
 * @since 3.3
 * CONTEXTLAUNCHING
 */
public class LaunchShortcutSelectionDialog extends AbstractDebugListSelectionDialog {

	private static final String DIALOG_SETTINGS = IDebugUIConstants.PLUGIN_ID + ".SELECT_LAUNCH_SHORTCUT_DIALOG"; //$NON-NLS-1$;
	
	/**
	 * The list of input for the dialog
	 */
	private String fModeName = null;
	private String fMode = null;
	private IResource fResource = null;
	private List fShortcuts = null;
	private Text fDescriptionText = null;
	
	/**
	 * Constructor
	 * @param input
	 * @param resource
	 * @param mode
	 */
	public LaunchShortcutSelectionDialog(List shortcuts, IResource resource, String mode) {
		super(DebugUIPlugin.getShell());
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fShortcuts = shortcuts;
		fResource = resource;
		fMode = mode;
		ILaunchMode lmode = DebugPlugin.getDefault().getLaunchManager().getLaunchMode(mode);
		fModeName = mode;
		if (lmode != null) {
			fModeName = DebugUIPlugin.removeAccelerators(lmode.getLabel());
		}
		setTitle(MessageFormat.format(LaunchConfigurationsMessages.LaunchShortcutSelectionDialog_0, new String[] {fModeName}));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.SELECT_LAUNCH_METHOD_DIALOG;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getDialogSettingsId()
	 */
	protected String getDialogSettingsId() {
		return DIALOG_SETTINGS;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugListSelectionDialog#addViewerListeners(org.eclipse.jface.viewers.StructuredViewer)
	 */
	protected void addViewerListeners(StructuredViewer viewer) {
		super.addViewerListeners(viewer);
		viewer.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (!selection.isEmpty()) {
					LaunchShortcutExtension shortcutSource = (LaunchShortcutExtension) ((IStructuredSelection)selection).getFirstElement();
					String description = shortcutSource.getShortcutDescription(fMode);
					fDescriptionText.setText((description == null ? LaunchConfigurationsMessages.LaunchShortcutSelectionDialog_3 : description)); 
				}
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#addCustomFooterControls(org.eclipse.swt.widgets.Composite)
	 */
	protected void addCustomFooterControls(Composite parent) {
		super.addCustomFooterControls(parent);
		Group group = SWTFactory.createGroup(parent, LaunchConfigurationsMessages.LaunchShortcutSelectionDialog_2, 1, 1, GridData.FILL_BOTH);
		GridData gd = (GridData) group.getLayoutData();
		gd.heightHint = 100;
		fDescriptionText = SWTFactory.createText(group, SWT.WRAP | SWT.READ_ONLY, 1, GridData.FILL_HORIZONTAL);
		fDescriptionText.setBackground(group.getBackground());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getViewerInput()
	 */
	protected Object getViewerInput() {
		return fShortcuts;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getViewerLabel()
	 */
	protected String getViewerLabel() {
		if(fResource == null) {
			return MessageFormat.format(LaunchConfigurationsMessages.LaunchShortcutSelectionDialog_4, new String[] {fModeName.toLowerCase()});
		}
		else {
			return MessageFormat.format(LaunchConfigurationsMessages.LaunchShortcutSelectionDialog_1, new String[] {fModeName.toLowerCase(), fResource.getName()});
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugListSelectionDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setEnabled(!getViewer().getSelection().isEmpty());
	}
	
}
