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
package org.eclipse.core.resources.team;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.*;

/**
 * A general hook class for operations that team providers may be 
 * interested in participating in.  Implementors of the hook should provide
 * a concrete subclass, and override any methods they are interested in.
 * <p>
 * This class is intended to be subclassed by the team component in
 * conjunction with the <code>org.eclipse.core.resources.teamHook</code>
 * standard extension point. Individual team providers may also subclass this
 * class. It is not intended to be subclassed by other clients. The methods
 * defined on this class are called from within the implementations of
 * workspace API methods and must not be invoked directly by clients.
 * </p>
 * 
 * @since 2.1
 */
public abstract class TeamHook {
/**
 * Creates a new team hook.  Default constructor for use by subclasses and the 
 * resources plugin only.
 */
protected TeamHook() {
}	
/**
 * Validates whether a particular attempt at link creation is allowed.  This gives
 * team providers an opportunity to hook into the beginning of the implementation
 * of <code>IFile.createLink</code>.
 * <p>
 * The implementation of this method runs "below" the resources API and is
 * therefore very restricted in what resource API method it can call. The
 * list of useable methods includes most resource operations that read but
 * do not update the resource tree; resource operations that modify 
 * resources and trigger deltas must not be called from within the dynamic
 * scope of the invocation of this method.
 * </p>
 * <p>
 * This method should be overridden by subclasses that want to control what
 * links are created.  The default implementation of this method allows all links
 * to be created.
 * </p>
 * 
 * @param file the file to be linked
 * @param updateFlags bit-wise or of update flag constants
 *   (only ALLOW_MISSING_LOCAL is relevant here)
 * @param location a file system path where the file should be linked
 * @return a status object with code <code>IStatus.OK</code> 
 * 	if linking is allowed, otherwise a status object with severity 
 * 	<code>IStatus.ERROR</code> indicating why the creation is not allowed.
 * @see org.eclipse.core.resources.IResource#ALLOW_MISSING_LOCAL
 */
public IStatus validateCreateLink(IFile file, int updateFlags, IPath location) {
	return Status.OK_STATUS;
}
/**
 * Validates whether a particular attempt at link creation is allowed.  This gives
 * team providers an opportunity to hook into the beginning of the implementation
 * of <code>IFolder.createLink</code>.
 * <p>
 * The implementation of this method runs "below" the resources API and is
 * therefore very restricted in what resource API method it can call. The
 * list of useable methods includes most resource operations that read but
 * do not update the resource tree; resource operations that modify 
 * resources and trigger deltas must not be called from within the dynamic
 * scope of the invocation of this method.
 * </p>
 * <p>
 * This method should be overridden by subclasses that want to control what
 * links are created.  The default implementation of this method allows all links
 * to be created.
 * </p>
 * 
 * @param folder the file to be linked
 * @param updateFlags bit-wise or of update flag constants
 *   (only ALLOW_MISSING_LOCAL is relevant here)
 * @param location a file system path where the folder should be linked
 * @return a status object with code <code>IStatus.OK</code> 
 * 	if linking is allowed, otherwise a status object with severity 
 * 	<code>IStatus.ERROR</code> indicating why the creation is not allowed.
 * @see org.eclipse.core.resources.IResource#ALLOW_MISSING_LOCAL
 */
public IStatus validateCreateLink(IFolder folder, int updateFlags, IPath location) {
	return Status.OK_STATUS;
}
}
