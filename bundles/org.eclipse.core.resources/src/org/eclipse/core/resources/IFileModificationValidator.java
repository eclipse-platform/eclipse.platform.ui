package org.eclipse.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
/**
 * @since 2.0
 */
public interface IFileModificationValidator {
/**
 * Validates that the given files can be modified.  The files must all exist
 * in the workspace.  The optional context object may be supplied if
 * UI-based validation is required.  If the context is null, the validator
 * must attempt to perform the validation in a headless manner.
 * The returned status is <code>IStatus.OK</code> if this validator 
 * believes the given file can be modified.  Other return statuses indicate
 * the reason why the individual files cannot be modified.
 */
public IStatus validateEdit(IFile[] files, Object context);
/**
 * Validates that the given file can be saved.  This method is called from 
 * <code>IFile#setContents</code> and <code>IFile#appendContents</code> 
 * before any attempt to write data to disk.  The returned status is 
 * <code>IStatus.OK</code> if this validator believes the given file can be 
 * successfully saved.  In all other cases the return value is a non-OK status.  
 * Note that a return value of <code>IStatus.OK</code> does not guarantee 
 * that the save will succeed.
 * 
 * @return a status indicating whether or not it is reasonable to try writing to the given file.
 *	A return value with an <code>IStatus.OK</code> code indicates a save should be attempted.
 */
public IStatus validateSave(IFile file);
}