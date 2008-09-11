/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Oakland Software (Francis Upton) <francisu@ieee.org> -
 *          Fix for Bug 63149 [ltk] allow changes to be executed after the 'main' change during an undo [refactoring]
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests.participants;

import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

public class ElementRenameRefactoring extends RenameRefactoring {

	// Use a working participant
	public static final int WORKING= 0x01;

	// Cause the main refactoring to fail
	public static final int FAIL_TO_EXECUTE= 0x02;

	// Use the working pre-change participant
	public static final int PRE_CHANGE= 0x04;

	// Use the participants that are never disabled
	public static final int ALWAYS_ENABLED= 0x08;

	public ElementRenameRefactoring(int options) {
		super(new ElementRenameProcessor(options));
	}
}
