/*******************************************************************************
 * Copyright (c) 2008, 2013 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.examples.core.pda.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Object representing a register in the registers command results.
 * 
 * @see PDARCommand 
 */

public class PDARegisterData {

    final public String fName;
    final public boolean fWritable;
    final public PDABitFieldData[] fBitFields;
    
    PDARegisterData(String regString) {
        StringTokenizer st = new StringTokenizer(regString, "|"); //$NON-NLS-1$
        
        String regInfo = st.nextToken();
        StringTokenizer regSt = new StringTokenizer(regInfo, " "); //$NON-NLS-1$
        fName = regSt.nextToken();
        fWritable = Boolean.getBoolean(regSt.nextToken());
        
        List bitFieldsList = new ArrayList();
        while (st.hasMoreTokens()) {
            bitFieldsList.add(new PDABitFieldData(st.nextToken()));
        }
        fBitFields = (PDABitFieldData[])bitFieldsList.toArray(new PDABitFieldData[bitFieldsList.size()]);
    }
}