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

import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;



public class FlatFileSorter implements IFileSorter {

    public static class Node {

		private String fSortKey;
		private IFile fFile;

		public Node(String name, IFile file) {
			fSortKey= name;
			fFile= file;
		}

	}
	private static final int FLAT_KEY_VALUE= Integer.MIN_VALUE;
	private static final Integer FLAT_KEY= new Integer(FLAT_KEY_VALUE);
	private Collator fCollator;
	private HashMap fCollationKeys;

	public FlatFileSorter(Collator collator) {
        fCollator= collator;
    }

    public void sortFiles(IFile[] files) {
		ArrayList nodeList= new ArrayList(files.length);
		for (int i= 0; i < files.length; i++) {
			IFile file= files[i];
			nodeList.add(new Node(file.getName(), file));
		}

		fCollationKeys= new HashMap();
		try {
			sortNodesToFiles(nodeList, 0, files, 0, true);
		} finally {
			fCollationKeys= null;
		}
	}
    
    private void sortNodesToFiles(ArrayList nodeList, int cmpIdx, IFile[] files, int fileIdx, boolean resolveWithDirectory) {
    	int nodeListSize= nodeList.size();
    	switch(nodeListSize) {
    		case 0:
    			return;
    		case 1:
    			files[fileIdx]= ((Node)nodeList.get(0)).fFile;
    		return;
    	}

    	Node node= (Node) nodeList.get(0);
    	int commonChar= getChar(node.fSortKey, cmpIdx);
    	int i= 1;
    	for (; i<nodeListSize; i++) {
    		node= (Node) nodeList.get(i);
    		int c= getChar(node.fSortKey, cmpIdx);
    		if (c != commonChar) {
    			break;
    		}
    	}
    	
    	if (i==nodeListSize) {
    		if (commonChar == FLAT_KEY_VALUE) {
    			handleFlatList(nodeList, files, fileIdx, resolveWithDirectory);
    		}
    		else {
    			sortNodesToFiles(nodeList, cmpIdx+1, files, fileIdx, resolveWithDirectory);
    		}
    		return;
    	}

    	HashMap splitMap= new HashMap();
    	ArrayList firstList= new ArrayList(nodeList.subList(0, i));
    	splitMap.put(new Integer(commonChar), firstList);
    	
    	for (; i<nodeListSize; i++) {
    		node= (Node) nodeList.get(i);
			int c= getChar(node.fSortKey, cmpIdx);
    		addToMap(new Integer(c), node, splitMap);
    	}
    	
    	ArrayList flatList= (ArrayList) splitMap.remove(FLAT_KEY);
    	if (flatList != null) {
    		handleFlatList(flatList, files, fileIdx, resolveWithDirectory);
    		fileIdx+= flatList.size();
    	}
    	
    	SortedMap sorted= new TreeMap();
    	for (Iterator iter= splitMap.entrySet().iterator(); iter.hasNext();) {
    		Map.Entry entry= (Map.Entry) iter.next();
    		CollationKey key= getCollationKey((Integer) entry.getKey());
    		List existing= (List) sorted.get(key);
    		if (existing != null) {
    			existing.addAll((List) entry.getValue());
    		}
    		else {
    			sorted.put(key, entry.getValue());
    		}
    	}
    	for (Iterator iter= sorted.values().iterator(); iter.hasNext();) {
    		ArrayList list= (ArrayList) iter.next();
    		sortNodesToFiles(list, cmpIdx+1, files, fileIdx, resolveWithDirectory);
    		fileIdx+= list.size();
    	}
    }
    	
    private void handleFlatList(ArrayList flatList, IFile[] files, int fileIdx, boolean resolveWithDirectory) {
    	switch (flatList.size()) {
			case 0:
				break;
			case 1:
				files[fileIdx++]= ((Node) flatList.get(0)).fFile;
				break;
			default:
				if (resolveWithDirectory) {
					for (Iterator iter= flatList.iterator(); iter.hasNext();) {
						Node flatNode= (Node) iter.next();
						flatNode.fSortKey= flatNode.fFile.getFullPath().toString();
					}
					sortNodesToFiles(flatList, 0, files, fileIdx, false);
					fileIdx+= flatList.size();
				} else {
					for (Iterator iter= flatList.iterator(); iter.hasNext();) {
						Node flatNode= (Node) iter.next();
						files[fileIdx++]= flatNode.fFile;
					}
				}
				break;
		}
	}

	private int getChar(String sortKey, int cmpIdx) {
		int result;
    	try {
    		result= sortKey.charAt(cmpIdx);
    	}
    	catch(StringIndexOutOfBoundsException e) {
    		result= FLAT_KEY_VALUE;
    	}
    	return result;
	}

	private void addToMap(Object key, Node node, HashMap map) {
		List list= (List) map.get(key);
		if (list == null) {
			list= new ArrayList();
			map.put(key, list);
		}
		list.add(node);
	}

	private CollationKey getCollationKey(Integer ch) {
		CollationKey key= (CollationKey) fCollationKeys.get(ch);
		if (key == null) {
			key= fCollator.getCollationKey(String.valueOf((char) ch.intValue()));
			fCollationKeys.put(ch, key);
		}
		return key;
	}
}
