/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
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
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.AbstractDebugListSelectionDialog;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This dialog is used to select a preferred launcher, and also provides access to the 
 * workspace defaults for preferred launchers
 * 
 *  @since 3.3
 */
public class SelectLaunchersDialog extends AbstractDebugListSelectionDialog {

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
	
	Text description = null;
	Button configspecific = null;
	private ILaunchDelegate[] fDelegates = null;
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
		Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 2, 1, GridData.FILL_HORIZONTAL, 0, 0);
		SWTFactory.createWrapLabel(comp, LaunchConfigurationsMessages.SelectLaunchersDialog_2, 2);
		
		SWTFactory.createVerticalSpacer(comp, 1);
		
		this.configspecific = SWTFactory.createCheckButton(comp, LaunchConfigurationsMessages.SelectLaunchersDialog_1, null, true, 1);
		this.configspecific.setSelection(false);
		GridData gd = (GridData) this.configspecific.getLayoutData();
		gd.grabExcessHorizontalSpace = true;
		this.configspecific.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				boolean checked = ((Button)e.widget).getSelection();
				getViewer().getControl().setEnabled(checked);
				resetDelegate();
			}
		});
		
		Link link = new Link(comp, SWT.WRAP);
		link.setText(LaunchConfigurationsMessages.SelectLaunchersDialog_4);
		link.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		link.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SWTFactory.showPreferencePage("org.eclipse.debug.ui.LaunchDelegatesPreferencePage"); //$NON-NLS-1$
				if(!SelectLaunchersDialog.this.configspecific.getSelection()) {
					resetDelegate();
				}
			}
		});
	}
	
	/**
	 * Returns the currently checked launch delegate
	 * @return the currently selected launch delegate or <code>null</code> if none are checked
	 */
	protected ILaunchDelegate getSelectedDelegate() {
		IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
		return (ILaunchDelegate) selection.getFirstElement();
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		ILaunchDelegate delegate = null;
		Set modes = getCurrentModeSet();
		if(configspecific.getSelection()) {
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
		try {
			ILaunchDelegate preferred = fConfiguration.getType().getPreferredDelegate(getCurrentModeSet());
			Viewer viewer = getViewer();
			if(preferred != null) {
				viewer.setSelection(new StructuredSelection(preferred));
			}
			else {
				viewer.setSelection(new StructuredSelection());
			}
			getButton(IDialogConstants.OK_ID).setEnabled(isValid());
		}
		catch (CoreException ce) {DebugUIPlugin.log(ce);}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#addCustomFooterControls(org.eclipse.swt.widgets.Composite)
	 */
	protected void addCustomFooterControls(Composite parent) {
		Group group = SWTFactory.createGroup(parent, LaunchConfigurationsMessages.SelectLaunchersDialog_5, 1, 1, GridData.FILL_BOTH);
		this.description = SWTFactory.createText(group, SWT.WRAP | SWT.READ_ONLY, 1, GridData.FILL_BOTH);
		this.description.setBackground(group.getBackground());
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
	 * @see org.eclipse.debug.internal.ui.AbstractDebugCheckboxSelectionDialog#addViewerListeners(org.eclipse.jface.viewers.StructuredViewer)
	 */
	protected void addViewerListeners(StructuredViewer viewer) {
		// Override super to use custom listeners
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection ss = (IStructuredSelection) event.getSelection();
				if(ss != null && !ss.isEmpty()) {
					SelectLaunchersDialog.this.description.setText(((ILaunchDelegate)ss.getFirstElement()).getDescription());
				}
				else {
					SelectLaunchersDialog.this.description.setText(IInternalDebugCoreConstants.EMPTY_STRING);
				}
			}
		});
		super.addViewerListeners(viewer);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#initializeControls()
	 */
	protected void initializeControls() {
		final Viewer viewer = getViewer();
		try {
			ILaunchDelegate delegate = fConfiguration.getPreferredDelegate(getCurrentModeSet());
			if(delegate != null) {
				viewer.setSelection(new StructuredSelection(delegate));
				configspecific.setSelection(true);
			}
			else {
				viewer.getControl().setEnabled(false);
				resetDelegate();
			}
		}
		catch (CoreException ce) {DebugUIPlugin.log(ce);}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getViewerLabel()
	 */
	protected String getViewerLabel() {
		return LaunchConfigurationsMessages.SelectLaunchersDialog_launchers;
	}
}
