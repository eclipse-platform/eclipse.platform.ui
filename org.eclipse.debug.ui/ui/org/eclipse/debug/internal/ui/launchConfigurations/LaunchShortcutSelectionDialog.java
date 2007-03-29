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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DefaultLabelProvider;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;

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
public class LaunchShortcutSelectionDialog extends ListDialog {

	private static final String DIALOG_SETTINGS = IDebugUIConstants.PLUGIN_ID + ".SELECT_LAUNCH_SHORTCUT_DIALOG"; //$NON-NLS-1$;
	
	/**
	 * The list of input for the dialog
	 */
	private String fMode = null;
	private IResource fResource = null;
	private Text fDescriptionText = null;
	
	/**
	 * Constructor
	 * @param input
	 * @param resource
	 * @param mode
	 */
	public LaunchShortcutSelectionDialog(IResource resource, String mode) {
		super(DebugUIPlugin.getShell());
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fResource = resource;
		fMode = mode;
		ILaunchMode lmode = DebugPlugin.getDefault().getLaunchManager().getLaunchMode(fMode);
		String modename = fMode;
		if (lmode != null) {
			modename = DebugUIPlugin.removeAccelerators(lmode.getLabel());
		}
		setTitle(MessageFormat.format(LaunchConfigurationsMessages.LaunchShortcutSelectionDialog_0, new String[] {modename}));
		setAddCancelButton(true);
		if(fResource == null) {
			setMessage(MessageFormat.format(LaunchConfigurationsMessages.LaunchShortcutSelectionDialog_4, new String[] {modename.toLowerCase()}));
		}
		else {
			setMessage(MessageFormat.format(LaunchConfigurationsMessages.LaunchShortcutSelectionDialog_1, new String[] {modename.toLowerCase(), fResource.getName()}));
		}
		setLabelProvider(new DefaultLabelProvider());
		setContentProvider(new ArrayContentProvider());
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite comp = (Composite) super.createContents(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(comp, IDebugHelpContextIds.SELECT_LAUNCH_METHOD_DIALOG);
		return comp;
	}

	/**
	 * @see org.eclipse.ui.dialogs.SelectionDialog#getDialogBoundsSettings()
	 */
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings settings = DebugUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(DIALOG_SETTINGS);
		if (section == null) {
			section = settings.addNewSection(DIALOG_SETTINGS);
		} 
		return section;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
	 */
	protected Point getInitialSize() {
		IDialogSettings settings = getDialogBoundsSettings();
		if(settings != null) {
			try {
				int width = settings.getInt("DIALOG_WIDTH"); //$NON-NLS-1$
				int height = settings.getInt("DIALOG_HEIGHT"); //$NON-NLS-1$
				if(width > 0 & height > 0) {
					return new Point(width, height);
				}
			}
			catch (NumberFormatException nfe) {
				return new Point(450, 450);
			}
		}
		return new Point(450, 450);
	}
	
	/**
	 * @see org.eclipse.ui.dialogs.ListDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getOkButton().setEnabled(false);
		getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				getOkButton().setEnabled(!event.getSelection().isEmpty());
			}	
		});
	}
	
	/**
	 * @see org.eclipse.ui.dialogs.ListDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite container) {
		Composite comp = (Composite) super.createDialogArea(container);
		if(fResource != null) {
			List input = new ArrayList(DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchShortcuts(fResource));
			getTableViewer().setInput(input);
		}
		Group group = SWTFactory.createGroup(comp, LaunchConfigurationsMessages.LaunchShortcutSelectionDialog_2, 1, 1, GridData.FILL_BOTH);
		GridData gd = (GridData) group.getLayoutData();
		gd.heightHint = 175;
		fDescriptionText = SWTFactory.createText(group, SWT.WRAP | SWT.READ_ONLY, 1, GridData.FILL_BOTH);
		fDescriptionText.setBackground(group.getBackground());
		getTableViewer().getTable().addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				Object o = e.item.getData();
				if(o instanceof LaunchShortcutExtension) {
					String txt = ((LaunchShortcutExtension)o).getShortcutDescription(fMode);
					fDescriptionText.setText((txt == null ? LaunchConfigurationsMessages.LaunchShortcutSelectionDialog_3 : txt)); 
				}
			}
		});
		return comp;
	}
}
