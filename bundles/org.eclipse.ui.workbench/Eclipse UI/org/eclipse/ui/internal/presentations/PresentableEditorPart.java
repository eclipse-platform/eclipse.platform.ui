/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.internal.EditorPane;
import org.eclipse.ui.internal.WorkbenchPartReference;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.presentations.IPartMenu;
import org.eclipse.ui.presentations.IPresentablePart;

/**
 * This is a lightweight wrapper around EditorPane. It adapts an EditorPane into an IPresentablePart.
 * All methods here should either redirect directly to EditorPane or do trivial conversions.
 */
public class PresentableEditorPart implements IPresentablePart {

    private final List listeners = new ArrayList();

    private EditorPane pane;

    private final IPropertyListener propertyListenerProxy = new IPropertyListener() {

        public void propertyChanged(Object source, int propId) {
            for (int i = 0; i < listeners.size(); i++)
                ((IPropertyListener) listeners.get(i)).propertyChanged(
                        PresentableEditorPart.this, propId);
        }
    };

    public PresentableEditorPart(EditorPane pane) {
        this.pane = pane;
    }

    public void addPropertyListener(final IPropertyListener listener) {
        if (listeners.isEmpty())
                getEditorReference().addPropertyListener(propertyListenerProxy);

        listeners.add(listener);
    }

    private IEditorReference getEditorReference() {
        return pane.getEditorReference();
    }

    public String getName() {
        WorkbenchPartReference ref = (WorkbenchPartReference) pane
                .getPartReference();
        return Util.safeString(ref.getRegisteredName());
    }

    public String getTitle() {
        return Util.safeString(getEditorReference().getTitle());
    }

    public Image getTitleImage() {
        return getEditorReference().getTitleImage();
    }

    public String getTitleToolTip() {
        return Util.safeString(getEditorReference().getTitleToolTip());
    }

    public boolean isDirty() {
        return getEditorReference().isDirty();
    }

    public void removePropertyListener(final IPropertyListener listener) {
        listeners.remove(listener);

        if (listeners.isEmpty())
                getEditorReference().removePropertyListener(
                        propertyListenerProxy);
    }

    public void setBounds(Rectangle bounds) {
        pane.setBounds(bounds);
    }

    public void setFocus() {
        pane.setFocus();
    }

    public void setVisible(boolean isVisible) {
        pane.setVisible(isVisible);
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.IPresentablePart#isBusy()
	 */
	public boolean isBusy() {
		// editors do not support busy currently
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.IPresentablePart#getToolBar()
	 */
	public Control getToolBar() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.IPresentablePart#getPartMenu()
	 */
	public IPartMenu getMenu() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.IPresentablePart#getControl()
	 */
	public Control getControl() {
		return pane.getControl();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.IPresentablePart#getTitleStatus()
	 */
	public String getTitleStatus() {
		return new String();
	}

}