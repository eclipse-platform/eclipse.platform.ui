/*******************************************************************************
 * Copyright (c) 2010, 2022 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Dirk Fauth <dirk.fauth@googlemail.com> - Bug 426986
 * Steven Spungin <steven@spungin.tv> - Bug 430660, 430664, Bug 430809, 430717
 * Steven Spungin <steven@spungin.tv> - Ongoing maintenance
 * Olivier Prouvost <olivier.prouvost@opcoach.com> - Bug 412567
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.ContributionURIValidator;
import org.eclipse.e4.tools.emf.ui.common.IContributionClassCreator;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.E4Properties;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.AbstractPickList.PickListFeatures;
import org.eclipse.e4.tools.emf.ui.internal.common.E4PickList;
import org.eclipse.e4.tools.emf.ui.internal.common.E4StringPickList;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.BindingContextSelectionDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.ContributionClassDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.FindImportElementDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.properties.ProjectOSGiTranslationProvider;
import org.eclipse.e4.tools.services.IClipboardService.Handler;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.e4.ui.dialogs.filteredtree.PatternFilter;
import org.eclipse.e4.ui.internal.workbench.E4XMIResource;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.commands.MBindings;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.BasicEMap;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerValueProperty;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

/**
 * This class creates different widgets in editors...
 *
 * As all these widgets are common to a lot of editors. So this class could be
 * moved in AbstractComponentEditor and the code would be really simplified
 * because all parameters in static functions are already defined in
 * AbstractComponentEditor
 */
public class ControlFactory {
	public static final String COPY_HANDLER = ControlFactory.class.getName() + ".COPY_HANDLER"; //$NON-NLS-1$

	public static class TextPasteHandler implements Handler {
		private final Text t;

		public TextPasteHandler(Text t) {
			this.t = t;
		}

		public static void createFor(Text t) {
			t.setData(COPY_HANDLER, new TextPasteHandler(t));
		}

		@Override
		public void paste() {
			final Clipboard cp = new Clipboard(t.getDisplay());
			final Object o = cp.getContents(TextTransfer.getInstance());
			cp.dispose();
			if (o == null) {
				return;
			}

			if (validate(o.toString())) {
				t.paste();
			}
		}

		@Override
		public void cut() {
			t.cut();
		}

		@Override
		public void copy() {
			t.copy();
		}

		public boolean validate(String text) {
			return true;
		}
	}

	public static <M> void createXMIId(Composite parent, AbstractComponentEditor<M> editor) {
		final Label l = new Label(parent, SWT.NONE);
		l.setText(Messages.ModelTooling_XMIID);
		l.setLayoutData(new GridData());

		final Text t = new Text(parent, SWT.BORDER);
		t.setEditable(false);
		final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		t.setLayoutData(gd);

		TextPasteHandler.createFor(t);

		editor.getMaster().addValueChangeListener(event -> {
			final M val = event.diff.getNewValue();
			if (val != null && val instanceof EObject && !t.isDisposed()) {
				final Resource res = ((EObject) val).eResource();
				if (res instanceof E4XMIResource) {
					final String v = ((E4XMIResource) res).getID((EObject) val);
					if (v != null && v.trim().length() > 0) {
						t.setText(v);
					}
				}
			}
		});
	}

	public static Composite createMapProperties(Composite parent, final Messages Messages,
			final AbstractComponentEditor<?> editor, String label, final EStructuralFeature feature, int vIndent) {
		return createMapProperties(parent, Messages, editor, label, null, feature, vIndent);
	}

