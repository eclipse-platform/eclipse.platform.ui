/*******************************************************************************
 * Copyright (c) 2013 Remain Software, Industrial-TSI and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wim Jongman <wim.jongman@remainsoftware.com> - Bug 395174: e4xmi should participate in package renaming 
 *                                                    Bug 432892: Eclipse 4 Application does not work after renaming the project name
 ******************************************************************************/
package org.eclipse.e4.tools.emf.editor3x;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.e4.tools.emf.editor3x.extension.Util;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;

public class ModelMoveParticipant extends MoveParticipant {
	private IType fType;
	private IPackageFragment fPckage;
	private IFile fFile;
	private RefactorModel fModel;

	@Override
	protected boolean initialize(Object pElement) {

		fModel = RefactorModel.getModel(this);

		if (pElement instanceof IType) {
			fType = (IType) pElement;
			return true;
		}

		if (pElement instanceof IPackageFragment) {
			fPckage = (IPackageFragment) pElement;
			return true;
		}

		if (pElement instanceof IFile) {
			fFile = (IFile) pElement;
			return true;
		}

		return false;
	}

	@Override
	public String getName() {
		return "Workbench Model Contribution Participant";
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pMonitor,
			CheckConditionsContext pContext) throws OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pMonitor) throws CoreException,
			OperationCanceledException {

		pMonitor.beginTask("Creating Change ..", IProgressMonitor.UNKNOWN);

		Change change = null;

		if (fType != null) {
			change = createClassChange(pMonitor, fType);
		}

		else if (fPckage != null) {
			change = createPackageChange(pMonitor, fPckage);
		}

		else if (fFile != null) {
			change = createFileChange(pMonitor, fFile);
		}

		pMonitor.done();

		return change;
	}

	private Change createFileChange(IProgressMonitor pMonitor, IFile file)
			throws CoreException {

		String newUrl = "platform:/plugin/";
		if (getArguments().getDestination() instanceof IFolder) {
			IFolder folder = (IFolder) getArguments().getDestination();
			newUrl += folder.getProject().getName() + "/"
					+ folder.getProjectRelativePath().toString() + "/"
					+ file.getName();
		} else {
			IProject project = (IProject) getArguments().getDestination();
			newUrl += project.getName() + "/" + file.getName();

		}

		String oldUrl = "platform:/plugin" + file.getFullPath();

		fModel.addTextRename(oldUrl, newUrl);

		return RefactorParticipantDelegate.createChange(pMonitor, fModel);
	}

	private Change createPackageChange(IProgressMonitor pMonitor,
			IPackageFragment pPckage) throws CoreException,
			OperationCanceledException {
		String fromBundle = Util.getBundleSymbolicName(pPckage.getJavaProject()
				.getProject());

		IPackageFragmentRoot fragmentRoot = (IPackageFragmentRoot) getArguments()
				.getDestination();
		String toBundle = Util.getBundleSymbolicName(fragmentRoot
				.getJavaProject().getProject());

		final String newUrl = "bundleclass://" + toBundle + "/"
				+ pPckage.getElementName();

		String oldUrl = "bundleclass://" + fromBundle + "/"
				+ pPckage.getElementName();

		fModel.addTextRename(oldUrl, newUrl);

		return RefactorParticipantDelegate.createChange(pMonitor, fModel);
	}

	private Change createClassChange(IProgressMonitor pMonitor, IType pType)
			throws CoreException, OperationCanceledException {
		String fromBundle = Util.getBundleSymbolicName(fType.getJavaProject()
				.getProject());
		String fromClassname = pType.getFullyQualifiedName();

		IPackageFragment fragment = (IPackageFragment) getArguments()
				.getDestination();
		String toBundle = Util.getBundleSymbolicName(fragment.getJavaProject()
				.getProject());
		String toClassName = fragment.getElementName().length() == 0 ? pType
				.getElementName() : fragment.getElementName() + "."
				+ pType.getElementName();

		return RefactorParticipantDelegate.createChange(
				pMonitor,
				fModel.addTextRename("bundleclass://" + fromBundle + "/"
						+ fromClassname, "bundleclass://" + toBundle + "/"
						+ toClassName));
	}

}
