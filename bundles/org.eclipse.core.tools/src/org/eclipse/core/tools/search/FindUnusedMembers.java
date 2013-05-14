/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringScopeFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;

/*
 * Class that removes field declarations which aren't referenced.
 */
public class FindUnusedMembers implements IRunnableWithProgress {

	public static interface IResultReporter {
		void unusedElementFound(IMember member) throws CoreException;
	}

	public static class OutputWriter implements IResultReporter {

		private final Writer output;
		private IType lastType;

		public OutputWriter(Writer writer) {
			output = writer;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.tools.search.FindUnusedMembers.IResultReporter#unusedElementFound(org.eclipse.jdt.core.IMethod)
		 */
		public void unusedElementFound(IMember member) throws CoreException {
			try {
				if (!member.getDeclaringType().equals(lastType)) {
					lastType = member.getDeclaringType();
					writeHeader(lastType);
				}
				if (member instanceof IMethod) {
					writeResult((IMethod) member);
				} else if (member instanceof IField) {
					writeResult((IField) member);
				}
			} catch (IOException e) {
				// to do
			}
		}

		private void writeHeader(IType type) throws IOException {
			output.write("\n\n" + type.getFullyQualifiedName()); //$NON-NLS-1$
		}

		private void writeResult(IField field) throws IOException, JavaModelException {
			output.write("\n\t"); //$NON-NLS-1$
			output.write(Signature.toString(field.getTypeSignature()));
			output.write(" "); //$NON-NLS-1$
			output.write(field.getElementName());
		}

		private void writeResult(IMethod method) throws IOException, JavaModelException {
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

	private final IResultReporter result;
	private ICompilationUnit[] units;
	protected int unusedMemberCount = 0;

	public FindUnusedMembers(ICompilationUnit[] units, Writer writer) {
		this(units, new OutputWriter(writer));
	}

	public FindUnusedMembers(ICompilationUnit[] units, IResultReporter resultReporter) {
		this.units = units;
		this.result = resultReporter;
	}

	private void doSearchCU(ICompilationUnit cu, IProgressMonitor monitor) throws CoreException {
		IType[] allTypes = cu.getAllTypes();
		monitor.beginTask("Processing " + cu.getElementName(), allTypes.length + 1); //$NON-NLS-1$
		try {

			ASTParser astParser = ASTParser.newParser(AST.JLS3);
			astParser.setResolveBindings(true);
			astParser.setProject(cu.getJavaProject());

			IBinding[] bindings = astParser.createBindings(allTypes, new NullProgressMonitor());
			for (int i = 0; i < bindings.length; i++) {
				if (monitor.isCanceled())
					throw new OperationCanceledException();
				ITypeBinding typeBinding = (ITypeBinding) bindings[i];
				monitor.subTask("Processing '" + typeBinding.getQualifiedName() + "'"); //$NON-NLS-1$//$NON-NLS-2$
				doSearchType(typeBinding, new NullProgressMonitor());
			}
		} finally {
			monitor.done();
		}
	}

	private boolean methodOverrides(IMethodBinding binding) {
		return Bindings.findOverriddenMethod(binding, true) != null;
	}

	public void doSearchType(ITypeBinding typeBinding, IProgressMonitor monitor) throws CoreException {
		IMethodBinding[] methods = typeBinding.getDeclaredMethods();
		IVariableBinding[] fields = typeBinding.getDeclaredFields();

		monitor.beginTask("Searching for references.", methods.length + fields.length); //$NON-NLS-1$

		try {
			for (int i = 0; i < methods.length; i++) {
				if (monitor.isCanceled())
					throw new OperationCanceledException();

				IMethodBinding methodBinding = methods[i];
				if (methodOverrides(methodBinding))
					continue;

				IMethod method = (IMethod) methodBinding.getJavaElement();
				if (method == null)
					continue;

				if (hasReferences(method, new SubProgressMonitor(monitor, 1)))
					continue;
				result.unusedElementFound(method);
				unusedMemberCount++;
			}
			for (int i = 0; i < fields.length; i++) {
				if (monitor.isCanceled())
					throw new OperationCanceledException();

				IVariableBinding fieldBinding = fields[i];
				IField field = (IField) fieldBinding.getJavaElement();
				if (field == null)
					continue;
				if (hasReferences(field, new SubProgressMonitor(monitor, 1)))
					continue;
				result.unusedElementFound(field);
				unusedMemberCount++;
			}

			if (monitor.isCanceled())
				throw new OperationCanceledException();
		} finally {
			monitor.done();
		}
	}

	public int getUnusedMethodCount() {
		return unusedMemberCount;
	}

	private boolean hasReferences(IMember member, IProgressMonitor monitor) throws JavaModelException {

		final class ReferenceFound extends Error {
			private static final long serialVersionUID = 1L;
		}

		try {
			IJavaSearchScope searchScope = RefactoringScopeFactory.create(member.getDeclaringType());
			SearchPattern pattern = SearchPattern.createPattern(member, IJavaSearchConstants.REFERENCES);
			SearchRequestor requestor = new SearchRequestor() {
				public void acceptSearchMatch(SearchMatch match) throws CoreException {
					if (match.getAccuracy() == SearchMatch.A_ACCURATE) {
						throw new ReferenceFound();
					}
				}
			};
			new SearchEngine().search(pattern, new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()}, searchScope, requestor, monitor);
		} catch (CoreException e) {
			throw new JavaModelException(e);
		} catch (ReferenceFound e) {
			return true;
		}
		return false;
	}

	public void process(IProgressMonitor monitor) throws CoreException {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		try {
			monitor.beginTask("Searching unused members", this.units.length); //$NON-NLS-1$
			for (int i = 0; i < this.units.length; i++) {
				doSearchCU(units[i], new SubProgressMonitor(monitor, 1));
			}
		} finally {
			monitor.done();
		}
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			process(monitor);
		} catch (OperationCanceledException e) {
			throw new InterruptedException();
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
	}

}
