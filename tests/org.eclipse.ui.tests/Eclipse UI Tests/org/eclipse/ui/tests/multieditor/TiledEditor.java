/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.multieditor;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiEditor;
import org.eclipse.ui.tests.harness.util.CallHistory;

/**
 * Implementation of a TiledEditor. This is the testable version copied from bug
 * 42641.
 */
public class TiledEditor extends MultiEditor {

	private CLabel innerEditorTitle[];

	public CallHistory callHistory;

	public TiledEditor() {
		super();
		callHistory = new CallHistory(this);
	}

	/**
	 * technically not part of our framework API, but it will mark when the
	 * widgets were disposed.
	 */
	public void widgetsDisposed() {
		callHistory.add("widgetsDisposed");
	}

	/*
	 * @see IWorkbenchPart#createPartControl(Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		callHistory.add("createPartControl");

		parent.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				widgetsDisposed();
			}
		});

		parent = new Composite(parent, SWT.BORDER);

		parent.setLayout(new FillLayout());
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		IEditorPart innerEditors[] = getInnerEditors();

		for (int i = 0; i < innerEditors.length; i++) {
			final IEditorPart e = innerEditors[i];
			ViewForm viewForm = new ViewForm(sashForm, SWT.NONE);
			viewForm.marginWidth = 0;
			viewForm.marginHeight = 0;

			createInnerEditorTitle(i, viewForm);

			Composite content = createInnerPartControl(viewForm, e);

			viewForm.setContent(content);
			updateInnerEditorTitle(e, innerEditorTitle[i]);

			final int index = i;
			e.addPropertyListener(new IPropertyListener() {
				@Override
				public void propertyChanged(Object source, int property) {
					if (property == IEditorPart.PROP_DIRTY
							|| property == IWorkbenchPart.PROP_TITLE) {
						if (source instanceof IEditorPart) {
							updateInnerEditorTitle((IEditorPart) source,
									innerEditorTitle[index]);
						}
					}
				}
			});
		}
	}

	/**
	 * Draw the gradient for the specified editor.
	 */
	@Override
	protected void drawGradient(IEditorPart innerEditor, Gradient g) {
		CLabel label = innerEditorTitle[getIndex(innerEditor)];
		if ((label == null) || label.isDisposed()) {
			return;
		}

		label.setForeground(g.fgColor);
		label.setBackground(g.bgColors, g.bgPercents);
	}

	/*
	 * Create the label for each inner editor.
	 */
	protected void createInnerEditorTitle(int index, ViewForm parent) {

		CLabel titleLabel = new CLabel(parent, SWT.SHADOW_NONE);
		// hookFocus(titleLabel);
		titleLabel.setAlignment(SWT.LEFT);
		titleLabel.setBackground(null, null);
		parent.setTopLeft(titleLabel);
		if (innerEditorTitle == null) {
			innerEditorTitle = new CLabel[getInnerEditors().length];
		}
		innerEditorTitle[index] = titleLabel;
	}

	/*
	 * Update the tab for an editor. This is typically called by a site when the
	 * tab title changes.
	 */
	public void updateInnerEditorTitle(IEditorPart editor, CLabel label) {

		if ((label == null) || label.isDisposed()) {
			return;
		}
		String title = editor.getTitle();
		if (editor.isDirty())
		 {
			title = "*" + title; //$NON-NLS-1$
		}
		label.setText(title);
		Image image = editor.getTitleImage();
		if (image != null) {
			if (!image.equals(label.getImage())) {
				label.setImage(image);
			}
		}
		label.setToolTipText(editor.getTitleToolTip());
	}

	/*
	 *
	 */
	@Override
	protected int getIndex(IEditorPart editor) {
		IEditorPart innerEditors[] = getInnerEditors();
		for (int i = 0; i < innerEditors.length; i++) {
			if (innerEditors[i] == editor) {
				return i;
			}
		}
		return -1;
	}

	//
	// These are public methods from the parent that are overriden to
	// add them to the call history.
	//

	@Override
	public Composite createInnerPartControl(Composite parent, IEditorPart e) {
		callHistory.add("createInnerPartControl");
		return super.createInnerPartControl(parent, e);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		callHistory.add("init");
		super.init(site, input);
	}

	@Override
	public void setFocus() {
		callHistory.add("setFocus");
		super.setFocus();
	}

	@Override
	public void updateGradient(IEditorPart editor) {
		callHistory.add("updateGradient");
		super.updateGradient(editor);
	}

	@Override
	public void setInitializationData(IConfigurationElement cfig,
			String propertyName, Object data) {
		callHistory.add("setInitializationData");
		super.setInitializationData(cfig, propertyName, data);
	}

	@Override
	public void dispose() {
		callHistory.add("dispose");
		super.dispose();
	}
}
