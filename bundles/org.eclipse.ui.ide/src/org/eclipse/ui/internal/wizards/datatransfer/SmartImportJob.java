/*******************************************************************************
 * Copyright (c) 2014-2016 Red Hat Inc., and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobGroup;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.wizards.datatransfer.ProjectConfigurator;

/**
 * The {@link SmartImportJob} is a Job to import a given folder or archive. It
 * detects the nested projects in the given source and configure projects
 * according to the metadata it could find. The behavior is extensible, and
 * contributors can add a {@link ProjectConfigurator} strategy via extension
 * point to add support for more project kinds.
 *
 * @since 3.12
 *
 */
public class SmartImportJob extends Job {

	/*
	 * Input parameters
	 */
	private File rootDirectory;
	private Set<File> directoriesToImport;
	private Set<File> excludedDirectories;
	private boolean discardRootProject;
	private boolean deepChildrenDetection;
	private boolean configureProjects;
	private boolean reconfigureEclipseProjects;
	private IWorkingSet[] workingSets;

	/*
	 * working fields
	 */
	private IProject rootProject;
	private IWorkspaceRoot workspaceRoot;
	private ProjectConfiguratorExtensionManager configurationManager;
	private RecursiveImportListener listener;

	protected Map<File, List<ProjectConfigurator>> importProposals;
	private Map<IProject, List<ProjectConfigurator>> report;
	private Map<IPath, Exception> errors;

	private JobGroup crawlerJobGroup;

	/**
	 * Builds a new instance of the job
	 *
	 * @param rootDirectory
	 *            the root directory to import and analyze
	 * @param workingSets
	 *            working sets to assign to imported projects
	 * @param configureProjects
	 *            whether we want to configure projects (natures etc...)
	 *            according to their metadata
	 * @param recuriveChildrenDetection
	 *            whether to recurse for detection of nested projects
	 */
	public SmartImportJob(File rootDirectory, Set<IWorkingSet> workingSets, boolean configureProjects, boolean recuriveChildrenDetection) {
		super(rootDirectory.getAbsolutePath());
		this.workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		this.rootDirectory = rootDirectory;
		if (workingSets != null) {
			this.workingSets = workingSets.toArray(new IWorkingSet[workingSets.size()]);
		} else {
			this.workingSets = new IWorkingSet[0];
		}
		this.configureProjects = configureProjects;
		this.deepChildrenDetection = recuriveChildrenDetection;
		this.report = Collections.synchronizedMap(new HashMap<IProject, List<ProjectConfigurator>>());
		this.errors = Collections.synchronizedMap(new HashMap<IPath, Exception>());
		this.crawlerJobGroup = new JobGroup(DataTransferMessages.SmartImportJob_detectAndConfigureProjects, 0, 1);
	}

	/**
	 * @return The root directory for the import operation
	 */
	public File getRoot() {
		return this.rootDirectory;
	}

	/**
	 * Sets the directories that have been detected by preliminary detection and that
	 * user has selected to import. Those will be imported and configured in any case.
	 * This does not impact output of {@link #getImportProposals(IProgressMonitor)}
	 * @param directories
	 */
	public void setDirectoriesToImport(Set<File> directories) {
		this.directoriesToImport = directories;
	}

	/**
	 * Set directories that users specifically configured as to NOT import.
	 * Projects UNDER those directories may be imported, but never project directly
	 * in one of those directories.
	 * This does not impact output of {@link #getImportProposals(IProgressMonitor)}
	 * @param directories
	 */
	public void setExcludedDirectories(Set<File> directories) {
		this.excludedDirectories = directories;
	}

