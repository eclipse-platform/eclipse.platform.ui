/*******************************************************************************
 * Copyright (c) 2002, 2003 GEBIT Gesellschaft fuer EDV-Beratung
 * und Informatik-Technologien mbH, 
 * Berlin, Duesseldorf, Frankfurt (Germany).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     GEBIT Gesellschaft fuer EDV-Beratung und Informatik-Technologien mbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.externaltools.internal.ant.editor;

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