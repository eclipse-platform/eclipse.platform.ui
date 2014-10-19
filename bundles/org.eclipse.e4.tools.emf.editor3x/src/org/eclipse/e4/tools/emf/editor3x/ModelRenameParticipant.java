/*******************************************************************************
 * Copyright (c) 2011 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Wim Jongman <wim.jongman@remainsoftware.com> - Bug 395174: e4xmi should participate in package renaming
 * Bug 432892: Eclipse 4 Application does not work after renaming the project name
 ******************************************************************************/
package org.eclipse.e4.tools.emf.editor3x;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.e4.tools.emf.editor3x.extension.Util;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;

public class ModelRenameParticipant extends
org.eclipse.ltk.core.refactoring.participants.RenameParticipant {
	private IType fType;
	private IPackageFragment fPckage;
	private IFile fFile;
	private IFolder fFolder;
	private IProject fProject;
	private RefactorModel fModel;

	@Override
	protected boolean initialize(Object element) {

		if (element instanceof IType) {
			fType = (IType) element;
			return true;
		}

		if (element instanceof IPackageFragment) {
			fPckage = (IPackageFragment) element;
			return true;
		}

		if (element instanceof IFile) {
			fFile = (IFile) element;
			return true;
		}

		if (element instanceof IFolder) {
			fFolder = (IFolder) element;
			return true;
		}

		if (element instanceof IProject) {
			fProject = (IProject) element;
			return true;
		}

		return false;
	}

	@Override
	public String getName() {
		return "Workbench Model Contribution Participant"; //$NON-NLS-1$
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm,
		CheckConditionsContext context) throws OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
	OperationCanceledException {

		fModel = RefactorModel.getModel(this);

		if (fType != null) {
			return createClassChange(pm, fType);
		}

		if (fPckage != null) {
			return createPackageChange(pm, fPckage);
		}

		if (fFile != null) {
			return createFileChange(pm, fFile);
		}

		if (fFolder != null) {
			return createFolderChange(pm, fFolder);
		}

		if (fProject != null) {
			return createProjectChange(pm, fProject);
		}

		return null;
	}

	private Change createProjectChange(IProgressMonitor pm, IProject project)
		throws CoreException {

		if (!getArguments().getUpdateReferences()) {
			return null;
		}

		fModel.addTextRename("platform:/plugin/" + project.getName() + "/", //$NON-NLS-1$ //$NON-NLS-2$
			"platform:/plugin/" + getArguments().getNewName() + "/"); //$NON-NLS-1$ //$NON-NLS-2$
		fModel.addTextRename("bundleclass://" + project.getName() + "/", //$NON-NLS-1$ //$NON-NLS-2$
			"bundleclass://" + getArguments().getNewName() + "/"); //$NON-NLS-1$ //$NON-NLS-2$

		fModel.setProjectRename(project, ((IWorkspaceRoot) project.getParent())
			.getProject(getArguments().getNewName()));

		return RefactorParticipantDelegate.createChange(pm, fModel);

	}

	private Change createFolderChange(IProgressMonitor pm, IFolder folder)
		throws CoreException {

		String SPLIT = "/"; //$NON-NLS-1$
		if (folder.getParent().getFullPath().segmentCount() == 1) {
			SPLIT = ""; //$NON-NLS-1$
		}

		final String newUrl = "platform:/plugin/" + folder.getProject().getName() //$NON-NLS-1$
			+ "/" + folder.getParent().getProjectRelativePath().toString() //$NON-NLS-1$
			+ SPLIT + getArguments().getNewName();

		final String oldUrl = "platform:/plugin" + folder.getFullPath(); //$NON-NLS-1$

		fModel.addTextRename(oldUrl, newUrl);

		return RefactorParticipantDelegate.createChange(pm, fModel);
	}

	private Change createFileChange(IProgressMonitor pm, IFile file)
		throws CoreException {

		String SPLIT = "/"; //$NON-NLS-1$
		if (file.getParent().getFullPath().segmentCount() == 1) {
			SPLIT = ""; //$NON-NLS-1$
		}
		final String newUrl = "platform:/plugin/" + file.getProject().getName() + "/" //$NON-NLS-1$ //$NON-NLS-2$
			+ file.getParent().getProjectRelativePath().toString() + SPLIT
			+ getArguments().getNewName();
		final String oldUrl = "platform:/plugin" + file.getFullPath(); //$NON-NLS-1$
		fModel.addTextRename(oldUrl, newUrl);

		return RefactorParticipantDelegate.createChange(pm, fModel);
	}

	private Change createPackageChange(IProgressMonitor pm,
		IPackageFragment pckage) throws CoreException {
		final String bundle = Util.getBundleSymbolicName(pckage.getJavaProject()
			.getProject());

		final String newUrl = "bundleclass://" + bundle + "/" //$NON-NLS-1$ //$NON-NLS-2$
			+ getArguments().getNewName();

		final String oldUrl = "bundleclass://" + bundle + "/" //$NON-NLS-1$ //$NON-NLS-2$
			+ pckage.getElementName();
		fModel.addTextRename(oldUrl, newUrl);

		return RefactorParticipantDelegate.createChange(pm, fModel);
	}

	private Change createClassChange(IProgressMonitor pm, IType type)
		throws CoreException {
		final String bundle = Util.getBundleSymbolicName(type.getJavaProject()
			.getProject());

		final String newUrl = "bundleclass://" //$NON-NLS-1$
			+ bundle
			+ "/" //$NON-NLS-1$
			+ (type.getPackageFragment().getElementName().length() == 0 ? getArguments()
				.getNewName() : type.getPackageFragment()
				.getElementName() + "." + getArguments().getNewName()); //$NON-NLS-1$
		final String oldUrl = "bundleclass://" + bundle + "/" //$NON-NLS-1$ //$NON-NLS-2$
			+ type.getFullyQualifiedName().replace(".", "\\."); //$NON-NLS-1$//$NON-NLS-2$
		fModel.addTextRename(oldUrl, newUrl);

		return RefactorParticipantDelegate.createChange(pm, fModel);
	}

}
