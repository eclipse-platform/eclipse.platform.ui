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
package org.eclipse.ui.tests.dynamicplugins;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.Category;
import org.eclipse.ui.internal.registry.ViewDescriptor;
import org.eclipse.ui.internal.registry.ViewRegistry;
import org.eclipse.ui.tests.leaks.LeakTests;
import org.eclipse.ui.views.IStickyViewDescriptor;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

/**
 * Tests to ensure the addition of new views with dynamic plug-ins.
 */
public class ViewTests extends DynamicTestCase {

    private static final String VIEW_ID = "org.eclipse.newView1.newView1";
    private static final String CATEGORY_ID = "org.eclipse.newView1.newCategory1";
    
	public ViewTests(String testName) {
        super(testName);
    }
    
	public void testViewClosure() throws CoreException {
		IWorkbenchWindow window = openTestWindow(IDE.RESOURCE_PERSPECTIVE_ID);
		getBundle();
				
		ReferenceQueue queue = new ReferenceQueue();
		IViewPart part = window.getActivePage().showView(VIEW_ID);
		// we need to ensure that the view is closed in all open perspectives but this is not currently possible.
		// window.getActivePage().setPerspective(WorkbenchPlugin.getDefault().getPerspectiveRegistry().findPerspectiveWithId(EmptyPerspective.PERSP_ID2));
		WeakReference ref = new WeakReference(part, queue);
        assertNotNull(part);
        part = null; //null the reference

		removeBundle();
		try {
			LeakTests.checkRef(queue, ref);
		} catch (Exception e) {
			fail(e.getMessage());
		} 
		
        assertNull(window.getActivePage().findView(VIEW_ID));		
	}
    
    /**
     * Tests to ensure that the showView handler is removed when the plugin is unloaded.
     */
    public void testHandlerRemoval() {
        IViewRegistry registry = WorkbenchPlugin.getDefault().getViewRegistry();
        
        assertNull(registry.find(VIEW_ID));
        getBundle();
        ViewDescriptor desc = (ViewDescriptor) registry.find(VIEW_ID);
        assertNotNull(desc);
        HandlerSubmission submission = desc.getHandlerSubmission();
        ICommand command = PlatformUI.getWorkbench().getCommandSupport().getCommandManager().getCommand(submission.getCommandId());
        assertTrue(command.isHandled());
        removeBundle();
        assertFalse(command.isHandled());
    }

	public void testViewProperties() {
		IViewRegistry registry = WorkbenchPlugin.getDefault().getViewRegistry();
		
		assertNull(registry.find(VIEW_ID));
		getBundle();
		IViewDescriptor desc = registry.find(VIEW_ID);
		assertNotNull(desc);
	    
		testViewProperties(desc);
		removeBundle();	
		assertNull(registry.find(VIEW_ID));
		try {
			testViewProperties(desc);
			fail();		
		}
		catch (RuntimeException e) {	
			// no-op
		}
	}
	
	public void testStickyViewProperties() {
		ViewRegistry registry = (ViewRegistry)WorkbenchPlugin.getDefault().getViewRegistry();
		IStickyViewDescriptor [] descs = registry.getStickyViews();
		for (int i = 0; i < descs.length; i++) {
			assertFalse(VIEW_ID.equals(descs[i].getId()));
		}
		
		getBundle();
		
		descs = registry.getStickyViews();
		IStickyViewDescriptor desc = null;
		for (int i = 0; i < descs.length; i++) {
			if (VIEW_ID.equals(descs[i].getId())) {
				desc = descs[i];
				break;
			}					
		}
		assertNotNull(desc);
		testStickyViewProperties(desc);
		removeBundle();	
		
		descs = registry.getStickyViews();
		for (int i = 0; i < descs.length; i++) {
			assertFalse(VIEW_ID.equals(descs[i].getId()));
		}
		
		try {
			testStickyViewProperties(desc);
			fail();			
		}
		catch (RuntimeException e) {	
			// no-op
		}		
	}
	
	private void testStickyViewProperties(IStickyViewDescriptor desc) {
		assertNotNull(desc.getId());
		assertFalse(desc.isMoveable());
		assertFalse(desc.isCloseable());
		assertEquals(IPageLayout.BOTTOM, desc.getLocation());		
	}

	public void testCategoryViewContainmentProperties() {
		ViewRegistry registry = (ViewRegistry)WorkbenchPlugin.getDefault().getViewRegistry();
		
		assertNull(registry.find(VIEW_ID));
		assertNull(registry.findCategory(CATEGORY_ID));

		getBundle();
		
		IViewDescriptor desc = registry.find(VIEW_ID);
		assertNotNull(desc);	    
		Category category = registry.findCategory(CATEGORY_ID);
		assertNotNull(category);
		
		testCategoryProperties(category);		
		assertTrue(category.getElements().contains(desc));

		removeBundle();	
		assertNull(registry.find(VIEW_ID));
		assertNull(registry.findCategory(CATEGORY_ID));
		try {
			testCategoryProperties(category);
			fail();
		}
		catch (RuntimeException e) {	
			// no-op
		}
	
	}	
	
	/**
	 * @param category
	 */
	private void testCategoryProperties(Category desc) {
		assertNotNull(desc.getId());
		assertNotNull(desc.getLabel());
		assertEquals(1, desc.getElements().size());
	}

	/**
	 * @param desc
	 */
	private void testViewProperties(IViewDescriptor desc) {
		assertNotNull(desc.getId());
		assertNotNull(desc.getLabel());
		assertNotNull(desc.getImageDescriptor());
		assertNotNull(desc.getDescription());
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionId()
	 */
	protected String getExtensionId() {
		return "newView1.testDynamicViewAddition";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionPoint()
	 */
	protected String getExtensionPoint() {
		return IWorkbenchConstants.PL_VIEWS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getInstallLocation()
	 */
	protected String getInstallLocation() {
		return "data/org.eclipse.newView1";
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getMarkerClass()
	 */
	protected String getMarkerClass() {
		return "org.eclipse.ui.dynamic.DynamicView";
	}
}
