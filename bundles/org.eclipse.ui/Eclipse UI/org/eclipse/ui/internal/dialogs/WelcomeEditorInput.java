package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.internal.*;
import org.eclipse.ui.*;
import org.eclipse.jface.resource.*;
/**
 * A simple editor input for the welcome editor
 */	
public class WelcomeEditorInput implements IEditorInput {
	private final String FACTORY_ID = "org.eclipse.ui.internal.dialogs.WelcomeEditorInputFactory"; //$NON-NLS-1$
/**
 * WelcomeEditorInput constructor comment.
 */
public WelcomeEditorInput() {
	super();
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
					return;
				}
			};
		}
		public boolean equals(Object o) {
			if((o != null) && (o instanceof WelcomeEditorInput))
				return true;
			return false;
		}
		public String getToolTipText() {
			return WorkbenchMessages.getString("WelcomeEditor.title"); //$NON-NLS-1$	
		}
}
