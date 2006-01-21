/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.core.history.provider;

import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.history.ITag;
import org.eclipse.team.core.variants.FileState;

/**
 * Abstract FileRevision class.
 * @see IFileRevision
 * @since 3.2
 */
public abstract class FileRevision extends FileState implements IFileRevision {

	public String getContentIdentifier() {
		return null;
	}
	public String getAuthor() {
		return null;
	}
	public String getComment() {
		return null;
	}
	
	public ITag[] getTags() {
		return new ITag[0];
	}

	
	
}
