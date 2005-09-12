/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dnd;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.internal.dnd.TestDropLocation;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.autotests.AutoTestSuite;

/**
 * @since 3.0
 */
public class DragTestSuite extends AutoTestSuite {

    /**
     * Returns the suite. This is required to use the JUnit Launcher.
     */
    public static Test suite() {
        return new DragTestSuite();
    }

    public DragTestSuite() {
        super(Platform.find(TestPlugin.getDefault().getBundle(), new Path("data/dragtests.xml")));
        
        String resNav = IPageLayout.ID_RES_NAV;
        String probView = IPageLayout.ID_PROBLEM_VIEW;

        // Drag sources for views
        TestDragSource[] viewDragSources = new TestDragSource[] {
                new ViewDragSource(resNav, false),
                new ViewDragSource(resNav, true),
                new ViewDragSource(probView, false),
                new ViewDragSource(probView, true) };

        // Drag sources for editors
        TestDragSource[] editorDragSources = new TestDragSource[] {
                new EditorDragSource(0, false), new EditorDragSource(0, true),
                new EditorDragSource(2, false), new EditorDragSource(2, true) };

        // Drop targets that will only be tested for views

        // Drag sources for maximized views
        TestDragSource[] maximizedViewDragSources = new TestDragSource[] {
                new ViewDragSource(resNav, false, true),
                new ViewDragSource(resNav, true, true),
                new ViewDragSource(probView, false, true),
                new ViewDragSource(probView, true, true) };

        // Now generate all test cases
        for (int i = 0; i < maximizedViewDragSources.length; i++) {
            TestDragSource source = maximizedViewDragSources[i];
            
            addAllCombinations(source, getMaximizedViewDropTargets(source));
        }
        
        for (int i = 0; i < viewDragSources.length; i++) {
            TestDragSource source = viewDragSources[i];
            
            addAllCombinations(source, getViewDropTargets(source));
            addAllCombinations(source, getCommonDropTargets(source));
        }
      
        for (int i = 0; i < editorDragSources.length; i++) {
            TestDragSource source = editorDragSources[i];
            
            addAllCombinations(source, getEditorDropTargets(source));
            addAllCombinations(source, getCommonDropTargets(source));
        }
        
        addTest(new TestSuite(Bug87211Test.class));
    }

    /**
     * Returns drop targets that will only be tested for maximized views. (we only need to ensure
     * that the view will become un-maximized -- the regular view test cases will excercise
     * the remainder of the view dragging code). We need to drag each kind of maximized view
     * to something that couldn't be seen while the view is maximized -- like the editor area).
     * 
     * @param dragSource
     * @return
     * @since 3.1
     */
    private TestDropLocation[] getMaximizedViewDropTargets(IWorkbenchWindowProvider originatingWindow) {
        return new TestDropLocation[] { 
                new EditorAreaDropTarget(originatingWindow, SWT.RIGHT) };        
    }
    
    private TestDropLocation[] getCommonDropTargets(IWorkbenchWindowProvider dragSource) {
        
        String resNav = IPageLayout.ID_RES_NAV;
        String probView = IPageLayout.ID_PROBLEM_VIEW;
        
        return new TestDropLocation[] { 
            // Test dragging to the edges of the workbench window
            new WindowDropTarget(dragSource, SWT.TOP),
            new WindowDropTarget(dragSource, SWT.BOTTOM),
            new WindowDropTarget(dragSource, SWT.LEFT), 
            new WindowDropTarget(dragSource, SWT.RIGHT) };
    }
    
    /**
     * Return all drop targets that only apply to views, given the window being dragged from.
     * 
     * @param provider
     * @return
     * @since 3.1
     */
    private TestDropLocation[] getViewDropTargets(IWorkbenchWindowProvider dragSource) {
        
        String resNav = IPageLayout.ID_RES_NAV;
        String probView = IPageLayout.ID_PROBLEM_VIEW;
        
        return new TestDropLocation[] {
                // Editor area
                new EditorAreaDropTarget(dragSource, SWT.LEFT),
                new EditorAreaDropTarget(dragSource, SWT.RIGHT),
                new EditorAreaDropTarget(dragSource, SWT.TOP),
                new EditorAreaDropTarget(dragSource, SWT.BOTTOM),

                // Resource navigator (a view that isn't in a stack)
                new ViewDropTarget(dragSource, resNav, SWT.LEFT),
                new ViewDropTarget(dragSource, resNav, SWT.RIGHT),
                new ViewDropTarget(dragSource, resNav, SWT.BOTTOM),
                new ViewDropTarget(dragSource, resNav, SWT.CENTER),
                new ViewDropTarget(dragSource, resNav, SWT.TOP),

                // Problems view (a view that is in a stack)
                // Omit the top from this test, since the meaning of dropping on the top border of 
                // a stack may change in the near future
                new ViewDropTarget(dragSource, probView, SWT.LEFT),
                new ViewDropTarget(dragSource, probView, SWT.RIGHT),
                new ViewDropTarget(dragSource, probView, SWT.BOTTOM),
                new ViewDropTarget(dragSource, probView, SWT.CENTER),
                new ViewDropTarget(dragSource, probView, SWT.TOP),

                // Fast view bar
                new FastViewBarDropTarget(dragSource),

                // View tabs
                new ViewTabDropTarget(dragSource, resNav), 
                new ViewTabDropTarget(dragSource, probView), 
                
                // Detached windows 
                new DetachedDropTarget()};

    }

