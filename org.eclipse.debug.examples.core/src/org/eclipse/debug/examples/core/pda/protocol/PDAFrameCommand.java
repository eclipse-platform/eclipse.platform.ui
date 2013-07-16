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
 * Retrieves command stack frame information for frame <code>frame_number</code>
 * (stack frames are indexed from 0, 0 being the oldest).  
 * 
 * <pre>
 *    C: stack {thread_id} {frame_number}
 *    R: {file}|{line}|{function}|{var_1}|{var_2}|...
 *    
 * Errors:
 *    error: invalid thread
 * </pre>
 */
public class PDAFrameCommand extends PDACommand {

    public PDAFrameCommand(int threadId, int frameNum) {
        super("frame " + threadId + " " + frameNum); //$NON-NLS-1$ //$NON-NLS-2$
    }
    

    public PDACommandResult createResult(String resultText) {
        return new PDAFrameCommandResult(resultText);
    }
}
