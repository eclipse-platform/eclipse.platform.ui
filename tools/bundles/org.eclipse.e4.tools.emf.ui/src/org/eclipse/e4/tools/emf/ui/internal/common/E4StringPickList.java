/*******************************************************************************
 * Copyright (c) 2014, 2017 TwelveTone LLC and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Ongoing Maintenance
 *     Olivier Prouvost <olivier.prouvost@opcoach.com> - Bug 466731
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common;

import java.util.Arrays;
import java.util.List;

import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.MoveCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A PickList for creating a string list. Action include ordering, adding,
 * removing, and replacing.
 *
 * @author Steven Spungin
 */
public class E4StringPickList extends AbstractPickList {

	private final Text text;
	private final Button tiReplace;
	private final AbstractComponentEditor<?> editor;
	private final EStructuralFeature feature;

	@Deprecated
	public E4StringPickList(Composite parent, int flags, List<PickListFeatures> list, Messages messages,
			AbstractComponentEditor<?> editor, EStructuralFeature feature) {
		this(parent, flags, list, editor, feature);
	}

	public E4StringPickList(Composite parent, int flags, List<PickListFeatures> list,
			AbstractComponentEditor<?> editor,
			EStructuralFeature feature) {
		super(parent, flags, Arrays.asList(PickListFeatures.NO_PICKER), editor);

		this.editor = editor;
		this.feature = feature;

		// TODO does not respect NO_ORDER yet

		tiReplace = new Button(getToolBar(), SWT.PUSH);
		tiReplace.setToolTipText(Messages.E4StringPickList_Replace);
		tiReplace.setImage(editor.createImage(ResourceProvider.IMG_Obj16_world_edit));
		tiReplace.setFont(getButtonFont());

		Composite valueParent = new Composite(getToolBar().getParent(), SWT.BORDER);
		valueParent.setLayout(new GridLayout(5, false));
		valueParent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label l = new Label(valueParent, SWT.NONE);
		l.setText(Messages.E4StringPickList_NewValue);
		l.setToolTipText(Messages.E4StringPickList_ToolTipNewValue);
		text = new Text(valueParent, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		text.setToolTipText(Messages.E4StringPickList_ToolTipNewValue);

		tiReplace.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleReplaceText();
			}
		});

		getList().setContentProvider(new ObservableListContentProvider<>());

		text.addModifyListener(e -> updateUiState());

		text.addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(FocusEvent e) {
				updateUiState();

			}
		});

		updateUiState();

	}

	@Override
	protected void moveUpPressed() {
		if (!viewer.getSelection().isEmpty()) {
			IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
			if (s.size() == 1) {
				Object obj = s.getFirstElement();
				Object container = editor.getMaster().getValue();
				tryEObjectMove(obj, (EObject) container, -1);
			}
		}
	}

	@Override
	protected void moveDownPressed() {
		if (!viewer.getSelection().isEmpty()) {
			IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
			if (s.size() == 1) {
				Object obj = s.getFirstElement();
				Object container = editor.getMaster().getValue();
				tryEObjectMove(obj, (EObject) container, 1);
			}
		}
	}

	@Override
	protected void removePressed() {
		if (!viewer.getSelection().isEmpty()) {
			List<?> keybinding = ((IStructuredSelection) viewer.getSelection()).toList();
			Command cmd = RemoveCommand.create(editor.getEditingDomain(), editor.getMaster().getValue(), feature, keybinding);
			if (cmd.canExecute()) {
				editor.getEditingDomain().getCommandStack().execute(cmd);
			}
		}
	}

	protected void handleReplaceText() {
		if (getTextWidget().getText().trim().length() > 0) {
			if (!viewer.getSelection().isEmpty()) {
				String[] tags = getTextWidget().getText().split(";"); //$NON-NLS-1$
				for (int i = 0; i < tags.length; i++) {
					tags[i] = tags[i].trim();
				}

				MApplicationElement appEl = (MApplicationElement) editor.getMaster().getValue();
				EObject el = (EObject) editor.getMaster().getValue();
				List<?> ids = ((IStructuredSelection) viewer.getSelection()).toList();
				Object curVal = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				EObject container = (EObject) editor.getMaster().getValue();
				List<?> l = (List<?>) container.eGet(feature);
				int idx = l.indexOf(curVal);
				if (idx >= 0) {
					Command cmdRemove = RemoveCommand.create(editor.getEditingDomain(), el, feature, ids);
					Command cmdInsert = AddCommand.create(editor.getEditingDomain(), appEl, feature, Arrays.asList(tags), idx);
					if (cmdRemove.canExecute() && cmdInsert.canExecute()) {
						editor.getEditingDomain().getCommandStack().execute(cmdRemove);
						editor.getEditingDomain().getCommandStack().execute(cmdInsert);
					}
					getTextWidget().setText(""); //$NON-NLS-1$
				}
			}
		}
	}

	@Override
	public void updateUiState() {
		super.updateUiState();

		if (text != null) {
			IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
			Object firstViewerElement = sel.getFirstElement();
			String txt = text.getText();
			boolean diff = !txt.equals(firstViewerElement);
			tiReplace.setEnabled(firstViewerElement != null && !getTextWidget().getText().isEmpty() && diff);
			tiAdd.setEnabled(txt.length() > 0 && text.isFocusControl());
		}
	}

	@Override
	protected int getItemCount() {
		if (viewer.getContentProvider() == null || viewer.getInput() == null) {
			return 0;
		}
		return ((ObservableListContentProvider<?>) viewer.getContentProvider()).getElements(viewer.getInput()).length;
	}

	protected Button getReplaceWidget() {
		return tiReplace;
	}

	public Text getTextWidget() {
		return text;
	}

	protected void tryEObjectMove(Object obj, EObject container, int delta) {
		List<?> l = (List<?>) container.eGet(feature);
		int idx = l.indexOf(obj) + delta;
		if ((delta > 0 && idx < l.size()) || (delta < 0 && idx >= 0)) {
			Command cmd = MoveCommand.create(editor.getEditingDomain(), editor.getMaster().getValue(), feature, obj, idx);

			if (cmd.canExecute()) {
				editor.getEditingDomain().getCommandStack().execute(cmd);
				viewer.setSelection(new StructuredSelection(obj));
			}
		}
	}
}
