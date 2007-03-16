/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jface.viewers.StructuredSelection;

public class StructuredSelectionTest extends TestCase {

    public StructuredSelectionTest(String name) {
        super(name);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(StructuredSelectionTest.class);
    }

    public void testEquals() {
        String element = "A selection";
        StructuredSelection sel1 = new StructuredSelection(element);
        StructuredSelection sel2 = new StructuredSelection(element);
        assertEquals(sel1, sel2);
    }

    public void testEquals2() {
        String element1 = "A selection";
        String element2 = "A selection";
        String element3 = "Other";
        StructuredSelection sel1 = new StructuredSelection(element1);
        StructuredSelection sel2 = new StructuredSelection(element2);
        StructuredSelection sel3 = new StructuredSelection(element3);
        assertEquals(sel1, sel2);
        assertTrue(!sel1.equals(sel3));
    }

    public void testEquals3() { // two empty selections
        StructuredSelection empty1 = new StructuredSelection();
        StructuredSelection empty2 = new StructuredSelection();
        assertTrue(empty1.equals(empty2));
        assertTrue(empty2.equals(empty1));
    }

    public void testEquals4() { // empty selection with non-empty selection
        StructuredSelection sel = new StructuredSelection("A selection");
        StructuredSelection empty = new StructuredSelection();
        assertTrue(!sel.equals(empty));
        assertTrue(!empty.equals(sel));
    }

    public void testEquals5() { // equality is order-dependent
        List l1 = new ArrayList();
        l1.add("element 1");
        l1.add("element 2");

        List l2 = new ArrayList();
        l2.add("element 2");
        l2.add("element 1");

        StructuredSelection sel1 = new StructuredSelection(l1);
        StructuredSelection sel2 = new StructuredSelection(l2);
        assertTrue(!sel1.equals(sel2));
        assertTrue(!sel2.equals(sel1));
    }

    public void testEquals6() { // different selections
        List l1 = new ArrayList();
        l1.add("element 1");
        l1.add("element 2");

        List l2 = new ArrayList();
        l2.add("element 2");
        l2.add("element 3");
        l2.add("element 1");

        StructuredSelection sel1 = new StructuredSelection(l1);
        StructuredSelection sel2 = new StructuredSelection(l2);
        assertTrue(!sel1.equals(sel2));
        assertTrue(!sel2.equals(sel1));
    }

    /**
     * Empty selections via different constructors.
     * Regression test for bug 40245.
     */
    public void testEquals7() {
        StructuredSelection empty1 = new StructuredSelection();
        StructuredSelection empty2 = new StructuredSelection(new Object[0]);
        assertTrue(empty1.equals(empty2));
        assertTrue(empty2.equals(empty1));
    }
}
