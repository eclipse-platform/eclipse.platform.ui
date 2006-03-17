/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.cheatsheets.composite.explorer;


import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.internal.cheatsheets.composite.parser.ICompositeCheatsheetTags;
import org.eclipse.ui.internal.provisional.cheatsheets.ICompositeCheatSheet;
import org.eclipse.ui.internal.provisional.cheatsheets.ICompositeCheatSheetTask;
import org.eclipse.ui.internal.provisional.cheatsheets.TaskExplorer;

public class TreeTaskExplorer extends TaskExplorer {
	private TreeViewer viewer;

	
	public String getId() {
		return ICompositeCheatsheetTags.TREE;
	}	

	public void createControl(Composite parent, FormToolkit toolkit) {
		Tree tree = new Tree(parent, toolkit.getOrientation());
		toolkit.adapt(tree, false, false);
		viewer = new TreeViewer(tree);
		viewer.setContentProvider(new TreeContentProvider());
		viewer.setLabelProvider(new TreeLabelProvider());
	}
	
	public void taskUpdated(ICompositeCheatSheetTask task) {
		viewer.update(task, null);
	}

	public Control getControl() {
		return viewer.getControl();
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public ISelectionProvider getSelectionProvider() {
		return viewer;
	}

	public void setCompositeCheatSheet(ICompositeCheatSheet compositeCheatSheet) {
		viewer.setInput(compositeCheatSheet);
	}

	public void dispose() {
		viewer.getLabelProvider().dispose();		
	}

	public void setSelection(ISelection selection, boolean reveal) {
		viewer.setSelection(selection, reveal);
	}

}