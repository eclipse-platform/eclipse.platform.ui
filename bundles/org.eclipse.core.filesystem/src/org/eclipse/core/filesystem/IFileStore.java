/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.filesystem;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.*;

/**
 * A file store is responsible for storage and retrieval of a single file in some file system.  
 * The actual protocols and media used for communicating with the file system
 * are abstracted away by this interface, apart from the store's ability
 * to represent itself as a hierarchical {@link java.net.URI}.
 * <p>
 * File store instances are lightweight handle objects;  a store knows how to access
 * and store file information, but does not retain a large memory footprint or
 * operating system connections such as sockets or file handles. The presence
 * of a file store instance does not imply the existence of a corresponding file
 * in the file system represented by that store.  A store that has a
 * corresponding file in its file system is said to <i>exist</i>.
 * </p>
 * <p>
 * As much as possible, implementations of this API maintain the characteristics of the
 * underlying file system represented by this store.  For example, store instances
 * will be case-sensitive and case-preserving only when representing case-sensitive
 * and case-preserving file systems.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.  File store
 * implementations must subclass {@link FileStore} rather than implementing
 * this interface directly.
 * </p>
 * 
 * @since 1.0
 */
public interface IFileStore extends IAdaptable {

	/**
	 * Returns an {@link IFileInfo} instance for each file and directory contained 
	 * within this store.
	 * 
	 * @param options bit-wise or of option flag constants ().
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return An array of information about the children of this store, or an empty 
	 * array if this store has no children.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This store does not exist.</li>
	 * </ul>
	 */
	public abstract IFileInfo[] childInfos(int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns the names of the files and directories contained within this store.
	 * 
	 * @param options bit-wise or of option flag constants ().
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return The names of the children of this store, or an empty array if this
	 * store has no children.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This store does not exist.</li>
	 * </ul>
	 */
	public abstract String[] childNames(int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns an {@link IFileStore} instance for each file and directory contained 
	 * within this store.
	 * 
	 * @param options bit-wise or of option flag constants ().
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return The children of this store, or an empty array if this
	 * store has no children.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This store does not exist.</li>
	 * </ul>
	 */
	public abstract IFileStore[] childStores(int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Copies the file represented by this store to the provided destination store.
	 * Copying occurs with best-effort semantics; if some files cannot be copied,
	 * exceptions are recorded but other files will continue to be copied if possible.
	 * 
	 * <p>
	 * The {@link EFS#OVERWRITE} option flag indicates how
	 * this method deals with files that already exist at the copy destination. If
	 * the <code>OVERWRITE</code> flag is present, then existing files at the
	 * destination are overwritten with the corresponding files from the source
	 * of the copy operation.  When this flag is not present, existing files at
	 * the destination are not overwritten and an exception is thrown indicating
	 * what files could not be copied.
	 * </p>
	 * <p>
	 * The {@link EFS#SHALLOW} option flag indicates how
	 * this method deals with copying of directories. If the <code>SHALLOW</code> 
	 * flag is present, then a directory will be copied but the files and directories
	 * within it will not.  When this flag is not present, all child directories and files
	 * of a directory are copied recursively.
	 * </p>
	 * 
	 * @param destination The destination of the copy.
	 * @param options bit-wise or of option flag constants (
	 * {@link EFS#OVERWRITE} or {@link EFS#SHALLOW}).
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This store does not exist.</li>
	 * <li> The <code>OVERWRITE</code> flag is not specified and a file of the
	 * same name already exists at the copy destination.</li>
	 * </ul>
	 */
	public abstract void copy(IFileStore destination, int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Deletes the files and directories represented by this store. Deletion of a file
	 * that does not exist has no effect.
	 * <p>
	 * Deletion occurs with best-effort semantics; if some files cannot be deleted,
	 * exceptions are recorded but other files will continue to be deleted if possible.
	 * </p>
	 * 
	 * @param options bit-wise or of option flag constants (none currently applicable).
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li>Files or directories could not be deleted.
	 * </ul>
	 */
	public abstract void delete(int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Fetches and returns information about this file from the underlying file
	 * system.
	 * <p>
	 * This is a convenience method, fully equivalent to 
	 * <code>fetchInfo(EFS.NONE, null)</code>.
	 * </p>
	 * @return A structure containing information about this file.
	 * @see #fetchInfo(int, IProgressMonitor)
	 */
	public abstract IFileInfo fetchInfo();

	/**
	 * Fetches and returns information about this file from the underlying file
	 * system.
	 * <p>
	 * This method succeeds regardless of whether a corresponding
	 * file currently exists in the underlying file system. In the case of a non-existent
	 * file, the returned info will include the file's name and will return <code>false</code>
	 * when IFileInfo#exists() is called, but all other information will assume default 
	 * values.
	 * 
	 * @param options bit-wise or of option flag constants (none are currently applicable).
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return A structure containing information about this file.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li>Problems occurred while contacting the file system.</li>
	 * </ul>
	 */
	public abstract IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException;

	
	/**
	 * Returns a child of this store as specified by the provided path.  The
	 * path is treated as relative to this store.  This is equivalent to
	 * <pre>
	 *    IFileStore result = this;
	 *    for (int i = 0; i < path.segmentCount(); i++) {
	 *       result = result.getChild(path.segment(i));
	 *    return result;
	 * </pre>
	 * </p>
	 * <p>
	 * This is a handle-only method; a child is provided regardless
	 * of whether this store or the child store exists, or whether this store
	 * represents a directory or not.
	 * </p>
	 * 
	 * @param path The path of the child store to return
	 * @return A child file store.
	 */
	public abstract IFileStore getChild(IPath path);

	/**
	 * Returns a child store with the provided name whose parent is
	 * this store.  This is a handle-only method; a child is provided regardless
	 * of whether this store or the child store exists, or whether this store
	 * represents a directory or not.
	 * 
	 * @param name The name of the child store to return
	 * @return A child file store.
	 */
	public abstract IFileStore getChild(String name);

	/**
	 * Returns the file system this store belongs to.
	 * 
	 * @return The file system this store belongs to.
	 */
	public abstract IFileSystem getFileSystem();

	/**
	 * Returns the name of this store.  This is a handle-only method; the name
	 * is returned regardless of whether this store exists.
	 * <p>
	 * Note that when dealing with case-insensitive file systems, this name
	 * may differ in case from the name of the corresponding file in the file
	 * system.  To obtain the exact name used in the file system, use
	 * <code>fetchInfo().getName()</code>.
	 * </p>
	 * @return The name of this store
	 */
	public abstract String getName();

	/**
	 * Returns the parent of this store.  This is a handle only method; the parent
	 * is returned regardless of whether this store or the parent store exists. This
	 * method returns <code>null</code> when this store represents the root
	 * directory of a file system.
	 * 
	 * @return The parent store, or <code>null</code> if this store is the root
	 * of a file system.
	 */
	public abstract IFileStore getParent();

	/**
	 * Returns whether this store is a parent of the provided store.  This
	 * is equivalent to, but typically more efficient than, the following:
	 * <code>
	 * while (other != null) {
	 *    if (this.equals(other))
	 *       return true;
	 *    other = other.getParent();
	 * }
	 * return false;
	 * </code>
	 * <p>
	 * This is a handle only method; this test works regardless of whether
	 * this store or the parameter store exists.
	 * </p>
	 * 
	 * @param other The store to test for parentage.
	 * @return <code>true</code> if this store is a parent of the provided
	 * store, and <code>false</code> otherwise.
	 */
	public abstract boolean isParentOf(IFileStore other);

	/**
	 * Creates and returns a new directory.  If the directory already exists,
	 * this method has no effect.
	 * <p>
	 * The {@link EFS#SHALLOW} option flag indicates how
	 * this method deals with creation when the parent directory does not exist.
	 * If the <code>SHALLOW</code> flag is present, this method will fail if
	 * the parent directory does not exist.  When the flag is not present, all
	 * necessary parent directories are also created.
	 * </p>
	 * 
	 * @param options bit-wise or of option flag constants ({@link EFS#SHALLOW}).
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return The created directory
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li>The directory could not be created</li>
	 * <li>The {@link EFS#SHALLOW} option flag was
	 * specified and the parent of this directory does not exist.</li>
	 * </ul>
	 */
	public abstract IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Moves the file represented by this store to the provided destination store.
	 * Moving occurs with best-effort semantics; if some files cannot be moved,
	 * exceptions are recorded but other files will continue to be moved if possible.
	 * 
	 * <p>
	 * The {@link EFS#OVERWRITE} option flag indicates how
	 * this method deals with files that already exist at the move destination. If
	 * the <code>OVERWRITE</code> flag is present, then existing files at the
	 * destination are overwritten with the corresponding files from the source
	 * of the move operation.  When this flag is not present, existing files at
	 * the destination are not overwritten and an exception is thrown indicating
	 * what files could not be moved.
	 * </p>
	 * 
	 * @param destination The destination of the move.
	 * @param options bit-wise or of option flag constants 
	 * ({@link EFS#OVERWRITE}).
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This store does not exist.</li>
	 * <li> The {@link EFS#OVERWRITE} flag is not specified and a file of the
	 * same name already exists at the destination.</li>
	 * </ul>
	 */
	public abstract void move(IFileStore destination, int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns an open input stream on the contents of this file.  The caller
	 * is responsible for closing the provided stream when it is no longer
	 * needed.
	 * <p>
	 * The returned stream is not guaranteed to be buffered efficiently.  When reading
	 * large blocks of data from the stream, a <code>BufferedInputStream</code>
	 * wrapper should be used, or some other form of content buffering.
	 * </p>
	 * 
	 * @param options bit-wise or of option flag constants ().
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return An input stream on the contents of this file.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This store does not exist.</li>
	 * <li>This store represents a directory.</li>
	 * </ul>
	 */
	public abstract InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns an open output stream on the contents of this file.  The caller
	 * is responsible for closing the provided stream when it is no longer
	 * needed.  This file need not exist in the underlying file system at the
	 * time this method is called.
	 * <p>
	 * The returned stream is not guaranteed to be buffered efficiently.  When reading
	 * large blocks of data from the stream, a <code>BufferedOutputStream</code>
	 * wrapper should be used, or some other form of content buffering.
	 * </p>
	 * <p>
	 * The {@link EFS#APPEND} update flag controls where
	 * output is written to the file.  If this flag is specified, content written
	 * to the stream will be appended to the end of the file.  If this flag is
	 * not specified, the contents of the existing file, if any, is truncated to zero
	 * and the new output will be written from the start of the file.
	 * </p>
	 * 
	 * @param options bit-wise or of option flag constants (
	 * {@link EFS#APPEND}).
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return An output stream on the contents of this file.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This store does not exist.</li>
	 * <li>This store represents a directory.</li>
	 * </ul>
	 */
	public abstract OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Writes information about this file to the underlying file system. Only 
	 * certain parts of the file information structure can be written using this
	 * method, as specified by the option flags.  Other changed information
	 * in the provided info will be ignored.  This method has no effect when no 
	 * option flags are provided.  The following example sets the last modified
	 * time for a file store, leaving other values unchanged:
	 * <pre>
	 *    IFileInfo info = FileSystemCore#createFileInfo();
	 *    info.setLastModified(System.currentTimeMillis());
	 *    store.putInfo(info, EFS.SET_LAST_MODIFIED, monitor);
	 * </pre>
	 * <p>
	 * The {@link EFS#SET_ATTRIBUTES} update flag controls 
	 * whether the file's attributes are changed.  When this flag is specified,
	 * the <code>EFS#ATTRIBUTE_*</code> values, with
	 * the exception of <code>EFS#ATTRIBUTE_DIRECTORY</code>
	 * are set for this file. When this flag is not specified, changed attributes
	 * on the provided file info are ignored.
	 * </p>
	 * <p>
	 * The {@link EFS#SET_LAST_MODIFIED} update flag controls 
	 * whether the file's last modified time is changed.  When this flag is specified,
	 * the last modified time for the file in the underlying file system is updated
	 * to the value in the provided info object.  Due to the different granularities
	 * of file systems, the time that is set might not exact match the provided
	 * time.
	 * </p>
	 * 
	 * @param info The file information instance containing the values to set.
	 * @param options bit-wise or of option flag constants (
	 * {@link EFS#SET_ATTRIBUTES} or {@link EFS#SET_LAST_MODIFIED}).
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This store does not exist.</li>
	 * </ul>
	 * @see FileSystemCore#createFileInfo()
	 */
	public abstract void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns a string representation of this store.  The string will be translated
	 * if applicable, and suitable for displaying in error messages to an end-user.
	 * The actual format of the string is unspecified.
	 * 
	 * @return A string representation of this store.
	 */
	public String toString();

	/**
	 * Returns a URI instance corresponding to this store.  The resulting URI,
	 * when passed to {@link FileSystemCore#getStore(URI)}, will return a store equal
	 * to this instance.
	 * 
	 * @return A URI corresponding to this store.
	 * @see FileSystemCore#getStore(URI)
	 */
	public abstract URI toURI();
}