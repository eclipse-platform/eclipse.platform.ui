/*
 * Copyright (c) 2000, 2003 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package org.eclipse.compare.internal;

public class CompareWithEditionAction extends EditionAction {
	
	public CompareWithEditionAction() {
		super(false, "org.eclipse.compare.internal.CompareWithEditionAction"); //$NON-NLS-1$
		this.fHelpContextId= ICompareContextIds.COMPARE_WITH_EDITION_DIALOG;
	}
}

