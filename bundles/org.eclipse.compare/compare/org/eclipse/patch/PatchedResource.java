/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.patch;

import java.io.InputStream;
import java.io.ByteArrayInputStream;

import org.eclipse.compare.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;


/* package */ class PatchedResource implements ITypedElement, IStreamContentAccessor {
	
	Diff fDiff;
	IStreamContentAccessor fCurrent;
	String fName;
	
	/* package */ PatchedResource(IStreamContentAccessor current, Diff diff, String name) {
		fDiff= diff;
		fCurrent= current;
		fName= name;
	}
	
	public InputStream getContents() throws CoreException {
		InputStream is= null;
		try {
			is= fCurrent.getContents();
		} catch (CoreException ex) {
			is= new ByteArrayInputStream(new byte[0]);
		}
		return fDiff.patch(is);
	}
	
	public Image getImage() {
		return CompareUI.getImage(getType());
	}
	
	public String getName() {
		return fName;
	}
	
	public String getType() {
		int pos= fName.indexOf('.');
		if (pos >= 0)
			return fName.substring(pos+1);
//		if (fResource instanceof IContainer)
//			return ITypedElement.FOLDER_TYPE;
//		if (fResource != null) {
//			String s= fResource.getFileExtension();
//			if (s != null)
//				return s;
//		}
		return ITypedElement.UNKNOWN_TYPE;
	}

}

