package org.eclipse.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.viewers.*;
import java.text.Collator;

/**
 * A viewer sorter that sorts elements with registered workbench adapters by their text property.
 * Note that capitalization differences are not considered by this
 * sorter, so a &gt; B &gt; c
 *
 * @see IWorkbenchAdapter
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
public boolean isSorterProperty(Object element,String propertyId) {
	return propertyId.equals(IBasicPropertyConstants.P_TEXT);
}
}
