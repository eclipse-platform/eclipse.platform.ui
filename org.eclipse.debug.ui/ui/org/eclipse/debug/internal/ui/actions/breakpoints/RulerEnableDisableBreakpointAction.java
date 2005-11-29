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
package org.eclipse.debug.internal.ui.actions.breakpoints;

import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

/**
 * @since 3.2
 *
 */
public class RulerEnableDisableBreakpointAction extends Action implements IUpdate {
	
	private ITextEditor fEditor;
	private IVerticalRulerInfo fRulerInfo;
	private IBreakpoint fBreakpoint;
	
	public RulerEnableDisableBreakpointAction(ITextEditor editor, IVerticalRulerInfo info) {
		fEditor = editor;
		fRulerInfo = info;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		if (fBreakpoint != null) {
			try {
				fBreakpoint.setEnabled(!fBreakpoint.isEnabled());
			} catch (CoreException e) {
				DebugUIPlugin.errorDialog(fEditor.getSite().getShell(), ActionMessages.RulerEnableDisableBreakpointAction_0, ActionMessages.RulerEnableDisableBreakpointAction_1, e.getStatus());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		fBreakpoint = null;
		IAnnotationModel annotationModel = fEditor.getDocumentProvider().getAnnotationModel(fEditor.getEditorInput());
		IDocument document = fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
		if (annotationModel != null) {
			Iterator iterator = annotationModel.getAnnotationIterator();
			while (iterator.hasNext()) {
				Object object = iterator.next();
				if (object instanceof SimpleMarkerAnnotation) {
					SimpleMarkerAnnotation markerAnnotation = (SimpleMarkerAnnotation) object;
					IMarker marker = markerAnnotation.getMarker();
					try {
						if (marker.isSubtypeOf(IBreakpoint.BREAKPOINT_MARKER)) {
							Position position = annotationModel.getPosition(markerAnnotation);
							int line = document.getLineOfOffset(position.getOffset());
							if (line == fRulerInfo.getLineOfLastMouseButtonActivity()) {
								IBreakpoint breakpoint = DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(marker);
								if (breakpoint != null) {
									if (breakpoint.isEnabled()) {
										setText(ActionMessages.RulerEnableDisableBreakpointAction_2);
									} else {
										setText(ActionMessages.RulerEnableDisableBreakpointAction_3);
									}
									fBreakpoint = breakpoint;
									setEnabled(true);
									return;
								}
							}
						}
					} catch (CoreException e) {
					} catch (BadLocationException e) {
					}
				}
			}
		}
		setEnabled(false);
	}

}
