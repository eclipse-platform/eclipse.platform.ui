/*******************************************************************************
 * Copyright (c) 2010-2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Andrej ten Brummelhuis <andrejbrummelhuis@gmail.com> - Bug 395283
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.PatternFilter;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.fragment.MModelFragment;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.model.fragment.MStringModelFragment;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SharedElementsDialog extends SaveDialogBoundsSettingsDialog {
	private TableViewer viewer;
	private MPlaceholder placeholder;
	private IModelResource resource;
	private ModelEditor editor;
	private Messages Messages;

	public SharedElementsDialog(Shell parentShell, ModelEditor editor, MPlaceholder placeholder, IModelResource resource, Messages Messages) {
		super(parentShell);
		this.editor = editor;
		this.placeholder = placeholder;
		this.resource = resource;
		this.Messages = Messages;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);

		getShell().setText(Messages.SharedElementsDialog_ShellTitle);
		setTitle(Messages.SharedElementsDialog_Title);
		setMessage(Messages.SharedElementsDialog_Message);

		Composite container = new Composite(comp, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label l = new Label(container, SWT.NONE);
		l.setText(Messages.SharedElementsDialog_Name);

		Text searchText = new Text(container, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
		searchText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		l = new Label(container, SWT.NONE);

		viewer = new TableViewer(container);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new LabelProviderImpl());
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});

		if (resource.getRoot().get(0) instanceof MApplication) {
			List<MUIElement> list = new ArrayList<MUIElement>();
			for (MWindow m : ((MApplication) resource.getRoot().get(0)).getChildren()) {
				list.addAll(filter(m.getSharedElements()));
			}
			viewer.setInput(list);
		} else if (resource.getRoot().get(0) instanceof MModelFragments) {
			List<MApplicationElement> list = new ArrayList<MApplicationElement>();
			for (MModelFragment f : ((MModelFragments) resource.getRoot().get(0)).getFragments()) {
				if (f instanceof MStringModelFragment) {
					if (((MStringModelFragment) f).getFeaturename().equals("sharedElements")) { //$NON-NLS-1$
						list.addAll(filter(f.getElements()));
					}
				}
			}

			// NEW IMPLEMENTATION:
			for (MApplicationElement f : ((MModelFragments) resource.getRoot().get(0)).getImports()) {
				// let filter() do its job
				list.addAll(filter(Collections.singletonList(f)));
			}
			viewer.setInput(list);
		}

		final PatternFilter filter = new PatternFilter() {
			@Override
			protected boolean isParentMatch(Viewer viewer, Object element) {
				return viewer instanceof AbstractTreeViewer && super.isParentMatch(viewer, element);
			}
		};
		viewer.addFilter(filter);

		ControlFactory.attachFiltering(searchText, viewer, filter);

		return comp;
	}

	@Override
	protected void okPressed() {
		if (!viewer.getSelection().isEmpty()) {
			IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
			Command cmd = SetCommand.create(resource.getEditingDomain(), placeholder, AdvancedPackageImpl.Literals.PLACEHOLDER__REF, s.getFirstElement());
			if (cmd.canExecute()) {
				resource.getEditingDomain().getCommandStack().execute(cmd);
				super.okPressed();
			}
		}
	}

	private static <T> List<T> filter(List<T> o) {
		List<T> rv = new ArrayList<T>();
		for (T i : o) {
			if (i instanceof MPart || i instanceof MPartSashContainer || i instanceof MArea || i instanceof MPartStack) {
				rv.add(i);
			}
		}
		return rv;
	}

	private class LabelProviderImpl extends StyledCellLabelProvider implements ILabelProvider {
		@Override
		public void update(final ViewerCell cell) {
			EObject o = (EObject) cell.getElement();

			StyledString string = new StyledString(getTypename(o));

			if (o instanceof MUILabel) {
				string.append(" - " + ((MUILabel) o).getLabel(), StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
			}

			MApplicationElement el = (MApplicationElement) o;
			string.append(" - " + el.getElementId(), StyledString.DECORATIONS_STYLER); //$NON-NLS-1$

			cell.setText(string.getString());
			cell.setStyleRanges(string.getStyleRanges());
			cell.setImage(getImage(o));
		}

		@Override
		public String getText(Object element) {
			EObject o = (EObject) element;
			MApplicationElement el = (MApplicationElement) o;

			if (el instanceof MUILabel) {
				MUILabel label = (MUILabel) el;
				return getTypename(o) + " - " + el.getElementId() + " - " + label.getLabel(); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				return getTypename(o) + " - " + el.getElementId() + " - "; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		private String getTypename(EObject o) {
			AbstractComponentEditor editor = SharedElementsDialog.this.editor.getEditor((o).eClass());
			if (editor != null) {
				return editor.getLabel(o);
			} else {
				return o.eClass().getName();
			}
		}

		@Override
		public Image getImage(Object element) {
			AbstractComponentEditor editor = SharedElementsDialog.this.editor.getEditor(((EObject) element).eClass());
			if (editor != null) {
				return editor.getImage(element, getShell().getDisplay());
			}
			return null;
		}
	}
}
