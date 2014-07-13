/*******************************************************************************
 * Copyright (c) 2012 Remain BV and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wim Jongman <wim.jongman@remainsoftware.com> - initial API and implementation
 *     Steven Spungin <steve@spungin.tv> -Bug 439284 - [model editor] make list a combo with autocomplete for add part descriptor
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.uistructure;

import javax.inject.Inject;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.ComboViewerAutoComplete;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.services.Translation;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * A viewer with buttons.
 * 
 * @author wim.jongman@remainsoftware.com
 * 
 */
public class ViewerElement {

	Messages Messages;

	private StructuredViewer viewer;
	private AbstractComponentEditor editor;
	private ComboViewer dropDown;
	private Button addButton;
	private Composite parent;

	private Button removeButton;

	private Button downButton;

	private Button upButton;

	/**
	 * @param parent
	 * @param reference
	 * @param editor
	 */
	@Inject
	public ViewerElement(@Translation Messages Messages, Composite parent, AbstractComponentEditor editor) {
		this.parent = parent;
		this.editor = editor;
		this.Messages = Messages;
		createControl();
	}

	public StructuredViewer getViewer() {
		return viewer;
	}

	public void createControl() {

		createTopButtons();

		if (viewer == null)
			viewer = new TableViewer(parent);

		GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true, 3, 1);
		viewer.getControl().setLayoutData(gd);

		createBottomButtons();

	}

	private void createBottomButtons() {
		Composite buttonCompBot = new Composite(parent, SWT.NONE);
		buttonCompBot.setLayoutData(new GridData(GridData.FILL, GridData.END, false, false, 3, 1));
		GridLayout gl = new GridLayout(3, false);
		gl.marginLeft = 0;
		gl.marginRight = 0;
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		buttonCompBot.setLayout(gl);

		upButton = new Button(buttonCompBot, SWT.PUSH | SWT.FLAT);
		upButton.setText(Messages.ModelTooling_Common_Up);
		upButton.setImage(editor.createImage(ResourceProvider.IMG_Obj16_arrow_up));
		upButton.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));

		downButton = new Button(buttonCompBot, SWT.PUSH | SWT.FLAT);
		downButton.setText(Messages.ModelTooling_Common_Down);
		downButton.setImage(editor.createImage(ResourceProvider.IMG_Obj16_arrow_down));
		downButton.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));

		removeButton = new Button(buttonCompBot, SWT.PUSH | SWT.FLAT);
		removeButton.setText(Messages.ModelTooling_Common_Remove);
		removeButton.setImage(editor.createImage(ResourceProvider.IMG_Obj16_table_delete));
		removeButton.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));

	}

	/**
	 * Returns the button that removes an element from the list. Use it to add
	 * your {@link SelectionListener} to it.
	 * 
	 * @return
	 */
	public Button getButtonRemove() {
		return removeButton;
	}

	/**
	 * Returns the button that adds an element to the list. Use it to add your
	 * {@link SelectionListener} to it.
	 * 
	 * @return
	 */
	public Button getButtonAdd() {
		return addButton;
	}

	/**
	 * Returns the button that moves an element down in the list. Use it to add
	 * your {@link SelectionListener} to it.
	 * 
	 * @return
	 */
	public Button getButtonDown() {
		return downButton;
	}

	/**
	 * Returns the button that moves an element up in the list. Use it to add
	 * your {@link SelectionListener} to it.
	 * 
	 * @return
	 */
	public Button getButtonUp() {
		return upButton;
	}

	private void createTopButtons() {
		Composite buttonCompTop = new Composite(parent, SWT.NONE);
		buttonCompTop.setLayoutData(new GridData(GridData.FILL, GridData.END, false, false, 3, 1));
		GridLayout buttonCompTopLayout = new GridLayout(2, false);
		buttonCompTopLayout.marginLeft = 0;
		buttonCompTopLayout.marginRight = 0;
		buttonCompTopLayout.marginWidth = 0;
		buttonCompTopLayout.marginHeight = 0;
		buttonCompTop.setLayout(buttonCompTopLayout);

		dropDown = new ComboViewer(buttonCompTop, SWT.READ_ONLY | SWT.SIMPLE);
		dropDown.getControl().setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

		ComboViewerAutoComplete autocomplete = new ComboViewerAutoComplete(dropDown);

		addButton = new Button(buttonCompTop, SWT.PUSH | SWT.FLAT);
		addButton.setImage(editor.createImage(ResourceProvider.IMG_Obj16_table_add));
		addButton.setText(Messages.ModelTooling_Common_AddEllipsis);
		addButton.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

	}

	public ComboViewer getDropDown() {
		return dropDown;
	}

	/**
	 * Creates and returns a new instance of this class based on the supplied
	 * context.
	 * 
	 * @param parentContext
	 * @param parent
	 * @param reference
	 * @param editor
	 * @return a new {@link ViewerElement}
	 */
	public static ViewerElement create(IEclipseContext parentContext, Composite parent, AbstractComponentEditor editor) {
		IEclipseContext mycontext = parentContext.createChild();
		mycontext.set(Composite.class, parent);
		mycontext.set(AbstractComponentEditor.class, editor);
		return ContextInjectionFactory.make(ViewerElement.class, mycontext);
	}
}
