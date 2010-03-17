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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.tools.emf.ui.common.IModelResource.ModelListener;
import org.eclipse.e4.tools.emf.ui.internal.wbm.ApplicationModelEditor;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.engine.CSSErrorHandler;
import org.eclipse.e4.ui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.edit.ui.util.EditUIUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorPart;

@SuppressWarnings("restriction")
public class E4WorkbenchModelEditor extends EditorPart {
	private XMIModelResource resource;
	private ApplicationModelEditor editor;
	private UndoAction undoAction;
	private RedoAction redoAction;

	private static final String CSS_URI = "platform:/plugin/org.eclipse.e4.tools.emf.editor3x/css/default.css";

	private ModelListener listener = new ModelListener() {

		public void dirtyChanged() {
			firePropertyChange(PROP_DIRTY);
		}

		public void commandStackChanged() {
			// TODO Auto-generated method stub

		}
	};

	@Override
	public void doSave(IProgressMonitor monitor) {
		editor.save();
	}

	@Override
	public void doSaveAs() {

	}

	private void setupCss(Display display) {
		CSSEngine engine = (CSSEngine) display
				.getData("org.eclipse.e4.ui.css.core.engine");

		if (engine == null) {
			engine = new CSSSWTEngineImpl(display, true);
			engine.setErrorHandler(new CSSErrorHandler() {
				public void error(Exception e) {
					e.printStackTrace();
				}
			});
			display.setData("org.eclipse.e4.ui.css.core.engine", engine);

			try {
				URL url = FileLocator.resolve(new URL(CSS_URI.toString()));
				display.setData("org.eclipse.e4.ui.css.core.cssURL", url); //$NON-NLS-1$		

				InputStream stream = url.openStream();
				engine.parseStyleSheet(stream);
				stream.close();
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);

		URI resourceURI = EditUIUtil.getURI(input);
		if (resourceURI != null) {
			resource = new XMIModelResource(resourceURI);
			resource.addModelListener(listener);
		}
	}

	@Override
	public boolean isDirty() {
		return resource.isDirty();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		setupCss(parent.getDisplay());
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setBackground(comp.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		FillLayout layout = new FillLayout();
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		comp.setLayout(layout);
		IFileEditorInput input = (IFileEditorInput)getEditorInput();
		editor = new ApplicationModelEditor(comp, resource, input.getFile().getProject());

		try {
			parent.setRedraw(false);
			parent.reskin(SWT.ALL);
		} finally {
			parent.setRedraw(true);
		}

		makeActions();
	}

	private void makeActions() {
		undoAction = new UndoAction(resource);
		redoAction = new RedoAction(resource);

		getEditorSite().getActionBars().setGlobalActionHandler(
				ActionFactory.UNDO.getId(), undoAction);
		getEditorSite().getActionBars().setGlobalActionHandler(
				ActionFactory.REDO.getId(), redoAction);
	}

	@Override
	public void dispose() {
		if (undoAction != null)
			undoAction.dispose();

		if (redoAction != null)
			redoAction.dispose();

		if (listener != null && resource != null)
			resource.removeModelListener(listener);

		super.dispose();
	}

	@Override
	public void setFocus() {
		editor.setFocus();
	}

}
