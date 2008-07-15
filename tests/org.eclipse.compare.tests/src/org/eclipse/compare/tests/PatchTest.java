/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.tests;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.internal.core.patch.FileDiff;
import org.eclipse.compare.internal.core.patch.FileDiffResult;
import org.eclipse.compare.internal.core.patch.LineReader;
import org.eclipse.compare.internal.patch.WorkspacePatcher;
import org.eclipse.compare.patch.ApplyPatchOperation;
import org.eclipse.compare.patch.IFilePatch;
import org.eclipse.compare.patch.IFilePatchResult;
import org.eclipse.compare.patch.PatchConfiguration;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

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
	
	class FileStorage implements IStorage {
		File file;
		public FileStorage(File file) {
			this.file = file;
		}
		public InputStream getContents() throws CoreException {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				// ignore, should never happen
			}
			return null;
		}
		public IPath getFullPath() {
			return new Path(file.getAbsolutePath());
		}
		public String getName() {
			return file.getName();
		}
		public boolean isReadOnly() {
			return true;
		}
		public Object getAdapter(Class adapter) {
			return null;
		}
	}
	
	class JarEntryStorage implements IStorage {
		JarEntry jarEntry;
		JarFile jarFile;
		public JarEntryStorage(JarEntry jarEntry, JarFile jarFile) {
			this.jarEntry = jarEntry;
			this.jarFile = jarFile;
		}
		public InputStream getContents() throws CoreException {
			try {
				return jarFile.getInputStream(jarEntry);
			} catch (IOException e) {
				// ignore, should never happen
			}
			return null;
		}
		public IPath getFullPath() {
			// TODO: is it enough?
			return new Path(jarFile.getName());
		}
		public String getName() {
			return jarEntry.getName();
		}
		public boolean isReadOnly() {
			return true;
		}
		public Object getAdapter(Class adapter) {
			return null;
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
		assertEquals(LineReader.createString(false, expected), LineReader.createString(false, lines));
	}
	
	public void testDateUnknown() throws CoreException {
		IStorage patchStorage = new StringStorage("patch_dateunknown.txt");
		IFilePatch[] patches = ApplyPatchOperation.parsePatch(patchStorage);
		assertEquals(IFilePatch.DATE_UNKNOWN, patches[0].getBeforeDate());
		assertEquals(IFilePatch.DATE_UNKNOWN, patches[0].getAfterDate());
	}
	
	public void testDateError() throws CoreException {
		IStorage patchStorage = new StringStorage("patch_dateerror.txt");
		IFilePatch[] patches = ApplyPatchOperation.parsePatch(patchStorage);
		assertEquals(IFilePatch.DATE_UNKNOWN, patches[0].getBeforeDate());
		assertEquals(IFilePatch.DATE_UNKNOWN, patches[0].getAfterDate());
	}
	
	public void testDateKnown() throws CoreException {
		IStorage patchStorage = new StringStorage("patch_datevalid.txt");
		IFilePatch[] patches = ApplyPatchOperation.parsePatch(patchStorage);
		assertFalse(IFilePatch.DATE_UNKNOWN == patches[0].getBeforeDate());
		assertFalse(IFilePatch.DATE_UNKNOWN == patches[0].getAfterDate());
	}
	
	//Test creation of new workspace patch 
	public void testWorkspacePatch_Create(){
		//Note the order that exists in the array of expected results is based purely on the order of the files in the patch 
		patchWorkspace(new String[]{"addition.txt", "addition.txt"}, "patch_workspacePatchAddition.txt", new String[] { "exp_workspacePatchAddition2.txt","exp_workspacePatchAddition.txt"}, false, 0);   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}
	
	//Test applying the reverse of workspace creation patch 
	public void testWorkspacePatch_Create_Reverse(){
		//Note the order that exists in the array of expected results is based purely on the order of the files in the patch 
		patchWorkspace(new String[]{"exp_workspacePatchAddition2.txt","exp_workspacePatchAddition.txt"}, "patch_workspacePatchAddition.txt", new String[] {"addition.txt", "addition.txt"}, true, 0);   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}
	
	//Test the patching of an already existing file, the creation of a new one and the deletion of elements in a file
	public void testWorkspacePatch_Modify(){
		//Note the order that exists in the array of expected results is based purely on the order of the files in the patch 
		patchWorkspace(new String[]{"exp_workspacePatchAddition2.txt","exp_workspacePatchAddition.txt", "addition.txt"}, "patch_workspacePatchMod.txt", new String[] { "exp_workspacePatchMod1.txt","exp_workspacePatchMod2.txt", "exp_workspacePatchMod3.txt"}, false, 0 );   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
	}
	
	//Test applying the reverse of a workspace modify patch
	public void testWorkspacePatch_Modify_Reverse(){
		//Note the order that exists in the array of expected results is based purely on the order of the files in the patch 
		patchWorkspace(new String[]{ "exp_workspacePatchMod1.txt","exp_workspacePatchMod2.txt", "exp_workspacePatchMod3.txt"}, "patch_workspacePatchMod.txt", new String[] {"exp_workspacePatchAddition2.txt","exp_workspacePatchAddition.txt", "addition.txt"}, true, 0 );   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
	}
	
	//Tests the deletion of an already existing file, and the modification of another file
	public void testWorkspacePatch_Delete(){
		//Note the order that exists in the array of expected results is based purely on the order of the files in the patch 
		patchWorkspace(new String[]{"exp_workspacePatchMod2.txt","addition.txt", "exp_workspacePatchMod1.txt","addition.txt"}, "patch_workspacePatchDelete.txt", new String[] { "addition.txt","exp_workspacePatchDelete2.txt", "addition.txt", "exp_workspacePatchDelete1.txt"}, false, 0 );   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
	}
	
	//Test applying the reverse of a workspace deletion patch
	public void testWorkspacePatch_Delete_Reverse(){
		//Note the order that exists in the array of expected results is based purely on the order of the files in the patch 
		patchWorkspace(new String[]{"addition.txt","exp_workspacePatchDelete2.txt", "addition.txt", "exp_workspacePatchDelete1.txt" }, "patch_workspacePatchDelete.txt", new String[] {"exp_workspacePatchMod2.txt","addition.txt", "exp_workspacePatchMod1.txt","addition.txt"}, true, 0 );   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
	}
	
	// Keeps track of the failures
	private List failures = new ArrayList();
	
	public void testPatchdataSubfolders() throws IOException, CoreException {
		URL patchdataFolderUrl = getClass().getResource("patchdata");
		patchdataFolderUrl = FileLocator.resolve(patchdataFolderUrl);
		
		Map mapOfFilenames = null;
		if (patchdataFolderUrl.getProtocol().equals("file")) {
			mapOfFilenames = extractNamesForFileProtocol(patchdataFolderUrl);
		} else if (patchdataFolderUrl.getProtocol().equals("jar")) {
			mapOfFilenames = extractNamesForJarProtocol(patchdataFolderUrl);	
		} else {
			// TODO: silently return or loudly fail?
			fail("Unknown protocol");
		}
		
		//TODO: silently return or loudly fail?
		assertNotNull(mapOfFilenames);
		
		for (Iterator iterator = mapOfFilenames.keySet().iterator(); iterator
				.hasNext();) {
			
			String subfolder = (String) iterator.next();
			String[] filenames = (String[]) mapOfFilenames.get(subfolder);
			
			// create a message to distinguish tests from different subfolders
			String msg = "Test for subfolder [patchdata/" + subfolder
					+ "] failed.";

			// get the fuzz factor for the patch if provided
			// TODO: what if fuzz > 3
			String patch = filenames[1].substring(subfolder.length());
			int fuzz = -1;
			if (patch.indexOf("fuzz3") > -1 || patch.indexOf("f3") > -1)
				fuzz = 3;
			if (patch.indexOf("fuzz2") > -1 || patch.indexOf("f2") > -1)
				fuzz = 2;
			if (patch.indexOf("fuzz1") > -1 || patch.indexOf("f1") > -1)
				fuzz = 1;
			if (patch.indexOf("fuzz0") > -1 || patch.indexOf("f0") > -1)
				fuzz = 0;
			
			try {
				// test with expected result
				patchWorkspace(msg, new String[] { filenames[0] },
						filenames[1], new String[] { filenames[2] }, false,
						fuzz);
			} catch (AssertionFailedError e) {
				failures.add(e);
			}

			// test with actual result, should fail
			if (filenames[3] != null) {
				try {
					patchWorkspace(msg, new String[] { filenames[0] },
							filenames[1], new String[] { filenames[3] }, false,
							fuzz);
				} catch (AssertionFailedError e) {
					// a failure is expected
					continue; // continue with a next subfolder
				}
				failures.add(new AssertionFailedError(
						"\npatchWorkspace should fail for file ["
								+ filenames[3] + "] in folder [patchdata/"
								+ subfolder + "]."));
			}
		}
		
		if (failures.isEmpty())
			return;

		if (failures.size() == 1)
			throw (AssertionFailedError) failures.get(0);

		StringBuffer sb = new StringBuffer(
				"Failures occured while testing data from patchdata subfolder (Please check log for further details):");
		for (Iterator iterator = failures.iterator(); iterator.hasNext();) {
			AssertionFailedError error = (AssertionFailedError) iterator.next();
			log("org.eclipse.compare.tests", error);
			sb.append("\n" + error.getMessage());
		}
		throw new AssertionFailedError(sb.toString());
	}
	
	// both copy-pasted from CoreTest
	
	private void log(String pluginID, IStatus status) {
		Platform.getLog(Platform.getBundle(pluginID)).log(status);
	}
	
	private void log(String pluginID, Throwable e) {
		log(pluginID, new Status(IStatus.ERROR, pluginID, IStatus.ERROR, "Error", e)); //$NON-NLS-1$
	}
	
	/**
	 * @param url
	 * @return A map with subfolder name as a key and an array of filenames as a
	 *         value (e.g. <code>"bug12345" -> { "bug12345/file.txt", 
	 *         "bug12345/patch.txt", "bug12345/expected.txt", 
	 *         "bug12345/actual.txt" }</code>).
	 *         The last value in the array can be <code>null</code> as testing
	 *         against actual result is optional.
	 * @throws IOException
	 * @throws CoreException
	 */
	private Map extractNamesForJarProtocol(URL url) throws IOException,
			CoreException {
		JarURLConnection conn = (JarURLConnection) url.openConnection();
		JarFile jarFile = conn.getJarFile();
		
		// look for the patchdata folder entry
		String patchdataName = null;
		Enumeration entries1 = jarFile.entries();
		while (entries1.hasMoreElements()) {
			JarEntry entry = (JarEntry) entries1.nextElement();
			String entryName = entry.getName();
			if (entryName.endsWith("/patchdata/")) {
				patchdataName = entryName;
				break;
			}
		}
		// patchdata folder not found
		if (patchdataName == null)
			return null;
		// System.out.println("patchdataName : " + patchdataName);
		
		// look for files in patchdata subfolders
		Map mapOfSubfolders = new HashMap();
		Enumeration entries = jarFile.entries();
		while (entries.hasMoreElements()) {
			JarEntry entry = (JarEntry) entries.nextElement();
			String entryName = entry.getName();
			if (!entryName.equals(patchdataName) &&  entryName.startsWith(patchdataName)) {
				// a subfolder found
				if (!entryName.endsWith("/")) {
					// file within a subfolder of 'patchdata' folder
					String relativePath = entryName.substring(patchdataName.length());
					
					StringTokenizer st = new StringTokenizer(relativePath, "/");
					if (st.countTokens() != 2) 
						continue; // accept only files in a direct subfolder
					
					String subfolder = st.nextToken();
					String filename = st.nextToken();
					
					if (filename.indexOf("patch") > -1) {
						assertTrue(ApplyPatchOperation
								.isPatch(new JarEntryStorage(entry, jarFile)));
						String[] names = (String[]) mapOfSubfolders
								.get(subfolder);
						if (names == null)
							mapOfSubfolders.put(subfolder, new String[] { null,
									relativePath, null, null });
						else
							names[1] = relativePath;
					} else if (filename.indexOf("exp") > -1) {
						String[] names = (String[]) mapOfSubfolders
								.get(subfolder);
						if (names == null)
							mapOfSubfolders.put(subfolder, new String[] { null,
									null, relativePath, null });
						else
							names[2] = relativePath;
					} else if (filename.indexOf("act") > -1) {
						String[] names = (String[]) mapOfSubfolders
								.get(subfolder);
						if (names == null)
							mapOfSubfolders.put(subfolder, new String[] { null,
									null, null, relativePath });
						else
							names[3] = relativePath;
					} else {
						String[] names = (String[]) mapOfSubfolders
								.get(subfolder);
						if (names == null)
							mapOfSubfolders.put(subfolder, new String[] {
									relativePath, null, null, null });
						else
							names[0] = relativePath;
					}
				}
			}
		}
		return mapOfSubfolders;
	}
	
	private Map extractNamesForFileProtocol(URL patchdataFolderUrl)
			throws CoreException {

		Map result = new HashMap();

		IPath patchdataFolderPath = new Path(patchdataFolderUrl.getPath());
		File patchdataFolderFile = patchdataFolderPath.toFile();
		assertTrue(patchdataFolderFile.isDirectory());
		File[] listOfSubfolders = patchdataFolderFile
				.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return pathname.isDirectory();
					}
				});
		for (int i = 0; i < listOfSubfolders.length; i++) {
			File subfolder = listOfSubfolders[i];
			File[] files = subfolder.listFiles();
			File patchFile = null;
			File fileToPatch = null;
			File fileWithExpectedResult = null;
			File fileWithActualResult = null; // optional
			for (int j = 0; j < files.length; j++) {
				File file = files[j];
				String filename = file.getName();
				if (filename.indexOf("patch") > -1) {
					assertTrue(ApplyPatchOperation
							.isPatch(new FileStorage(file)));
					patchFile = file;
				} else if (filename.indexOf("exp") > -1) {
					fileWithExpectedResult = file;
				} else if (filename.indexOf("act") > -1) {
					fileWithActualResult = file;
				} else {
					fileToPatch = file;
				}
			}
			
			// make the paths relative
			String fileToPatchString = fileToPatch.getPath().substring(
					patchdataFolderFile.getPath().length() + 1);
			String patchFileString = patchFile.getPath().substring(
					patchdataFolderFile.getPath().length() + 1);
			String fileWithExpectedResultString = fileWithExpectedResult
					.getPath().substring(
							patchdataFolderFile.getPath().length() + 1);
			String fileWithActualResultString = null;
			if (fileWithActualResult != null)
				fileWithActualResultString = fileWithActualResult.getPath()
						.substring(patchdataFolderFile.getPath().length() + 1);

			result.put(subfolder.getName(), new String[] { fileToPatchString,
					patchFileString, fileWithExpectedResultString,
					fileWithActualResultString });
		}
		return result;
	}
	
	// Test changing
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
		String expected = LineReader.createString(false, inLines);
		
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
	
	private void patchWorkspace(String[] originalFiles, String patch,
			String[] expectedOutcomeFiles, boolean reverse,
			int fuzzFactor) {
		patchWorkspace(null, originalFiles, patch, expectedOutcomeFiles,
				reverse, fuzzFactor);
	}
	
	/**
	 * Parses a workspace patch and applies the diffs to the appropriate files
	 * 
	 * @param msg
	 * @param originalFiles
	 * @param patch
	 * @param expectedOutcomeFiles
	 * @param reverse
	 * @param fuzzFactor
	 *            The fuzz factor to use, ranging from 0 (all context must
	 *            match) to 2 (the default maximum fuzz factor). <code>-1</code>
	 *            means that the fuzz factor should be calculated automatically.
	 */
	private void patchWorkspace(String msg, String[] originalFiles, String patch, String[] expectedOutcomeFiles, boolean reverse, int fuzzFactor) {
		
		//ensure that we have the same number of input files as we have expected files
		Assert.assertEquals(originalFiles.length, expectedOutcomeFiles.length);
		
		// Parse the passed in patch and extract all the Diffs
		WorkspacePatcher patcher = new WorkspacePatcher();
		try {
			patcher.getConfiguration().setFuzz(fuzzFactor);
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
			
			Assert.assertEquals(msg, expected.length, result.length);
			
			for (int j= 0; j < expected.length; j++)
				Assert.assertEquals(msg, expected[j], result[j]);
		}
	}
}
