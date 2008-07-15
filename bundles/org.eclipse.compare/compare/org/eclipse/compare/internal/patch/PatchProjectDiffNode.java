/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal.patch;

import org.eclipse.compare.CompareUI;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.internal.core.patch.DiffProject;
import org.eclipse.compare.patch.PatchConfiguration;
import org.eclipse.compare.structuremergeviewer.*;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.graphics.Image;

public class PatchProjectDiffNode extends PatchDiffNode {

	private final DiffProject project;
	private final PatchConfiguration configuration;

	public PatchProjectDiffNode(IDiffContainer parent, DiffProject project, PatchConfiguration configuration) {
		super(project, parent, Differencer.NO_CHANGE);
		this.project = project;
		this.configuration = configuration;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.DiffNode#getName()
	 */
	public String getName() {
		return project.getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.DiffNode#getImage()
	 */
	public Image getImage() {
		Image image = CompareUI.getImage(project.getProject());
		if (containsProblems()) {
			LocalResourceManager imageCache = PatchCompareEditorInput.getImageCache(getConfiguration());
			image = HunkTypedElement.getHunkErrorImage(image, imageCache, true);
		}
		return image;
	}

	private boolean containsProblems() {
		IDiffElement[] elements = getChildren();
		for (int i = 0; i < elements.length; i++) {
			IDiffElement diffElement = elements[i];
			if (diffElement instanceof PatchFileDiffNode) {
				PatchFileDiffNode node = (PatchFileDiffNode) diffElement;
				if (node.getDiffResult().containsProblems())
					return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.DiffNode#getType()
	 */
	public String getType() {
		return ITypedElement.FOLDER_TYPE;
	}
	
	protected PatchConfiguration getConfiguration() {
		return configuration;
	}

	public DiffProject getDiffProject() {
		return project;
	}

}
