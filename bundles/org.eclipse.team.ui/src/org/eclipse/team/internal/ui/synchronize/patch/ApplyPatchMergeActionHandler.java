/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize.patch;

import org.eclipse.compare.internal.patch.HunkDiffNode;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ui.mapping.ResourceMergeHandler;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

public class ApplyPatchMergeActionHandler extends ResourceMergeHandler {

	public ApplyPatchMergeActionHandler(
			ISynchronizePageConfiguration configuration, boolean overwrite) {
		super(configuration, overwrite);
	}

	public void updateEnablement(IStructuredSelection selection) {
		super.updateEnablement(selection);
		// disable merge for hunks
		Object[] elements = getOperation().getElements();
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] instanceof HunkDiffNode) {
				setEnabled(false);
				return;
			}
		}
	}

}
