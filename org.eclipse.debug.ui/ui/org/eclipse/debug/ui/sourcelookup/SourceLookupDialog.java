/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.sourcelookup;

import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DialogSettingsHelper;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.sourcelookup.SourceLookupPanel;
import org.eclipse.debug.internal.ui.sourcelookup.SourceLookupUIMessages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * A dialog for editing the source lookup path of a
 * source lookup director.
 * <p>
 * This class may be instantiated; it is not intended to be
 * subclassed.
 * </p>
 * @since 3.0
 */
public class SourceLookupDialog extends TitleAreaDialog {
	
	private SourceLookupPanel fPanel;
	private ISourceLookupDirector fDirector;
	
	/**
	 * Constructs a dialog to edit the source lookup path managed by the
	 * given source lookup director. Persists the resulting source lookup
	 * path on the launch configuration associated with the given source
	 * lookup director.
	 * 
	 * @param shell shell to parent the dialog
	 * @param director source lookup director managing the source lookup
	 *  path to be edited
	 */
	public SourceLookupDialog(Shell shell, ISourceLookupDirector director) {
		super(shell);	
        setShellStyle(getShellStyle() | SWT.RESIZE);
		fDirector = director;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		// create a composite with standard margins and spacing
		setTitle(SourceLookupUIMessages.manageSourceDialog_description); 
		setTitleImage(DebugPluginImages.getImage(IInternalDebugUIConstants.IMG_EDIT_SRC_LOC_WIZ));
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.marginHeight =
			convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth =
			convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing =
			convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing =
			convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);			
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());
		fPanel = new SourceLookupPanel();
		fPanel.createControl(composite);
		fPanel.initializeFrom(fDirector);
		
		Dialog.applyDialogFont(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(),  IDebugHelpContextIds.EDIT_SOURCELOOKUP_DIALOG);
		
		return composite;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		fPanel.performApply(null);
        DialogSettingsHelper.persistShellGeometry(getShell(), getClass().getName());
		super.okPressed();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */	
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(SourceLookupUIMessages.manageSourceDialog_title); 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		fPanel.dispose();
		return super.close();
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#getInitialLocation(org.eclipse.swt.graphics.Point)
     * @since 3.1
     */
    protected Point getInitialLocation(Point initialSize) {
        Point initialLocation= DialogSettingsHelper.getInitialLocation(getClass().getName());
        if (initialLocation != null) {
            return initialLocation;
        }
        return super.getInitialLocation(initialSize);
    }  
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#getInitialSize()
     * @since 3.1
     */
    protected Point getInitialSize() {
        Point size = super.getInitialSize();
        return DialogSettingsHelper.getInitialSize(getClass().getName(), size);
    }    
}
