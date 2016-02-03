/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests.rules;

import org.junit.Test;

import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

public class DefaultPartitionerTest extends FastPartitionerTest {
	@Override
	protected IDocumentPartitioner createPartitioner(IPartitionTokenScanner scanner) {
		return new DefaultPartitioner(scanner, new String[] { DEFAULT, COMMENT });
	}

	@Override
	@Test
	public void testPR130900() throws Exception {
		System.out.println("Bug130900 not fixed in DefaultPartitioner");
	}
}
