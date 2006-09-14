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
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.ui.internal.registry.Category;
import org.eclipse.ui.internal.registry.ViewRegistry;
import org.eclipse.ui.internal.registry.ViewRegistryReader;
import org.eclipse.ui.views.IViewCategory;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * This is used to sort views in a ShowViewDialog.
 */
public class ViewComparator extends ViewerComparator {
    private ViewRegistry viewReg;

    /**
     * ViewSorter constructor comment.
     * 
     * @param reg an IViewRegistry
     */
    public ViewComparator(ViewRegistry reg) {
        super();
        viewReg = reg;
    }

    /**
     * Returns a negative, zero, or positive number depending on whether
     * the first element is less than, equal to, or greater than
     * the second element.
     */
    public int compare(Viewer viewer, Object e1, Object e2) {
        if (e1 instanceof IViewDescriptor) {
            String str1 = DialogUtil.removeAccel(((IViewDescriptor) e1)
                    .getLabel());
            String str2 = DialogUtil.removeAccel(((IViewDescriptor) e2)
                    .getLabel());
            return getComparator().compare(str1, str2);
        } else if (e1 instanceof IViewCategory) {
        	IViewCategory generalCategory = viewReg.findCategory(ViewRegistryReader.GENERAL_VIEW_ID);
        	if (generalCategory != null){
        		if (((IViewCategory)e1).getId().equals(generalCategory.getId())) {
					return -1;
				}
        		if (((IViewCategory)e2).getId().equals(generalCategory.getId())) {
					return 1;
				}
        	}
			Category miscCategory = viewReg.getMiscCategory();
			if(miscCategory != null){
				if (((IViewCategory)e1).getId().equals(miscCategory.getId())) {
					return 1;
				}
				if (((IViewCategory)e2).getId().equals(miscCategory.getId())) {
					return -1;
				}
			}
            String str1 = DialogUtil.removeAccel(((IViewCategory) e1).getLabel());
            String str2 = DialogUtil.removeAccel(((IViewCategory) e2).getLabel());
            return getComparator().compare(str1, str2);
        }
        return 0;
    }
}
