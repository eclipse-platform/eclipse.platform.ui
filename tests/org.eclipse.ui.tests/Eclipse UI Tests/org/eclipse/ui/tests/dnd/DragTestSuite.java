/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dnd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.tests.TestPlugin;

/**
 * @since 3.0
 */
public class DragTestSuite extends TestSuite {

    /**
     * Returns the suite. This is required to use the JUnit Launcher.
     */
    public static Test suite() {
        return new DragTestSuite();
    }

    public DragTestSuite() {
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
        AbstractTestDropTarget[] viewDropTargets = new AbstractTestDropTarget[] {

        // Editor area
                new EditorAreaDropTarget(SWT.LEFT),
                new EditorAreaDropTarget(SWT.RIGHT),
                new EditorAreaDropTarget(SWT.TOP),
                new EditorAreaDropTarget(SWT.BOTTOM),

                // Resource navigator (a view that isn't in a stack)
                new ViewDropTarget(resNav, SWT.LEFT),
                new ViewDropTarget(resNav, SWT.RIGHT),
                new ViewDropTarget(resNav, SWT.BOTTOM),
                new ViewDropTarget(resNav, SWT.CENTER),
                new ViewDropTarget(resNav, SWT.TOP),

                // Problems view (a view that is in a stack)
                // Omit the top from this test, since the meaning of dropping on the top border of 
                // a stack may change in the near future
                new ViewDropTarget(probView, SWT.LEFT),
                new ViewDropTarget(probView, SWT.RIGHT),
                new ViewDropTarget(probView, SWT.BOTTOM),
                new ViewDropTarget(probView, SWT.CENTER),

                // Fast view bar
                new FastViewBarDropTarget(),

                // View tabs
                new ViewTabDropTarget(resNav), new ViewTabDropTarget(probView), 
                
        		// Detached windows 
                new DetachedDropTarget()};

        // Drop targets that will only be tested for editors
        AbstractTestDropTarget[] editorDropTargets = new AbstractTestDropTarget[] {
        // A view
                new ViewDropTarget(resNav, SWT.CENTER),

                // A stand-alone editor
                new EditorDropTarget(2, SWT.LEFT),
                new EditorDropTarget(2, SWT.RIGHT),
                new EditorDropTarget(2, SWT.TOP),
                new EditorDropTarget(2, SWT.BOTTOM),
                new EditorDropTarget(2, SWT.CENTER),

                // Editors (a stack of editors)
                new EditorDropTarget(0, SWT.LEFT),
                new EditorDropTarget(0, SWT.RIGHT),
                new EditorDropTarget(0, SWT.BOTTOM),
                new EditorDropTarget(0, SWT.CENTER) };

        // Drop targets that will be tested for both views and editors
        AbstractTestDropTarget[] commonDropTargets = new AbstractTestDropTarget[] {
        // Test dragging to the edges of the workbench window
                new WindowDropTarget(SWT.TOP),
                new WindowDropTarget(SWT.BOTTOM),
                new WindowDropTarget(SWT.LEFT), new WindowDropTarget(SWT.RIGHT) };

        // Drag sources for maximized views
        TestDragSource[] maximizedViewDragSources = new TestDragSource[] {
                new ViewDragSource(resNav, false, true),
                new ViewDragSource(resNav, true, true),
                new ViewDragSource(probView, false, true),
                new ViewDragSource(probView, true, true) };

        // Drop targets that will only be tested for maximized views (we only need to ensure
        // that the view will become un-maximized -- the regular view test cases will excercise
        // the remainder of the view dragging code). We need to drag each kind of maximized view
        // to something that couldn't be seen while the view is maximized -- like the editor area).
        AbstractTestDropTarget[] maximizedViewDropTargets = new AbstractTestDropTarget[] { new EditorAreaDropTarget(
                SWT.RIGHT) };

        Map expectedResults = loadExpectedResults();

        // Now generate all test cases
        addAllCombinations(maximizedViewDragSources, maximizedViewDropTargets,
                expectedResults);
        addAllCombinations(viewDragSources, viewDropTargets, expectedResults);
        addAllCombinations(viewDragSources, commonDropTargets, expectedResults);
        addAllCombinations(editorDragSources, editorDropTargets,
                expectedResults);
        addAllCombinations(editorDragSources, commonDropTargets,
                expectedResults);
    }

    private void addAllCombinations(TestDragSource[] dragSources,
            AbstractTestDropTarget[] dropTargets, Map expectedResults) {

        // Now, attempt to drag every combination of drag sources to drop targets.
        for (int srcId = 0; srcId < dragSources.length; srcId++) {
            for (int destId = 0; destId < dropTargets.length; destId++) {
                TestDragSource src = dragSources[srcId];
                AbstractTestDropTarget target = dropTargets[destId];

                DragTest newTest = new DragTest(src, target);
                newTest.setExpectedResult((String) expectedResults.get(newTest
                        .getName()));
                addTest(newTest);
            }
        }
    }

    public Map generateExpectedResults(IProgressMonitor mon) throws Exception {
        int count = testCount();
        mon.beginTask("Running tests", count);
        HashMap result = new HashMap(count * 3);

        for (int idx = 0; idx < count; idx++) {
            DragTest next = (DragTest) testAt(idx);
            String name = next.getName();

            mon.subTask(name);
            String testResult = next.performTest();

            result.put(name, testResult);
            mon.worked(1);

            if (mon.isCanceled()) {
                return result;
            }
        }

        mon.done();

        return result;
    }

    public static void saveResults(String filename, Map toWrite)
            throws IOException {
        //toSave.createNewFile();

        FileOutputStream output;
        output = new FileOutputStream(filename);

        OutputStreamWriter writer = new OutputStreamWriter(output);

        XMLMemento memento = XMLMemento.createWriteRoot("dragtests");

        Iterator iter = toWrite.keySet().iterator();
        while (iter.hasNext()) {
            String next = (String) iter.next();

            IMemento child = memento.createChild("test");
            child.putString("name", next);
            child.putString("result", (String) toWrite.get(next));
        }

        memento.save(writer);
        output.close();
    }

    /**
     * Loads and returns the set of expected test results from the file data/dragtests.xml
     * Returns a map where the test names are keys and the resulting layout are values.
     * 
     * @return the set of expected results from the drag tests
     * @throws Exception
     */
    public Map loadExpectedResults() {
        TestPlugin plugin = TestPlugin.getDefault();
        HashMap result = new HashMap();

        URL fullPathString = plugin.getDescriptor().find(
                new Path("data/dragtests.xml"));

        if (fullPathString != null) {

            IPath path = new Path(fullPathString.getPath());

            File theFile = path.toFile();

            FileInputStream input;
            try {
                input = new FileInputStream(theFile);
            } catch (FileNotFoundException e) {
                return result;
            }

            InputStreamReader reader = new InputStreamReader(input);

            XMLMemento memento;
            try {
                memento = XMLMemento.createReadRoot(reader);
                IMemento[] children = memento.getChildren("test");

                for (int idx = 0; idx < children.length; idx++) {
                    IMemento child = children[idx];

                    String name = child.getString("name");
                    String testResult = child.getString("result");

                    result.put(name, testResult);
                }
            } catch (WorkbenchException e1) {
                return result;
            }

        }

        return result;
    }
}