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

package org.eclipse.jface.databinding.internal.beans;

import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.databinding.BindingException;
import org.eclipse.jface.databinding.IChangeEvent;
import org.eclipse.jface.databinding.ITree;
import org.eclipse.jface.databinding.TreeModelDescription;

/**
 *
 */
public class JavaBeanTree implements ITree {
	
	private class TreeNode {
		private Object parent;
		private List children;
		
		/**
		 * @param parent
		 */
		public TreeNode(Object parent) {
			this.parent=parent;
		}

		/**
		 * @return children
		 */
		public List getChildren() {
			return children==null? Collections.EMPTY_LIST: children;
		}

		/**
		 * @param children
		 */
		public void setChildren(List children) {
			this.children = children;
		}

		/**
		 * @return parent
		 */
		public Object getParent() {
			return parent;
		}
	}
	
	private final static int READ = 0;
	
	private final static int WRITE = 0;

	private TreeModelDescription modelDescription;
	
	private Object[]  rootElements;
	
	private HashMap descriptors = new HashMap();
	
	private ITree.ChangeSupport changeSupport = null;
	
	// TODO use IdentityWrapper
	private HashMap nodes = new HashMap();
	
	private boolean updating = false;
	
		
	private PropertyChangeListener elementListener = new PropertyChangeListener() {
		public void propertyChange(java.beans.PropertyChangeEvent event) {
			if (!updating && changeSupport!=null) {	
				String[] properties = modelDescription.getChildrenProperties(event.getSource().getClass());
				if (properties!=null && event.getPropertyName()!=null && 
						Arrays.asList(properties).contains(event.getPropertyName())) {
					// Assume children have changed
					changeSupport.fireTreeChange(IChangeEvent.REPLACE, null, getChildren(event.getSource()), event.getSource(), -1);
				}
				else {
					// Assume a property has changed that may change the way this element
					// is rendered in a viewer
					TreeNode node = (TreeNode)nodes.get(event.getSource());
					if (node!=null) {
						int index = -1;
						Object[] children = getChildren(node.getParent());
						if (children!=null) {
							index = Arrays.asList(children).indexOf(event.getSource());
						}
						if (index<0) index = IChangeEvent.POSITION_UNKNOWN;
					    changeSupport.fireTreeChange(IChangeEvent.CHANGE, null, event.getSource(), node.getParent(), index);
					}
				}
			}
		}
	};
	
	HashMap listenerSupport = new HashMap();
	
	/**
	 * @param modelDescripton
	 */
	public JavaBeanTree(TreeModelDescription modelDescripton) {
		this.modelDescription = modelDescripton;
		if (modelDescripton.getRoot()==null)
			rootElements = Collections.EMPTY_LIST.toArray();
		else
			rootElements = modelDescripton.getRoot().getClass().isArray()?
				          (Object[]) modelDescripton.getRoot():
				           new Object[] { modelDescripton.getRoot() };
		TreeNode root = new TreeNode(null);
		nodes.put(this, root);
		hookup(null, rootElements);		
	}
	
	
	// pick up the leaf most registered class that is assignable
	// from o.class.
	private Class getRelevantClass(Object o) {
		if (o==null)
			return null;
		Class leafClass = null;
		Class current = o.getClass();
		Class[] list = modelDescription.getTypes();		
		for (int i = 0; i < list.length; i++)
			if (list[i].isAssignableFrom(current)) {
				// found registered class
				if (leafClass==null)
					leafClass=list[i];
				else
					if (leafClass.isAssignableFrom(list[i]))
							leafClass=list[i]; 
			}
		return leafClass;
	}
		
	private Method[] getInvocationMethods(Class clazz, int methodType) {
		if (clazz==null)
			return null;
		String[] childrenProperties = modelDescription
				.getChildrenProperties(clazz);
		if (childrenProperties == null || childrenProperties.length == 0)
			return null;
		
		PropertyHelper[] properties = (PropertyHelper[]) descriptors.get(clazz);
		if (properties == null) {
			List newpProperties = new ArrayList();

			// Walk by properties to preserve the tree order
			for (int i = 0; i < childrenProperties.length; i++) {
				PropertyHelper property = new PropertyHelper(
						childrenProperties[i], clazz);
				if (property.getGetter() != null ||  property.getSetter() != null) {
					newpProperties.add(property);
				}
			}

			properties = (PropertyHelper[]) newpProperties
					.toArray(new PropertyHelper[newpProperties.size()]);
			descriptors.put(clazz, properties);

		}
		List methods = new ArrayList();
		for (int i = 0; i < properties.length; i++) {
			if (methodType==READ)
				methods.add(properties[i].getGetter());
			else
				methods.add(properties[i].getSetter());			
		}		
		return (Method[])methods.toArray(new Method[methods.size()]);
	}
		
