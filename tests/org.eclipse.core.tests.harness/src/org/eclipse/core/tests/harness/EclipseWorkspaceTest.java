/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.harness;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.utils.UniversalUniqueIdentifier;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

/**
 * Tests that use the Eclipse Platform workspace.
 */
public class EclipseWorkspaceTest extends TestCase {
	public static final String PI_HARNESS = "org.eclipse.core.tests.harness"; //$NON-NLS-1$	

	//constants for nature sets	
	protected static final String SET_STATE = "org.eclipse.core.tests.resources.stateSet";
	protected static final String SET_OTHER = "org.eclipse.core.tests.resources.otherSet";

	//constants for nature ids

	//simpleNature
	protected static final String NATURE_SIMPLE = "org.eclipse.core.tests.resources.simpleNature";
	//snowNature, requires: waterNature, one-of: otherSet
	protected static final String NATURE_SNOW = "org.eclipse.core.tests.resources.snowNature";
	//waterNature, oneof: stateSet
	protected static final String NATURE_WATER = "org.eclipse.core.tests.resources.waterNature";
	//earthNature, oneof: stateSet
	protected static final String NATURE_EARTH = "org.eclipse.core.tests.resources.earthNature";
	//mudNature, requires: waterNature, earthNature, one-of: otherSet
	protected static final String NATURE_MUD = "org.eclipse.core.tests.resources.mudNature";
	//invalidNature
	protected static final String NATURE_INVALID = "org.eclipse.core.tests.resources.invalidNature";
	//cycle1 requires: cycle2
	protected static final String NATURE_CYCLE1 = "org.eclipse.core.tests.resources.cycle1";
	//cycle2 requires: cycle3
	protected static final String NATURE_CYCLE2 = "org.eclipse.core.tests.resources.cycle2";
	//cycle3 requires: cycle1
	protected static final String NATURE_CYCLE3 = "org.eclipse.core.tests.resources.cycle3";
	//missing nature
	protected static final String NATURE_MISSING = "no.such.nature.Missing";
	//missing pre-req nature
	protected static final String NATURE_MISSING_PREREQ = "org.eclipse.core.tests.resources.missingPrerequisiteNature";
	//nature that installs and runs a builder (regression test for bug 29116)
	protected static final String NATURE_29116 = "org.eclipse.core.tests.resources.nature29116";

	/** delta change listener if requested */
	public static IResourceChangeListener deltaListener;

	/**
	 * Log messages if we are in debug mode.
	 */
	public static void log(String message) {
		String id = "org.eclipse.core.tests.harness/debug";
		String option = Platform.getDebugOption(id);
		if (Boolean.TRUE.toString().equalsIgnoreCase(option))
			System.out.println(message);
	}

	/**
	 * Need a zero argument constructor to satisfy the test harness.
	 * This constructor should not do any real work nor should it be
	 * called by user code.
	 */
	public EclipseWorkspaceTest() {
		super(null);
	}

	/**
	 * Creates a new EclipseWorkspaceTest
	 * @param name java.lang.String
	 */
	public EclipseWorkspaceTest(String name) {
		super(name);
	}

	/**
	 * Assert that each element of the resource array does not exist in the 
	 * local store.
	 */
	public void assertDoesNotExistInFileSystem(IResource[] resources) {
		assertDoesNotExistInFileSystem("", resources); //$NON-NLS-1$
	}

	/**
	 * Assert that each element of the resource array does not exist in the 
	 * local store.
	 */
	public void assertDoesNotExistInFileSystem(String message, IResource[] resources) {
		for (int i = 0; i < resources.length; i++)
			assertDoesNotExistInFileSystem(message, resources[i]);
	}

	/**
	 * Assert that the given resource does not exist in the local store.
	 */
	public void assertDoesNotExistInFileSystem(String message, IResource resource) {
		if (existsInFileSystem(resource)) {
			String formatted = message == null ? "" : message + " ";
			fail(formatted + resource.getFullPath() + " unexpectedly exists in the file system");
		}
	}

	/**
	 * Assert that the given resource does not exist in the local store.
	 */
	public void assertDoesNotExistInFileSystem(IResource resource) {
		assertDoesNotExistInFileSystem("", resource); //$NON-NLS-1$
	}

