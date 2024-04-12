/*******************************************************************************
 * Copyright (c) 2014-2017 Red Hat Inc., and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 *     Snjezana Peco (Red Hat Inc.)
 ******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.wizards.datatransfer.ProjectConfigurator;
import org.osgi.framework.FrameworkUtil;

/**
 * This {@link SmartImportWizard} allows user to control an import operation. It
 * takes as input a directory and assist user in proposing what and how to
 * import, relying and various strategies contributed as extension of
 * {@link ProjectConfigurator}
 *
 * @since 3.12
 */
public class SmartImportWizard extends Wizard implements IImportWizard {

	/**
	 * Expands an archive onto provided filesystem directory
	 * @since 3.12
	 */
	private static final class ExpandArchiveIntoFilesystemOperation implements IRunnableWithProgress {
		private File archive;
		private File destination;

		private ExpandArchiveIntoFilesystemOperation(File archive, File destination) {
			this.archive = archive;
			this.destination = destination;
		}

		@Override
		public void run(IProgressMonitor monitor) throws InvocationTargetException, OperationCanceledException {
			monitor.beginTask(NLS.bind(DataTransferMessages.SmartImportWizardPage_expandingArchive, archive.getName(), destination.getName()),
					1);
			try (ILeveledImportStructureProvider importStructureProvider = createImportStructureProvider()) {
				LinkedList<Object> toProcess = new LinkedList<>();
				toProcess.add(importStructureProvider.getRoot());
				while (!toProcess.isEmpty()) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					Object current = toProcess.pop();
					String path = importStructureProvider.getFullPath(current);
					File toCreate = null;
					if (path.equals("/")) { //$NON-NLS-1$
						toCreate = destination;
					} else {
						toCreate = new File(destination, path);
					}
					if (importStructureProvider.isFolder(current)) {
						toCreate.mkdirs();
					} else {
						try (InputStream content = importStructureProvider.getContents(current)) {
							// known IImportStructureProviders already log an
							// exception before returning null
							if (content != null) {
								Files.copy(content, toCreate.toPath());
							}
						}
					}
					List<?> children = importStructureProvider.getChildren(current);
					if (children != null) {
						toProcess.addAll(children);
					}
				}
				monitor.worked(1);
				monitor.done();
			} catch (Exception ex) {
				throw new InvocationTargetException(ex);
			}
		}

