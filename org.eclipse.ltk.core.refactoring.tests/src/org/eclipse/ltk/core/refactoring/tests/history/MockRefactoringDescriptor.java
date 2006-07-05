/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests.history;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class MockRefactoringDescriptor extends RefactoringDescriptor {

	public static final String ID= "org.eclipse.ltk.core.mock";

	private final Map fArguments= new HashMap();

	public MockRefactoringDescriptor(String project, String description, String comment, int flags) {
		super(ID, project, description, comment, flags);
	}

	public MockRefactoringDescriptor(String project, String description, String comment, Map arguments, int flags) {
		this(project, description, comment, flags);
		fArguments.putAll(arguments);
	}

	/**
	 * {@inheritDoc}
	 */
	public Refactoring createRefactoring(RefactoringStatus status) throws CoreException {
		return new MockRefactoring();
	}

	public Map getArguments() {
		return fArguments;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {

		final StringBuffer buffer= new StringBuffer(128);

		buffer.append(getClass().getName());
		if (getID().equals(ID_UNKNOWN))
			buffer.append("[unknown refactoring]"); //$NON-NLS-1$
		else {
			buffer.append("[timeStamp="); //$NON-NLS-1$
			buffer.append(getTimeStamp());
			buffer.append(",id="); //$NON-NLS-1$
			buffer.append(getID());
			buffer.append(",description="); //$NON-NLS-1$
			buffer.append(getDescription());
			buffer.append(",project="); //$NON-NLS-1$
			buffer.append(getProject());
			buffer.append(",comment="); //$NON-NLS-1$
			buffer.append(getComment());
			buffer.append(",arguments="); //$NON-NLS-1$
			buffer.append(getArguments());
			buffer.append(",flags="); //$NON-NLS-1$
			buffer.append(getFlags());
			buffer.append("]"); //$NON-NLS-1$
		}

		return buffer.toString();
	}
}