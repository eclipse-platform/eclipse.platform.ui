/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.ltk.core.refactoring.ContentStamp;

public class ContentStamps {
	
	private static class ContentStampImpl extends ContentStamp {
		private long fValue;
		
		private ContentStampImpl(long value) {
			fValue= value;
		}
		public long getValue() {
			return fValue;
		}
		public boolean isNullStamp() {
			return false;
		}
		public boolean equals(Object obj) {
			if (!(obj instanceof ContentStampImpl))
				return false;
			return ((ContentStampImpl)obj).fValue == fValue;
		}
		public int hashCode() {
			return (int)fValue;
		}
		public String toString() {
			return "Stamp: " + fValue; //$NON-NLS-1$
		}
	}
	
	private static class NullContentStamp extends ContentStamp {
		public boolean isNullStamp() {
			return true;
		}
		public String toString() {
			return "Null Stamp"; //$NON-NLS-1$
		}
	}
	
	public static final ContentStamp NULL_CONTENT_STAMP= new NullContentStamp();
	
	public static ContentStamp get(IFile file) {
		long modificationStamp= file.getModificationStamp();
		if (modificationStamp == IResource.NULL_STAMP)
			return NULL_CONTENT_STAMP;
		return new ContentStampImpl(modificationStamp);
	}
	
	public static void set(IFile file, ContentStamp stamp) throws CoreException {
		if (!(stamp instanceof ContentStampImpl))
			return;
		file.revertModificationStamp(((ContentStampImpl)stamp).getValue());
	}	
}
