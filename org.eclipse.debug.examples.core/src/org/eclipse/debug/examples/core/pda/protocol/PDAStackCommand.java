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
 * Return the contents of the control stack (program counters, function and
 * variable names). The reply is control stack from oldest to newest as a single string
 * <code>frame#frame#frame...#frame</code>, where each frame is a string
 * <code>"filename|pc|function name|variable name|variable name|...|variable name"</code></li>. 
 * 
 * <pre>
 *    C: stack {thread_id}
 *    R: {file}|{line}|{function}|{var_1}|{var_2}|...#{file}|{line}|{function}|{var_1}|{var_2}|...#...
 *    
 * Errors:
 *    error: invalid thread
 * </pre>
 */

public class PDAStackCommand extends PDACommand {

    public PDAStackCommand(int threadId) {
        super("stack " + threadId); //$NON-NLS-1$
    }
    

    public PDACommandResult createResult(String resultText) {
        return new PDAStackCommandResult(resultText);
    }
}
