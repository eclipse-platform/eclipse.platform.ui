/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.filesystem.PrefixPool;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;

/**
 * This class provides information regarding the structure and
 * content of specified file system File objects.
 * 
 * class copied from org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider as its singleton
 */
public class FileSystemStructureProvider implements IImportStructureProvider {

    private PrefixPool pathPrefixHistory;
    private PrefixPool rootPathHistory; 

    /**
     * Creates an instance of <code>FileSystemStructureProvider</code>.
     */
    public FileSystemStructureProvider() {
        super();
    }

    /* (non-Javadoc)
     * Method declared on IImportStructureProvider
     */
    public List getChildren(Object element) {
        File folder = (File) element;
        String[] children = folder.list();
        int childrenLength = children == null ? 0 : children.length;
        List result = new ArrayList(childrenLength);

        for (int i = 0; i < childrenLength; i++) {
        	File file = new File(folder, children[i]);
        	if(file.isDirectory() && isRecursiveLink(folder, file))
        		continue;
        	result.add(file);
		}
        
        return result;
    }

    private void initLinkHistoriesIfNeeded(){
    	if(pathPrefixHistory == null){
    		pathPrefixHistory = new PrefixPool(20);
    		rootPathHistory = new PrefixPool(20);
    	}
    }
    
	private boolean isRecursiveLink(File parentFile, File childFile) {

		boolean isRecursive = false;
		try {

			//Need canonical paths to check all other possibilities
			String parentPath = parentFile.getCanonicalPath() + '/';
			String childPath = childFile.getCanonicalPath() + '/';
			
			//get or instantiate the prefix and root path histories.
			//Might be done earlier - for now, do it on demand.
			
			initLinkHistoriesIfNeeded();
			
			//insert the parent for checking loops
			pathPrefixHistory.insertLonger(parentPath);
			if (pathPrefixHistory.containsAsPrefix(childPath)) {
				//found a potential loop: is it spanning up a new tree?
				if (!rootPathHistory.insertShorter(childPath)) {
					//not spanning up a new tree, so it is a real loop.
					isRecursive = true;
				}
			} else if (rootPathHistory.hasPrefixOf(childPath)) {
				//child points into a different portion of the tree that we visited already before, or will certainly visit.
				//This does not introduce a loop yet, but introduces duplicate resources.
				//TODO Ideally, such duplicates should be modelled as linked resources. See bug 105534
				isRecursive = false;
			} else {
				//child neither introduces a loop nor points to a known tree.
				//It probably spans up a new tree of potential prefixes.
				rootPathHistory.insertShorter(childPath);
			}
		} catch (IOException e) {
			//ignore
		}
		return isRecursive;
	}

	/* (non-Javadoc)
     * Method declared on IImportStructureProvider
     */
    public InputStream getContents(Object element) {
        try {
            return new FileInputStream((File) element);
        } catch (FileNotFoundException e) {
        	IDEWorkbenchPlugin.log(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /* (non-Javadoc)
     * Method declared on IImportStructureProvider
     */
    public String getFullPath(Object element) {
        return ((File) element).getPath();
    }

    /* (non-Javadoc)
     * Method declared on IImportStructureProvider
     */
    public String getLabel(Object element) {

        //Get the name - if it is empty then return the path as it is a file root
        File file = (File) element;
        String name = file.getName();
        if (name.length() == 0) {
			return file.getPath();
		}
        return name;
    }

    /* (non-Javadoc)
     * Method declared on IImportStructureProvider
     */
    public boolean isFolder(Object element) {
        return ((File) element).isDirectory();
    }
}
