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
public class FindUnusedMembers implements IRunnableWithProgress {
	private boolean headerWritten = false;
	private final Writer output;

	ICompilationUnit unit;
	protected int unusedMemberCount = 0;

	public FindUnusedMembers(ICompilationUnit unit, Writer output) {
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

		// Search for references
		IMethod[] methods = type.getMethods();
		IField[] fields= type.getFields();
		monitor.beginTask("Searching for references.", methods.length + fields.length); //$NON-NLS-1$
		try {
			for (int i = 0; i < methods.length; i++) {
				if (monitor.isCanceled())
					throw new OperationCanceledException();
				if (hasReferences(methods[i], monitor))
					continue;
				writeResult(methods[i]);
				unusedMemberCount++;
			}
			for (int i = 0; i < fields.length; i++) {
				if (monitor.isCanceled())
					throw new OperationCanceledException();
				if (hasReferences(fields[i], monitor))
					continue;
				writeResult(fields[i]);
				unusedMemberCount++;
			}

			if (monitor.isCanceled())
				throw new OperationCanceledException();
		} finally {
			monitor.done();
		}
	}

	private boolean hasReferences(IMember member, IProgressMonitor monitor) throws JavaModelException {
		ICompilationUnit[] affectedUnits = RefactoringSearchEngine.findAffectedCompilationUnits(SearchPattern.createPattern(member, IJavaSearchConstants.REFERENCES), RefactoringScopeFactory.create(member.getDeclaringType()), new SubProgressMonitor(monitor, 1), new RefactoringStatus());
		return affectedUnits.length > 0;
	}

	public int getUnusedMethodCount() {
		return unusedMemberCount;
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
	
	private void writeHeader(IType type) throws IOException {
		if (!headerWritten) {
			headerWritten = true;
			output.write("\n\n" + type.getFullyQualifiedName()); //$NON-NLS-1$
		}
	}

	/**
	 * @param method
	 * @throws IOException
	 * @throws JavaModelException 
	 * @throws IllegalArgumentException 
	 */
	private void writeResult(IMethod method) throws IOException, IllegalArgumentException, JavaModelException {
		writeHeader(method.getDeclaringType());
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
	private void writeResult(IField field) throws IOException, IllegalArgumentException, JavaModelException {
		writeHeader(field.getDeclaringType());
		output.write("\n\t"); //$NON-NLS-1$
		output.write(Signature.toString(field.getTypeSignature()));
		output.write(" "); //$NON-NLS-1$
		output.write(field.getElementName());
	}
}
