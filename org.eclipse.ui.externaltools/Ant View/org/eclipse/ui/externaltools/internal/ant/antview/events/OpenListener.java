/*
 * Copyright (c) 2002, Roscoe Rush. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public License
 * Version 0.5 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.eclipse.org/
 *
 */
package org.eclipse.ui.externaltools.internal.ant.antview.events;

import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.ui.externaltools.internal.ant.antview.tree.TreeNode;

public class OpenListener implements IOpenListener {
     public void open(OpenEvent e) {
		TreeViewer view = (TreeViewer) e.getViewer();
		IStructuredSelection selection = (IStructuredSelection) e.getSelection();
		
		try {
		   TreeNode item = (TreeNode) selection.getFirstElement();
		   item.setSelected();
		} catch (ClassCastException cce) {
		   return;
		} catch (NullPointerException npe) {
		   return;
		}
		view.refresh();     
     }
}
