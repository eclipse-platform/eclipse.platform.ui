/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.resources;

import org.eclipse.core.runtime.*;
import java.io.InputStream;

/**
 * Files are leaf resources which contain data.
 * The contents of a file resource is stored as a file in the local
 * file system.
 * <p>
 * Files, like folders, may exist in the workspace but
 * not be local; non-local file resources serve as placeholders for
 * files whose content and properties have not yet been fetched from
 * a repository.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * Files implement the <code>IAdaptable</code> interface;
 * extensions are managed by the platform's adapter manager.
 * </p>
 *
 * @see Platform#getAdapterManager
 */
public interface IFile extends IResource, IStorage, IAdaptable {
	/**
	 * Character encoding constant (value 0) which identifies
	 * files that have an unknown character encoding scheme.
	 * 
	 * @see IFile#getEncoding
	 */
	public int ENCODING_UNKNOWN = 0;
	/**
	 * Character encoding constant (value 1) which identifies
	 * files that are encoded with the US-ASCII character encoding scheme.
	 * 
	 * @see IFile#getEncoding
	 */
	public int ENCODING_US_ASCII = 1;
	/**
	 * Character encoding constant (value 2) which identifies
	 * files that are encoded with the ISO-8859-1 character encoding scheme,
	 * also known as ISO-LATIN-1.
	 * 
	 * @see IFile#getEncoding
	 */
	public int ENCODING_ISO_8859_1 = 2;
	/**
	 * Character encoding constant (value 3) which identifies
	 * files that are encoded with the UTF-8 character encoding scheme.
	 * 
	 * @see IFile#getEncoding
	 */
	public int ENCODING_UTF_8 = 3;
	/**
	 * Character encoding constant (value 4) which identifies
	 * files that are encoded with the UTF-16BE character encoding scheme.
	 * 
	 * @see IFile#getEncoding
	 */
	public int ENCODING_UTF_16BE = 4;
	/**
	 * Character encoding constant (value 5) which identifies
	 * files that are encoded with the UTF-16LE character encoding scheme.
	 * 
	 * @see IFile#getEncoding
	 */
	public int ENCODING_UTF_16LE = 5;
	/**
	 * Character encoding constant (value 6) which identifies
	 * files that are encoded with the UTF-16 character encoding scheme.
	 * 
	 * @see IFile#getEncoding
	 */
	public int ENCODING_UTF_16 = 6;

/**
 * Appends the entire contents of the given stream to this file.
 * <p>
 * This is a convenience method, fully equivalent to:
 * <pre>
 *   appendContents(source, (keepHistory ? KEEP_HISTORY : IResource.NONE) | (force ? FORCE : IResource.NONE), monitor);
 * </pre>
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event, including an indication 
 * that this file's content have been changed.
 * </p>
 * <p>
 * This method is long-running; progress and cancelation are provided
 * by the given progress monitor. 
 * </p>
 *
 * @param source an input stream containing the new contents of the file
 * @param force a flag controlling how to deal with resources that
 *    are not in sync with the local file system
 * @param keepHistory a flag indicating whether or not to store
 *    the current contents in the local history
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancelation are not desired
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This resource does not exist.</li>
 * <li> The corresponding location in the local file system
 *       is occupied by a directory.</li>
 * <li> The workspace is not in sync with the corresponding location
 *       in the local file system and <code>force </code> is <code>false</code>.</li>
 * <li> Resource changes are disallowed during certain types of resource change 
 *       event notification. See IResourceChangeEvent for more details.</li>
 * <li> The file modification validator disallowed the change.</li>
 * </ul>
 * @see #appendContents(java.io.InputStream,int,IProgressMonitor)
 */
public void appendContents(InputStream source, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException;

/**
 * Appends the entire contents of the given stream to this file.
 * The stream, which must not be <code>null</code>, will get closed 
 * whether this method succeeds or fails.
 * <p>
 * The <code>FORCE</code> update flag controls how this method deals with
 * cases where the workspace is not completely in sync with the local file 
 * system. If <code>FORCE</code> is not specified, the method will only attempt
 * to overwrite a corresponding file in the local file system provided
 * it is in sync with the workspace. This option ensures there is no 
 * unintended data loss; it is the recommended setting.
 * However, if <code>FORCE</code> is specified, an attempt will be made
 * to write a corresponding file in the local file system, overwriting any
 * existing one if need be. In either case, if this method succeeds, the 
 * resource will be marked as being local (even if it wasn't before).
 * </p>
 * <p>
 * If this file is non-local then this method will always fail. The only exception
 * is when <code>FORCE</code> is specified and the file exists in the local 
 * file system. In this case the file is made local and the given contents are appended.
 * </p>
 * <p>
 * The <code>KEEP_HISTORY</code> update flag controls whether or not a copy of
 * current contents of this file should be captured in the workspace's local
 * history (properties are not recorded in the local history). The local history
 * mechanism serves as a safety net to help the user recover from mistakes that
 * might otherwise result in data loss. Specifying <code>KEEP_HISTORY</code>
 * is recommended except in circumstances where past states of the files are of
 * no conceivable interested to the user. Note that local history is maintained
 * with each individual project, and gets discarded when a project is deleted
 * from the workspace.
 * </p>
 * <p>
 * Update flags other than <code>FORCE</code> and <code>KEEP_HISTORY</code> 
 * are ignored.
 * </p>
 * <p>
 * Prior to modifying the contents of this file, the file modification validator (if provided 
 * by the VCM plug-in), will be given a chance to perform any last minute preparations.  Validation
 * is performed by calling <code>IFileModificationValidator.validateSave</code> on this file.
 * If the validation fails, then this operation will fail.
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event, including an indication 
 * that this file's content have been changed.
 * </p>
 * <p>
 * This method is long-running; progress and cancelation are provided
 * by the given progress monitor. 
 * </p>
 *
 * @param source an input stream containing the new contents of the file
 * @param updateFlags bit-wise or of update flag constants
 *   (<code>FORCE</code> and <code>KEEP_HISTORY</code>)
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancelation are not desired
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This resource does not exist.</li>
 * <li> The corresponding location in the local file system
 *       is occupied by a directory.</li>
 * <li> The workspace is not in sync with the corresponding location
 *       in the local file system and <code>FORCE</code> is not specified.</li>
 * <li> Resource changes are disallowed during certain types of resource change 
 *       event notification. See IResourceChangeEvent for more details.</li>
 * <li> The file modification validator disallowed the change.</li>
 * </ul>
 * @since 2.0
 */
public void appendContents(InputStream source, int updateFlags, IProgressMonitor monitor) throws CoreException;

/**
 * Creates a new file resource as a member of this handle's parent resource.
 * <p>
 * This is a convenience method, fully equivalent to:
 * <pre>
 *   create(source, (force ? FORCE : IResource.NONE), monitor);
 * </pre>
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event, including an indication 
 * that the file has been added to its parent.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 *
 * @param source an input stream containing the initial contents of the file,
 *    or <code>null</code> if the file should be marked as not local
 * @param force a flag controlling how to deal with resources that
 *    are not in sync with the local file system
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This resource already exists in the workspace.</li>
 * <li> The parent of this resource does not exist.</li>
 * <li> The project of this resource is not accessible.</li>
 * <li> The parent contains a resource of a different type 
 *      at the same path as this resource.</li>
 * <li> The name of this resource is not valid (according to 
 *    <code>IWorkspace.validateName</code>).</li>
 * <li> The corresponding location in the local file system is occupied
 *    by a directory.</li>
 * <li> The corresponding location in the local file system is occupied
 *    by a file and <code>force </code> is <code>false</code>.</li>
 * <li> Resource changes are disallowed during certain types of resource change 
 *       event notification. See IResourceChangeEvent for more details.</li>
 * </ul>
 */
public void create(InputStream source, boolean force, IProgressMonitor monitor) throws CoreException;

/**
 * Creates a new file resource as a member of this handle's parent resource.
 * The resource's contents are supplied by the data in the given stream.
 * This method closes the stream whether it succeeds or fails.
 * If the stream is <code>null</code> then a file is not created in the local
 * file system and the created file is marked as being non-local.
 * <p>
 * The <code>FORCE</code> update flag controls how this method deals with
 * cases where the workspace is not completely in sync with the local file 
 * system. If <code>FORCE</code> is not specified, the method will only attempt
 * to write a file in the local file system if it does not already exist. 
 * This option ensures there is no unintended data loss; it is the recommended
 * setting. However, if <code>FORCE</code> is specified, this method will 
 * attempt to write a corresponding file in the local file system, 
 * overwriting any existing one if need be.
 * </p>
 * <p>
 * Update flags other than <code>FORCE</code> are ignored.
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event, including an indication 
 * that the file has been added to its parent.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 *
 * @param source an input stream containing the initial contents of the file,
 *    or <code>null</code> if the file should be marked as not local
 * @param updateFlags bit-wise or of update flag constants
 *   (only <code>FORCE</code> is relevant here)
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This resource already exists in the workspace.</li>
 * <li> The parent of this resource does not exist.</li>
 * <li> The project of this resource is not accessible.</li>
 * <li> The parent contains a resource of a different type 
 *      at the same path as this resource.</li>
 * <li> The name of this resource is not valid (according to 
 *    <code>IWorkspace.validateName</code>).</li>
 * <li> The corresponding location in the local file system is occupied
 *    by a directory.</li>
 * <li> The corresponding location in the local file system is occupied
 *    by a file and <code>FORCE</code> is not specified.</li>
 * <li> Resource changes are disallowed during certain types of resource change 
 *       event notification. See IResourceChangeEvent for more details.</li>
 * </ul>
 * @since 2.0
 */
public void create(InputStream source, int updateFlags, IProgressMonitor monitor) throws CoreException;

/**
 * Deletes this file from the workspace.
 * <p>
 * This is a convenience method, fully equivalent to:
 * <pre>
 *   delete((keepHistory ? KEEP_HISTORY : IResource.NONE) | (force ? FORCE : IResource.NONE), monitor);
 * </pre>
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event, including an indication 
 * that this folder has been removed from its parent.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 * 
 * @param force a flag controlling whether resources that are not
 *    in sync with the local file system will be tolerated
 * @param keepHistory a flag controlling whether files under this folder
 *    should be stored in the workspace's local history
  * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This resource could not be deleted for some reason.</li>
 * <li> This resource is out of sync with the local file system
 *      and <code>force</code> is <code>false</code>.</li>
 * <li> Resource changes are disallowed during certain types of resource change 
 *       event notification. See IResourceChangeEvent for more details.</li>
 * </ul>
 * @see IResource#delete(int,IProgressMonitor)
 */
public void delete(boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException;

/**
 * Returns an open input stream on the contents of this file.
 * This refinement of the corresponding <code>IStorage</code> method 
 * returns an open input stream on the contents of this file.
 * The client is responsible for closing the stream when finished.
 *
 * @return an input stream containing the contents of the file
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This resource does not exist.</li>
 * <li> This resource is not local.</li>
 * <li> The workspace is not in sync with the corresponding location
 *       in the local file system.</li>
 * </ul>
 */
public InputStream getContents() throws CoreException;
/**
 * This refinement of the corresponding <code>IStorage</code> method 
 * returns an open input stream on the contents of this file.
 * The client is responsible for closing the stream when finished.
 * If force is <code>true</code> the file is opened and an input
 * stream returned regardless of the sync state of the file. The file
 * is not synchronized with the workspace.
 * If force is <code>false</code> the method fails if not in sync.
 *
 * @param force a flag controlling how to deal with resources that
 *    are not in sync with the local file system
 * @return an input stream containing the contents of the file
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This resource does not exist.</li>
 * <li> This resource is not local.</li>
 * <li> The workspace is not in sync with the corresponding location
 *       in the local file system and force is <code>false</code>.</li>
 * </ul>
 */
public InputStream getContents(boolean force) throws CoreException;
/**
 * Returns a constant identifying the character encoding of this file, or 
 * ENCODING_UNKNOWN if it could not be determined.  The returned constant
 * will be one of the ENCODING_* constants defined on IFile.
 * 
 * This method attempts to guess the file's character encoding by analyzing
 * the first few bytes of the file.  If no identifying pattern is found at the 
 * beginning of the file, ENC_UNKNOWN will be returned.  This method will
 * not attempt any complex analysis of the file to make a guess at the 
 * encoding that is used.
 * 
 * @return The character encoding of this file
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This resource does not exist.</li>
 * <li> This resource could not be read.</li>
 * <li> This resource is not local.</li>
 * <li> The corresponding location in the local file system
 *       is occupied by a directory.</li>
 * </ul>
 */
public int getEncoding() throws CoreException;
/**
 * Returns the full path of this file. 
 * This refinement of the corresponding <code>IStorage</code> and <code>IResource</code>
 * methods links the semantics of resource and storage object paths such that
 * <code>IFile</code>s always have a path and that path is relative to the
 * containing workspace.
 *
 * @see IResource#getFullPath
 * @see IStorage#getFullPath
 */
public IPath getFullPath();
/**
 * Returns a list of past states of this file known to this workspace.
 * Recently added states first.
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 * 
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @return an array of states of this file
 * @exception CoreException if this method fails.
 */
public IFileState[] getHistory(IProgressMonitor monitor) throws CoreException;
/**
 * Returns the name of this file. 
 * This refinement of the corresponding <code>IStorage</code> and <code>IResource</code>
 * methods links the semantics of resource and storage object names such that
 * <code>IFile</code>s always have a name and that name equivalent to the
 * last segment of its full path.
 *
 * @see IResource#getName
 * @see IStorage#getName
 */
public String getName();
/**
 * Returns whether this file is read-only.
 * This refinement of the corresponding <code>IStorage</code> and <code>IResource</code>
 * methods links the semantics of read-only resources and read-only storage objects.
 *
 * @see IResource#isReadOnly
 * @see IStorage#isReadOnly
 */
public boolean isReadOnly();

/**
 * Moves this resource to be at the given location.
 * <p>
 * This is a convenience method, fully equivalent to:
 * <pre>
 *   move(destination, (keepHistory ? KEEP_HISTORY : IResource.NONE) | (force ? FORCE : IResource.NONE), monitor);
 * </pre>
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event, including an indication 
 * that this file has been removed from its parent and a new file
 * has been added to the parent of the destination.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 * 
 * @param destination the destination path 
 * @param force a flag controlling whether resources that are not
 *    in sync with the local file system will be tolerated
 * @param keepHistory a flag controlling whether files under this folder
 *    should be stored in the workspace's local history
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this resource could not be moved. Reasons include:
 * <ul>
 * <li> This resource does not exist.</li>
 * <li> This resource is not local.</li>
 * <li> The resource corresponding to the parent destination path does not exist.</li>
 * <li> The resource corresponding to the parent destination path is a closed 
 *      project.</li>
 * <li> A resource at destination path does exist.</li>
 * <li> A resource of a different type exists at the destination path.</li>
 * <li> This resource is out of sync with the local file system
 *      and <code>force</code> is <code>false</code>.</li>
 * <li> The workspace and the local file system are out of sync
 *      at the destination resource or one of its descendents.</li>
 * <li> Resource changes are disallowed during certain types of resource change 
 *       event notification. See IResourceChangeEvent for more details.</li>
 * </ul>
 *
 * @see IResource#move(IPath,int,IProgressMonitor)
 */
public void move(IPath destination, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException;

/**
 * Sets the contents of this file to the bytes in the given input stream.
 * <p>
 * This is a convenience method, fully equivalent to:
 * <pre>
 *   setContents(source, (keepHistory ? KEEP_HISTORY : IResource.NONE) | (force ? FORCE : IResource.NONE), monitor);
 * </pre>
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event, including an indication 
 * that this file's content have been changed.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 *
 * @param source an input stream containing the new contents of the file
 * @param force a flag controlling how to deal with resources that
 *    are not in sync with the local file system
 * @param keepHistory a flag indicating whether or not store
 *    the current contents in the local history
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This resource does not exist.</li>
 * <li> The corresponding location in the local file system
 *       is occupied by a directory.</li>
 * <li> The workspace is not in sync with the corresponding location
 *       in the local file system and <code>force </code> is <code>false</code>.</li>
 * <li> Resource changes are disallowed during certain types of resource change 
 *       event notification. See IResourceChangeEvent for more details.</li>
 * <li> The file modification validator disallowed the change.</li>
 * </ul>
 * @see #setContents(java.io.InputStream,int,IProgressMonitor)
 */
public void setContents(InputStream source, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException;

/**
 * Sets the contents of this file to the bytes in the given file state.
 * <p>
 * This is a convenience method, fully equivalent to:
 * <pre>
 *   setContents(source, (keepHistory ? KEEP_HISTORY : IResource.NONE) | (force ? FORCE : IResource.NONE), monitor);
 * </pre>
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event, including an indication 
 * that this file's content have been changed.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 *
 * @param source a previous state of this resource
 * @param force a flag controlling how to deal with resources that
 *    are not in sync with the local file system
 * @param keepHistory a flag indicating whether or not store
 *    the current contents in the local history
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This resource does not exist.</li>
 * <li> The state does not exist.</li>
 * <li> The corresponding location in the local file system
 *       is occupied by a directory.</li>
 * <li> The workspace is not in sync with the corresponding location
 *       in the local file system and <code>force </code> is <code>false</code>.</li>
 * <li> Resource changes are disallowed during certain types of resource change 
 *       event notification. See IResourceChangeEvent for more details.</li>
 * <li> The file modification validator disallowed the change.</li>
 * </ul>
 * @see #setContents(IFileState,int,IProgressMonitor)
 */
public void setContents(IFileState source, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException;

/**
 * Sets the contents of this file to the bytes in the given input stream.
 * The stream will get closed whether this method succeeds or fails.
 * If the stream is <code>null</code> then the content is set to be the
 * empty sequence of bytes.
 * <p>
 * The <code>FORCE</code> update flag controls how this method deals with
 * cases where the workspace is not completely in sync with the local file 
 * system. If <code>FORCE</code> is not specified, the method will only attempt
 * to overwrite a corresponding file in the local file system provided
 * it is in sync with the workspace. This option ensures there is no 
 * unintended data loss; it is the recommended setting.
 * However, if <code>FORCE</code> is specified, an attempt will be made
 * to write a corresponding file in the local file system, overwriting any
 * existing one if need be. In either case, if this method succeeds, the 
 * resource will be marked as being local (even if it wasn't before).
 * </p>
 * <p>
 * The <code>KEEP_HISTORY</code> update flag controls whether or not a copy of
 * current contents of this file should be captured in the workspace's local
 * history (properties are not recorded in the local history). The local history
 * mechanism serves as a safety net to help the user recover from mistakes that
 * might otherwise result in data loss. Specifying <code>KEEP_HISTORY</code>
 * is recommended except in circumstances where past states of the files are of
 * no conceivable interested to the user. Note that local history is maintained
 * with each individual project, and gets discarded when a project is deleted
 * from the workspace.
 * </p>
 * <p>
 * Update flags other than <code>FORCE</code> and <code>KEEP_HISTORY</code> 
 * are ignored.
 * </p>
 * <p>
 * Prior to modifying the contents of this file, the file modification validator (if provided 
 * by the VCM plug-in), will be given a chance to perform any last minute preparations.  Validation
 * is performed by calling <code>IFileModificationValidator.validateSave</code> on this file.
 * If the validation fails, then this operation will fail.
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event, including an indication 
 * that this file's content have been changed.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 *
 * @param source an input stream containing the new contents of the file
 * @param updateFlags bit-wise or of update flag constants
 *   (<code>FORCE</code> and <code>KEEP_HISTORY</code>)
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This resource does not exist.</li>
 * <li> The corresponding location in the local file system
 *       is occupied by a directory.</li>
 * <li> The workspace is not in sync with the corresponding location
 *       in the local file system and <code>FORCE</code> is not specified.</li>
 * <li> Resource changes are disallowed during certain types of resource change 
 *       event notification. See IResourceChangeEvent for more details.</li>
 * <li> The file modification validator disallowed the change.</li>
 * </ul>
 * @since 2.0
 */
public void setContents(InputStream source, int updateFlags, IProgressMonitor monitor) throws CoreException;

/**
 * Sets the contents of this file to the bytes in the given file state.
 * <p>
 * The <code>FORCE</code> update flag controls how this method deals with
 * cases where the workspace is not completely in sync with the local file 
 * system. If <code>FORCE</code> is not specified, the method will only attempt
 * to overwrite a corresponding file in the local file system provided
 * it is in sync with the workspace. This option ensures there is no 
 * unintended data loss; it is the recommended setting.
 * However, if <code>FORCE</code> is specified, an attempt will be made
 * to write a corresponding file in the local file system, overwriting any
 * existing one if need be. In either case, if this method succeeds, the 
 * resource will be marked as being local (even if it wasn't before).
 * </p>
 * <p>
 * The <code>KEEP_HISTORY</code> update flag controls whether or not a copy of
 * current contents of this file should be captured in the workspace's local
 * history (properties are not recorded in the local history). The local history
 * mechanism serves as a safety net to help the user recover from mistakes that
 * might otherwise result in data loss. Specifying <code>KEEP_HISTORY</code>
 * is recommended except in circumstances where past states of the files are of
 * no conceivable interested to the user. Note that local history is maintained
 * with each individual project, and gets discarded when a project is deleted
 * from the workspace.
 * </p>
 * <p>
 * Update flags other than <code>FORCE</code> and <code>KEEP_HISTORY</code> 
 * are ignored.
 * </p>
 * <p>
 * Prior to modifying the contents of this file, the file modification validator (if provided 
 * by the VCM plug-in), will be given a chance to perform any last minute preparations.  Validation
 * is performed by calling <code>IFileModificationValidator.validateSave</code> on this file.
 * If the validation fails, then this operation will fail.
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event, including an indication 
 * that this file's content have been changed.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 *
 * @param source a previous state of this resource
 * @param updateFlags bit-wise or of update flag constants
 *   (<code>FORCE</code> and <code>KEEP_HISTORY</code>)
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This resource does not exist.</li>
 * <li> The state does not exist.</li>
 * <li> The corresponding location in the local file system
 *       is occupied by a directory.</li>
 * <li> The workspace is not in sync with the corresponding location
 *       in the local file system and <code>FORCE</code> is not specified.</li>
 * <li> Resource changes are disallowed during certain types of resource change 
 *       event notification. See IResourceChangeEvent for more details.</li>
 * <li> The file modification validator disallowed the change.</li>
 * </ul>
 * @since 2.0
 */
public void setContents(IFileState source, int updateFlags, IProgressMonitor monitor) throws CoreException;
}
