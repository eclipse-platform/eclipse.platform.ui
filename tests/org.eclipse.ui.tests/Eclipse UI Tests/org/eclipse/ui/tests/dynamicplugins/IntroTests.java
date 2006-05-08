/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.intro.IIntroDescriptor;
import org.eclipse.ui.internal.intro.IIntroRegistry;
import org.eclipse.ui.internal.intro.IntroDescriptor;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.tests.leaks.LeakTests;

/**
 * @since 3.1
 */
public class IntroTests extends DynamicTestCase {

	private static final String PRODUCT_ID = "org.eclipse.ui.tests.someProduct";
	private static final String INTRO_ID = "org.eclipse.newIntro1.newIntro1";
	private IntroDescriptor oldDesc;
	private IWorkbenchWindow window;
	/**
	 * @param testName
	 */
	public IntroTests(String testName) {
		super(testName);
	}
	
	public void testIntroClosure() {	
		getBundle();
		Workbench workbench = Workbench.getInstance();
        IntroDescriptor testDesc = (IntroDescriptor) WorkbenchPlugin
        .getDefault().getIntroRegistry().getIntro(
        		INTRO_ID);
        workbench.setIntroDescriptor(testDesc);

		ReferenceQueue queue = new ReferenceQueue();
		IIntroPart intro = workbench.getIntroManager().showIntro(window, false);
		WeakReference ref = new WeakReference(intro, queue);
        assertNotNull(intro);
        intro = null; //null the reference
        removeBundle();
        try {
			LeakTests.checkRef(queue, ref);
		} catch (Exception e) {
			fail(e.getMessage());
		} 
        assertNull(workbench.getIntroManager().getIntro());
	}
	
	public void testIntroProperties() {
		IIntroRegistry registry = WorkbenchPlugin.getDefault().getIntroRegistry();
		assertNull(registry.getIntroForProduct(PRODUCT_ID));
		assertNull(registry.getIntro(INTRO_ID));
		getBundle();
		assertNotNull(registry.getIntroForProduct(PRODUCT_ID));
		IIntroDescriptor desc = registry.getIntro(INTRO_ID);
		assertNotNull(desc);
		try {
			testIntroProperties(desc);
		}
		catch (CoreException e) {
			fail(e.getMessage());
		}
		removeBundle();	
		assertNull(registry.getIntro(INTRO_ID));
		assertNull(registry.getIntroForProduct(PRODUCT_ID));
		try {
			testIntroProperties(desc);
			fail();		
		}
		catch (CoreException e) {
			fail(e.getMessage());
		}
		catch (RuntimeException e) {			
		}
	}

	/**
	 * @param desc
	 * @throws CoreException
	 */
	private void testIntroProperties(IIntroDescriptor desc) throws CoreException {
		assertNotNull(desc.getId());
		try {
			assertNotNull(desc.createIntro());
		}
		catch (CoreException e) {
			e.printStackTrace();
			throw e;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionId()
	 */
	protected String getExtensionId() {		
		return "newIntro1.testDynamicIntroAddition";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionPoint()
	 */
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_INTRO;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getInstallLocation()
	 */
	protected String getInstallLocation() {
		return "data/org.eclipse.newIntro1";
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.util.UITestCase#doSetUp()
     */
    protected void doSetUp() throws Exception {
        super.doSetUp();
        oldDesc = Workbench.getInstance().getIntroDescriptor();
        window = openTestWindow();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.util.UITestCase#doTearDown()
     */
    protected void doTearDown() throws Exception {
        super.doTearDown();
        Workbench.getInstance().setIntroDescriptor(oldDesc);
    }    
    
	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getMarkerClass()
	 */
	protected String getMarkerClass() {
		return "org.eclipse.ui.dynamic.DynamicIntro";
	}
}
