package org.eclipse.jface.tests.performance;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class RefreshTestContentProvider implements IStructuredContentProvider {

	static TestElement[] allElements;
	public static int ELEMENT_COUNT = 10000;
	TestElement[] currentElements;
	
	static{
		allElements = new TestElement[ELEMENT_COUNT];
		for (int i = 0; i < ELEMENT_COUNT; i++) {
			allElements[i] = new TestElement(i);			
		}
	}
	
	public RefreshTestContentProvider(int size){
		Assert.isTrue(size <= ELEMENT_COUNT);
		setElements(size);
	}
	
	/**
	 * Set the size of the amount we are currently displaying 
	 * to size.
	 * @param size
	 */
	private void setElements(int size) {
		
		currentElements = new TestElement[size];
		for (int i = 0; i < currentElements.length; i++) {
			currentElements[i] = allElements[i];
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return currentElements;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		currentElements = null;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		//Do nothing here
	}

}
