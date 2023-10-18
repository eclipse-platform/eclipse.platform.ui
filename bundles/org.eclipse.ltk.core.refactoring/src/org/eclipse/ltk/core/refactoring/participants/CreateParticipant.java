/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.core.refactoring.participants;

/**
 * A participant to participate in refactorings that create elements. A create
 * participant can't assume that its associated processor is of a specific type.
 * A create could be triggered as a side effect of another refactoring.
 * <p>
 * Create participants are registered via the extension point <code>
 * org.eclipse.ltk.core.refactoring.createParticipants</code>. Extensions to
 * this extension point must therefore extend this abstract class.
 * </p>
 *
 * @since 3.0
 */
public abstract class CreateParticipant extends RefactoringParticipant {

	private CreateArguments fArguments;

	@Override
	protected final void initialize(RefactoringArguments arguments) {
		fArguments= (CreateArguments)arguments;
	}

	/**
	 * Returns the create arguments.
	 *
	 * @return the create arguments
	 */
	public CreateArguments getArguments() {
		return fArguments;
	}
}
