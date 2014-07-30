/*******************************************************************************
 * Copyright (c) 2009, 2013 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     IBM Corporation - clean-up
 *******************************************************************************/
package org.eclipse.debug.tests.viewer.model;

import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.internal.ui.viewers.model.IInternalTreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewerFilter;
import org.eclipse.debug.tests.viewer.model.TestModel.TestElement;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Tests that verify that the viewer property retrieves all the content 
 * from the model.
 * 
 * @since 3.8
 */
abstract public class FilterTests extends TestCase implements ITestModelUpdatesListenerConstants {
    
    Display fDisplay;
    Shell fShell;
    ITreeModelViewer fViewer;
    TestModelUpdatesListener fListener;
    
    public FilterTests(String name) {
        super(name);
    }

    /**
     * @throws java.lang.Exception
     */
    @Override
	protected void setUp() throws Exception {
        fDisplay = PlatformUI.getWorkbench().getDisplay();
        fShell = new Shell(fDisplay);
        fShell.setMaximized(true);
        fShell.setLayout(new FillLayout());

        fViewer = createViewer(fDisplay, fShell);
        
        fListener = new TestModelUpdatesListener(fViewer, true, true);

        fShell.open ();
    }

    abstract protected IInternalTreeModelViewer createViewer(Display display, Shell shell);
    
