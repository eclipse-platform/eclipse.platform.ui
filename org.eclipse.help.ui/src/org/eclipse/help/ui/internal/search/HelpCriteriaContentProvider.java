/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.search;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.ui.internal.Messages;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class HelpCriteriaContentProvider implements ITreeContentProvider {
	
	public static final String UNCATEGORIZED = "Uncategorized"; //$NON-NLS-1$

	public static class CriterionName{
		public CriterionName(String id, Object parent) {
			this.id = id;
			this.parent = parent;
		}
		
		private String id;
		private Object parent;
		
		public String getId() {
			return id;
		}
		
		public String getName() {
			return HelpPlugin.getCriteriaManager().getCriterionDisplayName(id, Platform.getNL());
		}

		public Object getParent() {
			return parent;
		}
		
		public boolean equals(Object arg0) {
			if (arg0 instanceof CriterionName) {
				CriterionName other = (CriterionName) arg0;
				return other.id.equals(this.id);
			}
			return false;
		}
		
		public int hashCode() {
			return id.hashCode();
		}
	}
	
	public static class CriterionValue{
		public CriterionValue(String id, Object parent) {
			this.id = id;
			this.parent = parent;
		}
		
		private String id;
		private Object parent;
		
		public String getId() {
			return id;
		}
		
		public String getName() {
			if (id.equals(UNCATEGORIZED)) {
				return Messages.UncategorizedCriteria;
			}
			CriterionName parentCriterion = (CriterionName) parent;
			return HelpPlugin.getCriteriaManager().getCriterionValueDisplayName(parentCriterion.id, id, Platform.getNL());
		}

		public Object getParent() {
			return parent;
		}
		
		public boolean equals(Object arg0) {
			if (arg0 instanceof CriterionValue) {
				CriterionValue other = (CriterionValue) arg0;
				return other.id.equals(this.id) && parent.equals(other.parent);
			}
			return false;
		}
		
		public int hashCode() {
			return id.hashCode() + parent.hashCode();
		}
	}

	/**
	 * Constructor for HelpWorkingSetTreeContentProvider.
	 */
	public HelpCriteriaContentProvider() {
		super();
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof String[]) {
			String[] names = (String[]) parentElement;
			CriterionName criterionNames[] = new  CriterionName[names.length];
			for (int i = 0; i < names.length; i++) {
				criterionNames[i] = new CriterionName(names[i], parentElement);
			}
			return criterionNames;
		} else if (parentElement instanceof CriterionName) {
			CriterionName parentCriterion = (CriterionName) parentElement;
			
			String[] values = BaseHelpSystem.getWorkingSetManager().getCriterionValueIds(parentCriterion.getId());
		    CriterionValue[] criterionValues = new CriterionValue[values.length];
			for (int i = 0; i < values.length; i++) {
				criterionValues[i] = new CriterionValue(values[i], parentElement);
			}
			return criterionValues;
		} else {
			return new Object[0];
		}
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if (element instanceof CriterionName) {
			return ((CriterionName) element).getParent();
		} else if (element instanceof CriterionValue) {
			return ((CriterionValue) element).getParent();
		} else 
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	/**
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
