package org.eclipse.update.internal.ui.manager;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.*;

public class UpdateManagerInput implements IEditorInput {
	private static final String FACTORY_ID = "org.eclipse.update.ui.internal.manager.UpdateManagerInputFactory";

	/**
	 * @see IEditorInput#exists()
	 */
	public boolean exists() {
		return false;
	}

	/**
	 * @see IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	/**
	 * @see IEditorInput#getName()
	 */
	public String getName() {
		return "Update Manager";
	}

	/**
	 * @see IEditorInput#getPersistable()
	 */
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

	/**
	 * @see IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		return "Update Manager";
	}

	/**
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class arg0) {
		return null;
	}
}

