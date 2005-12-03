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

package org.eclipse.jface.databinding;

import java.util.Collections;
import java.util.HashMap;

import org.eclipse.jface.databinding.internal.beans.PropertyHelper;


/**
 * A description object for a property based tree structure.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 *  If a domain model is organized as a tree, you can describe the tree with this class in order
 *  to bind it to a visual tree.
 * 
 * @see ITree for a more flexible manner to bind a tree. 
 * @since 3.2
 *
 */

public class TreeModelDescription {
	
	private HashMap childrenProperties = new HashMap();
	
	private Class[] types = new Class[0];
	
	private Object[] rootObjects;
	
	private ITree treeModel=null;
	
	private boolean updating = false;

	/**
	 * @param rootObject
	 */
	public TreeModelDescription(Object[] rootObject) {
		this.rootObjects = rootObject;
	}
	
	/**
	 * Register a property for getting, or setting the Tree's children(array or collection).
	 * This property may or may not be an index property. 
	 * A given instanceType, may have multiple children properties.
	 * 
	 * @param instanceType
	 * @param childrenProperty
	 * @throws BindingException 
	 */
	public void addChildrenProperty (Class instanceType, String childrenProperty) throws BindingException{		
		PropertyHelper prop = new PropertyHelper(childrenProperty, instanceType);
		if (prop.getGetter()==null)
			throw new BindingException("Invalid children property: "+childrenProperty);		 //$NON-NLS-1$				
		addType(instanceType);
		childrenProperties.put(instanceType, addProperty(childrenProperty, (String[])childrenProperties.get(instanceType)));
	}
	
	
	private String[] addProperty(String property, String[] list) {
		if (list==null)
			return new String[] { property } ;
		
		for (int i = 0; i < list.length; i++) {
			if (list[i].equals(property)) return list;			
		}
		String[] newList = new String[list.length+1];
		System.arraycopy(list,0, newList, 0, list.length);
		newList[list.length]=property;
		return newList;
	}
	
	private String[] addProperties(String[] properties, String[] list) {
		String[] result = list;
		for (int i = 0; i < properties.length; i++) 
			result = addProperty(properties[i], result);			
		return result;
	}
	
	private void addType(Class instanceType) {
		for (int i = 0; i < types.length; i++) {
			if (types[i]==instanceType) return;			
		}
		Class[] newTypes = new Class[types.length+1];
		System.arraycopy(types,0, newTypes, 0, types.length);
		newTypes[types.length]=instanceType;
		types = newTypes;
	}
	
	/**
	 * @param instanceType
	 * @return property denoting the children for instanceType
	 */
	public String[] getChildrenProperties(Class instanceType) {
		String[] properties = null;
		
		for (int i = 0; i < types.length; i++) {
			if (types[i].isAssignableFrom(instanceType)) {
				String[] props = (String[])childrenProperties.get(types[i]);
				properties = addProperties(props, properties);		
			}
		}
		
		return properties;
	}
	
	/**
	 * @return types associated with the tree
	 */
	public Class[] getTypes() {
		return types;
	}
	
	/**
	 * @return Tree root objects
	 */
	public Object[] getRootObjects() {
		return rootObjects==null? Collections.EMPTY_LIST.toArray(): rootObjects;
	}

	/**
	 * @param rootObjects
	 */
	public void setRootObjects(Object[] rootObjects) {		
		if (!updating)
			try {
				updating = true;
				this.rootObjects = rootObjects;
				if (treeModel != null)
					treeModel.setChildren(null, rootObjects);
			} finally {
				updating = false;
			}
	}

	/**
	 * @return ITree that is associated with this description
	 */
	public ITree getTreeModel() {
		return treeModel;
	}

	/**
	 * @param treeModel
	 */
	public void setTreeModel(ITree treeModel) {		
		if (!updating) {
			try {
				updating=true;
				if (this.treeModel!=null)
					this.treeModel.setChildren(null, null);
				this.treeModel = treeModel;
				this.treeModel.setChildren(null, rootObjects);
			}
			finally {
				updating=false;
			}
		}
	}	
    
}