	/**
	 * Assert that each element of the resource array does not exist 
	 * in the workspace resource info tree.
	 */
	public void assertDoesNotExistInWorkspace(IResource[] resources) {
		assertDoesNotExistInWorkspace("", resources); //$NON-NLS-1$
	}

	/**
	 * Assert that each element of the resource array does not exist 
	 * in the workspace resource info tree.
	 */
	public void assertDoesNotExistInWorkspace(String message, IResource[] resources) {
		for (int i = 0; i < resources.length; i++) {
			assertDoesNotExistInWorkspace(message, resources[i]);
		}
	}

	protected void assertEquals(String message, Object[] expected, Object[] actual) {
		if (expected == null && actual == null)
			return;
		if (expected == null || actual == null)
			fail(message);
		if (expected.length != actual.length)
			fail(message);
		for (int i = 0; i < expected.length; i++)
			assertEquals(message, expected[i], actual[i]);
	}

	/**
	 * Assert that the given resource does not exist in the workspace 
	 * resource info tree.
	 */
	public void assertDoesNotExistInWorkspace(String message, IResource resource) {
		if (existsInWorkspace(resource, false)) {
			String formatted = message == null ? "" : message + " ";
			fail(formatted + resource.getFullPath().toString() + " unexpectedly exists in the workspace");
		}
	}

	/**
	 * Assert that the given resource does not exist in the workspace 
	 * resource info tree.
	 */
	public void assertDoesNotExistInWorkspace(IResource resource) {
		assertDoesNotExistInWorkspace("", resource); //$NON-NLS-1$
	}

	/**
	 * Assert that each element in the resource array  exists in the local store.
	 */
	public void assertExistsInFileSystem(IResource[] resources) {
		assertExistsInFileSystem("", resources); //$NON-NLS-1$
	}

	/**
	 * Assert that each element in the resource array  exists in the local store.
	 */
	public void assertExistsInFileSystem(String message, IResource[] resources) {
		for (int i = 0; i < resources.length; i++)
			assertExistsInFileSystem(message, resources[i]);
	}

	/**
	 * Assert whether or not the given resource exists in the local 
	 * store. Use the resource manager to ensure that we have a 
	 * correct Path -> File mapping.
	 */
	public void assertExistsInFileSystem(String message, IResource resource) {
		if (!existsInFileSystem(resource)) {
			String formatted = message == null ? "" : message + " ";
			fail(formatted + resource.getFullPath() + " unexpectedly does not exist in the file system");
		}
	}

	/**
	 * Assert whether or not the given resource exists in the local 
	 * store. Use the resource manager to ensure that we have a 
	 * correct Path -> File mapping.
	 */
	public void assertExistsInFileSystem(IResource resource) {
		assertExistsInFileSystem("", resource); //$NON-NLS-1$
	}

	/**
	 * Assert that each element of the resource array exists in the 
	 * workspace resource info tree.
	 */
	public void assertExistsInWorkspace(IResource[] resources) {
		assertExistsInWorkspace("", resources, false); //$NON-NLS-1$
	}

	/**
	 * Assert that each element of the resource array exists in the 
	 * workspace resource info tree.
	 */
	public void assertExistsInWorkspace(IResource[] resources, boolean phantom) {
		assertExistsInWorkspace("", resources, phantom); //$NON-NLS-1$
	}

	/**
	 * Assert that each element of the resource array exists in the 
	 * workspace resource info tree.
	 */
	public void assertExistsInWorkspace(String message, IResource[] resources) {
		for (int i = 0; i < resources.length; i++)
			assertExistsInWorkspace(message, resources[i], false);
	}

	/**
	 * Assert that each element of the resource array exists in the 
	 * workspace resource info tree.
	 */
	public void assertExistsInWorkspace(String message, IResource[] resources, boolean phantom) {
		for (int i = 0; i < resources.length; i++)
			assertExistsInWorkspace(message, resources[i], phantom);
	}

