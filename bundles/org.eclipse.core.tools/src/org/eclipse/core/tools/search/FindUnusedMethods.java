/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tools.search;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringScopeFactory;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringSearchEngine;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/*
 * Class that removes field declarations which aren't referenced.
 */
public class FindUnusedMethods implements IRunnableWithProgress {
	private boolean headerWritten = false;
	private final Writer output;

	ICompilationUnit unit;
	protected int unusedMethodCount = 0;

	public FindUnusedMethods(ICompilationUnit unit, Writer output) {
		super();
		this.unit = unit;
		this.output = output;
	}

	private void doSearchCU(ICompilationUnit cu, IProgressMonitor monitor) throws JavaModelException, IOException {
		IType[] allTypes = cu.getAllTypes();
		for (int i = 0; i < allTypes.length; i++) {
			doSearchType(allTypes[i], monitor);
		}
	}

	public void doSearchType(IType type, IProgressMonitor monitor) throws JavaModelException, IOException {
		RefactoringStatus result = new RefactoringStatus();

		// Search for references
		IMethod[] methods = type.getMethods();
		monitor.beginTask("Searching for references.", methods.length); //$NON-NLS-1$
		try {
			for (int i = 0; i < methods.length; i++) {
				if (monitor.isCanceled())
					throw new OperationCanceledException();
				IMethod method = methods[i];
				String name = method.getElementName();
				monitor.subTask("Searching for references to: " + name); //$NON-NLS-1$
				// search for references
				ICompilationUnit[] affectedUnits = RefactoringSearchEngine.findAffectedCompilationUnits(SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES), RefactoringScopeFactory.create(type), new SubProgressMonitor(monitor, 1), result);
				// there are references so go to the next field
				if (affectedUnits.length > 0)
					continue;
				if (!headerWritten) {
					headerWritten = true;
					output.write("\n\n" + type.getFullyQualifiedName()); //$NON-NLS-1$
				}
				writeResult(method);
				unusedMethodCount++;
			}

			if (monitor.isCanceled())
				throw new OperationCanceledException();
		} finally {
			monitor.done();
		}
	}

	public int getUnusedMethodCount() {
		return unusedMethodCount;
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		try {
			doSearchCU(unit, monitor);
		} catch (OperationCanceledException e) {
			throw new InterruptedException();
		} catch (JavaModelException e) {
			throw new InvocationTargetException(e);
		} catch (IOException e) {
			throw new InvocationTargetException(e);
		}
	}

	/**
	 * @param method
	 * @throws IOException
	 * @throws JavaModelException 
	 * @throws IllegalArgumentException 
	 */
	private void writeResult(IMethod method) throws IOException, IllegalArgumentException, JavaModelException {
		output.write("\n\t");//$NON-NLS-1$
		output.write(Signature.toString(method.getReturnType()));
		output.write(" "); //$NON-NLS-1$
		output.write(method.getElementName());
		output.write("("); //$NON-NLS-1$
		String[] types = method.getParameterTypes();
		for (int i = 0; i < types.length; i++) {
			output.write(Signature.toString(types[i]));
			if (i < types.length - 1)
				output.write(","); //$NON-NLS-1$
		}
		output.write(")"); //$NON-NLS-1$
	}
}
