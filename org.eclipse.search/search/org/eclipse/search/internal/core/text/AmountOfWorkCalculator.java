package org.eclipse.search.internal.core.text;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.search.internal.core.ISearchScope;

/**
 * The visitor that does the actual work.
 */
public class AmountOfWorkCalculator extends TypedResourceVisitor {
	private ISearchScope fScope;
	private int fResult;
		
	protected boolean visitFile(IFile file) throws CoreException {
		if (fScope.encloses(file))
			fResult++;
		return true;	
	}
	
	public int process(Collection projects, ISearchScope scope) throws CoreException {
		fResult= 0;
		fScope= scope;
		
		Iterator i= projects.iterator();
		while(i.hasNext()) {
			IProject project= (IProject)i.next();
			int save= fResult;
			project.accept(this);
			// Project doesn't contain any files that are in scope
			if (save == fResult)
				i.remove();	
		}
		
		return fResult;
	}
}
