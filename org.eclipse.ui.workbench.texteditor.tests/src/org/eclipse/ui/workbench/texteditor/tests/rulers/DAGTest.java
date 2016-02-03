/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.tests.rulers;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import org.eclipse.ui.internal.texteditor.rulers.DAG;

/**
 * @since 3.3
 */
public class DAGTest {
	private static final Object A= "A";
	private static final Object B= "B";
	private static final Object C= "C";
	private static final Object D= "D";
	private static final Set<Object> AS= Collections.singleton(A);
	private static final Set<Object> BS= Collections.singleton(B);
	private static final Set<Object> CS= Collections.singleton(C);
	private static final Set<Object> AD= new LinkedHashSet<>(Arrays.asList(new Object[] { A, D }));
	private static final Set<Object> CD= new LinkedHashSet<>(Arrays.asList(new Object[] { C, D }));
	private static final Set<Object> ACD= new LinkedHashSet<>(Arrays.asList(new Object[] { A, C, D }));
	private static final Set<Object> BD= new LinkedHashSet<>(Arrays.asList(new Object[] { B, D }));

	private DAG<Object> fDag= new DAG<>();

	@Test
	public void testEmpty() throws Exception {
		assertTrue(fDag.getChildren(new Object()).isEmpty());
		assertTrue(fDag.getSources().isEmpty());
		assertTrue(fDag.getSinks().isEmpty());
	}
	
	@Test
	public void testIllegal() throws Exception {
		assertFalse(fDag.addEdge(A, A));
		try {
			fDag.addEdge(A, null);
			fail();
		} catch (RuntimeException x) {
		}
		try {
			fDag.addEdge(null, A);
			fail();
		} catch (RuntimeException x) {
		}
		try {
			fDag.addEdge(null, null);
			fail();
		} catch (RuntimeException x) {
		}
		try {
			fDag.addVertex(null);
			fail();
		} catch (RuntimeException x) {
		}
	}
	
	@Test
	public void testDag() throws Exception {
		assertTrue(fDag.addEdge(A, B));
		assertEquals(AS, fDag.getSources());
		assertEquals(BS, fDag.getSinks());
		assertFalse(fDag.addEdge(B, A));
		assertEquals(AS, fDag.getSources());
		assertEquals(BS, fDag.getSinks());
		assertEquals(BS, fDag.getChildren(A));
		assertTrue(fDag.getChildren(B).isEmpty());
		assertTrue(fDag.getChildren(C).isEmpty());
		assertTrue(fDag.getChildren(D).isEmpty());

		assertTrue(fDag.addEdge(B, C));
		assertEquals(AS, fDag.getSources());
		assertEquals(CS, fDag.getSinks());
		assertEquals(BS, fDag.getChildren(A));
		assertEquals(CS, fDag.getChildren(B));
		assertTrue(fDag.getChildren(C).isEmpty());
		assertTrue(fDag.getChildren(D).isEmpty());

		fDag.addVertex(C);
		assertEquals(AS, fDag.getSources());
		assertEquals(CS, fDag.getSinks());
		assertEquals(BS, fDag.getChildren(A));
		assertEquals(CS, fDag.getChildren(B));
		assertTrue(fDag.getChildren(C).isEmpty());
		assertTrue(fDag.getChildren(D).isEmpty());

		fDag.addVertex(D);
		assertEquals(AD, fDag.getSources());
		assertEquals(CD, fDag.getSinks());
		assertEquals(BS, fDag.getChildren(A));
		assertEquals(CS, fDag.getChildren(B));
		assertTrue(fDag.getChildren(C).isEmpty());
		assertTrue(fDag.getChildren(D).isEmpty());

		fDag.removeVertex(A);
		assertEquals(BD, fDag.getSources());
		assertEquals(CD, fDag.getSinks());
		assertTrue(fDag.getChildren(A).isEmpty());
		assertEquals(CS, fDag.getChildren(B));
		assertTrue(fDag.getChildren(C).isEmpty());
		assertTrue(fDag.getChildren(D).isEmpty());

		assertTrue(fDag.addEdge(A, B));
		assertTrue(fDag.addEdge(D, B));
		assertEquals(AD, fDag.getSources());
		assertEquals(CS, fDag.getSinks());
		assertEquals(BS, fDag.getChildren(A));
		assertEquals(CS, fDag.getChildren(B));
		assertTrue(fDag.getChildren(C).isEmpty());
		assertEquals(BS, fDag.getChildren(D));

		fDag.removeVertex(B);
		assertEquals(ACD, fDag.getSources());
		assertEquals(ACD, fDag.getSinks());
		assertTrue(fDag.getChildren(A).isEmpty());
		assertTrue(fDag.getChildren(B).isEmpty());
		assertTrue(fDag.getChildren(C).isEmpty());
		assertTrue(fDag.getChildren(D).isEmpty());
	}
}
