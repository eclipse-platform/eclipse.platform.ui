package org.eclipse.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import java.io.InputStream;

/**
 * A previous state of a file stored in the workspace's local history. 
 * <p>
 * Certain methods for updating, deleting, or moving a file cause the
 * "before" contents of the file to be copied to an internal area of the
 * workspace called the <b>local history area</b> thus providing 
 * a limited history of earlier states of a file.
 * </p>
 * <p>
 * This history is based on paths only. Thus, for example, moving a file
 * does not move its history. The history, if any, remains related to
 * the original path. Deleting a file and creating another one at the
 * same path also does not affect the history. If the original file had
 * history, that same history will be available for the new one.
 * </p>
 * <p>
 * The local history does not track resource properties.
 * File states are volatile; the platform does not guarantee that a 
 * certain state will always be in the local history.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * File state objects implement the <code>IAdaptable</code> interface;
 * extensions are managed by the platform's adapter manager.
 * </p>
 *
 * @see IFile
 * @see IStorage
 * @see Platform#getAdapterManager
 */
public interface IFileState extends IStorage, IAdaptable {
/**
 * Returns whether this file state still exists in the local history.
 *
 * @return <code>true</code> if this state exists, and <code>false</code>
 *   if it does not
 */
public boolean exists();
/**
 * Returns an open input stream on the contents of this file state.
 * This refinement of the corresponding
 * <code>IStorage</code> method returns an open input stream 
 * on the contents this file state represents.
 * The client is responsible for closing the stream when finished.
 *
 * @return an input stream containing the contents of the file
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This state does not exist.</li>
 * </ul>
 */
public InputStream getContents() throws CoreException;
/**
 * Returns the full path of this file state.
 * This refinement of the corresponding <code>IStorage</code> 
 * method specifies that <code>IFileState</code>s always have a 
 * path and that path is the full workspace path of the file represented by this state.
 *
 * @see IResource#getFullPath
 * @see IStorage#getFullPath
 */
public IPath getFullPath();
/**
 * Returns the modification time of the file. If you create a file at
 * 9:00 and modify it at 11:00, the file state added to the history
 * at 11:00 will have 9:00 as its modification time.
 * <p>
 * Note that is used only to give the user a general idea of how
 * old this file state is.
 *
 * @return the time of last modification, in milliseconds since 
 * January 1, 1970, 00:00:00 GMT.
 */
public long getModificationTime();
/**
 * Returns the name of this file state. 
 * This refinement of the corresponding <code>IStorage</code> 
 * method specifies that <code>IFileState</code>s always have a 
 * name and that name is equivalent to the last segment of the full path
 * of the resource represented by this state.
 *
 * @see IResource#getName
 * @see IStorage#getName
 */
public String getName();
/**
 * Returns whether this file state is read-only.
 * This refinement of the corresponding
 * <code>IStorage</code> method restricts <code>IFileState</code>s
 * always be read-only.
 *
 * @see IStorage
 */
public boolean isReadOnly();
}
