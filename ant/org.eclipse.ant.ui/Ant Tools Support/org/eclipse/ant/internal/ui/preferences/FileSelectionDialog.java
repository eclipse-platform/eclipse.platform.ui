/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.preferences;

import java.util.List;

import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceSorter;

public class FileSelectionDialog extends ElementTreeSelectionDialog {
	
	private ViewerFilter fFilter;
	private String fFilterMessage;
	private boolean fShowAll= false;
	private final static String DIALOG_SETTING= "AntPropertiesFileSelectionDialog.showAll";  //$NON-NLS-1$
	
	public FileSelectionDialog(Shell parent, List propertyFiles, String title, String message, String filterExtension, String filterMessage) {
		super(parent, new WorkbenchLabelProvider(), new WorkbenchContentProvider());
		
		setTitle(title);  //$NON-NLS-1$
		setMessage(message); //$NON-NLS-1$
		fFilter= new FileFilter(propertyFiles, filterExtension);
		fFilterMessage= filterMessage;
		setInput(ResourcesPlugin.getWorkspace().getRoot());	
		setSorter(new ResourceSorter(ResourceSorter.NAME));
		
		ISelectionStatusValidator validator= new ISelectionStatusValidator() {
			public IStatus validate(Object[] selection) {
				if (selection.length == 0) {
					return new Status(IStatus.ERROR, AntUIPlugin.getUniqueIdentifier(), 0, "", null); //$NON-NLS-1$
				}
				for (int i= 0; i < selection.length; i++) {
					if (!(selection[i] instanceof IFile)) {
						return new Status(IStatus.ERROR, AntUIPlugin.getUniqueIdentifier(), 0, "", null); //$NON-NLS-1$
					}
				}
				return new Status(IStatus.OK, AntUIPlugin.getUniqueIdentifier(), 0, "", null); //$NON-NLS-1$
			}			
		};
		setValidator(validator);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		
		Composite result= (Composite)super.createDialogArea(parent);
		final Button button = new Button(result, SWT.CHECK);
		button.setText(fFilterMessage);
		
		button.setFont(parent.getFont());
		GridData data= new GridData();
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		button.setLayoutData(data);
		IDialogSettings settings= AntUIPlugin.getDefault().getDialogSettings();
		fShowAll= settings.getBoolean(DIALOG_SETTING);
		if (!fShowAll) {
			getTreeViewer().addFilter(fFilter);
			button.setSelection(true);
		}
		
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (button.getSelection()) {
					fShowAll= false;
					getTreeViewer().addFilter(fFilter);
				} else {
					fShowAll= true;
					getTreeViewer().removeFilter(fFilter);
				}
			}
		});
		applyDialogFont(result);		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		IDialogSettings settings= AntUIPlugin.getDefault().getDialogSettings();
		settings.put(DIALOG_SETTING, fShowAll);
		return super.close();
	}
}
