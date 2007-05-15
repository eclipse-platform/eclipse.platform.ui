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

import org.eclipse.jface.internal.databinding.internal.swt.TableSingleSelectionObservableValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * @since 3.2
 */
public class TableObservableValueTest extends TestCase {
    public void testSetValue() throws Exception {
        Shell shell = new Shell();
        Table table = new Table(shell, SWT.NONE);
        TableSingleSelectionObservableValue observableValue = new TableSingleSelectionObservableValue(table);
        new TableItem(table, SWT.NONE);
        
        assertEquals(-1, table.getSelectionIndex());
        assertEquals(-1, ((Integer) observableValue.getValue()).intValue());
        
        Integer value = new Integer(0);
        observableValue.setValue(value);
        assertEquals("table selection index", value.intValue(), table.getSelectionIndex());
        assertEquals("observable value", value, observableValue.getValue());
        shell.dispose();
    }
}
