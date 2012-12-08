/*******************************************************************************
 * Copyright (c) 2012 Remain BV and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wim Jongman <wim.jongman@remainsoftware.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.uistructure;

import java.util.List;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.IEditorFeature.FeatureClass;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.services.Translation;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.MoveCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
	private EReference reference;

	/**
	 * @param parent
	 * @param reference
	 * @param editor
	 */
	@Inject
	public ViewerElement(@Translation Messages Messages, Composite parent, EReference reference, AbstractComponentEditor editor) {
		this.parent = parent;
		this.reference = reference;
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

		Button b = new Button(buttonCompBot, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.ModelTooling_Common_Up);
		b.setImage(editor.createImage(ResourceProvider.IMG_Obj16_arrow_up));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
					if (s.size() == 1) {
						Object obj = s.getFirstElement();
						MModelFragments container = (MModelFragments) editor.getMaster().getValue();
						int idx = container.getImports().indexOf(obj) - 1;
						if (idx >= 0) {
							Command cmd = MoveCommand.create(editor.getEditingDomain(), editor.getMaster().getValue(), reference, obj, idx);

							if (cmd.canExecute()) {
								editor.getEditingDomain().getCommandStack().execute(cmd);
								viewer.setSelection(new StructuredSelection(obj));
							}
						}

					}
				}
			}
		});

		b = new Button(buttonCompBot, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.ModelTooling_Common_Down);
		b.setImage(editor.createImage(ResourceProvider.IMG_Obj16_arrow_down));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
					if (s.size() == 1) {
						Object obj = s.getFirstElement();
						MModelFragments container = (MModelFragments) editor.getMaster().getValue();
						int idx = container.getImports().indexOf(obj) + 1;
						if (idx < container.getImports().size()) {
							Command cmd = MoveCommand.create(editor.getEditingDomain(), editor.getMaster().getValue(), reference, obj, idx);

							if (cmd.canExecute()) {
								editor.getEditingDomain().getCommandStack().execute(cmd);
								viewer.setSelection(new StructuredSelection(obj));
							}
						}

					}
				}
			}
		});

		b = new Button(buttonCompBot, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.ModelTooling_Common_Remove);
		b.setImage(editor.createImage(ResourceProvider.IMG_Obj16_table_delete));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					List<?> elements = ((IStructuredSelection) viewer.getSelection()).toList();

					Command cmd = RemoveCommand.create(editor.getEditingDomain(), editor.getMaster().getValue(), reference, elements);
					if (cmd.canExecute()) {
						editor.getEditingDomain().getCommandStack().execute(cmd);
					}
				}
			}
		});
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

		dropDown = new ComboViewer(buttonCompTop);
		dropDown.getControl().setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

		addButton = new Button(buttonCompTop, SWT.PUSH | SWT.FLAT);
		addButton.setImage(editor.createImage(ResourceProvider.IMG_Obj16_table_add));
		addButton.setText(Messages.ModelTooling_Common_AddEllipsis);
		addButton.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				EClass eClass = ((FeatureClass) ((IStructuredSelection) dropDown.getSelection()).getFirstElement()).eClass;
				EObject eObject = EcoreUtil.create(eClass);

				Command cmd = AddCommand.create(editor.getEditingDomain(), editor.getMaster().getValue(), reference, eObject);

				if (cmd.canExecute()) {
					editor.getEditingDomain().getCommandStack().execute(cmd);
					editor.getEditor().setSelection(eObject);
				}
			}
		});

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
	public static ViewerElement create(IEclipseContext parentContext, Composite parent, EReference reference, AbstractComponentEditor editor) {
		IEclipseContext mycontext = parentContext.createChild();
		mycontext.set(Composite.class, parent);
		mycontext.set(AbstractComponentEditor.class, editor);
		mycontext.set(EReference.class, FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS);
		return ContextInjectionFactory.make(ViewerElement.class, mycontext);
	}
}
