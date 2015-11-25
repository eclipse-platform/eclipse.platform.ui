/*******************************************************************************
 * Copyright (c) 2014-2016 Red Hat Inc., and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 *     Snjezana Peco (Red Hat Inc.)
 ******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.wizards.datatransfer.ProjectConfigurator;

/**
 * This {@link SmartImportWizard} allows user to control an import operation. It
 * takes as input a directory and assist user in proposing what and how to
 * import, relying and various strategies contributed as extension of
 * {@link ProjectConfigurator}
 *
 * @since 3.12
 *
 */
public class SmartImportWizard extends Wizard implements IImportWizard {

	/**
	 * Expands an archive onto provided filesystem directory
	 * @since 3.12
	 *
	 */
	private static final class ExpandArchiveIntoFilesystemOperation implements IRunnableWithProgress {
		private File archive;
		private File destination;

		/**
		 * @param archive
		 * @param destination
		 */
		private ExpandArchiveIntoFilesystemOperation(File archive, File destination) {
			this.archive = archive;
			this.destination = destination;
		}

		@Override
		public void run(IProgressMonitor monitor) throws InvocationTargetException, OperationCanceledException {
			monitor.beginTask(NLS.bind(DataTransferMessages.SmartImportWizardPage_expandingArchive, archive.getName(), destination.getName()),
					1);
			TarFile tarFile = null;
			ZipFile zipFile = null;
			InputStream content = null;
			FileOutputStream fileStream = null;
			ILeveledImportStructureProvider importStructureProvider = null;
			try {
				if (ArchiveFileManipulations.isTarFile(archive.getAbsolutePath())) {
					tarFile = new TarFile(archive);
					importStructureProvider = new TarLeveledStructureProvider(tarFile);
				} else if (ArchiveFileManipulations.isZipFile(archive.getAbsolutePath())) {
					zipFile = new ZipFile(archive);
					importStructureProvider = new ZipLeveledStructureProvider(zipFile);
				}
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
						toCreate.createNewFile();
						fileStream = null;
						content = null;
						fileStream = new FileOutputStream(toCreate);
						content = importStructureProvider.getContents(current);
						byte[] buffer = new byte[1024];
						int nbBytes = 0;
						while ((nbBytes = content.read(buffer, 0, 1024))  > 0) {
							fileStream.write(buffer, 0, nbBytes);
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
			} finally {
				if (importStructureProvider != null) {
					importStructureProvider.closeArchive();
				}
				if (tarFile != null)
					try {
						tarFile.close();
					} catch (IOException ex) {
					}
				if (zipFile != null)
					try {
						zipFile.close();
					} catch (IOException ex) {
					}
				if (fileStream != null)
					try {
						fileStream.close();
					} catch (IOException ex) {
					}
				if (content != null)
					try {
						content.close();
					} catch (IOException ex) {
					}
			}
		}
	}

	private File initialSelection;
	private Set<IWorkingSet> initialWorkingSets = new HashSet<>();
	private SmartImportRootWizardPage projectRootPage;
	private SmartImportJob easymportJob;
	/**
	 * the selected directory or the directory when archive got expanded
	 */
	private File directoryToImport;

	/**
	 *
	 */
	public SmartImportWizard() {
		super();
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
		IDialogSettings dialogSettings = getDialogSettings();
		if (dialogSettings == null) {
			dialogSettings = IDEWorkbenchPlugin.getDefault().getDialogSettings();
			setDialogSettings(dialogSettings);
		}
		setWindowTitle(DataTransferMessages.SmartImportWizardPage_importProjectsInFolderTitle);
		setDefaultPageImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/newprj_wiz.png")); //$NON-NLS-1$
	}

	/**
	 * Sets the initial directory or archive to import in workspace.
	 *
	 * @param directoryOrArchive
	 */
	public void setInitialImportSource(File directoryOrArchive) {
		this.initialSelection = directoryOrArchive;
	}

	/**
	 * Sets the initial selected working sets for the wizard
	 *
	 * @param workingSets
	 */
	public void setInitialWorkingSets(Set<IWorkingSet> workingSets) {
		this.initialWorkingSets = workingSets;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		if (selection != null) {
			for (Object item : selection.toList()) {
				File asFile = toFile(item);
				if (asFile != null && this.initialSelection == null) {
					this.initialSelection = asFile;
				} else {
					IWorkingSet asWorkingSet = toWorkingSet(item);
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
			return ((IResource)o).getLocation().toFile();
		} else if (o instanceof IAdaptable) {
			IResource resource = ((IAdaptable)o).getAdapter(IResource.class);
			if (resource != null) {
				return resource.getLocation().toFile();
			}
		}
		return null;
	}

	private IWorkingSet toWorkingSet(Object o) {
		if (o instanceof IWorkingSet) {
			return (IWorkingSet)o;
		} else if (o instanceof IAdaptable) {
			return ((IAdaptable)o).getAdapter(IWorkingSet.class);
		}
		return null;
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
		SmartImportJob job = getImportJob();
		if (projectRootPage.isDetectNestedProject() || projectRootPage.isConfigureProjects()) {
			SmartImportJobReportDialog dialog = new SmartImportJobReportDialog(null);
			dialog.setBlockOnOpen(false);
			getContainer().getShell().setEnabled(false);
			dialog.show(job, getShell());
		}
		job.schedule();
		return true;
	}

	/**
	 * Get the import job that will be processed by this wizard. Can be null (if
	 * provided directory is invalid).
	 *
	 * @return the import job
	 */
	public SmartImportJob getImportJob() {
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
 		}
		if (this.easymportJob == null || !matchesPage(this.easymportJob, this.projectRootPage)) {
			this.easymportJob = new SmartImportJob(this.directoryToImport, projectRootPage.getSelectedWorkingSets(),
					projectRootPage.isConfigureProjects(), projectRootPage.isDetectNestedProject());
		}
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

	/**
	 * @param easymportJob2
	 * @param projectRootPage2
	 * @return
	 */
	private static boolean matchesPage(SmartImportJob easymportJob2, SmartImportRootWizardPage projectRootPage2) {
		return easymportJob2.getRoot().getAbsoluteFile().equals(projectRootPage2.getSelectedRoot().getAbsoluteFile())
				&& easymportJob2.isDetectNestedProjects() == projectRootPage2.isDetectNestedProject()
				&& easymportJob2.isConfigureProjects() == projectRootPage2.isConfigureProjects();
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == this.projectRootPage && !this.projectRootPage.isDetectNestedProject()) {
			return null;
		}
		return super.getNextPage(page);
	}

}