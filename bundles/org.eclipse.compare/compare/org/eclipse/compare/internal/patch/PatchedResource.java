/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal.patch;

import java.io.*;

import org.eclipse.swt.graphics.Image;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;

import org.eclipse.compare.*;


/* package */ class PatchedResource implements ITypedElement, IStreamContentAccessor {
	
	private Diff fDiff;
	private IStreamContentAccessor fCurrent;
	private IPath fPath;
	private byte[] fContent;
	private Patcher fPatcher;
	
	/* package */ PatchedResource(IStreamContentAccessor current, Diff diff, IPath path, Patcher patcher) {
		fDiff= diff;
		fCurrent= current;
		fPath= path;
		fPatcher= patcher;
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
				
				try {
					String encoding= ResourcesPlugin.getEncoding();
					BufferedReader br= new BufferedReader(new InputStreamReader(is, encoding));
					String s= fPatcher.patch(fDiff,br, null);
					if (s != null)
						fContent= s.getBytes(encoding);
				} catch (UnsupportedEncodingException e) {
					throw new CoreException(new Status(IStatus.ERROR, CompareUI.PLUGIN_ID, Platform.PLUGIN_ERROR, e.getMessage(), e));
				}
					
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

