package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.URL;

import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.*;
import org.eclipse.jface.resource.*;
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
