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

import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.filehistory.IFileHistory;
import org.eclipse.team.core.filehistory.IFileRevision;

public abstract class FileHistory implements IFileHistory {

	public abstract IFileRevision[] getFileRevisions() throws TeamException;

	public abstract IFileRevision getFileRevision(String id) throws TeamException;

	public abstract IFileRevision getPredecessor(IFileRevision revision) throws TeamException;

	public abstract IFileRevision[] getDirectDescendents(IFileRevision revision) throws TeamException;
}
