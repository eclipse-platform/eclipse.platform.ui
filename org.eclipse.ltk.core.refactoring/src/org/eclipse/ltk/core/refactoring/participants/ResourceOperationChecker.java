/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.participants;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.resources.mapping.ResourceChangeValidator;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * A resource operation checker is a shared checker to collect all
 * changes done by the refactoring and the participants to resources
 * so that they can be validated as one change. A resource operation
 * checker supersedes the {@link ValidateEditChecker}. So if clients
 * add their content changes to this checker there is no need to add
 * them to the {@link ValidateEditChecker} as well.
 * <p> 
 * Note: this class is not intended to be extended by clients.
 * </p>
 *
 * @see ResourceChangeValidator
 * 
 * @since 3.2
 */
public class ResourceOperationChecker implements IConditionChecker {

	private IResourceChangeDescriptionFactory fDeltaFactory;
	
	public ResourceOperationChecker() {
		fDeltaFactory= ResourceChangeValidator.getValidator().createDeltaFactory();
	}
	
	/**
	 * Returns the delta factory to be used to record resource 
	 * operations.
	 * 
	 * @return the delta factory
	 */
	public IResourceChangeDescriptionFactory getDeltaFactory() {
		return fDeltaFactory;
	}
	
	public RefactoringStatus check(IProgressMonitor monitor) throws CoreException {
		IStatus status= ResourceChangeValidator.getValidator().validateChange(fDeltaFactory.getDelta(), monitor);
		return RefactoringStatus.create(status);
	}

	/* package */ IFile[] getChangedFiles() throws CoreException {
		IResourceDelta root= fDeltaFactory.getDelta();
		final List result= new ArrayList();
		root.accept(new IResourceDeltaVisitor() {
			public boolean visit(IResourceDelta delta) throws CoreException {
				if ((delta.getKind() & IResourceDelta.CHANGED) != 0 && delta.getResource().getType() == IResource.FILE) {
					result.add(delta.getResource());
				}
				return true;
			}
		});
		return (IFile[]) result.toArray(new IFile[result.size()]);
	}
}