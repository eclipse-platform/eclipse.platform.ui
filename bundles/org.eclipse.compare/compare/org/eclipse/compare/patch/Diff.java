/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.patch;

import java.util.*;
import java.io.*;

import org.eclipse.core.runtime.IPath;

import org.eclipse.compare.structuremergeviewer.Differencer;


/* package */ class Diff {
		
	IPath fOldPath, fNewPath;
	long fOldDate, fNewDate;	// if 0: no file
	List fHunks= new ArrayList();
	
 	/* package */ Diff(IPath oldPath, long oldDate, IPath newPath, long newDate) {
		
		if (oldPath == null)
			oldDate= 0;
			
		if (newPath == null)
			newDate= 0;
		
		fOldPath= oldPath;
		fOldDate= oldDate;
		fNewPath= newPath;
		fNewDate= newDate;	
	}
	
	IPath getPath() {
		if (fOldPath != null)
			return fOldPath;
		return fNewPath;
	}
	
	void finish() {
		if (fHunks.size() == 1) {
			Hunk h= (Hunk) fHunks.get(0);
			if (h.fNewLength == 0) {
				fNewDate= 0;
				fNewPath= fOldPath;
			}
		}
	}
	
	/* package */ void add(Hunk hunk) {
		fHunks.add(hunk);
	}
	
	/* package */ int getType() {
		if (fOldDate == 0)
			return Differencer.ADDITION;
		if (fNewDate == 0)
			return Differencer.DELETION;
		return Differencer.CHANGE;
	}
	
	/* package */ String getDescription(int strip) {
		IPath path= fOldPath;
		if (fOldDate == 0)
			path= fNewPath;
		if (strip > 0 && strip < path.segmentCount())
			path= path.removeFirstSegments(strip);
		String s= path.toOSString();
		return s;
//		if (fOldDate == 0)
//			return "+ " + s;
//		if (fNewDate == 0)
//			return "- " + s;
//		return "! " + s;
	}
	
//	/* package */ int tryPatch(BufferedReader reader) {
//		List lines= new LineReader(reader).readLines();
//		if (lines == null)
//			lines= new ArrayList();
//
//		int shift= 0;
//		Iterator iter= fHunks.iterator();
//		while (iter.hasNext()) {
//			Hunk hunk= (Hunk) iter.next();
//			shift= hunk.tryPatch(lines, shift);
//		}
//		
//		return 0;
//	}
		
	/* package */ String patch(InputStream is) {
		return patch(new BufferedReader(new InputStreamReader(is)));
	}
		
	/* package */ String patch(BufferedReader reader) {
		List lines= new LineReader(reader).readLines();
		if (lines == null)
			lines= new ArrayList();

		int shift= 0;
		Iterator iter= fHunks.iterator();
		while (iter.hasNext()) {
			Hunk hunk= (Hunk) iter.next();
			shift= hunk.patch(lines, shift);
		}
		
		StringBuffer sb= new StringBuffer();
		iter= lines.iterator();
		while (iter.hasNext())
			sb.append((String)iter.next());
		return sb.toString();
	}
	
	/* package */ void compare(List l1, List l2) {
		Iterator i1= l1.iterator();
		Iterator i2= l2.iterator();
		while (i1.hasNext() && i2.hasNext()) {	
			String s1= (String) i1.next();
			String s2= (String) i2.next();
			if (! Hunk.equals(s1, s2)) {
				System.out.println("mismatch: <" + s1 + "> <" + s2 + ">");
			}
		}
		if (i1.hasNext() || i2.hasNext())
			System.out.println("mismatch: premature EOF");
	}

	/* package */ void dump() {
		System.out.println(getDescription(0));
		Iterator iter= fHunks.iterator();
		while (iter.hasNext()) {
			Hunk hunk= (Hunk) iter.next();
			hunk.dump();
		}
	}
}

