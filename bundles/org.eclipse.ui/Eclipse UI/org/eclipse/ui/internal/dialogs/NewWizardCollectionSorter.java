package org.eclipse.ui.internal.dialogs;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.registry.NewWizardsRegistryReader;
import org.eclipse.jface.viewers.*;

/**
 *	A Viewer element sorter that sorts Elements by their name attribute.
 *	Note that capitalization differences are not considered by this
 *	sorter, so a < B < c.
 *
 *	NOTE one exception to the above: an element with the system's reserved
 *	name for base Wizards will always be sorted such that it will
 *	ultimately be placed at the beginning of the sorted result.
 */
class NewWizardCollectionSorter extends ViewerSorter {
	public final static NewWizardCollectionSorter INSTANCE = new NewWizardCollectionSorter();
/**
 * Creates an instance of <code>NewWizardCollectionSorter</code>.  Since this
 * is a stateless sorter, it is only accessible as a singleton; the private
 * visibility of this constructor ensures this.
 */
private NewWizardCollectionSorter() {
	super();
}
/**
 * The 'compare' method of the sort operation.
 *
 * @return  the value <code>0</code> if the argument o1 is equal to o2;
 * 			a value less than <code>0</code> if o1 is less than o2;
 *			and a value greater than <code>0</code> if o1 is greater than o2.
 */
public int compare(Viewer viewer,Object o1,Object o2) {
	String name1 = ((WizardCollectionElement)o1).getLabel(o1);
	String name2 = ((WizardCollectionElement)o2).getLabel(o2);
	if (name1.equals(name2))
		return 0;

	// note that this must be checked for name2 before name1 because if they're
	// BOTH equal to BASE_CATEGORY then we want to answer false by convention
	if (name2.equalsIgnoreCase(NewWizardsRegistryReader.BASE_CATEGORY))
		return 1;
		
	if (name1.equalsIgnoreCase(NewWizardsRegistryReader.BASE_CATEGORY))
		return -1;
		
	return name1.compareTo(name2);
}
/**
 *	Return true if this sorter is affected by a property 
 *	change of propertyName on the specified element.
 */
public boolean isSorterProperty(Object object, String propertyId) {
	return propertyId.equals(IBasicPropertyConstants.P_TEXT);
}
}
