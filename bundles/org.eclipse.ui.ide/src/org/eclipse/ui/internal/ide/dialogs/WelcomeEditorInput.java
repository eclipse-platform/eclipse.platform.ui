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
package org.eclipse.ui.internal.ide.dialogs;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.internal.ide.AboutInfo;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * A simple editor input for the welcome editor
 */
public class WelcomeEditorInput implements IEditorInput {
    private AboutInfo aboutInfo;

    private final static String FACTORY_ID = "org.eclipse.ui.internal.dialogs.WelcomeEditorInputFactory"; //$NON-NLS-1$

    public final static String FEATURE_ID = "featureId"; //$NON-NLS-1$

    /**
     * WelcomeEditorInput constructor comment.
     */
    public WelcomeEditorInput(AboutInfo info) {
        super();
        if (info == null) {
            throw new IllegalArgumentException();
        }
        aboutInfo = info;
    }

    public boolean exists() {
        return false;
    }

    public Object getAdapter(Class adapter) {
        return null;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return IDEWorkbenchMessages.WelcomeEditor_title;
    }

    public IPersistableElement getPersistable() {
        return new IPersistableElement() {
            public String getFactoryId() {
                return FACTORY_ID;
            }

            public void saveState(IMemento memento) {
                memento.putString(FEATURE_ID, aboutInfo.getFeatureId() + ':'
                        + aboutInfo.getVersionId());
            }
        };
    }

    public AboutInfo getAboutInfo() {
        return aboutInfo;
    }

    public boolean equals(Object o) {
        if ((o != null) && (o instanceof WelcomeEditorInput)) {
            if (((WelcomeEditorInput) o).aboutInfo.getFeatureId().equals(
                    aboutInfo.getFeatureId())) {
				return true;
			}
        }
        return false;
    }

    public String getToolTipText() {
        return NLS.bind(IDEWorkbenchMessages.WelcomeEditor_toolTip, aboutInfo.getFeatureLabel());
    }
}
