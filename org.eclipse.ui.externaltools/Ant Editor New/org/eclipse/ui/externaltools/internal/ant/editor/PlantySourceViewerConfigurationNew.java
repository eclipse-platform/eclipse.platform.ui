/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.ui.externaltools.internal.ant.editor;

import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.externaltools.internal.ant.editor.outline.NotifyingReconciler;
import org.eclipse.ui.externaltools.internal.ant.editor.outline.XMLReconcilingStrategy;

/**
 * PlantySourceViewerConfiguration.java
 */
public class PlantySourceViewerConfigurationNew extends PlantySourceViewerConfiguration {

	private PlantyEditor fEditor;

	public PlantySourceViewerConfigurationNew(PlantyEditor editor) {
		super();
		fEditor= editor;
	}
	
	/*
	 * @see SourceViewerConfiguration#getReconciler(ISourceViewer)
	 */
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		NotifyingReconciler reconciler= new NotifyingReconciler(fEditor, new XMLReconcilingStrategy(fEditor), false);
		reconciler.setDelay(500);
//		TODO: remove or implement?
//		reconciler.addReconcilingParticipant(fEditor);
		return reconciler;
	}

}
