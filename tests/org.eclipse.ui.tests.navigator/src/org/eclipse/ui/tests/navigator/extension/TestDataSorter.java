package org.eclipse.ui.tests.navigator.extension;

import java.text.Collator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class TestDataSorter extends ViewerSorter {

	public TestDataSorter() {
		super(); 
	}

	public TestDataSorter(Collator collator) {
		super(collator); 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
	
		if(e1 instanceof TestExtensionTreeData) {
			if(e2 instanceof TestExtensionTreeData) {
				TestExtensionTreeData lvalue = (TestExtensionTreeData) e1;
				TestExtensionTreeData rvalue = (TestExtensionTreeData) e2;
				
				return lvalue.getName().compareTo(rvalue.getName());
			}
			return -1;
		} else if(e2 instanceof TestExtensionTreeData) {
			return +1;
		}
		
		return super.compare(viewer, e1, e2);
	}

}
