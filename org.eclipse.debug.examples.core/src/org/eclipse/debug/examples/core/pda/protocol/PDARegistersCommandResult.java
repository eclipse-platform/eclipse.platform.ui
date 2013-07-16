/*******************************************************************************
 * Copyright (c) 2008, 2013 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     IBM Corporation - bug fixing
 *******************************************************************************/
package org.eclipse.debug.examples.core.pda.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * @see PDARegistersCommand
 */

public class PDARegistersCommandResult extends PDACommandResult {
    
    /**
     * Array of registers returned by the registers commands.  
     */
    final public PDARegisterData[] fRegisters;
    
    PDARegistersCommandResult(String response) {
        super(response);
        StringTokenizer st = new StringTokenizer(response, "#"); //$NON-NLS-1$
        List regList = new ArrayList();
        
        while (st.hasMoreTokens()) {
            regList.add(new PDARegisterData(st.nextToken()));
        }
        fRegisters = (PDARegisterData[])regList.toArray(new PDARegisterData[regList.size()]);
    }
}
