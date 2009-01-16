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
package org.eclipse.debug.examples.core.protocol;


/**
 * Resumes the execution of a single thread.  Can be issued only if the virtual 
 * machine is running.
 * 
 * <pre>
 *    C: resume {thread_id}
 *    R: ok
 *    E: resumed {thread_id} client
 *    
 * Errors:
 *    error: invalid thread
 *    error: cannot resume thread when vm is suspended
 *    error: thread already running
 * </pre>
 */

public class PDAResumeCommand extends PDACommand {

    public PDAResumeCommand(int threadId) {
        super("resume " + threadId);
    }
    

    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
