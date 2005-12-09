/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding.internal;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IDataBindingContext;
import org.eclipse.jface.databinding.IUpdatable;
import org.eclipse.jface.databinding.IUpdatableCollection;
import org.eclipse.jface.databinding.IUpdatableTree;
import org.eclipse.jface.databinding.IUpdatableValue;
import org.eclipse.jface.databinding.TreeModelDescription;
import org.eclipse.jface.databinding.Updatable;
import org.eclipse.jface.util.Assert;

/**
 * @since 3.2
 *
 */
public class NestedUpdatableTree extends Updatable implements IUpdatableTree {
	
	private IDataBindingContext  databindingContext;
	private TreeModelDescription originalDescription;
	
	private IUpdatableTree innerUpdatableTree;
	
	private boolean updating = false;
	
	private IChangeListener innerChangeListener = new IChangeListener() {
		public void handleChange(ChangeEvent changeEvent) {
			if (!updating) {
				ChangeEvent nestedEvent = fireChangeEvent(changeEvent.getChangeType(), changeEvent
						.getOldValue(), changeEvent.getNewValue(), changeEvent.getParent(), changeEvent.getPosition());
				if(nestedEvent.getVeto()) {
					changeEvent.setVeto(true);
				}
			}
		}
	};
	

	/**
	 * @param databindingContext
	 * @param description
	 */
	public NestedUpdatableTree(IDataBindingContext databindingContext, TreeModelDescription description) {
		this.databindingContext=databindingContext;
		this.originalDescription=description;	
		Assert.isTrue(description.getRoot() instanceof IUpdatableValue);
		
		final IUpdatable outerUpdatableValue = (IUpdatable) description.getRoot();		
		updateInnerUpdatableValue(outerUpdatableValue);
		IChangeListener outerChangeListener = new IChangeListener() {
			public void handleChange(ChangeEvent changeEvent) {
				Object[] old = getElements(null);
				updateInnerUpdatableValue(outerUpdatableValue);				
				fireChangeEvent(ChangeEvent.REPLACE, old, getElements(null), null, -1); 
			}
		};
		outerUpdatableValue.addChangeListener(outerChangeListener);
	
	}
	
	private void updateInnerUpdatableValue(IUpdatable outerUpdatableValue) {
		
		updating=true;
		try {
			if (innerUpdatableTree != null) {
				innerUpdatableTree.removeChangeListener(innerChangeListener);
				innerUpdatableTree.dispose();
			}
			
			Object currentOuterValue;
			if (outerUpdatableValue instanceof IUpdatableValue)
				currentOuterValue = ((IUpdatableValue)outerUpdatableValue).getValue();
			else {
				IUpdatableCollection collection = (IUpdatableCollection)outerUpdatableValue;
				Object[] elements = new Object[collection.getSize()];
				for (int i = 0; i < elements.length; i++) 
					elements[i]=collection.getElement(i);
				currentOuterValue=elements;					
			}
			// Construct a new description
			TreeModelDescription newDescriptor = new TreeModelDescription(currentOuterValue);
			Class[] types = originalDescription.getTypes();
			for (int i = 0; i < types.length; i++) {
				String[] properties = originalDescription.getChildrenProperties(types[i]);
				for (int j = 0; j < properties.length; j++) 
					newDescriptor.addChildrenProperty(types[i], properties[j]);
			}							    
			innerUpdatableTree = (IUpdatableTree) databindingContext.createUpdatable(newDescriptor);
			innerUpdatableTree.addChangeListener(innerChangeListener);
		}
		finally {
			updating=false;
		}
	}

	public int addElement(Object parentElement, int index, Object value) {
		return innerUpdatableTree.addElement(parentElement, index, value);
	}

	public void removeElement(Object parentElement, int index) {
		innerUpdatableTree.removeElement(parentElement, index);
		
	}

	public void setElement(Object parentElement, int index, Object value) {
		innerUpdatableTree.setElement(parentElement, index, value);		
	}

	public void setElements(Object parentElement, Object[] values) {
		innerUpdatableTree.setElements(parentElement, values);		
	}

	public Object getElement(Object parentElement, int index) {
		return innerUpdatableTree.getElement(parentElement, index);
	}

	public Object[] getElements(Object parentElement) {
		return innerUpdatableTree.getElements(parentElement);
	}

	public Class[] getTypes() {
		return innerUpdatableTree.getTypes();
	}

}
