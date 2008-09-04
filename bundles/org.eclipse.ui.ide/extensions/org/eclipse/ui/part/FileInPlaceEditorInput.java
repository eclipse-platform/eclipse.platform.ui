/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.part;

import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;

import org.eclipse.ui.IInPlaceEditor;
import org.eclipse.ui.IInPlaceEditorInput;

/**
 * Adapter for making a file resource a suitable input for an in-place editor.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class FileInPlaceEditorInput extends FileEditorInput implements
        IInPlaceEditorInput {
    IInPlaceEditor embeddedEditor;

    /**
     * A resource listener to update the input and in-place
     * editor if the input's file resource changes.
     */
    private IResourceChangeListener resourceListener = new IResourceChangeListener() {
        public void resourceChanged(IResourceChangeEvent event) {
            IResourceDelta mainDelta = event.getDelta();
            if (mainDelta != null && embeddedEditor != null) {
                IResourceDelta affectedElement = mainDelta.findMember(getFile()
                        .getFullPath());
                if (affectedElement != null) {
                	processDelta(affectedElement);
                }
            }
        }

        private boolean processDelta(final IResourceDelta delta) {
            Runnable changeRunnable = null;

            switch (delta.getKind()) {
            case IResourceDelta.REMOVED:
                if ((IResourceDelta.MOVED_TO & delta.getFlags()) != 0) {
                    changeRunnable = new Runnable() {
                        public void run() {
                            IPath path = delta.getMovedToPath();
                            IFile newFile = delta.getResource().getWorkspace()
                                    .getRoot().getFile(path);
                            if (newFile != null && embeddedEditor != null) {
                                embeddedEditor
                                        .sourceChanged(new FileInPlaceEditorInput(
                                                newFile));
                            }
                        }
                    };
                } else {
                    changeRunnable = new Runnable() {
                        public void run() {
                            if (embeddedEditor != null) {
                                embeddedEditor.sourceDeleted();
                                embeddedEditor.getSite().getPage().closeEditor(
                                        embeddedEditor, true);
                            }
                        }
                    };

                }

                break;
            }

            if (changeRunnable != null && embeddedEditor != null) {
                embeddedEditor.getSite().getShell().getDisplay().asyncExec(
                        changeRunnable);
            }

            return true; // because we are sitting on files anyway
        }
    };

    /**
     * Creates an in-place editor input based on a file resource.
     *
     * @param file the file resource
     */
    public FileInPlaceEditorInput(IFile file) {
        super(file);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IInPlaceEditorInput#setInPlaceEditor(org.eclipse.ui.IInPlaceEditor)
     */
    public void setInPlaceEditor(IInPlaceEditor editor) {
        if (embeddedEditor != editor) {
            if (embeddedEditor != null) {
                getFile().getWorkspace().removeResourceChangeListener(
                        resourceListener);
            }

            embeddedEditor = editor;

            if (embeddedEditor != null) {
                getFile().getWorkspace().addResourceChangeListener(
                        resourceListener);
            }
        }
    }
}
