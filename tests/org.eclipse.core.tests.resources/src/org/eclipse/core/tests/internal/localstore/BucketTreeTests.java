/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.localstore;

import java.io.*;
import java.util.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.localstore.Bucket;
import org.eclipse.core.internal.localstore.BucketTree;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.resources.ResourceTest;

public class BucketTreeTests extends ResourceTest {

	static class SimpleBucket extends Bucket {

		static class SimpleEntry extends Entry {
			private Map value;

			public SimpleEntry(IPath path, Map value) {
				super(path);
				this.value = value;
			}

			public int getOccurrences() {
				return value.size();
			}

			public String getProperty(String key) {
				return (String) value.get(key);
			}

			public Object getValue() {
				return value;
			}
		}

		public SimpleBucket() {
			super();
		}

		protected String getIndexFileName() {
			return "simple_bucket.index";
		}

		protected String getVersionFileName() {
			return "simple_bucket.version";
		}

		protected Entry createEntry(IPath path, Object value) {
			return new SimpleEntry(path, (Map) value);
		}

		public Map getValue(IPath key) {
			return (Map) getEntryValue(key.toString());
		}

		protected byte getVersion() {
			return 0;
		}

		protected Object readEntryValue(DataInputStream source) throws IOException {
			int length = source.readUnsignedShort();
			Map value = new HashMap(length);
			for (int j = 0; j < length; j++)
				value.put(source.readUTF(), source.readUTF());
			return value;
		}

		public void set(IPath path, String key, String value) {
			String pathAsString = path.toString();
			Map existing = (Map) getEntryValue(pathAsString);
			if (existing == null) {
				if (value != null) {
					existing = new HashMap();
					existing.put(key, value);
					setEntryValue(pathAsString, existing);
				}
				return;
			}
			if (value == null) {
				existing.remove(key);
				if (existing.isEmpty())
					existing = null;
			} else
				existing.put(key, value);
			setEntryValue(pathAsString, existing);
		}

		protected void writeEntryValue(DataOutputStream destination, Object entryValue) throws IOException {
			Map value = (Map) entryValue;
			int length = value.size();
			destination.writeShort(length);
			for (Iterator i = value.entrySet().iterator(); i.hasNext();) {
				Map.Entry element = (Map.Entry) i.next();
				destination.writeUTF((String) element.getKey());
				destination.writeUTF((String) element.getValue());
			}
		}
	}

	public static Test suite() {
		return new TestSuite(BucketTreeTests.class);
	}

	public BucketTreeTests(String name) {
		super(name);
	}

