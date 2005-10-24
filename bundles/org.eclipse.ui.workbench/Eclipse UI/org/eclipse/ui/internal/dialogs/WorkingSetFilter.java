/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.dialogs;

import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.IWorkingSet;

public class WorkingSetFilter extends ViewerFilter {
		Set workingSetIds;
		
		public WorkingSetFilter(Set workingSetIds) {
			this.workingSetIds = workingSetIds;
		}
		
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element instanceof IWorkingSet) {
            IWorkingSet workingSet = (IWorkingSet) element;
			String id = workingSet.getId();
            //if (!workingSet.isVisible())
            //	return false;
            if (workingSetIds != null && id != null) {
                return workingSetIds.contains(id);
            }
        }
        return true;
    }
}