	/**
	 * Assert whether or not the given resource exists in the workspace 
	 * resource info tree.
	 */
	public void assertExistsInWorkspace(String message, IResource resource) {
		assertExistsInWorkspace(message, resource, false);
	}

	/**
	 * Assert whether or not the given resource exists in the workspace 
	 * resource info tree.
	 */
	public void assertExistsInWorkspace(String message, IResource resource, boolean phantom) {
		if (!existsInWorkspace(resource, phantom)) {
			String formatted = message == null ? "" : message + " ";
			fail(formatted + resource.getFullPath().toString() + " unexpectedly does not exist in the workspace");
		}
	}

	/**
	 * Assert whether or not the given resource exists in the workspace 
	 * resource info tree.
	 */
	public void assertExistsInWorkspace(IResource resource) {
		assertExistsInWorkspace("", resource, false); //$NON-NLS-1$
	}

	/**
	 * Assert whether or not the given resource exists in the workspace 
	 * resource info tree.
	 */
	public void assertExistsInWorkspace(IResource resource, boolean phantom) {
		assertExistsInWorkspace("", resource, phantom); //$NON-NLS-1$
	}

	/**
	 * Return a collection of resources the hierarcy defined by defineHeirarchy().
	 */
	public IResource[] buildResources() {
		return buildResources(getWorkspace().getRoot(), defineHierarchy());
	}

	/**
	 * Return a collection of resources for the given hierarchy at 
	 * the given root.
	 */
	public IResource[] buildResources(IContainer root, String[] hierarchy) {
		IResource[] result = new IResource[hierarchy.length];
		for (int i = 0; i < hierarchy.length; i++) {
			IPath path = new Path(hierarchy[i]);
			IPath fullPath = root.getFullPath().append(path);
			switch (fullPath.segmentCount()) {
				case 0 :
					result[i] = getWorkspace().getRoot();
					break;
				case 1 :
					result[i] = getWorkspace().getRoot().getProject(fullPath.segment(0));
					break;
				default :
					if (hierarchy[i].charAt(hierarchy[i].length() - 1) == IPath.SEPARATOR)
						result[i] = root.getFolder(path);
					else
						result[i] = root.getFile(path);
					break;
			}
		}
		return result;
	}

