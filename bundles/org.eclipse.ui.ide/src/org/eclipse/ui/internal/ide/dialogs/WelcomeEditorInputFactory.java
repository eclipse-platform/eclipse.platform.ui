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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.ide.AboutInfo;

/**
 * A simple factory for the welcome editor
 */
public class WelcomeEditorInputFactory implements IElementFactory {
    /**
     * WelcomeEditorInputFactory constructor comment.
     */
    public WelcomeEditorInputFactory() {
        super();
    }

    /**
     * Re-creates and returns an object from the state captured within the given 
     * memento. 
     * <p>
     * Under normal circumstances, the resulting object can be expected to be
     * persistable; that is,
     * <pre>
     * result.getAdapter(org.eclipse.ui.IPersistableElement.class)
     * </pre>
     * should not return <code>null</code>.
     * </p>
     *
     * @param memento a memento containing the state for the object
     * @return an object, or <code>null</code> if the element could not be created
     */
    public IAdaptable createElement(IMemento memento) {
        // Get the feature id.
        String versionedFeatureId = memento
                .getString(WelcomeEditorInput.FEATURE_ID);
        if (versionedFeatureId == null) {
            return null;
        }
        int colonPos = versionedFeatureId.indexOf(':');
        if (colonPos == -1) {
            // assume the memento is stale or mangled
            return null;
        }
        String featureId = versionedFeatureId.substring(0, colonPos);
        String versionId = versionedFeatureId.substring(colonPos + 1);
        // @issue using feature id for plug-in id
        AboutInfo info = AboutInfo.readFeatureInfo(featureId, versionId);
        if (info == null) {
            return null;
        }
        return new WelcomeEditorInput(info);
    }
}
