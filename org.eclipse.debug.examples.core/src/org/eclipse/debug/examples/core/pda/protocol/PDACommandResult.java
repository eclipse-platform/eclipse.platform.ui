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
 * Basic command result object.  This command result simply allows access to the 
 * PDA response.  Sub-classes may override to optionally parse the response text
 * and return higher-level objects.
 */
public class PDACommandResult {

    final public String fResponseText;
    
    public PDACommandResult(String response) {
        fResponseText = response;
    }
}
