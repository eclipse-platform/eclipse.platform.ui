/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.wizards;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.merge.ProjectElement;
import org.eclipse.team.internal.ccvs.ui.merge.TagElement;
import org.eclipse.team.internal.ccvs.ui.merge.ProjectElement.ProjectElementSorter;
import org.eclipse.team.internal.ccvs.ui.repo.RepositorySorter;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * This page is used to obtain a tag from the user when the project being shared 
 * alreasy exists remotely.
 */
public class SharingWizardTagPage extends CVSWizardPage {
	
	private TreeViewer tagTree;
	private ICVSFolder remote;
	private CVSTag selectedTag;
	
	// Needed to dynamicaly create refresh buttons
	private Composite composite;
	private Control buttons;
	
	public SharingWizardTagPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		composite = createComposite(parent, 1);
		setControl(composite);
		
		// set F1 help
		WorkbenchHelp.setHelp(composite, IHelpContextIds.SHARE_WITH_EXISTING_TAG_SELETION_DIALOG);
		
		createWrappingLabel(composite, Policy.bind("SharingWizard.selectTag"), 0); //$NON-NLS-1$
		
		tagTree = createTree(composite);
		tagTree.setSorter(new ProjectElementSorter());
		setInput();
		
		Dialog.applyDialogFont(parent);	
	}
	
	private void setInput() {
		if (remote != null && tagTree != null && !tagTree.getControl().isDisposed()) {
			tagTree.setInput(new ProjectElement(remote, TagSelectionDialog.INCLUDE_HEAD_TAG | TagSelectionDialog.INCLUDE_BRANCHES));
			tagTree.setSelection(new StructuredSelection(new TagElement(CVSTag.DEFAULT)));
			if (buttons != null) {
				buttons.dispose();
				buttons = null;
			}
			Runnable refresh = new Runnable() {
				public void run() {
					getShell().getDisplay().syncExec(new Runnable() {
						public void run() {
							tagTree.refresh();
						}
					});
				}
			};
			buttons = TagConfigurationDialog.createTagDefinitionButtons(getShell(), composite, new ICVSFolder[] { remote }, 
														  convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT), 
														  convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH),
														  refresh, refresh);
			composite.layout();
		}
	}

	private TreeViewer createTree(Composite parent) {
		Tree tree = new Tree(parent, SWT.SINGLE | SWT.BORDER);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));	
		TreeViewer result = new TreeViewer(tree);
		result.setContentProvider(new WorkbenchContentProvider());
		result.setLabelProvider(new WorkbenchLabelProvider());
		result.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				if (selection.isEmpty() || !(selection.getFirstElement() instanceof TagElement)) {
					selectedTag = null;
				} else {
					selectedTag = ((TagElement)selection.getFirstElement()).getTag();
				}
				updateEnablement();
			}
		});
		// select and close on double click
		// To do: use defaultselection instead of double click
		result.getTree().addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				IStructuredSelection selection = (IStructuredSelection)tagTree.getSelection();
				if (!selection.isEmpty() && (selection.getFirstElement() instanceof TagElement)) {
					SharingWizardTagPage.this.getContainer().showPage(getNextPage());
				}
			}
		});
		result.setSorter(new RepositorySorter());
		
		return result;
	}
	
	/**
	 * Updates the dialog enablement.
	 */
	private void updateEnablement() {
		setPageComplete(selectedTag != null);
	}
	
	public ICVSFolder getFolder() {
		return remote;
	}
	
	public void setFolder(ICVSFolder remote) {
		this.remote = remote;
		setInput();
	}
	
	public CVSTag getSelectedTag() {
		return selectedTag;
	}
}
