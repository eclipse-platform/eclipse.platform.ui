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

package org.eclipse.ant.tests.ui.support.inputHandlers;

import org.eclipse.ant.internal.ui.antsupport.inputhandler.SWTInputHandler;


public class TestSWTInputHandler extends SWTInputHandler {
	
   
    /* (non-Javadoc)
     * @see org.eclipse.ant.internal.ui.antsupport.inputhandler.SWTInputHandler#open(java.lang.String, java.lang.String, boolean[])
     */
    protected void open(String title, String prompt, boolean[] result) {
        fRequest.setInput("TestSWTInputHandler");
        result[0]= true;
    }
}