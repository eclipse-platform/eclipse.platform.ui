/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * Internal class used in providing increased binary compatibility for pre-3.0
 * plug-ins. This declaration masks the empty class of the same name declared in
 * the Workbench proper. This class implements IWorkbenchPage that existed in
 * 2.1 but were removed in 3.0 because they referenced resource API.
 * <p>
 * Plug-ins should not refer to this type or its containing fragment from their
 * class path. It is intended only to provide binary compatibility for pre-3.0
 * plug-ins, and should not be referenced at development time.
 * </p>
 * 
 * @since 3.0
 */
public class CompatibleWorkbenchPage implements ICompatibleWorkbenchPage {

    /**
     * openEditor(IFile) is declared on IWorkbenchPage in 2.1. This method was
     * removed in 3.0 because it references resource API.
     */
    public IEditorPart openEditor(IFile input) throws PartInitException {
        // invoke org.eclipse.ui.ide.IDE.openEditor(IWorkbenchPage, IFile,
        // boolean);
        return openEditor(new Class[] { IWorkbenchPage.class, IFile.class,
                boolean.class },
                new Object[] { this, input, new Boolean(true) });
    }

    /**
     * openEditor(IFile,String) is declared on IWorkbenchPage in 2.1. This
     * method was removed in 3.0 because it references resource API.
     */
    public IEditorPart openEditor(IFile input, String editorID)
            throws PartInitException {
        return openEditor(input, editorID, true);
    }

    /**
     * openEditor(IFile,String,boolean) is declared on IWorkbenchPage in 2.1.
     * This method was removed in 3.0 because it references resource API.
     */
    public IEditorPart openEditor(IFile input, String editorID, boolean activate)
            throws PartInitException {
        return ((IWorkbenchPage) this).openEditor(getFileEditorInput(input),
                editorID);
    }

    /**
     * openEditor(IMarker) is declared on IWorkbenchPage in 2.1. This method was
     * removed in 3.0 because it references resource API.
     */
    public IEditorPart openEditor(IMarker marker) throws PartInitException {
        return openEditor(marker, true);
    }

    /**
     * openEditor(IMarker,boolean) is declared on IWorkbenchPage in 2.1. This
     * method was removed in 3.0 because it references resource API.
     */
    public IEditorPart openEditor(IMarker marker, boolean activate)
            throws PartInitException {
        // invoke org.eclipse.ui.ide.IDE.openEditor(IWorkbenchPage, IMarker,
        // boolean);
        return openEditor(new Class[] { IWorkbenchPage.class, IMarker.class,
                boolean.class }, new Object[] { this, marker,
                new Boolean(activate) });
    }

    /**
     * openSystemEditor(IFile) is declared on IWorkbenchPage in 2.1. This method
     * was removed in 3.0 because it references resource API.
     */
    public void openSystemEditor(IFile file) throws PartInitException {
        ((IWorkbenchPage) this).openEditor(getFileEditorInput(file),
                IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);

    }

    /*
     * Implementation support: Use reflection for the following code pattern:
     * new org.eclipse.ui.part.FileEditorInput(file) The class FileEditorInput
     * is found in the org.eclipse.ui.ide plug-in.
     */
    private IEditorInput getFileEditorInput(IFile file)
            throws PartInitException {
        IPluginDescriptor desc = Platform.getPluginRegistry()
                .getPluginDescriptor("org.eclipse.ui.ide"); //$NON-NLS-1$		
        Exception problem;
        try {
            Class clazz = desc.getPluginClassLoader().loadClass(
                    "org.eclipse.ui.part.FileEditorInput"); //$NON-NLS-1$
            Constructor constructor = clazz
                    .getConstructor(new Class[] { IFile.class });
            return (IEditorInput) constructor
                    .newInstance(new Object[] { file });
        } catch (NullPointerException e) {
            problem = e;
        } catch (ClassNotFoundException e) {
            problem = e;
        } catch (NoSuchMethodException e) {
            problem = e;
        } catch (IllegalArgumentException e) {
            problem = e;
        } catch (IllegalAccessException e) {
            problem = e;
        } catch (InvocationTargetException e) {
            problem = e;
        } catch (InstantiationException e) {
            problem = e;
        }
        IStatus status = new Status(
                IStatus.ERROR,
                WorkbenchPlugin.PI_WORKBENCH,
                0,
                "openEditor() compatibility support failed - new FileEditorInput(file)", problem); //$NON-NLS-1$
        WorkbenchPlugin.log(status.getMessage(), status);
        throw new PartInitException(status);
    }

    /*
     * Implementation support: Use reflection to invoke the appropriate static
     * openEditor(...) method on IDE The IDE class is found in the
     * org.eclipse.ui.ide plug-in.
     */
    private IEditorPart openEditor(Class[] argTypes, Object[] args)
            throws PartInitException {
        IPluginDescriptor desc = Platform.getPluginRegistry()
                .getPluginDescriptor("org.eclipse.ui.ide"); //$NON-NLS-1$
        Throwable problem;
        try {
            Class clazz = desc.getPluginClassLoader().loadClass(
                    "org.eclipse.ui.ide.IDE"); //$NON-NLS-1$
            Method method = clazz.getMethod("openEditor", argTypes); //$NON-NLS-1$
            return (IEditorPart) method.invoke(null, args);
        } catch (NullPointerException e) {
            problem = e;
        } catch (ClassNotFoundException e) {
            problem = e;
        } catch (NoSuchMethodException e) {
            problem = e;
        } catch (IllegalArgumentException e) {
            problem = e;
        } catch (IllegalAccessException e) {
            problem = e;
        } catch (InvocationTargetException e) {
            problem = e;
        }
        IStatus status = new Status(
                IStatus.ERROR,
                WorkbenchPlugin.PI_WORKBENCH,
                0,
                "openEditor() compatibility support failed - IDE.openEditor()", problem); //$NON-NLS-1$
        WorkbenchPlugin.log(status.getMessage(), status);
        throw new PartInitException(status);
    }
}
