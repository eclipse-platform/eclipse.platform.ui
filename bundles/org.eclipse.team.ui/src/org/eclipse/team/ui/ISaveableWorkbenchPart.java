/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui;

import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPart;

public interface ISaveableWorkbenchPart extends ISaveablePart, IWorkbenchPart {
	
	/**
	 * The property id for <code>isDirty</code>.
	 */
	public static final int PROP_DIRTY = ISaveableWorkbenchPart.PROP_DIRTY;
}
