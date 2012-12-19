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

		pm.beginTask("Creating Change ..", IProgressMonitor.UNKNOWN);

		Change change = null;

		if (_type != null) {
			change = createClassChange(pm, _type);
		}

		else if (_pckage != null) {
			change = createPackageChange(pm, _pckage);
		}

		else if (_file != null) {
			change = createFileChange(pm, _file);
		}

		//
		// if (_folder != null) {
		// return createFolderChange(pm, _folder);
		// }

		pm.done();

		return change;
	}

	private Change createFileChange(IProgressMonitor pm, IFile file)
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

		return RefactorParticipantDelegate.createChange(pm, this, oldUrl,
				newUrl);
	}

	private Change createPackageChange(IProgressMonitor pm,
			IPackageFragment pckage) throws CoreException,
			OperationCanceledException {
		String fromBundle = Util.getBundleSymbolicName(pckage.getJavaProject()
				.getProject());

		IPackageFragmentRoot fragmentRoot = (IPackageFragmentRoot) getArguments()
				.getDestination();
		String toBundle = Util.getBundleSymbolicName(fragmentRoot
				.getJavaProject().getProject());

		final String newUrl = "bundleclass://" + toBundle + "/"
				+ pckage.getElementName();

		String oldUrl = "bundleclass://" + fromBundle + "/"
				+ pckage.getElementName();

		return RefactorParticipantDelegate.createChange(pm, this, oldUrl,
				newUrl);
	}

	private Change createClassChange(IProgressMonitor pm, IType type)
			throws CoreException, OperationCanceledException {
		String fromBundle = Util.getBundleSymbolicName(_type.getJavaProject()
				.getProject());
		String fromClassname = type.getFullyQualifiedName();

		IPackageFragment fragment = (IPackageFragment) getArguments()
				.getDestination();
		String toBundle = Util.getBundleSymbolicName(fragment.getJavaProject()
				.getProject());
		String toClassName = fragment.getElementName().length() == 0 ? type
				.getElementName() : fragment.getElementName() + "."
				+ type.getElementName();

		return RefactorParticipantDelegate.createChange(pm, this,
				"bundleclass://" + fromBundle + "/" + fromClassname,
				"bundleclass://" + toBundle + "/" + toClassName);
	}

}
