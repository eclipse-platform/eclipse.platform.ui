package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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
