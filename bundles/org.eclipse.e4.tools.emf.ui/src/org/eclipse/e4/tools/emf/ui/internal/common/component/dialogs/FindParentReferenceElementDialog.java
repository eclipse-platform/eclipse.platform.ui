/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Steven Spungin <steven@spungin.tv> - Bug 437469
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.e4.tools.emf.ui.common.IModelElementProvider.Filter;
import org.eclipse.e4.tools.emf.ui.common.IModelElementProvider.ModelResultHandler;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.Util.InternalPackage;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.ClassContributionCollector;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.fragment.MStringModelFragment;
import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class FindParentReferenceElementDialog extends TitleAreaDialog {
	private MStringModelFragment fragment;
	private AbstractComponentEditor editor;
	private TableViewer viewer;
	private Messages Messages;
	private ModelResultHandlerImpl currentResultHandler;
	private WritableList list;
	private ComboViewer eClassViewer;
	private Text searchText;
	private AutoCompleteField acText;

	public FindParentReferenceElementDialog(Shell parentShell, AbstractComponentEditor editor, MStringModelFragment fragment, Messages Messages) {
		super(parentShell);
		this.fragment = fragment;
		this.editor = editor;
		this.Messages = Messages;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);

		final Image titleImage = new Image(parent.getDisplay(), getClass().getClassLoader().getResourceAsStream("/icons/full/wizban/import_wiz.png")); //$NON-NLS-1$
		setTitleImage(titleImage);
		getShell().addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				titleImage.dispose();
			}
		});

		getShell().setText(Messages.FindParentReferenceElementDialog_ShellTitle);
		setTitle(Messages.FindParentReferenceElementDialog_Title);
		setMessage(Messages.FindParentReferenceElementDialog_Message);

		Composite container = new Composite(comp, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new GridLayout(2, false));

		Label l = new Label(container, SWT.NONE);
		l.setText(Messages.FindParentReferenceElementDialog_ContainerType);

		Combo combo = new Combo(container, SWT.NONE);
		eClassViewer = new ComboViewer(combo);
		eClassViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((EClass) element).getName();
			}
		});
		eClassViewer.setContentProvider(new ArrayContentProvider());
		final List<EClass> eClassList = new ArrayList<EClass>();
		for (InternalPackage p : Util.loadPackages()) {
			eClassList.addAll(p.getAllClasses());
		}
		eClassViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				EClass ec1 = (EClass) e1;
				EClass ec2 = (EClass) e2;
				return ec1.getName().compareTo(ec2.getName());
			}
		});
		eClassViewer.setInput(eClassList);
		eClassViewer.getCombo().select(0);
		eClassViewer.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		eClassViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateSearch();
			}
		});

		ArrayList<String> vals = new ArrayList<String>();
		for (EClass item : eClassList) {
			vals.add(item.getName());
		}
		final String[] values = vals.toArray(new String[0]);
		ComboContentAdapter textContentAdapter = new ComboContentAdapter() {
			@Override
			public void setControlContents(Control control, String text1, int cursorPosition) {
				super.setControlContents(control, text1, cursorPosition);
				int index = Arrays.asList(values).indexOf(text1);
				EClass eClass = eClassList.get(index);
				eClassViewer.setSelection(new StructuredSelection(eClass));
			}
		};
		acText = new AutoCompleteField(combo, textContentAdapter, values);

		l = new Label(container, SWT.NONE);
		l.setText(Messages.FindParentReferenceElementDialog_Search);

		searchText = new Text(container, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		searchText.setLayoutData(gd);

		l = new Label(container, SWT.PUSH);

		viewer = new TableViewer(container);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 200;
		viewer.getControl().setLayoutData(gd);
		viewer.setLabelProvider(new StyledCellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				EObject o = (EObject) cell.getElement();
				AbstractComponentEditor editor = FindParentReferenceElementDialog.this.editor.getEditor().getEditor(o.eClass());
				cell.setImage(editor.getImage(o, searchText.getDisplay()));

				MApplicationElement appEl = (MApplicationElement) o;

				StyledString styledString = new StyledString(editor.getLabel(o) + " (" + (appEl.getElementId() == null ? "<" + Messages.FindParentReferenceElementDialog_NoId + ">" : appEl.getElementId()) + ")", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				String detailLabel = editor.getDetailLabel(o);
				if (detailLabel != null && !detailLabel.equals(appEl.getElementId())) {
					styledString.append(" - " + detailLabel, StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
				}

				styledString.append(" - " + o.eResource().getURI(), StyledString.COUNTER_STYLER); //$NON-NLS-1$
				cell.setStyleRanges(styledString.getStyleRanges());
				cell.setText(styledString.getString());
			}
		});
		viewer.setContentProvider(new ObservableListContentProvider());
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});

		list = new WritableList();
		viewer.setInput(list);

		final ClassContributionCollector collector = getCollector();

		searchText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				updateSearch();
			}
		});

		Button button = new Button(container, SWT.PUSH);
		button.setText(Messages.FindParentReferenceElementDialog_ClearCache);
		button.setLayoutData(new GridData(GridData.END, GridData.CENTER, true, false, 2, 1));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				collector.clearModelCache();
			}
		});
		return comp;
	}

	protected void updateSearch() {
		if (currentResultHandler != null) {
			currentResultHandler.cancled = true;
		}
		list.clear();

		Filter filter = new Filter((EClass) ((IStructuredSelection) eClassViewer.getSelection()).getFirstElement(), searchText.getText());

		currentResultHandler = new ModelResultHandlerImpl(list, filter, editor, ((EObject) fragment).eResource());
		getCollector().findModelElements(filter, currentResultHandler);

	}

	@Override
	protected void okPressed() {
		IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
		if (!s.isEmpty()) {
			MApplicationElement el = (MApplicationElement) s.getFirstElement();
			if (el.getElementId() != null && el.getElementId().trim().length() > 0) {
				Command cmd = SetCommand.create(editor.getEditingDomain(), fragment, FragmentPackageImpl.Literals.STRING_MODEL_FRAGMENT__PARENT_ELEMENT_ID, el.getElementId());
				if (cmd.canExecute()) {
					editor.getEditingDomain().getCommandStack().execute(cmd);
					super.okPressed();
				}
			} else {
				setErrorMessage(Messages.FindParentReferenceElementDialog_NoReferenceId);
			}
		}
	}

	private ClassContributionCollector getCollector() {
		Bundle bundle = FrameworkUtil.getBundle(FindParentReferenceElementDialog.class);
		BundleContext context = bundle.getBundleContext();
		ServiceReference ref = context.getServiceReference(ClassContributionCollector.class.getName());
		if (ref != null) {
			return (ClassContributionCollector) context.getService(ref);
		}
		return null;
	}

	private static class ModelResultHandlerImpl implements ModelResultHandler {
		private boolean cancled = false;
		private IObservableList list;
		private Filter filter;
		private AbstractComponentEditor editor;
		private Resource resource;

		public ModelResultHandlerImpl(IObservableList list, Filter filter, AbstractComponentEditor editor, Resource resource) {
			this.list = list;
			this.filter = filter;
			this.editor = editor;
			this.resource = resource;
		}

		@Override
		public void result(EObject data) {
			if (!cancled) {
				if (!resource.getURI().equals(data.eResource().getURI())) {
					if (data instanceof MApplicationElement) {
						String elementId = ((MApplicationElement) data).getElementId();
						if (elementId == null) {
							list.add(data);
							return;
						}

						if (elementId != null && elementId.trim().length() > 0) {
							if (filter.elementIdPattern.matcher(elementId).matches()) {
								list.add(data);
								return;
							}
						}

						String label = editor.getDetailLabel(data);
						if (elementId != null && label != null && label.trim().length() > 0) {
							if (filter.elementIdPattern.matcher(label).matches()) {
								list.add(data);
								return;
							}
						}
					}
				}
			}
		}
	}

}