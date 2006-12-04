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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Thsi dialog is used to select a preferred launcher, and alos provides access to the 
 * worspace defaults for preferred launchers
 * 
 *  @since 3.3
 *  EXPERIMENTAL
 */
public class SelectLaunchersDialog extends AbstractDebugSelectionDialog {

	/**
	 * Builds labels for table control
	 */
	class DelegatesLabelProvider implements ILabelProvider {
		public Image getImage(Object element) {return null;}
		public String getText(Object element) {
			if(element instanceof ILaunchDelegate) {
				ILaunchDelegate ldp = (ILaunchDelegate) element;
				String name = ldp.getName();
				if(name == null) {
					name = ldp.getContributorName();
				}
				return name;
			}
			return element.toString();
		}
		public void addListener(ILabelProviderListener listener) {}
		public void dispose() {}
		public boolean isLabelProperty(Object element, String property) {return false;}
		public void removeListener(ILabelProviderListener listener) {}
	}
	
	private Text fDescriptionText = null;
	private ILaunchDelegate[] fDelegates = null;
	private Button fUseSystemLauncher = null;
	private ILaunchConfigurationWorkingCopy fConfiguration = null;
	private String fLaunchMode = null;
	
	/**
	 * Constructor
	 * @param parentShell
	 */
	public SelectLaunchersDialog(Shell parentShell, ILaunchDelegate[] delegates, ILaunchConfigurationWorkingCopy configuration, String launchmode) {
		super(parentShell);
		super.setTitle(LaunchConfigurationsMessages.SelectLaunchersDialog_0);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fDelegates = delegates;
		fConfiguration = configuration;
		fLaunchMode = launchmode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getDialogSettingsId()
	 */
	protected String getDialogSettingsId() {
		return IDebugUIConstants.PLUGIN_ID + ".SELECT_LAUNCHERS_DIALOG"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.SELECT_LAUNCHERS_DIALOG;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getLabelProvider()
	 */
	protected IBaseLabelProvider getLabelProvider() {
		return new DelegatesLabelProvider();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getViewerInput()
	 */
	protected Object getViewerInput() {
		return fDelegates;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#addCustomHeaderControls(org.eclipse.swt.widgets.Composite)
	 */
	protected void addCustomHeaderControls(Composite parent) {
		SWTUtil.createWrapLabel(parent, LaunchConfigurationsMessages.SelectLaunchersDialog_2, 1);
		Link link = new Link(parent, SWT.WRAP);
		link.setText(LaunchConfigurationsMessages.SelectLaunchersDialog_4);
		link.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		link.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				SWTUtil.showPreferencePage("org.eclipse.debug.ui.LaunchDelegatesPreferencePage"); //$NON-NLS-1$
				if(!fUseSystemLauncher.getSelection()) {
					resetDelegate();
				}
			}
		});
		fUseSystemLauncher = SWTUtil.createCheckButton(parent, LaunchConfigurationsMessages.SelectLaunchersDialog_1, null, true);
		fUseSystemLauncher.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				boolean checked = ((Button)e.widget).getSelection();
				fTable.setEnabled(checked);
				resetDelegate();
			}
		});
	}
	
	/**
	 * Returns the currently checked launch delegate
	 * @return the currently selected launch delegate or <code>null</code> if none are checked
	 */
	protected ILaunchDelegate getSelectedDelegate() {
		Object[] checked = fTableViewer.getCheckedElements();
		if(checked.length > 0) {
			return (ILaunchDelegate) checked[0];
		}
		return null;
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		ILaunchDelegate delegate = null;
		Set modes = getCurrentModeSet();
		if(fUseSystemLauncher.getSelection()) {
			delegate = getSelectedDelegate();	
			if(delegate != null) {
				fConfiguration.setPreferredLaunchDelegate(modes, delegate.getId());
			}
		}
		else {
			fConfiguration.setPreferredLaunchDelegate(modes, null);
		}
		if(fConfiguration.isDirty()) {
			try {
				fConfiguration.doSave();
			} 
			catch (CoreException e) {DebugUIPlugin.log(e);}
		}
		super.okPressed();
	}

	/**
	 * Resets the selected and checked delegate in the preferred launcher view part to be the one from the workspace
	 */
	private void resetDelegate() {
		if(!fUseSystemLauncher.getSelection()) {
			try {
				ILaunchDelegate preferred = fConfiguration.getType().getPreferredDelegate(getCurrentModeSet());
				if(preferred != null) {
					fTableViewer.setSelection(new StructuredSelection(preferred));
					fTableViewer.setCheckedElements(new Object[] {preferred});
				}
				else {
					fTableViewer.setSelection(new StructuredSelection());
					fTableViewer.setAllChecked(false);
				}
			}
			catch (CoreException ce) {DebugUIPlugin.log(ce);}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#addCustomFooterControls(org.eclipse.swt.widgets.Composite)
	 */
	protected void addCustomFooterControls(Composite parent) {
		Group group = SWTUtil.createGroup(parent, LaunchConfigurationsMessages.SelectLaunchersDialog_5, 1, 1, GridData.FILL_BOTH);
		fDescriptionText = SWTUtil.createText(group, SWT.WRAP | SWT.READ_ONLY, 1, GridData.FILL_BOTH);
		fDescriptionText.setBackground(group.getBackground());
	}

	/**
	 * @return the complete set of modes that the associated launch configuration is concerned with
	 */
	protected Set getCurrentModeSet() {
		Set modes = new HashSet();
		try {
			modes = fConfiguration.getModes();
			modes.add(fLaunchMode);
		}
		catch (CoreException ce) {DebugUIPlugin.log(ce);}
		return modes;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#initializeControls()
	 */
	protected void initializeControls() {
		fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection ss = (IStructuredSelection) event.getSelection();
				if(ss != null && !ss.isEmpty()) {
					fDescriptionText.setText(((ILaunchDelegate)ss.getFirstElement()).getDescription());
				}
				else {
					fDescriptionText.setText(IInternalDebugUIConstants.EMPTY_STRING);
				}
			}
		});
		try {
			ILaunchDelegate delegate = fConfiguration.getPreferredDelegate(getCurrentModeSet());
			boolean custom = delegate != null;
			fUseSystemLauncher.setSelection(custom);
			if(custom) {
				fTableViewer.setSelection(new StructuredSelection(delegate));
				fTableViewer.setCheckedElements(new Object[] {delegate});
			}
			else {
				resetDelegate();
				fTable.setEnabled(false);
			}
		}
		catch (CoreException ce) {DebugUIPlugin.log(ce);}
	}
}
