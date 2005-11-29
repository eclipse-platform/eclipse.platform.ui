/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ClosePerspectiveAction;
import org.eclipse.ui.internal.SaveableHelper;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.PerspectiveState;
import org.eclipse.ui.tests.util.CallHistory;
import org.eclipse.ui.tests.util.EmptyPerspective;
import org.eclipse.ui.tests.util.FileUtil;
import org.eclipse.ui.tests.util.UITestCase;

public class IWorkbenchPageTest extends UITestCase {

    private IWorkbenchPage fActivePage;

    private IWorkbenchWindow fWin;

    private IProject proj;

    public IWorkbenchPageTest(String testName) {
        super(testName);
    }

    protected void doSetUp() throws Exception {
        super.doSetUp();
        fWin = openTestWindow();
        fActivePage = fWin.getActivePage();
    }

    protected void doTearDown() throws Exception {
        super.doTearDown();
        if (proj != null) {
            FileUtil.deleteProject(proj);
            proj = null;
        }
    }
    
    /**
	 * Tests the new working set API.
	 * 
	 * @since 3.2
	 */
	public void testWorkingSets1() {
		IWorkbenchPage page = fActivePage;
		IWorkingSet[] sets = page.getWorkingSets();
		assertNotNull(sets);
		assertEquals(0, sets.length);

		IWorkingSetManager manager = page.getWorkbenchWindow().getWorkbench()
				.getWorkingSetManager();

		IWorkingSet set1 = null, set2 = null;
		try {
			set1 = manager.createWorkingSet("w1", new IAdaptable[0]);
			manager.addWorkingSet(set1);
			set2 = manager.createWorkingSet("w2", new IAdaptable[0]);
			manager.addWorkingSet(set2);
			
			page.setWorkingSets(new IWorkingSet[] {set1});
			sets = page.getWorkingSets();
			
			assertNotNull(sets);
			assertEquals(1, sets.length);
			assertEquals(set1, sets[0]);
			
			page.setWorkingSets(new IWorkingSet[0]);
			sets = page.getWorkingSets();
			assertNotNull(sets);
			assertEquals(0, sets.length);
			
			page.setWorkingSets(new IWorkingSet[] {set1, set2});
			sets = page.getWorkingSets();
			
			assertNotNull(sets);
			assertEquals(2, sets.length);
			Set realSet = new HashSet(Arrays.asList(sets));
			assertTrue(realSet.contains(set1));
			assertTrue(realSet.contains(set2));
			
			page.setWorkingSets(new IWorkingSet[0]);
			sets = page.getWorkingSets();
			assertNotNull(sets);
			assertEquals(0, sets.length);
		}
		finally {
			if (set1 != null) 
				manager.removeWorkingSet(set1);
			if (set2 != null)
				manager.removeWorkingSet(set2);
		}
	}
	
	 /**
	 * Tests the new working set API.
	 * 
	 * @since 3.2
	 */
	public void testWorkingSets2() {
		fActivePage.setWorkingSets(null);
		IWorkingSet [] sets = fActivePage.getWorkingSets();
		assertNotNull(sets);
		assertEquals(0, sets.length);
	}
	
