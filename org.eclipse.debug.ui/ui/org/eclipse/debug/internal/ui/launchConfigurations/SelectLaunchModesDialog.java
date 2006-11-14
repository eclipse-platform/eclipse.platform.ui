/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * This class provides a dialog to present the user with a list of of viable launch options in the event 
 * the plug-in that provides either a launch option or a contributed launch delegate is no longer available.
 * The user can select one of the launch mode/option configuration from this dialog and repair the option 
 * configuration state of the the current launch configuration
 * 
 *  @since 3.3
 *  
 *  EXPERIMENTAL
 */
public class SelectLaunchModesDialog extends SelectionDialog {

	/**
	 * Builds labels for list control
	 */
	class OptionsLabelProvider implements ILabelProvider {
		public Image getImage(Object element) {return null;}
		public String getText(Object element) {
			Set modes = (Set) element;
			List names = LaunchConfigurationPresentationManager.getDefault().getLaunchModeNames(modes);
			return names.toString();
		}
		public void addListener(ILabelProviderListener listener) {}
		public void dispose() {}
		public boolean isLabelProperty(Object element, String property) {return false;}
		public void removeListener(ILabelProviderListener listener) {}
	}
	
	private static final String SETTINGS_ID = IDebugUIConstants.PLUGIN_ID + ".SELECT_LAUNCH_MODES_DIALOG"; //$NON-NLS-1$
	
	private CheckboxTableViewer fTableViewer = null;
	private Table fTable  = null;
	private List fValidModes = null;
	
	/**
	 * Constructor
	 * @param parentShell the parent shell
	 * @param mode the current mode context
	 * @param configuration the current launch configuration context
	 * 
	 * @throws CoreException
	 */
	public SelectLaunchModesDialog(Shell parentShell, String mode, ILaunchConfiguration configuration) throws CoreException {
		super(parentShell);
		super.setMessage(LaunchConfigurationsMessages.SelectLaunchOptionsDialog_2);
		super.setTitle(LaunchConfigurationsMessages.SelectLaunchOptionsDialog_3);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fValidModes = new ArrayList();
		Set modes = configuration.getType().getSupportedModeCombinations();
		Set modeset = null;
		for(Iterator iter = modes.iterator(); iter.hasNext();) {
			modeset = (Set) iter.next();
			if(modeset.contains(mode)) {
				fValidModes.add(modeset);
			}
		}
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		initializeDialogUnits(parent);
		Composite comp = (Composite) super.createDialogArea(parent);
		SWTUtil.createLabel(comp, LaunchConfigurationsMessages.SelectLaunchOptionsDialog_4, 1);
		fTable = new Table(comp, SWT.BORDER | SWT.SINGLE | SWT.CHECK);
		fTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		fTableViewer = new CheckboxTableViewer(fTable);
		fTableViewer.setLabelProvider(new OptionsLabelProvider());
		fTableViewer.setContentProvider(new ArrayContentProvider());
		fTableViewer.setInput(fValidModes.toArray());
		fTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				fTableViewer.setAllChecked(false);
				fTableViewer.setChecked(event.getElement(), true);
				getButton(IDialogConstants.OK_ID).setEnabled(true);
			}
		});
		Dialog.applyDialogFont(comp);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(comp, IDebugHelpContextIds.SELECT_LAUNCH_MODES_DIALOG);
		return comp;
	}
	
	/**
	 * @see org.eclipse.ui.dialogs.SelectionDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		Object[] o =  fTableViewer.getCheckedElements();
		if(o.length > 0) {
			setResult(Arrays.asList(o));
		}
		super.okPressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.SelectionDialog#getDialogBoundsSettings()
	 */
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings settings = DebugUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(SETTINGS_ID);
		if (section == null) {
			section = settings.addNewSection(SETTINGS_ID);
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
				return new Point(350, 400);
			}
		}
		return new Point(350, 400);
	}

}