	public void testVisitor() {
		IPath baseLocation = getRandomLocation();
		try {
			// keep the reference around - it is the same returned by tree.getCurrent()
			SimpleBucket bucket = new SimpleBucket();
			BucketTree tree = new BucketTree((Workspace) getWorkspace(), bucket);
			IProject proj1 = getWorkspace().getRoot().getProject("proj1");
			IProject proj2 = getWorkspace().getRoot().getProject("proj2");
			IFile file1 = proj1.getFile("file1.txt");
			IFolder folder1 = proj1.getFolder("folder1");
			IFile file2 = folder1.getFile("file2.txt");
			ensureExistsInWorkspace(new IResource[] {file1, file2, proj2}, true);
			IPath[] paths = {Path.ROOT, proj1.getFullPath(), file1.getFullPath(), folder1.getFullPath(), file2.getFullPath(), proj2.getFullPath()};
			for (int i = 0; i < paths.length; i++) {
				try {
					tree.loadBucketFor(paths[i]);
				} catch (CoreException e) {
					fail("0.1." + i, e);
				}
				bucket.set(paths[i], "path", paths[i].toString());
				bucket.set(paths[i], "segments", Integer.toString(paths[i].segmentCount()));
			}
			try {
				bucket.save();
			} catch (CoreException e) {
				fail("0.2", e);
			}
			verify(tree, "1.1", Path.ROOT, BucketTree.DEPTH_ZERO, Arrays.asList(new IPath[] {Path.ROOT}));
			verify(tree, "1.2", Path.ROOT, BucketTree.DEPTH_ONE, Arrays.asList(new IPath[] {Path.ROOT, proj1.getFullPath(), proj2.getFullPath()}));
			verify(tree, "1.3", Path.ROOT, BucketTree.DEPTH_INFINITE, Arrays.asList(new IPath[] {Path.ROOT, proj1.getFullPath(), file1.getFullPath(), folder1.getFullPath(), file2.getFullPath(), proj2.getFullPath()}));
			verify(tree, "2.1", proj1.getFullPath(), BucketTree.DEPTH_ZERO, Arrays.asList(new IPath[] {proj1.getFullPath()}));
			verify(tree, "2.2", proj1.getFullPath(), BucketTree.DEPTH_ONE, Arrays.asList(new IPath[] {proj1.getFullPath(), file1.getFullPath(), folder1.getFullPath()}));
			verify(tree, "2.3", proj1.getFullPath(), BucketTree.DEPTH_INFINITE, Arrays.asList(new IPath[] {proj1.getFullPath(), file1.getFullPath(), folder1.getFullPath(), file2.getFullPath()}));
			verify(tree, "3.1", file1.getFullPath(), BucketTree.DEPTH_ZERO, Arrays.asList(new IPath[] {file1.getFullPath()}));
			verify(tree, "3.2", file1.getFullPath(), BucketTree.DEPTH_ONE, Arrays.asList(new IPath[] {file1.getFullPath()}));
			verify(tree, "3.3", file1.getFullPath(), BucketTree.DEPTH_INFINITE, Arrays.asList(new IPath[] {file1.getFullPath()}));
			verify(tree, "4.1", folder1.getFullPath(), BucketTree.DEPTH_ZERO, Arrays.asList(new IPath[] {folder1.getFullPath()}));
			verify(tree, "4.2", folder1.getFullPath(), BucketTree.DEPTH_ONE, Arrays.asList(new IPath[] {folder1.getFullPath(), file2.getFullPath()}));
			verify(tree, "4.3", folder1.getFullPath(), BucketTree.DEPTH_INFINITE, Arrays.asList(new IPath[] {folder1.getFullPath(), file2.getFullPath()}));
			verify(tree, "5.1", file2.getFullPath(), BucketTree.DEPTH_ZERO, Arrays.asList(new IPath[] {file2.getFullPath()}));
			verify(tree, "5.2", file2.getFullPath(), BucketTree.DEPTH_ONE, Arrays.asList(new IPath[] {file2.getFullPath()}));
			verify(tree, "5.3", file2.getFullPath(), BucketTree.DEPTH_INFINITE, Arrays.asList(new IPath[] {file2.getFullPath()}));
			verify(tree, "6.1", proj2.getFullPath(), BucketTree.DEPTH_ZERO, Arrays.asList(new IPath[] {proj2.getFullPath()}));
			verify(tree, "6.2", proj2.getFullPath(), BucketTree.DEPTH_ONE, Arrays.asList(new IPath[] {proj2.getFullPath()}));
			verify(tree, "6.3", proj2.getFullPath(), BucketTree.DEPTH_INFINITE, Arrays.asList(new IPath[] {proj2.getFullPath()}));

		} finally {
			ensureDoesNotExistInFileSystem(baseLocation.toFile());
		}
	}

	public void verify(BucketTree tree, final String tag, IPath root, int depth, final Collection expected) {
		final Set visited = new HashSet();
		SimpleBucket.Visitor verifier = new SimpleBucket.Visitor() {
			public int visit(org.eclipse.core.internal.localstore.Bucket.Entry entry) {
				SimpleBucket.SimpleEntry simple = (SimpleBucket.SimpleEntry) entry;
				IPath path = simple.getPath();
				assertTrue(tag + ".0 " + path, expected.contains(path));
				visited.add(path);
				assertEquals(tag + ".1 " + path, path.toString(), simple.getProperty("path"));
				assertEquals(tag + ".2 " + path, Integer.toString(path.segmentCount()), simple.getProperty("segments"));
				return CONTINUE;
			}
		};
		try {
			tree.accept(verifier, root, depth);
		} catch (CoreException e) {
			fail(tag + ".3", e);
		}
		assertEquals(tag + ".4", expected.size(), visited.size());
		for (Iterator i = expected.iterator(); i.hasNext();) {
			IPath path = (IPath) i.next();
			assertTrue(tag + ".5 " + path, visited.contains(path));
		}
	}
}
