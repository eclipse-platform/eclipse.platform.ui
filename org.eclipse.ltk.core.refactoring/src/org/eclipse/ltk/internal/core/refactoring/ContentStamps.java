/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import org.eclipse.core.resources.IFile;

import org.eclipse.ltk.core.refactoring.ContentStamp;

public class ContentStamps {
	
	private static class ContentStampImpl extends ContentStamp {
		
		public static final int NULL_VALUE= -1;
		
		private int fValue;
		
		private ContentStampImpl(int value) {
			fValue= value;
		}
		public boolean isNullStamp() {
			return fValue == NULL_VALUE;
		}
		public boolean equals(Object obj) {
			if (!(obj instanceof ContentStampImpl))
				return false;
			return ((ContentStampImpl)obj).fValue == fValue;
		}
		public int hashCode() {
			return fValue;
		}
		public String toString() {
			if (fValue == NULL_VALUE)
				return "Null Stamp"; //$NON-NLS-1$
			return "Stamp: " + fValue; //$NON-NLS-1$
		}
	}
	
	public static final ContentStamp NULL_CONTENT_STAMP= new ContentStampImpl(ContentStampImpl.NULL_VALUE);
	
	private static final QualifiedName CONTENT_STAMP= new QualifiedName(
		RefactoringCorePlugin.getPluginId(), 
		"contentStamp"); //$NON-NLS-1$
	
	public static ContentStamp get(IFile file) {
		try {
			ContentStamp result= (ContentStamp)file.getSessionProperty(CONTENT_STAMP);
			if (result != null)
				return result;
			return NULL_CONTENT_STAMP;
		} catch (CoreException e) {
			// fall through
		}
		return NULL_CONTENT_STAMP;
	}
	
	public static ContentStamp get(IFile file, boolean create) {
		ContentStamp result= get(file);
		if (result.isNullStamp() && create) {
			result= new ContentStampImpl(0);
			try {
				file.setSessionProperty(CONTENT_STAMP, result);
			} catch (CoreException e) {
				return NULL_CONTENT_STAMP;
			}
		}
		return result;
	}
	
	public static void remove(IFile file) {
		try {
			file.setSessionProperty(CONTENT_STAMP, null);
		} catch (CoreException e) {
		}
	}
	
	public static void increment(IFile file) {
		try {
			ContentStampImpl stamp= (ContentStampImpl)file.getSessionProperty(CONTENT_STAMP);
			if (stamp == null)
				return;
			file.setSessionProperty(CONTENT_STAMP, new ContentStampImpl(stamp.fValue + 1));
		} catch (CoreException e) {
		}
	}
	
	public static void set(IFile file, ContentStamp stamp) {
		try {
			file.setSessionProperty(CONTENT_STAMP, stamp);
		} catch (CoreException e) {
		}
	}	
}
