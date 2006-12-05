/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.observable.list;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.tests.databinding.observable.ThreadRealm;

/**
 * @since 3.2
 *
 */
public class WritableListTest extends TestCase {
	
	public void testClear() {
		ThreadRealm realm = new ThreadRealm();
		realm.init(Thread.currentThread());
		
		WritableList writableList = new WritableList(realm);
		writableList.add("hello");
		writableList.add("world");
		assertEquals(2, writableList.size());
		writableList.clear();
		assertEquals(0, writableList.size());
	}

}
