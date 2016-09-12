/*******************************************************************************
 * Copyright (c) 2014, 2016 Red Hat Inc., and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 *     Snjezana Peco (Red Hat Inc.)
 *     Lars Vogel <Lars.Vogel@vogella.com>
 *     Rüdiger Herrmann <ruediger.herrmann@gmx.de>
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 500836
 ******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.dialogs.WorkingSetConfigurationBlock;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.progress.ProgressManager;
import org.eclipse.ui.internal.progress.ProgressManager.JobMonitor;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.wizards.datatransfer.ProjectConfigurator;

/**
 * This page of {@link SmartImportWizard} simply asks for what to import, and asks
 * user a basic choice of how to import (import raw, infer sub- projects...)
 *
 * @since 3.12
 *
 */
public class SmartImportRootWizardPage extends WizardPage {

	static final String IMPORTED_SOURCES = SmartImportRootWizardPage.class.getName() + ".knownSources"; //$NON-NLS-1$

	// Root
	private File selection;
	private Combo rootDirectoryText;
	private ControlDecoration rootDirectoryTextDecorator;
	// Proposal part
	private CheckboxTreeViewer tree;
	private ControlDecoration proposalSelectionDecorator;
	private Set<File> alreadyExistingProjects;
	private Set<File> notAlreadyExistingProjects;
	private Label selectionSummary;
	protected Map<File, List<ProjectConfigurator>> potentialProjects = Collections.emptyMap();
	// Configuration part
	private boolean detectNestedProjects = true;
	private boolean configureProjects = true;
	// Working sets
	private Set<IWorkingSet> workingSets;
	private WorkingSetConfigurationBlock workingSetsBlock;
	// Progress monitor
	protected Supplier<ProgressMonitorPart> wizardProgressMonitor = new Supplier<ProgressMonitorPart>() {
		private ProgressMonitorPart progressMonitorPart;
		@Override
		public ProgressMonitorPart get() {
			if (progressMonitorPart == null) {
				try {
					getWizard().getContainer().run(false, true, monitor -> {
						if (monitor instanceof ProgressMonitorPart) {
							progressMonitorPart = (ProgressMonitorPart) monitor;
						}
					});
				} catch (InvocationTargetException ite) {
					IStatus status = new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH,
							DataTransferMessages.SmartImportWizardPage_scanProjectsFailed, ite.getCause());
					StatusManager.getManager().handle(status, StatusManager.LOG | StatusManager.SHOW);
				} catch (InterruptedException operationCanceled) {
					Thread.interrupted();
				}
			}
			return progressMonitorPart;
		}
	};

	private Job refreshProposalsJob;
	private JobMonitor jobMonitor;
	private DelegateProgressMonitorInUIThreadAndPreservingFocus delegateMonitor;
	private SelectionListener cancelWorkListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			stopAndDisconnectCurrentWork();
		}
	};

	private class FolderForProjectsLabelProvider extends CellLabelProvider implements IColorProvider {
		public String getText(Object o) {
			File file = (File) o;
			Path filePath = file.toPath();
			Path rootPath = getWizard().getImportJob().getRoot().toPath();
			if (filePath.startsWith(rootPath)) {
				if (rootPath.getParent() != null) {
					Path relative = rootPath.getParent().relativize(filePath);
					if (relative.getNameCount() > 0) {
						return relative.toString();
					}
				}
			}
			return filePath.toString();
		}

		@Override
		public Color getBackground(Object o) {
			return null;
		}

		@Override
		public Color getForeground(Object o) {
			if (alreadyExistingProjects.contains(o)) {
				return Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
			}
			return null;
		}

		@Override
		public void update(ViewerCell cell) {
			cell.setText(getText(cell.getElement()));
			Color color = getForeground(cell.getElement());
			if (color != null) {
				cell.setForeground(color);
			}
		}
	}

	private class ProjectConfiguratorLabelProvider extends CellLabelProvider implements IColorProvider {
		public String getText(Object o) {
			File file = (File) o;
			if (alreadyExistingProjects.contains(file)) {
				return DataTransferMessages.SmartImportProposals_alreadyImportedAsProject_title;
			}
			List<ProjectConfigurator> configurators = SmartImportRootWizardPage.this.potentialProjects.get(file);
			if (configurators.isEmpty()) {
				return ""; //$NON-NLS-1$
			}
			return ProjectConfiguratorExtensionManager.getLabel(configurators.get(0));
		}

		@Override
		public Color getBackground(Object o) {
			return null;
		}

		@Override
		public Color getForeground(Object o) {
			if (alreadyExistingProjects.contains(o)) {
				return Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
			}
			return null;
		}

		@Override
		public void update(ViewerCell cell) {
			cell.setText(getText(cell.getElement()));
			Color color = getForeground(cell.getElement());
			if (color != null) {
				cell.setForeground(color);
			}
		}
	}

	/**
	 * Constructs a new instance of that page
	 *
	 * @param wizard
	 *            the container wizard (most likely an {@link SmartImportWizard}
	 * @param initialSelection
	 *            initial selection (directory or archive)
	 * @param initialWorkingSets
	 *            initial working sets
	 */
	public SmartImportRootWizardPage(SmartImportWizard wizard, File initialSelection,
			Set<IWorkingSet> initialWorkingSets) {
		super(SmartImportRootWizardPage.class.getName());
		this.selection = initialSelection;
		this.workingSets = initialWorkingSets;
		if (this.workingSets == null) {
			this.workingSets = new HashSet<>();
		}
		setWizard(wizard);
	}

	@Override
	public void setWizard(IWizard easymportWizard) {
		Assert.isTrue(easymportWizard instanceof SmartImportWizard);
		super.setWizard(easymportWizard);
	}

	@Override
	public SmartImportWizard getWizard() {
		return (SmartImportWizard) super.getWizard();
	}

	@Override
	public void createControl(Composite parent) {
		setTitle(DataTransferMessages.SmartImportWizardPage_importProjectsInFolderTitle);
		setDescription(DataTransferMessages.SmartImportWizardPage_importProjectsInFolderDescription);
		initializeDialogUnits(parent);
		Composite res = new Composite(parent, SWT.NONE);
		res.setLayout(new GridLayout(4, false));

		createInputSelectionOptions(res);

		GridData proposalsGroupLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
		proposalsGroupLayoutData.verticalIndent = 12;
		createProposalsGroup(res).setLayoutData(proposalsGroupLayoutData);

		createConfigurationOptions(res);

		Group workingSetsGroup = new Group(res, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1);
		layoutData.verticalIndent = 20;
		workingSetsGroup.setLayoutData(layoutData);
		workingSetsGroup.setLayout(new GridLayout(1, false));
		workingSetsGroup.setText(DataTransferMessages.SmartImportWizardPage_workingSets);
		workingSetsBlock = new WorkingSetConfigurationBlock(getDialogSettings(),
				"org.eclipse.ui.resourceWorkingSetPage"); //$NON-NLS-1$
		if (this.workingSets != null) {
			workingSetsBlock.setWorkingSets(this.workingSets.toArray(new IWorkingSet[this.workingSets.size()]));
		}
		workingSetsBlock.createContent(workingSetsGroup);

		if (this.selection != null) {
			rootDirectoryText.setText(this.selection.getAbsolutePath());
			validatePage();
		}

		setControl(res);
	}

	/**
	 * @param res
	 */
	private void createInputSelectionOptions(Composite res) {
		Label rootDirectoryLabel = new Label(res, SWT.NONE);
		rootDirectoryLabel.setText(DataTransferMessages.SmartImportWizardPage_selectRootDirectory);
		rootDirectoryText = new Combo(res, SWT.BORDER);
		String[] knownSources = getWizard().getDialogSettings().getArray(IMPORTED_SOURCES);
		if (knownSources != null) {
			rootDirectoryText.setItems(knownSources);
		}
		GridData rootDirectoryTextLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		rootDirectoryText.setLayoutData(rootDirectoryTextLayoutData);
		rootDirectoryText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				SmartImportRootWizardPage.this.selection = new File(((Combo) e.widget).getText());
				if (SmartImportWizard.isValidArchive(selection)) {
					if (SmartImportWizard.getExpandDirectory(selection).isDirectory()) {
						if (MessageDialog.openConfirm(getShell(),
								DataTransferMessages.SmartImportWizardPage_overwriteArchiveDirectory_title,
								NLS.bind(DataTransferMessages.SmartImportWizardPage_overwriteArchiveDirectory_message,
										SmartImportWizard.getExpandDirectory(selection)))) {
							expandSelectedArchive();
						}
					} else {
						expandSelectedArchive();
					}
				}
				validatePage();
				refreshProposals();
			}

			private void expandSelectedArchive() {
				try {
					getContainer().run(true, true, new IRunnableWithProgress() {
						@Override
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException, InterruptedException {
							getWizard().expandArchive(selection, monitor);
							if (monitor.isCanceled()) {
								throw new InterruptedException();
							}
						}
					});
				} catch (Exception ex) {
					MessageDialog.openWarning(getShell(),
							DataTransferMessages.SmartImportWizardPage_incompleteExpand_title,
							NLS.bind(DataTransferMessages.SmartImportWizardPage_incompleteExpand_title,
									SmartImportWizard.getExpandDirectory(selection)));
				}
			}
		});
		this.rootDirectoryTextDecorator = new ControlDecoration(rootDirectoryText, SWT.TOP | SWT.LEFT);
		Image errorImage = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();
		rootDirectoryTextLayoutData.horizontalIndent += errorImage.getBounds().width;
		this.rootDirectoryTextDecorator.setImage(errorImage);
		this.rootDirectoryTextDecorator
				.setDescriptionText(DataTransferMessages.SmartImportWizardPage_incorrectRootDirectory);
		this.rootDirectoryTextDecorator.hide();
		Button directoryButton = new Button(res, SWT.PUSH);
		directoryButton.setText(DataTransferMessages.SmartImportWizardPage_browse);
		setButtonLayoutData(directoryButton);
		directoryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.setText(DataTransferMessages.SmartImportWizardPage_browseForFolder);
				dialog.setMessage(DataTransferMessages.SmartImportWizardPage_selectFolderOrArchiveToImport);
				if (rootDirectoryText.getText() != null) {
					File current = new File(rootDirectoryText.getText());
					if (current.isDirectory()) {
						dialog.setFilterPath(current.getAbsolutePath());
					} else if (current.isFile()) {
						dialog.setFilterPath(current.getParentFile().getAbsolutePath());
					}
				}
				if (dialog.getFilterPath() == null) {
					dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().toString());
				}
				String directory = dialog.open();
				if (directory != null) {
					rootDirectoryText.setText(directory);
				}
			}
		});
		Button browseArchiveButton = new Button(res, SWT.PUSH);
		browseArchiveButton.setText(DataTransferMessages.SmartImportWizardPage_selectArchiveButton);
		setButtonLayoutData(browseArchiveButton);
		browseArchiveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell());
				dialog.setText(DataTransferMessages.SmartImportWizardPage_selectArchiveTitle);
				dialog.setFilterExtensions(new String[] { "*.zip;*.tar;*.tar.gz" }); //$NON-NLS-1$
				dialog.setFilterNames(new String[] { DataTransferMessages.SmartImportWizardPage_allSupportedArchives });
				if (rootDirectoryText.getText() != null) {
					File current = new File(rootDirectoryText.getText());
					if (current.isDirectory()) {
						dialog.setFilterPath(current.getAbsolutePath());
					} else if (current.isFile()) {
						dialog.setFilterPath(current.getParentFile().getAbsolutePath());
						dialog.setFileName(current.getName());
					}
				}
				if (dialog.getFilterPath() == null) {
					dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().toString());
				}
				String archive = dialog.open();
				if (archive != null) {
					rootDirectoryText.setText(archive);
				}
			}
		});
	}

	/**
	 * Creates the UI elements for the import options
	 */
	private void createConfigurationOptions(Composite res) {
		Link showDetectorsLink = new Link(res, SWT.NONE);
		showDetectorsLink.setText(DataTransferMessages.SmartImportWizardPage_showAvailableDetectors);
		showDetectorsLink.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		showDetectorsLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StringBuilder message = new StringBuilder();
				message.append(DataTransferMessages.SmartImportWizardPage_availableDetectors_description);
				message.append('\n');
				message.append('\n');
				List<String> extensionsLabels = new ArrayList<>(
						ProjectConfiguratorExtensionManager.getAllExtensionLabels());
				Collections.sort(extensionsLabels);
				for (String extensionLabel : extensionsLabels) {
					message.append("* "); //$NON-NLS-1$
					message.append(extensionLabel);
					message.append('\n');
				}
				MessageDialog.openInformation(getShell(),
						DataTransferMessages.SmartImportWizardPage_availableDetectors_title, message.toString());
			}
		});
		final Button detectNestedProjectsCheckbox = new Button(res, SWT.CHECK);
		detectNestedProjectsCheckbox.setText(DataTransferMessages.SmartImportWizardPage_detectNestedProjects);
		detectNestedProjectsCheckbox.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		detectNestedProjectsCheckbox.setSelection(this.detectNestedProjects);
		detectNestedProjectsCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SmartImportRootWizardPage.this.detectNestedProjects = detectNestedProjectsCheckbox.getSelection();
				refreshProposals();
			}
		});
		final Button configureProjectsCheckbox = new Button(res, SWT.CHECK);
		configureProjectsCheckbox.setText(DataTransferMessages.SmartImportWizardPage_configureProjects);
		configureProjectsCheckbox.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		configureProjectsCheckbox.setSelection(this.configureProjects);
		configureProjectsCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SmartImportRootWizardPage.this.configureProjects = configureProjectsCheckbox.getSelection();
				refreshProposals();
			}
		});

	}

	/**
	 * @param res
	 */
	private Composite createProposalsGroup(Composite parent) {
		Composite res = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(res);
		PatternFilter patternFilter = new PatternFilter();
		patternFilter.setIncludeLeadingWildcard(true);
		FilteredTree filterTree = new FilteredTree(res, SWT.BORDER | SWT.CHECK, patternFilter, true) {
			@Override
			public CheckboxTreeViewer doCreateTreeViewer(Composite treeParent, int style) {
				return new CheckboxTreeViewer(treeParent, style);
			}
		};
		tree = (CheckboxTreeViewer) filterTree.getViewer();
		GridData treeGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		treeGridData.heightHint = 90;
		tree.getControl().setLayoutData(treeGridData);
		tree.setContentProvider(new ITreeContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				Map<File, ?> potentialProjects = (Map<File, ?>) inputElement;
				return potentialProjects.keySet().toArray(new File[potentialProjects.size()]);
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				return null;
			}

			@Override
			public Object getParent(Object element) {
				return null;
			}

			@Override
			public boolean hasChildren(Object element) {
				return false;
			}

		});
		tree.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer v, Object o1, Object o2) {
				return ((File) o1).getAbsolutePath().compareTo(((File) o2).getAbsolutePath());
			}
		});
		tree.setCheckStateProvider(new ICheckStateProvider() {
			@Override
			public boolean isGrayed(Object element) {
				return false;
			}

			@Override
			public boolean isChecked(Object element) {
				return getWizard().getImportJob() == null || getWizard().getImportJob().getDirectoriesToImport() == null
						|| getWizard().getImportJob().getDirectoriesToImport().contains(element);
			}
		});
		tree.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (SmartImportRootWizardPage.this.alreadyExistingProjects.contains(event.getElement())) {
					tree.setChecked(event.getElement(), false);
				} else {
					proposalsSelectionChanged();
				}
			}
		});

		tree.getTree().setHeaderVisible(true);
		ViewerColumn pathColumn = new TreeViewerColumn(tree, SWT.NONE);
		pathColumn.setLabelProvider(new FolderForProjectsLabelProvider());
		tree.getTree().getColumn(0).setText(DataTransferMessages.SmartImportProposals_folder);
		tree.getTree().getColumn(0).setWidth(400);
		ViewerColumn projectTypeColumn = new TreeViewerColumn(tree, SWT.NONE);
		projectTypeColumn.setLabelProvider(new ProjectConfiguratorLabelProvider());
		tree.getTree().getColumn(1).setText(DataTransferMessages.SmartImportProposals_importAs);
		tree.getTree().getColumn(1).setWidth(250);

		this.proposalSelectionDecorator = new ControlDecoration(tree.getTree(), SWT.TOP | SWT.LEFT);
		Image errorImage = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
				.getImage();
		treeGridData.horizontalIndent += errorImage.getBounds().width;
		this.proposalSelectionDecorator.setImage(errorImage);
		this.proposalSelectionDecorator
				.setDescriptionText(DataTransferMessages.SmartImportWizardPage_selectAtLeastOneFolderToOpenAsProject);
		this.proposalSelectionDecorator.hide();

		Composite selectionButtonsGroup = new Composite(res, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(selectionButtonsGroup);
		selectionButtonsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		Button selectAllButton = new Button(selectionButtonsGroup, SWT.PUSH);
		setButtonLayoutData(selectAllButton);
		selectAllButton.setText(DataTransferMessages.DataTransfer_selectAll);
		selectAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tree.setCheckedElements(SmartImportRootWizardPage.this.notAlreadyExistingProjects.toArray());
				proposalsSelectionChanged();
			}
		});
		Button deselectAllButton = new Button(selectionButtonsGroup, SWT.PUSH);
		setButtonLayoutData(deselectAllButton);
		deselectAllButton.setText(DataTransferMessages.DataTransfer_deselectAll);
		deselectAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tree.setCheckedElements(new Object[0]);
				proposalsSelectionChanged();
			}
		});

		selectionSummary = new Label(selectionButtonsGroup, SWT.NONE);
		selectionSummary.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true, 1, 1));
		selectionSummary.setText(NLS.bind(DataTransferMessages.SmartImportProposals_selectionSummary, 0, 0));
		Button hideProjectsAlreadyInWorkspace = new Button(selectionButtonsGroup, SWT.CHECK);
		hideProjectsAlreadyInWorkspace.setText(DataTransferMessages.SmartImportProposals_hideExistingProjects);
		hideProjectsAlreadyInWorkspace.addSelectionListener(new SelectionAdapter() {
			final ViewerFilter existingProjectsFilter = new ViewerFilter() {
				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					return !alreadyExistingProjects.contains(element);
				}
			};

			@Override
			public void widgetSelected(SelectionEvent e) {
				ViewerFilter[] currentFilters = tree.getFilters();
				ViewerFilter[] newFilters = null;
				if (((Button) e.widget).getSelection()) {
					newFilters = new ViewerFilter[currentFilters.length + 1];
					System.arraycopy(currentFilters, 0, newFilters, 0, currentFilters.length);
					newFilters[newFilters.length - 1] = existingProjectsFilter;
				} else {
					List<ViewerFilter> filters = new ArrayList<>(
							currentFilters.length > 0 ? currentFilters.length - 1 : 0);
					for (ViewerFilter filter : currentFilters) {
						if (filter != existingProjectsFilter) {
							filters.add(filter);
						}
					}
					newFilters = filters.toArray(new ViewerFilter[filters.size()]);
				}
				tree.setFilters(newFilters);
			}
		});
		tree.setInput(Collections.emptyMap());

		return res;
	}

	protected void validatePage() {
		// reset error message
		setErrorMessage(null);
		// order of invocation of setErrorMessage == reverse order of priority
		// ie: most important one must call setErrorMessage last
		if (tree.getCheckedElements().length == 0) {
			this.proposalSelectionDecorator.show();
			setErrorMessage(this.proposalSelectionDecorator.getDescriptionText());
		} else {
			this.proposalSelectionDecorator.hide();
		}

		if (!sourceIsValid()) {
			this.rootDirectoryTextDecorator.show();
			setErrorMessage(this.rootDirectoryTextDecorator.getDescriptionText());
		} else {
			this.rootDirectoryTextDecorator.hide();
		}
		setPageComplete(isPageComplete());
	}

	@Override
	public boolean isPageComplete() {
		return sourceIsValid() && getWizard().getImportJob() != null
				&& tree.getCheckedElements().length > 0;
	}

	private boolean sourceIsValid() {
		return this.selection != null
				&& (this.selection.isDirectory() || SmartImportWizard.isValidArchive(this.selection));
	}

	/**
	 *
	 * @return The selected source of import (can be a directory or an archive)
	 */
	public File getSelectedRoot() {
		return this.selection;
	}

	/**
	 * Sets the initial source to import.
	 *
	 * @param directoryOrArchive
	 */
	public void setInitialImportRoot(File directoryOrArchive) {
		this.selection = directoryOrArchive;
		this.rootDirectoryText.setText(directoryOrArchive.getAbsolutePath());
		refreshProposals();
	}

	/**
	 *
	 * @return The user selected working sets to assign to imported projects.
	 */
	public Set<IWorkingSet> getSelectedWorkingSets() {
		this.workingSets.clear();
		// workingSetsBlock doesn't support listeners...
		Runnable workingSetsRetriever = new Runnable() {
			@Override
			public void run() {
				for (IWorkingSet workingSet : SmartImportRootWizardPage.this.workingSetsBlock.getSelectedWorkingSets()) {
					SmartImportRootWizardPage.this.workingSets.add(workingSet);
				}
			}
		};
		if (Display.getCurrent() == null) {
			getContainer().getShell().getDisplay().syncExec(workingSetsRetriever);
		} else {
			workingSetsRetriever.run();
		}
		return this.workingSets;
	}

	private void proposalsSelectionChanged() {
		if (getWizard().getImportJob() != null) {
			Map<File, Collection<?>> input = (Map<File, Collection<?>>) tree.getInput();
			if (input.size() == 1 && input.values().iterator().next().isEmpty()) {
				getWizard().getImportJob().setDirectoriesToImport(null);
				getWizard().getImportJob().setExcludedDirectories(null);
				selectionSummary.setText(NLS.bind(DataTransferMessages.SmartImportProposals_selectionSummary,
						0, tree.getCheckedElements().length));
			} else {
				Object[] selected = tree.getCheckedElements();
				Set<File> excludedDirectories = new HashSet(((Map<File, ?>) this.tree.getInput()).keySet());
				Set<File> selectedProjects = new HashSet<>();
				for (Object item : selected) {
					File directory = (File) item;
					excludedDirectories.remove(directory);
					selectedProjects.add(directory);
				}
				getWizard().getImportJob().setDirectoriesToImport(selectedProjects);
				getWizard().getImportJob().setExcludedDirectories(excludedDirectories);
				selectionSummary.setText(NLS.bind(DataTransferMessages.SmartImportProposals_selectionSummary,
						selectedProjects.size(),
						potentialProjects.size()));
			}
		}
		setPageComplete(isPageComplete());
	}

	/**
	 * @return whether the job should recurse to find nested projects
	 */
	public boolean isDetectNestedProject() {
		return this.detectNestedProjects;
	}

	/**
	 * @return whether the job should configure projects (add natures etc...)
	 */
	public boolean isConfigureProjects() {
		return this.configureProjects;
	}

	private void refreshProposals() {
		stopAndDisconnectCurrentWork();
		SmartImportRootWizardPage.this.potentialProjects = Collections.emptyMap();
		SmartImportRootWizardPage.this.notAlreadyExistingProjects = Collections.emptySet();
		SmartImportRootWizardPage.this.alreadyExistingProjects = Collections.emptySet();
		proposalsUpdated();
		// compute new state
		if (sourceIsValid()) {
			tree.getControl().setEnabled(false);
			TreeItem computingItem = new TreeItem(tree.getTree(), SWT.DEFAULT);
			computingItem
					.setText(NLS.bind(DataTransferMessages.SmartImportJob_inspecting, selection.getAbsolutePath()));
			final SmartImportJob importJob = getWizard().getImportJob();
			refreshProposalsJob = new Job(
					NLS.bind(DataTransferMessages.SmartImportJob_inspecting, selection.getAbsolutePath())) {
				@Override
				public IStatus run(IProgressMonitor monitor) {
					SmartImportRootWizardPage.this.potentialProjects = importJob.getImportProposals(monitor);
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
					if (!potentialProjects.containsKey(importJob.getRoot())) {
						potentialProjects.put(importJob.getRoot(), Collections.emptyList());
					}

					SmartImportRootWizardPage.this.notAlreadyExistingProjects = new HashSet<>(
							potentialProjects.keySet());
					SmartImportRootWizardPage.this.alreadyExistingProjects = new HashSet<>();
					for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
						IPath location = project.getLocation();
						if (location == null) {
							continue;
						}
						SmartImportRootWizardPage.this.notAlreadyExistingProjects.remove(location.toFile());
						SmartImportRootWizardPage.this.alreadyExistingProjects.add(location.toFile());
					}
					return Status.OK_STATUS;
				}
			};
			Control previousFocusControl = tree.getControl().getDisplay().getFocusControl();
			if (previousFocusControl == null) {
				previousFocusControl = rootDirectoryText;
			}
			Point initialSelection = rootDirectoryText.getSelection();
			wizardProgressMonitor.get().attachToCancelComponent(null);
			wizardProgressMonitor.get().setVisible(true);
			// restore focus and selection because IWizardDialog.run(...) and
			// attachToCancelComponent take them
			previousFocusControl.setFocus();
			rootDirectoryText.setSelection(initialSelection);
			ToolItem stopButton = getStopButton(wizardProgressMonitor.get());
			stopButton.addSelectionListener(this.cancelWorkListener);
			jobMonitor = ProgressManager.getInstance().progressFor(refreshProposalsJob);
			delegateMonitor = new DelegateProgressMonitorInUIThreadAndPreservingFocus(wizardProgressMonitor.get());
			jobMonitor.addProgressListener(delegateMonitor);
			refreshProposalsJob.setPriority(Job.INTERACTIVE);
			refreshProposalsJob.setUser(true);
			refreshProposalsJob.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					Control control = tree.getControl();
					if (!control.isDisposed()) {
						control.getDisplay().asyncExec(() -> {
							IStatus result = event.getResult();
							if (!control.isDisposed() && result.isOK()) {
								computingItem.dispose();
								if (sourceIsValid() && getWizard().getImportJob() == importJob) {
									proposalsUpdated();
								}
								tree.getTree().setEnabled(true);
							} else if (result.getCode() == IStatus.CANCEL) {
								computingItem.setText(DataTransferMessages.SmartImportProposals_inspecitionCanceled);
							} else if (result.getCode() == IStatus.ERROR) {
								computingItem.setText(
										NLS.bind(DataTransferMessages.SmartImportProposals_errorWhileInspecting,
												result.getMessage()));
							}
							if (!wizardProgressMonitor.get().isDisposed()
									&& refreshProposalsJob.getState() == Job.NONE) {
								wizardProgressMonitor.get().setVisible(false);
							}
						});
					}
				}
			});
			refreshProposalsJob.schedule(0);
		}
	}

	private static ToolItem getStopButton(ProgressMonitorPart part) {
		for (Control control : part.getChildren()) {
			if (control instanceof ToolBar) {
				for (ToolItem item : ((ToolBar) control).getItems()) {
					if (item.getToolTipText().equals(JFaceResources.getString("ProgressMonitorPart.cancelToolTip"))) { //$NON-NLS-1$ ))
						return item;
					}
				}
			}
		}
		return null;
	}

	private void stopAndDisconnectCurrentWork() {
		if (refreshProposalsJob != null) {
			refreshProposalsJob.cancel();
		}
	}

	private void proposalsUpdated() {
		tree.setInput(potentialProjects);
		tree.setCheckedElements(this.notAlreadyExistingProjects.toArray());
		proposalsSelectionChanged();
		validatePage();
	}

	@Override
	public void dispose() {
		stopAndDisconnectCurrentWork();
		getStopButton(wizardProgressMonitor.get()).removeSelectionListener(this.cancelWorkListener);
		super.dispose();
	}

	/**
	 * Only made public for testing purpose
	 *
	 * @return the Wizard progress monitor
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public ProgressMonitorPart getWizardProgressMonitor() {
		return this.wizardProgressMonitor.get();
	}

}