	private Method[] getReadMethods(Object element) {		
		Class clazz = getRelevantClass(element);
		if (clazz==null) return null;
		
		Method[] methods = getInvocationMethods(clazz, READ);		
		return methods;
	}
	
	
	private Method[] getWriteMethods (Object element) {
		Class clazz = getRelevantClass(element);
		if (clazz==null) return null;
		Method[] methods = getInvocationMethods(clazz, WRITE);		
		return methods;
	}
	
	private void hookup (Object parent, Object[] children) {
		Object key = parent==null?JavaBeanTree.this:parent;
		
		ListenerSupport support = (ListenerSupport) listenerSupport.get(key);
		if (support==null) {
			support = new ListenerSupport(elementListener);
			listenerSupport.put(key, support);
		}			
		support.setHookTargets(children);
		// Track a child's index
		trackChildren(parent, children);
	}
		
	public Object[] getChildren(Object parentElement) {
		if (parentElement==null) 
			return rootElements;
		
		
		Method[] getters = getReadMethods(parentElement);
		if (getters==null)
			return Collections.EMPTY_LIST.toArray();
		
		List children = new ArrayList();
		
		try {
			for (int i = 0; i < getters.length; i++) {
				Object list = getters[i].invoke(parentElement, new Object[0]);
				if (list!=null) {
					if (getters[i].getReturnType().isArray()) 					
						children.addAll(Arrays.asList((Object[])list));
					else
					    children.addAll((Collection) getters[i].invoke(parentElement, new Object[0]));
				}
			}
		} catch (IllegalArgumentException e) {
			throw new BindingException(e.getLocalizedMessage());	
		} catch (IllegalAccessException e) {
			throw new BindingException(e.getLocalizedMessage());			
		} catch (InvocationTargetException e) {
			throw new BindingException(e.getLocalizedMessage());			
		} 
		Object[] result = children.toArray();
		hookup(parentElement, result);
		trackChildren(parentElement, result);
		return result;
	}
	
	private void trackChildren(Object parentElement, Object[] children) {
		Object key = parentElement==null?JavaBeanTree.this : parentElement;
		TreeNode node = (TreeNode) nodes.get(key);
		Set removed = new HashSet(node.getChildren());
		List newList = children==null? new ArrayList(): Arrays.asList(children);
		if (children!=null)
			for (int i=0; i<children.length; i++) {
				if (!removed.remove(children[i])) {
					// new child
					TreeNode child = new TreeNode(parentElement);
					nodes.put(children[i], child);
				}				
			}
		for (Iterator itr=removed.iterator(); itr.hasNext();)
			nodes.remove(itr.next());
		node.setChildren(newList);
	}

	public void setChildren(Object parentElement, Object[] children) {
		if (parentElement==null) 
			throw new BindingException ("Changing root element/s is not supported"); //$NON-NLS-1$
		
		Method[] setters = getWriteMethods(parentElement);
		// TODO we can try to figure out (by type) which methods to invoke on which object
		if (setters==null || setters.length!=1)
			throw new BindingException("Can not determine children set method for: "+parentElement.getClass().getName()); //$NON-NLS-1$
		
		Method setter = setters[0];
		Class[] parameters = setter.getParameterTypes();
		Object arg;
		if (parameters[0].isArray())
			arg = children;
		else
			arg = Arrays.asList(children);		
		try {
			setter.invoke(parentElement, new Object[] { arg } );
		} catch (IllegalArgumentException e) {
			throw new BindingException(e.getLocalizedMessage());		
		} catch (IllegalAccessException e) {
			throw new BindingException(e.getLocalizedMessage());	
		} catch (InvocationTargetException e) {
			throw new BindingException(e.getLocalizedMessage());	
		}
		hookup(parentElement, children);		
		if (changeSupport!=null)
		   changeSupport.fireTreeChange(IChangeEvent.REPLACE, null, children, parentElement, -1);
	}

	public boolean hasChildren(Object element) {
		Object[] children = getChildren(element);
		return (children!=null && children.length>0);
	}

	public Class[] getTypes() {		
		return modelDescription.getTypes();
	}

	public void addTreeChangeListener(ITree.ChangeListener listener) {
		if (listener==null)
			return;
		if (changeSupport==null)
			changeSupport = new ITree.ChangeSupport(this);
		changeSupport.addTreeChangeListener(listener);		
	}

	public void removeTreeChangeListener(ITree.ChangeListener listener) {
		if (listener==null || changeSupport==null)
			return;
		
		changeSupport.removeTreeChangeListener(listener);		
	}

	public void dispose() {
		if (listenerSupport!=null) {
		  for (Iterator itr=listenerSupport.values().iterator(); itr.hasNext();) 
		   ((ListenerSupport)itr.next()).dispose();
		  listenerSupport.clear();
		  descriptors=null;
		  changeSupport=null;
		  modelDescription=null;
		}
	}



}
