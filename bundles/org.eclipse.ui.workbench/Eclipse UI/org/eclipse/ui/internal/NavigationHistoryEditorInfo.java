/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;

/**
 * Keeps the info to save, restore or identify and editor.
 * Instances of this class are shared between history entries and there should be
 * only one instance making reference to the same editor. 
 */
public class NavigationHistoryEditorInfo {
    String editorID;

    IEditorInput editorInput;

    int refCount = 0;

    IMemento memento;

    NavigationHistoryEditorInfo(IEditorPart part) {
        editorID = part.getSite().getId();
        editorInput = part.getEditorInput();
    }

    NavigationHistoryEditorInfo(IMemento memento) {
        this.memento = memento;
    }

    boolean isPersistable() {
        if (editorInput != null) {
            IPersistableElement persistable = editorInput.getPersistable();
            return persistable != null;
        }
        return memento != null;
    }

    void handlePartClosed() {
        if (!isPersistable()) {
			return;
		}
        if (memento == null) {
            IPersistableElement persistable = editorInput.getPersistable();
            memento = XMLMemento
                    .createWriteRoot(IWorkbenchConstants.TAG_EDITOR);
            memento.putString(IWorkbenchConstants.TAG_ID, editorID);
            memento.putString(IWorkbenchConstants.TAG_FACTORY_ID, persistable
                    .getFactoryId());
            persistable.saveState(memento);
        }
        editorID = null;
        editorInput = null;
    }

    void restoreEditor() {
        if (memento == null) {
			return;
		}
        String factoryID = memento
                .getString(IWorkbenchConstants.TAG_FACTORY_ID);
        IElementFactory factory = PlatformUI.getWorkbench().getElementFactory(
                factoryID);
        if (factory != null) {
            IAdaptable element = factory.createElement(memento);
            if (element instanceof IEditorInput) {
                editorInput = (IEditorInput) element;
                editorID = memento.getString(IWorkbenchConstants.TAG_ID);
            }
        }
        memento = null;
    }

    void saveState(IMemento mem) {
        if (editorInput != null) {
            IPersistableElement persistable = editorInput.getPersistable();
            mem.putString(IWorkbenchConstants.TAG_ID, editorID);
            mem.putString(IWorkbenchConstants.TAG_FACTORY_ID, persistable
                    .getFactoryId());
            persistable.saveState(mem);
        } else if (memento != null) {
            mem.putMemento(memento);
        }
    }
}
