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

package org.eclipse.ant.internal.ui.preferences;

public interface IClasspathEntry {
	
	/**
	 * Returns the classpath entries that are the children of this classpath entry
	 * 
	 * @return the child classpath entries of this entry
	 */
	public IClasspathEntry[] getEntries();
	
	/**
	 * Returns whether this classpath entries has child entries.
	 * 
	 * @return whether <code>true</code> if this classpath entry has childern, <code>false</code> otherwise.
	 */
	public boolean hasEntries();
	
	/**
	 * Returns the parent of this classpath entry
	 * 
	 * @return the parent of this classpath entry, or <code>null</code> if none.
	 */
	public IClasspathEntry getParent();
	
	/**
	 * Move this entries child entry in the direction indicated.
	 * @param up if <code>true</code> move the child up one position in the order of the entries.
	 * Otherwise, move the child down one position.
	 * @param child the child entry to move
	 */
	public void moveChild(boolean up, IClasspathEntry child);

}
