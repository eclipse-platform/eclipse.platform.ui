/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms;

import org.eclipse.swt.SWT;

public class Policy {


    /**
     * Returns the message with arg substitued in message. This is only being
     * called from SWT.
     * 
     * @param msg
     *            the message to do substitution in. Note that message is
     *            expected to be already NLS'ed.
     * @param arg
     *            substitution args
     * @return
     */
    public static String getMessage(String msg, String arg) {
        return getMessage(msg, new Object[] { arg });
    }


    public static String getMessage(String msg, Object[] args) {
        if (msg == null || args == null) {
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        }
        return Messages.bind(msg, args);
    }
}
