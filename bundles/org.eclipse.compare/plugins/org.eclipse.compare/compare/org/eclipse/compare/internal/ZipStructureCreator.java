/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000,2001
 */
package org.eclipse.compare.internal;

import java.io.*;
import java.util.Iterator;
import java.util.HashMap;
import java.util.zip.*;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.Assert;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.*;


public class ZipStructureCreator implements IStructureCreator {

	/**
	 * Common base class for ZipFolder and ZipFile
	 */
	static abstract class ZipResource implements IStructureComparator, ITypedElement {

		private String fName;

		ZipResource(String name) {
			fName= name;
		}

		public String getName() {
			return fName;
		}

		public Image getImage() {
			return CompareUIPlugin.getImage(getType());
		}

		/**
		 * Returns true if other is ITypedElement and names are equal.
		 * @see IComparator#equals
		 */
		public boolean equals(Object other) {
			if (other instanceof ITypedElement)
				return fName.equals(((ITypedElement) other).getName());
			return super.equals(other);
		}

		public int hashCode() {
			return fName.hashCode();
		}
	}

	static class ZipFolder extends ZipResource {

		private HashMap fChildren= new HashMap(10);

		ZipFolder(String name) {
			super(name);
		}

		public String getType() {
			return ITypedElement.FOLDER_TYPE;
		}

		public Object[] getChildren() {
			Object[] children= new Object[fChildren.size()];
			Iterator iter= fChildren.values().iterator();
			for (int i= 0; iter.hasNext(); i++)
				children[i]= iter.next();
			return children;
		}

		ZipFile createContainer(String path) {
			String entry= path;
			int pos= path.indexOf('/');
			if (pos < 0)
				pos= path.indexOf('\\');
			if (pos >= 0) {
				entry= path.substring(0, pos);
				path= path.substring(pos + 1);
			} else if (entry.length() > 0) {
				ZipFile ze= new ZipFile(entry);
				fChildren.put(entry, ze);
				return ze;
			} else
				return null;

			ZipFolder folder= null;
			if (fChildren != null) {
				Object o= fChildren.get(entry);
				if (o instanceof ZipFolder)
					folder= (ZipFolder) o;
			}

			if (folder == null) {
				folder= new ZipFolder(entry);
				fChildren.put(entry, folder);
			}

			return folder.createContainer(path);
		}
	}

	static class ZipFile extends ZipResource implements IStreamContentAccessor {

		private byte[] fContents;

		ZipFile(String name) {
			super(name);
		}

		public String getType() {
			String s= this.getName();
			int pos= s.lastIndexOf('.');
			if (pos >= 0)
				return s.substring(pos + 1);
			return ITypedElement.UNKNOWN_TYPE;
		}

		public Object[] getChildren() {
			return null;
		}
		
		public InputStream getContents() {
			if (fContents == null)
				fContents= new byte[0];
			return new ByteArrayInputStream(fContents);
		}

		byte[] getBytes() {
			return fContents;
		}

		void setBytes(byte[] buffer) {
			fContents= buffer;
		}
	}
	
	private String fTitle;

	public ZipStructureCreator() {
		this("Zip Archive Compare");
	}
	
	public ZipStructureCreator(String title) {
		fTitle= title;
	}

	public String getName() {
		return fTitle;
	}

	public IStructureComparator getStructure(Object input) {

		InputStream is= null;
		
		if (input instanceof IStreamContentAccessor) {
			IStreamContentAccessor sca= (IStreamContentAccessor) input;
			try {
				is= sca.getContents();
			} catch (CoreException ex) {
			}
		}

		if (is == null)
			return null;

		ZipInputStream zip= new ZipInputStream(is);
		ZipFolder root= new ZipFolder("");
		try {
			for (;;) {
				ZipEntry entry= zip.getNextEntry();
				if (entry == null)
					break;
				//System.out.println(entry.getName() + ": " + entry.getSize() + " " + entry.getCompressedSize());

				ZipFile ze= root.createContainer(entry.getName());
				if (ze != null) {
					int length= (int) entry.getSize();
					if (length >= 0) {
						byte[] buffer= new byte[length];
						int offset= 0;
	
						do {
							int n= zip.read(buffer, offset, length);
							offset += n;
							length -= n;
						} while (length > 0);
	
						ze.setBytes(buffer);
					}
				}
				zip.closeEntry();
			}
		} catch (IOException ex) {
			return null;
		} finally {
			try {
				zip.close();
			} catch (IOException ex) {
			}
		}

		if (root.fChildren.size() == 1) {
			Iterator iter= root.fChildren.values().iterator();
			return (IStructureComparator) iter.next();
		}
		return root;
	}

	public String getContents(Object o, boolean ignoreWhitespace) {
		if (o instanceof ZipFile) {
			byte[] bytes= ((ZipFile)o).getBytes();
			if (bytes != null)
				return new String(bytes);
			return "";
		}
		return null;
	}

	/**
	 * Returns <code>false</code> since we cannot update a zip archive.
	 */
	public boolean canSave() {
		return false;
	}

	/**
	 * Throws <code>AssertionFailedException</code> since we cannot update a zip archive.
	 */
	public void save(IStructureComparator structure, Object input) {
		Assert.isTrue(false, "cannot update zip archive");
	}
	
	public IStructureComparator locate(Object path, Object source) {
		return null;
	}
	
	public boolean canRewriteTree() {
		return false;
	}
	
	public void rewriteTree(Differencer diff, IDiffContainer root) {
	}
}

