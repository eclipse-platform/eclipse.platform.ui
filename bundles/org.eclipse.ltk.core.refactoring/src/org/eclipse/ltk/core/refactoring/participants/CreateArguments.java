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
 * Create arguments describe the data that a processor
 * provides to its create participants.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 *
 * @since 3.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CreateArguments extends RefactoringArguments {

	/**
	 * Creates a new create arguments object.
	 */
	public CreateArguments() {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.2
	 */
    @Override
	public String toString() {
    	return "create"; //$NON-NLS-1$
    }
}
