/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.patch;

import org.eclipse.compare.internal.patch.WorkspacePatcher;
import org.eclipse.core.resources.IProject;

/**
 * Provides the headers required to create a workspace patch.
 * @since 3.2
 */
public class WorkspacePatcherUI {
	/**
	 * Returns a string that must be the first line of a workspace patch (a multi-project patch 
	 * that is understood by the Apply Patch wizard). Each project to be included in the patch
	 * must be prefixed by the line obtained from the <code>getWorkspacePatchProjectHeader()</code>.
	 * This snippet outlines how the a workspace patch is to be created:
	 * <pre>
	 *  //Write out workspace patch header
	 *  stream.println(CompareUI.getWorkspacePatchHeader());
	 *  for (int i=0; i<projects.length; i++){
	 *    //Write out project header
	 *    stream.println(CompareUI.getWorkspacePatchProjectHeader(projects[i]);
	 *    //Write out patches in Unified Diff format
	 *  }
	 *  </pre>
	 * @return String
	 * @see WorkspacePatcherUI#getWorkspacePatchProjectHeader(IProject)
	 * @since 3.2
	 */
	public static String getWorkspacePatchHeader() {
		return WorkspacePatcher.MULTIPROJECTPATCH_HEADER+" "+WorkspacePatcher.MULTIPROJECTPATCH_VERSION; //$NON-NLS-1$
	}

	/**
	 * Returns the project header that must appear before any patches that apply to that
	 * project. All patches that are encountered after this header and before the next header
	 * are understood to belong the the project.
	 * @param project project to be patched
	 * @return String
	 * @see WorkspacePatcherUI#getWorkspacePatchHeader()
	 * @since 3.2
	 */
	public static String getWorkspacePatchProjectHeader(IProject project) {
		return WorkspacePatcher.MULTIPROJECTPATCH_PROJECT+" "+ project.getName(); //$NON-NLS-1$
	}
}
