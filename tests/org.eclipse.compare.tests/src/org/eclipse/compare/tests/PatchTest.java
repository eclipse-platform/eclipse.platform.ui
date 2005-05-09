/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.internal.patch.Diff;
import org.eclipse.compare.internal.patch.Patcher;
import org.eclipse.compare.internal.patch.LineReader;

import junit.framework.Assert;
import junit.framework.TestCase;

public class PatchTest extends TestCase {

	public PatchTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		// empty
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testCreatePatch() {
		patch("addition.txt", "patch_addition.txt", "exp_addition.txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testUnterminatedCreatePatch() {
		patch("addition.txt", "patch_addition2.txt", "exp_addition2.txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testContext0Patch() {
		patch("context.txt", "patch_context0.txt", "exp_context.txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testContext1Patch() {
		patch("context.txt", "patch_context1.txt", "exp_context.txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testContext3Patch() {
		patch("context.txt", "patch_context3.txt", "exp_context.txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	private BufferedReader getReader(String name) {
		InputStream resourceAsStream= getClass().getResourceAsStream("patchdata/" + name); //$NON-NLS-1$
		InputStreamReader reader2= new InputStreamReader(resourceAsStream);
		return new BufferedReader(reader2);
	}

	private void patch(String old, String patch, String expt) {
		
		LineReader lr= new LineReader(getReader(old));
		List inLines= lr.readLines();

		Patcher patcher= new Patcher();
		try {
			patcher.parse(getReader(patch));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Diff[] diffs= patcher.getDiffs();
		Assert.assertEquals(diffs.length, 1);
		
		List failedHunks= new ArrayList();
		patcher.patch(diffs[0], inLines, failedHunks);
		
		LineReader expectedContents= new LineReader(getReader(expt));
		List expectedLines= expectedContents.readLines();
		
		Object[] expected= expectedLines.toArray();
		Object[] result= inLines.toArray();
		
		Assert.assertEquals(expected.length, result.length);
		
		for (int i= 0; i < expected.length; i++)
			Assert.assertEquals(expected[i], result[i]);
	}
}
