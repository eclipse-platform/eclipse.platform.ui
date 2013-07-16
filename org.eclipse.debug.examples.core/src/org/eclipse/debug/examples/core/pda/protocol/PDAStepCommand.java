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
 * Executes next instruction 
 * 
 * <pre>
 * If VM running:
 *    C: step {thread_id}
 *    R: ok
 *    E: resumed {thread_id} step
 *    E: suspended {thread_id} step
 *    
 * If VM suspended:
 *    C: step {thread_id}
 *    R: ok
 *    E: vmresumed step
 *    E: vmsuspended {thread_id} step
 *    
 * Errors:
 *    error: invalid thread
 * </pre>
 */

public class PDAStepCommand extends PDACommand {

    public PDAStepCommand(int threadId) {
        super("step " + threadId); //$NON-NLS-1$
    }
    

    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
