package org.eclipse.ui.internal;
/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

/**
 * A WorkingSetFactory is used to recreate a persisted WorkingSet 
 * object.
 *
 * @see IElementFactory
 */
public class WorkingSetFactory implements IElementFactory {

	/**
	 * Implements IElementFactory.
	 * 
	 * @see IElementFactory#createElement(IMemento)
	 */
	public IAdaptable createElement(IMemento memento) {
		String workingSetName = memento.getString(IWorkbenchConstants.TAG_NAME);

		if (workingSetName == null)
			return null;

		IMemento[] itemMementos = memento.getChildren(IWorkbenchConstants.TAG_ITEM);
		Set items = new HashSet();
		for (int i = 0; i < itemMementos.length; i++) {
			IMemento itemMemento = itemMementos[i];
			String factoryID = itemMemento.getString(IWorkbenchConstants.TAG_FACTORY_ID);

			if (factoryID == null) {
				WorkbenchPlugin.log("Unable to restore working set item - no factory ID."); //$NON-NLS-1$
				continue;
			}
			IElementFactory factory = WorkbenchPlugin.getDefault().getElementFactory(factoryID);
			if (factory == null) {
				WorkbenchPlugin.log("Unable to restore working set item - cannot instantiate factory: " + factoryID); //$NON-NLS-1$
				continue;
			}
			IAdaptable item = factory.createElement(itemMemento);
			if (item == null) {
				WorkbenchPlugin.log("Unable to restore working set item - cannot instantiate item: " + factoryID); //$NON-NLS-1$
				continue;
			}
			items.add(item);
		}
		WorkingSet workingSet = new WorkingSet(workingSetName, (IAdaptable[]) items.toArray(new IAdaptable[items.size()]));
		return workingSet;
	}
}