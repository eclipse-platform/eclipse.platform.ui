/*
 * Copyright (c) 2000, 2003 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package org.eclipse.compare.internal;

import org.eclipse.compare.CompareUI;

/**
 * Help context ids for the Compare UI.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 * 
 */
public interface ICompareContextIds {
	
	public static final String PREFIX= CompareUI.PLUGIN_ID + '.';
	
	public static final String COMPARE_DIALOG= PREFIX + "compare_dialog_context"; //$NON-NLS-1$
	public static final String EDITION_DIALOG= PREFIX + "edition_dialog_context"; //$NON-NLS-1$
}
