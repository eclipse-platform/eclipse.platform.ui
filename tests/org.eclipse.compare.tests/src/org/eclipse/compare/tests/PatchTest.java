/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.tests;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.internal.patch.*;
import org.eclipse.compare.patch.*;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class PatchTest extends TestCase {

	
	class StringStorage implements IStorage {
		String fileName;
		public StringStorage(String old) {
			fileName = old;
		}
		public Object getAdapter(Class adapter) {
			return null;
		}
		public boolean isReadOnly() {
			return false;
		}
		public String getName() {
			return fileName;
		}
		public IPath getFullPath() {
			return null;
		}
		public InputStream getContents() throws CoreException {
			return new BufferedInputStream(asInputStream(fileName));
		}
	
	}
	
	public PatchTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		// empty
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testCreatePatch() throws CoreException, IOException {
		patch("addition.txt", "patch_addition.txt", "exp_addition.txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	public void testUnterminatedCreatePatch() throws CoreException, IOException {
		patch("addition.txt", "patch_addition2.txt", "exp_addition2.txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testContext0Patch() throws CoreException, IOException {
		patch("context.txt", "patch_context0.txt", "exp_context.txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testContext1Patch() throws CoreException, IOException {
		patch("context.txt", "patch_context1.txt", "exp_context.txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testContext3Patch() throws CoreException, IOException {
		patch("context.txt", "patch_context3.txt", "exp_context.txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	public void testContext3PatchWithHeader() throws CoreException, IOException {
		patch("context.txt", "patch_context3_header.txt", "exp_context.txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		IStorage patchStorage = new StringStorage("patch_context3_header.txt");
		IFilePatch[] patches = ApplyPatchOperation.parsePatch(patchStorage);
		String header = patches[0].getHeader();
		LineReader reader = new LineReader(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(header.getBytes()))));
		List lines = reader.readLines();
		List expected = new ArrayList();
		expected.add("Index: old.txt\n");
		expected.add("UID: 42\n");
		assertEquals(Patcher.createString(false, expected), Patcher.createString(false, lines));
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
		InputStream resourceAsStream = asInputStream(name);
		InputStreamReader reader2= new InputStreamReader(resourceAsStream);
		return new BufferedReader(reader2);
	}

	private InputStream asInputStream(String name) {
		InputStream resourceAsStream= getClass().getResourceAsStream("patchdata/" + name); //$NON-NLS-1$
		return resourceAsStream;
	}

	private void patch(final String old, String patch, String expt) throws CoreException, IOException {
		patcherPatch(old, patch, expt);
		filePatch(old, patch, expt);
	}

	private void filePatch(final String old, String patch, String expt) throws CoreException, IOException {
		LineReader lr= new LineReader(getReader(expt));
		List inLines= lr.readLines();
		String expected = Patcher.createString(false, inLines);
		
		IStorage oldStorage = new StringStorage(old);
		IStorage patchStorage = new StringStorage(patch);
		IFilePatch[] patches = ApplyPatchOperation.parsePatch(patchStorage);
		assertTrue(patches.length == 1);
		IFilePatchResult result = patches[0].apply(oldStorage, new PatchConfiguration(), null);
		assertTrue(result.hasMatches());
		assertFalse(result.hasRejects());
		InputStream actualStream = result.getPatchedContents();
		String actual = asString(actualStream);
		assertEquals(expected, actual);
	}

	private String asString(InputStream exptStream) throws IOException {
		return Utilities.readString(exptStream, ResourcesPlugin.getEncoding());
	}

	private void patcherPatch(String old, String patch, String expt) {
		LineReader lr= new LineReader(getReader(old));
		List inLines= lr.readLines();

		WorkspacePatcher patcher= new WorkspacePatcher();
		try {
			patcher.parse(getReader(patch));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		FileDiff[] diffs= patcher.getDiffs();
		Assert.assertEquals(diffs.length, 1);
		
		FileDiffResult diffResult = patcher.getDiffResult(diffs[0]);
		diffResult.patch(inLines, null);
		
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
	 * @param reverse
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
		FileDiff[] diffs= patcher.getDiffs();
		
		//Iterate through all of the original files, apply the diffs that belong to the file and compare
		//with the corresponding outcome file
		for (int i = 0; i < originalFiles.length; i++) {	
			LineReader lr= new LineReader(getReader(originalFiles[i]));
			List inLines= lr.readLines();
			
		
			FileDiffResult diffResult = patcher.getDiffResult(diffs[i]);
			diffResult.patch(inLines, null);
			
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
