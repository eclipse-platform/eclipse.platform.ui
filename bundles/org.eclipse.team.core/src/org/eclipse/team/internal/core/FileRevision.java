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

package org.eclipse.team.internal.core;

import java.net.URI;

import org.eclipse.core.resources.IStorage;
import org.eclipse.team.core.filehistory.IFileRevision;
import org.eclipse.team.core.filehistory.ITag;

public abstract class FileRevision implements IFileRevision {

	public abstract IStorage getStorage();

	public abstract String getContentIndentifier();

	public abstract URI getURI();

	public abstract long getTimestamp();
	
	public abstract boolean isDeletion();

	public String getAuthor() {
		return ""; //$NON-NLS-1$
	}

	public String getComment() {
		return ""; //$NON-NLS-1$
	}

	public ITag[] getTags() {
		return new ITag[0];
	}

	
	
}
