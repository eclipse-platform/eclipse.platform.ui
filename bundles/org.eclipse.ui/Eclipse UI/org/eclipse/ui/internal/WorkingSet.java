/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.ui.internal;

import java.util.*;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.internal.model.WorkbenchWorkingSet;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class WorkingSet implements IWorkingSet {
	String name;
	Set items; // of IAdaptable

	public WorkingSet(String name, IAdaptable[] elements) {
		Assert.isNotNull(name, "name must not be null"); //$NON-NLS-1$
		this.name = name;
		setItems(elements, true);
	}
	public boolean equals(Object o) {
		return (o instanceof IWorkingSet) && ((IWorkingSet) o).getName().equals(getName());
	}
	/*
	 * @see IWorkingSet#getName()
	 */
	public String getName() {
		return name;
	}
	/*
	 * @see IWorkingSet#getItems()
	 */
	public IAdaptable[] getItems() {
		return (IAdaptable[]) items.toArray(new IAdaptable[items.size()]);
	}
	public int hashCode() {
		return name.hashCode();
	}

	/*
	 * Public for use by org.eclipse.ui.internal.dialogs.WorkingSetDialog.
	 */
	public void setItems(IAdaptable[] elements) {
		setItems(elements, false);
	}

	private void setItems(IAdaptable[] elements, boolean internal) {
		Assert.isNotNull(elements, "IPath array must not be null"); //$NON-NLS-1$
		items = new HashSet(elements.length);
		for (int i = 0; i < elements.length; i++) {
			Assert.isTrue(!items.contains(elements[i]), "elements must only contain each element once"); //$NON-NLS-1$
			items.add(elements[i]);
		}
		if (!internal)
			WorkingSetRegistry.getInstance().saveWorkingSets();
	}
	/*
	 * Public for use by org.eclipse.ui.internal.dialogs.WorkingSetDialog.
	 */
	public void setName(String name) {
		Assert.isNotNull(name, "name must not be null"); //$NON-NLS-1$
		this.name = name;
		WorkingSetRegistry.getInstance().saveWorkingSets();
	}
	//--- Persistency -----------------------------------------------


}