//
// PlantyException.java
//
// Copyright:
// GEBIT Gesellschaft fuer EDV-Beratung
// und Informatik-Technologien mbH, 
// Berlin, Duesseldorf, Frankfurt (Germany) 2002
// All rights reserved.
//
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