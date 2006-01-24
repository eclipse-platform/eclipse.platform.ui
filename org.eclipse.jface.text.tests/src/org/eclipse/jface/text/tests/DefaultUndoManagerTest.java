/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jface.text.DefaultUndoManager;
import org.eclipse.jface.text.IUndoManager;

/**
 * Tests for DefaultUndoManager.
 *
 * @since 3.2
 */
public class DefaultUndoManagerTest extends AbstractUndoManagerTest {

	public static Test suite() {
		return new TestSuite(DefaultUndoManagerTest.class);
	}
	
	/*
	 * @see TestCase#TestCase(String)
	 */
	public DefaultUndoManagerTest(final String name) {
		super(name);	
	}
	
	/*
	 * @see org.eclipse.jface.text.tests.AbstractUndoManagerTest#createUndoManager(int)
	 * @since 3.2
	 */
	protected IUndoManager createUndoManager(int maxUndoLevel) {
		return new DefaultUndoManager(maxUndoLevel);
	}
	
}
