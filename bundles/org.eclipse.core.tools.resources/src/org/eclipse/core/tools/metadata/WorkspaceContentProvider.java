/**********************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools.metadata;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import org.eclipse.core.tools.AbstractTreeContentProvider;
import org.eclipse.core.tools.TreeContentProviderNode;

/**
 * A tree content provider for Workspace view. Its input is .metadata directory,
 * the root directory from which Eclipse workspace metadata files 
 * will be searched. The result is a tree containing directory that contains 
 * (any directories that contain) metadata files and the metadata files 
 * themselves.<br>
 * This content provider mantains its contents using 
 * <code>TreeContentProviderNode</code>
 * objects.
 * 
 * @see org.eclipse.core.tools.TreeContentProviderNode
 */
public class WorkspaceContentProvider extends AbstractTreeContentProvider {
	/** 
	 * The file filter. 
	 * 
	 * @see MetadataFileFilter
	 */
	private FileFilter fileFilter;

	/** 
	 * The directory filter. 
	 * 
	 * @see DirectoryFilter
	 */
	private FileFilter directoryFilter;

	/**
	 * Constructs a new content provider. 
	 * 
	 * @param registeredFileNames an array containing all metadata file names known 
	 */
	public WorkspaceContentProvider(String[] registeredFileNames) {
		super(true);
		this.fileFilter = new MetadataFileFilter(registeredFileNames);
		this.directoryFilter = new DirectoryFilter();
	}

	/**
	 * Returns true if the input is a <code>File</code> object pointing to 
	 * a directory called ".metadata".
	 * 
	 * @return true if this input object is a <code>File</code> pointing to a 
	 * .metadata directory.
	 * @param input an input object 
	 * @see org.eclipse.core.tools.AbstractTreeContentProvider#acceptInput(java.lang.Object)
	 */
	protected boolean acceptInput(Object input) {
		if (!(input instanceof File))
			return false;

		File rootDir = (File) input;
		return (rootDir.isDirectory() && rootDir.getName().equals(".metadata")); //$NON-NLS-1$
	}

	/**
	 * Updates the data model for this content provider upon the provided input.
	 * 
	 * @param input a File object pointing to a .metadata directory.
	 * 
	 * @see org.eclipse.core.tools.AbstractTreeContentProvider#rebuild(java.lang.Object)
	 */
	protected void rebuild(Object input) {
		File metadataRootDir = (File) input;
		TreeContentProviderNode metadataRootNode = makeNode(metadataRootDir);
		getRootNode().addChild(metadataRootNode);
		extractInfo((File) input, metadataRootNode);
	}

	/**
	 * Builds this content provider data model from a given root directory. This 
	 * method operates recursively, adding a tree node for each file of a registered 
	 * type it finds and for each directory that contains (any directories that 
	 * contain) a file of a registered type. This method returns a boolean value 
	 * indicating that it (or at least one of its sub dirs) contains files with one 
	 * of the registered types (so its parent will include it too).
	 * 
	 * @param dir a directory potentially containing known metadata files.
	 * @param dirNode the node corresponding to that directory 
	 * @return true if the provided dir (or at least one of its sub dirs) 
	 * contains files with one of the registered types, false otherwise
	 */
	private boolean extractInfo(File dir, TreeContentProviderNode dirNode) {

		TreeContentProviderNode childNode;

		// looks for files of registered types in this directory	
		File[] selectedFiles = dir.listFiles(fileFilter);
		Arrays.sort(selectedFiles);
		for (int i = 0; i < selectedFiles.length; i++) {
			childNode = makeNode(selectedFiles[i]);
			dirNode.addChild(childNode);
		}

		// looks for files of registered types in its subdirectories
		File[] subDirs = dir.listFiles(directoryFilter);
		Arrays.sort(subDirs);
		for (int i = 0; i < subDirs.length; i++) {
			// constructs a node for each subdir...
			childNode = makeNode(subDirs[i]);
			if (extractInfo(subDirs[i], childNode))
				// ...but only adds them if they have files of registered types
				dirNode.addChild(childNode);
		}
		// returns true if this dir has any file of any registered type
		return selectedFiles.length > 0 || dirNode.hasChildren();
	}

	/**
	 * Helper method that creates a new TreeContentroviderNode object given a File 
	 * object.
	 * 
	 * @param file the file a node will created for
	 * @return a <code>TreeContentProviderNode</code> 
	 */
	private TreeContentProviderNode makeNode(final File file) {
		return new TreeContentProviderNode(file.getName(), file) {
			// redefines toString so the root shows the full path while any other 
			// node shows only the file / directory name
			public String toString() {
				return isRoot() ? file.getAbsolutePath() : file.getName();
			}
		};
	}

	/**
	 * Filters accepted files (the ones who are registered in the DumperFactory).
	 * 
	 * @see WorkspaceContentProvider#WorkspaceContentProvider(String[]) 
	 * @see java.io.FileFilter
	 */
	private class MetadataFileFilter implements FileFilter {
		private String[] fileNames;

		private MetadataFileFilter(String[] fileNames) {
			this.fileNames = fileNames;
			Arrays.sort(this.fileNames);
		}

		/**
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		public boolean accept(File file) {
			return file.isFile() && Arrays.binarySearch(fileNames, file.getName()) >= 0;
		}
	}

	/**
	 * Filters directories entries.
	 * 
	 * @see java.io.FileFilter
	 */
	private class DirectoryFilter implements FileFilter {
		/**
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		public boolean accept(File file) {
			return file.isDirectory();
		}
	}

}