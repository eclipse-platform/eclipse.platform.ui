/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.sourcelookup;

import java.util.ArrayList;

import org.eclipse.debug.internal.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupUtils;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * The dialog for adding new source containers. Presents the user with a list of
 * source container types and allows them to select one.
 * 
 * @since 3.0
 */
public class AddSourceContainerDialog extends TitleAreaDialog {
	
	private TableViewer fViewer;
	private SourceContainerViewer fSourceContainerViewer;
	private boolean fDoubleClickSelects = true;
	
	/**
	 * Label content provider to retrieve source container names and icons from
	 * ISourceContainerType 
	 */
	class SourceContainerTypeLabelProvider extends LabelProvider {						
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			if (element instanceof ISourceContainerType)
				return ((ISourceContainerType) element).getName();
			
			return super.getText(element);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage(Object element) {
			if (element instanceof ISourceContainerType)
				return SourceLookupUIUtils.getSourceContainerImage(((ISourceContainerType) element).getId());			
			
			return super.getImage(element);
		}
	}	
	/**
	 * Constructor
	 */
	public AddSourceContainerDialog(Shell shell, SourceContainerViewer viewer)
	{		
		super(shell);
		fSourceContainerViewer=viewer;					
	}
	
	/**
	 * Creates the dialog area to display source container types that are "browseable"
	 */
	protected Control createDialogArea(Composite ancestor) {			
		
		getShell().setText(SourceLookupUIMessages.getString("addSourceLocation.title")); //$NON-NLS-1$
		setTitle(SourceLookupUIMessages.getString("addSourceLocation.description")); //$NON-NLS-1$
		setTitleImage(DebugPluginImages.getImage(IDebugUIConstants.IMG_ADD_SRC_LOC_WIZ));
		
		Composite parent = new Composite(ancestor, SWT.NULL);
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace=true;
		gd.grabExcessVerticalSpace=true;
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 1;
		parent.setLayout(topLayout);
		parent.setLayoutData(gd);	
		
		gd= new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace=true;
		gd.grabExcessVerticalSpace=true;
		
		ISourceContainerType[] types = removeTypesWithoutBrowsers(SourceLookupUtils.getSourceContainerTypes());
		final Table containerTable = new Table(parent, SWT.SINGLE | SWT.BORDER);
		topLayout = new GridLayout();
		topLayout.numColumns = 1;
		containerTable.setLayout(topLayout);
		containerTable.setLayoutData(gd);
		if (fDoubleClickSelects) {
			containerTable.addSelectionListener(new SelectionAdapter() {
				public void widgetDefaultSelected(SelectionEvent e) {
					if (containerTable.getSelectionCount() == 1)
						okPressed();
				}
			});
		}
		
		fViewer = new TableViewer(containerTable);
		fViewer.setLabelProvider(new SourceContainerTypeLabelProvider());
		fViewer.setContentProvider(new ArrayContentProvider());				
		if(types.length != 0)
		{	
			fViewer.setInput(types);
			fViewer.setSelection(new StructuredSelection(types[0]), true);
		}
		Dialog.applyDialogFont(parent);
		WorkbenchHelp.setHelp(getShell(), IDebugHelpContextIds.ADD_SOURCE_CONTAINER_DIALOG);
		return parent;
	}	
	
	/**
	 * Removes types without browsers from the provided list of types.
	 * @param types the complete list of source container types
	 * @return the list of source container types that have browsers
	 */
	private ISourceContainerType[] removeTypesWithoutBrowsers(ISourceContainerType[] types){
		ArrayList validTypes = new ArrayList();
		for (int i=0; i< types.length; i++)
		{
			if(SourceLookupUIUtils.getSourceContainerBrowser(types[i].getId()) != null)
				validTypes.add(types[i]);
		}	
		return (ISourceContainerType[]) validTypes.toArray(new ISourceContainerType[validTypes.size()]);
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		//single selection dialog, so take first item in array
		//there will always be a selected item since we set it with viewer.setSelection
		ISourceContainerType type = (ISourceContainerType) ((StructuredSelection) fViewer.getSelection()).getFirstElement();
		ISourceContainerBrowser browser = SourceLookupUIUtils.getSourceContainerBrowser(type.getId());
		if(browser == null)
			super.okPressed();
		ISourceContainer[] results = browser.createSourceContainers(getShell());
		if(results != null)
			fSourceContainerViewer.addEntries(results);
		super.okPressed();
	}		
	
}