	/**
	 * Adds a listener to be notified of progress (detection/configuration of
	 * sub-projects)
	 *
	 * @param listener
	 */
	public void setListener(RecursiveImportListener listener) {
		this.listener = listener;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceDescription description = workspace.getDescription();
			boolean isAutoBuilding = workspace.isAutoBuilding();
			if (isAutoBuilding) {
				description.setAutoBuilding(false);
				workspace.setDescription(description);
			}

			if (directoriesToImport != null) {
				this.deepChildrenDetection = false;
				SubMonitor subMonitor = SubMonitor.convert(monitor,
						directoriesToImport.size() * (configureProjects ? 2 : 1) + 1);
				subMonitor.beginTask(DataTransferMessages.SmartImportJob_crawling, directoriesToImport.size() + 1);
				Comparator<File> rootToLeafComparator = new Comparator<File>() {
					@Override
					public int compare(File arg0, File arg1) {
						int lengthDiff = arg0.getAbsolutePath().length() - arg1.getAbsolutePath().length();
						if (lengthDiff != 0) {
							return lengthDiff;
						}
						return arg0.compareTo(arg1);
					}
				};
				SortedSet<File> directories = new TreeSet<>(rootToLeafComparator);
				directories.addAll(this.directoriesToImport);
				SortedMap<File, IProject> leafToRootProjects = new TreeMap<>(Collections.reverseOrder(rootToLeafComparator));
				final Set<IProject> alreadyConfiguredProjects = new HashSet<>();
				subMonitor.worked(1);
				for (final File directoryToImport : directories) {
					final boolean alreadyAnEclipseProject = new File(directoryToImport, IProjectDescription.DESCRIPTION_FILE_NAME).isFile();
					try {
						IProject newProject = toExistingOrNewProject(directoryToImport, subMonitor,
								IResource.BACKGROUND_REFRESH);
						if (alreadyAnEclipseProject) {
							alreadyConfiguredProjects.add(newProject);
						}
						leafToRootProjects.put(directoryToImport, newProject);
						subMonitor.worked(1);
					} catch (CouldNotImportProjectException ex) {
						Path path = new Path(directoryToImport.getAbsolutePath());
						if (listener != null) {
							listener.errorHappened(path, ex);
						}
						this.errors.put(path, ex);
					}
				}
				if (configureProjects) {
					JobGroup multiDirectoriesJobGroup = new JobGroup(
							DataTransferMessages.SmartImportJob_configuringSelectedDirectories, 20, 1);
					SubMonitor mon = subMonitor.split(directoriesToImport.size());
					for (final IProject newProject : leafToRootProjects.values()) {
						Job directoryJob = new Job(
								NLS.bind(DataTransferMessages.SmartImportJob_configuring, newProject.getName())) {
							@Override
							protected IStatus run(IProgressMonitor aMonitor) {
								SubMonitor monitor = null;
								if (aMonitor instanceof SubMonitor) {
									monitor = (SubMonitor) aMonitor;
								} else {
									monitor = SubMonitor.convert(aMonitor);
								}
								try {
									importProjectAndChildrenRecursively(newProject,
											!alreadyConfiguredProjects.contains(newProject), monitor);
									return Status.OK_STATUS;
								} catch (Exception ex) {
									return new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH, ex.getMessage(),
											ex);
								}
							}
						};
						// Job1 on path1 and Job2 on path2 can be run in parallel IFF path1 isn't a prefix of path2 and vice-versa
						directoryJob.setRule(new SubdirectoryOrSameNameSchedulingRule(newProject));
						directoryJob.setUser(true);
						directoryJob.setJobGroup(multiDirectoriesJobGroup);
						directoryJob.schedule();
					}
					multiDirectoriesJobGroup.join(0, mon);
				}


			} else { // no specific projects included, consider only root
				SubMonitor subMonitor = SubMonitor.convert(monitor);
				File rootProjectFile = new File(this.rootDirectory, IProjectDescription.DESCRIPTION_FILE_NAME);
				boolean isRootANewProject = !rootProjectFile.isFile();
				this.rootProject = toExistingOrNewProject(this.rootDirectory, subMonitor, IResource.NONE);

				if (this.configureProjects) {
					importProjectAndChildrenRecursively(this.rootProject, isRootANewProject, subMonitor);

					if (isRootANewProject && rootProjectWorthBeingRemoved()) {
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								discardRootProject = MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
										DataTransferMessages.SmartImportJob_discardRootProject_title,
										DataTransferMessages.SmartImportJob_discardRootProject_description);
							}
						});
						if (this.discardRootProject) {
							this.rootProject.delete(false, true, subMonitor);
							if (isRootANewProject) {
								rootProjectFile.delete();
							}
							this.report.remove(this.rootProject);
						}
					}
				}
			}

			if (isAutoBuilding) {
				description.setAutoBuilding(true);
				workspace.setDescription(description);
			}
		} catch (Exception ex) {
			return new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH, ex.getMessage(), ex);
		}
		return Status.OK_STATUS;
	}

	protected boolean rootProjectWorthBeingRemoved() {
		if (this.report.size() == 1) {
			return false;
		}
		List<ProjectConfigurator> rootProjectConfigurators = this.report.get(this.rootProject);
		if (rootProjectConfigurators.isEmpty()) {
			return true;
		}
		boolean areOnlyDummyConfigurators = true;
		for (ProjectConfigurator configurator : rootProjectConfigurators) {
			// TODO: semantics whether configurator is "strong enough" for a root project should be put inside configurator
			areOnlyDummyConfigurators &= (configurator instanceof EclipseProjectConfigurator || configurator instanceof EclipseWorkspaceConfigurator);
		}
		return areOnlyDummyConfigurators;
	}


	private final class CrawlFolderJob extends Job {
		private final IFolder childFolder;
		private final Set<IProject> res;

		private CrawlFolderJob(String name, IFolder childFolder, Set<IProject> res) {
			super(name);
			this.childFolder = childFolder;
			this.res = res;
		}

		@Override
		public IStatus run(IProgressMonitor progressMonitor) {
			SubMonitor subMonitor = null;
			if (progressMonitor instanceof SubMonitor) {
				subMonitor = (SubMonitor) progressMonitor;
			} else {
				subMonitor = SubMonitor.convert(progressMonitor);
			}
			try {
				Set<IProject> projectFromCurrentContainer = importProjectAndChildrenRecursively(childFolder, false,
						subMonitor);
				res.addAll(projectFromCurrentContainer);
				return Status.OK_STATUS;
			} catch (Exception ex) {
				return new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH, ex.getMessage(), ex);
			}
		}
	}

	private Set<IProject> searchAndImportChildrenProjectsRecursively(IContainer parentContainer, Set<IPath> directoriesToExclude, final IProgressMonitor progressMonitor) throws Exception {
		for (IProject processedProjects : Collections.synchronizedSet(this.report.keySet())) {
			if (processedProjects.getLocation().equals(parentContainer.getLocation())) {
				return Collections.emptySet();
			}
		}
		parentContainer.refreshLocal(IResource.DEPTH_ONE, progressMonitor); // make sure we know all children
		Set<IFolder> childrenToProcess = new HashSet<>();
		final Set<IProject> res = Collections.synchronizedSet(new HashSet<IProject>());
		for (IResource childResource : parentContainer.members()) {
			if (progressMonitor.isCanceled()) {
				throw new InterruptedException();
			}
			if (childResource.getType() == IResource.FOLDER && !childResource.isDerived()) {
				boolean excluded = false;
				if (directoriesToExclude != null) {
					for (IPath excludedPath : directoriesToExclude) {
						if (!excludedPath.isPrefixOf(parentContainer.getLocation()) && excludedPath.isPrefixOf(childResource.getLocation())) {
							excluded = true;
						}
					}
				}
				if (!excluded) {
					childrenToProcess.add((IFolder)childResource);
				}
			}
		}

		Set<CrawlFolderJob> jobs = new HashSet<>();
		for (final IFolder childFolder : childrenToProcess) {
			if (progressMonitor.isCanceled()) {
				throw new InterruptedException();
			}
			CrawlFolderJob crawlerJob = new CrawlFolderJob(
					NLS.bind(DataTransferMessages.SmartImportJob_crawling, childFolder.getLocation().toString()),
					childFolder, res);
			if (crawlerJobGroup.getMaxThreads() == 0 || crawlerJobGroup.getActiveJobs().size() < crawlerJobGroup.getMaxThreads()) {
				crawlerJob.setJobGroup(crawlerJobGroup);
				jobs.add(crawlerJob);
				crawlerJob.schedule();
			} else {
				crawlerJob.run(progressMonitor);
			}
		}
		for (CrawlFolderJob job : jobs) {
			if (progressMonitor.isCanceled()) {
				throw new InterruptedException();
			}
			job.join();
		}
		return res;
	}

	private Set<IProject> importProjectAndChildrenRecursively(IContainer container, boolean forceFullProjectCheck,
			SubMonitor progressMonitor) throws Exception {
		SubMonitor monitor = progressMonitor.split(1);
		monitor.beginTask(null, ProjectConfiguratorExtensionManager.getAllExtensionLabels().size() * 2);
		progressMonitor.setTaskName(NLS.bind(DataTransferMessages.SmartImportJob_inspecting,
				container.getLocation().toFile().getAbsolutePath()));
		Set<IProject> projectFromCurrentContainer = new HashSet<>();
		boolean isAlreadyAnEclipseProject = false;
		Set<ProjectConfigurator> mainProjectConfigurators = new HashSet<>();
		Set<IPath> excludedPaths = new HashSet<>();
		if (this.excludedDirectories != null) {
			for (File excludedDirectory : this.excludedDirectories) {
				excludedPaths.add(new Path(excludedDirectory.getAbsolutePath()));
			}
		}
		container.refreshLocal(IResource.DEPTH_INFINITE, progressMonitor);
		if (!forceFullProjectCheck) {
			EclipseProjectConfigurator eclipseProjectConfigurator = new EclipseProjectConfigurator();
			if (eclipseProjectConfigurator.shouldBeAnEclipseProject(container, monitor)) {
				isAlreadyAnEclipseProject = true;
			}
		}

		if (this.configurationManager == null) {
			this.configurationManager = new ProjectConfiguratorExtensionManager();
		}
		Collection<ProjectConfigurator> activeConfigurators = this.configurationManager.getAllActiveProjectConfigurators(container);
		Set<ProjectConfigurator> potentialSecondaryConfigurators = new HashSet<>();
		IProject project = null;
		for (ProjectConfigurator configurator : activeConfigurators) {
			if (monitor.isCanceled()) {
				return null;
			}
			// exclude Eclipse project configurator for root project if is new
			if (configurator instanceof EclipseProjectConfigurator && forceFullProjectCheck) {
				continue;
			}
			if (configurator.shouldBeAnEclipseProject(container, monitor)) {
				mainProjectConfigurators.add(configurator);
				if (project == null) {
					// Create project
					try {
						project = toExistingOrNewProject(container.getLocation().toFile(), monitor,
								IResource.BACKGROUND_REFRESH);
					} catch (CouldNotImportProjectException ex) {
						this.errors.put(container.getLocation(), ex);
						if (this.listener != null) {
							this.listener.errorHappened(container.getLocation(), ex);
						}
						return projectFromCurrentContainer;
					}
					projectFromCurrentContainer.add(project);
				}
			} else {
				potentialSecondaryConfigurators.add(configurator);
			}
			monitor.worked(1);
		}

		if (!mainProjectConfigurators.isEmpty()) {
			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		}
		monitor.setWorkRemaining(activeConfigurators.size());
		for (ProjectConfigurator configurator : mainProjectConfigurators) {
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			if (configurator instanceof EclipseProjectConfigurator || !isAlreadyAnEclipseProject || this.reconfigureEclipseProjects) {
				configurator.configure(project, excludedPaths, monitor);
				this.report.get(project).add(configurator);
				if (this.listener != null) {
					listener.projectConfigured(project, configurator);
				}
			}
			excludedPaths.addAll(toPathSet(configurator.getDirectoriesToIgnore(project, monitor)));
			monitor.worked(1);
		}

		Set<IProject> allNestedProjects = new HashSet<>();
		if (deepChildrenDetection) {
			allNestedProjects.addAll( searchAndImportChildrenProjectsRecursively(container, excludedPaths, progressMonitor) );
			excludedPaths.addAll(toPathSet(allNestedProjects));
			projectFromCurrentContainer.addAll(allNestedProjects);
		}

		if (mainProjectConfigurators.isEmpty() && (!isAlreadyAnEclipseProject || forceFullProjectCheck)) {
			// Apply secondary configurators
			if (project == null) {
				// Create project
				try {
					project = toExistingOrNewProject(container.getLocation().toFile(), progressMonitor, IResource.BACKGROUND_REFRESH);
				} catch (CouldNotImportProjectException ex) {
					this.errors.put(container.getLocation(), ex);
					if (this.listener != null) {
						this.listener.errorHappened(container.getLocation(), ex);
					}
					return projectFromCurrentContainer;
				}
				projectFromCurrentContainer.add(project);
			}
			project.refreshLocal(IResource.DEPTH_ONE, monitor); // At least one,
																// maybe
																// INFINITE is
																// necessary
			progressMonitor.beginTask(
					NLS.bind(DataTransferMessages.SmartImportJob_continuingConfiguration, project.getName()),
					potentialSecondaryConfigurators.size());
			for (ProjectConfigurator additionalConfigurator : potentialSecondaryConfigurators) {
				if (progressMonitor.isCanceled()) {
					throw new InterruptedException();
				}
				if (additionalConfigurator.canConfigure(project, excludedPaths, progressMonitor)) {
					additionalConfigurator.configure(project, excludedPaths, monitor);
					this.report.get(project).add(additionalConfigurator);
					if (this.listener != null) {
						listener.projectConfigured(project, additionalConfigurator);
					}
					excludedPaths.addAll(toPathSet(additionalConfigurator.getDirectoriesToIgnore(project, monitor)));
				}
				monitor.worked(1);
			}
		}
		monitor.setWorkRemaining(0);
		return projectFromCurrentContainer;
	}

	private Set<IPath> toPathSet(Set<? extends IContainer> resources) {
		if (resources == null || resources.isEmpty()) {
			return Collections.emptySet();
		}
		Set<IPath> res = new HashSet<>();
		for (IContainer container : resources) {
			res.add(container.getLocation());
		}
		return res;
	}

	/**
	 * @param directory
	 * @param workingSets
	 * @param refreshMode One {@link IResource#BACKGROUND_REFRESH} for background refresh, or {@link IResource#NONE} for immediate refresh
	 * @return
	 * @throws Exception
	 */
	private IProject toExistingOrNewProject(File directory, IProgressMonitor progressMonitor, int refreshMode) throws CouldNotImportProjectException {
		try {
			progressMonitor.setTaskName(NLS.bind(DataTransferMessages.SmartImportJob_importingProjectIntoWorkspace,
					directory.getAbsolutePath()));
			IProject project = projectAlreadyExistsInWorkspace(directory);
			if (project == null) {
				project = createOrImportProject(directory, progressMonitor);
			}

			if (progressMonitor.isCanceled()) {
				return null;
			}
			project.open(refreshMode, progressMonitor);
			if (!this.report.containsKey(project)) {
				this.report.put(project, new ArrayList<ProjectConfigurator>());
			}
			if (this.listener != null) {
				this.listener.projectCreated(project);
			}
			return project;
		} catch (Exception ex) {
			throw new CouldNotImportProjectException(directory, ex);
		}
	}


	private IProject projectAlreadyExistsInWorkspace(File directory) {
		for (IProject project : workspaceRoot.getProjects()) {
			if (project.getLocation().toFile().getAbsoluteFile().equals(directory.getAbsoluteFile())) {
				return project;
			}
		}
		return null;
	}

	private IProject createOrImportProject(File directory, IProgressMonitor progressMonitor) throws Exception {
		IProjectDescription desc = null;
		File expectedProjectDescriptionFile = new File(directory, IProjectDescription.DESCRIPTION_FILE_NAME);
		if (expectedProjectDescriptionFile.exists()) {
			desc = ResourcesPlugin.getWorkspace().loadProjectDescription(new Path(expectedProjectDescriptionFile.getAbsolutePath()));
			String expectedName = desc.getName();
			IProject projectWithSameName = this.workspaceRoot.getProject(expectedName);
			if (projectWithSameName.exists()) {
				if (projectWithSameName.getLocation().toFile().equals(directory)) {
					// project seems already there
					return projectWithSameName;
				}
				throw new CouldNotImportProjectException(directory,
						NLS.bind(DataTransferMessages.SmartImportProposals_anotherProjectWithSameNameExists_description, expectedName));
			}
		} else {
			String projectName = directory.getName();
			if (this.workspaceRoot.getProject(directory.getName()).exists()) {
				int i = 1;
				do {
					projectName = directory.getName() + '(' + i + ')';
					i++;
				} while (this.workspaceRoot.getProject(projectName).exists());
			}

			desc = ResourcesPlugin.getWorkspace().newProjectDescription(projectName);
		}
		desc.setLocation(new Path(directory.getAbsolutePath()));
		IProject res = workspaceRoot.getProject(desc.getName());
		res.create(desc, progressMonitor);
		PlatformUI.getWorkbench().getWorkingSetManager().addToWorkingSets(res, this.workingSets);
		return res;
	}

	/**
	 *
	 * @return the project found/created for the root folder
	 */
	public IProject getRootProject() {
		return this.rootProject;
	}

	/**
	 *
	 * @return The list of projects found/imported and the strategy that were
	 *         used in order to configure them.
	 */
	public Map<IProject, List<ProjectConfigurator>> getConfiguredProjects() {
		return this.report;
	}

	/**
	 * @return the import errors that happened.
	 */
	public Map<IPath, Exception> getErrors() {
		return this.errors;
	}

	/**
	 *
	 * @param monitor
	 * @return the proposals for the import operation.
	 */
	public Map<File, List<ProjectConfigurator>> getImportProposals(IProgressMonitor monitor) {
		if (!this.deepChildrenDetection) {
			Map<File, List<ProjectConfigurator>> res = new HashMap<>();
			res.put(rootDirectory, Collections.emptyList());
			return res;
		}
		if (this.importProposals == null) {
			Map<File, List<ProjectConfigurator>> res = new HashMap<>();
			if (this.configurationManager == null) {
				this.configurationManager = new ProjectConfiguratorExtensionManager();
			}
			for (ProjectConfigurator configurator : configurationManager
					.getAllActiveProjectConfigurators(this.rootDirectory)) {
				Set<File> supportedFiles = configurator.findConfigurableLocations(SmartImportJob.this.rootDirectory,
						monitor);
				if (supportedFiles != null) {
					for (File supportedFile : supportedFiles) {
						if (!res.containsKey(supportedFile)) {
							res.put(supportedFile, new ArrayList<ProjectConfigurator>());
						}
						res.get(supportedFile).add(configurator);
					}
				}
			}
			this.importProposals = res;
		}
		return this.importProposals;
	}

	/**
	 * @return whether the job is set to configure projects (set natures and
	 *         other).
	 */
	public boolean isConfigureProjects() {
		return this.configureProjects;
	}

	/**
	 *
	 * @return whether the job will look for nested projects in case no
	 *         directory is passed to {@link #setDirectoriesToImport(Set)}
	 */
	public boolean isDetectNestedProjects() {
		return this.deepChildrenDetection;
	}

	/**
	 * Sets whether the job should look for nested projects. This value is
	 * ignored if consumer specifies directories to import via
	 * {@link #setDirectoriesToImport(Set)}.
	 *
	 * @param detectNestedProjects
	 */
	public void setDetectNestedProjects(boolean detectNestedProjects) {
		this.deepChildrenDetection = detectNestedProjects;
	}

	/**
	 * Forget the initial import proposals.
	 */
	public void resetProposals() {
		this.importProposals = null;
	}

	/**
	 *
	 * @return The directories that will be crawled for import
	 */
	public Set<File> getDirectoriesToImport() {
		return this.directoriesToImport;
	}

	@Override
	public boolean belongsTo(Object family) {
		return family == SmartImportJob.class;
	}

}
