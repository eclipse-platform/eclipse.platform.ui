package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.InputStream;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.resources.IStorage;

/**
 * Interface for a <code>IStorage</code> input to an editor.
 * <p>
 * Clients should implement this interface to declare new types of 
 * <code>IStorage</code> editor inputs.
 * </p>
 * <p>
 * File-oriented editors should support this as a valid input type, and display
 * its content for viewing (but not allow modification).
 * Within the editor, the "save" and "save as" operations should create a new 
 * file resource within the workspace.
 * <p>
 * All editor inputs must implement the <code>IAdaptable</code> interface;
 * extensions are managed by the platform's adapter manager.
 * </p>
 */
public interface IStorageEditorInput extends IEditorInput {
/**
 * Returns the underlying IStorage object.
 *
 * @return an IStorage object.
 * @exception CoreException if this method fails
 */
public IStorage getStorage() throws CoreException;
}