	/**
	 * Tests the working set listeners.
	 * 
	 * @since 3.2
	 */
	public void testWorkingSets3() {
		IWorkingSetManager manager = fActivePage.getWorkbenchWindow().getWorkbench()
				.getWorkingSetManager();

		IWorkingSet set1 = null;
		final IWorkingSet [][] sets = new IWorkingSet[1][];
		sets[0] = new IWorkingSet[0];
		IPropertyChangeListener listener = new IPropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent event) {
				IWorkingSet [] oldSets = (IWorkingSet[]) event.getOldValue();
				assertTrue(Arrays.equals(sets[0], oldSets));
				sets[0] = (IWorkingSet[]) event.getNewValue();
			}};
		try {
			set1 = manager.createWorkingSet("w1", new IAdaptable[0]);
			manager.addWorkingSet(set1);

			fActivePage.addPropertyChangeListener(listener);
			
			fActivePage.setWorkingSets(new IWorkingSet[] { set1 });
			fActivePage.setWorkingSets(new IWorkingSet[] { });
			fActivePage.setWorkingSets(new IWorkingSet[] { set1 });
			
			sets[0] = fActivePage.getWorkingSets();

			assertNotNull(sets[0]);
			assertEquals(1, sets[0].length);
			assertEquals(set1, sets[0][0]);

		} finally {
			fActivePage.removePropertyChangeListener(listener);
			if (set1 != null)
				manager.removeWorkingSet(set1);
		}
	}
    
    /**
	 * Test the VIEW_VISIBLE parameter for showView, opening the view in the
	 * stack that does not contain the active view. Ensures that the created
	 * view is not the active part but is the top part in its stack.
	 */
    public void testView_VISIBLE2() {
        WorkbenchPage page = (WorkbenchPage) fActivePage;
        try {
            page.setPerspective(WorkbenchPlugin.getDefault()
                    .getPerspectiveRegistry().findPerspectiveWithId(
                            "org.eclipse.ui.tests.api.ViewPerspective"));
            
            //create a part to be active
            IViewPart activePart = page.showView(MockViewPart.ID3);

            IViewPart createdPart = page.showView(MockViewPart.ID2, null,
                    IWorkbenchPage.VIEW_VISIBLE);
            
            IViewPart[] stack = page.getViewStack(createdPart);
            assertEquals(2, stack.length);

            assertEquals(createdPart, stack[0]);
            assertEquals(page.findView(MockViewPart.ID), stack[1]);

            assertTrue(page.isPartVisible(createdPart));

            assertEquals(activePart, page.getActivePart());
        } catch (PartInitException e) {
            fail(e.getMessage());
        }
    }
    
    /**
     * openEditor(IEditorInput input, String editorId, boolean activate) 
     */
    public void testOpenEditor4() throws Throwable {
        proj = FileUtil.createProject("testOpenEditor");
        final String id = MockEditorPart.ID1;
        IEditorInput input = new FileEditorInput(FileUtil.createFile(
                "test.mock1", proj));
        MockPartListener listener = new MockPartListener();
        fActivePage.addPartListener(listener);
        CallHistory callTrace = listener.getCallHistory();

        /*
         javadoc: The editor type is determined by mapping editorId to an editor extension 
         registered with the workbench. 
         javadoc: If activate == true the editor will be activated
         */
        //open an editor with activation
        
        // Start with a view active in order to verify that the editor gets activated
        fActivePage.showView(IPageLayout.ID_PROBLEM_VIEW, null, IWorkbenchPage.VIEW_ACTIVATE);
        callTrace.clear();
        IEditorPart editor = IDE.openEditor(fActivePage, input, id, true);
        assertEquals(editor.getEditorInput(), input);
        assertEquals(editor.getSite().getId(), id);
        assertEquals(hasEditor(editor), true);
        assertEquals(fActivePage.getActiveEditor(), editor);
        assertEquals(callTrace.contains("partActivated"), true);

        //we need another editor so that the editor under test can receive events.
        //otherwise, events will be ignored.
        IEditorPart extra = IDE.openEditor(fActivePage, FileUtil.createFile(
                "aaaaa", proj));

        //close the first editor after the second has opened; necessary for
        //test to work with fix to PR 7743
        fActivePage.closeEditor(editor, false);

        // Start with a view active in order to verify that the editor does not get activated
        // (note: regardless of the activate=false flag, the editor would always be activated
        // if it is being opened in the same stack as the active editor. Making a view active
        // initially tests the function of the activate flag)
        fActivePage.showView(IPageLayout.ID_PROBLEM_VIEW, null, IWorkbenchPage.VIEW_ACTIVATE);
        callTrace.clear();
        //open an editor without activation
        editor = IDE.openEditor(fActivePage, input, id, false);
        
        assertEquals(editor.getEditorInput(), input);
        assertEquals(editor.getSite().getId(), id);
        assertEquals(hasEditor(editor), true);
        assertEquals(callTrace.contains("partActivated"), false);
        assertEquals(callTrace.contains("partBroughtToTop"), true);

        fActivePage.activate(extra);

        /*
         javadoc: If this page already has an editor open on the target object that editor is brought to the front
         */
        //open the editor under test second time without activation
        fActivePage.showView(IPageLayout.ID_PROBLEM_VIEW, null, IWorkbenchPage.VIEW_ACTIVATE);
        callTrace.clear();
        assertEquals(IDE.openEditor(fActivePage, input, id, false), editor);
        assertEquals(callTrace.contains("partBroughtToTop"), true);
        assertEquals(callTrace.contains("partActivated"), false);

        //activate the other editor
        fActivePage.activate(extra);

        /*
         javadoc: If activate == true the editor will be activated
         */
        //open the editor under test second time with activation
        fActivePage.showView(IPageLayout.ID_PROBLEM_VIEW, null, IWorkbenchPage.VIEW_ACTIVATE);
        callTrace.clear();
        assertEquals(IDE.openEditor(fActivePage, input, id, true), editor);
        assertEquals(callTrace.contains("partBroughtToTop"), true);
        assertEquals(callTrace.contains("partActivated"), true);
        
        /*
        javadoc: If activate == false but another editor in the same stack was active, the 
        new editor will be activated regardless.
        */
        // Close the old editor
        fActivePage.closeEditor(editor, false);
        // Ensure another editor in the stack is active
        fActivePage.activate(extra);
        callTrace.clear();
        // Verify that the editor is still activated
        IDE.openEditor(fActivePage, input, id, false);
        assertEquals(callTrace.contains("partBroughtToTop"), true);
        assertEquals(callTrace.contains("partActivated"), true);
    }
    
    /**
     *	tests both of the following:	
     *	setEditorAreaVisible()
     *	isEditorAreaVisible()
     */
    public void testGet_SetEditorAreaVisible() throws Throwable {
        fActivePage.setEditorAreaVisible(true);
        assertTrue(fActivePage.isEditorAreaVisible() == true);

        fActivePage.setEditorAreaVisible(false);
        assertTrue(fActivePage.isEditorAreaVisible() == false);
    }

    
    
    /**
     *  openEditor(IWorkbenchPage page, IFile input)
     */
    public void testOpenEditor() throws Throwable {
        proj = FileUtil.createProject("testOpenEditor");

        /*
         javadoc: 1. The workbench editor registry is consulted to determine if an editor extension has been
         registered for the file type. If so, an instance of the editor extension is opened on the file
         */
        IFile file = FileUtil.createFile("test.mock1", proj);
        IEditorPart editor = IDE.openEditor(fActivePage, file);

        boolean foundEditor = hasEditor(editor);
        assertEquals(foundEditor, true);
        assertEquals(fActivePage.getActiveEditor(), editor);
        assertEquals(editor.getSite().getId(), fWorkbench.getEditorRegistry()
                .getDefaultEditor(file.getName()).getId());

        /*
         javadoc: 2. Next, the native operating system will be consulted to determine if a native editor exists for 
         the file type. If so, a new process is started and the native editor is opened on the file.
         */
        //can not be tested
        /*
		 * javadoc: 3. If all else fails the file will be opened in a default
		 * text editor.
		 */

		file = FileUtil.createFile("a.null and void", proj);
		editor = IDE.openEditor(fActivePage, file);
		assertEquals(hasEditor(editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);
		assertEquals(editor.getSite().getId(),
				"org.eclipse.ui.DefaultTextEditor");

        // open another editor to take the focus away from the first editor
        IDE.openEditor(fActivePage, FileUtil.createFile("test.mock2", proj));

        /*  
         javadoc: If this page already has an editor open on the target object that editor is activated
         */
        //open the editor second time.      
        assertEquals(editor, IDE.openEditor(fActivePage, file));
        assertEquals(editor, fActivePage.getActiveEditor());
    }

    /**
     * openEditor(IWorkbenchPage page, IFile input, String editorID)
     */
    public void testOpenEditor2() throws Throwable {
        proj = FileUtil.createProject("testOpenEditor");
        final IFile file = FileUtil.createFile("asfasdasdf", proj);
        final String id = MockEditorPart.ID1;

        /*
         javadoc: The editor type is determined by mapping editorId to an editor extension registered with the workbench.
         */
        IEditorPart editor = IDE.openEditor(fActivePage, file, id);
        assertEquals(editor.getSite().getId(), id);
        assertEquals(hasEditor(editor), true);
        assertEquals(fActivePage.getActiveEditor(), editor);

        //open another editor to take the focus away from the first editor
        IDE.openEditor(fActivePage, FileUtil.createFile("test.mock2", proj));

        /*
         javadoc: If this page already has an editor open on the target object that editor is activated
         */
        //open the first editor second time.
        assertEquals(IDE.openEditor(fActivePage, file, id), editor);
        assertEquals(fActivePage.getActiveEditor(), editor);
    }

    /**
     * openEditor(IEditorInput input,String editorId)                       
     */
    public void testOpenEditor3() throws Throwable {
        proj = FileUtil.createProject("testOpenEditor");
        final String id = MockEditorPart.ID1;
        IEditorInput input = new FileEditorInput(FileUtil.createFile(
                "test.mock1", proj));

        /*
         javadoc: The editor type is determined by mapping editorId to an editor extension registered with the workbench
         */
        IEditorPart editor = IDE.openEditor(fActivePage, input, id);
        assertEquals(editor.getEditorInput(), input);
        assertEquals(editor.getSite().getId(), id);
        assertEquals(hasEditor(editor), true);
        assertEquals(fActivePage.getActiveEditor(), editor);

        //open another editor to take the focus away from the first editor
        IDE.openEditor(fActivePage, FileUtil.createFile("test.mock2", proj));

        /*
         javadoc: If this page already has an editor open on the target object that editor is activated
         */
        //open the first editor second time.
        assertEquals(IDE.openEditor(fActivePage, input, id), editor);
        assertEquals(fActivePage.getActiveEditor(), editor);
    }

    /**
     * openEditor(IMarker marker)                       
     */
    public void testOpenEditor5() throws Throwable {
        proj = FileUtil.createProject("testOpenEditor");
        IMarker marker = FileUtil.createFile("aa.mock2", proj).createMarker(
                IMarker.TASK);
        CallHistory callTrace;

        /*  
         javadoc: the cursor and selection state of the editor is then updated from information 
         recorded in the marker. 
         */
        //open the registered editor for the marker resource 
        IEditorPart editor = IDE.openEditor(fActivePage, marker);
        callTrace = ((MockEditorPart) editor).getCallHistory();
        assertEquals(editor.getSite().getId(), MockEditorPart.ID2);
        assertEquals(hasEditor(editor), true);
        assertEquals(fActivePage.getActiveEditor(), editor);
        assertEquals(callTrace.contains("gotoMarker"), true);
        fActivePage.closeEditor(editor, false);

        /*  
         javadoc: If the marker contains an EDITOR_ID_ATTR attribute the attribute value will be used to 
         determine the editor type to be opened
         */
        marker.setAttribute(IDE.EDITOR_ID_ATTR, MockEditorPart.ID1);
        editor = IDE.openEditor(fActivePage, marker);
        callTrace = ((MockEditorPart) editor).getCallHistory();
        assertEquals(editor.getSite().getId(), MockEditorPart.ID1);
        assertEquals(hasEditor(editor), true);
        assertEquals(fActivePage.getActiveEditor(), editor);
        assertEquals(callTrace.contains("gotoMarker"), true);
        //do not close the editor this time

        /*
         javdoc: If this page already has an editor open on the target object that editor is activated
         */
        callTrace.clear();
        assertEquals(IDE.openEditor(fActivePage, marker), editor);
        assertEquals(fActivePage.getActiveEditor(), editor);
        assertEquals(callTrace.contains("gotoMarker"), true);
        fActivePage.closeEditor(editor, false);
    }

    /**
     *  openEditor(IMarker marker, boolean activate)                                           
     */
    public void testOpenEditor6() throws Throwable {
        proj = FileUtil.createProject("testOpenEditor");
        IMarker marker = FileUtil.createFile("aa.mock2", proj).createMarker(
                IMarker.TASK);
        MockPartListener listener = new MockPartListener();
        fActivePage.addPartListener(listener);
        CallHistory listenerCall = listener.getCallHistory();
        CallHistory editorCall;

        //we need another editor so that the editor under test can receive events.
        //otherwise, events will be ignored.
        IEditorPart extra = IDE.openEditor(fActivePage, FileUtil.createFile(
                "aaaaa", proj));

        /*
         javadoc: If activate == true the editor will be activated
         */
        //open the registered editor for the marker resource with activation
        IEditorPart editor = IDE.openEditor(fActivePage, marker, true);
        editorCall = ((MockEditorPart) editor).getCallHistory();
        assertEquals(editor.getSite().getId(), MockEditorPart.ID2);
        assertEquals(hasEditor(editor), true);
        assertEquals(fActivePage.getActiveEditor(), editor);

        /*  
         javadoc: the cursor and selection state of the editor is then updated from information 
         recorded in the marker. 
         */
        assertEquals(editorCall.contains("gotoMarker"), true);
        fActivePage.closeEditor(editor, false);

        fActivePage.activate(extra);

        //open the registered editor for the marker resource without activation
        fActivePage.showView(IPageLayout.ID_PROBLEM_VIEW, null, IWorkbenchPage.VIEW_ACTIVATE);
        listenerCall.clear();
        editor = IDE.openEditor(fActivePage, marker, false);
        editorCall = ((MockEditorPart) editor).getCallHistory();
        assertEquals(editor.getSite().getId(), MockEditorPart.ID2);
        assertEquals(hasEditor(editor), true);
        assertEquals(listenerCall.contains("partBroughtToTop"), true);
        assertEquals(listenerCall.contains("partActivated"), false);
        assertEquals(editorCall.contains("gotoMarker"), true);
        fActivePage.closeEditor(editor, false);

        /*  
         javadoc: If the marker contains an EDITOR_ID_ATTR attribute the attribute value will be used to 
         determine the editor type to be opened
         */
        String id = MockEditorPart.ID1;
        marker.setAttribute(IDE.EDITOR_ID_ATTR, id);

        //open an editor with activation
        listenerCall.clear();

        editor = IDE.openEditor(fActivePage, marker, true);
        editorCall = ((MockEditorPart) editor).getCallHistory();
        assertEquals(editor.getSite().getId(), id);
        assertEquals(hasEditor(editor), true);
        assertEquals(fActivePage.getActiveEditor(), editor);
        assertEquals(editorCall.contains("gotoMarker"), true);
        fActivePage.closeEditor(editor, false);

        fActivePage.activate(extra);

        //open an editor without activation
        fActivePage.showView(IPageLayout.ID_PROBLEM_VIEW, null, IWorkbenchPage.VIEW_ACTIVATE);
        listenerCall.clear();
        editor = IDE.openEditor(fActivePage, marker, false);
        editorCall = ((MockEditorPart) editor).getCallHistory();
        assertEquals(editor.getSite().getId(), id);
        assertEquals(hasEditor(editor), true);
        assertEquals(editorCall.contains("gotoMarker"), true);
        assertEquals(listenerCall.contains("partActivated"), false);
        assertEquals(listenerCall.contains("partBroughtToTop"), true);
        //do not close the editor this time

        fActivePage.activate(extra);
        /*
         javadoc: If this page already has an editor open on the target object that editor is brought to front
         Note: we need to make a non-editor active first or bringing the editor to front would activate it
         */
        //open the editor second time without activation
        fActivePage.showView(IPageLayout.ID_PROBLEM_VIEW, null, IWorkbenchPage.VIEW_ACTIVATE);
        listenerCall.clear();
        assertEquals(IDE.openEditor(fActivePage, marker, false), editor);
        assertEquals(listenerCall.contains("partBroughtToTop"), true);
        assertEquals(listenerCall.contains("partActivated"), false);

        fActivePage.activate(extra);

        /*
         javdoc: If activate == true the editor will be activated
         */
        //open the editor second time with activation       
        listenerCall.clear();
        assertEquals(IDE.openEditor(fActivePage, marker, true), editor);
        assertEquals(editorCall.contains("gotoMarker"), true);
        assertEquals(listenerCall.contains("partBroughtToTop"), true);
        assertEquals(listenerCall.contains("partActivated"), true);
        
        /*
        javadoc: If activate == false but another editor in the same stack was active, the 
        new editor will be activated regardless.
        */
        // Close the old editor
        fActivePage.closeEditor(editor, false);
        // Ensure another editor in the stack is active
        fActivePage.activate(extra);
        listenerCall.clear();
        // Verify that the editor is still activated
        IDE.openEditor(fActivePage, marker, false);
        assertEquals(listenerCall.contains("partBroughtToTop"), true);
        assertEquals(listenerCall.contains("partActivated"), true);
    }

    
    public void testGetPerspective() throws Throwable {
        assertNotNull(fActivePage.getPerspective());

        IWorkbenchPage page = fWin.openPage(EmptyPerspective.PERSP_ID, getPageInput());
        assertEquals(EmptyPerspective.PERSP_ID, page.getPerspective().getId());
    }

    public void testSetPerspective() throws Throwable {
        IPerspectiveDescriptor per = PlatformUI.getWorkbench()
                .getPerspectiveRegistry().findPerspectiveWithId(
                        EmptyPerspective.PERSP_ID);
        fActivePage.setPerspective(per);
        assertEquals(per, fActivePage.getPerspective());
    }

    public void testGetLabel() {
        assertNotNull(fActivePage.getLabel());
    }

    public void testGetInput() throws Throwable {
        IAdaptable input = getPageInput();
        IWorkbenchPage page = fWin.openPage(input);
        assertEquals(input, page.getInput());
    }

    public void testActivate() throws Throwable {
        MockViewPart part = (MockViewPart) fActivePage
                .showView(MockViewPart.ID);
        MockViewPart part2 = (MockViewPart) fActivePage
                .showView(MockViewPart.ID2);

        MockPartListener listener = new MockPartListener();
        fActivePage.addPartListener(listener);
        fActivePage.activate(part);

        CallHistory callTrace;

        callTrace = part2.getCallHistory();
        callTrace.clear();
        fActivePage.activate(part2);
        assertTrue(callTrace.contains("setFocus"));
        assertTrue(listener.getCallHistory().contains("partActivated"));

        callTrace = part.getCallHistory();
        callTrace.clear();
        fActivePage.activate(part);
        assertTrue(callTrace.contains("setFocus"));
        assertTrue(listener.getCallHistory().contains("partActivated"));
    }

    public void testBringToTop() throws Throwable {
        proj = FileUtil.createProject("testOpenEditor");
        IEditorPart part = IDE.openEditor(fActivePage, FileUtil.createFile(
                "a.mock1", proj));
        IEditorPart part2 = IDE.openEditor(fActivePage, FileUtil.createFile(
                "b.mock1", proj));

        MockPartListener listener = new MockPartListener();
        fActivePage.addPartListener(listener);
        CallHistory callTrace = listener.getCallHistory();

        //at this point, part2 is active
        fActivePage.bringToTop(part);
        assertEquals(callTrace.contains("partBroughtToTop"), true);

        callTrace.clear();
        fActivePage.bringToTop(part2);
        assertEquals(callTrace.contains("partBroughtToTop"), true);
    }

    public void testGetWorkbenchWindow() {
        /*
         * Commented out because until test case can be updated to work
         * with new window/page/perspective implementation
         * 
         assertEquals(fActivePage.getWorkbenchWindow(), fWin);
         IWorkbenchPage page = openTestPage(fWin);
         assertEquals(page.getWorkbenchWindow(), fWin);
         */
    }

    public void testShowView() throws Throwable {
        /*
         javadoc: Shows a view in this page and give it focus
         */
        MockViewPart view = (MockViewPart) fActivePage
                .showView(MockViewPart.ID);
        assertNotNull(view);
        assertTrue(view.getCallHistory().verifyOrder(
                new String[] { "init", "createPartControl", "setFocus" }));

        fActivePage.showView(MockViewPart.ID2);

        /*
         javadoc: If the view is already visible, it is given focus
         */
        CallHistory callTrace = view.getCallHistory();
        callTrace.clear();
        assertEquals(fActivePage.showView(MockViewPart.ID), view);
        assertEquals(callTrace.contains("setFocus"), true);
    }

    /**
     * Tests showing multi-instance views (docked normally).
     */
    public void testShowViewMult() throws Throwable {
        /*
         javadoc: Shows the view identified by the given view id and secondary id 
         in this page and gives it focus. 
         This allows multiple instances of a particular view to be created.  
         They are disambiguated using the secondary id.
         */
        MockViewPart view = (MockViewPart) fActivePage
                .showView(MockViewPart.IDMULT);
        assertNotNull(view);
        assertTrue(view.getCallHistory().verifyOrder(
                new String[] { "init", "createPartControl", "setFocus" }));
        MockViewPart view2 = (MockViewPart) fActivePage.showView(
                MockViewPart.IDMULT, "2", IWorkbenchPage.VIEW_ACTIVATE);
        assertNotNull(view2);
        assertTrue(view2.getCallHistory().verifyOrder(
                new String[] { "init", "createPartControl", "setFocus" }));
        assertTrue(!view.equals(view2));
        MockViewPart view3 = (MockViewPart) fActivePage.showView(
                MockViewPart.IDMULT, "3", IWorkbenchPage.VIEW_ACTIVATE);
        assertNotNull(view3);
        assertTrue(view3.getCallHistory().verifyOrder(
                new String[] { "init", "createPartControl", "setFocus" }));
        assertTrue(!view.equals(view3));
        assertTrue(!view2.equals(view3));

        /*
         javadoc: If there is a view identified by the given view id and 
         secondary id already open in this page, it is given focus.
         */
        CallHistory callTrace = view.getCallHistory();
        callTrace.clear();
        assertEquals(fActivePage.showView(MockViewPart.IDMULT), view);
        assertEquals(callTrace.contains("setFocus"), true);
        CallHistory callTrace2 = view2.getCallHistory();
        callTrace.clear();
        callTrace2.clear();
        assertEquals(fActivePage.showView(MockViewPart.IDMULT, "2",
                IWorkbenchPage.VIEW_ACTIVATE), view2);
        assertEquals(callTrace2.contains("setFocus"), true);
        assertEquals(callTrace.contains("setFocus"), false);
        CallHistory callTrace3 = view3.getCallHistory();
        callTrace.clear();
        callTrace2.clear();
        callTrace3.clear();
        assertEquals(fActivePage.showView(MockViewPart.IDMULT, "3",
                IWorkbenchPage.VIEW_ACTIVATE), view3);
        assertEquals(callTrace3.contains("setFocus"), true);
        assertEquals(callTrace.contains("setFocus"), false);
        assertEquals(callTrace2.contains("setFocus"), false);

        /*
         javadoc: If a secondary id is given, the view must allow multiple instances by
         having specified allowMultiple="true" in its extension.
         */
        boolean exceptionThrown = false;
        try {
            fActivePage.showView(MockViewPart.ID, "2",
                    IWorkbenchPage.VIEW_ACTIVATE);
        } catch (PartInitException e) {
            assertEquals(e.getMessage().indexOf("mult") != -1, true);
            exceptionThrown = true;
        }
        assertEquals(exceptionThrown, true);
    }

    /**
     * Tests showing multi-instance views (as fast views).
     * This is a regression test for bug 76669 - [Perspectives] NullPointerException in Perspective.getFastViewWidthRatio()
     */
    public void testShowViewMultFast() throws Throwable {
        /*
         javadoc: Shows the view identified by the given view id and secondary id 
         in this page and gives it focus. 
         This allows multiple instances of a particular view to be created.  
         They are disambiguated using the secondary id.
         */
        MockViewPart view = (MockViewPart) fActivePage
                .showView(MockViewPart.IDMULT);
        assertNotNull(view);
        assertTrue(view.getCallHistory().verifyOrder(
                new String[] { "init", "createPartControl", "setFocus" }));
        MockViewPart view2 = (MockViewPart) fActivePage.showView(
                MockViewPart.IDMULT, "2", IWorkbenchPage.VIEW_ACTIVATE);
        assertNotNull(view2);
        assertTrue(view2.getCallHistory().verifyOrder(
                new String[] { "init", "createPartControl", "setFocus" }));
        assertTrue(!view.equals(view2));
        
        WorkbenchPage page = (WorkbenchPage) fActivePage;
        IViewReference ref = (IViewReference) page.getReference(view);
        IViewReference ref2 = (IViewReference) page.getReference(view2);
        page.addFastView(ref);
        page.addFastView(ref2);

        page.activate(view);
        assertEquals(view, page.getActivePart());

        page.activate(view2);
        assertEquals(view2, page.getActivePart());
    }

    /**
     * Tests saving the page state when there is a fast view that is also a multi-instance view.
     * This is a regression test for bug 76669 - [Perspectives] NullPointerException in Perspective.getFastViewWidthRatio()
     */
    public void testBug76669() throws Throwable {
        MockViewPart view = (MockViewPart) fActivePage
                .showView(MockViewPart.IDMULT);
        MockViewPart view2 = (MockViewPart) fActivePage.showView(
                MockViewPart.IDMULT, "2", IWorkbenchPage.VIEW_ACTIVATE);
        
        WorkbenchPage page = (WorkbenchPage) fActivePage;
        IViewReference ref = (IViewReference) page.getReference(view);
        IViewReference ref2 = (IViewReference) page.getReference(view2);
        page.addFastView(ref);
        page.addFastView(ref2);

        IMemento memento = XMLMemento.createWriteRoot("page");
        page.saveState(memento);
        IMemento persps = memento.getChild("perspectives");
        IMemento persp = persps.getChildren("perspective")[0];
        IMemento[] fastViews = persp.getChild("fastViews").getChildren("view");
        assertEquals(2, fastViews.length);
    }

    public void testFindView() throws Throwable {
        String id = MockViewPart.ID3;
        //id of valid, but not open view
        assertNull(fActivePage.findView(id));

        IViewPart view = fActivePage.showView(id);
        assertEquals(fActivePage.findView(id), view);

        //close view		
        fActivePage.hideView(view);
        assertNull(fActivePage.findView(id));
    }

    public void testFindViewReference() throws Throwable {
        fActivePage.getWorkbenchWindow().getWorkbench().showPerspective(
                SessionPerspective.ID, fActivePage.getWorkbenchWindow());
        assertNull(fActivePage.findViewReference(MockViewPart.ID4));

        fActivePage.showView(MockViewPart.ID4);
        assertNotNull(fActivePage.findViewReference(MockViewPart.ID4));
    }

    public void testGetViews() throws Throwable {
        int totalBefore = fActivePage.getViewReferences().length;

        IViewPart view = fActivePage.showView(MockViewPart.ID2);
        assertEquals(hasView(view), true);
        assertEquals(fActivePage.getViewReferences().length, totalBefore + 1);

        fActivePage.hideView(view);
        assertEquals(hasView(view), false);
        assertEquals(fActivePage.getViewReferences().length, totalBefore);
    }

    public void testHideViewWithPart() throws Throwable {
        // test that nothing bad happens with a null parameter
        try {
            fActivePage.hideView((IViewPart) null);
        } catch (RuntimeException e) {
            fail(e.getMessage());
        }

        IViewPart view = fActivePage.showView(MockViewPart.ID3);

        fActivePage.hideView(view);
        CallHistory callTrace = ((MockViewPart) view).getCallHistory();
        assertTrue(callTrace.contains("dispose"));
    }

    public void testHideViewWithReference() throws Throwable {
        // test that nothing bad happens with a null parameter
        try {
            fActivePage.hideView((IViewReference) null);
        } catch (RuntimeException e) {
            fail(e.getMessage());
        }

        IViewPart view = fActivePage.showView(MockViewPart.ID4);
        IViewReference ref = fActivePage.findViewReference(MockViewPart.ID4);
        fActivePage.hideView(ref);
        CallHistory callTrace = ((MockViewPart) view).getCallHistory();
        assertTrue(callTrace.contains("dispose"));

    }

	public void testHideSaveableView() throws Throwable {
		String viewId = SaveableMockViewPart.ID;
		SaveableMockViewPart view = (SaveableMockViewPart) fActivePage.showView(viewId);
		fActivePage.hideView(view);
		CallHistory callTrace = view.getCallHistory();
		assertTrue(callTrace.contains("isDirty"));
		assertTrue(callTrace.contains("dispose"));
		assertEquals(fActivePage.findView(viewId), null);

		try {
			SaveableHelper.testSetAutomatedResponse(1);  // No
			view = (SaveableMockViewPart) fActivePage.showView(viewId);
			view.setDirty(true);
			fActivePage.hideView(view);
			callTrace = view.getCallHistory();
			assertTrue(callTrace.contains("isDirty"));		
			assertFalse(callTrace.contains("doSave"));		
			assertTrue(callTrace.contains("dispose"));
			assertEquals(fActivePage.findView(viewId), null);

			SaveableHelper.testSetAutomatedResponse(2);  // Cancel
			view = (SaveableMockViewPart) fActivePage.showView(viewId);
			view.setDirty(true);
			fActivePage.hideView(view);
			callTrace = view.getCallHistory();
			assertTrue(callTrace.contains("isDirty"));		
			assertFalse(callTrace.contains("doSave"));		
			assertFalse(callTrace.contains("dispose"));
			assertEquals(fActivePage.findView(viewId), view);

			SaveableHelper.testSetAutomatedResponse(0);  // Yes
			view = (SaveableMockViewPart) fActivePage.showView(viewId);
			view.setDirty(true);
			fActivePage.hideView(view);
			callTrace = view.getCallHistory();
			assertTrue(callTrace.contains("isDirty"));		
			assertTrue(callTrace.contains("doSave"));
			assertTrue(callTrace.contains("dispose"));
			assertEquals(fActivePage.findView(viewId), null);

			// don't leave the view showing, or the UI will block on window close
		}
		finally {
			SaveableHelper.testSetAutomatedResponse(-1);  // restore default (prompt)
		}
	}
	
    /**
     * Tests that a close will fall back to the default if the view returns
     * ISaveable2.DEFAULT.
     * 
     * @throws Throwable
     */
    public void testCloseWithSaveNeeded() throws Throwable {
		String viewId = UserSaveableMockViewPart.ID;
		UserSaveableMockViewPart view = (UserSaveableMockViewPart) fActivePage
				.showView(viewId);
		fActivePage.hideView(view);

		UserSaveableMockViewPart view2 = null;

		CallHistory callTrace = view.getCallHistory();
		assertTrue(callTrace.contains("isDirty"));
		assertTrue(callTrace.contains("dispose"));
		assertEquals(fActivePage.findView(UserSaveableMockViewPart.ID), null);

		try {
			SaveableHelper.testSetAutomatedResponse(3); // DEFAULT
			view = (UserSaveableMockViewPart) fActivePage.showView(viewId);
			view.setDirty(true);
			view2 = (UserSaveableMockViewPart) fActivePage.showView(viewId,
					"2", IWorkbenchPage.VIEW_ACTIVATE);
			assertNotNull(view2);
			view2.setDirty(true);

			WorkbenchPage page = (WorkbenchPage) fActivePage;
			page.getEditorManager().saveAll(true, false);
			
			assertFalse(view.isDirty());
			assertFalse(view2.isDirty());

			callTrace = view.getCallHistory();
			fActivePage.hideView(view);
			fActivePage.hideView(view2);
			
			assertTrue(callTrace.contains("isDirty"));
			assertTrue(callTrace.contains("doSave"));
			assertEquals(fActivePage.findView(viewId), null);

			// don't leave the view showing, or the UI will block on window
			// close
		} finally {
			SaveableHelper.testSetAutomatedResponse(-1); // restore default
			// (prompt)
		}
	}

	
    public void testClose() throws Throwable {
        IWorkbenchPage page = openTestPage(fWin);

        proj = FileUtil.createProject("testOpenEditor");
        final IFile file = FileUtil.createFile("aaa.mock1", proj);
        IEditorPart editor = IDE.openEditor(page, file);
        CallHistory callTrace = ((MockEditorPart) editor).getCallHistory();
        callTrace.clear();

        /*
         javadoc: If the page has open editors with unsaved content and save is true, the user will be given the opportunity to save them
         */
        assertEquals(page.close(), true);
        assertEquals(callTrace
                .verifyOrder(new String[] { "isDirty", "dispose" }), true);
        assertEquals(fWin.getActivePage(), fActivePage);
    }

    public void testCloseEditor() throws Throwable {
        proj = FileUtil.createProject("testOpenEditor");
        final IFile file = FileUtil.createFile("test.mock1", proj);
        IEditorPart editor;
        CallHistory callTrace;
        MockEditorPart mock;

        /*
         javadoc: Parameters: save - true to save the editor contents if required (recommended)
         */
        //create a clean editor that needs to be saved on closing
        editor = IDE.openEditor(fActivePage, file);
        mock = (MockEditorPart) editor;
        mock.setSaveNeeded(true);
        callTrace = mock.getCallHistory();
        callTrace.clear();
        //close the editor with save confirmation
        assertEquals(fActivePage.closeEditor(editor, true), true);
        assertEquals(callTrace
                .verifyOrder(new String[] { "isDirty", "dispose" }), true);

        /*
         javadoc: If the editor has unsaved content and save is true, the user will be given the opportunity to save it.
         */
        //can't be tested
        /*
         javadoc: Parameters: save - false to discard any unsaved changes
         */
        //create a dirty editor
        editor = IDE.openEditor(fActivePage, file);
        mock = (MockEditorPart) editor;
        mock.setDirty(true);
        mock.setSaveNeeded(true);
        callTrace = mock.getCallHistory();
        callTrace.clear();
        //close the editor and discard changes
        assertEquals(fActivePage.closeEditor(editor, false), true);
        assertEquals(callTrace.contains("isSaveOnCloseNeeded"), false);
        /*
         * It is possible that some action may query the isDirty value of
         * the editor to update its enabled state. There is nothing wrong
         * in doing that, so do not test for no isDirty call here.
         *
         * assertEquals(callTrace.contains( "isDirty"), false);
         */
        assertEquals(callTrace.contains("doSave"), false);
        assertEquals(callTrace.contains("dispose"), true);
    }

    public void testCloseEditors() throws Throwable {
        int total = 5;
        final IFile[] files = new IFile[total];
        IEditorPart[] editors = new IEditorPart[total];
        IEditorReference[] editorRefs = new IEditorReference[total];
        CallHistory[] callTraces = new CallHistory[total];
        MockEditorPart[] mocks = new MockEditorPart[total];

        proj = FileUtil.createProject("testCloseEditors");
        for (int i = 0; i < total; i++)
            files[i] = FileUtil.createFile(i + ".mock2", proj);

        /*
         javadoc: If the page has open editors with unsaved content and save is true, the user will be given the opportunity to save them.
         */
        //close all clean editors with confirmation
        for (int i = 0; i < total; i++) {
            editors[i] = IDE.openEditor(fActivePage, files[i]);
            callTraces[i] = ((MockEditorPart) editors[i]).getCallHistory();
        }

        editorRefs = fActivePage.getEditorReferences();
        assertEquals(fActivePage.closeEditors(editorRefs, true), true);
        for (int i = 0; i < total; i++) {
            assertEquals(callTraces[i].contains("isDirty"), true);
            assertEquals(callTraces[i].contains("doSave"), false);
            callTraces[i].clear();
        }

        //close all dirty editors with confirmation
        //can't be tested		

        //close all dirty editors discarding them		
        for (int i = 0; i < total; i++) {
            editors[i] = IDE.openEditor(fActivePage, files[i]);
            mocks[i] = (MockEditorPart) editors[i];
            mocks[i].setDirty(true);
            callTraces[i] = mocks[i].getCallHistory();
        }
        editorRefs = fActivePage.getEditorReferences();
        assertEquals(fActivePage.closeEditors(editorRefs, false), true);
        for (int i = 0; i < total; i++) {
            assertEquals(callTraces[i].contains("doSave"), false);
        }

        //close empty array of editors
        total = 1;
        for (int i = 0; i < total; i++) {
            editors[i] = IDE.openEditor(fActivePage, files[i]);
            mocks[i] = (MockEditorPart) editors[i];
            mocks[i].setDirty(true);
            callTraces[i] = mocks[i].getCallHistory();
        }
        // empty array test
        editorRefs = new IEditorReference[0];
        assertEquals(fActivePage.closeEditors(editorRefs, true), true);
        for (int i = 0; i < total; i++) {
            assertEquals(callTraces[i].contains("isDirty"), true);
            assertEquals(callTraces[i].contains("doSave"), false);
            callTraces[i].clear();
        }

        // close the last remaining editor, with save=false
        editorRefs = fActivePage.getEditorReferences();
        fActivePage.closeEditors(editorRefs, false);
    }

    public void testCloseAllEditors() throws Throwable {
        int total = 5;
        final IFile[] files = new IFile[total];
        IEditorPart[] editors = new IEditorPart[total];
        CallHistory[] callTraces = new CallHistory[total];
        MockEditorPart[] mocks = new MockEditorPart[total];

        proj = FileUtil.createProject("testOpenEditor");
        for (int i = 0; i < total; i++)
            files[i] = FileUtil.createFile(i + ".mock2", proj);

        /*
         javadoc: If the page has open editors with unsaved content and save is true, the user will be given the opportunity to save them.
         */
        //close all clean editors with confirmation
        for (int i = 0; i < total; i++) {
            editors[i] = IDE.openEditor(fActivePage, files[i]);
            callTraces[i] = ((MockEditorPart) editors[i]).getCallHistory();
        }
        assertEquals(fActivePage.closeAllEditors(true), true);
        for (int i = 0; i < total; i++) {
            assertEquals(callTraces[i].contains("isDirty"), true);
            assertEquals(callTraces[i].contains("doSave"), false);
            callTraces[i].clear();
        }

        //close all dirty editors with confirmation
        //can't be tested		

        //close all dirty editors discarding them		
        for (int i = 0; i < total; i++) {
            editors[i] = IDE.openEditor(fActivePage, files[i]);
            mocks[i] = (MockEditorPart) editors[i];
            mocks[i].setDirty(true);
            callTraces[i] = mocks[i].getCallHistory();
        }
        assertEquals(fActivePage.closeAllEditors(false), true);
        for (int i = 0; i < total; i++) {
            assertEquals(callTraces[i].contains("doSave"), false);
        }
    }

    public void testSaveEditor() throws Throwable {
        proj = FileUtil.createProject("testOpenEditor");
        final IFile file = FileUtil.createFile("test.mock1", proj);
        IEditorPart editor;
        CallHistory callTrace;
        MockEditorPart mock;

        //create a clean editor
        editor = IDE.openEditor(fActivePage, file);
        mock = (MockEditorPart) editor;
        callTrace = mock.getCallHistory();
        callTrace.clear();

        /*
         javadoc: Saves the contents of the given editor if dirty. 
         If not, this method returns without effect
         */
        //save the clean editor with confirmation		
        assertEquals(fActivePage.saveEditor(editor, true), true);
        assertEquals(callTrace.contains("isDirty"), true);
        assertEquals(callTrace.contains("doSave"), false);

        /*
         javadoc: If confirm is true the user is prompted to confirm the command. 
         */
        //can't be tested
        /*
         javadoc: Otherwise, the save happens without prompt. 
         */
        //save the clean editor without confirmation
        assertEquals(fActivePage.saveEditor(editor, false), true);
        assertEquals(callTrace.contains("isDirty"), true);
        assertEquals(callTrace.contains("doSave"), false);

        //save the dirty editor without confirmation
        mock.setDirty(true);
        callTrace.clear();
        assertEquals(fActivePage.saveEditor(editor, false), true);
        assertEquals(callTrace
                .verifyOrder(new String[] { "isDirty", "doSave" }), true);
    }

    public void testIDESaveAllEditors() throws Throwable {
        int total = 3;

        final IFile[] files = new IFile[total];
        IEditorPart[] editors = new IEditorPart[total];
        CallHistory[] callTraces = new CallHistory[total];
        MockEditorPart[] mocks = new MockEditorPart[total];

        proj = FileUtil.createProject("testOpenEditor");
        for (int i = 0; i < total; i++) {
            files[i] = FileUtil.createFile(i + ".mock2", proj);
            editors[i] = IDE.openEditor(fActivePage, files[i]);
            mocks[i] = (MockEditorPart) editors[i];
            callTraces[i] = mocks[i].getCallHistory();
        }

        /*
         javadoc: If there are no dirty editors this method returns without effect.
         javadoc: If confirm is true the user is prompted to confirm the command
         */
        //save all clean editors with confirmation
        assertEquals(IDE.saveAllEditors(new IResource[] { proj }, true), true);
        for (int i = 0; i < total; i++) {
            assertEquals(callTraces[i].contains("isDirty"), true);
            assertEquals(callTraces[i].contains("doSave"), false);
            callTraces[i].clear();
        }

        //save all dirty editors with confirmation can't be tested

        /*
         javadoc: Parameters: confirm - false to save unsaved changes without asking
         */
        //save all clean editors without confirmation
        assertEquals(IDE.saveAllEditors(new IResource[] { proj }, false), true);
        for (int i = 0; i < total; i++) {
            assertEquals(callTraces[i].contains("isDirty"), true);
            assertEquals(callTraces[i].contains("doSave"), false);
            callTraces[i].clear();
        }

        //save all dirty editors with resource that IS NOT a parent
        // of the contents of the dirty editors without confirmation, this should not 
        // save any as they are not parented by the resource provided
        for (int i = 0; i < total; i++)
            mocks[i].setDirty(true);

        IResource emptyProj = FileUtil
                .createProject("testOpenEditorEmptyProject");
        assertEquals(IDE.saveAllEditors(new IResource[] { emptyProj }, false),
                true);
        for (int i = 0; i < total; i++) {
            // the editors were not in the empty project hence still dirty
            assertEquals(mocks[i].isDirty(), true);
            callTraces[i].clear();
        }

        //save all dirty editors with resource that IS a parent
        // of the contents of the editors without confirmation, this should 
        // save them as they are parented by the resource provided
        assertEquals(IDE.saveAllEditors(new IResource[] { proj }, false), true);
        for (int i = 0; i < total; i++) {
            // the editors were not in the empty project hence still dirty
            assertEquals(mocks[i].isDirty(), false);
            assertEquals(callTraces[i].contains("isDirty"), true);
            assertEquals(callTraces[i].contains("doSave"), true);
            callTraces[i].clear();
        }

        //save all dirty editors with resource that IS NOT a parent
        // of the contents of the dirty editors without confirmation, this should not 
        // save any as they are not parented by the resource provided
        for (int i = 0; i < total; i++)
            mocks[i].setDirty(true);
        assertEquals(IDE.saveAllEditors(new IResource[] {}, false), true);
        for (int i = 0; i < total; i++) {
            // the editors were not in the empty project hence still dirty
            assertEquals(mocks[i].isDirty(), true);
            callTraces[i].clear();
        }

        // clear the dirty state so the tearDown does not open a confirm dialog.
        for (int i = 0; i < total; i++)
            mocks[i].setDirty(false);
    }

    public void testSaveAllEditors() throws Throwable {
        int total = 3;

        final IFile[] files = new IFile[total];
        IEditorPart[] editors = new IEditorPart[total];
        CallHistory[] callTraces = new CallHistory[total];
        MockEditorPart[] mocks = new MockEditorPart[total];

        proj = FileUtil.createProject("testOpenEditor");
        for (int i = 0; i < total; i++) {
            files[i] = FileUtil.createFile(i + ".mock2", proj);
            editors[i] = IDE.openEditor(fActivePage, files[i]);
            mocks[i] = (MockEditorPart) editors[i];
            callTraces[i] = mocks[i].getCallHistory();
        }

        /*
         javadoc: If there are no dirty editors this method returns without effect.
         javadoc: If confirm is true the user is prompted to confirm the command
         */
        //save all clean editors with confirmation
        assertEquals(fActivePage.saveAllEditors(true), true);
        for (int i = 0; i < total; i++) {
            assertEquals(callTraces[i].contains("isDirty"), true);
            assertEquals(callTraces[i].contains("doSave"), false);
            callTraces[i].clear();
        }

        //save all dirty editors with confirmation can't be tested

        /*
         javadoc: Parameters: confirm - false to save unsaved changes without asking
         */
        //save all clean editors without confirmation
        assertEquals(fActivePage.saveAllEditors(false), true);
        for (int i = 0; i < total; i++) {
            assertEquals(callTraces[i].contains("isDirty"), true);
            assertEquals(callTraces[i].contains("doSave"), false);
            callTraces[i].clear();
        }

        //save all dirty editors without confirmation
        for (int i = 0; i < total; i++)
            mocks[i].setDirty(true);
        assertEquals(fActivePage.saveAllEditors(false), true);
        for (int i = 0; i < total; i++)
            assertEquals(callTraces[i].verifyOrder(new String[] { "isDirty",
                    "doSave" }), true);
    }

    public void testGetEditors() throws Throwable {
        proj = FileUtil.createProject("testOpenEditor");
        int totalBefore = fActivePage.getEditorReferences().length;
        int num = 3;
        IEditorPart[] editors = new IEditorPart[num];

        for (int i = 0; i < num; i++) {
            editors[i] = IDE.openEditor(fActivePage, FileUtil.createFile(i
                    + ".mock2", proj));
            assertEquals(hasEditor(editors[i]), true);
        }
        assertEquals(fActivePage.getEditorReferences().length, totalBefore
                + num);

        fActivePage.closeEditor(editors[0], false);
        assertEquals(hasEditor(editors[0]), false);
        assertEquals(fActivePage.getEditorReferences().length, totalBefore
                + num - 1);

        fActivePage.closeAllEditors(false);
        assertEquals(fActivePage.getEditorReferences().length, 0);
    }

    public void testShowActionSet() {
        String id = MockActionDelegate.ACTION_SET_ID;
        WorkbenchPage page = (WorkbenchPage) fActivePage;

        int totalBefore = page.getActionSets().length;
        fActivePage.showActionSet(id);

        IActionSetDescriptor[] sets = ((WorkbenchPage) fActivePage)
                .getActionSets();
        boolean found = false;
        for (int i = 0; i < sets.length; i++)
            if (id.equals(sets[i].getId()))
                found = true;
        assertEquals(found, true);

        //check that the method does not add an invalid action set to itself
        id = IConstants.FakeID;
        fActivePage.showActionSet(id);

        sets = ((WorkbenchPage) fActivePage).getActionSets();
        found = false;
        for (int i = 0; i < sets.length; i++)
            if (id.equals(sets[i].getId()))
                found = true;
        assertEquals(found, false);
        assertEquals(page.getActionSets().length, totalBefore + 1);
    }

    public void testHideActionSet() {
        WorkbenchPage page = (WorkbenchPage) fActivePage;
        int totalBefore = page.getActionSets().length;

        String id = MockWorkbenchWindowActionDelegate.SET_ID;
        fActivePage.showActionSet(id);
        assertEquals(page.getActionSets().length, totalBefore + 1);

        fActivePage.hideActionSet(id);
        assertEquals(page.getActionSets().length, totalBefore);

        IActionSetDescriptor[] sets = page.getActionSets();
        boolean found = false;
        for (int i = 0; i < sets.length; i++)
            if (id.equals(sets[i].getId()))
                found = true;
        assertEquals(found, false);
    }

    /**
     * Return whether or not the editor exists in the current page.
     * @param editor
     *  @return boolean
     */
    private boolean hasEditor(IEditorPart editor) {
        IEditorReference[] references = fActivePage.getEditorReferences();
        for (int i = 0; i < references.length; i++) {
            if (references[i].getEditor(false).equals(editor))
                return true;
        }
        return false;
    }

    /**
     * Return whether or not the view exists in the current page.
     * @param editor
     * @return boolean
     */
    private boolean hasView(IViewPart view) {
        IViewReference[] references = fActivePage.getViewReferences();
        for (int i = 0; i < references.length; i++) {
            if (references[i].getView(false).equals(view))
                return true;
        }
        return false;
    }

    public void testStackOrder() {
        WorkbenchPage page = (WorkbenchPage) fActivePage;
        try {
            IViewPart part1 = page.showView(MockViewPart.ID);
            IViewPart part2 = page.showView(MockViewPart.ID2);
            IViewPart part3 = page.showView(MockViewPart.ID3);
            IViewPart part4 = page.showView(MockViewPart.ID4);

            IViewPart[] stack = page.getViewStack(part1);
            assertTrue(stack.length == 4);
            assertTrue(stack[0] == part4);
            assertTrue(stack[1] == part3);
            assertTrue(stack[2] == part2);
            assertTrue(stack[3] == part1);

            page.activate(part2);
            stack = page.getViewStack(part1);
            assertTrue(stack.length == 4);
            assertTrue(stack[0] == part2);
            assertTrue(stack[1] == part4);
            assertTrue(stack[2] == part3);
            assertTrue(stack[3] == part1);

            page.activate(part1);
            stack = page.getViewStack(part1);
            assertTrue(stack.length == 4);
            assertTrue(stack[0] == part1);
            assertTrue(stack[1] == part2);
            assertTrue(stack[2] == part4);
            assertTrue(stack[3] == part3);

            page.activate(part3);
            stack = page.getViewStack(part1);
            assertTrue(stack.length == 4);
            assertTrue(stack[0] == part3);
            assertTrue(stack[1] == part1);
            assertTrue(stack[2] == part2);
            assertTrue(stack[3] == part4);
        } catch (PartInitException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test the VIEW_CREATE parameter for showView.  Ensures that the created
     * view is not the active part.
     *
     */
    public void testView_CREATE1() {
        WorkbenchPage page = (WorkbenchPage) fActivePage;
        try {

            page.setPerspective(WorkbenchPlugin.getDefault()
                    .getPerspectiveRegistry().findPerspectiveWithId(
                            "org.eclipse.ui.tests.api.ViewPerspective"));

            //create a part to be active
            IViewPart activePart = page.showView(MockViewPart.ID);
            IViewPart createdPart = page.showView(MockViewPart.ID2, null,
                    IWorkbenchPage.VIEW_CREATE);

            IViewPart[] stack = page.getViewStack(activePart);
            assertEquals(2, stack.length);

            assertEquals(activePart, stack[0]);
            assertEquals(createdPart, stack[1]);

            assertFalse(page.isPartVisible(createdPart));

            assertEquals(activePart, page.getActivePart());
        } catch (PartInitException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test the VIEW_CREATE parameter for showView.  Ensures that the created
     * view is not the active part and is not visible
     */
    public void testView_CREATE2() {
        WorkbenchPage page = (WorkbenchPage) fActivePage;
        try {

            page.setPerspective(WorkbenchPlugin.getDefault()
                    .getPerspectiveRegistry().findPerspectiveWithId(
                            "org.eclipse.ui.tests.api.ViewPerspective"));

            //create a part to be active
            IViewPart activePart = page.showView(MockViewPart.ID3);
            IViewPart createdPart = page.showView(MockViewPart.ID2, null,
                    IWorkbenchPage.VIEW_CREATE);

            IViewPart[] stack = page.getViewStack(createdPart);
            assertEquals(2, stack.length);

            assertEquals(page.findView(MockViewPart.ID), stack[0]);
            assertEquals(createdPart, stack[1]);

            assertFalse(page.isPartVisible(createdPart));

            assertEquals(activePart, page.getActivePart());
        } catch (PartInitException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test the VIEW_CREATE parameter for showView.  Ensures that the created
     * view is not the active part and is visible.
     */
    public void testView_CREATE3() {
        WorkbenchPage page = (WorkbenchPage) fActivePage;
        try {

            page.setPerspective(WorkbenchPlugin.getDefault()
                    .getPerspectiveRegistry().findPerspectiveWithId(
                            "org.eclipse.ui.tests.api.ViewPerspective"));

            //create a part to be active
            IViewPart activePart = page.showView(MockViewPart.ID3);
            IViewPart createdPart = page.showView(MockViewPart.ID4, null,
                    IWorkbenchPage.VIEW_CREATE);

            IViewPart[] stack = page.getViewStack(createdPart);
            assertEquals(1, stack.length);

            assertEquals(createdPart, stack[0]);

            assertTrue(page.isPartVisible(createdPart));

            assertEquals(activePart, page.getActivePart());
        } catch (PartInitException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test the VIEW_VISIBLE parameter for showView, opening the view in the 
     * stack containing the active view.  Ensures that the created view is not 
     * the active part and is not visible.
     */
    public void testView_VISIBLE1() {
        WorkbenchPage page = (WorkbenchPage) fActivePage;
        try {
            page.setPerspective(WorkbenchPlugin.getDefault()
                    .getPerspectiveRegistry().findPerspectiveWithId(
                            "org.eclipse.ui.tests.api.ViewPerspective"));

            //create a part to be active
            IViewPart activePart = page.showView(MockViewPart.ID);
            IViewPart createdPart = page.showView(MockViewPart.ID2, null,
                    IWorkbenchPage.VIEW_VISIBLE);
            IViewPart[] stack = page.getViewStack(activePart);
            assertEquals(2, stack.length);

            assertEquals(activePart, stack[0]);
            assertEquals(createdPart, stack[1]);

            assertFalse(page.isPartVisible(createdPart));

            assertEquals(activePart, page.getActivePart());
        } catch (PartInitException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test the VIEW_VISIBLE parameter for showView, opening the view in its 
     * own stack.  Ensures that the created view is not active part but is the 
     * top part in its stack.
     */
    public void testView_VISIBLE3() {
        WorkbenchPage page = (WorkbenchPage) fActivePage;
        try {
            page.setPerspective(WorkbenchPlugin.getDefault()
                    .getPerspectiveRegistry().findPerspectiveWithId(
                            "org.eclipse.ui.tests.api.ViewPerspective"));

            //create a part to be active
            IViewPart activePart = page.showView(MockViewPart.ID3);

            IViewPart createdPart = page.showView(MockViewPart.ID4, null,
                    IWorkbenchPage.VIEW_VISIBLE);
            IViewPart[] stack = page.getViewStack(createdPart);
            assertEquals(1, stack.length);

            assertEquals(createdPart, stack[0]);

            assertTrue(page.isPartVisible(createdPart));

            assertEquals(activePart, page.getActivePart());
        } catch (PartInitException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test opening a perspective with a fast view.
     */
    public void testOpenPerspectiveWithFastView() {
        WorkbenchPage page = (WorkbenchPage) fActivePage;

        try {
            fWin.getWorkbench().showPerspective(
                    PerspectiveWithFastView.PERSP_ID, fWin);
        } catch (WorkbenchException e) {
            fail("Unexpected WorkbenchException: " + e);
        }

        assertEquals(page.getFastViews().length, 1);
        assertEquals(page.getFastViews()[0].getId(),
                "org.eclipse.ui.views.ResourceNavigator");
        assertEquals(page.getViewReferences().length, 1);
        assertTrue(page.getViewReferences()[0].isFastView());

        ClosePerspectiveAction closePespective = new ClosePerspectiveAction(
                fWin);
        closePespective.run();

    }

    /**
     * Test opening a perspective with placeholders for multi instance views.
     * The placeholders are added at top level (not in any folder).
     * 
     * @since 3.1
     */
    public void testOpenPerspectiveWithMultiViewPlaceholdersAtTopLevel() {
        WorkbenchPage page = (WorkbenchPage) fActivePage;

        try {
            fWin.getWorkbench().showPerspective(
                    PerspectiveWithMultiViewPlaceholdersAtTopLevel.PERSP_ID, fWin);
        } catch (WorkbenchException e) {
            fail("Unexpected WorkbenchException: " + e);
        }

        PerspectiveState state = new PerspectiveState(page);
        ArrayList partIds = state.getPartIds(null);
        assertTrue(partIds.contains("*"));
        assertTrue(partIds.contains(MockViewPart.IDMULT));
        assertTrue(partIds.contains(MockViewPart.IDMULT + ":secondaryId"));
        assertTrue(partIds.contains(MockViewPart.IDMULT + ":*"));
    }

   /**
     * Test opening a perspective with placeholders for multi instance views.
     * The placeholders are added in a placeholder folder.
     * This is a regression test for bug 72383 [Perspectives] Placeholder folder error with multiple instance views
     * 
     * @since 3.1
     */
    public void testOpenPerspectiveWithMultiViewPlaceholdersInPlaceholderFolder() {
        WorkbenchPage page = (WorkbenchPage) fActivePage;

        try {
            fWin.getWorkbench().showPerspective(
                    PerspectiveWithMultiViewPlaceholdersInPlaceholderFolder.PERSP_ID, fWin);
        } catch (WorkbenchException e) {
            fail("Unexpected WorkbenchException: " + e);
        }

        PerspectiveState state = new PerspectiveState(page);
        ArrayList partIds = state.getPartIds("placeholderFolder");
        assertTrue(partIds.contains("*"));
        assertTrue(partIds.contains(MockViewPart.IDMULT));
        assertTrue(partIds.contains(MockViewPart.IDMULT + ":secondaryId"));
        assertTrue(partIds.contains(MockViewPart.IDMULT + ":*"));
    }
    
    /**
     * Test opening a perspective with placeholders for multi instance views.
     * The placeholders are added at top level (not in any folder).
     * 
     * @since 3.1
     */
    public void testOpenPerspectiveWithMultiViewPlaceholdersInFolder() {
        WorkbenchPage page = (WorkbenchPage) fActivePage;

        try {
            fWin.getWorkbench().showPerspective(
                    PerspectiveWithMultiViewPlaceholdersInFolder.PERSP_ID, fWin);
        } catch (WorkbenchException e) {
            fail("Unexpected WorkbenchException: " + e);
        }

        PerspectiveState state = new PerspectiveState(page);
        ArrayList partIds = state.getPartIds("folder");
        assertTrue(partIds.contains("*"));
        assertTrue(partIds.contains(MockViewPart.IDMULT));
        assertTrue(partIds.contains(MockViewPart.IDMULT + ":secondaryId"));
        assertTrue(partIds.contains(MockViewPart.IDMULT + ":*"));
    }

    /**
     * Tests the getNewWizardShortcuts() method.
     *
     * @since 3.1
     */
    public void testGetNewWizardShortcuts() {
        String[] shortcuts = fActivePage.getNewWizardShortcuts();
        assertNotNull(shortcuts);
        assertEquals(0, shortcuts.length);
        
        IWorkbenchWindow win = openTestWindow(IDE.RESOURCE_PERSPECTIVE_ID);
        IWorkbenchPage page = win.getActivePage();
        shortcuts = page.getNewWizardShortcuts();
        List shortcutList = Arrays.asList(shortcuts);
        assertTrue(shortcutList.contains("org.eclipse.ui.wizards.new.folder"));
        assertTrue(shortcutList.contains("org.eclipse.ui.wizards.new.file"));
    }

    /**
     * Tests the getShowViewShortcuts() method.
     *
     * @since 3.1
     */
    public void testGetShowViewShortcuts() {
        String[] shortcuts = fActivePage.getShowViewShortcuts();
        assertNotNull(shortcuts);
        assertEquals(0, shortcuts.length);
        
        IWorkbenchWindow win = openTestWindow(IDE.RESOURCE_PERSPECTIVE_ID);
        IWorkbenchPage page = win.getActivePage();
        shortcuts = page.getShowViewShortcuts();
        List shortcutList = Arrays.asList(shortcuts);
        assertTrue(shortcutList.contains(IPageLayout.ID_RES_NAV));
        assertTrue(shortcutList.contains(IPageLayout.ID_OUTLINE));
        assertTrue(shortcutList.contains(IPageLayout.ID_PROP_SHEET));
        assertTrue(shortcutList.contains(IPageLayout.ID_PROBLEM_VIEW));
    }

    /**
     * Tests the getPerspectiveShortcuts() method.
     *
     * @since 3.1
     */
    public void testGetPerspectiveShortcuts() {
        String[] shortcuts = fActivePage.getPerspectiveShortcuts();
        assertNotNull(shortcuts);
        assertEquals(0, shortcuts.length);
        // not much of a test
    }
    
    /**
     * Tests the getOpenPerspectives() method.
     *
     * @since 3.1
     */
    public void testGetOpenPerspectives() {
        IPerspectiveDescriptor[] openPersps = fActivePage.getOpenPerspectives();
        assertEquals(1, openPersps.length);
        assertEquals(EmptyPerspective.PERSP_ID, openPersps[0].getId());
        
        IPerspectiveRegistry reg = fWorkbench.getPerspectiveRegistry();
        IPerspectiveDescriptor resourcePersp = reg.findPerspectiveWithId(IDE.RESOURCE_PERSPECTIVE_ID);
        fActivePage.setPerspective(resourcePersp);
        openPersps = fActivePage.getOpenPerspectives();
        assertEquals(2, openPersps.length);
        assertEquals(EmptyPerspective.PERSP_ID, openPersps[0].getId());
        assertEquals(IDE.RESOURCE_PERSPECTIVE_ID, openPersps[1].getId());
        
        IPerspectiveDescriptor emptyPersp = reg.findPerspectiveWithId(EmptyPerspective.PERSP_ID);
        fActivePage.setPerspective(emptyPersp);
        openPersps = fActivePage.getOpenPerspectives();
        assertEquals(2, openPersps.length);
        assertEquals(EmptyPerspective.PERSP_ID, openPersps[0].getId());
        assertEquals(IDE.RESOURCE_PERSPECTIVE_ID, openPersps[1].getId());
        
        fActivePage.closeAllPerspectives(false, false);
        openPersps = fActivePage.getOpenPerspectives();
        assertEquals(0, openPersps.length);
        
        fActivePage.close();
        openPersps = fActivePage.getOpenPerspectives();
        assertEquals(0, openPersps.length);
    }

    /**
     * Tests the getSortedPerspectives() method.
     *
     * @since 3.1
     */
    public void testGetSortedPerspectives() {
        IPerspectiveDescriptor[] openPersps = fActivePage.getSortedPerspectives();
        assertEquals(1, openPersps.length);
        assertEquals(EmptyPerspective.PERSP_ID, openPersps[0].getId());
        
        IPerspectiveRegistry reg = fWorkbench.getPerspectiveRegistry();
        IPerspectiveDescriptor resourcePersp = reg.findPerspectiveWithId(IDE.RESOURCE_PERSPECTIVE_ID);
        fActivePage.setPerspective(resourcePersp);
        openPersps = fActivePage.getSortedPerspectives();
        assertEquals(2, openPersps.length);
        assertEquals(EmptyPerspective.PERSP_ID, openPersps[0].getId());
        assertEquals(IDE.RESOURCE_PERSPECTIVE_ID, openPersps[1].getId());
        
        IPerspectiveDescriptor emptyPersp = reg.findPerspectiveWithId(EmptyPerspective.PERSP_ID);
        fActivePage.setPerspective(emptyPersp);
        openPersps = fActivePage.getSortedPerspectives();
        assertEquals(2, openPersps.length);
        assertEquals(IDE.RESOURCE_PERSPECTIVE_ID, openPersps[0].getId());
        assertEquals(EmptyPerspective.PERSP_ID, openPersps[1].getId());
        
        fActivePage.closeAllPerspectives(false, false);
        openPersps = fActivePage.getSortedPerspectives();
        assertEquals(0, openPersps.length);
        
        fActivePage.close();
        openPersps = fActivePage.getSortedPerspectives();
        assertEquals(0, openPersps.length);
    } 

    /**
     * Tests the closePerspective method.
     *
     * @since 3.1
     */
    public void testClosePerspective() {
        // TODO: Need to test variants with saveEditors==true
        
        IPerspectiveRegistry reg = fWorkbench.getPerspectiveRegistry();
        IPerspectiveDescriptor emptyPersp = reg.findPerspectiveWithId(EmptyPerspective.PERSP_ID);
        IPerspectiveDescriptor resourcePersp = reg.findPerspectiveWithId(IDE.RESOURCE_PERSPECTIVE_ID);

        fActivePage.setPerspective(resourcePersp);
        IPerspectiveDescriptor[] openPersps = fActivePage.getOpenPerspectives();
        assertEquals(2, openPersps.length);
        
        fActivePage.closePerspective(resourcePersp, false, false);
        openPersps = fActivePage.getOpenPerspectives();
        assertEquals(1, openPersps.length);
        assertEquals(EmptyPerspective.PERSP_ID, openPersps[0].getId());
        
        fActivePage.closePerspective(emptyPersp, false, false);
        assertEquals(fActivePage, fWin.getActivePage());  // page not closed
        openPersps = fActivePage.getOpenPerspectives();
        assertEquals(0, openPersps.length);
        
        fActivePage.setPerspective(emptyPersp);
        openPersps = fActivePage.getOpenPerspectives();
        assertEquals(1, openPersps.length);

        fActivePage.closePerspective(emptyPersp, false, true);
        openPersps = fActivePage.getOpenPerspectives();
        assertEquals(0, openPersps.length);
        assertNull(fWin.getActivePage());  // page closed
    } 

    /**
     * Tests the closeAllPerspectives method.
     *
     * @since 3.1
     */
    public void testCloseAllPerspectives() {
        // TODO: Need to test variants with saveEditors==true
        
        IPerspectiveRegistry reg = fWorkbench.getPerspectiveRegistry();
        IPerspectiveDescriptor emptyPersp = reg.findPerspectiveWithId(EmptyPerspective.PERSP_ID);
        IPerspectiveDescriptor resourcePersp = reg.findPerspectiveWithId(IDE.RESOURCE_PERSPECTIVE_ID);

        fActivePage.setPerspective(resourcePersp);
        IPerspectiveDescriptor[] openPersps = fActivePage.getOpenPerspectives();
        assertEquals(2, openPersps.length);
        
        fActivePage.closeAllPerspectives(false, false);
        assertEquals(fActivePage, fWin.getActivePage());  // page not closed
        openPersps = fActivePage.getOpenPerspectives();
        assertEquals(0, openPersps.length);

        fActivePage.setPerspective(emptyPersp);
        openPersps = fActivePage.getOpenPerspectives();
        assertEquals(1, openPersps.length);

        fActivePage.closeAllPerspectives(false, true);
        openPersps = fActivePage.getOpenPerspectives();
        assertEquals(0, openPersps.length);
        assertNull(fWin.getActivePage());  // page closed
    } 
    
    /**
     * Regression test for Bug 76285 [Presentations] Folder tab does not indicate current view.
     * Tests that, when switching between perspectives, the remembered old part correctly
     * handles multi-view instances. 
     */
    public void testBug76285() {
        IWorkbenchPage page = fActivePage;
        IPerspectiveDescriptor originalPersp = page.getPerspective();
        IPerspectiveDescriptor resourcePersp = PlatformUI.getWorkbench()
                .getPerspectiveRegistry().findPerspectiveWithId(
                        IDE.RESOURCE_PERSPECTIVE_ID);
        // test requires switching between two different perspectives
        assertNotSame(originalPersp, resourcePersp);
        
        int n = 5;
        IViewPart[] views = new IViewPart[n];
        for (int i = 0; i < n; ++i) {
            try {
                views[i] = page.showView(MockViewPart.IDMULT, Integer
                        .toString(i), IWorkbenchPage.VIEW_CREATE);
            } catch (PartInitException e) {
                fail(e.getMessage());
            }
        }
        assertEquals(5, page.getViews().length);
        for (int i = 0; i < n; ++i) {
            page.activate(views[i]);
            page.setPerspective(resourcePersp);
            assertFalse(page.getActivePart() instanceof MockViewPart);
            page.setPerspective(originalPersp);
            assertEquals(views[i], page.getActivePart());
        }
    }
    
    /**
     * Tests that IShowEditorInput.showEditorInput is called when a
     * matching editor is found during openEditor, and is not called
     * when a new editor is opened.
     * 
     * @since 3.1
     */
    public void testShowEditorInput() throws Exception {
        IWorkbenchPage page = fActivePage;
        proj = FileUtil.createProject("testShowEditorInput");
        IFile file = FileUtil.createFile("a.mock1", proj);
        MockEditorPart part1 = (MockEditorPart) IDE.openEditor(page, file);
        assertFalse(part1.getCallHistory().contains("showEditorInput"));

        MockEditorPart part2 = (MockEditorPart) IDE.openEditor(page, file);
        assertTrue(part1 == part2);
        assertTrue(part2.getCallHistory().contains("showEditorInput"));
    }
    
    /**
     * Tests that the openEditor and findEditor variants that accepts match flags work as expected.
     * 
     * @since 3.2
     */
    public void testOpenAndFindEditorWithMatchFlags() throws Exception {
        IWorkbenchPage page = fActivePage;
        proj = FileUtil.createProject("testOpenEditorMatchFlags");
        IFile file1 = FileUtil.createFile("a.mock1", proj);
        IFile file2 = FileUtil.createFile("a.mock2", proj);
        FileEditorInput input1 = new FileEditorInput(file1);
        FileEditorInput input2 = new FileEditorInput(file2);
        String id1 = MockEditorPart.ID1;
        String id2 = MockEditorPart.ID2;
        
        // first editor (no match)
        MockEditorPart part1 = (MockEditorPart) page.openEditor(input1, id1, true, IWorkbenchPage.MATCH_INPUT);
        assertNotNull(part1);
        
        // same input, same id, matching input (should match part1)
        MockEditorPart part2 = (MockEditorPart) page.openEditor(input1, id1, true, IWorkbenchPage.MATCH_INPUT);
        assertTrue(part1 == part2);
        
        // same input, different id, matching input (should match part1)
        MockEditorPart part3 = (MockEditorPart) page.openEditor(input1, id2, true, IWorkbenchPage.MATCH_INPUT);
        assertTrue(part1 == part3);
        
        // same input, different id, matching input and id (no match)
        MockEditorPart part4 = (MockEditorPart) page.openEditor(input1, id2, true, IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
        assertNotNull(part4);
        assertTrue(part4 != part1);
        
        // same input, same id, matching nothing (no match)
        MockEditorPart part5 = (MockEditorPart) page.openEditor(input1, id1, true, IWorkbenchPage.MATCH_NONE);
        assertNotNull(part5);
        assertTrue(part5 != part1);
        assertTrue(part5 != part4);
        
        // different input, same id, matching id (should match part5 instead of part1, because it was active)
        MockEditorPart part6 = (MockEditorPart) page.openEditor(input2, id1, true, IWorkbenchPage.MATCH_ID);
        assertTrue(part6 == part5);
        
        // different input, different id, matching input and id (no match)
        MockEditorPart part7 = (MockEditorPart) page.openEditor(input2, id2, true, IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
        assertNotNull(part7);
        assertTrue(part7 != part1);
        assertTrue(part7 != part4);
        assertTrue(part7 != part5);

        // At this point, there are 4 editors open: 
        //   part1 (input1, id1), part4 (input1, id2), part5 (input1, id1), and part7 (input2, id2).
        // with part7 active.
        
        // find with MATCH_NONE is always empty
        IEditorReference[] refs = page.findEditors(input1, id1, IWorkbenchPage.MATCH_NONE);
        assertEquals(0, refs.length);

        // find input1 with MATCH_INPUT finds 3 editors: part1, part4 and part5 (in order)
        refs = page.findEditors(input1, null, IWorkbenchPage.MATCH_INPUT);
        assertEquals(3, refs.length);
        assertEquals(part1, refs[0].getPart(true));
        assertEquals(part4, refs[1].getPart(true));
        assertEquals(part5, refs[2].getPart(true));

        // find input2 with MATCH_INPUT finds 1 editor: part7
        refs = page.findEditors(input2, null, IWorkbenchPage.MATCH_INPUT);
        assertEquals(1, refs.length);
        assertEquals(part7, refs[0].getPart(true));
        
        // find id1 with MATCH_ID finds 2 editors: part1 and part5 (in order)
        refs = page.findEditors(null, id1, IWorkbenchPage.MATCH_ID);
        assertEquals(2, refs.length);
        assertEquals(part1, refs[0].getPart(true));
        assertEquals(part5, refs[1].getPart(true));

        // find id2 with MATCH_ID finds 2 editors: part4 and part7 (with part7 first because it was active)
        refs = page.findEditors(null, id2, IWorkbenchPage.MATCH_ID);
        assertEquals(2, refs.length);
        assertEquals(part7, refs[0].getPart(true));
        assertEquals(part4, refs[1].getPart(true));

        // find input1 and id1 with MATCH_INPUT and MATCH_ID finds 2 editors: part1 and part5 (in order)
        refs = page.findEditors(input1, id1, IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
        assertEquals(2, refs.length);
        assertEquals(part1, refs[0].getPart(true));
        assertEquals(part5, refs[1].getPart(true));

        // find input1 and id2 with MATCH_INPUT and MATCH_ID finds 1 editors: part4
        refs = page.findEditors(input1, id2, IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
        assertEquals(1, refs.length);
        assertEquals(part4, refs[0].getPart(true));
    }
}
