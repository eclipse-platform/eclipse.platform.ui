/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.jobs;

import java.util.Iterator;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;

public class JobsView extends ViewPart {
			
	private AbstractTreeViewer tree;
	private Action clearDone;
	private Action cancelJob;
	
	public void createPartControl(Composite parent) {
		tree = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setContentProvider(new JobsContentProvider(this));
		tree.setLabelProvider(new JobsLabelProvider());
		tree.setInput(Platform.getJobManager());
		initializeActions();
		
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(tree.getControl());
		tree.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, tree);		
	}

	private void initializeActions() {
		clearDone = new Action("Clear") {
			public boolean isEnabled() {
				IStructuredSelection s = (IStructuredSelection)tree.getSelection();
				Object element = s.getFirstElement();
				if(element instanceof JobStateCategory) {
					return ((JobStateCategory)element).name.equals("Done");
				}
				return false;
			}
			public void run() {
				IStructuredSelection s = (IStructuredSelection)tree.getSelection();
				Object element = s.getFirstElement();
				if(element instanceof JobStateCategory) {
					((JobStateCategory)element).removeAll();
				}
			}
		};
		cancelJob = new Action("Cancel") {
			public boolean isEnabled() {
				IStructuredSelection s = (IStructuredSelection)tree.getSelection();
				Iterator it = s.iterator();
				while(it.hasNext()) {
					Object element = it.next();
					if(!(element instanceof Job)) {
						return false;
					}
				}
				return true;
			}
			public void run() {
				IStructuredSelection s = (IStructuredSelection)tree.getSelection();
				Iterator it = s.iterator();
				while(it.hasNext()) {
					Job job = (Job)it.next();
					job.cancel();
				}
			}
		};
	}

	protected void fillContextMenu(IMenuManager manager) {
		clearDone.setEnabled(clearDone.isEnabled());
		manager.add(clearDone);
		cancelJob.setEnabled(cancelJob.isEnabled());
		manager.add(cancelJob);
		manager.add(new Separator("Additions"));
	}

	public void setFocus() {		
	}
	
	public AbstractTreeViewer getViewer() {
		return tree;
	}
}
