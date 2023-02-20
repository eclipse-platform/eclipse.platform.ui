/*******************************************************************************
 * Copyright (c) 2010, 2017 BestSolution.at and others.
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
 * Andrej ten Brummelhuis <andrejbrummelhuis@gmail.com> - Bug 395283
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.e4.tools.emf.ui.common.IModelElementProvider.Filter;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.ClassContributionCollector;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class FindImportElementDialog extends SaveDialogBoundsSettingsDialog {
	private final EObject element;
	private final AbstractComponentEditor<?> editor;
	private TableViewer viewer;
	private final Messages Messages;

	public FindImportElementDialog(Shell parentShell, AbstractComponentEditor<?> editor, EObject element,
			Messages Messages) {
		super(parentShell);
		this.element = element;
		this.editor = editor;
		this.Messages = Messages;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite comp = (Composite) super.createDialogArea(parent);

		final Image titleImage = new Image(parent.getDisplay(), getClass().getClassLoader().getResourceAsStream(
				"/icons/full/wizban/import_wiz.png")); //$NON-NLS-1$
		setTitleImage(titleImage);
		getShell().addDisposeListener(e -> titleImage.dispose());

		getShell().setText(Messages.FindImportElementDialog_ShellTitle);
		setTitle(Messages.FindImportElementDialog_Title);
		setMessage(Messages.FindImportElementDialog_Message);

		final Composite container = new Composite(comp, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new GridLayout(2, false));

		Label l = new Label(container, SWT.NONE);
		l.setText(Messages.FindImportElementDialog_Search);

		final Text searchText = new Text(container, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
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
				final EObject o = (EObject) cell.getElement();
				cell.setImage(editor.getImage(o));

				final MApplicationElement appEl = (MApplicationElement) o;
				final StyledString styledString = new StyledString(
						editor.getLabel(o)
						+ " (" + (appEl.getElementId() == null ? "<" + Messages.FindImportElementDialog_noId + ">" : appEl.getElementId()) + ")", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				final String detailLabel = editor.getDetailLabel(o);
				if (detailLabel != null && !detailLabel.equals(appEl.getElementId())) {
					styledString.append(" - " + detailLabel, StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
				}

				styledString.append(" - " + o.eResource().getURI(), StyledString.COUNTER_STYLER); //$NON-NLS-1$
				cell.setStyleRanges(styledString.getStyleRanges());
				cell.setText(styledString.getString());
			}
		});
		viewer.setContentProvider(new ObservableListContentProvider<>());
		viewer.addDoubleClickListener(event -> okPressed());

		final WritableList<Object> list = new WritableList<>();
		viewer.setInput(list);

		final ClassContributionCollector collector = getCollector();

		searchText.addModifyListener(new ModifyListener() {
			private ModelResultHandlerImpl currentResultHandler;

			@Override
			public void modifyText(ModifyEvent e) {
				if (currentResultHandler != null) {
					currentResultHandler.cancel();
				}
				list.clear();
				final Filter filter = new Filter(element.eClass(), searchText.getText());
				currentResultHandler = new ModelResultHandlerImpl(list, filter, editor, element.eResource());
				collector.findModelElements(filter, currentResultHandler);
			}
		});

		final Button button = new Button(container, SWT.PUSH);
		button.setText(Messages.FindImportElementDialog_ClearCache);
		button.setLayoutData(new GridData(GridData.END, GridData.CENTER, true, false, 2, 1));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				collector.clearModelCache();
			}
		});

		return comp;
	}

	@Override
	protected void okPressed() {
		final IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
		if (!s.isEmpty()) {
			final MApplicationElement el = (MApplicationElement) s.getFirstElement();
			if (el.getElementId() != null && el.getElementId().trim().length() > 0) {
				final Command cmd = SetCommand.create(editor.getEditingDomain(), element,
						ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID, el.getElementId());
				if (cmd.canExecute()) {
					editor.getEditingDomain().getCommandStack().execute(cmd);
					super.okPressed();
				}
			} else {
				setErrorMessage(Messages.FindImportElementDialog_NoIdReference);
			}
		}
	}

	private ClassContributionCollector getCollector() {
		final Bundle bundle = FrameworkUtil.getBundle(FindImportElementDialog.class);
		final BundleContext context = bundle.getBundleContext();
		final ServiceReference<?> ref = context.getServiceReference(ClassContributionCollector.class.getName());
		if (ref != null) {
			return (ClassContributionCollector) context.getService(ref);
		}
		return null;
	}

}