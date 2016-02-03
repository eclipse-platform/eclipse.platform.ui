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
package org.eclipse.text.tests;

import org.eclipse.jface.text.GapTextStore;
import org.eclipse.jface.text.ITextStore;

public class GapTextStoreTest extends TextStoreTest {

	@Override
	protected ITextStore createTextStore() {
		return new GapTextStore(256, 4096, 0.1f);
	}

}
