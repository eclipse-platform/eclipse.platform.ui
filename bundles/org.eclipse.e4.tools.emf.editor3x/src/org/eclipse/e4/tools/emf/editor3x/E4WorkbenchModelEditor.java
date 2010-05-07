/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.editor3x;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.tools.emf.editor3x.compat.E4CompatEditorPart;
import org.eclipse.e4.tools.emf.ui.common.IModelResource.ModelListener;
import org.eclipse.e4.tools.emf.ui.internal.wbm.ApplicationModelEditor;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.emf.common.util.URI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorPart;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

@SuppressWarnings("restriction")
public class E4WorkbenchModelEditor extends E4CompatEditorPart {

	public E4WorkbenchModelEditor() {
		super("platform:/plugin/org.eclipse.e4.tools.emf.ui/org.eclipse.e4.tools.emf.ui.internal.wbm.ApplicationModelEditor");
	}
	
//	private XMIModelResource resource;
//	private ApplicationModelEditor editor;
//	private UndoAction undoAction;
//	private RedoAction redoAction;
//
//	private ModelListener listener = new ModelListener() {
//
//		public void dirtyChanged() {
//			firePropertyChange(PROP_DIRTY);
//		}
//
//		public void commandStackChanged() {
//			// TODO Auto-generated method stub
//
//		}
//	};
//
//	@Override
//	public void doSave(IProgressMonitor monitor) {
//		editor.doSave(monitor);
//	}
//
//	@Override
//	public void doSaveAs() {
//
//	}
//
//	@Override
//	public void init(IEditorSite site, IEditorInput input)
//			throws PartInitException {
//		setSite(site);
//		setInput(input);
//
//		URI resourceURI = EditUIUtil.getURI(input);
//		if (resourceURI != null) {
//			resource = new XMIModelResource(resourceURI);
//			resource.addModelListener(listener);
//		}
//	}
//
//	@Override
//	public boolean isDirty() {
//		return resource.isDirty();
//	}
//
//	@Override
//	public boolean isSaveAsAllowed() {
//		return false;
//	}
//
//	@Override
//	public void createPartControl(Composite parent) {
//		Composite comp = new Composite(parent, SWT.NONE);
//		comp.setBackground(comp.getDisplay().getSystemColor(SWT.COLOR_WHITE));
//		comp.setBackgroundMode(SWT.INHERIT_DEFAULT);
//
//		FillLayout layout = new FillLayout();
//		layout.marginWidth = 10;
//		layout.marginHeight = 10;
//		comp.setLayout(layout);
//		IFileEditorInput input = (IFileEditorInput)getEditorInput();
//		editor = new ApplicationModelEditor(comp, resource, input.getFile().getProject());
//
//		try {
//			parent.setRedraw(false);
//			parent.reskin(SWT.ALL);
//		} finally {
//			parent.setRedraw(true);
//		}
//
//		makeActions();
//		Bundle b = FrameworkUtil.getBundle(E4WorkbenchModelEditor.class);
//		if( b != null ) {
//			ServiceReference ref = b.getBundleContext().getServiceReference(IThemeManager.class.getName());
//			if( ref != null ) {
//				IThemeManager mgr = (IThemeManager) b.getBundleContext().getService(ref);
//				IThemeEngine engine = mgr.getEngineForDisplay(parent.getDisplay());
//				engine.applyStyles(parent, true);
//			}			
//		}
//	}
//
//	private void makeActions() {
//		undoAction = new UndoAction(resource);
//		redoAction = new RedoAction(resource);
//
//		getEditorSite().getActionBars().setGlobalActionHandler(
//				ActionFactory.UNDO.getId(), undoAction);
//		getEditorSite().getActionBars().setGlobalActionHandler(
//				ActionFactory.REDO.getId(), redoAction);
//	}
//
//	@Override
//	public void dispose() {
//		if (undoAction != null)
//			undoAction.dispose();
//
//		if (redoAction != null)
//			redoAction.dispose();
//
//		if (listener != null && resource != null)
//			resource.removeModelListener(listener);
//
//		super.dispose();
//	}
//
//	@Override
//	public void setFocus() {
//		editor.setFocus();
//	}

}
