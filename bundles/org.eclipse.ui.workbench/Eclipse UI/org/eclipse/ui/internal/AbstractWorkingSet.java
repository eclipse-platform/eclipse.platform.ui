/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;

/**
 * @since 3.2
 *
 */
public abstract class AbstractWorkingSet implements IAdaptable, IWorkingSet {

	protected static final String FACTORY_ID = "org.eclipse.ui.internal.WorkingSetFactory"; //$NON-NLS-1$
	static final String TAG_AGGREGATE = "aggregate"; //$NON-NLS-1$
	private String name;
	protected ArrayList elements;
	private IWorkingSetManager manager;
	//private boolean visible = true;

    protected IMemento workingSetMemento;
	private String label;

	/**ghrfgdfgetrtg
	 * 
	 * 
	 * erg
	 * 
	 * @param name
	 */
	public AbstractWorkingSet(String name, String label) {
		
		Assert.isNotNull(name, "name must not be null"); //$NON-NLS-1$
		this.name = name;
		this.label = label;
	}
	
	/**
	 * Returns the receiver if the requested type is either IWorkingSet 
	 * or IPersistableElement.
	 * 
	 * @param adapter the requested type
	 * @return the receiver if the requested type is either IWorkingSet 
	 * 	or IPersistableElement.
	 */
	public Object getAdapter(Class adapter) {
	    if (adapter == IWorkingSet.class
	            || adapter == IPersistableElement.class) {
	        return this;
	    }
	    return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	public String getName() {
	    return name;
	}

	/**
	 * Set whether this working set should be visible in the UI.
	 * 
	 * @param visible whether this working set should be visible in the UI
	 * @since 3.2
	 */
	/*public void setVisible(boolean visible) {
			this.visible = visible;
	}*/

	public void setName(String newName) {
	    Assert.isNotNull(newName, "Working set name must not be null"); //$NON-NLS-1$
	    
	    name = newName;
	    fireWorkingSetChanged(IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE, null);
	}

	public void connect(IWorkingSetManager manager) {
		Assert.isTrue(this.manager == null, "A working set can only be connected to one manager"); //$NON-NLS-1$
		this.manager= manager;
	}

	public void disconnect() {
		this.manager= null;
	}

	protected void fireWorkingSetChanged(String property, Object oldValue) {
		AbstractWorkingSetManager receiver= manager != null
			? (AbstractWorkingSetManager)manager
			: (AbstractWorkingSetManager)WorkbenchPlugin.getDefault().getWorkingSetManager();
		receiver.workingSetChanged(this, property, oldValue);
	}

	/*public boolean isVisible() {
		return visible;
	}*/

	/**
	 * Create a copy of the elements to store in the receiver.
	 * 
	 * @param elements the elements to store a copy of in the 
	 * 	receiver.
	 */
	protected void internalSetElements(IAdaptable[] newElements) {
	    Assert.isNotNull(newElements,
	            "Working set elements array must not be null"); //$NON-NLS-1$
	
	    elements = new ArrayList(newElements.length);
	    for (int i = 0; i < newElements.length; i++) {
	        elements.add(newElements[i]);
	    }
	}

	public IAdaptable[] getElements() {
	    ArrayList list = getElementsArray();
	    return (IAdaptable[]) list.toArray(new IAdaptable[list.size()]);
	}

	/**
	 * Returns the elements array list. Lazily restores the elements from
	 * persistence memento. 
	 * 
	 * @return the elements array list
	 */
	protected ArrayList getElementsArray() {
	    if (elements == null) {
	        restoreWorkingSet();
	        workingSetMemento = null;
	    }
	    return elements;
	}
	
	abstract void restoreWorkingSet();
	
	protected IWorkingSetManager getManager() {
		return manager;
	}

	public String getFactoryId() {
	    return FACTORY_ID;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String uniqueId) {
		this.label = uniqueId;
	}
}
