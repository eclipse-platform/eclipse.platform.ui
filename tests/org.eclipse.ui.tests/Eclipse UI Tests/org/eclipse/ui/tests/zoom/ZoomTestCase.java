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
package org.eclipse.ui.tests.zoom;

import junit.framework.Assert;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.PartPane;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.api.MockEditorPart;
import org.eclipse.ui.tests.dnd.DragOperations;
import org.eclipse.ui.tests.dnd.EditorAreaDropTarget;
import org.eclipse.ui.tests.dnd.ExistingWindowProvider;
import org.eclipse.ui.tests.util.FileUtil;
import org.eclipse.ui.tests.util.UITestCase;

public class ZoomTestCase extends UITestCase {
//    protected static final String view1Id = IPageLayout.ID_RES_NAV;
//
//    protected static final String view2Id = IPageLayout.ID_OUTLINE;

    protected WorkbenchWindow window;

    protected WorkbenchPage page;

    protected IProject project;

    protected IFile file1, file2;
    
    protected IEditorPart editor1, editor2, editor3;

    protected IViewPart stackedView1;
    protected IViewPart stackedView2;
    protected IViewPart unstackedView;
    protected IViewPart fastView;

    private IFile file3;
    
    public ZoomTestCase(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.util.UITestCase#doTearDown()
     */
    protected void doTearDown() throws Exception {
        page.testInvariants();
        
        super.doTearDown();
    }
    
    protected void doSetUp() throws Exception {
        super.doSetUp();
        window = (WorkbenchWindow) openTestWindow(ZoomPerspectiveFactory.PERSP_ID);
        page = (WorkbenchPage) window.getActivePage();

        // Disable animations since they occur concurrently and can interferre
        // with locating drop targets
        IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();
        apiStore.setValue(
                IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS,
                false);

        try {
            project = FileUtil.createProject("IEditorPartTest"); //$NON-NLS-1$
            file1 = FileUtil.createFile("Test1.txt", project); //$NON-NLS-1$
            file2 = FileUtil.createFile("Test2.txt", project); //$NON-NLS-1$
            file3 = FileUtil.createFile("Test3.txt", project); //$NON-NLS-1$
            editor1 = page.openEditor(new FileEditorInput(file1),
                    MockEditorPart.ID1);
            editor2 = page.openEditor(new FileEditorInput(file2),
                    MockEditorPart.ID2);
            editor3 = page.openEditor(new FileEditorInput(file3),
                    MockEditorPart.ID2);

            DragOperations
        		.drag(editor3, new EditorAreaDropTarget(new ExistingWindowProvider(window), SWT.RIGHT), false);
        } catch (PartInitException e) {
        } catch (CoreException e) {
        }
        
        stackedView1 = findView(ZoomPerspectiveFactory.STACK1_VIEW1);
        stackedView2 = findView(ZoomPerspectiveFactory.STACK1_VIEW2);
        unstackedView = findView(ZoomPerspectiveFactory.UNSTACKED_VIEW1);
        fastView = findView(ZoomPerspectiveFactory.FASTVIEW1);
    }

    // zooms the given part
    protected void zoom(IWorkbenchPart part) {
        if (part == null)
            throw new NullPointerException();
        page.activate(part);
        page.toggleZoom(((PartSite) part.getSite()).getPane()
                .getPartReference());
        Assert.assertTrue(page.isZoomed());
        Assert.assertTrue(isZoomed(part));
    }

    // open the given file in an editor
    protected void openEditor(IFile file, boolean activate) {
        try {
            if (file == file1)
                editor1 = IDE.openEditor(page, file, activate);
            if (file == file2)
                editor2 = IDE.openEditor(page, file, activate);
        } catch (PartInitException e) {
        }
    }

    protected IViewPart findView(String id) {
        IViewPart view = page.findView(id);
        assertNotNull("View " + id + " not found", view);
        return view;
    }
    
    // show the given view as a regular view
    protected IViewPart showRegularView(String id, int mode) {
        try {
            IViewPart view = page.showView(id, null, mode);
            IViewReference ref = (IViewReference) page.getReference(view);
            if (page.isFastView(ref))
                page.removeFastView(ref);
            return view;
        } catch (PartInitException e) {
        }
        return null;
    }

    // show the given view
    protected IViewPart showFastView(String id) {
        try {
            IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
        	
            int oldMode = store.getInt(IPreferenceConstants.OPEN_VIEW_MODE);
			store.setValue(IPreferenceConstants.OPEN_VIEW_MODE, IPreferenceConstants.OVM_FAST);
			
            IViewPart view = page.showView(id);
            IViewReference ref = (IViewReference) page.getReference(view);
            page.addFastView(ref);
            Assert.assertTrue(page.isFastView(ref));
            store.setValue(IPreferenceConstants.OPEN_VIEW_MODE, oldMode);
            return view;
        } catch (PartInitException e) {
        }
        return null;
    }

    // returns whether this part is zoomed
    protected boolean isZoomed(IWorkbenchPart part) {
        PartSite site = (PartSite) part.getSite();
        PartPane pane = site.getPane();
        
        return pane.isZoomed();
    }
    
    /**
     * Asserts that the given part is zoomed. If the part is null, asserts
     * that no parts are zoomed.
     * 
     * @param part
     * @since 3.1
     */
    protected void assertZoomed(IWorkbenchPart part) {
        if (part == null) {
            Assert.assertFalse("Page should not be zoomed", isZoomed());
        } else {
            // Assert that the part is zoomed
            Assert.assertTrue("Expecting " + partName(part) + " to be zoomed", isZoomed(part));
            // Assert that the page is zoomed (paranoia check)
            Assert.assertTrue("Page should be zoomed", isZoomed());
        }
    }

    /**
     * Asserts that the given part is active.
     * 
     * @param part
     * @since 3.1
     */
    protected void assertActive(IWorkbenchPart part) {
        IWorkbenchPart activePart = page.getActivePart();
        
        // Assert that the part is active
        Assert.assertTrue("Unexpected active part: expected " + partName(part) 
                + " and found " + partName(activePart), activePart == part);
        
        // If the part is an editor, assert that the editor is active
        if (part instanceof IEditorPart) {
            assertActiveEditor((IEditorPart)part);
        }
    }
    
    protected String partName(IWorkbenchPart part) {
        if (part == null) {
            return "null";
        }
        
        return Util.safeString(part.getTitle());
    }
    
    protected void assertActiveEditor(IEditorPart part) {
        IWorkbenchPart activeEditor = page.getActiveEditor();
        
        Assert.assertTrue("Unexpected active editor: expected " + partName(part) 
                + " and found " + partName(activeEditor), activeEditor == part);        
    }
    
    // returns true if the page is not zoomed, false otherwise
    protected boolean isZoomed() {
        return page.isZoomed();
    }
    
    public void close(IWorkbenchPart part) {
        if (part instanceof IViewPart) {
            page.hideView((IViewPart)part);
        } else if (part instanceof IEditorPart) {
            page.closeEditor((IEditorPart)part, false);
        }
    }
}