		private ILeveledImportStructureProvider createImportStructureProvider()
				throws TarException, IOException, ZipException {
			ILeveledImportStructureProvider importStructureProvider = null;
			if (ArchiveFileManipulations.isTarFile(archive.getAbsolutePath())) {
				importStructureProvider = new TarLeveledStructureProvider(new TarFile(archive));
			} else if (ArchiveFileManipulations.isZipFile(archive.getAbsolutePath())) {
				importStructureProvider = new ZipLeveledStructureProvider(new ZipFile(archive));
			}
			return importStructureProvider;
		}
	}

	private static final String SMART_IMPORT_SECTION_NAME = SmartImportWizard.class.getSimpleName();

	private File initialSelection;
	private Set<IWorkingSet> initialWorkingSets = new HashSet<>();
	private SmartImportRootWizardPage projectRootPage;
	private SmartImportJob easymportJob;
	/**
	 * the selected directory or the directory when archive got expanded
	 */
	private File directoryToImport;

	public SmartImportWizard() {
		super();
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
		IDialogSettings dialogSettings = getDialogSettings();
		if (dialogSettings == null) {
			dialogSettings = DialogSettings.getOrCreateSection(PlatformUI
					.getDialogSettingsProvider(FrameworkUtil.getBundle(SmartImportWizard.class)).getDialogSettings(),
					SMART_IMPORT_SECTION_NAME);
			setDialogSettings(dialogSettings);
		}
		setWindowTitle(DataTransferMessages.SmartImportWizardPage_importProjectsInFolderTitle);
		setDefaultPageImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/newprj_wiz.png")); //$NON-NLS-1$
	}

	/**
	 * Sets the initial directory or archive to import in workspace.
	 */
	public void setInitialImportSource(File directoryOrArchive) {
		this.initialSelection = directoryOrArchive;
	}

	/**
	 * Sets the initial selected working sets for the wizard
	 */
	public void setInitialWorkingSets(Set<IWorkingSet> workingSets) {
		this.initialWorkingSets = workingSets;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		if (selection != null) {
			for (Object item : selection) {
				File asFile = toFile(item);
				if (asFile != null && this.initialSelection == null) {
					this.initialSelection = asFile;
				} else {
					IWorkingSet asWorkingSet = Adapters.adapt(item, IWorkingSet.class);
					if (asWorkingSet != null) {
						this.initialWorkingSets.add(asWorkingSet);
					}
				}
			}
		}
	}

	/**
	 * Tries to infer a file location from given object, using various
	 * strategies
	 *
	 * @param o
	 *            an object
	 * @return a {@link File} associated to this object, or null.
	 */
	public static File toFile(Object o) {
		if (o instanceof File) {
			return (File)o;
		} else if (o instanceof IResource) {
			IPath location = ((IResource)o).getLocation();
			return location == null ? null : location.toFile();
		} else if (o instanceof IAdaptable) {
			IResource resource = ((IAdaptable)o).getAdapter(IResource.class);
			if (resource != null) {
				IPath location = resource.getLocation();
				return location == null ? null : location.toFile();
			}
		}
		return null;
	}

	/**
	 * Tries to infer a file path string from given object, using various
	 * strategies
	 *
	 * @param o
	 *            an object
	 * @return a {@link File#getAbsolutePath} associated to this object, or
	 *         empty string.
	 */
	public static String toAbsolutePath(Object o) {
		File file = toFile(o);
		return file == null ? "" : file.getAbsolutePath(); //$NON-NLS-1$
	}

	@Override
	public void addPages() {
		this.projectRootPage = new SmartImportRootWizardPage(this, this.initialSelection, this.initialWorkingSets);
		addPage(this.projectRootPage);
	}

	@Override
	public boolean performFinish() {
		String[] previousProposals = getDialogSettings().getArray(SmartImportRootWizardPage.IMPORTED_SOURCES);
		if (previousProposals == null) {
			previousProposals = new String[0];
		}
		if (!Arrays.asList(previousProposals).contains(this.projectRootPage.getSelectedRoot().getAbsolutePath())) {
			String[] newProposals = new String[previousProposals.length + 1];
			newProposals[0] = this.projectRootPage.getSelectedRoot().getAbsolutePath();
			System.arraycopy(previousProposals, 0, newProposals, 1, previousProposals.length);
			getDialogSettings().put(SmartImportRootWizardPage.IMPORTED_SOURCES, newProposals);
		}
		SmartImportJob job = createOrGetConfiguredImportJob();
		boolean runInBackground = WorkbenchPlugin.getDefault().getPreferenceStore()
				.getBoolean(IPreferenceConstants.RUN_IN_BACKGROUND);
		job.setProperty(IProgressConstants.PROPERTY_IN_DIALOG, runInBackground);
		job.schedule();
		return true;
	}

	/**
	 * Get or create the import job that will be processed by this wizard. Can be
	 * <code>null</code> (if the provided directory is invalid).
	 *
	 * <strong>IMPORTANT:</strong> If there was already a job but it didn't match
	 * the current configuration of the wizard then a new job will be created and
	 * returned.
	 *
	 * Regardless of whether or not the job was created, some configurations will be
	 * applied to it.
	 *
	 * @return the (newly created) import job
	 * @see #getCurrentImportJob()
	 */
	public SmartImportJob createOrGetConfiguredImportJob() {
		final File root = this.projectRootPage.getSelectedRoot();
		if (root == null) {
			return null;
		}
		if (root.isDirectory()) {
			this.directoryToImport = root;
		} else if (SmartImportWizard.isValidArchive(root)) {
			this.directoryToImport = getExpandDirectory(root);
			if (!directoryToImport.isDirectory()) {
				throw new IllegalArgumentException("Archive wasn't expanded first"); //$NON-NLS-1$
			}
		} else {
			return null;
		}

		if (this.easymportJob == null || !matchesPage(this.easymportJob, this.projectRootPage)) {
			this.easymportJob = new SmartImportJob(this.directoryToImport, projectRootPage.getSelectedWorkingSets(),
					projectRootPage.isConfigureProjects(), projectRootPage.isDetectNestedProject());
		}

		// always update working set on request as the job isn't updated on
		// WS change automatically
		this.easymportJob.setWorkingSets(projectRootPage.getSelectedWorkingSets());
		this.easymportJob.setCloseProjectsAfterImport(projectRootPage.isCloseProjectsAfterImport());

		return this.easymportJob;
	}

	/**
	 * Gets the current import job but it will not create it if it's
	 * <code>null</code>. If you need to create the job based on the current
	 * configuration of the wizard then you can call {@link #createOrGetConfiguredImportJob()}
	 *
	 * @return The current import job (it might be <code>null</code>).
	 * @see #createOrGetConfiguredImportJob()
	 */
	public SmartImportJob getCurrentImportJob() {
		return this.easymportJob;
	}

	static boolean isValidArchive(File file) {
		return ArchiveFileManipulations.isZipFile(file.getAbsolutePath())
				|| ArchiveFileManipulations.isTarFile(file.getAbsolutePath());
	}

	void expandArchive(final File archive, IProgressMonitor monitor)
			throws InvocationTargetException, OperationCanceledException {
		if (!isValidArchive(archive)) {
			throw new IllegalArgumentException("Input must be an archive"); //$NON-NLS-1$
		}
		this.directoryToImport = getExpandDirectory(archive);
		ExpandArchiveIntoFilesystemOperation expandOperation = new ExpandArchiveIntoFilesystemOperation(archive,
				directoryToImport);
		expandOperation.run(monitor);
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}

	static File getExpandDirectory(final File archive) {
		if (!isValidArchive(archive)) {
			throw new IllegalArgumentException("Input must be an archive"); //$NON-NLS-1$
		}
		return new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile(),
				archive.getName() + "_expanded"); //$NON-NLS-1$
	}

	private static boolean matchesPage(SmartImportJob job, SmartImportRootWizardPage page) {
		File jobRoot = job.getRoot().getAbsoluteFile();
		File pageRoot = page.getSelectedRoot().getAbsoluteFile();
		boolean sameSource = jobRoot.equals(pageRoot)
				|| (isValidArchive(pageRoot) && getExpandDirectory(pageRoot).getAbsoluteFile().equals(jobRoot));
		return sameSource && job.isDetectNestedProjects() == page.isDetectNestedProject()
				&& job.isConfigureProjects() == page.isConfigureProjects();
	}

}