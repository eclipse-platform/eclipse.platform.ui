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


/**
 * Set the contents of variable <code>variable</code> from the control stack 
 * <code>frame_number</code> to value <code>value</code> (the control stack is 
 * indexed from 0, 0 being the oldest). 
 * 
 * <pre>
 *    C: setvar {thread_id} {frame_number} {variable} {value}
 *    R: ok
 *    
 * Errors:
 *    error: invalid thread
 * </pre>
 */

public class PDASetVarCommand extends PDACommand {

    public PDASetVarCommand(int threadId, int frame, String variable, String value) {
        super("setvar " + threadId + " " + frame + " " + variable + " " + value); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
    

    @Override
	public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
