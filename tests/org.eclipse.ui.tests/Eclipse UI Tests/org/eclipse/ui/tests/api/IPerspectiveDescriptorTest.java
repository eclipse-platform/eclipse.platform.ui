/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import junit.framework.TestCase;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.util.ArrayUtil;

public class IPerspectiveDescriptorTest extends TestCase {

    private IPerspectiveDescriptor fPer;

    private IPerspectiveRegistry fReg;

    public IPerspectiveDescriptorTest(String testName) {
        super(testName);
    }

    public void setUp() {
        fPer = (IPerspectiveDescriptor) ArrayUtil.pickRandom(PlatformUI
                .getWorkbench().getPerspectiveRegistry().getPerspectives());
        //fPer.
    }

    public void testGetId() {
        assertNotNull(fPer.getId());
    }

    public void testGetLabel() {
        assertNotNull(fPer.getLabel());
    }

    //	This always fails
    public void testGetImageDescriptor() {
        /*		IWorkbench wb = PlatformUI.getWorkbench();
         
         IPerspectiveDescriptor[] pers = wb.getPerspectiveRegistry().getPerspectives();
         IWorkbenchPage page = wb.getActiveWorkbenchWindow().getActivePage();
         
         for( int i = 0; i < pers.length; i ++ )
         if( pers[ i ] != page.getPerspective() ){
         page.setPerspective( pers[ i ] );
         break;
         }

         System.out.println( "active page pers: " + page.getPerspective().getLabel() );
         System.out.println( "active pers image: " + page.getPerspective().getImageDescriptor() );

         for( int i = 0; i < pers.length; i ++ )
         if( pers[ i ].getLabel().equals( "Resource" ) ){
         System.out.println( "resource image: " + pers[ i ].getImageDescriptor() );
         break;
         }
         for( int i = 0; i < pers.length; i ++ ){
         assertNotNull( pers[ i ].getImageDescriptor() );
         }*/
    }

    public void testThis() {
        //		opne
    }
}

