/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.ant.editor;

/**********************************************************************
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
//
// Copyright:
// GEBIT Gesellschaft fuer EDV-Beratung
// und Informatik-Technologien mbH, 
// Berlin, Duesseldorf, Frankfurt (Germany) 2002
// All rights reserved.
//

/**
 * Exception that might be thrown in the Planty context.
 * 
 * @version 18.09.2002
 * @author Alf Schiefelbein
 */
public class PlantyException extends RuntimeException {

    /**
     * Constructor for PlantyException.
     */
    public PlantyException() {
        super();
    }

    /**
     * Constructor for PlantyException.
     * @param s
     */
    public PlantyException(String s) {
        super(s);
    }

}