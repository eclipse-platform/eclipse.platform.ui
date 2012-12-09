/*******************************************************************************
 * Copyright (c) 2011 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.editor3x;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.e4.tools.emf.editor3x.extension.Util;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;

public class ClassRenameParticipant extends
		org.eclipse.ltk.core.refactoring.participants.RenameParticipant {
	private IType _type;
	private IPackageFragment _pckage;
	private IFile _file;
	private IFolder _folder;

	@Override
	protected boolean initialize(Object element) {

		if (element instanceof IType) {
			_type = (IType) element;
			return true;
		}

		if (element instanceof IPackageFragment) {
			_pckage = (IPackageFragment) element;
			return true;
		}

		if (element instanceof IFile) {
			_file = (IFile) element;
			return true;
		}

		if (element instanceof IFolder) {
			_folder = (IFolder) element;
			return true;
		}
		return false;
	}

	@Override
	public String getName() {
		return "Workbench Model Contribution Participant";
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		if (_type != null) {
			return createClassChange(pm, _type);
		}

		if (_pckage != null) {
			return createPackageChange(pm, _pckage);
		}

		if (_file != null) {
			return createFileChange(pm, _file);
		}

		if (_folder != null) {
			return createFolderChange(pm, _folder);
		}

		return null;
	}

	private Change createFolderChange(IProgressMonitor pm, IFolder folder)
			throws CoreException {

		String SPLIT = "/";
		if (folder.getParent().getFullPath().segmentCount() == 1) {
			SPLIT = "";
		}

		String newUrl = "platform:/plugin/" + folder.getProject().getName() + "/"
				+ folder.getParent().getProjectRelativePath().toString()
				+ SPLIT + getArguments().getNewName();

		String oldUrl = "platform:/plugin" + folder.getFullPath();

		return RefactorParticipantDelegate.createChange(pm, this, oldUrl,
				newUrl);
	}

	private Change createFileChange(IProgressMonitor pm, IFile file)
			throws CoreException {

		String SPLIT = "/";
		if (file.getParent().getFullPath().segmentCount() == 1) {
			SPLIT = "";
		}
		String newUrl = "platform:/plugin/" + file.getProject().getName() + "/"
				+ file.getParent().getProjectRelativePath().toString() + SPLIT
				+ getArguments().getNewName();

		String oldUrl = "platform:/plugin" + file.getFullPath();

		return RefactorParticipantDelegate.createChange(pm, this, oldUrl,
				newUrl);
	}

	private Change createPackageChange(IProgressMonitor pm,
			IPackageFragment pckage) throws CoreException {
		String bundle = Util.getBundleSymbolicName(pckage.getJavaProject()
				.getProject());

		final String newUrl = "bundleclass://" + bundle + "/"
				+ getArguments().getNewName();

		String oldUrl = "bundleclass://" + bundle + "/"
				+ pckage.getElementName();

		return RefactorParticipantDelegate.createChange(pm, this, oldUrl,
				newUrl);
	}

	private Change createClassChange(IProgressMonitor pm, IType type)
			throws CoreException {
		String bundle = Util.getBundleSymbolicName(type.getJavaProject()
				.getProject());

		final String newUrl = "bundleclass://"
				+ bundle
				+ "/"
				+ (type.getPackageFragment().getElementName().length() == 0 ? getArguments()
						.getNewName() : type.getPackageFragment()
						.getElementName() + "." + getArguments().getNewName());
		String oldUrl = "bundleclass://" + bundle + "/"
				+ type.getFullyQualifiedName().replace(".", "\\.");

		return RefactorParticipantDelegate.createChange(pm, this, oldUrl,
				newUrl);
	}

}
