/*******************************************************************************
 * Copyright (c) 2005,2025 IBM Corporation and others.
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
package org.eclipse.jface.text.tests.rules;

import org.junit.Test;

import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

@Deprecated
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