	/**
	 * Returns a boolean value indicating whether or not the contents
	 * of the given streams are considered to be equal. Closes both input streams.
	 */
	public boolean compareContent(InputStream a, InputStream b) {
		int c, d;
		if (a == null && b == null)
			return true;
		try {
			if (a == null || b == null)
				return false;
			while ((c = a.read()) == (d = b.read()) && (c != -1 && d != -1));
			return (c == -1 && d == -1);
		} catch (IOException e) {
			return false;
		} finally {
			try {
				if (a != null)
					a.close();
			} catch (IOException e) {
				// ignore
			}
			try {
				if (b != null)
					b.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	private IPath computeDefaultLocation(IResource target) {
		switch (target.getType()) {
			case IResource.ROOT :
				return Platform.getLocation();
			case IResource.PROJECT :
				return Platform.getLocation().append(target.getFullPath());
			default :
				IPath location = computeDefaultLocation(target.getProject());
				location = location.append(target.getFullPath().removeFirstSegments(1));
				return location;
		}
	}

	protected void create(final IResource resource, boolean local) throws CoreException {
		if (resource == null || resource.exists())
			return;
		if (!resource.getParent().exists())
			create(resource.getParent(), local);
		switch (resource.getType()) {
			case IResource.FILE :
				((IFile) resource).create(local ? new ByteArrayInputStream(new byte[0]) : null, true, getMonitor());
				break;
			case IResource.FOLDER :
				((IFolder) resource).create(true, local, getMonitor());
				break;
			case IResource.PROJECT :
				((IProject) resource).create(getMonitor());
				((IProject) resource).open(getMonitor());
				break;
		}
	}

	/**
	 * Create the given file in the local store. 
	 */
	public void createFileInFileSystem(IPath path) throws CoreException {
		java.io.File file = path.toFile();
		file.getParentFile().mkdirs();
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(file);
			output.write("".getBytes("UTF8"));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, "foo", 2, "Failed during write: " + path, e));
		} finally {
			try {
				if (output != null)
					output.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	/**
	 * Create the given file in the local store. 
	 */
	public void createFileInFileSystem(IPath path, InputStream contents) throws IOException {
		java.io.File file = path.toFile();
		file.getParentFile().mkdirs();
		FileOutputStream output = new FileOutputStream(file);
		transferData(contents, output);
	}

	public IResource[] createHierarchy() {
		IResource[] result = buildResources();
		ensureExistsInWorkspace(result, true);
		return result;
	}

	/**
	 * Returns a collection of string paths describing the standard 
	 * resource hierarchy for this test.  In the string forms, folders are
	 * represented as having trailing separators ('/').  All other resources
	 * are files.  It is generally assumed that this hierarchy will be 
	 * inserted under some project structure.
	 * For example, 	
	 * <pre>
	 *    return new String[] {"/", "/1/", "/1/1", "/1/2", "/1/3", "/2/", "/2/1"};
	 * </pre>
	 */
	public String[] defineHierarchy() {
		return new String[0];
	}

	/**
	 * Delete the resources in the array from the local store.
	 */
	public void ensureDoesNotExistInFileSystem(IResource[] resources) {
		for (int i = 0; i < resources.length; i++)
			ensureDoesNotExistInFileSystem(resources[i]);
	}

	protected void ensureDoesNotExistInFileSystem(java.io.File file) {
		FileSystemHelper.clear(file);
	}

	/**
	 * Delete the given resource from the local store. Use the resource
	 * manager to ensure that we have a correct Path -> File mapping.
	 */
	public void ensureDoesNotExistInFileSystem(IResource resource) {
		IPath path = resource.getLocation();
		if (path != null)
			ensureDoesNotExistInFileSystem(path.toFile());
	}

	/**
	 * Delete each element of the resource array from the workspace 
	 * resource info tree.
	 */
	public void ensureDoesNotExistInWorkspace(final IResource[] resources) {
		IWorkspaceRunnable body = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) {
				for (int i = 0; i < resources.length; i++)
					ensureDoesNotExistInWorkspace(resources[i]);
			}
		};
		try {
			getWorkspace().run(body, null);
		} catch (CoreException e) {
			fail("#ensureDoesNotExistInWorkspace(IResource[])", e);
		}
	}

	/**
	 * Delete the given resource from the workspace resource tree.
	 */
	public void ensureDoesNotExistInWorkspace(IResource resource) {
		try {
			if (resource.exists())
				resource.delete(true, null);
		} catch (CoreException e) {
			fail("#ensureDoesNotExistInWorkspace(IResource): " + resource.getFullPath(), e);
		}
	}

	/**
	 * Create the each resource of the array in the local store.
	 */
	public void ensureExistsInFileSystem(IResource[] resources) {
		for (int i = 0; i < resources.length; i++)
			ensureExistsInFileSystem(resources[i]);
	}

	/**
	 * Create the given file in the local store. Use the resource manager
	 * to ensure that we have a correct Path -> File mapping.
	 */
	public void ensureExistsInFileSystem(IFile file) {
		IPath path = file.getLocation();
		try {
			if (path != null)
				createFileInFileSystem(path);
		} catch (CoreException e) {
			fail("#ensureExistsInFileSystem(IFile): " + file.getFullPath(), e);
		}
	}

	/**
	 * Create the given folder in the local store. Use the resource
	 * manager to ensure that we have a correct Path -> File mapping.
	 */
	public void ensureExistsInFileSystem(IResource resource) {
		if (resource instanceof IFile)
			ensureExistsInFileSystem((IFile) resource);
		else {
			IPath path = resource.getLocation();
			if (path != null)
				path.toFile().mkdirs();
		}
	}

	/**
	 * Create each element of the resource array in the workspace resource 
	 * info tree.
	 */
	public void ensureExistsInWorkspace(final IResource[] resources, final boolean local) {
		IWorkspaceRunnable body = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				for (int i = 0; i < resources.length; i++)
					create(resources[i], local);
			}
		};
		try {
			getWorkspace().run(body, null);
		} catch (CoreException e) {
			fail("#ensureExistsInWorkspace(IResource[])", e);
		}
	}

	/**
	 * Modifies the resource in the filesystem so that it is out of sync
	 * with the workspace.
	 */
	public void ensureOutOfSync(final IResource resource) {
		if (resource.getType() != IResource.FILE)
			return;
		IFile file = (IFile) resource;
		ensureExistsInWorkspace(file, true);
		while (file.isSynchronized(IResource.DEPTH_ZERO)) {
			modifyInFileSystem(file);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

	/**
	 * Create the given file in the workspace resource info tree.
	 */
	public void ensureExistsInWorkspace(final IFile resource, final InputStream contents) {
		if (resource == null)
			return;
		IWorkspaceRunnable body = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				if (resource.exists()) {
					resource.setContents(contents, true, false, null);
				} else {
					ensureExistsInWorkspace(resource.getParent(), true);
					resource.create(contents, true, null);
				}
			}
		};
		try {
			getWorkspace().run(body, null);
		} catch (CoreException e) {
			fail("#ensureExistsInWorkspace(IFile, InputStream): " + resource.getFullPath(), e);
		}
	}

	/**
	 * Create the given file in the workspace resource info tree.
	 */
	public void ensureExistsInWorkspace(IFile resource, String contents) {
		ensureExistsInWorkspace(resource, new ByteArrayInputStream(contents.getBytes()));
	}

	/**
	 * Create the given resource in the workspace resource info tree.
	 */
	public void ensureExistsInWorkspace(final IResource resource, final boolean local) {
		IWorkspaceRunnable body = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				create(resource, local);
			}
		};
		try {
			getWorkspace().run(body, null);
		} catch (CoreException e) {
			fail("#ensureExistsInWorkspace(IResource): " + resource.getFullPath(), e);
		}
	}

	private boolean existsInFileSystem(IResource resource) {
		IPath path = resource.getLocation();
		if (path == null)
			path = computeDefaultLocation(resource);
		return path.toFile().exists();
	}

	private boolean existsInWorkspace(IResource resource, boolean phantom) {
		IResource target = getWorkspace().getRoot().findMember(resource.getFullPath(), phantom);
		return target != null && target.getType() == resource.getType();
	}

	/**
	 * Fails the test due to the given throwable.
	 * @param message
	 * @param e
	 */
	public void fail(String message, Throwable e) {
		// If the exception is a CoreException with a multistatus
		// then print out the multistatus so we can see all the info.
		if (e instanceof CoreException) {
			IStatus status = ((CoreException) e).getStatus();
			if (status.getChildren().length > 0)
				write(status, 0);
		}
		fail(message + ": " + e);
	}

	protected void indent(OutputStream output, int indent) {
		for (int i = 0; i < indent; i++)
			try {
				output.write("\t".getBytes());
			} catch (IOException e) {
				// ignore
			}
	}

	protected void write(IStatus status, int indent) {
		PrintStream output = System.out;
		indent(output, indent);
		output.println("Severity: " + status.getSeverity());

		indent(output, indent);
		output.println("Plugin ID: " + status.getPlugin());

		indent(output, indent);
		output.println("Code: " + status.getCode());

		indent(output, indent);
		output.println("Message: " + status.getMessage());

		if (status.isMultiStatus()) {
			IStatus[] children = status.getChildren();
			for (int i = 0; i < children.length; i++)
				write(children[i], indent + 1);
		}
	}

	/**
	 * Does some garbage collections to free unused resources
	 */
	protected static void gc() {
		/* make sure old stores get finalized so they free old files */
		for (int i = 0; i < 2; i++) {
			System.runFinalization();
			System.gc();
		}
	}

	/** 
	 * Returns the unqualified class name of the receiver (ie. without the package prefix).
	 */
	protected String getClassName() {
		String fullClassName = getClass().getName();
		return fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
	}

	public InputStream getContents(java.io.File target, String errorCode) {
		try {
			return new FileInputStream(target);
		} catch (IOException e) {
			fail(errorCode, e);
		}
		return null; // never happens
	}

	/**
	 * Return an input stream with some the specified text to use
	 * as contents for a file resource.
	 */
	public InputStream getContents(String text) {
		return new ByteArrayInputStream(text.getBytes());
	}

	/**
	 * Returns invalid sets of natures
	 */
	protected String[][] getInvalidNatureSets() {
		return new String[][] { {NATURE_SNOW}, //missing water pre-req
				{NATURE_WATER, NATURE_EARTH}, //duplicates from state-set
				{NATURE_WATER, NATURE_MUD}, //missing earth pre-req
				{NATURE_WATER, NATURE_EARTH, NATURE_MUD}, //duplicates from state-set
				{NATURE_SIMPLE, NATURE_SNOW, NATURE_WATER, NATURE_MUD}, //dups from other-set, missing pre-req
				{NATURE_MISSING}, //doesn't exist
				{NATURE_SIMPLE, NATURE_MISSING}, //missing doesn't exist
				{NATURE_MISSING_PREREQ}, //requires nature that doesn't exist
				{NATURE_SIMPLE, NATURE_MISSING_PREREQ}, //requires nature that doesn't exist
				{NATURE_CYCLE1}, //missing pre-req
				{NATURE_CYCLE2, NATURE_CYCLE3}, //missing pre-req
				{NATURE_CYCLE1, NATURE_SIMPLE, NATURE_CYCLE2, NATURE_CYCLE3}, //cycle
		};
	}

	public IProgressMonitor getMonitor() {
		return new FussyProgressMonitor();
	}

	/**
	 * Return an input stream with some random text to use
	 * as contents for a file resource.
	 */
	public InputStream getRandomContents() {
		return new ByteArrayInputStream(getRandomString().getBytes());
	}

	/**
	 * Returns a unique location on disk.  It is guaranteed that no file currently
	 * exists at that location.  The returned location will be unique with respect 
	 * to all other locations generated by this method in the current session.  
	 * If the caller creates a folder or file at this location, they are responsible for 
	 * deleting it when finished.
	 */
	public IPath getRandomLocation() {
		return FileSystemHelper.getRandomLocation(getTempDir());
	}

	/**
	 * Return String with some random text to use
	 * as contents for a file resource.
	 */
	public String getRandomString() {
		switch ((int) Math.round(Math.random() * 10)) {
			case 0 :
				return "este e' o meu conteudo (portuguese)";
			case 1 :
				return "ho ho ho";
			case 2 :
				return "I'll be back";
			case 3 :
				return "don't worry, be happy";
			case 4 :
				return "there is no imagination for more sentences";
			case 5 :
				return "customize yours";
			case 6 :
				return "foo";
			case 7 :
				return "bar";
			case 8 :
				return "foobar";
			case 9 :
				return "case 9";
			default :
				return "these are my contents";
		}
	}

	/*
	 * Return the root directory for the temp dir.
	 */
	public IPath getTempDir() {
		return FileSystemHelper.getTempDir();
	}

	public String getUniqueString() {
		return new UniversalUniqueIdentifier().toString();
	}

	/**
	 * Returns valid sets of natures
	 */
	protected String[][] getValidNatureSets() {
		return new String[][] { {}, {NATURE_SIMPLE}, {NATURE_SNOW, NATURE_WATER}, {NATURE_EARTH}, {NATURE_WATER, NATURE_SIMPLE, NATURE_SNOW},};
	}

	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Modifies the content of the given file in the file system by
	 * appending an 'f'.
	 * @param file
	 */
	protected void modifyInFileSystem(IFile file) {
		String m = getClassName() + ".modifyInFileSystem(IFile): ";
		String newContent = readStringInFileSystem(file) + "f";
		java.io.File osFile = file.getLocation().toFile();
		try {
			FileOutputStream os = null;
			try {
				os = new FileOutputStream(osFile);
				os.write(newContent.getBytes("UTF8"));
			} finally {
				os.close();
			}
		} catch (IOException e) {
			fail(m + "0.0", e);
		}
	}

	/**
	 * Modifies the content of the given file in the workspace by
	 * appending a 'w'.
	 * @param file
	 */
	protected void modifyInWorkspace(IFile file) throws CoreException {
		String m = getClassName() + ".modifyInWorkspace(IFile): ";
		try {
			String newContent = readStringInWorkspace(file) + "w";
			ByteArrayInputStream is = new ByteArrayInputStream(newContent.getBytes("UTF8"));
			file.setContents(is, false, false, null);
		} catch (UnsupportedEncodingException e) {
			fail(m + "0.0");
		}
	}

	/**
	 * Returns the content of the given file in the file system as a
	 * byte array.
	 * @param file
	 */
	protected byte[] readBytesInFileSystem(IFile file) {
		String m = getClassName() + ".readBytesInFileSystem(IFile): ";
		try {
			IPath location = file.getLocation();
			if (location == null)
				fail("0.1 - null location for file: " + file);
			java.io.File osFile = location.toFile();
			FileInputStream is = new FileInputStream(osFile);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			transferData(is, os);
			return os.toByteArray();
		} catch (IOException e) {
			fail(m + "0.0", e);
		}
		return null;
	}

	/**
	 * Returns the content of the given file in the workspace as a
	 * byte array.
	 */
	protected byte[] readBytesInWorkspace(IFile file) {
		String m = getClassName() + ".readBytesInWorkspace(IFile): ";
		try {
			InputStream is = file.getContents(false);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			transferData(is, os);
			return os.toByteArray();
		} catch (CoreException e) {
			fail(m + "0.0", e);
		}
		return null;
	}

	/**
	 * Returns the content of the given file in the file system as a
	 * String (UTF8).
	 * @param file
	 */
	protected String readStringInFileSystem(IFile file) {
		String m = getClassName() + ".readStringInFileSystem(IFile): ";
		try {
			return new String(readBytesInFileSystem(file), "UTF8");
		} catch (UnsupportedEncodingException e) {
			fail(m + "0.0", e);
		}
		return null;
	}

	/**
	 * Returns the content of the given file in the workspace as a
	 * String (UTF8).
	 * @param file
	 */
	protected String readStringInWorkspace(IFile file) {
		String m = getClassName() + ".readStringInWorkspace(IFile): ";
		try {
			return new String(readBytesInWorkspace(file), "UTF8");
		} catch (UnsupportedEncodingException e) {
			fail(m + "0.0", e);
		}
		return null;
	}

	/**
	 * The environment should be set-up in the main method.
	 */
	protected void setUp() throws Exception {
		assertNotNull("Workspace was not setup", getWorkspace());
		if (EclipseTestHarnessApplication.deltasEnabled() && deltaListener == null) {
			deltaListener = new DeltaDebugListener();
			getWorkspace().addResourceChangeListener(deltaListener);
		}
	}

