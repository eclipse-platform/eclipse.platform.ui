/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.AboutInfo;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
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
	String featureId = memento.getString(WelcomeEditorInput.FEATURE_ID);
	if (featureId == null) 
		return null;

	AboutInfo info = null;
	AboutInfo [] infos = 
		((Workbench)(WorkbenchPlugin.getDefault().getWorkbench())).getConfigurationInfo().getFeaturesInfo();
	for (int i = 0; i < infos.length; i++) {
		if (featureId.equals(infos[i].getFeatureId())) {
			info = infos[i];
			break;
		}
	}	
	if (info == null)
		return null;

	return new WelcomeEditorInput(info);
}
}
