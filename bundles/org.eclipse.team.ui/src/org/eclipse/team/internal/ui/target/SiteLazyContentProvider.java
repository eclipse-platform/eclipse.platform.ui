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
package org.eclipse.team.internal.ui.target;

import org.eclipse.ui.model.WorkbenchContentProvider;

public class SiteLazyContentProvider extends WorkbenchContentProvider {

	public boolean hasChildren(Object element) {
		if (element == null) {
			return false;
		}
		// the + box will always appear, but then disappear
		// if not needed after you first click on it.
		return true;	// just always return true to avoid going to server
	}
}
