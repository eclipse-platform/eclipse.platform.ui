/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.examples.filesystem.ui;


/**
 * A replace is simply a get that overwrite local changes
 */
public class ReplaceAction extends GetAction {
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.examples.filesystem.ui.GetAction#isOverwriteOutgoing()
	 */
	protected boolean isOverwriteOutgoing() {
		return true;
	}
}
