/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.repo;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.actions.CVSAction;
import org.eclipse.team.internal.ccvs.ui.model.BranchCategory;

/**
 * Abstract superclass for actions in the repositories view
 */
public abstract class CVSRepoViewAction extends CVSAction {

	/**
	 * Returns the selected CVS Repository locations
	 */
	protected ICVSRepositoryLocation[] getSelectedRepositoryLocations() {
		ArrayList tags = new ArrayList();
		IStructuredSelection selection = getSelection();
		if (!selection.isEmpty()) {
			Iterator elements = selection.iterator();
			while (elements.hasNext()) {
				Object element = elements.next();
				Object adapter = getAdapter(element, ICVSRepositoryLocation.class);
				if (adapter != null) {
					tags.add(adapter);
				} else {
					adapter = getAdapter(element, BranchCategory.class);
					if(adapter != null) {
						tags.add(((BranchCategory)adapter).getRepository(adapter));
					}
				}
			}
		}
		return (ICVSRepositoryLocation[])tags.toArray(new ICVSRepositoryLocation[tags.size()]);
	}

}
