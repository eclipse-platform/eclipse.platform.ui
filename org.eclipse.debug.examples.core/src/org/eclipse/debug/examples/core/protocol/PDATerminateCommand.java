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
 * Instructs the debugger to terminate.
 * 
 * <pre>
 *    C: terminate
 *    R: ok
 *    E: vmterminated
 * </pre>
 */

public class PDATerminateCommand extends PDACommand {

    public PDATerminateCommand() {
        super("terminate");
    }
    

    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