//	/**
//	 * Returns the test suite for this test class.
//	 */
//	public static Test suite() {
//		// subclasses must provide their own suite method
//		throw new UnsupportedOperationException("Every test class must provide a suite() method");
//	}

	/**
	 * Copy the data from the input stream to the output stream.
	 * Close both streams when finished.
	 */
	public void transferData(InputStream input, OutputStream output) {
		try {
			try {
				int c = 0;
				while ((c = input.read()) != -1)
					output.write(c);
			} finally {
				input.close();
				output.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(e.toString(), false);
		}
	}

	/**
	 * Copy the data from the input stream to the output stream.
	 * Do not close either of the streams.
	 */
	public void transferDataWithoutClose(InputStream input, OutputStream output) {
		try {
			int c = 0;
			while ((c = input.read()) != -1)
				output.write(c);
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(e.toString(), false);
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		// Ensure everything is in a clean state for next one.
		// Session tests should overwrite it.
		ensureDoesNotExistInWorkspace(getWorkspace().getRoot());
		getWorkspace().save(true, null);
	}

	/**
	 * Blocks the calling thread until autobuild completes.
	 */
	protected void waitForBuild() {
		try {
			Platform.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
		} catch (OperationCanceledException e) {
			//ignore
		} catch (InterruptedException e) {
			//ignore
		}
	}
	
	public static void log(IStatus status) {
		Platform.getPlugin(PI_HARNESS).getLog().log(status);
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, EclipseWorkspaceTest.PI_HARNESS, IStatus.ERROR, "Error", e)); //$NON-NLS-1$
	}

	
}