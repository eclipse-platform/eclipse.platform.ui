/*******************************************************************************
 * Copyright (c) 2015, 2019 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simon Scholz<simon.scholz@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.snippets.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.AbstractSelectionDialog;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * A snippet to demonstrate the usage of the {@link AbstractSelectionDialog}.
 */
public class Snippet070GenericSelectionDialog {

	public Snippet070GenericSelectionDialog(final Shell shell) {
		Label text = WidgetFactory.label(SWT.CENTER).create(shell);
		List<Model> models = getSampleModelElements();

		GenericSelectionDialog genericSelectionDialog = new GenericSelectionDialog(shell, models, models.get(0),
				"SelectionDialog with generics", "Select one or more of the Model elements");

		int open = genericSelectionDialog.open();
		if (Dialog.OK == open) {
			Collection<Model> result = genericSelectionDialog.getResult();
			text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			text.setText(result.stream().map(Object::toString)
					.collect(Collectors.joining(",\r\n")));
			text.redraw();
			System.out.println("Selected model elements: " + result);
		}
	}

	private List<Model> getSampleModelElements() {
		List<Model> models = new ArrayList<>();

		models.add(new Model("Simon Scholz"));
		models.add(new Model("Lars Vogel"));
		models.add(new Model("Dani Megert"));
		models.add(new Model("Wim Jongman"));
		models.add(new Model("Tom Schindl"));
		return models;
	}

	public static void main(String[] args) {

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		shell.open();

		new Snippet070GenericSelectionDialog(shell);

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}

	private class GenericSelectionDialog extends AbstractSelectionDialog<Model> {

		/**
		 * List width in characters.
		 */
		private static final int LIST_WIDTH = 60;

		/**
		 * List height in characters.
		 */
		private static final int LIST_HEIGHT = 10;

		private final Collection<Model> models;

		/**
		 * List to display the resolutions.
		 */
		private ListViewer listViewer;

		/**
		 * Creates an instance of this dialog to display the given features.
		 * <p>
		 * There must be at least one feature.
		 * </p>
		 *
		 * @param shell
		 *            the parent shell
		 * @param models
		 *            the models to display
		 * @param shellTitle
		 *            shell title
		 * @param shellMessage
		 *            shell message
		 */
		public GenericSelectionDialog(Shell shell, Collection<Model> models, Model initialSelection, String shellTitle,
				String shellMessage) {

			super(shell);
			if (models == null || models.isEmpty()) {
				throw new IllegalArgumentException();
			}
			this.models = models;
			setTitle(shellTitle);
			setMessage(shellMessage);

			// set a safe default
			setInitialSelection(initialSelection);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);

			// Create label
			createMessageArea(composite);
			// Create list viewer
			listViewer = new ListViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			GridData data = new GridData(GridData.FILL_BOTH);
			data.heightHint = convertHeightInCharsToPixels(LIST_HEIGHT);
			data.widthHint = convertWidthInCharsToPixels(LIST_WIDTH);
			listViewer.getList().setLayoutData(data);
			listViewer.getList().setFont(parent.getFont());
			// Set the label provider
			LabelProvider labelProvider = LabelProvider
					.createTextProvider(element -> element == null ? "" : ((Model) element).getName());
			listViewer.setLabelProvider(labelProvider);

			// Set the content provider
			listViewer.setContentProvider(ArrayContentProvider.getInstance());
			listViewer.setInput(models);

			// Set the initial selection
			listViewer.setSelection(new StructuredSelection(getInitialSelection()), true);

			// Add a selection change listener
			listViewer.addSelectionChangedListener(
					event -> getButton(IDialogConstants.OK_ID).setEnabled(!event.getSelection().isEmpty()));

			// Add double-click listener
			listViewer.addDoubleClickListener(event -> okPressed());
			return composite;
		}

		@Override
		protected Control createButtonBar(Composite parent) {
			Control buttonSection = super.createButtonBar(parent);
			// disable ok button if the selection is empty
			getButton(IDialogConstants.OK_ID).setEnabled(!listViewer.getStructuredSelection().isEmpty());

			return buttonSection;
		}

		@Override
		protected void okPressed() {
			IStructuredSelection selection = listViewer.getStructuredSelection();
			setResult(selection, Model.class);
			super.okPressed();
		}
	}

	private static class Model {

		private String name;

		public Model(String name) {
			this.setName(name);
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "Model [name=" + name + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			return prime * result + ((name == null) ? 0 : name.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			Model other = (Model) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	}
}
