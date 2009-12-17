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
package org.eclipse.ui.model;

import java.text.Collator; // can't use ICU, public API

import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * A viewer sorter that sorts elements with registered workbench adapters by their text property.
 * Note that capitalization differences are not considered by this
 * sorter, so a &gt; B &gt; c
 *
 * @see IWorkbenchAdapter
 * @deprecated as of 3.3, use {@link WorkbenchViewerComparator} instead
 */
public class WorkbenchViewerSorter extends ViewerSorter {

    /**
     * Creates a workbench viewer sorter using the default collator.
     */
    public WorkbenchViewerSorter() {
        super();
    }

    /**
     * Creates a workbench viewer sorter using the given collator.
     *
     * @param collator the collator to use to sort strings
     */
    public WorkbenchViewerSorter(Collator collator) {
        super(collator);
    }

    /* (non-Javadoc)
     * Method declared on ViewerSorter.
     */
    public boolean isSorterProperty(Object element, String propertyId) {
        return propertyId.equals(IBasicPropertyConstants.P_TEXT);
    }
}
