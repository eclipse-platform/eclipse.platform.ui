/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.dialogs;

import java.text.Collator;

import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 *	A Viewer element sorter that sorts Elements by their name attribute.
 *	Note that capitalization differences are not considered by this
 *	sorter, so a < B < c.
 *
 *	NOTE exceptions to the above: an element with the system's reserved
 *	category for Other Wizards will always be sorted such that it will
 *	ultimately be placed at the end of the sorted result, and an elemen 
 *  with the reserved category name for General wizards will always be 
 *  placed at the beginning of the sorted result.
 *  
 *  @since 3.2
 */
class DataTransferWizardCollectionSorter extends ViewerSorter {
    public final static DataTransferWizardCollectionSorter INSTANCE = new DataTransferWizardCollectionSorter();
    
    private final static String CATEGORY_GENERAL = WorkbenchMessages.WizardsGeneralCategory_label;
    
    private Collator collator = Collator.getInstance();

    /**
     * Creates an instance of <code>DataTransferWizardCollectionSorter</code>.  Since this
     * is a stateless sorter, it is only accessible as a singleton; the private
     * visibility of this constructor ensures this.
     */
    private DataTransferWizardCollectionSorter() {
        super();
    }

    /**
     * The 'compare' method of the sort operation.
     *
     * @return  the value <code>0</code> if the argument o1 is equal to o2;
     * 			a value less than <code>0</code> if o1 is less than o2;
     *			and a value greater than <code>0</code> if o1 is greater than o2.
     */
    public int compare(Viewer viewer, Object o1, Object o2) {
        String name1 = ((WorkbenchAdapter) o1).getLabel(o1);
        String name2 = ((WorkbenchAdapter) o2).getLabel(o2);
        if (name1.equals(name2))
            return 0;

        // Be sure that the Other category is at the end of the wizard categories
        if (name2
                .equalsIgnoreCase(WizardsRegistryReader.UNCATEGORIZED_WIZARD_CATEGORY_LABEL))
            return -1;

        if (name1
                .equalsIgnoreCase(WizardsRegistryReader.UNCATEGORIZED_WIZARD_CATEGORY_LABEL))
            return 1;

        // note that this must be checked for name2 before name1 because if they're
        // BOTH equal to GENERAL_CATEGORY then we want to answer false by convention
        if (name2.equalsIgnoreCase(CATEGORY_GENERAL))
            return 1;

        if (name1.equalsIgnoreCase(CATEGORY_GENERAL))
            return -1;

        return collator.compare(name1, name2);
    }

    /**
     *	Return true if this sorter is affected by a property 
     *	change of propertyName on the specified element.
     */
    public boolean isSorterProperty(Object object, String propertyId) {
        return propertyId.equals(IBasicPropertyConstants.P_TEXT);
    }
}