    private TestDropLocation[] getEditorDropTargets(IWorkbenchWindowProvider originatingWindow) {
        String resNav = IPageLayout.ID_RES_NAV;
        // Drop targets that will only be tested for editors
        return new TestDropLocation[] {
        // A view
                new ViewDropTarget(originatingWindow, resNav, SWT.CENTER),
    
                // A stand-alone editor
                new EditorDropTarget(originatingWindow, 2, SWT.LEFT),
                new EditorDropTarget(originatingWindow, 2, SWT.RIGHT),
                new EditorDropTarget(originatingWindow, 2, SWT.TOP),
                new EditorDropTarget(originatingWindow, 2, SWT.BOTTOM),
                new EditorDropTarget(originatingWindow, 2, SWT.CENTER),
    
                // Editors (a stack of editors)
                new EditorDropTarget(originatingWindow, 0, SWT.LEFT),
                new EditorDropTarget(originatingWindow, 0, SWT.RIGHT),
                new EditorDropTarget(originatingWindow, 0, SWT.BOTTOM),
                new EditorDropTarget(originatingWindow, 0, SWT.CENTER) };
    }
    
    private void addAllCombinations(TestDragSource dragSource,
            TestDropLocation[] dropTargets) {

        for (int destId = 0; destId < dropTargets.length; destId++) {
            DragTest newTest = new DragTest(dragSource, dropTargets[destId], getLog());
            addTest(newTest);
        }
    }

//    public Map generateExpectedResults(IProgressMonitor mon) throws Exception {
//        int count = testCount();
//        mon.beginTask("Running tests", count);
//        HashMap result = new HashMap(count * 3);
//
//        for (int idx = 0; idx < count; idx++) {
//            DragTest next = (DragTest) testAt(idx);
//            String name = next.getName();
//
//            mon.subTask(name);
//            String testResult = next.performTest();
//
//            result.put(name, testResult);
//            mon.worked(1);
//
//            if (mon.isCanceled()) {
//                return result;
//            }
//        }
//
//        mon.done();
//
//        return result;
//    }
//
//    public static void saveResults(String filename, Map toWrite)
//            throws IOException {
//        //toSave.createNewFile();
//
//        FileOutputStream output;
//        output = new FileOutputStream(filename);
//
//        OutputStreamWriter writer = new OutputStreamWriter(output);
//
//        XMLMemento memento = XMLMemento.createWriteRoot("dragtests");
//
//        Iterator iter = toWrite.keySet().iterator();
//        while (iter.hasNext()) {
//            String next = (String) iter.next();
//
//            IMemento child = memento.createChild("test");
//            child.putString("name", next);
//            child.putString("result", (String) toWrite.get(next));
//        }
//
//        memento.save(writer);
//        output.close();
//    }
//
//    /**
//     * Loads and returns the set of expected test results from the file data/dragtests.xml
//     * Returns a map where the test names are keys and the resulting layout are values.
//     * 
//     * @return the set of expected results from the drag tests
//     * @throws Exception
//     */
//    public Map loadExpectedResults() {
//        TestPlugin plugin = TestPlugin.getDefault();
//        HashMap result = new HashMap();
//
//        URL fullPathString = plugin.getDescriptor().find(
//                new Path("data/dragtests.xml"));
//
//        if (fullPathString != null) {
//
//            IPath path = new Path(fullPathString.getPath());
//
//            File theFile = path.toFile();
//
//            FileInputStream input;
//            try {
//                input = new FileInputStream(theFile);
//            } catch (FileNotFoundException e) {
//                return result;
//            }
//
//            InputStreamReader reader = new InputStreamReader(input);
//
//            XMLMemento memento;
//            try {
//                memento = XMLMemento.createReadRoot(reader);
//                IMemento[] children = memento.getChildren("test");
//
//                for (int idx = 0; idx < children.length; idx++) {
//                    IMemento child = children[idx];
//
//                    String name = child.getString("name");
//                    String testResult = child.getString("result");
//
//                    result.put(name, testResult);
//                }
//            } catch (WorkbenchException e1) {
//                return result;
//            }
//
//        }
//
//        return result;
//    }
}
