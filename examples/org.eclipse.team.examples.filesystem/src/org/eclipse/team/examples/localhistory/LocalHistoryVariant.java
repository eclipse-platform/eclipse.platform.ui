/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.examples.localhistory;

import java.text.DateFormat;
import java.util.Date;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.IResourceVariant;

public class LocalHistoryVariant implements IResourceVariant {

	private final IFileState state;

	public LocalHistoryVariant(IFileState state) {
		this.state = state;
	}
	
	public String getName() {
		return state.getName();
	}

	public boolean isContainer() {
		return false;
	}

	public IStorage getStorage(IProgressMonitor monitor) throws TeamException {
		return state;
	}

	public String getContentIdentifier() {
		return DateFormat.getDateTimeInstance().format(new Date(state.getModificationTime()));
	}

	public byte[] asBytes() {
		return null;
	}
	
	public IFileState getFileState() {
		return state;
	}
}
