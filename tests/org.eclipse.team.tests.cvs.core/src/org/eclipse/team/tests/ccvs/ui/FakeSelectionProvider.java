package org.eclipse.team.tests.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Serves up fake selections.
 * 
 * Note: originally borrowed from org.eclipse.jdt.ui.tests.actions
 */
public class FakeSelectionProvider implements ISelectionProvider {
	private Object[] fElems;
	public FakeSelectionProvider(Object[] elements){
		fElems = elements;
	}
	
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
	}

	public ISelection getSelection() {
		return new StructuredSelection(fElems);
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
	}

	public void setSelection(ISelection selection) {
	}
}
