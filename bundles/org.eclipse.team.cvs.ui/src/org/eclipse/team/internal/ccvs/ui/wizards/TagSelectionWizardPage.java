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
package org.eclipse.team.internal.ccvs.ui.wizards;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.merge.ProjectElement;
import org.eclipse.team.internal.ccvs.ui.merge.TagElement;
import org.eclipse.team.internal.ccvs.ui.merge.ProjectElement.ProjectElementSorter;
import org.eclipse.team.internal.ccvs.ui.repo.RepositorySorter;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * General tag selection page that allows the selection of a tag
 * for a particular remote folder
 */
public class TagSelectionWizardPage extends CVSWizardPage {

	private TreeViewer tagTree;
	private ICVSFolder[] remoteFolders;
	private CVSTag selectedTag;
	
	// Needed to dynamicaly create refresh buttons
	private Composite composite;
	private Control buttons;
	
	private String label;
	private int includeFlags;
	
	// Fields for allowing the use of the tag from the local workspace
	boolean allowNoTag = false;
	private Button useResourceTagButton;
	private Button selectTagButton;
	private boolean useResourceTag = false;
	private String helpContextId;
	
	public TagSelectionWizardPage(String pageName, String title, ImageDescriptor titleImage, String description, String label, int includeFlags) {
		super(pageName, title, titleImage, description);
		this.label = label;
		this.includeFlags = includeFlags;
	}

	public void setHelpContxtId(String helpContextId) {
		this.helpContextId = helpContextId;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		composite = createComposite(parent, 1);
		setControl(composite);
		
		// set F1 help
		if (helpContextId != null)
			WorkbenchHelp.setHelp(composite, helpContextId);
		
		if (allowNoTag) {
			SelectionListener listener = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					useResourceTag = useResourceTagButton.getSelection();
					updateEnablement();
				}
			};
			useResourceTag = true;
			useResourceTagButton = createRadioButton(composite, Policy.bind("TagSelectionWizardPage.0"), 1); //$NON-NLS-1$
			selectTagButton = createRadioButton(composite, Policy.bind("TagSelectionWizardPage.1"), 1); //$NON-NLS-1$
			useResourceTagButton.setSelection(useResourceTag);
			selectTagButton.setSelection(!useResourceTag);
			useResourceTagButton.addSelectionListener(listener);
			selectTagButton.addSelectionListener(listener);
		} else if (label != null) {
			createWrappingLabel(composite, label, 0);
		}
		
		tagTree = createTree(composite);
		tagTree.setSorter(new ProjectElementSorter());
		setInput();
		
		Dialog.applyDialogFont(parent);	
	}
	
	private void setInput() {
		if (remoteFolders != null 
				&& remoteFolders.length > 0 
				&& tagTree != null 
				&& !tagTree.getControl().isDisposed()) {
			tagTree.setInput(new ProjectElement(remoteFolders[0], includeFlags));
			try {
				selectedTag = remoteFolders[0].getFolderSyncInfo().getTag();
			} catch (CVSException e) {
				CVSUIPlugin.log(e);
			}
			if (selectedTag == null) {
				selectedTag = CVSTag.DEFAULT;
			}
			// TODO: Hack to instantiate the model before revealing the selection
			tagTree.expandToLevel(2);
			tagTree.collapseAll();
			// Reveal the selection
			tagTree.reveal(new TagElement(selectedTag));
			tagTree.setSelection(new StructuredSelection(new TagElement(selectedTag)));
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
			buttons = TagConfigurationDialog.createTagDefinitionButtons(getShell(), composite, remoteFolders, 
														  convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT), 
														  convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH),
														  refresh, refresh);
			composite.layout();
		}
	}

	private TreeViewer createTree(Composite parent) {
		Tree tree = new Tree(parent, SWT.SINGLE | SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		tree.setLayoutData(gridData);
		gridData.heightHint = 150;
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
		result.getTree().addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				IStructuredSelection selection = (IStructuredSelection)tagTree.getSelection();
				if (!selection.isEmpty() && (selection.getFirstElement() instanceof TagElement)) {
					gotoNextPage();
				}
			}
		});
		result.setSorter(new RepositorySorter());
		
		return result;
	}
	
	private void updateEnablement() {
		tagTree.getControl().setEnabled(!useResourceTag);
		setPageComplete(useResourceTag || selectedTag != null);
	}
	
	public ICVSFolder getFolder() {
		return remoteFolders[0];
	}
	
	public void setFolder(ICVSFolder remote) {
		setFolders(new ICVSFolder[] { remote });
	}
	
	public CVSTag getSelectedTag() {
		if (useResourceTag) 
			return null;
		return selectedTag;
	}
	
	protected void gotoNextPage() {
		TagSelectionWizardPage.this.getContainer().showPage(getNextPage());
	}

	public void setFolders(ICVSFolder[] remoteFolders) {
		this.remoteFolders = remoteFolders;
		setInput();
	}
	
	public void setAllowNoTag(boolean b) {
		allowNoTag = b;
	}
}
