/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ui.IHelpContextIds;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.*;

public class ExportProjectSetMainPage extends TeamWizardPage {
	Combo fileCombo;
	protected Text workspaceText;
	protected String file = ""; //$NON-NLS-1$
	protected IFile workspaceFile;
	Button browseButton;
	List selectedProjects = new ArrayList();

	CheckboxTableViewer tableViewer;
	Table table;

	private boolean saveToFileSystem;
	private Button fileRadio;
	private Button workspaceRadio;

	class ProjectContentProvider extends WorkbenchContentProvider {
		public Object[] getElements(Object element) {
			if (element instanceof IProject[])
				return (IProject[]) element;
			return null;
		}
	};

	class LocationPageContentProvider extends BaseWorkbenchContentProvider {
		//Never show closed projects
		boolean showClosedProjects = false;

		public Object[] getChildren(Object element) {
			if (element instanceof IWorkspace) {
				// check if closed projects should be shown
				IProject[] allProjects = ((IWorkspace) element).getRoot().getProjects();
				if (showClosedProjects)
					return allProjects;

				ArrayList accessibleProjects = new ArrayList();
				for (int i = 0; i < allProjects.length; i++) {
					if (allProjects[i].isOpen()) {
						accessibleProjects.add(allProjects[i]);
					}
				}
				return accessibleProjects.toArray();
			}

			return super.getChildren(element);
		}
	}

	class WorkspaceDialog extends TitleAreaDialog {

		protected TreeViewer wsTreeViewer;
		protected Text wsFilenameText;
		protected IContainer wsContainer;
		protected Image dlgTitleImage;

		public WorkspaceDialog(Shell shell) {
			super(shell);
		}

		protected Control createContents(Composite parent) {
			Control control = super.createContents(parent);
			setTitle(TeamUIMessages.ExportProjectSetMainPage_WorkspaceDialogTitle);
			setMessage(TeamUIMessages.ExportProjectSetMainPage_WorkspaceDialogTitleMessage);

			return control;
		}

		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);

			GridLayout layout = new GridLayout();
			layout.numColumns = 1;
			composite.setLayout(layout);
			final GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
			composite.setLayoutData(data);

			getShell().setText(TeamUIMessages.ExportProjectSetMainPage_WorkspaceDialogMessage);

			wsTreeViewer = new TreeViewer(composite, SWT.BORDER);
			final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.widthHint = 550;
			gd.heightHint = 250;
			wsTreeViewer.getTree().setLayoutData(gd);

			wsTreeViewer.setContentProvider(new LocationPageContentProvider());
			wsTreeViewer.setLabelProvider(new WorkbenchLabelProvider());
			wsTreeViewer.setInput(ResourcesPlugin.getWorkspace());

			final Composite group = new Composite(composite, SWT.NONE);
			layout = new GridLayout(2, false);
			layout.marginWidth = 0;
			group.setLayout(layout);
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			final Label label = new Label(group, SWT.NONE);
			label.setLayoutData(new GridData());
			label.setText(TeamUIMessages.ExportProjectSetMainPage_WorkspaceDialogFilename);

			wsFilenameText = new Text(group, SWT.BORDER);
			wsFilenameText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			wsFilenameText.setText("projectSet.psf"); //$NON-NLS-1$

			setupListeners();

