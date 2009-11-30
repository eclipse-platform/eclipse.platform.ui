/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.sourcelookup;

import java.util.ArrayList;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.sourcelookup.ISourceContainerBrowser;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;

/**
 * The dialog for adding new source containers. Presents the user with a list of
 * source container types and allows them to select one.
 * 
 * @since 3.0
 */
public class AddSourceContainerDialog extends TitleAreaDialog {
	
	private TableViewer fViewer;
	private SourceContainerViewer fSourceContainerViewer;
	private ISourceLookupDirector fDirector;
	
	/**
	 * Constructor
	 */
	public AddSourceContainerDialog(Shell shell, SourceContainerViewer viewer, ISourceLookupDirector director) {		
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fSourceContainerViewer=viewer;		
		fDirector = director;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {			
		
		getShell().setText(SourceLookupUIMessages.addSourceLocation_title); 
		setTitle(SourceLookupUIMessages.addSourceLocation_description); 
		setTitleImage(DebugPluginImages.getImage(IInternalDebugUIConstants.IMG_ADD_SRC_LOC_WIZ));
		setMessage(SourceLookupUIMessages.AddSourceContainerDialog_select_source_container);
		
		Composite comp = (Composite) super.createDialogArea(parent);
		GridData gd= new GridData(GridData.FILL_BOTH);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 1;
		comp.setLayout(topLayout);
		comp.setLayoutData(gd);	
				
		ISourceContainerType[] types = filterTypes(DebugPlugin.getDefault().getLaunchManager().getSourceContainerTypes());
		
		fViewer = new TableViewer(comp, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE);
		final Table table = fViewer.getTable();
		gd = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(gd);

		fViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				ISourceContainerType type = (ISourceContainerType) ((IStructuredSelection) event.getSelection()).getFirstElement();
				addEntries(type);
			}
		});
		
		fViewer.setLabelProvider(new SourceContainerLabelProvider());
		fViewer.setContentProvider(new ArrayContentProvider());			
		fViewer.setComparator(new ViewerComparator());
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (!selection.isEmpty()) {
					ISourceContainerType type = (ISourceContainerType) ((IStructuredSelection)selection).getFirstElement();
					setMessage(type.getDescription());
				}
				else {
					setMessage(SourceLookupUIMessages.AddSourceContainerDialog_select_source_container);
				}
			}
		});
		if(types.length != 0) {	
			fViewer.setInput(types);
		}
		Dialog.applyDialogFont(comp);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(), IDebugHelpContextIds.ADD_SOURCE_CONTAINER_DIALOG);
		return comp;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}
	
	/**
	 * Delegate method to add created entries to the backing source container viewer
	 * @param type
	 * @since 3.6
	 */
	void addEntries(ISourceContainerType type) {
		if (type != null) {
            ISourceContainerBrowser browser = DebugUITools.getSourceContainerBrowser(type.getId());
            if (browser != null) {
                ISourceContainer[] results = browser.addSourceContainers(getShell(), fDirector);
                if (results != null && results.length > 0) {
                    fSourceContainerViewer.addEntries(results);
                }
            }
        }
	}
	
	/**
	 * Removes types without browsers from the provided list of types.
	 * @param types the complete list of source container types
	 * @return the list of source container types that have browsers
	 */
	private ISourceContainerType[] filterTypes(ISourceContainerType[] types){
		ArrayList validTypes = new ArrayList();
		for (int i=0; i< types.length; i++) {
			ISourceContainerType type = types[i];
			if (fDirector.supportsSourceContainerType(type)) {
				ISourceContainerBrowser sourceContainerBrowser = DebugUITools.getSourceContainerBrowser(type.getId());
				if(sourceContainerBrowser != null && sourceContainerBrowser.canAddSourceContainers(fDirector)) {
					validTypes.add(type);
				}
			}
		}	
		return (ISourceContainerType[]) validTypes.toArray(new ISourceContainerType[validTypes.size()]);
		
	}
}
