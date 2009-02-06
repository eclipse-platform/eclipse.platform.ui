/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.examples.core.pda.protocol;


/**
 * Return the contents of variable <code>variable_name</code> in the control 
 * stack frame <code>frame_number</code> (stack frames are indexed from 0, 0 
 * being the oldest). 
 * 
 * <pre>
 *    C: var  {thread_id} {frame_number} {variable_name}
 *    R: {variable_value}
 *    
 * Errors:
 *    error: invalid thread
 *    error: variable undefined
 * </pre>
 */

public class PDAVarCommand extends PDACommand {

    public PDAVarCommand(int threadId, int frameId, String name) {
        super("var " + threadId + " " + frameId + " " + name);
    }
    

    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