	public static Composite createMapProperties(Composite parent, final Messages messages,
			final AbstractComponentEditor<?> editor, String label, String tooltip, final EStructuralFeature feature,
			int vIndent) {

		final E4PickList pickList = new E4PickList(parent, SWT.NONE, Arrays.asList(PickListFeatures.NO_PICKER),
				editor, feature) {
			@Override
			protected List<?> getContainerChildren(Object master) {
				return null;
			}

			@Override
			protected void addPressed() {
				final Dialog dialog = new Dialog(getShell()) {
					private Text key;
					private Text value;

					@Override
					protected Control createDialogArea(Composite parent) {
						getShell().setText(messages.ControlFactory_KeyValueShellTitle);
						final Composite comp = (Composite) super.createDialogArea(parent);
						final Composite container = new Composite(comp, SWT.NONE);
						container.setLayout(new GridLayout(2, false));
						container.setLayoutData(new GridData(GridData.FILL_BOTH));

						Label l = new Label(container, SWT.NONE);
						l.setText(messages.ControlFactory_Key);

						key = new Text(container, SWT.BORDER);
						key.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

						l = new Label(container, SWT.NONE);
						l.setText(messages.ControlFactory_Value);

						value = new Text(container, SWT.BORDER);
						value.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

						return comp;
					}

					@Override
					protected void okPressed() {
						if (key.getText().trim().length() > 0) {
							final BasicEMap.Entry<String, String> entry = (org.eclipse.emf.common.util.BasicEMap.Entry<String, String>) ApplicationFactoryImpl.eINSTANCE
									.createStringToStringMap();
							entry.setHash(key.hashCode());
							entry.setKey(key.getText());
							entry.setValue(value.getText().trim().length() > 0 ? value.getText() : null);
							final Command cmd = AddCommand.create(editor.getEditingDomain(), editor.getMaster()
									.getValue(), feature, entry);
							if (cmd.canExecute()) {
								editor.getEditingDomain().getCommandStack().execute(cmd);
								super.okPressed();
							}
						}
					}
				};
				dialog.open();
			}
		};
		pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		pickList.setText(label);
		if (tooltip != null) {
			pickList.setToolTipText(tooltip);
		}

		final TableViewer tableviewer = pickList.getList();
		tableviewer.getTable().setHeaderVisible(true);
		tableviewer.setContentProvider(new ObservableListContentProvider<>());

		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.verticalIndent = vIndent;
		tableviewer.getControl().setLayoutData(gd);

		TableViewerColumn column = new TableViewerColumn(tableviewer, SWT.NONE);
		column.getColumn().setText(Messages.ControlFactory_KeyColumn);
		column.getColumn().setWidth(200);
		column.setLabelProvider(new ColumnLabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public String getText(Object element) {
				final Entry<String, String> entry = (Entry<String, String>) element;
				return entry.getKey();
			}
		});

