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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;

public class WorkingSetScopeDescription implements IScopeDescription {
	private static final String KEY_WORKING_SET_NAME= "workingset-names"; //$NON-NLS-1$
	private static final String KEY_LABEL= "workingset-label"; //$NON-NLS-1$
	private static final IResource[] EMPTY_ARRAY= new IResource[0];
	private String[] fWorkingSetNames;
	private String fLabel;

	public WorkingSetScopeDescription() {
	}

	public WorkingSetScopeDescription(IWorkingSet[] set) {
		fWorkingSetNames= new String[set.length];
		for (int i= 0; i < set.length; i++) {
			fWorkingSetNames[i]= set[i].getName();
		}
		fLabel= computeLabel();
	}

	private String computeLabel() {
		StringBuffer label= new StringBuffer();
        for (int i = 0; i < fWorkingSetNames.length; i++) {
            if (i>0){
            	label.append('+');
            }
            label.append(fWorkingSetNames[i]);
        }
        return label.toString();
	}

	public String getLabelForCombo() {
		return fLabel;
	}

	public String getNameForDescription() {
		return fLabel;
	}

	protected void setLabel(String label) {
		fLabel= label;
	}

	public IResource[] getRoots(IWorkbenchPage page) {
		return getRootsFromWorkingSets(getWorkingSets());
	}

	protected IResource[] getRootsFromWorkingSets(IWorkingSet[] ws) {
		if (ws == null) {
			return EMPTY_ARRAY;
		}

		HashSet resources= new HashSet();
		for (int i= 0; i < ws.length; i++) {
			getResources(ws[i].getElements(), resources);			
		}

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
		fWorkingSetNames= section.getArray(KEY_WORKING_SET_NAME);
		if (fWorkingSetNames == null) {
			throw new IllegalArgumentException();
		}
		fLabel= section.get(KEY_LABEL);
		if (fLabel == null) {
			fLabel= computeLabel();
		}
	}

	public void store(IDialogSettings section) {
		section.put(KEY_WORKING_SET_NAME, fWorkingSetNames);
		section.put(KEY_LABEL, fLabel);
	}

	public void store(Properties props, String prefix) {
		props.put(prefix + KEY_WORKING_SET_NAME, fWorkingSetNames);
		props.put(prefix + KEY_LABEL, fLabel);
	}

	public void restore(Properties props, String prefix) {
		fWorkingSetNames= (String[]) props.get(prefix + KEY_WORKING_SET_NAME);
		fLabel= (String) props.get(prefix + KEY_LABEL);
		if (fLabel == null) {
			fLabel= computeLabel();
		}
	}

	public static IScopeDescription createWithDialog(IWorkbenchPage page, IScopeDescription scope) {
		if (page == null) {
			return null;
		}
		IWorkingSetManager manager= PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSetSelectionDialog dialog= manager.createWorkingSetSelectionDialog(page.getWorkbenchWindow().getShell(), true);

		IScopeDescription result= null;
		if (scope instanceof WindowWorkingSetScopeDescription) {
			dialog.setSelection(new IWorkingSet[] {page.getAggregateWorkingSet()});
		} else
			if (scope instanceof WorkingSetScopeDescription) {
				WorkingSetScopeDescription wsetscope= (WorkingSetScopeDescription) scope;
				dialog.setSelection(wsetscope.getWorkingSets());
			}

		if (dialog.open() == Window.OK) {
			IWorkingSet[] wsarray= dialog.getSelection();
			if (wsarray != null && wsarray.length > 0) {
				if (wsarray.length == 1 && wsarray[0] == page.getAggregateWorkingSet()) {
					result= new WindowWorkingSetScopeDescription();
				} 
				else if (wsarray.length > 0) {
					result= new WorkingSetScopeDescription(wsarray);
					if (wsarray.length == 0) {
						manager.addRecentWorkingSet(wsarray[0]);
					}
				}
			} else {
				result= new WorkspaceScopeDescription();
			}
		}
		return result;
	}

	private IWorkingSet[] getWorkingSets() {
		IWorkingSetManager manager= PlatformUI.getWorkbench().getWorkingSetManager();
		ArrayList workingSets= new ArrayList();
		for (int i= 0; i < fWorkingSetNames.length; i++) {
			IWorkingSet ws= manager.getWorkingSet(fWorkingSetNames[i]);
			if (ws != null) {
				workingSets.add(ws);
			}
		}
		return (IWorkingSet[]) workingSets.toArray(new IWorkingSet[workingSets.size()]);
	}

	public IFile[] getFiles(IWorkbenchPage page) {
		return null;
	}
}
