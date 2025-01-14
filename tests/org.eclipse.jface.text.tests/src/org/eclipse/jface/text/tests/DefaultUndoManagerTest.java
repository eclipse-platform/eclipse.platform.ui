/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import org.eclipse.jface.text.DefaultUndoManager;
import org.eclipse.jface.text.IUndoManager;

/**
 * Tests for DefaultUndoManager.
 *
 * @since 3.2
 */
public class DefaultUndoManagerTest extends AbstractUndoManagerTest {

	@SuppressWarnings("removal")
	@Override
	protected IUndoManager createUndoManager(int maxUndoLevel) {
		return new DefaultUndoManager(maxUndoLevel);
	}

}
