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
package org.eclipse.ui.internal.editorsupport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.*;
import org.eclipse.swt.SWT;
/**
 * This class provides an OS independent interface to the
 * components available on the platform
 */
public final class ComponentSupport {
	/**
	 * Return the default component Editor.
	 * <p>
	 * Check if we are on Win32 - if so then return an OLEEditor, if not return
	 * null.
	 * @see ComponentSupport
	 */
	public static IEditorPart getComponentEditor() {
		if (SWT.getPlatform().equals("win32")) { //$NON-NLS-1$
			return getOleEditor();
		}
		return null;
	}
	/**
	 * Return Component Editor associated with the file input
	 * is to be opened on. Only return one if in Win32.
	 * <p>
	 * @param IFile the input on which the componenet editor
	 * @return IEditorPart or <code>null</code> if no editor exists
	 * @see ComponentSupport
	 */
	public static IEditorPart getComponentEditor(IFile input) {
		if ((SWT.getPlatform().equals("win32")) //$NON-NLS-1$
			&& testForOleEditor(input)) { //$NON-NLS-1$
			return getOleEditor();
		}
		return null;
	}
	/**
	 * Get a new OLEEditor
	 * @return IEditorPart
	 */
	private static IEditorPart getOleEditor() {
		try {
			Class oleEditorClass =
				Class.forName("org.eclipse.ui.internal.editorsupport.win32.OleEditor"); //$NON-NLS-1$
			return (IEditorPart) oleEditorClass.newInstance();
		} catch (ClassNotFoundException exception) {
			return null;
		}
		catch (IllegalAccessException exception) {
			return null;
		}
		catch (InstantiationException exception) {
			return null;
		}
	}

	public static boolean testForOleEditor(IFile input) {
		String strName = input.getName();
		int nDot = strName.lastIndexOf('.');
		if (nDot >= 0) {
			try {
				strName = strName.substring(nDot);
				Class oleClass = Class.forName("org.eclipse.swt.ole.win32.OLE"); //$NON-NLS-1$
				Method findMethod =
					oleClass.getDeclaredMethod("findProgramID", new Class[] { String.class }); //$NON-NLS-1$
				strName = (String) findMethod.invoke(null, new Object[] { strName });
				if (strName.length() > 0)
					return true;
			} catch (ClassNotFoundException exception) {
				//Couldn't ask so return false
				return false;
			}
			catch (NoSuchMethodException exception) {
				//Couldn't find the method so return false
				return false;
			}
			catch (IllegalAccessException exception) {
				return false;
			}
			catch (InvocationTargetException exception) {
				return false;
			}

		}
		return false;
	}
}
