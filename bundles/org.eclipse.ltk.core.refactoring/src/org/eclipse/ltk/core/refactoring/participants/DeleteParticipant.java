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
 * A participant to participate in refactorings that delete elements. A delete
 * participant can't assume that its associated refactoring processor is a
 * delete processor. A delete operation might be a side effect of another
 * refactoring operation.
 * <p>
 * Delete participants are registered via the extension point <code>
 * org.eclipse.ltk.core.refactoring.deleteParticipants</code>.
 * Extensions to this extension point must therefore extend this abstract class.
 * </p>
 *
 * @since 3.0
 */
public abstract class DeleteParticipant extends RefactoringParticipant {

	private DeleteArguments fArguments;

	@Override
	protected final void initialize(RefactoringArguments arguments) {
		fArguments= (DeleteArguments)arguments;
	}

	/**
	 * Returns the delete arguments.
	 *
	 * @return the delete arguments
	 */
	public DeleteArguments getArguments() {
		return fArguments;
	}
}
