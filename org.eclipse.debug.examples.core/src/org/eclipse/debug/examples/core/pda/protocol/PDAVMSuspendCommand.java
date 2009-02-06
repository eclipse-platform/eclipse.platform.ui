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
 * Suspends the execution of the whole virtual machine 
 * 
 * <pre>
 *    C: vmsuspend
 *    R: ok
 *    E: vmsuspended client
 *    
 * Errors:
 *    error: thread already suspended
 * </pre>
 */

public class PDAVMSuspendCommand extends PDACommand {

    public PDAVMSuspendCommand() {
        super("vmsuspend");
    }
    

    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
