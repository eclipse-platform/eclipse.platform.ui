package org.eclipse.team.internal.ccvs.ui.merge;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.wizards.CVSWizardPage;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class MergeWizardEndPage extends CVSWizardPage {
	IProject project;
	TreeViewer tree;
	CVSTag result;
	ICVSRemoteFolder remote;
	// for accessing the start tag
	MergeWizardStartPage startPage;
	
	/**
	 * MergeWizardEndPage constructor.
	 * 
	 * @param pageName  the name of the page
	 * @param title  the title of the page
	 * @param titleImage  the image for the page
	 */
	public MergeWizardEndPage(String pageName, String title, ImageDescriptor titleImage, MergeWizardStartPage startPage) {
		super(pageName, title, titleImage);
		setDescription(Policy.bind("MergeWizardEndPage.description")); //$NON-NLS-1$
		this.startPage = startPage;
	}
	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 2);
		// set F1 help
		// WorkbenchHelp.setHelp(composite, new DialogPageContextComputer (this, ITeamHelpContextIds.REPO_CONNECTION_MAIN_PAGE));
		
		tree = createTree(composite);
		tree.setContentProvider(new WorkbenchContentProvider());
		tree.setLabelProvider(new WorkbenchLabelProvider());
		tree.setSorter(new ViewerSorter() {
			public int compare(Viewer v, Object o1, Object o2) {
				int result = super.compare(v, o1, o2);
				if (o1 instanceof TagElement && o2 instanceof TagElement) {
					return -result;
				}
				return result;
			}
		});
		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object selected = ((IStructuredSelection)tree.getSelection()).getFirstElement();
				if (selected instanceof TagElement) {
					result = ((TagElement)selected).getTag();
					if(!result.equals(startPage.getTag())) {
						setPageComplete(true);
						setMessage(null);
					} else {
						setMessage(Policy.bind("MergeWizardEndPage.duplicateTagSelected", result.getName()), WARNING_MESSAGE); //$NON-NLS-1$
						setPageComplete(false);
					}
				} else {
					setMessage(null);
					result = null;
					setPageComplete(false);
				}
			}
		});
		setControl(composite);
		tree.setInput(new ProjectElement(CVSWorkspaceRoot.getCVSFolderFor(project), true /*show HEAD as tag*/));
		setPageComplete(false);
	}
	protected TreeViewer createTree(Composite parent) {
		Tree tree = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		return new TreeViewer(tree);
	}
	public void setProject(IProject project) {
		this.project = project;
		try {
			this.remote = (ICVSRemoteFolder) CVSWorkspaceRoot.getRemoteResourceFor(project);
		} catch (TeamException e) {
			// To do
		}
	}
	public CVSTag getTag() {
		return result;
	}
	/**
	 * @see IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		// refresh the tree because tags may of been added in the previous page
		tree.refresh();
	}

}
