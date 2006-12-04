/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.internal.swt;

import junit.framework.TestCase;

import org.eclipse.jface.internal.databinding.internal.swt.ControlObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.SWTProperties;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.2
 *
 */
public class ControlObservableValueTest extends TestCase {
    public void testSetValue() throws Exception {
        Shell shell = new Shell();
        ControlObservableValue observableValue = new ControlObservableValue(shell, SWTProperties.ENABLED);
        assertTrue(shell.isEnabled());
        
        Boolean value = Boolean.FALSE;
        observableValue.setValue(value);
        assertFalse("shell enabled", shell.isEnabled());
        assertEquals("observable value", value, observableValue.getValue());
        shell.dispose();
    }
}