		final TextCellEditor keyEditor = new TextCellEditor(tableviewer.getTable());
		column.setEditingSupport(new EditingSupport(tableviewer) {

			@Override
			protected void setValue(Object element, Object value) {
				final Command cmd = SetCommand.create(editor.getEditingDomain(), element,
						ApplicationPackageImpl.Literals.STRING_TO_STRING_MAP__KEY,
						value.toString().trim().length() == 0 ? null : value.toString());
				if (cmd.canExecute()) {
					editor.getEditingDomain().getCommandStack().execute(cmd);
					tableviewer.refresh();
					tableviewer.getTable().getColumn(0).pack();
				}
			}

			@Override
			protected Object getValue(Object element) {
				@SuppressWarnings("unchecked")
				final Entry<String, String> entry = (Entry<String, String>) element;
				return entry.getKey();
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return keyEditor;
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
		});

		// FIXME How can we react upon changes in the Map-Value?
		column = new TableViewerColumn(tableviewer, SWT.NONE);
		column.getColumn().setText(Messages.ControlFactory_ValueColumn);
		column.getColumn().setWidth(200);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				@SuppressWarnings("unchecked")
				final Entry<String, String> entry = (Entry<String, String>) element;
				return entry.getValue();
			}
		});

		final TextCellEditor cellEditor = new TextCellEditor(tableviewer.getTable());
		column.setEditingSupport(new EditingSupport(tableviewer) {

			@Override
			protected void setValue(Object element, Object value) {
				final Command cmd = SetCommand.create(editor.getEditingDomain(), element,
						ApplicationPackageImpl.Literals.STRING_TO_STRING_MAP__VALUE,
						value.toString().trim().length() == 0 ? null : value.toString());
				if (cmd.canExecute()) {
					editor.getEditingDomain().getCommandStack().execute(cmd);
					tableviewer.refresh();
				}
			}

			@SuppressWarnings("unchecked")
			@Override
			protected Object getValue(Object element) {
				final Entry<String, String> entry = (Entry<String, String>) element;
				return entry.getValue() == null ? "" : entry.getValue(); //$NON-NLS-1$
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return cellEditor;
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
		});

		@SuppressWarnings("unchecked")
		final IListProperty<Object, Object> prop = EMFEditProperties.list(editor.getEditingDomain(), feature);
		final IObservableList<Object> observableList = prop.observeDetail(editor.getMaster());
		tableviewer.setInput(observableList);
		observableList.addListChangeListener(event -> tableviewer.getTable().getColumn(0).pack());

		return pickList;
	}

	public static <M> void createTextField(Composite parent, String label, IObservableValue<M> master,
			EMFDataBindingContext context, IWidgetValueProperty<Text, String> textProp,
			IValueProperty<? super M, String> modelProp) {
		createTextField(parent, label, null, master, context, textProp, modelProp, null);
	}

	public static <M> void createTextField(Composite parent, String label, String tooltip, IObservableValue<M> master,
			EMFDataBindingContext context, IWidgetValueProperty<Text, String> textProp,
			IValueProperty<? super M, String> modelProp) {
		createTextField(parent, label, tooltip, master, context, textProp, modelProp, null);
	}

	public static <M> void createTextField(Composite parent, String label, IObservableValue<M> master,
			EMFDataBindingContext context, IWidgetValueProperty<Text, String> textProp,
			IValueProperty<? super M, String> modelProp,
			final String warningText) {
		createTextField(parent, label, null, master, context, textProp, modelProp, warningText);
	}

	/**
	 *
	 * @param warningText
	 *            Non null warningText means that a warning with this non-null text
	 *            will be shown when the field is left empty
	 */
	public static <M> void createTextField(Composite parent, String label, String tooltip, IObservableValue<M> master,
			EMFDataBindingContext context, IWidgetValueProperty<Text, String> textProp,
			IValueProperty<? super M, String> modelProp,
			final String warningText) {
		createTextField(parent, label, tooltip, master, context, textProp, modelProp, warningText,
				FieldDecorationRegistry.DEC_WARNING);
	}

	/**
	 *
	 * @param decorationText
	 *            Non null decorationText means that a message with this non-null
	 *            text will be shown when the field is left empty
	 * @param decorationType
	 *            Non null decorationType describes the type of the decoration.
	 *            Supported types: FieldDecorationRegistry.DEC_ERROR,
	 *            FieldDecorationRegistry.DEC_WARNING,
	 *            FieldDecorationRegistry.DEC_INFORMATION
	 */
	public static <M> void createTextField(Composite parent, String label, String tooltip, IObservableValue<M> master,
			EMFDataBindingContext context, IWidgetValueProperty<Text, String> textProp,
			IValueProperty<? super M, String> modelProp,
			final String decorationText, final String decorationType) {
		final Label l = new Label(parent, SWT.NONE);
		l.setText(label);
		if (tooltip != null) {
			l.setToolTipText(tooltip);
		}
		l.setLayoutData(new GridData());

		final Text t = new Text(parent, SWT.BORDER);
		final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		t.setLayoutData(gd);
		TextPasteHandler.createFor(t);
		if (decorationText != null) {
			final ControlDecoration controlDecoration = new ControlDecoration(t, SWT.LEFT | SWT.TOP);
			controlDecoration.setDescriptionText(decorationText);
			final FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
					decorationType);
			controlDecoration.setImage(fieldDecoration.getImage());
			final IValidator<Object> iv = value -> {
				if (value == null) {
					controlDecoration.show();
					return ValidationStatus.warning(decorationText);
				}
				if (value instanceof String) {
					final String text = (String) value;
					if (text.trim().length() == 0) {
						controlDecoration.show();
						return getValidationStatus(decorationType, decorationText);
					}

					controlDecoration.hide();
					return Status.OK_STATUS;
				}

				controlDecoration.hide();
				return Status.OK_STATUS;
			};
			final UpdateValueStrategy<String, String> acv = new UpdateValueStrategy<String, String>().setAfterConvertValidator(iv);
			context.bindValue(textProp.observeDelayed(200, t), modelProp.observeDetail(master), acv, acv);
		} else {
			context.bindValue(textProp.observeDelayed(200, t), modelProp.observeDetail(master));
		}
	}

	public static <M> void createTranslatedTextField(Composite parent, String label, IObservableValue<M> master,
			EMFDataBindingContext context, IWidgetValueProperty<? super Text, String> textProp,
			IValueProperty<? super M, String> modelProp,
			IResourcePool resourcePool, IProject project) {
		createTranslatedTextField(parent, label, null, master, context, textProp, modelProp, resourcePool, project);
	}

	public static <M> void createTranslatedTextField(Composite parent, String label, String tooltip,
			IObservableValue<M> master, EMFDataBindingContext context,
			IWidgetValueProperty<? super Text, String> textProp,
			IValueProperty<? super M, String> modelProp, IResourcePool resourcePool, IProject project) {
		final Label l = new Label(parent, SWT.NONE);
		l.setText(label);
		if (tooltip != null) {
			l.setToolTipText(tooltip);
		}
		l.setLayoutData(new GridData());

		final Text t = new Text(parent, SWT.BORDER);
		final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		if (project == null) {
			gd.horizontalSpan = 2;
		} else {
			gd.horizontalSpan = 2;
		}

		t.setLayoutData(gd);

		TextPasteHandler.createFor(t);
		context.bindValue(textProp.observeDelayed(200, t), modelProp.observeDetail(master));

		// if (project != null) {
		// Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
		// b.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Obj16_world_edit));
		// }
	}

	private static IStatus getValidationStatus(String decorationType, String decorationText) {
		switch (decorationType) {
		case FieldDecorationRegistry.DEC_ERROR:
			return ValidationStatus.error(decorationText);
		case FieldDecorationRegistry.DEC_WARNING:
			return ValidationStatus.warning(decorationText);
		case FieldDecorationRegistry.DEC_INFORMATION:
			return ValidationStatus.info(decorationText);

		default:
			break;
		}

		return ValidationStatus.warning(decorationText);
	}


	public static void createFindImport(Composite parent, final Messages Messages,
			final AbstractComponentEditor<? extends MApplicationElement> editor, EMFDataBindingContext context) {
		final IWidgetValueProperty<Text, String> textProp = WidgetProperties.text(SWT.Modify);

		final Label l = new Label(parent, SWT.NONE);
		l.setText(Messages.ModelTooling_Common_RefId);
		l.setLayoutData(new GridData());

		final Text t = new Text(parent, SWT.BORDER);
		final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		t.setLayoutData(gd);

		TextPasteHandler.createFor(t);

		context.bindValue(
				textProp.observeDelayed(200, t),
				E4Properties.elementId(editor.getEditingDomain()).observeDetail(editor.getMaster()));

		Button b = ControlFactory.createFindButton(parent, editor.resourcePool);
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final FindImportElementDialog dialog = new FindImportElementDialog(b.getShell(), editor,
						(EObject) editor.getMaster().getValue(), Messages);
				dialog.open();
			}
		});
	}

	public static <E extends MUIElement, M extends MElementContainer<E>> void createSelectedElement(
			Composite parent, final AbstractComponentEditor<M> editor, final EMFDataBindingContext context,
			String label) {
		final Label l = new Label(parent, SWT.NONE);
		l.setText(label);
		l.setLayoutData(new GridData());

		final ComboViewer viewer = new ComboViewer(parent);
		final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		viewer.getControl().setLayoutData(gd);
		final IViewerValueProperty<ComboViewer, E> vProp = ViewerProperties.singleSelection();

		final Binding[] binding = new Binding[1];
		final IObservableValue<E> uiObs = vProp.observe(viewer);
		final IObservableValue<E> mObs = E4Properties.<E>selectedElement(editor.getEditingDomain())
				.observeDetail(editor.getMaster());
		editor.getMaster().addValueChangeListener(event -> {
			if (binding[0] != null) {
				binding[0].dispose();
			}
		});

		final IObservableList<E> list = E4Properties.<E>children(editor.getEditingDomain())
				.observeDetail(editor.getMaster());
		final ObservableListContentProvider<E> cp = new ObservableListContentProvider<>();
		viewer.setContentProvider(cp);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final IObservableMap<?, ?>[] attributeMaps = {
				// Cast, because MUILabel is not part of E's type
				((IValueProperty) E4Properties.label(editor.getEditingDomain())).observeDetail(cp.getKnownElements()),
				E4Properties.elementId(editor.getEditingDomain()).observeDetail(cp.getKnownElements()) };
		viewer.setLabelProvider(new ObservableMapLabelProvider(attributeMaps) {
			@Override
			public String getText(Object element) {
				final EObject o = (EObject) element;
				final String rv = o.eClass().getName();

				if (element instanceof MUILabel) {
					final MUILabel label = (MUILabel) element;
					if (!Util.isNullOrEmpty(label.getLabel())) {
						return rv + " - " + label.getLabel().trim(); //$NON-NLS-1$
					}

				}

				if (element instanceof MApplicationElement) {
					final MApplicationElement appEl = (MApplicationElement) element;
					if (!Util.isNullOrEmpty(appEl.getElementId())) {
						return rv + " - " + appEl.getElementId(); //$NON-NLS-1$
					}
				}

				return rv + "[" + list.indexOf(element) + "]"; //$NON-NLS-1$//$NON-NLS-2$
			}
		});
		viewer.setInput(list);

		editor.getMaster().addValueChangeListener(event -> binding[0] = context.bindValue(uiObs, mObs));
	}

	public static void createBindingContextWiget(Composite parent, final Messages Messages,
			final AbstractComponentEditor<? extends MBindings> editor, String label) {
		createBindingContextWiget(parent, Messages, editor, label, null);
	}

	public static void createBindingContextWiget(Composite parent, final Messages Messages,
			final AbstractComponentEditor<? extends MBindings> editor, String label, String tooltip) {
		{
			final E4PickList pickList = new E4PickList(parent, SWT.NONE, Arrays.asList(PickListFeatures.NO_ORDER,
					PickListFeatures.NO_PICKER), editor, CommandsPackageImpl.Literals.BINDINGS__BINDING_CONTEXTS) {
				@Override
				protected void addPressed() {
					final BindingContextSelectionDialog dialog = new BindingContextSelectionDialog(getShell(), editor
							.getEditor().getModelProvider(), Messages);
					if (dialog.open() == IDialogConstants.OK_ID) {
						final Command cmd = AddCommand.create(editor.getEditingDomain(), editor.getMaster().getValue(),
								CommandsPackageImpl.Literals.BINDINGS__BINDING_CONTEXTS, dialog.getSelectedContext());
						if (cmd.canExecute()) {
							editor.getEditingDomain().getCommandStack().execute(cmd);
						}
					}
				}
			};
			pickList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));

			pickList.setText(label);
			pickList.getList().setInput(E4Properties.contexts().observeDetail(editor.getMaster()));
		}
	}

	public static Composite createStringListWidget(Composite parent, Messages Messages,
			final AbstractComponentEditor<? extends MApplicationElement> editor, String label,
			final EStructuralFeature feature, int vIndent) {
		return createStringListWidget(parent, Messages, editor, label, null, feature, vIndent);
	}

	public static Composite createStringListWidget(Composite parent, Messages Messages,
			final AbstractComponentEditor<? extends MApplicationElement> editor, String label, String tooltip,
			final EStructuralFeature feature,
			int vIndent) {

		final E4StringPickList pickList = new E4StringPickList(parent, SWT.NONE, null, editor, feature) {
			@Override
			protected void addPressed() {
				handleAddText(editor, feature, getTextWidget());
			}
		};

		pickList.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		pickList.setText(label);
		if (tooltip != null) {
			pickList.setToolTipText(tooltip);
		}

		final Text t = pickList.getTextWidget();
		t.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.LF) {
					handleAddText(editor, feature, t);
				}
			}
		});

		TextPasteHandler.createFor(t);

		final TableViewer viewer = pickList.getList();
		viewer.setLabelProvider(new LabelProvider());
		final GridData gd = (GridData) viewer.getTable().getLayoutData();
		gd.heightHint = 150;

		@SuppressWarnings("unchecked")
		final IListProperty<Object, Object> prop = EMFProperties.list(feature);
		viewer.setInput(prop.observeDetail(editor.getMaster()));

		viewer.addSelectionChangedListener(event -> {
			final String strSelected = (String) ((StructuredSelection) event.getSelection()).getFirstElement();
			t.setText(strSelected != null ? strSelected : ""); //$NON-NLS-1$
		});


		return pickList;
	}

	private static void handleAddText(AbstractComponentEditor<? extends MApplicationElement> editor,
			EStructuralFeature feature, Text tagText) {
		if (tagText.getText().trim().length() > 0) {
			final String[] tags = tagText.getText().split(";"); //$NON-NLS-1$
			for (int i = 0; i < tags.length; i++) {
				tags[i] = tags[i].trim();
			}

			final MApplicationElement appEl = editor.getMaster().getValue();
			final Command cmd = AddCommand.create(editor.getEditingDomain(), appEl, feature, Arrays.asList(tags));
			if (cmd.canExecute()) {
				editor.getEditingDomain().getCommandStack().execute(cmd);
			}
			tagText.setText(""); //$NON-NLS-1$
		}
	}

	// This method is left in for reference purposes
	@SuppressWarnings("unused")
	private static void handleReplaceText(AbstractComponentEditor<? extends MApplicationElement> editor,
			EStructuralFeature feature, Text tagText, TableViewer viewer) {
		if (tagText.getText().trim().length() > 0) {
			if (!viewer.getSelection().isEmpty()) {
				final String[] tags = tagText.getText().split(";"); //$NON-NLS-1$
				for (int i = 0; i < tags.length; i++) {
					tags[i] = tags[i].trim();
				}

				final MApplicationElement appEl = editor.getMaster().getValue();
				final EObject el = (EObject) editor.getMaster().getValue();
				final List<?> ids = ((IStructuredSelection) viewer.getSelection()).toList();
				final Object curVal = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				final EObject container = (EObject) editor.getMaster().getValue();
				final List<?> l = (List<?>) container.eGet(feature);
				final int idx = l.indexOf(curVal);
				if (idx >= 0) {
					final Command cmdRemove = RemoveCommand.create(editor.getEditingDomain(), el, feature, ids);
					final Command cmdInsert = AddCommand.create(editor.getEditingDomain(), appEl, feature,
							Arrays.asList(tags), idx);
					if (cmdRemove.canExecute() && cmdInsert.canExecute()) {
						editor.getEditingDomain().getCommandStack().execute(cmdRemove);
						editor.getEditingDomain().getCommandStack().execute(cmdInsert);
					}
					tagText.setText(""); //$NON-NLS-1$
				}
			}
		}
	}

	public static <M> void createCheckBox(Composite parent, String label, IObservableValue<M> master,
			EMFDataBindingContext context, IWidgetValueProperty<Button, Boolean> selectionProp,
			IValueProperty<? super M, Boolean> modelProp) {
		createCheckBox(parent, label, null, master, context, selectionProp, modelProp);
	}

	public static <M> void createCheckBox(Composite parent, String label, String tooltip, IObservableValue<M> master,
			EMFDataBindingContext context, IWidgetValueProperty<Button, Boolean> selectionProp,
			IValueProperty<? super M, Boolean> modelProp) {
		final Button checkBox = new Button(parent, SWT.CHECK);
		checkBox.setText(label);
		if (tooltip != null) {
			checkBox.setToolTipText(tooltip);
		}
		checkBox.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 3, 1));
		context.bindValue(selectionProp.observe(checkBox), modelProp.observeDetail(master));
	}

	public static String getLocalizedLabel(ProjectOSGiTranslationProvider translationProvider, MUILabel element) {
		return getLocalizedValue(translationProvider, (MApplicationElement) element,
				UiPackageImpl.Literals.UI_LABEL__LABEL, UiPackageImpl.Literals.UI_LABEL__LOCALIZED_LABEL);
	}

	public static String getLocalizedValue(ProjectOSGiTranslationProvider translationProvider,
			MApplicationElement element, EStructuralFeature feature, EStructuralFeature localizedFeature) {
		final EObject eo = (EObject) element;
		if (translationProvider == null) {
			final String value = (String) eo.eGet(localizedFeature);
			if (value != null && value.trim().length() > 0) {
				return value;
			}
		}

		final String value = (String) eo.eGet(feature);
		if (value != null && value.trim().length() > 0) {
			return tr(translationProvider, value);
		}
		return null;

	}

	public static String tr(ProjectOSGiTranslationProvider translationProvider, String label) {
		if (label.startsWith("%") && translationProvider != null) { //$NON-NLS-1$
			final String key = label.substring(1);
			final String translation = translationProvider.translate(key);
			return translation == key ? label : translation;
		}
		return label;
	}

	public static void attachFiltering(Text searchText, final TableViewer viewer, final PatternFilter filter) {
		searchText.addModifyListener(e -> {
			filter.setPattern(((Text) e.widget).getText());
			viewer.refresh();
			if (viewer.getTable().getItemCount() > 0) {
				final Object data = viewer.getTable().getItem(0).getData();
				viewer.setSelection(new StructuredSelection(data));
			}
		});
		searchText.addTraverseListener(e -> {
			if (e.keyCode == SWT.ARROW_DOWN && viewer.getTable().getItemCount() > 0) {
				viewer.getControl().setFocus();
			}
		});
	}

	/**
	 * This method create a ClassURI field containing : a link to the class, a text
	 * field (to set the value bundleclass://....) and a button find to find the
	 * object. It must be called by all editors that need to get a class URI It adds
	 * a validator to check if the value exists (OK), or if it is null or empty
	 * (WARNING), or if the class does not exists (ERROR)
	 *
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id= 412567">Bug
	 *      412567 </a> *
	 * @param parent
	 *            the parent composite
	 * @param Messages
	 *            The messages container class
	 * @param editor
	 *            the current abstractComponentEditor containing a classURI field
	 * @param title
	 *            the title of the field
	 * @param feature
	 *            the associated Feature in the model
	 * @param c
	 *            the Contribution Class creator to use to create the URI class
	 * @param project
	 *            current project where application model stands
	 * @param context
	 *            the EMFBinding context
	 * @param eclipseContext
	 *            the Eclipse context
	 * @param adapter
	 *            the selection adapter to use. For objects extending MContribution
	 *            there is a default adapter. But for MPartDescriptors, which is not
	 *            a MContribution, a specific adapter must be set (@see
	 *            MPartDescriptorEditor)
	 */
	public static void createClassURIField(Composite parent, final Messages Messages,
			final AbstractComponentEditor<?> editor, String title, final EAttribute feature,
			IContributionClassCreator c,
			IProject project,
			EMFDataBindingContext context, IEclipseContext eclipseContext, SelectionAdapter adapter) {
		final Link lnk;
		final IWidgetValueProperty<Text, String> textProp = WidgetProperties.text(SWT.Modify);

		if (project != null && c != null) {
			lnk = new Link(parent, SWT.NONE);
			lnk.setText("<A>" + title + "</A>"); //$NON-NLS-1$//$NON-NLS-2$
			lnk.setLayoutData(new GridData());
			lnk.addSelectionListener(adapter);
		} else {
			lnk = null;
			final Label l = new Label(parent, SWT.NONE);
			l.setText(title);
			l.setLayoutData(new GridData());
		}

		final Text t = new Text(parent, SWT.BORDER);
		TextPasteHandler.createFor(t);
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		t.addModifyListener(e -> {
			if (lnk != null) {
				lnk.setToolTipText(((Text) e.getSource()).getText());
			}
		});
		@SuppressWarnings("unchecked")
		final Binding binding = context.bindValue(textProp.observeDelayed(200, t),
				((IValueProperty<Object, Object>) EMFEditProperties.value(editor.getEditingDomain(), feature))
				.observeDetail(editor.getMaster()),
				new UpdateValueStrategy<>().setAfterConvertValidator(new ContributionURIValidator()),
				new UpdateValueStrategy<>());
		Util.addDecoration(t, binding);

		Button b = ControlFactory.createFindButton(parent, editor.resourcePool);
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final ContributionClassDialog dialog = new ContributionClassDialog(b.getShell(), eclipseContext,
						editor.getEditingDomain(), (MApplicationElement) editor.getMaster().getValue(),
						feature, Messages);
				dialog.open();
			}
		});
	}

	public static void createClassURIField(Composite parent, final Messages Messages,
			final AbstractComponentEditor<?> editor, String title, final EAttribute feature,
			IContributionClassCreator c, IProject project, EMFDataBindingContext context,
			IEclipseContext eclipseContext) {
		createClassURIField(parent, Messages, editor, title, feature, c, project, context, eclipseContext,
				new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				c.createOpen((MContribution) editor.getMaster().getValue(), editor.getEditingDomain(), project,
						parent.getShell());
			}
		});

	}

	public static Button createFindButton(Composite parent, IResourcePool resourcePool) {
		Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.ModelTooling_Common_FindEllipsis);
		b.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Obj16_zoom));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
		return b;
	}

}