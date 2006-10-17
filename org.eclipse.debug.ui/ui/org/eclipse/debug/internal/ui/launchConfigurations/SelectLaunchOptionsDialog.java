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

import java.util.Set;

import org.eclipse.debug.internal.core.LaunchDelegate;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
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
 * the plugin that provides either a launch option or a contributed launch delegate is no longer available.
 * The user can select one of the launch mode/option configuration sform this dialog and repair the option 
 * configuration state of the the current launch configuration
 * 
 *  @since 3.3
 *  
 *  EXPERIMENTAL
 */
public class SelectLaunchOptionsDialog extends SelectionDialog {

	/**
	 * Builds labels for list control of the form: Mode + (no options) | [optionslist]
	 */
	class OptionsLabelProvider implements ILabelProvider {
		public Image getImage(Object element) {return null;}
		public String getText(Object element) {
			LaunchDelegate del = (LaunchDelegate) element;
			Set set = del.getOptions();
			return del.getName() + LaunchConfigurationsMessages.SelectLaunchOptionsDialog_5 + fMode + (set.isEmpty() ? LaunchConfigurationsMessages.SelectLaunchOptionsDialog_0 : LaunchConfigurationsMessages.SelectLaunchOptionsDialog_1 + set);
		}
		public void addListener(ILabelProviderListener listener) {}
		public void dispose() {}
		public boolean isLabelProperty(Object element, String property) {return false;}
		public void removeListener(ILabelProviderListener listener) {}
	}
	
	private static final String SETTINGS_ID = IDebugUIConstants.PLUGIN_ID + ".SELECT_LAUNCH_OPTIONS_DIALOG"; //$NON-NLS-1$
	
	private LaunchDelegate[] fDelegates = null;
	private String fMode = null;
	private Object[] fResult = null;
	private CheckboxTableViewer fTableViewer = null;
	private Table fTable  = null;
	
	/**
	 * Constructor
	 * @param parentShell the parent shell
	 * @param message the message for the dialog
	 * @param options the listing of arrays of options (each entry in the list must be an <code>Set</code> of options)
	 */
	public SelectLaunchOptionsDialog(Shell parentShell, String mode, LaunchDelegate[] delegates) {
		super(parentShell);
		super.setMessage(LaunchConfigurationsMessages.SelectLaunchOptionsDialog_2);
		super.setTitle(LaunchConfigurationsMessages.SelectLaunchOptionsDialog_3);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fDelegates = delegates;
		fMode = mode;
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
		fTableViewer.setInput(fDelegates);
		fTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				fTableViewer.setAllChecked(false);
				fTableViewer.setChecked(event.getElement(), true);
			}
		});
		Dialog.applyDialogFont(comp);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(comp, IDebugHelpContextIds.SELECT_LAUNCH_OPTIONS_DIALOG);
		return comp;
	}
	
	/**
	 * @see org.eclipse.ui.dialogs.SelectionDialog#getResult()
	 */
	public Object[] getResult() {
		return fResult;
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		Object[] o =  fTableViewer.getCheckedElements();
		if(o.length > 0) {
			fResult = ((LaunchDelegate)o[0]).getOptions().toArray();
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
