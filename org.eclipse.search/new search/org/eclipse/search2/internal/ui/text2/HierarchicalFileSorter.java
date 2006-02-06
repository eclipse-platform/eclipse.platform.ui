/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;



/**
 * A file sorter that efficently sorts files as they appear in a hierarchy.
 * The collator is used to sort files on one level, folders are assumed to
 * appear ahead of the files.
 */
public class HierarchicalFileSorter implements IFileSorter {
    private final static Object FILE_KEY= new Object();
    
    private Collator fCollator;
    private Comparator fFilenameComparator = new Comparator() {

        public int compare(Object o1, Object o2) {
            return fCollator.compare(((IResource) o1).getName(), ((IResource) o2).getName());
        }
    };

    public HierarchicalFileSorter(Collator collator) {
        fCollator= collator;
    }

    public void sortFiles(IFile[] files) {
        HashMap all= new HashMap();
        ArrayList roots= new ArrayList();
        for (int i= 0; i < files.length; i++) {
			IFile file= files[i];
            HashMap map= getMap(roots, all, file.getParent());
            ArrayList list= (ArrayList) map.get(FILE_KEY);
            if (list == null) {
                list= new ArrayList();
                map.put(FILE_KEY, list);
            }
            list.add(file);
        }
        int nextIndex= 0;
        for (Iterator iterator = roots.iterator(); iterator.hasNext();) {
            HashMap map = (HashMap) iterator.next();
            nextIndex= addToList(map, files, nextIndex);
        }
    }
    private int addToList(HashMap map, IFile[] files, int nextIndex) {
        List localFiles= (List) map.remove(FILE_KEY);
        ArrayList folders= new ArrayList(map.keySet());
        Collections.sort(folders, fFilenameComparator);
        for (int i = 0; i < folders.size(); i++) {
            Object folder = folders.get(i);
            nextIndex= addToList((HashMap) map.get(folder), files, nextIndex);
        }
        if (localFiles != null) {
            Collections.sort(localFiles, fFilenameComparator);
            for (Iterator iter= localFiles.iterator(); iter.hasNext();) {
				files[nextIndex++]= (IFile) iter.next();
			}
        }
        return nextIndex;
    }
    private HashMap getMap(ArrayList roots, HashMap all, IContainer cont) {
        HashMap result= (HashMap) all.get(cont);
        if (result == null) {
            result= new HashMap();
            if (cont.getType() == IResource.PROJECT) {
                roots.add(result);
            }
            else {
                HashMap parentMap= getMap(roots, all, cont.getParent());
                parentMap.put(cont, result);
            }
            all.put(cont, result);
        }
        return result;
    }
}
