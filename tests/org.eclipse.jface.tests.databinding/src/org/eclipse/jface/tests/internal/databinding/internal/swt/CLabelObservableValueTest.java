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

import org.eclipse.jface.internal.databinding.internal.swt.CLabelObservableValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.2
 *
 */
public class CLabelObservableValueTest extends TestCase {
    public void testSetValue() throws Exception {
        Shell shell = new Shell();
        CLabel label = new CLabel(shell, SWT.NONE);
        CLabelObservableValue observableValue = new CLabelObservableValue(label);
        
        assertEquals(null, label.getText());
        assertEquals(null, observableValue.getValue());
        
        String value = "value";
        observableValue.setValue(value);
        assertEquals("label value", value, label.getText());
        assertEquals("observable value was incorrect", value, observableValue.getValue());
        shell.dispose();
    }
}