			return parent;
		}

		protected void okPressed() {
			//make sure that a filename has been typed in 

			String patchName = wsFilenameText.getText();

			if (patchName.equals("")) { //$NON-NLS-1$
				setErrorMessage(TeamUIMessages.ExportProjectSetMainPage_WorkspaceDialogErrorNoFilename);
				return;
			}

			//make sure that the filename does not contain more than one segment
			if (!(ResourcesPlugin.getWorkspace().validateName(patchName, IResource.FILE)).isOK()) {
				wsFilenameText.setText(""); //$NON-NLS-1$
				setErrorMessage(TeamUIMessages.ExportProjectSetMainPage_WorkspaceDialogErrorFilenameSegments);
				return;
			}

			//Make sure that a container has been selected
			if (wsContainer == null) {
				getSelectedContainer();
			}
			//Assert.isNotNull(wsContainer);

			workspaceFile = wsContainer.getFile(new Path(wsFilenameText.getText()));
			if (workspaceFile != null) {
				workspaceText.setText(workspaceFile.getFullPath().toString());
			}
			//this.page.validatePage();
			//workspaceText.setText(wsFilenameText.getText());
			super.okPressed();
		}

		private void getSelectedContainer() {
			Object obj = ((IStructuredSelection) wsTreeViewer.getSelection()).getFirstElement();

			if (obj instanceof IContainer)
				wsContainer = (IContainer) obj;
			else if (obj instanceof IFile) {
				wsContainer = ((IFile) obj).getParent();
			}
		}

		protected void cancelPressed() {
			//this.page.validatePage();
			getSelectedContainer();
			super.cancelPressed();
		}

		public boolean close() {
			/*     if (dlgTitleImage != null)
			 dlgTitleImage.dispose();*/
			return super.close();
		}

		void setupListeners() {
			wsTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					IStructuredSelection s = (IStructuredSelection) event.getSelection();
					Object obj = s.getFirstElement();
					if (obj != null) {

					}
					if (obj instanceof IContainer)
						wsContainer = (IContainer) obj;
					else if (obj instanceof IFile) {
						IFile tempFile = (IFile) obj;
						wsContainer = tempFile.getParent();
						wsFilenameText.setText(tempFile.getName());
					}
				}
			});

			wsTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
				public void doubleClick(DoubleClickEvent event) {
					ISelection s = event.getSelection();
					if (s instanceof IStructuredSelection) {
						Object item = ((IStructuredSelection) s).getFirstElement();
						if (wsTreeViewer.getExpandedState(item))
							wsTreeViewer.collapseToLevel(item, 1);
						else
							wsTreeViewer.expandToLevel(item, 1);
					}
				}
			});

			wsFilenameText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					setErrorMessage(null);
				}
			});
		}
	}

	public ExportProjectSetMainPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		setDescription(TeamUIMessages.ExportProjectSetMainPage_description);
	}

	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		initializeDialogUnits(composite);

		// set F1 help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IHelpContextIds.EXPORT_PROJECT_SET_PAGE);

		createLabel(composite, TeamUIMessages.ExportProjectSetMainPage_Select_the_projects_to_include_in_the_project_set__2);

		table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		tableViewer = new CheckboxTableViewer(table);
		table.setLayout(new TableLayout());
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 300;
		table.setLayoutData(data);
		tableViewer.setContentProvider(new ProjectContentProvider());
		tableViewer.setLabelProvider(new WorkbenchLabelProvider());
		tableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				IProject project = (IProject) event.getElement();
				if (event.getChecked()) {
					selectedProjects.add(project);
				} else {
					selectedProjects.remove(project);
				}
				updateEnablement();
			}
		});

		Composite buttonComposite = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		buttonComposite.setLayout(layout);
		data = new GridData(SWT.FILL, SWT.FILL, true, false);
		buttonComposite.setLayoutData(data);

		Button selectAll = new Button(buttonComposite, SWT.PUSH);
		data = new GridData();
		data.verticalAlignment = GridData.BEGINNING;
		data.horizontalAlignment = GridData.END;
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, selectAll.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		selectAll.setLayoutData(data);
		selectAll.setText(TeamUIMessages.ExportProjectSetMainPage_SelectAll);
		selectAll.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				tableViewer.setAllChecked(true);
				selectedProjects.removeAll(selectedProjects);
				Object[] checked = tableViewer.getCheckedElements();
				for (int i = 0; i < checked.length; i++) {
					selectedProjects.add(checked[i]);
				}
				updateEnablement();
			}
		});

		Button deselectAll = new Button(buttonComposite, SWT.PUSH);
		data = new GridData();
		data.verticalAlignment = GridData.BEGINNING;
		data.horizontalAlignment = GridData.END;
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, deselectAll.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		deselectAll.setLayoutData(data);
		deselectAll.setText(TeamUIMessages.ExportProjectSetMainPage_DeselectAll);
		deselectAll.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				tableViewer.setAllChecked(false);
				selectedProjects.removeAll(selectedProjects);
				updateEnablement();
			}
		});

		Group locationGroup = new Group(composite, SWT.None);
		layout = new GridLayout();
		locationGroup.setLayout(layout);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		locationGroup.setLayoutData(data);
		locationGroup.setText(TeamUIMessages.ExportProjectSetMainPage_Project_Set_File_Name__3);

		createExportToFile(locationGroup);

		createExportToWorkspace(locationGroup);

		saveToFileSystem = true;

		initializeProjects();
		setControl(composite);
		updateEnablement();
		Dialog.applyDialogFont(parent);
	}

	private void createExportToFile(Composite composite) {
		fileRadio = new Button(composite, SWT.RADIO);
		fileRadio.setText(TeamUIMessages.ExportProjectSetMainPage_FileButton);
		fileRadio.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				saveToFileSystem = true;
				updateEnablement();
			}
		});

		Composite inner = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		inner.setLayout(layout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		inner.setLayoutData(data);

		fileCombo = createDropDownCombo(inner);
		file = PsfFilenameStore.getSuggestedDefault();
		fileCombo.setItems(PsfFilenameStore.getHistory());
		fileCombo.setText(file);
		fileCombo.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				file = fileCombo.getText();
				updateEnablement();
			}
		});

		browseButton = new Button(inner, SWT.PUSH);
		browseButton.setText(TeamUIMessages.ExportProjectSetMainPage_Browse_4);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, browseButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		browseButton.setLayoutData(data);
		browseButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (!isSaveToFileSystem())
					saveToFileSystem = true;

				FileDialog d = new FileDialog(getShell(), SWT.SAVE);
				d.setFilterExtensions(new String[] {"*.psf"}); //$NON-NLS-1$
				d.setFilterNames(new String[] {TeamUIMessages.ExportProjectSetMainPage_Project_Set_Files_3});
				d.setFileName(TeamUIMessages.ExportProjectSetMainPage_default);
				String fileName = getFileName();
				if (fileName != null) {
					int separator = fileName.lastIndexOf(System.getProperty("file.separator").charAt(0)); //$NON-NLS-1$
					if (separator != -1) {
						fileName = fileName.substring(0, separator);
					}
				}
				d.setFilterPath(fileName);
				String f = d.open();
				if (f != null) {
					fileCombo.setText(f);
					file = f;
				}
			}
		});
	}

	private void createExportToWorkspace(Composite composite) {
		workspaceRadio = new Button(composite, SWT.RADIO);
		workspaceRadio.setText(TeamUIMessages.ExportProjectSetMainPage_WorkspaceButton);
		workspaceRadio.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				saveToFileSystem = false;
				updateEnablement();
			}
		});

		final Composite nameGroup = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		nameGroup.setLayout(layout);
		final GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		nameGroup.setLayoutData(data);

		workspaceText = createTextField(nameGroup);
		workspaceText.setEditable(false);
		workspaceText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				file = workspaceFile.getLocation().toString();
				updateEnablement();
			}
		});
		Button wsBrowseButton = new Button(nameGroup, SWT.PUSH);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		gd.widthHint = Math.max(widthHint, wsBrowseButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		wsBrowseButton.setLayoutData(gd);
		wsBrowseButton.setText(TeamUIMessages.ExportProjectSetMainPage_Browse);
		wsBrowseButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (isSaveToFileSystem())
					saveToFileSystem = false;

				WorkspaceDialog d = new WorkspaceDialog(getShell());
				d.open();
			}
		});
	}

	private void initializeProjects() {
		List projectList = new ArrayList();
		IProject[] workspaceProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < workspaceProjects.length; i++) {
			if (RepositoryProvider.getProvider(workspaceProjects[i]) != null) {
				projectList.add(workspaceProjects[i]);
			}
		}
		tableViewer.setInput(projectList.toArray(new IProject[projectList.size()]));
		// Check any necessary projects
		if (selectedProjects != null) {
			tableViewer.setCheckedElements(selectedProjects.toArray(new IProject[selectedProjects.size()]));
		}
	}

	private void updateEnablement() {
		boolean complete;
		//update radio buttons
		fileRadio.setSelection(saveToFileSystem);
		workspaceRadio.setSelection(!saveToFileSystem);

		if (selectedProjects.size() == 0) {
			setMessage(null);
			complete = false;
		} else if (file.length() == 0) {
			setMessage(null);
			complete = false;
		} else {
			File f = new File(file);
			if (f.isDirectory()) {
				setMessage(TeamUIMessages.ExportProjectSetMainPage_You_have_specified_a_folder_5, ERROR);
				complete = false;
			} else {
				if (!isSaveToFileSystem() && workspaceFile == null)
					complete = false;
				else
					complete = true;
			}
		}
		if (complete) {
			setMessage(null);
		}
		setPageComplete(complete);
	}

	public String getFileName() {
		return file;
	}

	public void setFileName(String file) {
		if (file != null) {
			this.file = file;
		}
	}

	public IProject[] getSelectedProjects() {
		return (IProject[]) selectedProjects.toArray(new IProject[selectedProjects.size()]);
	}

	public void setSelectedProjects(IProject[] selectedProjects) {
		this.selectedProjects.addAll(Arrays.asList(selectedProjects));
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			fileCombo.setFocus();
		}
	}

	public boolean isSaveToFileSystem() {
		return saveToFileSystem;
	}

	public void refreshWorkspaceFile(IProgressMonitor monitor) throws CoreException {
		if (workspaceFile != null)
			workspaceFile.refreshLocal(IResource.DEPTH_ONE, monitor);
	}
}
