/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPart2;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.Components;
import org.eclipse.ui.internal.part.components.services.IActionBarContributor;
import org.eclipse.ui.internal.part.components.services.IDirtyHandler;
import org.eclipse.ui.internal.part.components.services.INameable;
import org.eclipse.ui.internal.part.services.NullActionBars;

/**
 * @since 3.1
 */
public class OldEditorToNewWrapper extends OldPartToNewWrapper {
	private CompatibilityPartSite site;
	
    private IActionBarContributor actionBarContributor;

	private final IDirtyHandler dirtyHandler;
    
	public OldEditorToNewWrapper(IEditorPart part, StandardWorkbenchServices services, IDirtyHandler dirtyHandler) throws CoreException, ComponentException {
        super(services);
        
		this.dirtyHandler = dirtyHandler;
        actionBarContributor = services.getActionBarContributorFactory().getContributor(services.getDescriptor());
        
        IActionBars actionBars = (IActionBars)Components.getAdapter(actionBarContributor, IActionBars.class);
        
        if (actionBars == null) {
            actionBars = new NullActionBars();
        }
        
		site = new CompatibilityPartSite(
                services, part, 
                (IEditorActionBarContributor)Components.getAdapter(actionBarContributor, IEditorActionBarContributor.class), 
                actionBars);
				
		try {
			part.init(site, services.getEditorInput());
		} catch (PartInitException e) {
			throw new ComponentException(part.getClass(), e);
		}

		part.createPartControl(services.getParentComposite());
		
		setPart(part);
		
		INameable nameable = services.getNameable();
		nameable.setName(getPartName());
		nameable.setContentDescription(getContentDescription());
		nameable.setTooltip(part.getTitleToolTip());
		nameable.setImage(ImageDescriptor.createFromImage(part.getTitleImage(), Display.getCurrent()));
		
		dirtyHandler.setDirty(isDirty());
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.IPartPropertyProvider#getContentDescription()
     */
    public String getContentDescription() {
        IWorkbenchPart part = getPart();
        if (part instanceof IWorkbenchPart2) {
            IWorkbenchPart2 wbp2 = (IWorkbenchPart2)part;
            
            return wbp2.getContentDescription();
        }
        
        return ""; //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.IPartPropertyProvider#getEditorInput()
     */
    public IEditorInput getEditorInput() {
        IEditorPart part = (IEditorPart)getPart();
        return part.getEditorInput();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.IPartPropertyProvider#getPartName()
     */
    public String getPartName() {
        IWorkbenchPart part = getPart();
        if (part instanceof IWorkbenchPart2) {
            IWorkbenchPart2 wbp2 = (IWorkbenchPart2)part;
            
            return wbp2.getPartName();
        }
        
        return part.getTitle();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.IPartPropertyProvider#isDirty()
     */
    public boolean isDirty() {
        IEditorPart part = (IEditorPart)getPart();
        return part.isDirty();
    }
    
    public void dispose() {
        super.dispose();
        
        actionBarContributor.dispose();
    }
    
    protected void propertyChanged(int propId) {
		switch (propId) {
		case IWorkbenchPartConstants.PROP_DIRTY:
			dirtyHandler.setDirty(isDirty());
			break;
		}
		super.propertyChanged(propId);
	}
}
