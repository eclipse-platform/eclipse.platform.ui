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
package org.eclipse.ui;

import org.eclipse.core.resources.IFile;

/**
 * This interface defines a file-oriented input to an editor.
 * <p>
 * File-oriented editors should support this as a valid input type, and allow
 * full read-write editing of its content.
 * </p><p>
 * A default implementation of this interface is provided by 
 * org.eclipse.ui.part.FileEditorInput.  
 * </p><p>
 * All editor inputs must implement the <code>IAdaptable</code> interface;
 * extensions are managed by the platform's adapter manager.
 * </p>
 *
 * @see org.eclipse.core.resources.IFile
 */
public interface IFileEditorInput extends IStorageEditorInput {
/**
 * Returns the file resource underlying this editor input.
 *
 * @return the underlying file
 */
public IFile getFile();
}
