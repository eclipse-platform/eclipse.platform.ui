/*******************************************************************************
 * Copyright (c) 2006, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 116920
 *******************************************************************************/

package org.eclipse.jface.examples.databinding.contentprovider.test;

import java.util.Collections;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.viewers.ListeningLabelProvider;
import org.eclipse.jface.databinding.viewers.ObservableSetContentProvider;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Tests UpdatableTreeContentProvider and DirtyIndicationLabelProvider. Creates
 * a tree containing three randomly-generated sets of integers, and one node
 * that contains the union of the other sets.
 *
 * @since 1.0
 */
public class LabelProviderTest {

	private Shell shell;

	private ListViewer list;

	private WritableSet<RenamableItem> setOfRenamables;

	private Button addButton;

	private Button removeButton;

	private Button renameButton;

	private SelectionListener buttonSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			Button pressed = (Button) e.widget;
			if (pressed == addButton) {
				setOfRenamables.add(new RenamableItem());
			} else if (pressed == removeButton) {
				setOfRenamables.remove(getCurrentSelection());
			} else if (pressed == renameButton) {
				rename(getCurrentSelection());
			}

			super.widgetSelected(e);
		}
	};

	private IObservableValue<RenamableItem> selectedRenamable;

	/**
	 *
	 */
	public LabelProviderTest() {

		// Create shell
		shell = new Shell(Display.getCurrent());

		// Initialize shell
		setOfRenamables = new WritableSet<>();

		list = new ListViewer(shell);
		ObservableSetContentProvider<RenamableItem> contentProvider = new ObservableSetContentProvider<>();
		list.setContentProvider(contentProvider);
		list.setLabelProvider(new ListeningLabelProvider<>(contentProvider.getKnownElements()) {
			RenamableItem.Listener listener = item -> fireChangeEvent(Collections.singleton(item));

			@Override
			public void updateLabel(ViewerLabel label, Object element) {
				if (element instanceof RenamableItem item) {
					label.setText(item.getName());
				}
			}

			@Override
			protected void addListenerTo(RenamableItem next) {
				next.addListener(listener);
			}

			@Override
			protected void removeListenerFrom(RenamableItem next) {
				next.removeListener(listener);
			}
		});
		list.setInput(setOfRenamables);

		selectedRenamable = ViewerProperties.singleSelection(RenamableItem.class).observe(list);

		Composite buttonBar = new Composite(shell, SWT.NONE);
		// Initialize buttonBar
		addButton = new Button(buttonBar, SWT.PUSH);
		addButton.setText("Add"); //$NON-NLS-1$
		addButton.addSelectionListener(buttonSelectionListener);
		removeButton = new Button(buttonBar, SWT.PUSH);
		removeButton.addSelectionListener(buttonSelectionListener);
		removeButton.setText("Remove"); //$NON-NLS-1$
		renameButton = new Button(buttonBar, SWT.PUSH);
		renameButton.addSelectionListener(buttonSelectionListener);
		renameButton.setText("Rename"); //$NON-NLS-1$

		selectedRenamable
				.addValueChangeListener(event -> {
					boolean shouldEnable = selectedRenamable.getValue() != null;
					removeButton.setEnabled(shouldEnable);
					renameButton.setEnabled(shouldEnable);
				});
		removeButton.setEnabled(false);
		renameButton.setEnabled(false);

		GridLayoutFactory.fillDefaults().generateLayout(buttonBar);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(
				LayoutConstants.getMargins()).generateLayout(shell);
	}

	/**
	 * @param currentSelection
	 */
	protected void rename(final RenamableItem currentSelection) {
		InputDialog inputDialog = new InputDialog(
				shell,
				"Edit name", "Enter the new item name", currentSelection.getName(), null); //$NON-NLS-1$ //$NON-NLS-2$
		if (Window.OK == inputDialog.open()) {
			currentSelection.setName(inputDialog.getValue());
		}
	}

	/**
	 * @return
	 */
	protected RenamableItem getCurrentSelection() {
		return selectedRenamable.getValue();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final Display display = Display.getDefault();
		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			LabelProviderTest test = new LabelProviderTest();
			Shell s = test.getShell();
			s.pack();
			s.setVisible(true);

			while (!s.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
		});
		display.dispose();
	}

	private Shell getShell() {
		return shell;
	}
}
