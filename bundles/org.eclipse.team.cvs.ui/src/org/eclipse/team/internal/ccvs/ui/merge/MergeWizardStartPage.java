package org.eclipse.team.internal.ccvs.ui.merge;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.wizards.CVSWizardPage;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class MergeWizardStartPage extends CVSWizardPage {
	TableViewer table;
	CVSTag result;
	IProject project;
	
	private static final int TABLE_HEIGHT_HINT = 350;
	
	/**
	 * MergeWizardStartPage constructor.
	 * 
	 * @param pageName  the name of the page
	 * @param title  the title of the page
	 * @param titleImage  the image for the page
	 */
	public MergeWizardStartPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		setDescription(Policy.bind("MergeWizardStartPage.description"));
	}
	protected TableViewer createTable(Composite parent) {
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = TABLE_HEIGHT_HINT;
		table.setLayoutData(data);
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(100, true));
		table.setLayout(layout);
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
	
		return new TableViewer(table);
	}
	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 2);
		// set F1 help
		// WorkbenchHelp.setHelp(composite, new DialogPageContextComputer (this, ITeamHelpContextIds.REPO_CONNECTION_MAIN_PAGE));
		
		table = createTable(composite);
		table.setContentProvider(new WorkbenchContentProvider());
		table.setLabelProvider(new WorkbenchLabelProvider());
		table.setSorter(new ViewerSorter() {
			public int compare(Viewer v, Object o1, Object o2) {
				int result = super.compare(v, o1, o2);
				if (o1 instanceof TagElement && o2 instanceof TagElement) {
					return -result;
				}
				return result;
			}
		});
		table.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				TagElement element = (TagElement)((IStructuredSelection)table.getSelection()).getFirstElement();
				result = element.getTag();
				setPageComplete(true);
			}
		});
		table.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				getContainer().showPage(getNextPage());
			}
		});

		setControl(composite);

		initialize();
		setPageComplete(false);
	}
	private void initialize() {
		try {
			ICVSRemoteFolder remoteResource = (ICVSRemoteFolder)CVSWorkspaceRoot.getRemoteResourceFor(project);
			table.setInput(new VersionsElement(remoteResource, getShell()));
		} catch (TeamException e) {
			// To do. This could only happen if the resource was not a child of the provider.
		}
	}
	public void setProject(IProject project) {
		this.project = project;
	}
	public CVSTag getTag() {
		return result;
	}
}
