/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests.history;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class MockRefactoringDescriptor extends RefactoringDescriptor {

	public static final String ID= "org.eclipse.ltk.core.mock";

	private final Map<String, String> fArguments= new HashMap<>();

	public MockRefactoringDescriptor(String project, String description, String comment, int flags) {
		super(ID, project, description, comment, flags);
	}

	public MockRefactoringDescriptor(String project, String description, String comment, Map<String, String> arguments, int flags) {
		this(project, description, comment, flags);
		fArguments.putAll(arguments);
	}

	@Override
	public Refactoring createRefactoring(RefactoringStatus status) throws CoreException {
		return new MockRefactoring();
	}

	public Map<String, String> getArguments() {
		return fArguments;
	}

	@Override
	public String toString() {

		final StringBuilder buffer= new StringBuilder(128);

		buffer.append(getClass().getName());
		if (ID_UNKNOWN.equals(getID()))
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
			buffer.append(new TreeMap<>(getArguments()));
			buffer.append(",flags="); //$NON-NLS-1$
			buffer.append(getFlags());
			buffer.append("]"); //$NON-NLS-1$
		}

		return buffer.toString();
	}
}