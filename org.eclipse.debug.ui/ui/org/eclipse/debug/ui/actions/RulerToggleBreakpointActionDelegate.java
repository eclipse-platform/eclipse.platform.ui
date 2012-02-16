/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.actions;

import org.eclipse.swt.widgets.Event;

import org.eclipse.jface.action.IAction;

import org.eclipse.jface.text.source.IVerticalRulerInfo;

import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Toggles a breakpoint when ruler is double-clicked. This action delegate can be 
 * contributed to an editor with the <code>editorActions</code> extension point.
 * This action is as a factory that creates another action that performs the
 * actual breakpoint toggling. The created action acts on the editor's
 * <code>IToggleBreakpointsTagret</code> to toggle breakpoints.
 * <p>
 * Following is example plug-in XML used to contribute this action to an editor.
 * Note that the label attribute of this action is not displayed in the editor.
 * Instead, the label of the created action is displayed. 
 * <pre>
 * &lt;extension point="org.eclipse.ui.editorActions"&gt;
 *    &lt;editorContribution
 *          targetID="example.editor"
 *          id="example.rulerActions"&gt;
 *       &lt;action
 *             label="Not Used"
 *             class="org.eclipse.debug.ui.actions.RulerToggleBreakpointActionDelegate"
 *             style="push"
 *             actionID="RulerDoubleClick"
 *             id="example.doubleClickBreakpointAction"/&gt;
 *    &lt;/editorContribution&gt;
 * &lt;/extension&gt;
 * </pre>
 * </p>
 * <p>
 * This action can also be contributed to a vertical ruler context menu via the
 * <code>popupMenus</code> extension point, by referencing the ruler's context
 * menu identifier in the <code>targetID</code> attribute.
 * <pre>
 * &lt;extension point="org.eclipse.ui.popupMenus"&gt;
 *   &lt;viewerContribution
 *     targetID="example.rulerContextMenuId"
 *     id="example.RulerPopupActions"&gt;
 *       &lt;action
 *         label="Toggle Breakpoint"
 *         class="org.eclipse.debug.ui.actions.RulerToggleBreakpointActionDelegate"
 *         menubarPath="additions"
 *         id="example.rulerContextMenu.toggleBreakpointAction"&gt;
 *       &lt;/action&gt;
 *   &lt;/viewerContribution&gt;
 * </pre>
 * </p>
 * <p>
 * Clients may refer to this class as an action delegate in plug-in XML. 
 * </p>
 * @since 3.1
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class RulerToggleBreakpointActionDelegate extends AbstractRulerActionDelegate implements IActionDelegate2 {
	
	private IEditorPart fEditor = null;
	private ToggleBreakpointAction fDelegate = null;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractRulerActionDelegate#createAction(org.eclipse.ui.texteditor.ITextEditor, org.eclipse.jface.text.source.IVerticalRulerInfo)
	 */
	protected IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo) {
		fDelegate = new ToggleBreakpointAction(editor, null, rulerInfo);
		return fDelegate;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction callerAction, IEditorPart targetEditor) {
		if (fEditor != null) {
			if (fDelegate != null) {
				fDelegate.dispose();
				fDelegate = null;
			}
		}
		fEditor = targetEditor;
		super.setActiveEditor(callerAction, targetEditor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	public void dispose() {
		if (fDelegate != null) {
			fDelegate.dispose();
		}
		fDelegate = null;
		fEditor = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		if(fDelegate != null) {
			fDelegate.runWithEvent(event);
		}
	}
}
