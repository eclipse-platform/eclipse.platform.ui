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

import org.eclipse.jface.internal.databinding.internal.swt.SWTProperties;
import org.eclipse.jface.internal.databinding.internal.swt.SpinnerObservableValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

/**
 * @since 3.2
 *
 */
public class SpinnerObservableValueTest extends TestCase {
    public void testSetValueSelection() throws Exception {
        Shell shell = new Shell();
        Spinner spinner = new Spinner(shell, SWT.NONE);
        SpinnerObservableValue observableValue = new SpinnerObservableValue(spinner, SWTProperties.SELECTION);
        
        assertEquals(0, spinner.getSelection());
        assertEquals(0, ((Integer) observableValue.getValue()).intValue());
        
        Integer value = new Integer(1);
        observableValue.setValue(value);
        assertEquals("spinner selection", value.intValue(), spinner.getSelection());
        assertEquals("observable value", value, observableValue.getValue());
        shell.dispose();
    }
}
