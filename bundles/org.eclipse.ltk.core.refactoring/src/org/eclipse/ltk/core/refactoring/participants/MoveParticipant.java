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
 * A participant to participate in refactorings that move elements. A move
 * participant can't assume that its associated refactoring processor is a
 * move processor. A move operation might be a side effect of another
 * refactoring operation.
 * <p>
 * Move participants are registered via the extension point <code>
 * org.eclipse.ltk.core.refactoring.moveParticipants</code>.
 * Extensions to this extension point must therefore extend this abstract class.
 * </p>
 *
 * @since 3.0
 */
public abstract class MoveParticipant extends RefactoringParticipant {

	private MoveArguments fArguments;

	@Override
	protected final void initialize(RefactoringArguments arguments) {
		fArguments= (MoveArguments)arguments;
	}

	/**
	 * Returns the move arguments.
	 *
	 * @return the move arguments
	 */
	public MoveArguments getArguments() {
		return fArguments;
	}
}
