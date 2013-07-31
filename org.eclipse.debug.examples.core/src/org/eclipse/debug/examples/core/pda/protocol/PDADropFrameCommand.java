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
 * Pops the top stack frame off the call stack setting the instruction pointer to 
 * the calling statement in the calling frame 
 * 
 * <pre>
 * If VM running:
 *    C: drop {thread_id}
 *    R: ok
 *    E: resumed {thread_id} drop
 *    E: suspended {thread_id} drop
 *    
 * If VM suspended:
 *    C: drop {thread_id}
 *    R: ok
 *    E: vmresumed drop
 *    E: vmsuspended {thread_id} drop
 *    
 * Errors:
 *    error: invalid thread
 * </pre>
 */
public class PDADropFrameCommand extends PDACommand {

    public PDADropFrameCommand(int threadId) {
        super("drop " + threadId); //$NON-NLS-1$
    }
    
    @Override
	public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
