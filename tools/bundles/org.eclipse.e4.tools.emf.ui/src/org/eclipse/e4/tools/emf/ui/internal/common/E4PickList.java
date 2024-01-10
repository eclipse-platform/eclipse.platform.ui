/*******************************************************************************
 * Copyright (c) 2014, 2023 TwelveTone LLC and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Steven Spungin <steven@spungin.tv> - initial API and implementation
 * Simon Scholz <simon.scholz@vogella.com> - Bug 475365
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common;

import java.util.List;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.E4Properties;
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
import org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellNavigationStrategy;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * <p>
 * Pressing CR in the combo will execute the ADD command.
 * </p>
 * <p>
 * <em>E4 specific:</em> The default feature is UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN. Other features must
 * override appropriate methods. The picker uses an Array Content Provider. The list uses an
 * ObservableListContentProvider and a Struct class to contain the object and label.
 * </p>
 *
 * @author Steven Spungin
 */
public class E4PickList extends AbstractPickList {

	AbstractComponentEditor<?> componentEditor;
	EStructuralFeature feature;
	TableViewerFocusCellManager focusCellMgr;

	public static class Struct {
		public final String label;
		public final EClass eClass;
		public final boolean separator;

		public Struct(String label, EClass eClass, boolean separator) {
			this.label = label;
			this.eClass = eClass;
			this.separator = separator;
		}
	}

	@Deprecated
	public E4PickList(Composite parent, int style, List<PickListFeatures> listFeatures, Messages messages,
			AbstractComponentEditor<?> componentEditor, final EStructuralFeature feature) {
		this(parent, style, listFeatures, componentEditor, feature);
	}

