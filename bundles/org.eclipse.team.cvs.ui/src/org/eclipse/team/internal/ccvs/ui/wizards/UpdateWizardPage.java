package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.TagConfigurationDialog;
import org.eclipse.team.internal.ccvs.ui.merge.ProjectElement;
import org.eclipse.team.internal.ccvs.ui.merge.TagElement;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class UpdateWizardPage extends CVSWizardPage {

	IProject project;
	TreeViewer tree;
	CVSTag result;
	ICVSRemoteFolder remote;
	boolean doOverwrite;
	
	public UpdateWizardPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	
	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 2);
		// set F1 help
		// WorkbenchHelp.setHelp(composite, new DialogPageContextComputer (this, ITeamHelpContextIds.REPO_CONNECTION_MAIN_PAGE));
		
		Label description = new Label(composite, SWT.WRAP);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.widthHint = 350;
		description.setLayoutData(data);
		description.setText(Policy.bind("UpdateWizardPage.description")); //$NON-NLS-1$
		
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
					setPageComplete(true);
				} else {
					result = null;
					setPageComplete(false);
				}
			}
		});
		
		createLabel(composite, ""); //$NON-NLS-1$
		createLabel(composite, ""); //$NON-NLS-1$
		
		doOverwrite = false;
		final Button overwrite = new Button(composite, SWT.CHECK);
		data = new GridData();
		data.horizontalSpan = 2;
		overwrite.setLayoutData(data);
		overwrite.setText(Policy.bind("UpdateWizardPage.overwrite")); //$NON-NLS-1$
		overwrite.setSelection(doOverwrite);
		overwrite.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				doOverwrite = overwrite.getSelection();
			}
		});
		
		setControl(composite);
		tree.setInput(new ProjectElement(CVSWorkspaceRoot.getCVSFolderFor(project), true /*show HEAD as tag*/));
		
		Runnable refresh = new Runnable() {
			public void run() {
				tree.refresh();
			}
		};
		TagConfigurationDialog.createTagDefinitionButtons(getShell(), composite, new IProject[] {project},convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT), 
																							convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH), refresh, refresh);
		
		Label seperator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		data = new GridData (GridData.FILL_BOTH);		
		data.horizontalSpan = 2;
		seperator.setLayoutData(data);
		
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
	
	public LocalOption[] getLocalOptions() {
		if (doOverwrite) {
			return new LocalOption[] { Update.IGNORE_LOCAL_CHANGES };
		}
		return Command.NO_LOCAL_OPTIONS;
	}
}
