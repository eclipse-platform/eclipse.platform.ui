/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * This class provides the framework for general selection dialog class.
 * 
 * TODO Use this abstract class hierarchy instead of the SelectionDialog for dialogs in the debug plugin
 * 
 * @see AbstractDebugListSelectionDialog
 * @see AbstractDebugCheckboxSelectionDialog
 * 
 * @since 3.3
 */
public abstract class AbstractDebugSelectionDialog extends SelectionDialog {

	protected StructuredViewer fViewer = null;
	
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
	 * Create and return a viewer to use in this dialog.
	 * 
	 * @param parent the composite the viewer should be created in
	 * @return the viewer to use in the dialog
	 */
	protected abstract StructuredViewer createViewer(Composite parent);
	
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
	 * Returns the help context id for this dialog
	 * @return the help context id for this dialog
	 */
	abstract protected String getHelpContextId();
	
	/**
	 * This method allows listeners to be added to the viewer after it
	 * is created.
	 */
	/**
	 * This method allows listeners to be added to the viewer.  Called
	 * after the viewer has been created and its input set.
	 * 
	 * @param viewer the viewer returned by createViewer()
	 */
	protected void addViewerListeners(StructuredViewer viewer){
		//do nothing by default
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		initializeDialogUnits(parent);
		Composite comp = (Composite) super.createDialogArea(parent);
		addCustomHeaderControls(comp);
		String label = getMessage();
		if(label != null && !"".equals(label)) { //$NON-NLS-1$
			SWTFactory.createWrapLabel(comp, label, 1);
		}
		label = getViewerLabel();
		if(label != null && !"".equals(label)) { //$NON-NLS-1$
			SWTFactory.createLabel(comp, label, 1);
		}
		fViewer = createViewer(comp);
		fViewer.setLabelProvider(getLabelProvider());
		fViewer.setContentProvider(getContentProvider());
		fViewer.setInput(getViewerInput());
		addViewerListeners(fViewer);
		addCustomFooterControls(comp);
		initializeControls();
		Dialog.applyDialogFont(comp);
		String help = getHelpContextId();
		if(help != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(comp, help);
		}
		return comp;
	}
	
	/**
	 * This method returns the label describing what to do with the viewer. Typically this label
	 * will include the key accelerator to get to the viewer via the keyboard
	 * @return the label for the viewer
	 */
	abstract protected String getViewerLabel();
	
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
				return new Point(300, 350);
			}
		}
		return new Point(300, 350);
	}
	
}
