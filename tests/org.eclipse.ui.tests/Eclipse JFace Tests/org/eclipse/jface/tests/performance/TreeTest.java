package org.eclipse.jface.tests.performance;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.test.performance.Dimension;

public class TreeTest extends ViewerTest {

	TreeViewer viewer;

	static int TEST_COUNT = 1000;

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
	 * @throws CoreException
	 *             Test addition to the tree one element at a time.
	 */
	public void testAddOneAtATime() throws CoreException {
		openBrowser();

		for (int i = 0; i < ITERATIONS; i++) {
			TestTreeElement input = new TestTreeElement(0, null);
			viewer.setInput(input);
			input.createChildren(TEST_COUNT);
			processEvents();
			startMeasuring();
			for (int j = 0; j < input.children.length; j++) {

				viewer.add(input, input.children[j]);
				processEvents();

			}
			stopMeasuring();
		}

		commitMeasurements();
		assertPerformance();
	}

	/**
	 * @throws CoreException
	 *             Test addition to the tree one element at a time.
	 */
	public void testAddTen() throws CoreException {

		doTestAdd(10);
	}

	/**
	 * @throws CoreException
	 *             Test addition to the tree one element at a time.
	 */
	public void testAddFifty() throws CoreException {

		doTestAdd(50);
	}

	/**
	 * @throws CoreException
	 *             Test addition to the tree one element at a time.
	 */
	public void testAddHundred() throws CoreException {

		tagIfNecessary("Add 100 items to tree", Dimension.ELAPSED_PROCESS);
		
		doTestAdd(100);
	}

	private void doTestAdd(final int count) throws CoreException {

		openBrowser();
		for (int i = 0; i < ITERATIONS; i++) {
			TestTreeElement input = new TestTreeElement(0, null);
			viewer.setInput(input);
			input.createChildren(TEST_COUNT);
			Collection batches = new ArrayList();
			int blocks = input.children.length / count;
			for (int j = 0; j < blocks; j = j + count) {
				Object[] batch = new Object[count];
				System.arraycopy(input.children, j * count, batch, 0, count);
				batches.add(batch);
			}
			processEvents();
			Object[] batchArray = batches.toArray();
			startMeasuring();
			for (int j = 0; j < batchArray.length; j++) {

				viewer.add(input, (Object[]) batchArray[j]);
				processEvents();

			}
			stopMeasuring();
		}
		commitMeasurements();
		assertPerformance();

	}

	/**
	 * Test addition to the tree.
	 */
	public void testAddThousand() throws CoreException {
		openBrowser();
		for (int i = 0; i < ITERATIONS; i++) {
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

	/**
	 * @throws CoreException
	 *             Test addition to the tree with the items presorted.
	 */
	public void testAddThousandPreSort() throws CoreException {
		tagIfNecessary("Add 1000 items to end of tree", Dimension.ELAPSED_PROCESS);
		
		openBrowser();
		for (int i = 0; i < ITERATIONS; i++) {
			TestTreeElement input = new TestTreeElement(0, null);
			viewer.setInput(input);
			input.createChildren(TEST_COUNT);
			viewer.getSorter().sort(viewer, input.children);
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