    /**
     * @throws java.lang.Exception
     */
    @Override
	protected void tearDown() throws Exception {
        fListener.dispose();
        fViewer.getPresentationContext().dispose();
        
        // Close the shell and exit.
        fShell.close();
        while (!fShell.isDisposed()) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}
    }

    @Override
	protected void runTest() throws Throwable {
        try {
            super.runTest();
        } catch (Throwable t) {
			throw new ExecutionException("Test failed: " + t.getMessage() + "\n fListener = " + fListener.toString(), t); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    protected IInternalTreeModelViewer getInternalViewer() {
        return (IInternalTreeModelViewer)fViewer;
    }
    

    class TestViewerFilter extends ViewerFilter {
    	
    	Pattern fPattern;
    	TestViewerFilter(String pattern) {
    		fPattern = Pattern.compile(pattern);
    	}
    	
    	
    	 @Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
    		 if (element instanceof TestElement) {
    			 TestElement te = (TestElement)element;
    			 return !fPattern.matcher(te.getLabel()).find();
    		 }
    		
    		return true;
    	}
    }

    class TestTMVFilter extends TreeModelViewerFilter {
        Pattern fPattern;
        Object fParentElement;
        TestTMVFilter(String pattern, Object parentElement) {
            fPattern = Pattern.compile(pattern);
            fParentElement = parentElement;
        }
        
        @Override
		public boolean isApplicable(ITreeModelViewer viewer, Object parentElement) {
            if (fParentElement != null) {
                return fParentElement.equals(parentElement);
            }

            return true;
        }
        
         @Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
             if (element instanceof TestElement) {
                 TestElement te = (TestElement)element;
                 return !fPattern.matcher(te.getLabel()).find();
             }
            
            return true;
        }
    }
    
    public void testSimpleSingleLevel() throws InterruptedException {
        TestModel model = TestModel.simpleSingleLevel();
		doTestSimpleLevel(model, new ViewerFilter[] { new TestViewerFilter("2") }); //$NON-NLS-1$
    }
    
    public void testSimpleSingleLevelWithTMVFilter() throws InterruptedException {
        TestModel model = TestModel.simpleSingleLevel();
		doTestSimpleLevel(model, new ViewerFilter[] { new TestTMVFilter("2", model.getRootElement()) }); //$NON-NLS-1$
    }
    
    public void testSimpleSingleLevelWithMixedFilters() throws InterruptedException {
        TestModel model = TestModel.simpleSingleLevel();
		doTestSimpleLevel(model, new ViewerFilter[] { new TestTMVFilter("2", model.getRootElement()), new TestViewerFilter("1") }); //$NON-NLS-1$ //$NON-NLS-2$
    }    

    public void testSimpleMultiLevel() throws InterruptedException {
        TestModel model = TestModel.simpleMultiLevel();
		doTestSimpleLevel(model, new ViewerFilter[] { new TestViewerFilter(".1"), new TestViewerFilter(".2") }); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public void testSimpleMultiLevelWithTMVFilter() throws InterruptedException {
        TestModel model = TestModel.simpleMultiLevel();
		doTestSimpleLevel(model, new ViewerFilter[] { new TestTMVFilter(".1", null), new TestTMVFilter(".2", null) }); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void testSimpleMultiLevelWithMixedFilters() throws InterruptedException {
        TestModel model = TestModel.simpleMultiLevel();
		doTestSimpleLevel(model, new ViewerFilter[] { new TestViewerFilter(".1"), new TestTMVFilter(".2", null) }); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void doTestSimpleLevel(TestModel model, ViewerFilter[] filters) throws InterruptedException {
        
        // Make sure that all elements are expanded
        fViewer.setAutoExpandLevel(-1);
        
        fViewer.setFilters(filters);
        
        // Create the listener which determines when the view is finished updating.
        // fListener.reset(TreePath.EMPTY, model.getRootElement(), filters, -1, false, false);
        fListener.reset(TreePath.EMPTY, model.getRootElement(), filters, -1, true, true);
        
        // Set the viewer input (and trigger updates).
        fViewer.setInput(model.getRootElement());
        
        // Wait for the updates to complete.
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}
        
        model.validateData(fViewer, TreePath.EMPTY, false, filters);
    }
    
    public void testLargeSingleLevel() throws InterruptedException {
		doTestLargeSingleLevel(new ViewerFilter[] { new TestViewerFilter("2") }); //$NON-NLS-1$
    }

    public void testLargeSingleLevelWithTMVFilter() throws InterruptedException {
		doTestLargeSingleLevel(new ViewerFilter[] { new TestTMVFilter("2", null) }); //$NON-NLS-1$
    }
    
    private void doTestLargeSingleLevel(ViewerFilter[] filters) throws InterruptedException {
        TestModel model = new TestModel();
		model.setRoot(new TestElement(model, "root", new TestElement[0])); //$NON-NLS-1$
		model.setElementChildren(TreePath.EMPTY, TestModel.makeSingleLevelModelElements(model, 3000, "model.")); //$NON-NLS-1$

        // Set filters
        fViewer.setFilters(filters);

        fListener.setFailOnRedundantUpdates(false);
        //fListener.setFailOnMultipleLabelUpdateSequences(false);
        fListener.reset();
        
        fViewer.setInput(model.getRootElement());
        
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}
    }
    
    
    /**
     * Replace an element that is not visible but filtered out.  With an element that is NOT filtered out.
     * Fire REPLACE delta.
     */
    public void testReplacedUnrealizedFilteredElement() throws InterruptedException {
		doTestReplacedUnrealizedFilteredElement(new ViewerFilter[] { new TestViewerFilter("2") }); //$NON-NLS-1$
    }
    

    /**
     * Replace an element that is not visible but filtered out.  With an element that is NOT filtered out.
     * Fire REPLACE delta.
     */
    public void testReplacedUnrealizedFilteredElementWithTMVFilter() throws InterruptedException {
		doTestReplacedUnrealizedFilteredElement(new ViewerFilter[] { new TestTMVFilter("2", null) }); //$NON-NLS-1$
    }
    
    private void doTestReplacedUnrealizedFilteredElement(ViewerFilter[] filters) throws InterruptedException {
        
        // Populate a view with a large model (only first 100 elements will be visible in virtual viewer).
        TestModel model = new TestModel();
		model.setRoot(new TestElement(model, "root", new TestElement[0])); //$NON-NLS-1$
		model.setElementChildren(TreePath.EMPTY, TestModel.makeSingleLevelModelElements(model, 300, "model.")); //$NON-NLS-1$

        fViewer.setFilters(filters);

        fListener.setFailOnRedundantUpdates(false);
        fListener.reset();

        // Populate the view (all elements containing a "2" will be filtered out.
        fViewer.setInput(model.getRootElement());
        
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}
        
        // Switch out element "201" which is filtered out, with a "replaced element" which should NOT be 
        // filtered out.
		TestElement replacedElement = new TestElement(model, "replaced element", new TestElement[0]); //$NON-NLS-1$
        IModelDelta replaceDelta = model.replaceElementChild(TreePath.EMPTY, 200, replacedElement);
        fListener.reset();
        model.postDelta(replaceDelta);
        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}

        // Reposition the viewer to make element 100 the top element, making the replaced element visible.
        fListener.reset();        
        ((IInternalTreeModelViewer) fViewer).reveal(TreePath.EMPTY, 150);
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}

        // Verify that the replaced element is in viewer now (i.e. it's not filtered out.
        TreePath[] replacedElementPaths = fViewer.getElementPaths(replacedElement);
        assertTrue(replacedElementPaths.length != 0);
    }


    public void testRefreshUnrealizedFilteredElement() throws InterruptedException {
		doTestRefreshUnrealizedFilteredElement(new ViewerFilter[] { new TestViewerFilter("2") }); //$NON-NLS-1$
    }

    public void testRefreshUnrealizedFilteredElementWithTMVFilter() throws InterruptedException {
		doTestRefreshUnrealizedFilteredElement(new ViewerFilter[] { new TestTMVFilter("2", null) }); //$NON-NLS-1$
    }

    /**
     * Replace an element that is not visible but filtered out.  With an element that is NOT filtered out.
     * Fire CONTENT delta on parent.
     */
    private void doTestRefreshUnrealizedFilteredElement(ViewerFilter[] filters) throws InterruptedException {
        // Populate a view with a large model (only first 100 elements will be visible in virtual viewer).
        TestModel model = new TestModel();
		model.setRoot(new TestElement(model, "root", new TestElement[0])); //$NON-NLS-1$
		model.setElementChildren(TreePath.EMPTY, TestModel.makeSingleLevelModelElements(model, 300, "model.")); //$NON-NLS-1$

        fViewer.setFilters(filters);

        fListener.setFailOnRedundantUpdates(false);
        fListener.reset();

        // Populate the view (all elements containing a "2" will be filtered out.
        fViewer.setInput(model.getRootElement());
        
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}
        
        // Switch out element "201" which is filtered out, with a "replaced element" which should NOT be 
        // filtered out.
		TestElement replacedElement = new TestElement(model, "replaced element", new TestElement[0]); //$NON-NLS-1$
        model.replaceElementChild(TreePath.EMPTY, 200, replacedElement);
        fListener.reset();
        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}

        // Reposition the viewer to make element 100 the top element, making the replaced element visible.
        fListener.reset();        
        ((IInternalTreeModelViewer) fViewer).reveal(TreePath.EMPTY, 150);
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}

        // Verify that the replaced element is in viewer now (i.e. it's not filtered out.
        TreePath[] replacedElementPaths = fViewer.getElementPaths(replacedElement);
        assertTrue(replacedElementPaths.length != 0);
    }    

    public void testRefreshToUnfilterElements() throws InterruptedException {
		doTestRefreshToUnfilterElements(new ViewerFilter[] { new TestViewerFilter(".1"), new TestViewerFilter(".2") }); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public void testRefreshToUnfilterElementsWithTMVFilter() throws InterruptedException {
		doTestRefreshToUnfilterElements(new ViewerFilter[] { new TestTMVFilter(".1", null), new TestTMVFilter(".2", null) }); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void testRefreshToUnfilterElementsWithMixedFilters() throws InterruptedException {
		doTestRefreshToUnfilterElements(new ViewerFilter[] { new TestViewerFilter(".1"), new TestTMVFilter(".2", null) }); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /**
     * Replace an element that is not visible but filtered out.  With an element that is NOT filtered out.
     * Fire CONTENT delta on parent.
     */
    private void doTestRefreshToUnfilterElements(ViewerFilter[] filters) throws InterruptedException {
        // Populate a view with a large model (only first 100 elements will be visible in virtual viewer).
        TestModel model = TestModel.simpleMultiLevel();

        fViewer.setFilters(filters);

        fListener.setFailOnRedundantUpdates(false);
        fListener.reset();

        // Make sure that all elements are expanded
        fViewer.setAutoExpandLevel(-1);
        
        // Populate the view (all elements containing a "2" will be filtered out.
        fViewer.setInput(model.getRootElement());
        
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}

        // Turn off filters and refresh.
        filters = new ViewerFilter[0];
        fViewer.setFilters(filters);
        fListener.reset();
        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}

        model.validateData(fViewer, TreePath.EMPTY, false, filters);
    }    

    public void testPreserveExpandedOnMultLevelContent() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        TestModel model = StateTests.alternatingSubsreesModel(6);

        // NOTE: WE ARE NOT EXPANDING ANY CHILDREN
        
        // Create the listener, only check the first level
        fListener.reset(TreePath.EMPTY, model.getRootElement(), 1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}
        model.validateData(fViewer, TreePath.EMPTY, true);

        StateTests.expandAlternateElements(fListener, model, true);
        
        // Set a selection in view
        // Set a selection in view
        TreeSelection originalSelection = new TreeSelection(
new TreePath[] { model.findElement("5"), model.findElement("5.1"), model.findElement("6") }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        fViewer.setSelection(originalSelection);
        assertTrue( StateTests.areTreeSelectionsEqual(originalSelection, (ITreeSelection)fViewer.getSelection()) );

        // Set a filter to remove element "1"
		ViewerFilter[] filters = new ViewerFilter[] { new TestViewerFilter("^1$") }; //$NON-NLS-1$
        fViewer.setFilters(filters);
        
        // Note: Re-expanding nodes causes redundant updates.
        fListener.reset(false, false);
        fListener.addUpdates(getInternalViewer(), TreePath.EMPTY, model.getRootElement(), filters, -1, ALL_UPDATES_COMPLETE);
        
        // Post the refresh delta
        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE | STATE_RESTORE_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}

        // Validate data
        model.validateData(fViewer, TreePath.EMPTY, true, filters);
		assertTrue(getInternalViewer().getExpandedState(model.findElement("2")) == false); //$NON-NLS-1$
		assertTrue(getInternalViewer().getExpandedState(model.findElement("3")) == true); //$NON-NLS-1$
		assertTrue(getInternalViewer().getExpandedState(model.findElement("3.1")) == true); //$NON-NLS-1$
		assertTrue(getInternalViewer().getExpandedState(model.findElement("4")) == false); //$NON-NLS-1$
		assertTrue(getInternalViewer().getExpandedState(model.findElement("5")) == true); //$NON-NLS-1$
		assertTrue(getInternalViewer().getExpandedState(model.findElement("5.1")) == true); //$NON-NLS-1$
		assertTrue(getInternalViewer().getExpandedState(model.findElement("6")) == false); //$NON-NLS-1$
        assertTrue( StateTests.areTreeSelectionsEqual(originalSelection, (ITreeSelection)fViewer.getSelection()) );
        
        // Note: in past it was observed sub-optimal coalescing in this test due 
        // to scattered update requests from viewer.
        assertTrue( fListener.checkCoalesced(TreePath.EMPTY, 0, 6) );
        
        // Clear the filter, to re-add the element
        filters = new ViewerFilter[0];
        fViewer.setFilters(filters);

        // Refresh again to get the filtered element back
        fListener.reset();
        fListener.addUpdates(getInternalViewer(), TreePath.EMPTY, model.getRootElement(), filters, -1, ALL_UPDATES_COMPLETE);
        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE | STATE_RESTORE_COMPLETE)) {
			if (!fDisplay.readAndDispatch ()) {
				Thread.sleep(0);
			}
		}

        // Validate data
        model.validateData(fViewer, TreePath.EMPTY, true, filters);
		assertTrue(getInternalViewer().getExpandedState(model.findElement("2")) == false); //$NON-NLS-1$
		assertTrue(getInternalViewer().getExpandedState(model.findElement("3")) == true); //$NON-NLS-1$
		assertTrue(getInternalViewer().getExpandedState(model.findElement("3.1")) == true); //$NON-NLS-1$
		assertTrue(getInternalViewer().getExpandedState(model.findElement("4")) == false); //$NON-NLS-1$
		assertTrue(getInternalViewer().getExpandedState(model.findElement("5")) == true); //$NON-NLS-1$
		assertTrue(getInternalViewer().getExpandedState(model.findElement("5.1")) == true); //$NON-NLS-1$
		assertTrue(getInternalViewer().getExpandedState(model.findElement("6")) == false); //$NON-NLS-1$
        assertTrue( StateTests.areTreeSelectionsEqual(originalSelection, (ITreeSelection)fViewer.getSelection()) );
        
    }
    
}
