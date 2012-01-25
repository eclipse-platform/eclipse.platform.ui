package org.eclipse.e4.tools.emf.editor3x;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.e4.tools.emf.editor3x.extension.Util;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;

public class ClassMoveParticipant extends
		org.eclipse.ltk.core.refactoring.participants.MoveParticipant {
	private IType type;
	
	@Override
	protected boolean initialize(Object element) {
		if( element instanceof IType ) {
			type = (IType) element;
		} else {
			type = null;
		}
		
		return type != null;
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
		String fromBundle = Util.getBundleSymbolicName(type.getJavaProject().getProject());
		String fromClassname = type.getFullyQualifiedName();
		
		IPackageFragment fragment = (IPackageFragment) getArguments().getDestination();
		String toBundle = Util.getBundleSymbolicName(fragment.getJavaProject().getProject());
		String toClassName = fragment.getElementName().length() == 0 ? type.getElementName() : fragment.getElementName() + "." + type.getElementName();
		
		return RefactorParticipantDelegate.createChange(pm, this, "bundleclass://"+fromBundle+"/"+fromClassname, "bundleclass://"+toBundle+"/"+toClassName);
	}

}
