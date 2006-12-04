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

import org.eclipse.jface.internal.databinding.internal.swt.ComboObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.SWTProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.2
 * 
 */
public class ComboObservableValueTest extends TestCase {
    public void testSetValueText() throws Exception {
        Shell shell = new Shell();
        Combo combo = new Combo(shell, SWT.NONE);
        ComboObservableValue observableValue = new ComboObservableValue(combo, SWTProperties.TEXT);
        assertEquals("", combo.getText());
        assertEquals("", observableValue.getValue());

        String value = "value";
        observableValue.setValue(value);
        assertEquals("combo text", value, combo.getText());
        assertEquals("observable value", value, observableValue.getValue());
        shell.dispose();
    }
}
