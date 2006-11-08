/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import junit.framework.TestCase;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.ArrayUtil;

public class IPerspectiveRegistryTest extends TestCase {

    private IPerspectiveRegistry fReg;

    public IPerspectiveRegistryTest(String testName) {
        super(testName);
    }

    public void setUp() {
        fReg = PlatformUI.getWorkbench().getPerspectiveRegistry();
    }

    public void testFindPerspectiveWithId() {
        IPerspectiveDescriptor pers = (IPerspectiveDescriptor) ArrayUtil
                .pickRandom(fReg.getPerspectives());

        IPerspectiveDescriptor suspect = fReg.findPerspectiveWithId(pers
                .getId());
        assertNotNull(suspect);
        assertEquals(pers, suspect);

        suspect = fReg.findPerspectiveWithId(IConstants.FakeID);
        assertNull(suspect);
    }

    /*	
     public void testFindPerspectiveWithLabel()
     {
     IPerspectiveDescriptor pers = ( IPerspectiveDescriptor )ArrayUtil.pickRandom( fReg.getPerspectives() );
     
     IPerspectiveDescriptor suspect = fReg.findPerspectiveWithLabel( pers.getLabel() );
     assertNotNull( suspect );
     assertEquals( pers, suspect );
     
     suspect = fReg.findPerspectiveWithLabel( IConstants.FakeLabel );
     assertNull( suspect );
     }
     */
    public void testGetDefaultPerspective() {
        String id = fReg.getDefaultPerspective();
        assertNotNull(id);

        IPerspectiveDescriptor suspect = fReg.findPerspectiveWithId(id);
        assertNotNull(suspect);
    }

    public void testSetDefaultPerspective() {
        IPerspectiveDescriptor pers = (IPerspectiveDescriptor) ArrayUtil
                .pickRandom(fReg.getPerspectives());
        fReg.setDefaultPerspective(pers.getId());

        assertEquals(pers.getId(), fReg.getDefaultPerspective());
    }

    public void testGetPerspectives() throws Throwable {
        IPerspectiveDescriptor[] pers = fReg.getPerspectives();
        assertNotNull(pers);

        for (int i = 0; i < pers.length; i++)
            assertNotNull(pers[i]);
    }
    
    public void testDeleteClonedPerspective() {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		String perspId = page.getPerspective().getId() + ".1";
		IPerspectiveDescriptor desc = fReg.clonePerspective(perspId, perspId, page.getPerspective());
		page.setPerspective(desc);
		
		assertNotNull(fReg.findPerspectiveWithId(perspId));
		
		page.closePerspective(desc, false, false);
		fReg.deletePerspective(desc);
		
		assertNull(fReg.findPerspectiveWithId(perspId));
	}
}
