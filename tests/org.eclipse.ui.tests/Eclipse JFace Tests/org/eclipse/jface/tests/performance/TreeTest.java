package org.eclipse.jface.tests.performance;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Shell;

public class TreeTest extends ViewerTest {

	TreeViewer viewer;

	private int TEST_COUNT = 1000;

	public TreeTest(String testName, int tagging) {
		super(testName, tagging);
	}

	public TreeTest(String testName) {
		super(testName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.tests.performance.ViewerTest#createViewer(org.eclipse.swt.widgets.Shell)
	 */
	protected StructuredViewer createViewer(Shell shell) {
		viewer = new TreeViewer(browserShell);
		viewer.setContentProvider(getContentProvider());
		viewer.setLabelProvider(getLabelProvider());
		viewer.setSorter(new ViewerSorter());
		return viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.tests.performance.ViewerTest#getInitialInput()
	 */
	protected Object getInitialInput() {
		return new TestTreeElement(0, null);
	}

	/**
	 * Get a content provider for the tree viewer.
	 * 
	 * @return
	 */
	private IContentProvider getContentProvider() {
		return new ITreeContentProvider() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
			 */
			public Object[] getChildren(Object parentElement) {
				TestTreeElement element = (TestTreeElement) parentElement;
				return element.children;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
			 */
			public Object getParent(Object element) {
				return ((TestTreeElement) element).parent;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
			 */
			public boolean hasChildren(Object element) {
				return ((TestTreeElement) element).children.length > 0;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return new Object[] { inputElement };
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
			 */
			public void dispose() {
				// Do nothing here
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				// Do nothing here
			}

		};
	}

	/**
	 * Test addition to the menu.
	 * 
	 */
	public void testAdd() {
		openBrowser();
		for (int i = 0; i < 25; i++) {

			TestTreeElement input = new TestTreeElement(0, null);
			viewer.setInput(input);
			input.createChildren(TEST_COUNT);
			processEvents();
			startMeasuring();
			viewer.add(input, input.children);
			processEvents();
			stopMeasuring();
		}

		commitMeasurements();
		assertPerformance();
	}


}
