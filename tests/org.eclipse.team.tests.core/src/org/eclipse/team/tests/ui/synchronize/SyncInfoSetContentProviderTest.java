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
package org.eclipse.team.tests.ui.synchronize;

import java.util.Iterator;
import java.util.List;

import junit.framework.Test;

import org.eclipse.core.resources.*;
import org.eclipse.team.core.synchronize.SyncInfoTree;
import org.eclipse.team.internal.ui.synchronize.SynchronizeModelProvider;

/**
 * Tests for the SyncInfoSet content providers.
 */
public class SyncInfoSetContentProviderTest extends TestDiffNodePresentationModel {
	
	/**
	 * Constructor for CVSProviderTest
	 */
	public SyncInfoSetContentProviderTest() {
		super();
	}

	/**
	 * Constructor for CVSProviderTest
	 */
	public SyncInfoSetContentProviderTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(SyncInfoSetContentProviderTest.class);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.tests.ui.synchronize.TestDiffNodePresentationModel#getDiffNodeController()
	 */
	protected SynchronizeModelProvider getDiffNodeController(SyncInfoTree set) {
		//return new CompressedFoldersModelProvider(set);
		return null;
	}

	private void assertFolderPresent(IFolder folder, List resources) {
		// First, if the folder is out-of-sync, it should be visible
		for (Iterator iter = resources.iterator(); iter.hasNext();) {
			IResource resource = (IResource) iter.next();
			if (resource.equals(folder)) {
				// The folder should be present.
				// Remove it since it has been verified
				iter.remove();
				return;
			}
		}
		// If the folder contains a file in the list, it is also OK
		for (Iterator iter = resources.iterator(); iter.hasNext();) {
			IResource resource = (IResource) iter.next();
			if (resource.getType() == IResource.FILE && resource.getParent().equals(folder)) {
				// The compressed folder is valid since it contains an out-of-sync file
				// However, the resource is left since it has not been verified (only it's parent)
				return;
			}
		}
		fail("Folder " + folder.getFullPath() + " should not be visible but is.");
	}

	private void assertFilePresent(IResource itemResource, List resources) {
		for (Iterator iter = resources.iterator(); iter.hasNext();) {
			IResource resource = (IResource) iter.next();
			if (resource.equals(itemResource)) {
				// The resource has been verified so it can be removed
				iter.remove();
				return;
			}
		}
		fail("Resource " + itemResource.getFullPath() + " should not be visible but is.");
	}

	private void assertProjectPresent(IProject project, List resources) {
//		First, if the project is out-of-sync, it should be visible
		for (Iterator iter = resources.iterator(); iter.hasNext();) {
			IResource resource = (IResource) iter.next();
			if (resource.equals(project)) {
				// The folder should be present.
				// Remove it since it has been verified
				iter.remove();
				return;
			}
		}
		for (Iterator iter = resources.iterator(); iter.hasNext();) {
			IResource resource = (IResource) iter.next();
			if (resource.getProject().equals(project)) {
				return;
			}
		}
		fail("Project " + project.getName() + " should not be visible but is.");
	}
}
