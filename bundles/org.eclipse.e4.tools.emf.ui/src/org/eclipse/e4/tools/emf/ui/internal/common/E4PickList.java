/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common;

import java.util.List;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.MoveCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;

/**
 * <p>
 * Pressing CR in the combo will execute the ADD command.
 * </p>
 * <p>
 * <em>E4 specific:</em> The default feature is
 * UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN. Other features must
 * override appropriate methods. The picker uses an Array Content Provider. The
 * list uses an ObservableListContentProvider and a Struct class to contain the
 * object and label.
 * </p>
 *
 * @author Steven Spungin
 *
 */
public class E4PickList extends AbstractPickList {

	AbstractComponentEditor componentEditor;
	EStructuralFeature feature;

	public static class Struct {
		final public String label;
		final public EClass eClass;
		final public boolean separator;

		public Struct(String label, EClass eClass, boolean separator) {
			this.label = label;
			this.eClass = eClass;
			this.separator = separator;
		}
	}

	public E4PickList(Composite parent, int style, List<PickListFeatures> listFeatures, final Messages messages, final AbstractComponentEditor componentEditor, final EStructuralFeature feature) {
		super(parent, style, listFeatures, messages, componentEditor);

		this.componentEditor = componentEditor;
		this.feature = feature;

		picker.setContentProvider(new ArrayContentProvider());
		picker.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				Struct struct = (Struct) element;
				return struct.label;
			}
		});

		viewer.setLabelProvider(new ComponentLabelProvider(componentEditor.getEditor(), messages));
		ObservableListContentProvider cp = new ObservableListContentProvider();
		viewer.setContentProvider(cp);
	}

	/**
	 * Used for StringToString maps
	 *
	 * @param obj
	 * @param container
	 * @param i
	 */
	protected void tryEObjectMove(Object obj, EObject container, int delta) {
		List<?> l = (List<?>) container.eGet(feature);
		int idx = l.indexOf(obj) + delta;
		if ((delta > 0 && idx < l.size()) || (delta < 0 && idx >= 0)) {
			Command cmd = MoveCommand.create(componentEditor.getEditingDomain(), componentEditor.getMaster().getValue(), feature, obj, idx);

			if (cmd.canExecute()) {
				componentEditor.getEditingDomain().getCommandStack().execute(cmd);
				viewer.setSelection(new StructuredSelection(obj));
			}
		}
	}

	@Override
	protected void addPressed() {
		if (!picker.getSelection().isEmpty()) {
			Struct struct = (Struct) ((IStructuredSelection) picker.getSelection()).getFirstElement();
			EClass eClass = struct.eClass;
			_handleAdd(eClass, struct.separator);
		}
	}

	protected void _handleAdd(EClass eClass, boolean separator) {
		MMenuElement eObject = (MMenuElement) EcoreUtil.create(eClass);
		setElementId(eObject);
		Command cmd = AddCommand.create(componentEditor.getEditingDomain(), componentEditor.getMaster().getValue(), feature, eObject);

		if (cmd.canExecute()) {
			componentEditor.getEditingDomain().getCommandStack().execute(cmd);
			if (!separator) {
				componentEditor.getEditor().setSelection(eObject);
			}
		}
	}

	protected void setElementId(Object element) {
		if (componentEditor.getEditor().isAutoCreateElementId() && element instanceof MApplicationElement) {
			MApplicationElement el = (MApplicationElement) element;
			if (el.getElementId() == null || el.getElementId().trim().length() == 0) {
				el.setElementId(Util.getDefaultElementId(((EObject) componentEditor.getMaster().getValue()).eResource(), el, componentEditor.getEditor().getProject()));
			}
		}
	}

	protected List<?> getContainerChildren(Object master) {
		if (master instanceof MElementContainer<?>) {
			return ((MElementContainer<?>) master).getChildren();
		} else {
			return null;
		}
	}

	@Override
	protected void moveUpPressed() {
		if (!viewer.getSelection().isEmpty()) {
			IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
			if (s.size() == 1) {
				Object obj = s.getFirstElement();
				Object container = componentEditor.getMaster().getValue();
				if (feature == ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE) {
					tryEObjectMove(obj, (EObject) container, -1);
					return;
				}
				List<?> children = getContainerChildren(container);
				if (children == null) {
					return;
				}
				int idx = children.indexOf(obj) - 1;
				if (idx >= 0) {
					if (obj instanceof MUIElement && feature == UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN) {
						if (Util.moveElementByIndex(componentEditor.getEditingDomain(), (MUIElement) obj, componentEditor.getEditor().isLiveModel(), idx)) {
							viewer.setSelection(new StructuredSelection(obj));
						}
					} else if (obj instanceof MApplicationElement) {
						Command cmd = MoveCommand.create(componentEditor.getEditingDomain(), componentEditor.getMaster().getValue(), feature, obj, idx);
						if (cmd.canExecute()) {
							componentEditor.getEditingDomain().getCommandStack().execute(cmd);
							viewer.setSelection(new StructuredSelection(obj));
						}
					}
				}
			}
		}
	}

	@Override
	protected void moveDownPressed() {
		if (!viewer.getSelection().isEmpty()) {
			IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
			if (s.size() == 1) {
				Object obj = s.getFirstElement();
				Object container = componentEditor.getMaster().getValue();
				if (feature == ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE) {
					tryEObjectMove(obj, (EObject) container, 1);
					return;
				}
				List<?> children = getContainerChildren(container);
				if (children == null) {
					return;
				}
				int idx = children.indexOf(obj) + 1;
				if (idx < children.size()) {
					if (obj instanceof MUIElement && feature == UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN) {
						if (Util.moveElementByIndex(componentEditor.getEditingDomain(), (MUIElement) obj, componentEditor.getEditor().isLiveModel(), idx)) {
							viewer.setSelection(new StructuredSelection(obj));
						}
					} else if (obj instanceof MApplicationElement) {
						Command cmd = MoveCommand.create(componentEditor.getEditingDomain(), componentEditor.getMaster().getValue(), feature, obj, idx);
						if (cmd.canExecute()) {
							componentEditor.getEditingDomain().getCommandStack().execute(cmd);
							viewer.setSelection(new StructuredSelection(obj));
						}
					}
				}
			}
		}
	}

	@Override
	protected void removePressed() {
		if (!viewer.getSelection().isEmpty()) {
			List<?> keybinding = ((IStructuredSelection) viewer.getSelection()).toList();
			Command cmd = RemoveCommand.create(componentEditor.getEditingDomain(), componentEditor.getMaster().getValue(), feature, keybinding);
			if (cmd.canExecute()) {
				componentEditor.getEditingDomain().getCommandStack().execute(cmd);
			}
		}
	}

	@Override
	protected int getItemCount() {
		if (viewer.getContentProvider() == null || viewer.getInput() == null) {
			return 0;
		}
		return ((ObservableListContentProvider) viewer.getContentProvider()).getElements(viewer.getInput()).length;
	}
}
