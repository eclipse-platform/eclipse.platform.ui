/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.participants;

/**
 * Delete arguments describes the data that a processor provides
 * to its delete participants.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 *
 * @since 3.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DeleteArguments extends RefactoringArguments {

	/**
	 * Creates a new delete arguments object.
	 */
	public DeleteArguments() {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.2
	 */
    public String toString() {
    	return "delete"; //$NON-NLS-1$
    }
}
