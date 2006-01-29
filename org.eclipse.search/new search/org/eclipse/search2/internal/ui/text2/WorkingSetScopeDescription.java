/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;

public class WorkingSetScopeDescription implements IScopeDescription {
	private static final String KEY_WORKING_SET_NAME= "workingset-name"; //$NON-NLS-1$
	private static final String KEY_LABEL= "workingset-label"; //$NON-NLS-1$
	private static final IResource[] EMPTY_ARRAY= new IResource[0];
	private String fWorkingSetName;
	private String fLabel;

	public WorkingSetScopeDescription() {
	}

	public WorkingSetScopeDescription(IWorkingSet set) {
		fWorkingSetName= set.getName();
		fLabel= set.getLabel();
		// try to change the label
		if (set.isAggregateWorkingSet()) {
			computeLabel();
		}
	}

	private void computeLabel() {
		if (fWorkingSetName.startsWith("Aggregate:") && //$NON-NLS-1$
				fWorkingSetName.charAt(fWorkingSetName.length() - 1) == ':') {
			fLabel= fWorkingSetName.substring(10, fWorkingSetName.length() - 1).replace(':', '+');
		}
	}

	public String getLabel() {
		return fLabel;
	}

	protected void setLabel(String label) {
		fLabel= label;
	}

	public IResource[] getRoots(IWorkbenchPage page) {
		IWorkingSetManager workingSetManager= PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet ws= workingSetManager.getWorkingSet(fWorkingSetName);

		return getRootsFromWorkingSet(ws);
	}

	protected IResource[] getRootsFromWorkingSet(IWorkingSet ws) {
		if (ws == null) {
			return EMPTY_ARRAY;
		}

		HashSet resources= new HashSet();
		getResources(ws.getElements(), resources);

		// remove duplicates
		for (Iterator iter= resources.iterator(); iter.hasNext();) {
			IResource r= (IResource) iter.next();
			IResource parent= r.getParent();
			while (parent != null) {
				if (resources.contains(parent)) {
					iter.remove();
					parent= null;
				} else {
					parent= parent.getParent();
				}
			}
		}
		return (IResource[]) resources.toArray(new IResource[resources.size()]);
	}

	public String getWorkingSetName() {
		return fWorkingSetName;
	}

	private void getResources(IAdaptable[] elements, Collection target) {
		for (int i= 0; i < elements.length; i++) {
			IAdaptable adaptable= elements[i];
			IResource r= (IResource) adaptable.getAdapter(IResource.class);
			if (r != null) {
				target.add(r);
			}
		}
	}

	public void restore(IDialogSettings section) {
		fWorkingSetName= section.get(KEY_WORKING_SET_NAME);
		fLabel= section.get(KEY_LABEL);
		if (fLabel == null) {
			fLabel= fWorkingSetName;
		}
	}

	public void store(IDialogSettings section) {
		section.put(KEY_WORKING_SET_NAME, fWorkingSetName);
		section.put(KEY_LABEL, fLabel);
	}

	public void store(Properties props, String prefix) {
		props.put(prefix + KEY_WORKING_SET_NAME, fWorkingSetName);
		props.put(prefix + KEY_LABEL, fLabel);
	}

	public void restore(Properties props, String prefix) {
		fWorkingSetName= (String) props.get(prefix + KEY_WORKING_SET_NAME);
		fLabel= (String) props.get(prefix + KEY_LABEL);
		if (fLabel == null) {
			fLabel= fWorkingSetName;
		}
	}

	public static IScopeDescription createWithDialog(IWorkbenchPage page, IScopeDescription scope) {
		if (page == null) {
			return null;
		}
		IWorkingSetManager manager= PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSetSelectionDialog dialog= manager.createWorkingSetSelectionDialog(page.getWorkbenchWindow().getShell(), false);

		IScopeDescription result= null;
		if (scope instanceof WindowWorkingSetScopeDescription) {
			dialog.setSelection(new IWorkingSet[] {page.getAggregateWorkingSet()});
		} else
			if (scope instanceof WorkingSetScopeDescription) {
				WorkingSetScopeDescription wsetscope= (WorkingSetScopeDescription) scope;
				String name= wsetscope.getWorkingSetName();
				IWorkingSet ws= manager.getWorkingSet(name);
				if (ws != null) {
					dialog.setSelection(new IWorkingSet[] {ws});
				}
			}

		if (dialog.open() == Window.OK) {
			IWorkingSet[] wsarray= dialog.getSelection();
			if (wsarray != null && wsarray.length > 0) {
				IWorkingSet ws= wsarray[0];
				if (ws == page.getAggregateWorkingSet()) {
					result= new WindowWorkingSetScopeDescription();
				} else {
					result= new WorkingSetScopeDescription(ws);
					manager.addRecentWorkingSet(ws);
				}
			} else {
				result= new WorkspaceScopeDescription();
			}
		}
		return result;
	}
}
