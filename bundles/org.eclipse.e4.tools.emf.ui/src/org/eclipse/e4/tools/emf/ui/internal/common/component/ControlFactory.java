/*******************************************************************************
 * Copyright (c) 2010, 2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 426986
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
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.PatternFilter;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ComponentLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.BindingContextSelectionDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.FindImportElementDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.properties.ProjectOSGiTranslationProvider;
import org.eclipse.e4.tools.services.IClipboardService.Handler;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.e4.ui.internal.workbench.E4XMIResource;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MBindings;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.BasicEMap;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.IEMFListProperty;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.databinding.edit.IEMFEditListProperty;
import org.eclipse.emf.databinding.edit.IEMFEditValueProperty;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.MoveCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerValueProperty;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

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
			Clipboard cp = new Clipboard(t.getDisplay());
			Object o = cp.getContents(TextTransfer.getInstance());
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

	public static void createXMIId(Composite parent, AbstractComponentEditor editor) {
		Label l = new Label(parent, SWT.NONE);
		l.setText("XMI:ID"); //$NON-NLS-1$
		l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		final Text t = new Text(parent, SWT.BORDER);
		t.setEditable(false);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		t.setLayoutData(gd);

		TextPasteHandler.createFor(t);

		editor.getMaster().addValueChangeListener(new IValueChangeListener() {

			@Override
			public void handleValueChange(ValueChangeEvent event) {
				Object val = event.diff.getNewValue();
				if (val != null && val instanceof EObject && !t.isDisposed()) {
					Resource res = ((EObject) val).eResource();
					if (res instanceof E4XMIResource) {
						String v = ((E4XMIResource) res).getID((EObject) val);
						if (v != null && v.trim().length() > 0) {
							t.setText(v);
						}
					}
				}
			}
		});
	}

	public static void createMapProperties(Composite parent, final Messages Messages, final AbstractComponentEditor editor, String label, final EStructuralFeature feature, int vIndent) {
		createMapProperties(parent, Messages, editor, label, null, feature, vIndent);
	}

	public static void createMapProperties(Composite parent, final Messages Messages, final AbstractComponentEditor editor, String label, String tooltip, final EStructuralFeature feature, int vIndent) {
		Label l = new Label(parent, SWT.NONE);
		l.setText(label);
		if (tooltip != null) {
			l.setToolTipText(tooltip);
		}
		GridData gd = new GridData(GridData.END, GridData.BEGINNING, false, false);
		gd.verticalIndent = vIndent;
		l.setLayoutData(gd);

		final TableViewer tableviewer = new TableViewer(parent);
		tableviewer.getTable().setHeaderVisible(true);
		ObservableListContentProvider cp = new ObservableListContentProvider();
		tableviewer.setContentProvider(cp);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 80;
		gd.verticalIndent = vIndent;
		tableviewer.getControl().setLayoutData(gd);

		TableViewerColumn column = new TableViewerColumn(tableviewer, SWT.NONE);
		column.getColumn().setText(Messages.ControlFactory_Key);
		column.getColumn().setWidth(200);
		column.setLabelProvider(new ColumnLabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public String getText(Object element) {
				Entry<String, String> entry = (Entry<String, String>) element;
				return entry.getKey();
			}
		});

		// FIXME How can we react upon changes in the Map-Value?
		column = new TableViewerColumn(tableviewer, SWT.NONE);
		column.getColumn().setText(Messages.ControlFactory_Value);
		column.getColumn().setWidth(200);
		column.setLabelProvider(new ColumnLabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public String getText(Object element) {
				Entry<String, String> entry = (Entry<String, String>) element;
				return entry.getValue();
			}
		});

		final TextCellEditor cellEditor = new TextCellEditor(tableviewer.getTable());
		column.setEditingSupport(new EditingSupport(tableviewer) {

			@Override
			protected void setValue(Object element, Object value) {
				Command cmd = SetCommand.create(editor.getEditingDomain(), element, ApplicationPackageImpl.Literals.STRING_TO_STRING_MAP__VALUE, value.toString().trim().length() == 0 ? null : value.toString());
				if (cmd.canExecute()) {
					editor.getEditingDomain().getCommandStack().execute(cmd);
					tableviewer.refresh();
				}
			}

			@SuppressWarnings("unchecked")
			@Override
			protected Object getValue(Object element) {
				Entry<String, String> entry = (Entry<String, String>) element;
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

		IEMFEditListProperty prop = EMFEditProperties.list(editor.getEditingDomain(), feature);
		tableviewer.setInput(prop.observeDetail(editor.getMaster()));

		final Composite buttonComp = new Composite(parent, SWT.NONE);
		buttonComp.setLayoutData(new GridData(GridData.FILL, GridData.END, false, false));
		GridLayout gl = new GridLayout();
		gl.marginLeft = 0;
		gl.marginRight = 0;
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		buttonComp.setLayout(gl);

		Button b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.ModelTooling_Common_AddEllipsis);
		b.setImage(editor.createImage(ResourceProvider.IMG_Obj16_table_add));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Dialog dialog = new Dialog(buttonComp.getShell()) {
					private Text key;
					private Text value;

					@Override
					protected Control createDialogArea(Composite parent) {
						getShell().setText(Messages.ControlFactory_KeyValueShellTitle);
						Composite comp = (Composite) super.createDialogArea(parent);
						Composite container = new Composite(comp, SWT.NONE);
						container.setLayout(new GridLayout(2, false));
						container.setLayoutData(new GridData(GridData.FILL_BOTH));

						Label l = new Label(container, SWT.NONE);
						l.setText(Messages.ControlFactory_Key);

						key = new Text(container, SWT.BORDER);
						key.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

						l = new Label(container, SWT.NONE);
						l.setText(Messages.ControlFactory_Value);

						value = new Text(container, SWT.BORDER);
						value.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

						return comp;
					}

					@Override
					protected void okPressed() {
						if (key.getText().trim().length() > 0) {
							BasicEMap.Entry<String, String> entry = (org.eclipse.emf.common.util.BasicEMap.Entry<String, String>) ApplicationFactoryImpl.eINSTANCE.createStringToStringMap();
							entry.setHash(key.hashCode());
							entry.setKey(key.getText());
							entry.setValue(value.getText().trim().length() > 0 ? value.getText() : null);
							Command cmd = AddCommand.create(editor.getEditingDomain(), editor.getMaster().getValue(), feature, entry);
							if (cmd.canExecute()) {
								editor.getEditingDomain().getCommandStack().execute(cmd);
								super.okPressed();
							}
						}
					}
				};
				dialog.open();

			}
		});

		b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.ModelTooling_Common_Remove);
		b.setImage(editor.createImage(ResourceProvider.IMG_Obj16_table_delete));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) tableviewer.getSelection();
				if (!selection.isEmpty()) {
					Command cmd = RemoveCommand.create(editor.getEditingDomain(), editor.getMaster().getValue(), feature, selection.toList());
					if (cmd.canExecute()) {
						editor.getEditingDomain().getCommandStack().execute(cmd);
					}
				}
			}
		});
	}

	public static void createTextField(Composite parent, String label, IObservableValue master, EMFDataBindingContext context, IWidgetValueProperty textProp, IEMFEditValueProperty modelProp) {
		createTextField(parent, label, null, master, context, textProp, modelProp, null);
	}

	public static void createTextField(Composite parent, String label, String tooltip, IObservableValue master, EMFDataBindingContext context, IWidgetValueProperty textProp, IEMFEditValueProperty modelProp) {
		createTextField(parent, label, tooltip, master, context, textProp, modelProp, null);
	}

	public static void createTextField(Composite parent, String label, IObservableValue master, EMFDataBindingContext context, IWidgetValueProperty textProp, IEMFEditValueProperty modelProp, final String warningText) {
		createTextField(parent, label, null, master, context, textProp, modelProp, warningText);
	}

	/**
	 *
	 * @param parent
	 * @param label
	 * @param master
	 * @param context
	 * @param textProp
	 * @param modelProp
	 * @param warningText
	 *            Non null warningText means that a warning with this non-null
	 *            text will be shown when the field is left empty
	 */
	public static void createTextField(Composite parent, String label, String tooltip, IObservableValue master, EMFDataBindingContext context, IWidgetValueProperty textProp, IEMFEditValueProperty modelProp, final String warningText) {
		Label l = new Label(parent, SWT.NONE);
		l.setText(label);
		if (tooltip != null) {
			l.setToolTipText(tooltip);
		}
		l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		final Text t = new Text(parent, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		t.setLayoutData(gd);
		TextPasteHandler.createFor(t);
		if (warningText != null) {
			final ControlDecoration controlDecoration = new ControlDecoration(t, SWT.LEFT | SWT.TOP);
			controlDecoration.setDescriptionText(warningText);
			FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_WARNING);
			controlDecoration.setImage(fieldDecoration.getImage());
			IValidator iv = new IValidator() {

				@Override
				public IStatus validate(Object value) {
					if (value == null) {
						controlDecoration.show();
						return ValidationStatus.warning(warningText);
					}
					if (value instanceof String) {
						String text = (String) value;
						if (text.trim().length() == 0) {
							controlDecoration.show();
							return ValidationStatus.warning(warningText);
						}
					}
					controlDecoration.hide();
					return Status.OK_STATUS;
				}
			};
			UpdateValueStrategy acv = new UpdateValueStrategy().setAfterConvertValidator(iv);
			context.bindValue(textProp.observeDelayed(200, t), modelProp.observeDetail(master), acv, acv);
		} else
			context.bindValue(textProp.observeDelayed(200, t), modelProp.observeDetail(master));
	}

	public static void createTranslatedTextField(Composite parent, String label, IObservableValue master, EMFDataBindingContext context, IWidgetValueProperty textProp, IEMFEditValueProperty modelProp, IResourcePool resourcePool, IProject project) {
		createTranslatedTextField(parent, label, null, master, context, textProp, modelProp, resourcePool, project);
	}

	public static void createTranslatedTextField(Composite parent, String label, String tooltip, IObservableValue master, EMFDataBindingContext context, IWidgetValueProperty textProp, IEMFEditValueProperty modelProp, IResourcePool resourcePool, IProject project) {
		Label l = new Label(parent, SWT.NONE);
		l.setText(label);
		if (tooltip != null) {
			l.setToolTipText(tooltip);
		}
		l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		final Text t = new Text(parent, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
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

	public static void createFindImport(Composite parent, final Messages Messages, final AbstractComponentEditor editor, EMFDataBindingContext context) {
		IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		Label l = new Label(parent, SWT.NONE);
		l.setText(Messages.ModelTooling_Common_RefId);
		l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		final Text t = new Text(parent, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		t.setLayoutData(gd);

		TextPasteHandler.createFor(t);

		context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(editor.getEditingDomain(), ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID).observeDetail(editor.getMaster()));

		final Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.ModelTooling_Common_FindEllipsis);
		b.setImage(editor.createImage(ResourceProvider.IMG_Obj16_zoom));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FindImportElementDialog dialog = new FindImportElementDialog(b.getShell(), editor, (EObject) editor.getMaster().getValue(), Messages);
				dialog.open();
			}
		});
	}

	public static void createSelectedElement(Composite parent, final AbstractComponentEditor editor, final EMFDataBindingContext context, String label) {
		Label l = new Label(parent, SWT.NONE);
		l.setText(label);
		l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		ComboViewer viewer = new ComboViewer(parent);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		viewer.getControl().setLayoutData(gd);
		IEMFEditListProperty listProp = EMFEditProperties.list(editor.getEditingDomain(), UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN);
		IEMFEditValueProperty labelProp = EMFEditProperties.value(editor.getEditingDomain(), UiPackageImpl.Literals.UI_LABEL__LABEL);
		IEMFEditValueProperty idProp = EMFEditProperties.value(editor.getEditingDomain(), ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID);

		IViewerValueProperty vProp = ViewerProperties.singleSelection();

		final Binding[] binding = new Binding[1];
		final IObservableValue uiObs = vProp.observe(viewer);
		final IObservableValue mObs = EMFEditProperties.value(editor.getEditingDomain(), UiPackageImpl.Literals.ELEMENT_CONTAINER__SELECTED_ELEMENT).observeDetail(editor.getMaster());
		editor.getMaster().addValueChangeListener(new IValueChangeListener() {

			@Override
			public void handleValueChange(ValueChangeEvent event) {
				if (binding[0] != null) {
					binding[0].dispose();
				}

			}
		});

		final IObservableList list = listProp.observeDetail(editor.getMaster());
		ObservableListContentProvider cp = new ObservableListContentProvider();
		viewer.setContentProvider(cp);
		IObservableMap[] attributeMaps = { labelProp.observeDetail(cp.getKnownElements()), idProp.observeDetail(cp.getKnownElements()) };
		viewer.setLabelProvider(new ObservableMapLabelProvider(attributeMaps) {
			@Override
			public String getText(Object element) {
				EObject o = (EObject) element;
				String rv = o.eClass().getName();

				if (element instanceof MUILabel) {
					MUILabel label = (MUILabel) element;
					if (!Util.isNullOrEmpty(label.getLabel())) {
						return rv + " - " + label.getLabel().trim(); //$NON-NLS-1$
					}

				}

				if (element instanceof MApplicationElement) {
					MApplicationElement appEl = (MApplicationElement) element;
					if (!Util.isNullOrEmpty(appEl.getElementId())) {
						return rv + " - " + appEl.getElementId(); //$NON-NLS-1$
					}
				}

				return rv + "[" + list.indexOf(element) + "]"; //$NON-NLS-1$//$NON-NLS-2$
			}
		});
		viewer.setInput(list);

		editor.getMaster().addValueChangeListener(new IValueChangeListener() {

			@Override
			public void handleValueChange(ValueChangeEvent event) {
				binding[0] = context.bindValue(uiObs, mObs);
			}
		});
	}

	public static void createBindingContextWiget(Composite parent, final Messages Messages, final AbstractComponentEditor editor, String label) {
		createBindingContextWiget(parent, Messages, editor, label, null);
	}

	public static void createBindingContextWiget(Composite parent, final Messages Messages, final AbstractComponentEditor editor, String label, String tooltip) {
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(label);
			if (tooltip != null) {
				l.setToolTipText(tooltip);
			}
			l.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

			final TableViewer viewer = new TableViewer(parent);
			GridData gd = new GridData(GridData.FILL_BOTH);
			gd.heightHint = 120;
			viewer.getControl().setLayoutData(gd);
			viewer.setContentProvider(new ObservableListContentProvider());
			viewer.setLabelProvider(new ComponentLabelProvider(editor.getEditor(), Messages));
			viewer.setInput(EMFProperties.list(CommandsPackageImpl.Literals.BINDINGS__BINDING_CONTEXTS).observeDetail(editor.getMaster()));

			final Composite buttonComp = new Composite(parent, SWT.NONE);
			buttonComp.setLayoutData(new GridData(GridData.FILL, GridData.END, false, false));
			GridLayout gl = new GridLayout();
			gl.marginLeft = 0;
			gl.marginRight = 0;
			gl.marginWidth = 0;
			gl.marginHeight = 0;
			buttonComp.setLayout(gl);

			Button b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_Up);
			b.setImage(editor.createImage(ResourceProvider.IMG_Obj16_arrow_up));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!viewer.getSelection().isEmpty()) {
						IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
						if (s.size() == 1) {
							Object obj = s.getFirstElement();
							MBindings container = (MBindings) editor.getMaster().getValue();
							int idx = container.getBindingContexts().indexOf(obj) - 1;
							if (idx >= 0) {
								Command cmd = MoveCommand.create(editor.getEditingDomain(), editor.getMaster().getValue(), CommandsPackageImpl.Literals.BINDINGS__BINDING_CONTEXTS, obj, idx);

								if (cmd.canExecute()) {
									editor.getEditingDomain().getCommandStack().execute(cmd);
									viewer.setSelection(new StructuredSelection(obj));
								}
							}

						}
					}
				}
			});

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_Down);
			b.setImage(editor.createImage(ResourceProvider.IMG_Obj16_arrow_down));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!viewer.getSelection().isEmpty()) {
						IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
						if (s.size() == 1) {
							Object obj = s.getFirstElement();
							MBindings container = (MBindings) editor.getMaster().getValue();
							int idx = container.getBindingContexts().indexOf(obj) + 1;
							if (idx < container.getBindingContexts().size()) {
								Command cmd = MoveCommand.create(editor.getEditingDomain(), editor.getMaster().getValue(), CommandsPackageImpl.Literals.BINDINGS__BINDING_CONTEXTS, obj, idx);

								if (cmd.canExecute()) {
									editor.getEditingDomain().getCommandStack().execute(cmd);
									viewer.setSelection(new StructuredSelection(obj));
								}
							}

						}
					}
				}
			});

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_AddEllipsis);
			b.setImage(editor.createImage(ResourceProvider.IMG_Obj16_table_add));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					BindingContextSelectionDialog dialog = new BindingContextSelectionDialog(buttonComp.getShell(), editor.getEditor().getModelProvider(), Messages);
					if (dialog.open() == IDialogConstants.OK_ID) {
						Command cmd = AddCommand.create(editor.getEditingDomain(), editor.getMaster().getValue(), CommandsPackageImpl.Literals.BINDINGS__BINDING_CONTEXTS, dialog.getSelectedContext());
						if (cmd.canExecute()) {
							editor.getEditingDomain().getCommandStack().execute(cmd);
						}
					}
				}
			});

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_Remove);
			b.setImage(editor.createImage(ResourceProvider.IMG_Obj16_table_delete));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Command cmd = RemoveCommand.create(editor.getEditingDomain(), editor.getMaster().getValue(), CommandsPackageImpl.Literals.BINDINGS__BINDING_CONTEXTS, ((IStructuredSelection) viewer.getSelection()).toList());
					if (cmd.canExecute()) {
						editor.getEditingDomain().getCommandStack().execute(cmd);
					}
				}
			});
		}
	}

	public static void createStringListWidget(Composite parent, Messages Messages, final AbstractComponentEditor editor, String label, final EStructuralFeature feature, int vIndent) {
		createStringListWidget(parent, Messages, editor, label, null, feature, vIndent);
	}

	public static void createStringListWidget(Composite parent, Messages Messages, final AbstractComponentEditor editor, String label, String tooltip, final EStructuralFeature feature, int vIndent) {
		Label l = new Label(parent, SWT.NONE);
		l.setText(label);
		if (tooltip != null) {
			l.setToolTipText(tooltip);
		}
		GridData gd = new GridData(GridData.END, GridData.BEGINNING, false, false);
		gd.verticalIndent = vIndent;
		l.setLayoutData(gd);

		final Text t = new Text(parent, SWT.BORDER);
		gd = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
		gd.verticalIndent = vIndent;
		t.setLayoutData(gd);
		t.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.LF) {
					handleAddText(editor, feature, t);
				}
			}
		});

		TextPasteHandler.createFor(t);

		Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.ModelTooling_Common_Add);
		b.setImage(editor.createImage(ResourceProvider.IMG_Obj16_table_add));
		gd = new GridData(GridData.FILL, GridData.CENTER, false, false);
		gd.verticalIndent = vIndent;
		b.setLayoutData(gd);
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleAddText(editor, feature, t);
			}
		});

		new Label(parent, SWT.NONE);

		final TableViewer viewer = new TableViewer(parent);
		viewer.setLabelProvider(new LabelProvider());
		viewer.setContentProvider(new ObservableListContentProvider());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 150;
		viewer.getControl().setLayoutData(gd);

		IEMFListProperty prop = EMFProperties.list(feature);
		viewer.setInput(prop.observeDetail(editor.getMaster()));

		Composite buttonComp = new Composite(parent, SWT.NONE);
		buttonComp.setLayoutData(new GridData(GridData.FILL, GridData.END, false, false));
		GridLayout gl = new GridLayout();
		gl.marginLeft = 0;
		gl.marginRight = 0;
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		buttonComp.setLayout(gl);

		b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.ModelTooling_Common_Up);
		b.setImage(editor.createImage(ResourceProvider.IMG_Obj16_arrow_up));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
					if (s.size() == 1) {
						Object obj = s.getFirstElement();
						EObject container = (EObject) editor.getMaster().getValue();
						List<?> l = (List<?>) container.eGet(feature);
						int idx = l.indexOf(obj) - 1;
						if (idx >= 0) {
							Command cmd = MoveCommand.create(editor.getEditingDomain(), editor.getMaster().getValue(), feature, obj, idx);

							if (cmd.canExecute()) {
								editor.getEditingDomain().getCommandStack().execute(cmd);
								viewer.setSelection(new StructuredSelection(obj));
							}
						}

					}
				}
			}
		});

		b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.ModelTooling_Common_Down);
		b.setImage(editor.createImage(ResourceProvider.IMG_Obj16_arrow_down));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
					if (s.size() == 1) {
						Object obj = s.getFirstElement();
						EObject container = (EObject) editor.getMaster().getValue();
						List<?> l = (List<?>) container.eGet(feature);
						int idx = l.indexOf(obj) + 1;
						if (idx < l.size()) {
							Command cmd = MoveCommand.create(editor.getEditingDomain(), editor.getMaster().getValue(), feature, obj, idx);

							if (cmd.canExecute()) {
								editor.getEditingDomain().getCommandStack().execute(cmd);
								viewer.setSelection(new StructuredSelection(obj));
							}
						}

					}
				}
			}
		});

		b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.ModelTooling_Common_Remove);
		b.setImage(editor.createImage(ResourceProvider.IMG_Obj16_table_delete));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					EObject el = (EObject) editor.getMaster().getValue();
					List<?> ids = ((IStructuredSelection) viewer.getSelection()).toList();
					Command cmd = RemoveCommand.create(editor.getEditingDomain(), el, feature, ids);
					if (cmd.canExecute()) {
						editor.getEditingDomain().getCommandStack().execute(cmd);
						if (ids.size() > 0) {
							viewer.setSelection(new StructuredSelection(ids.get(0)));
						}
					}
				}
			}
		});
	}

	private static void handleAddText(AbstractComponentEditor editor, EStructuralFeature feature, Text tagText) {
		if (tagText.getText().trim().length() > 0) {
			String[] tags = tagText.getText().split(";"); //$NON-NLS-1$
			for (int i = 0; i < tags.length; i++) {
				tags[i] = tags[i].trim();
			}

			MApplicationElement appEl = (MApplicationElement) editor.getMaster().getValue();
			Command cmd = AddCommand.create(editor.getEditingDomain(), appEl, feature, Arrays.asList(tags));
			if (cmd.canExecute()) {
				editor.getEditingDomain().getCommandStack().execute(cmd);
			}
			tagText.setText(""); //$NON-NLS-1$
		}
	}

	public static void createCheckBox(Composite parent, String label, IObservableValue master, EMFDataBindingContext context, IWidgetValueProperty selectionProp, IEMFEditValueProperty modelProp) {
		createCheckBox(parent, label, null, master, context, selectionProp, modelProp);
	}

	public static void createCheckBox(Composite parent, String label, String tooltip, IObservableValue master, EMFDataBindingContext context, IWidgetValueProperty selectionProp, IEMFEditValueProperty modelProp) {
		Label l = new Label(parent, SWT.NONE);
		l.setText(label);
		if (tooltip != null) {
			l.setToolTipText(tooltip);
		}
		l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		Button t = new Button(parent, SWT.CHECK);
		t.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));
		context.bindValue(selectionProp.observe(t), modelProp.observeDetail(master));

	}

	public static String getLocalizedLabel(ProjectOSGiTranslationProvider translationProvider, MUILabel element) {
		return getLocalizedValue(translationProvider, (MApplicationElement) element, UiPackageImpl.Literals.UI_LABEL__LABEL, UiPackageImpl.Literals.UI_LABEL__LOCALIZED_LABEL);
	}

	public static String getLocalizedValue(ProjectOSGiTranslationProvider translationProvider, MApplicationElement element, EStructuralFeature feature, EStructuralFeature localizedFeature) {
		EObject eo = (EObject) element;
		if (translationProvider == null) {
			String value = (String) eo.eGet(localizedFeature);
			if (value != null && value.trim().length() > 0) {
				return value;
			}
		}

		String value = (String) eo.eGet(feature);
		if (value != null && value.trim().length() > 0) {
			return tr(translationProvider, value);
		}
		return null;

	}

	public static String tr(ProjectOSGiTranslationProvider translationProvider, String label) {
		if (label.startsWith("%") && translationProvider != null) { //$NON-NLS-1$
			String key = label.substring(1);
			String translation = translationProvider.translate(key);
			return translation == key ? label : translation;
		}
		return label;
	}

	public static void attachFiltering(Text searchText, final TableViewer viewer, final PatternFilter filter) {
		searchText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				filter.setPattern(((Text) e.widget).getText());
				viewer.refresh();
				if (viewer.getTable().getItemCount() > 0) {
					Object data = viewer.getTable().getItem(0).getData();
					viewer.setSelection(new StructuredSelection(data));
				}
			}
		});
		searchText.addTraverseListener(new TraverseListener() {

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.keyCode == SWT.ARROW_DOWN && viewer.getTable().getItemCount() > 0) {
					viewer.getControl().setFocus();
				}
			}
		});
	}
}