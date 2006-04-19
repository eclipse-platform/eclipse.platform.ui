/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.filehistory;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.core.history.LocalFileRevision;

/**
 * This subclass is used exclusively for displaying a LocalFileRevision
 * in a CVSHistoryPage. This class was required to link local file revisions
 * to the CVSHistoryPageSource through the use of the adapter mechanism.
 *
 * @since 3.2
 */
public class CVSLocalFileRevision extends LocalFileRevision implements IAdaptable {

	public CVSLocalFileRevision(IFile file) {
		super(file);
	}

	public CVSLocalFileRevision(IFileState fileState) {
		super(fileState);
	}

	public Object getAdapter(Class adapter) {
		if (adapter == ICVSFile.class)
			return CVSWorkspaceRoot.getCVSFileFor(ResourcesPlugin.getWorkspace().getRoot().getFile(URIUtil.toPath(getURI())));
		
		return null;
	}


}
