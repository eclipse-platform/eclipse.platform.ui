/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.internal.core.refactoring.history;

import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;

/**
 * Wrapper change for refactorings which returns an unknown refactoring
 * descriptor in case the refactoring does not provide one.
 *
 * @since 3.2
 */
public final class RefactoringDescriptorChange extends CompositeChange {

	/**
	 * Creates a new refactoring descriptor change.
	 *
	 * @param name
	 *            the name of the change
	 */
	public RefactoringDescriptorChange(final String name) {
		super(name);
	}

	@Override
	public ChangeDescriptor getDescriptor() {
		final ChangeDescriptor descriptor= super.getDescriptor();
		if (descriptor == null) {
			return new RefactoringChangeDescriptor(new UnknownRefactoringDescriptor(getChildren()[0].getName()));
		}
		return descriptor;
	}
}
