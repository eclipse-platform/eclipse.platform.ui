package org.eclipse.ui.internal.editorsupport;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
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
	if (SWT.getPlatform().equals("win32")) {
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
	if ((SWT.getPlatform().equals("win32")) && testForOleEditor(input)) {
		return getOleEditor();
	}
	return null;
}
/**
 * Get a new OLEEditor
 * @return IEditorPart
 */
private static IEditorPart getOleEditor() {
	return new OleEditor();
}
private static boolean testForOleEditor(IFile input) {
	String strName = input.getName();
	int nDot = strName.lastIndexOf('.');
	if (nDot >= 0) {
		strName = strName.substring(nDot);
		strName = org.eclipse.swt.ole.win32.OLE.findProgramID(strName);
		if (strName.length() > 0)
			return true;
	}
	return false;
}
}
