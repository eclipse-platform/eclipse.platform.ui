/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.patch;

import java.io.*;

import org.eclipse.core.runtime.IPath;

import org.eclipse.compare.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;


/* package */ class PatchedResource implements ITypedElement, IStreamContentAccessor {
	
	Diff fDiff;
	IStreamContentAccessor fCurrent;
	IPath fPath;
	byte[] fContent;
	
	/* package */ PatchedResource(IStreamContentAccessor current, Diff diff, IPath path) {
		fDiff= diff;
		fCurrent= current;
		fPath= path;
	}
	
	public InputStream getContents() throws CoreException {
		if (fContent == null) {
			InputStream is= null;
			
			try {
				is= fCurrent.getContents();
			} catch (CoreException ex) {
				is= new ByteArrayInputStream(new byte[0]);
			}
			if (is != null) {
				String s= fDiff.patch(is);
				if (s != null)
					fContent= s.getBytes();
				try {
					is.close();
				} catch (IOException ex) {
				}
			}
		}
		return new ByteArrayInputStream(fContent);
	}
	
	public Image getImage() {
		return CompareUI.getImage(getType());
	}
	
	public String getName() {
		return fPath.toOSString();
	}
	
	public String getType() {
		String type= fPath.getFileExtension();
		if (type != null)
			return type;
		return ITypedElement.UNKNOWN_TYPE;
	}

}

