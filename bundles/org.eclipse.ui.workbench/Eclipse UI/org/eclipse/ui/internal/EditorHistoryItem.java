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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;

/**
 * An item in the editor history.
 */
public class EditorHistoryItem {

    private IEditorInput input;

    private IEditorDescriptor descriptor;

    private IMemento memento;

    /**
     * Constructs a new item.
     */
    public EditorHistoryItem(IEditorInput input, IEditorDescriptor descriptor) {
        this.input = input;
        this.descriptor = descriptor;
    }

    /**
     * Constructs a new item from a memento.
     */
    public EditorHistoryItem(IMemento memento) {
        this.memento = memento;
    }

    /**
     * Returns the editor descriptor.
     * 
     * @return the editor descriptor.
     */
    public IEditorDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Returns the editor input.
     * 
     * @return the editor input.
     */
    public IEditorInput getInput() {
        return input;
    }

    /**
     * Returns whether this item has been restored from the memento.
     */
    public boolean isRestored() {
        return memento == null;
    }

    /**
     * Returns the name of this item, either from the input if restored,
     * otherwise from the memento.
     */
    public String getName() {
        if (isRestored() && getInput() != null) {
            return getInput().getName();
        } else if (memento != null) {
            String name = memento.getString(IWorkbenchConstants.TAG_NAME);
            if (name != null) {
                return name;
            }
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Returns the tooltip text of this item, either from the input if restored,
     * otherwise from the memento.
     */
    public String getToolTipText() {
        if (isRestored() && getInput() != null) {
            return getInput().getToolTipText();
        } else if (memento != null) {
            String name = memento.getString(IWorkbenchConstants.TAG_TOOLTIP);
            if (name != null) {
                return name;
            }
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Returns whether this item matches the given editor input.
     */
    public boolean matches(IEditorInput input) {
        if (isRestored()) {
			return input.equals(getInput());
		}
        // if not restored, compare name, tool tip text and factory id,
        // avoiding as much work as possible
        if (!getName().equals(input.getName())) {
			return false;
		}
        if (!getToolTipText().equals(input.getToolTipText())) {
			return false;
		}
        IPersistableElement persistable = input.getPersistable();
        String inputId = persistable == null ? null : persistable
                .getFactoryId();
        String myId = getFactoryId();
        return myId == null ? inputId == null : myId.equals(inputId);
    }

    /**
     * Returns the factory id of this item, either from the input if restored,
     * otherwise from the memento.
     * Returns <code>null</code> if there is no factory id.
     */
    public String getFactoryId() {
        if (isRestored()) {
            if (input != null) {
                IPersistableElement persistable = input.getPersistable();
                if (persistable != null) {
                    return persistable.getFactoryId();
                }
            }
        } else if (memento != null) {
            return memento.getString(IWorkbenchConstants.TAG_FACTORY_ID);
        }
        return null;
    }

    /**
     * Restores the object state from the memento. 
     */
    public IStatus restoreState() {
        Assert.isTrue(!isRestored());

        Status result = new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0,
                "", null); //$NON-NLS-1$
        IMemento memento = this.memento;
        this.memento = null;

        String factoryId = memento
                .getString(IWorkbenchConstants.TAG_FACTORY_ID);
        if (factoryId == null) {
            WorkbenchPlugin
                    .log("Unable to restore mru list - no input factory ID.");//$NON-NLS-1$
            return result;
        }
        IElementFactory factory = PlatformUI.getWorkbench().getElementFactory(
                factoryId);
        if (factory == null) {
            return result;
        }
        IMemento persistableMemento = memento
                .getChild(IWorkbenchConstants.TAG_PERSISTABLE);
        if (persistableMemento == null) {
            WorkbenchPlugin
                    .log("Unable to restore mru list - no input element state: " + factoryId);//$NON-NLS-1$
            return result;
        }
        IAdaptable adaptable = factory.createElement(persistableMemento);
        if (adaptable == null || (adaptable instanceof IEditorInput) == false) {
            return result;
        }
        input = (IEditorInput) adaptable;
        // Get the editor descriptor.
        String editorId = memento.getString(IWorkbenchConstants.TAG_ID);
        if (editorId != null) {
            IEditorRegistry registry = WorkbenchPlugin.getDefault()
                    .getEditorRegistry();
            descriptor = registry.findEditor(editorId);
        }
        return result;
    }

    /**
     * Returns whether this history item can be saved.
     */
    public boolean canSave() {
        return !isRestored()
                || (getInput() != null && getInput().getPersistable() != null);
    }

    /**
     * Saves the object state in the given memento. 
     * 
     * @param memento the memento to save the object state in
     */
    public IStatus saveState(IMemento memento) {
        if (!isRestored()) {
            memento.putMemento(this.memento);
        } else if (input != null) {

            IPersistableElement persistable = input.getPersistable();
            if (persistable != null) {
                /*
                 * Store IPersistable of the IEditorInput in a separate section
                 * since it could potentially use a tag already used in the parent 
                 * memento and thus overwrite data.
                 */
                IMemento persistableMemento = memento
                        .createChild(IWorkbenchConstants.TAG_PERSISTABLE);
                persistable.saveState(persistableMemento);
                memento.putString(IWorkbenchConstants.TAG_FACTORY_ID,
                        persistable.getFactoryId());
                if (descriptor != null && descriptor.getId() != null) {
                    memento.putString(IWorkbenchConstants.TAG_ID, descriptor
                            .getId());
                }
                // save the name and tooltip separately so they can be restored
                // without having to instantiate the input, which can activate plugins
                memento
                        .putString(IWorkbenchConstants.TAG_NAME, input
                                .getName());
                memento.putString(IWorkbenchConstants.TAG_TOOLTIP, input
                        .getToolTipText());
            }
        }
        return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
    }

}
