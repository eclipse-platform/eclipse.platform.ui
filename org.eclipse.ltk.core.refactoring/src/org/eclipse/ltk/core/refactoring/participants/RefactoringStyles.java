/*******************************************************************************
 * Copyright (c) 2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ltk.core.refactoring.participants;


public final class RefactoringStyles {
	
	public static final int NONE= 0;
	public static final int NEEDS_PREVIEW= 1 << 0;
	public static final int FORCE_PREVIEW= 1 << 1;
	public static final int NEEDS_PROGRESS= 1 << 2;
	
	private RefactoringStyles() {
		// no instance
	}
}
