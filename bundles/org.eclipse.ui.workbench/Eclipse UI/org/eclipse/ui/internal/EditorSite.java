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

import java.util.ArrayList;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.SubActionBars2;
import org.eclipse.ui.dnd.IDragAndDropService;
import org.eclipse.ui.internal.registry.EditorDescriptor;

/**
 * An editor container manages the services for an editor.
 */
public class EditorSite extends PartSite implements IEditorSite {
    /* package */ //static final int PROP_REUSE_EDITOR = -0x101;

    private EditorDescriptor desc;

    //private ListenerList propChangeListeners = new ListenerList(1);

    private SubActionBars ab = null;
    
    /**
     * Constructs an EditorSite for an editor.
     */
    public EditorSite(IEditorReference ref, IEditorPart editor,
            WorkbenchPage page, EditorDescriptor desc) {
        super(ref, editor, page);
        Assert.isNotNull(desc);
        this.desc = desc;
        
        if (desc.getConfigurationElement() != null) {
            setConfigurationElement(desc.getConfigurationElement());
        } else {
            // system external and in-place editors do not have a corresponding configuration element
            setId(desc.getId());
            setRegisteredName(desc.getLabel());
        }

		// Initialize the services specific to this editor site.
        initializeDefaultServices();
    }

	/**
	 * Initialize the local services.
	 */
	private void initializeDefaultServices() {
		// Register an implementation of the service appropriate for the 
		// EditorSite.
		final IDragAndDropService editorDTService = new EditorSiteDragAndDropServiceImpl();
		serviceLocator.registerService(IDragAndDropService.class, editorDTService);
	}
	
    public void setActionBars(SubActionBars bars) {
        super.setActionBars(bars);
        
        if (bars instanceof IActionBars2) {
            ab = new SubActionBars2((IActionBars2)bars, this);
        } else {
            ab = new SubActionBars(bars, this);
        }
    }
    
    public void activateActionBars(boolean forceVisibility) {
        if (ab != null) {
            ab.activate(forceVisibility);
        }
        super.activateActionBars(forceVisibility);
    }

    public void deactivateActionBars(boolean forceHide) {
        if (ab != null) {
            ab.deactivate(forceHide);
        }
        super.deactivateActionBars(forceHide);
    }
    
    /**
     * Returns the editor action bar contributor for this editor.
     * <p>
     * An action contributor is responsable for the creation of actions.
     * By design, this contributor is used for one or more editors of the same type.
     * Thus, the contributor returned by this method is not owned completely
     * by the editor.  It is shared.
     * </p>
     *
     * @return the editor action bar contributor
     */
    public IEditorActionBarContributor getActionBarContributor() {
        EditorActionBars bars = (EditorActionBars) getActionBars();
        if (bars != null) {
			return bars.getEditorContributor();
		}
        
        return null;
    }

    /**
     * Returns the extension editor action bar contributor for this editor.
     */
    public IEditorActionBarContributor getExtensionActionBarContributor() {
        EditorActionBars bars = (EditorActionBars) getActionBars();
        if (bars != null) {
			return bars.getExtensionContributor();
		}
        
        return null;
    }

    /**
     * Returns the editor
     */
    public IEditorPart getEditorPart() {
        return (IEditorPart) getPart();
    }

    public EditorDescriptor getEditorDescriptor() {
        return desc;
    }

    protected String getInitialScopeId() {
        return "org.eclipse.ui.textEditorScope"; //$NON-NLS-1$
    }
    
    public void dispose() {
        super.dispose();
        
        if (ab != null) {
            ab.dispose();
        }
    }
    
    public final void registerContextMenu(final MenuManager menuManager,
            final ISelectionProvider selectionProvider,
            final boolean includeEditorInput) {
        registerContextMenu(getId(), menuManager, selectionProvider,
                includeEditorInput);
    }
    
    public final void registerContextMenu(final String menuId,
            final MenuManager menuManager,
            final ISelectionProvider selectionProvider,
            final boolean includeEditorInput) {
        if (menuExtenders == null) {
            menuExtenders = new ArrayList(1);
        }
        
        PartSite.registerContextMenu(menuId, menuManager, selectionProvider,
                includeEditorInput, getPart(), menuExtenders);
    }
}
