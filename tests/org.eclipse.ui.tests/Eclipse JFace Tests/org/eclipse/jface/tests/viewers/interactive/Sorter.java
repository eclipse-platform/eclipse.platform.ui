package org.eclipse.jface.tests.viewers.interactive;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ViewerSorter;

public class Sorter extends ViewerSorter {

	public boolean isSorterProperty(Object element, String property) {
		return IBasicPropertyConstants.P_TEXT.equals(property);
	}
}
