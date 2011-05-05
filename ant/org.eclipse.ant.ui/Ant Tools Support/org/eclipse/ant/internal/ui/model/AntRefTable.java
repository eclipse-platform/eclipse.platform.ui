/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.model;

import java.util.Hashtable;

import org.apache.tools.ant.UnknownElement;

/**
 * This class has been copied in its entirety from the static
 * inner class AntRefTable from {@link org.apache.tools.ant.Project}
 */
public class AntRefTable extends Hashtable {

	private static final long serialVersionUID = 1L;

	AntRefTable() {
        super();
    }

    /** 
     * Returns the unmodified original object.
     * This method should be called internally to
     * get the &quot;real&quot; object.
     * The normal get method will do the replacement
     * of UnknownElement (this is similar with the JDNI
     * refs behavior).
     */
    private Object getReal(Object key) {
        return super.get(key);
    }

    /** 
     * Get method for the reference table.
     *  It can be used to hook dynamic references and to modify
     * some references on the fly--for example for delayed
     * evaluation.
     *
     * It is important to make sure that the processing that is
     * done inside is not calling get indirectly.
     *
     * @param key lookup key.
     * @return mapped value.
     */
    public synchronized Object get(Object key) {
        Object o = getReal(key);
        if (o instanceof UnknownElement) {
            // Make sure that
            UnknownElement ue = (UnknownElement) o;
            ue.maybeConfigure();
            o = ue.getRealThing();
        }
        return o;
    }
}