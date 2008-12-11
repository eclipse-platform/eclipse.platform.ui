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
import java.io.StringReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

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
import org.osgi.framework.Bundle;

public class PatchTest extends TestCase {

	private static final String PATCHDATA = "patchdata";
	private static final String PATCH_CONFIGURATION = "patchConfiguration.properties";
	
	Properties defaultPatchProperties;
	
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
	
	class PatchTestConfiguration {
		String subfolderName;
		PatchConfiguration pc;
		String patchFileName;
		String[] originalFileNames;
		String[] expectedFileNames;
		String[] actualFileNames;
		// TODO: getters, setters
	}
	
	public PatchTest(String name) {
		super(name);
		defaultPatchProperties = new Properties();
		defaultPatchProperties.setProperty("patchFile", "patch.txt");
		defaultPatchProperties.setProperty("contextFile", "context.txt");
		defaultPatchProperties.setProperty("expectedResultFile", "exp_context.txt");
		defaultPatchProperties.setProperty("fuzzFactor", "-1");
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
		URL patchdataUrl = new URL(getBundle().getEntry("/"), new Path(PATCHDATA).toString());
		patchdataUrl = FileLocator.resolve(patchdataUrl);
		
		Map map = null;
		if (patchdataUrl.getProtocol().equals("file")) {
			map = extractNamesForFileProtocol(patchdataUrl);
		} else if (patchdataUrl.getProtocol().equals("jar")) {
			map = extractNamesForJarProtocol(patchdataUrl);	
		} else {
			fail("Unknown protocol");
		}
		assertNotNull(map);
		
		for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
			String sf = (String) iterator.next(); // subfolder
			PatchTestConfiguration ptc = (PatchTestConfiguration) map.get(sf);
			String[] originalFiles = ptc.originalFileNames;
			String patch = ptc.patchFileName;
			String[] expectedFiles = ptc.expectedFileNames;
			String[] actualFiles = ptc.actualFileNames;
			PatchConfiguration pc = ptc.pc;
			
			// create a message to distinguish tests from different subfolders
			String msg = "Test for subfolder [" + PATCHDATA + "/" + sf + "] failed.";
			
			try {
				// test with expected result
				patchWorkspace(msg, originalFiles, patch, expectedFiles, pc);
			} catch (AssertionFailedError e) {
				failures.add(e);
			}

			// test with actual result, should fail
			if (actualFiles != null) {
				try {
					patchWorkspace(msg, originalFiles, patch, actualFiles, pc);
				} catch (AssertionFailedError e) {
					// a failure is expected
					continue; // continue with a next subfolder
				}
				failures.add(new AssertionFailedError(
						"\npatchWorkspace should fail for folder [" + PATCHDATA
								+ "/" + sf + "]."));
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
	 * @param patchdataUrl
	 * @return A map with subfolder name as a key and an array of objects as a
	 *         value. The first object in the array is another array (of
	 *         Strings) containing file names for the test. The last value in
	 *         this array can be <code>null</code> as testing against actual
	 *         result is optional. The second object is an instance of
	 *         <code>PatchConfiguration</code> class.
	 * @throws IOException
	 * @throws CoreException
	 */
	private Map extractNamesForJarProtocol(URL patchdataUrl) throws IOException,
			CoreException {
		JarFile jarFile = ((JarURLConnection) patchdataUrl.openConnection()).getJarFile();
		
		// look for the patchdata folder entry
		String patchdataName = null;
		Enumeration entries = jarFile.entries();
		while (entries.hasMoreElements()) {
			JarEntry entry = (JarEntry) entries.nextElement();
			String entryName = entry.getName();
			if (entryName.endsWith("/" + PATCHDATA + "/")) {
				patchdataName = entryName;
				break;
			}
		}
		// patchdata folder not found
		if (patchdataName == null)
			return null;
		
		Map result = new HashMap();
		entries = jarFile.entries();
		while (entries.hasMoreElements()) {
			JarEntry entry = (JarEntry) entries.nextElement();
			String entryName = entry.getName();
			if (entry.isDirectory()) {
				if (!entryName.equals(patchdataName) && entryName.startsWith(patchdataName)) {
					// a subfolder found
					ZipEntry patchConf = jarFile.getEntry(entryName + "/" + PATCH_CONFIGURATION);
					if (patchConf != null) {
						JarEntryStorage jes = new JarEntryStorage(entry,jarFile);
						Properties properties = new Properties();
					    try {
					        properties.load(jes.getContents());
					    } catch (IOException e) {
					    	fail("IOException occured while loading the Patch Configuration file for "+entryName.toString());
					    }
					    processProperties(result, properties, entryName);
					} else {
						processProperties(result, defaultPatchProperties, entryName);
					}
				}
			} 
		}
		return result;
	}
	
	private Map extractNamesForFileProtocol(URL patchdataUrl)
			throws CoreException {

		Map result = new HashMap(); // configuration map

		IPath patchdataFolderPath = new Path(patchdataUrl.getPath());
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
			Path pcPath = new Path(subfolder.getPath() + "/" + PATCH_CONFIGURATION);
			File pcFile = pcPath.toFile();
			
			if (subfolder.getName().equals("CVS"))
				continue;
			if (pcFile.exists()) {
				Properties properties = new Properties();
			    try {
			        properties.load(new FileInputStream(pcFile));
			    } catch (IOException e) {
			    	fail("IOException occured while loading the Patch Configuration file for "
							+ subfolder.toString());
			    }
			    processProperties(result, properties, subfolder.getName());
			} else {
				processProperties(result, defaultPatchProperties, subfolder.getName());
			}
		}
		return result;
	}

	private void processProperties(Map result, Properties p, String subfolderName) {
		boolean skipTest = Boolean.valueOf(p.getProperty("skipTest", "false")).booleanValue();
		if (skipTest)
			return;
		String pf = p.getProperty("patchFile", "patch.txt");
		String[] cf = p.getProperty("contextFile", "context.txt").split(",");
		String[] erf = p.getProperty("expectedResultFile", "exp_context").split(",");
		// optional, can't guess the file name here, it might left empty intentionally
		String[] arf = null;
		String arfp = p.getProperty("actualResultFile", null);
		if (arfp != null)
			arf = arfp.split(",");
		int fuzzFactor = Integer.parseInt(p.getProperty("fuzzFactor", "0")); 
		boolean ignoreWhitespace = Boolean.valueOf(p.getProperty("ignoreWhitespace", "false")).booleanValue();
		int prefixSegmentStrip = Integer.parseInt(p.getProperty("prefixSegmentStrip", "0"));
		boolean reversed = Boolean.valueOf(p.getProperty("reversed", "false")).booleanValue();

		PatchConfiguration pc = new PatchConfiguration();
		pc.setFuzz(fuzzFactor);
		pc.setIgnoreWhitespace(ignoreWhitespace);
		pc.setPrefixSegmentStripCount(prefixSegmentStrip);
		pc.setReversed(reversed);

		// make the paths relative
		pf = subfolderName + "/" + pf;
		for (int i = 0; i < cf.length; i++) {
			cf[i] = subfolderName + "/" + cf[i];
		}
		for (int i = 0; i < erf.length; i++) {
			erf[i] = subfolderName + "/" + erf[i];
		}
		if (arf != null) { // optional
			for (int i = 0; i < arf.length; i++) {
				arf[i] = subfolderName + "/" + arf[i];
			}
		}

		PatchTestConfiguration tpc = new PatchTestConfiguration();
		tpc.originalFileNames = cf;
		tpc.patchFileName = pf;
		tpc.subfolderName = subfolderName;
		tpc.expectedFileNames= erf;
		tpc.actualFileNames = arf;
		tpc.pc = pc;

		result.put(subfolderName, tpc);
	}


	// Test changing
	private BufferedReader getReader(String name) {
		InputStream resourceAsStream = asInputStream(name);
		InputStreamReader reader2= new InputStreamReader(resourceAsStream);
		return new BufferedReader(reader2);
	}

	private InputStream asInputStream(String name) {
		IPath path= new Path(PATCHDATA).append(name);
		try {
			URL url= new URL(getBundle().getEntry("/"), path.toString());
			return url.openStream();
		} catch (IOException e) {
			fail("Failed while reading " + name);
			return null; // never reached
		}
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
		PatchConfiguration pc = new PatchConfiguration();
		pc.setReversed(reverse);
		pc.setFuzz(fuzzFactor);
		patchWorkspace(null, originalFiles, patch, expectedOutcomeFiles, pc);
	}

	/**
	 * Parses a workspace patch and applies the diffs to the appropriate files
	 * 
	 * @param msg
	 * @param originalFiles
	 * @param patch
	 * @param expectedOutcomeFiles
	 * @param patchConfiguration
	 *            The patch configuration to use. One of its parameters is fuzz
	 *            factor. If it equals <code>-1</code> it means that the fuzz
	 *            should be calculated automatically.
	 */
	private void patchWorkspace(String msg, String[] originalFiles, String patch, String[] expectedOutcomeFiles, PatchConfiguration patchConfiguration) {
		
		//ensure that we have the same number of input files as we have expected files
		Assert.assertEquals(originalFiles.length, expectedOutcomeFiles.length);
		
		// Parse the passed in patch and extract all the Diffs
		WorkspacePatcher patcher = new WorkspacePatcher();
		try {
			patcher.getConfiguration().setFuzz(patchConfiguration.getFuzz());
			patcher.getConfiguration().setIgnoreWhitespace(patchConfiguration.isIgnoreWhitespace());
			patcher.getConfiguration().setPrefixSegmentStripCount(patchConfiguration.getPrefixSegmentStripCount());
			patcher.parse(getReader(patch));
			patcher.setReversed(patchConfiguration.isReversed());
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

			String resultString = LineReader.createString(patcher.isPreserveLineDelimeters(), inLines);
			LineReader resultReader = new LineReader(new BufferedReader(new StringReader(resultString)));
			Object[] result = resultReader.readLines().toArray();

			Assert.assertEquals(msg, expected.length, result.length);
			
			for (int j= 0; j < expected.length; j++)
				Assert.assertEquals(msg, expected[j], result[j]);
		}
	}
	
	private Bundle getBundle() {
		return CompareTestPlugin.getDefault().getBundle();
	}	
}
