/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.externaltools.internal.ant.editor.outline;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.ui.externaltools.internal.ant.editor.IOutlineCreationListener;
import org.eclipse.ui.externaltools.internal.ant.editor.PlantyEditor;


public class NotifyingReconciler extends MonoReconciler implements IOutlineCreationListener {

	private List fReconcilingParticipants= new ArrayList();
	
	private PlantyEditor fEditor;
	
	/**
	 * Constructor for NotifyingReconciler.
	 * @param strategy
	 * @param isIncremental
	 */
	public NotifyingReconciler(PlantyEditor editor, IReconcilingStrategy strategy, boolean isIncremental) {
		super(strategy, isIncremental);
		fEditor= editor;
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.AbstractReconciler#process(org.eclipse.jface.text.reconciler.DirtyRegion)
	 */
	protected void process(DirtyRegion dirtyRegion) {
		super.process(dirtyRegion);
		notifyReconcilingParticipants();
	}

	public void addReconcilingParticipant(IReconcilingParticipant participant) {
		fReconcilingParticipants.add(participant);
	}

	public void removeReconcilingParticipant(IReconcilingParticipant participant) {
		fReconcilingParticipants.remove(participant);
	}

	protected void notifyReconcilingParticipants() {
		Iterator i= new ArrayList(fReconcilingParticipants).iterator();
		while (i.hasNext()) {
			((IReconcilingParticipant) i.next()).reconciled();
		}
	}

	/*
	 * @see org.eclipse.ui.externaltools.internal.ant.editor.IOutlineCreationListener#outlineCreated()
	 */
	public void outlineCreated() {
		IReconcilingStrategy reconcilingStrategy= getReconcilingStrategy(""); //$NON-NLS-1$
		if (reconcilingStrategy instanceof IOutlineCreationListener) {
			((IOutlineCreationListener) reconcilingStrategy).outlineCreated();
		}
		forceReconciling();
	}
	
	/*
	 * @see org.eclipse.jface.text.reconciler.IReconciler#install(org.eclipse.jface.text.ITextViewer)
	 */
	public void install(ITextViewer textViewer) {
		super.install(textViewer);
		fEditor.addOutlineCreationListener(this);
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconciler#uninstall()
	 */
	public void uninstall() {
		fEditor.removeOutlineCreationListener(this);
		super.uninstall();
	}

}
