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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.AboutInfo;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.misc.Assert;
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
	Assert.isNotNull(info);
	aboutInfo = info;	
}
		public boolean exists() {
			return false;
		}
		public Object getAdapter(Class adapter) {
			return null;
		}
		public ImageDescriptor  getImageDescriptor() {
			return null;
		}
		public String getName() {
			return WorkbenchMessages.getString("WelcomeEditor.title"); //$NON-NLS-1$	
		}
		public IPersistableElement getPersistable() {
			return new IPersistableElement() {
				public String getFactoryId() {
					return FACTORY_ID;
				}
				public void saveState(IMemento memento) {
					memento.putString(FEATURE_ID, aboutInfo.getFeatureId());
				}
			};
		}
		public AboutInfo getAboutInfo() {
			return aboutInfo;
		}
		public boolean equals(Object o) {
			if((o != null) && (o instanceof WelcomeEditorInput)) {
				if (((WelcomeEditorInput)o).aboutInfo.getFeatureId().equals(
					aboutInfo.getFeatureId()))
					return true;
			}
			return false;
		}
		public String getToolTipText() {
			return WorkbenchMessages.format("WelcomeEditor.toolTip", new Object[]{aboutInfo.getFeatureLabel()}); //$NON-NLS-1$	
		}
}
