/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.core.runtime.IPath;

/**
 * This interface defines a system path-oriented input to an editor.
 * <p>
 * Clients implementing this editor input interface should override
 * <code>Object.equals(Object)</code> to answer true for two inputs
 * that are the same. The <code>IWorbenchPage.openEditor</code> APIs
 * are dependent on this to find an editor with the same input.
 * </p><p>
 * Path-oriented editors should support this as a valid input type, and
 * can allow full read-write editing of its content.
 * </p><p>
 * A default implementation of this interface is provided by 
 * <code>org.eclipse.ui.part.PathEditorInput</code> and
 * <code>org.eclipse.ui.part.FileEditorInput</code>.  
 * </p><p>
 * All editor inputs must implement the <code>IAdaptable</code> interface;
 * extensions are managed by the platform's adapter manager.
 * </p>
 *
 * @see org.eclipse.core.runtime.IPath
 * @issue should we provide a default implementation of this?
 */
public interface IPathEditorInput extends IEditorInput {
	/**
	 * Returns the system path underlying this editor input.
	 *
	 * @return the underlying system path
	 */
	public IPath getPath();
}
