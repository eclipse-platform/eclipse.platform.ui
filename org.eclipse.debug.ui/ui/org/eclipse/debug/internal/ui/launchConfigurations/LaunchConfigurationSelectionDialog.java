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

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DefaultLabelProvider;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;

/**
 * This class provides a dialog for selecting a given launch configuration from a listing
 * 
 * @since 3.3.0
 * CONTEXTLAUNCHING
 */
public class LaunchConfigurationSelectionDialog extends ListDialog {

	private static final String DIALOG_SETTINGS = IDebugUIConstants.PLUGIN_ID + ".SELECT_LAUNCH_CONFIGURATION_DIALOG"; //$NON-NLS-1$;
	
	/**
	 * Constructor
	 * @param parent
	 */
	public LaunchConfigurationSelectionDialog(Shell parent) {
		super(parent);
		setTitle(LaunchConfigurationsMessages.LaunchConfigurationSelectionDialog_0);
		setMessage(LaunchConfigurationsMessages.LaunchConfigurationSelectionDialog_1);
		setLabelProvider(new DefaultLabelProvider());
		setContentProvider(new ArrayContentProvider());
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite comp = (Composite) super.createContents(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(comp, IDebugHelpContextIds.SELECT_LAUNCH_CONFIGURATION_DIALOG);
		return comp;
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
	
}
