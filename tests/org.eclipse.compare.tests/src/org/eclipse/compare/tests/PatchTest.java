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

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipse.compare.internal.patch.Diff;
import org.eclipse.compare.internal.patch.LineReader;
import org.eclipse.compare.internal.patch.WorkspacePatcher;

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
	
	//Test creation of new workspace patch 
	public void testWorkspacePatch_Create(){
		//Note the order that exists in the array of expected results is based purely on the order of the files in the patch 
		patchWorkspace(new String[]{"addition.txt", "addition.txt"}, "patch_workspacePatchAddition.txt", new String[] { "exp_workspacePatchAddition2.txt","exp_workspacePatchAddition.txt"}, false);   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}
	
	//Test applying the reverse of workspace creation patch 
	public void testWorkspacePatch_Create_Reverse(){
		//Note the order that exists in the array of expected results is based purely on the order of the files in the patch 
		patchWorkspace(new String[]{"exp_workspacePatchAddition2.txt","exp_workspacePatchAddition.txt"}, "patch_workspacePatchAddition.txt", new String[] {"addition.txt", "addition.txt"}, true);   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}
	
	//Test the patching of an already existing file, the creation of a new one and the deletion of elements in a file
	public void testWorkspacePatch_Modify(){
		//Note the order that exists in the array of expected results is based purely on the order of the files in the patch 
		patchWorkspace(new String[]{"exp_workspacePatchAddition2.txt","exp_workspacePatchAddition.txt", "addition.txt"}, "patch_workspacePatchMod.txt", new String[] { "exp_workspacePatchMod1.txt","exp_workspacePatchMod2.txt", "exp_workspacePatchMod3.txt"}, false );   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
	}
	
	//Test applying the reverse of a workspace modify patch
	public void testWorkspacePatch_Modify_Reverse(){
		//Note the order that exists in the array of expected results is based purely on the order of the files in the patch 
		patchWorkspace(new String[]{ "exp_workspacePatchMod1.txt","exp_workspacePatchMod2.txt", "exp_workspacePatchMod3.txt"}, "patch_workspacePatchMod.txt", new String[] {"exp_workspacePatchAddition2.txt","exp_workspacePatchAddition.txt", "addition.txt"}, true );   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
	}
	
	//Tests the deletion of an already existing file, and the modification of another file
	public void testWorkspacePatch_Delete(){
		//Note the order that exists in the array of expected results is based purely on the order of the files in the patch 
		patchWorkspace(new String[]{"exp_workspacePatchMod2.txt","addition.txt", "exp_workspacePatchMod1.txt","addition.txt"}, "patch_workspacePatchDelete.txt", new String[] { "addition.txt","exp_workspacePatchDelete2.txt", "addition.txt", "exp_workspacePatchDelete1.txt"}, false );   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
	}
	
	//Test applying the reverse of a workspace deletion patch
	public void testWorkspacePatch_Delete_Reverse(){
		//Note the order that exists in the array of expected results is based purely on the order of the files in the patch 
		patchWorkspace(new String[]{"addition.txt","exp_workspacePatchDelete2.txt", "addition.txt", "exp_workspacePatchDelete1.txt" }, "patch_workspacePatchDelete.txt", new String[] {"exp_workspacePatchMod2.txt","addition.txt", "exp_workspacePatchMod1.txt","addition.txt"}, true );   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
	}
	
	
	//Test changing 
	private BufferedReader getReader(String name) {
		InputStream resourceAsStream= getClass().getResourceAsStream("patchdata/" + name); //$NON-NLS-1$
		InputStreamReader reader2= new InputStreamReader(resourceAsStream);
		return new BufferedReader(reader2);
	}

	private void patch(String old, String patch, String expt) {
		
		LineReader lr= new LineReader(getReader(old));
		List inLines= lr.readLines();

		WorkspacePatcher patcher= new WorkspacePatcher();
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
	
	/**
	 * Parses a workspace patch and applies the diffs to the appropriate files
	 * @param originalFiles
	 * @param patch
	 * @param expectedOutcomeFiles
	 * @p
	 */
	private void patchWorkspace(String[] originalFiles, String patch, String[] expectedOutcomeFiles, boolean reverse) {
		
		//ensure that we have the same number of input files as we have expected files
		Assert.assertEquals(originalFiles.length, expectedOutcomeFiles.length);
		
		//Parse the passed in patch and extract all the Diffs
		WorkspacePatcher patcher= new WorkspacePatcher();
		try {
			patcher.parse(getReader(patch));
			patcher.setReversed(reverse);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Sort the diffs by project 
		Diff[] diffs= patcher.getDiffs();
		
		//Iterate through all of the original files, apply the diffs that belong to the file and compare
		//with the corresponding outcome file
		for (int i = 0; i < originalFiles.length; i++) {	
			LineReader lr= new LineReader(getReader(originalFiles[i]));
			List inLines= lr.readLines();
			
		
			List failedHunks= new ArrayList();
			patcher.patch(diffs[i], inLines, failedHunks);
			
			LineReader expectedContents= new LineReader(getReader(expectedOutcomeFiles[i]));
			List expectedLines= expectedContents.readLines();
			
			Object[] expected= expectedLines.toArray();
			Object[] result= inLines.toArray();
			
			Assert.assertEquals(expected.length, result.length);
			
			for (int j= 0; j < expected.length; j++)
				Assert.assertEquals(expected[j], result[j]);
		}
	}
}