	public E4PickList(Composite parent, int style, List<PickListFeatures> listFeatures,
			final AbstractComponentEditor<?> componentEditor, final EStructuralFeature feature) {
		super(parent, style, listFeatures, componentEditor);

		this.componentEditor = componentEditor;
		this.feature = feature;

		picker.setContentProvider(ArrayContentProvider.getInstance());
		picker.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				final Struct struct = (Struct) element;
				return struct.label;
			}
		});

		final ObservableListContentProvider<?> cp = new ObservableListContentProvider<>();
		viewer.setContentProvider(cp);

		final FontDescriptor italicFontDescriptor = FontDescriptor.createFrom(viewer.getControl().getFont())
				.setStyle(SWT.ITALIC);
		DelegatingStyledCellLabelProvider labelprovider = new DelegatingStyledCellLabelProvider(
				new ComponentLabelProvider(componentEditor.getEditor(), new Messages(), italicFontDescriptor));

		@SuppressWarnings({ "unchecked", "rawtypes" })
		// Cast, because MUILabel is not part of E's type
		final IObservableMap<?, ?> attributeMap = ((IValueProperty) E4Properties
				.label(componentEditor.getEditingDomain())).observeDetail(cp.getKnownElements());

		ObservableMapCellLabelProvider observableLabelProvider = new ObservableMapCellLabelProvider(attributeMap) {
			@Override
			public void update(ViewerCell cell) {
				labelprovider.update(cell);
			}
		};

		viewer.setLabelProvider(observableLabelProvider);

		viewer.addOpenListener(event -> {
			if (event.getSelection() instanceof IStructuredSelection selection) {
				ModelEditor editor = componentEditor.getEditor();
				if (selection.getFirstElement() instanceof EObject selected && editor != null) {
					editor.gotoEObject(ModelEditor.TAB_FORM, selected);
				}
			}
		});

		// enable tabbing and keyboard activation
		this.focusCellMgr = new TableViewerFocusCellManager(viewer, new FocusCellOwnerDrawHighlighter(viewer),
				new CellNavigationStrategy());

		TableViewerEditor.create(viewer, this.focusCellMgr, new TableViewerEditorActivationStrategy(viewer), //
				ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
				| ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);
	}

	/**
	 * Used for StringToString maps
	 */
	protected void tryEObjectMove(Object obj, EObject container, int delta) {
		final List<?> l = (List<?>) container.eGet(feature);
		final int idx = l.indexOf(obj) + delta;
		if (delta > 0 && idx < l.size() || delta < 0 && idx >= 0) {
			final Command cmd = MoveCommand.create(componentEditor.getEditingDomain(), componentEditor.getMaster()
					.getValue(), feature, obj, idx);

			if (cmd.canExecute()) {
				componentEditor.getEditingDomain().getCommandStack().execute(cmd);
				viewer.setSelection(new StructuredSelection(obj));
			}
		}
	}

	@Override
	protected void addPressed() {
		if (!picker.getSelection().isEmpty()) {
			final Struct struct = (Struct) picker.getStructuredSelection().getFirstElement();
			final EClass eClass = struct.eClass;
			_handleAdd(eClass, struct.separator);
		}
	}

	protected void _handleAdd(EClass eClass, boolean separator) {
		final MMenuElement eObject = (MMenuElement) EcoreUtil.create(eClass);
		setElementId(eObject);
		final Command cmd = AddCommand.create(componentEditor.getEditingDomain(), componentEditor.getMaster()
				.getValue(), feature, eObject);

		if (cmd.canExecute()) {
			componentEditor.getEditingDomain().getCommandStack().execute(cmd);
			if (!separator) {
				componentEditor.getEditor().setSelection(eObject);
			}
		}
	}

	protected void setElementId(Object element) {
		if (componentEditor.getEditor().isAutoCreateElementId() && element instanceof MApplicationElement el) {
			if (el.getElementId() == null || el.getElementId().trim().length() == 0) {
				el.setElementId(Util.getDefaultElementId(
						((EObject) componentEditor.getMaster().getValue()).eResource(), el, componentEditor.getEditor()
						.getProject()));
			}
		}
	}

	protected List<?> getContainerChildren(Object master) {
		if (master instanceof MElementContainer<?>) {
			return ((MElementContainer<?>) master).getChildren();
		}
		return null;
	}

	@Override
	protected void moveUpPressed() {
		if (!viewer.getSelection().isEmpty()) {
			final IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
			if (s.size() == 1) {
				final Object obj = s.getFirstElement();
				final Object container = componentEditor.getMaster().getValue();
				if (feature == ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE) {
					tryEObjectMove(obj, (EObject) container, -1);
					return;
				}
				final List<?> children = getContainerChildren(container);
				if (children == null) {
					return;
				}
				final int idx = children.indexOf(obj) - 1;
				if (idx >= 0) {
					if (obj instanceof MUIElement && feature == UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN) {
						if (Util.moveElementByIndex(componentEditor.getEditingDomain(), (MUIElement) obj,
								componentEditor.getEditor().isLiveModel(), idx)) {
							viewer.setSelection(new StructuredSelection(obj));
						}
					} else if (obj instanceof MApplicationElement
							|| obj instanceof org.eclipse.emf.ecore.impl.MinimalEObjectImpl.Container) {
						final Command cmd = MoveCommand.create(componentEditor.getEditingDomain(), componentEditor
								.getMaster().getValue(), feature, obj, idx);
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
			final IStructuredSelection s = viewer.getStructuredSelection();
			if (s.size() == 1) {
				final Object obj = s.getFirstElement();
				final Object container = componentEditor.getMaster().getValue();
				if (feature == ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE) {
					tryEObjectMove(obj, (EObject) container, 1);
					return;
				}
				final List<?> children = getContainerChildren(container);
				if (children == null) {
					return;
				}
				final int idx = children.indexOf(obj) + 1;
				if (idx < children.size()) {
					if (obj instanceof MUIElement && feature == UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN) {
						if (Util.moveElementByIndex(componentEditor.getEditingDomain(), (MUIElement) obj,
								componentEditor.getEditor().isLiveModel(), idx)) {
							viewer.setSelection(new StructuredSelection(obj));
						}
					} else if (obj instanceof MApplicationElement
							|| obj instanceof org.eclipse.emf.ecore.impl.MinimalEObjectImpl.Container) {
						final Command cmd = MoveCommand.create(componentEditor.getEditingDomain(), componentEditor
								.getMaster().getValue(), feature, obj, idx);
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
			final List<?> keybinding = viewer.getStructuredSelection().toList();
			final Command cmd = RemoveCommand.create(componentEditor.getEditingDomain(), componentEditor.getMaster()
					.getValue(), feature, keybinding);
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
		return ((ObservableListContentProvider<?>) viewer.getContentProvider()).getElements(viewer.getInput()).length;
	}

	private static class TableViewerEditorActivationStrategy extends ColumnViewerEditorActivationStrategy {

		public TableViewerEditorActivationStrategy(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
			if (event != null) {

				if (event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION) {
					MouseEvent mouseEvent = (MouseEvent) event.sourceEvent;
					if ((mouseEvent.stateMask & SWT.MOD1) != 0) {
						return false;
					}
				}

				if (event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION
						&& ((event.stateMask & SWT.MOD1) == 0)
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED
						&& (event.keyCode == SWT.F2 || event.keyCode == 32 || event.character == SWT.CR))) {

					return true;
				}
			}
			return false;
		}
	}

}
