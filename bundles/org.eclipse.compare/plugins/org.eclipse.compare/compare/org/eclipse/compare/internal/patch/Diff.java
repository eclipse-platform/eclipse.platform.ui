/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal.patch;

import java.util.*;

import org.eclipse.core.runtime.IPath;

import org.eclipse.compare.structuremergeviewer.Differencer;


/* package */ class Diff {
		
	IPath fOldPath, fNewPath;
	long fOldDate, fNewDate;	// if 0: no file
	List fHunks= new ArrayList();
	boolean fMatches= false;
	private boolean fIsEnabled2= true;
	String fRejected;

	
 	/* package */ Diff(IPath oldPath, long oldDate, IPath newPath, long newDate) {
		fOldPath= oldPath;
		fOldDate= oldPath == null ? 0 : oldDate;
		fNewPath= newPath;
		fNewDate= newPath == null ? 0 : newDate;	
	}
	
	boolean isEnabled() {
		return fIsEnabled2;
	}
	
	void setEnabled(boolean b) {
		fIsEnabled2= b;
	}
	
	void reverse() {
		IPath tp= fOldPath;
		fOldPath= fNewPath;
		fNewPath= tp;
		
		long t= fOldDate;
		fOldDate= fNewDate;
		fNewDate= t;
		
		Iterator iter= fHunks.iterator();
		while (iter.hasNext()) {
			Hunk hunk= (Hunk) iter.next();
			hunk.reverse();
		}
	}
	
	Hunk[] getHunks() {
		return (Hunk[]) fHunks.toArray((Hunk[]) new Hunk[fHunks.size()]);
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
		return path.toOSString();
	}
}

