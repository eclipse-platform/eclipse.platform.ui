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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.e4.tools.emf.editor3x.extension.Util;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;

public class ClassRenameParticipant extends
		org.eclipse.ltk.core.refactoring.participants.RenameParticipant {
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
		String bundle = Util.getBundleSymbolicName(type.getJavaProject().getProject());
		
		final String newUrl = "bundleclass://" + bundle + "/" + (type.getPackageFragment().getElementName().length() == 0 ? getArguments().getNewName() : type.getPackageFragment().getElementName() + "." + getArguments().getNewName());
		String oldUrl = "bundleclass://" + bundle + "/" + type.getFullyQualifiedName().replace(".", "\\.");
		return RefactorParticipantDelegate.createChange(pm, this, oldUrl, newUrl);
	}

}
