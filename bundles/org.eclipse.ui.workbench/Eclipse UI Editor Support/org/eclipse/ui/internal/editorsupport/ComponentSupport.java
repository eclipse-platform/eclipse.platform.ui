/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editorsupport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.internal.util.PrefUtil;
import org.osgi.framework.Bundle;

/**
 * This class provides an OS independent interface to the
 * components available on the platform
 */
public final class ComponentSupport {

    /**
     * Returns whether the current platform has support
     * for system in-place editor.
     */
    public static boolean inPlaceEditorSupported() {
    	// check preference
    	if (PrefUtil.getAPIPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.DISABLE_OPEN_EDITOR_IN_PLACE)) {
    		return false;
    	}
        // only Win32 is supported
        return SWT.getPlatform().equals("win32"); //$NON-NLS-1$
    }

    /**
     * Return the default system in-place editor part
     * or <code>null</code> if not support by platform.
     */
    public static IEditorPart getSystemInPlaceEditor() {
        if (inPlaceEditorSupported()) {
            return getOleEditor();
        }
        return null;
    }

    /**
     * Returns whether an in-place editor is available to
     * edit the file.
     * 
     * @param filename the file name in the system
     */
    public static boolean inPlaceEditorAvailable(String filename) {
        if (inPlaceEditorSupported()) {
            return testForOleEditor(filename);
        } else {
            return false;
        }
    }

    /**
     * Get a new OLEEditor
     * @return IEditorPart
     */
    private static IEditorPart getOleEditor() {
        // @issue currently assumes OLE editor is provided by IDE plug-in
        // IDE plug-in is not on prereq chain of generic wb plug-in
        // hence: IContributorResourceAdapter.class won't compile
        // and Class.forName("org.eclipse.ui.internal.editorsupport.win32.OleEditor") won't find it
        // need to be trickier...
        Bundle bundle = Platform.getBundle("org.eclipse.ui.ide"); //$NON-NLS-1$

        // it's not our job to activate the plug-in
        if (!BundleUtility.isActivated(bundle)) {
			return null;
		}

        try {
            Class c = bundle
                    .loadClass("org.eclipse.ui.internal.editorsupport.win32.OleEditor"); //$NON-NLS-1$
            return (IEditorPart) c.newInstance();
        } catch (ClassNotFoundException exception) {
            return null;
        } catch (IllegalAccessException exception) {
            return null;
        } catch (InstantiationException exception) {
            return null;
        }
    }

    public static boolean testForOleEditor(String filename) {
        int nDot = filename.lastIndexOf('.');
        if (nDot >= 0) {
            try {
                String strName = filename.substring(nDot);
                Class oleClass = Class.forName("org.eclipse.swt.ole.win32.OLE"); //$NON-NLS-1$
                Method findMethod = oleClass.getDeclaredMethod(
                        "findProgramID", new Class[] { String.class }); //$NON-NLS-1$
                strName = (String) findMethod.invoke(null,
                        new Object[] { strName });
                if (strName.length() > 0) {
					return true;
				}
            } catch (ClassNotFoundException exception) {
                //Couldn't ask so return false
                return false;
            } catch (NoSuchMethodException exception) {
                //Couldn't find the method so return false
                return false;
            } catch (IllegalAccessException exception) {
                return false;
            } catch (InvocationTargetException exception) {
                return false;
            }

        }
        return false;
    }
}
