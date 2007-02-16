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

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DefaultLabelProvider;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * This class provides a general selection dialog class to be specialized for 
 * selections using a checkbox table viewer
 * 
 * @since 3.3
 * EXPERIMENTAL
 */
public abstract class AbstractDebugSelectionDialog extends SelectionDialog {

	protected Table fTable = null;
	protected CheckboxTableViewer fTableViewer = null;
	
	/**
	 * Constructor
	 * @param parentShell
	 */
	public AbstractDebugSelectionDialog(Shell parentShell) {
		super(parentShell);
	}
	
	/**
	 * returns the dialog settings area id
	 * @return the id of the dialog settings area
	 */
	protected abstract String getDialogSettingsId();
	
	/**
	 * Returns the object to use as input for the viewer
	 * @return the object to use as input for the viewer
	 */
	protected abstract Object getViewerInput();
	
	/**
	 * Returns the content provider for the viewer
	 * @return the content provider for the viewer
	 */
	protected IContentProvider getContentProvider() {
		//by default return a simple array content provider
		return new ArrayContentProvider();
	}
	
	/**
	 * Returns the label provider used by the viewer
	 * @return the label provider used in the viewer
	 */
	protected IBaseLabelProvider getLabelProvider() {
		return new DefaultLabelProvider();
	}
	
	/**
	 * Returns the message for the viewer: i.e. Select Foo:
	 * @return the message for the viewer
	 */
	protected String getTableViewerMessage() {
		//do nothing by default
		return null;
	}
	
	/**
	 * Returns the help context id for this dialog
	 * @return the help context id for this dialog
	 */
	protected String getHelpContextId() {
		//do nothing by default
		return null;
	}
	
	/**
	 * This method allows custom controls to be added before the viewer
	 * @param parent the parent composite to add these custom controls to
	 */
	protected void addCustomHeaderControls(Composite parent) {
		//do nothing by default
	}
	
	/**
	 * This method allows custom controls to be added after the viewer
	 * @param parent the parent composite to add these controls to
	 */
	protected void addCustomFooterControls(Composite parent) {
		//do nothing by default
	}
	
	/**
	 * This method allows the newly created controls to be initialized, with this method being called from
	 * the createDialogArea method
	 */
	protected void initializeControls() {
		//do nothing by default
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		initializeDialogUnits(parent);
		Composite comp = (Composite) super.createDialogArea(parent);
		addCustomHeaderControls(comp);
		String label = getTableViewerMessage();
		if(label != null) {
			SWTFactory.createLabel(comp, label, 1);
		}
		fTable = new Table(comp, SWT.BORDER | SWT.SINGLE | SWT.CHECK);
		fTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		fTableViewer = new CheckboxTableViewer(fTable);
		fTableViewer.setLabelProvider(getLabelProvider());
		fTableViewer.setContentProvider(getContentProvider());
		fTableViewer.setInput(getViewerInput());
		fTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				fTableViewer.setCheckedElements(new Object[] {event.getElement()});
				getButton(IDialogConstants.OK_ID).setEnabled(true);
			}
		});
		addCustomFooterControls(comp);
		initializeControls();
		Dialog.applyDialogFont(comp);
		String help = getHelpContextId();
		if(help != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(comp, help);
		}
		return comp;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.SelectionDialog#getDialogBoundsSettings()
	 */
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings settings = DebugUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(getDialogSettingsId());
		if (section == null) {
			section = settings.addNewSection(getDialogSettingsId());
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
				return new Point(450, 500);
			}
		}
		return new Point(450, 500);
	}

}
