/*******************************************************************************
 * Copyright (c) 2006, 2018 The Pampered Chef, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     The Pampered Chef, Inc. - initial API and implementation
 *     Brad Reynolds - bug 116920
 *     Matthew Hall - bug 260329
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 434283
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Shows how to bind a Combo so that when update its items, the selection is
 * retained if at all possible.
 */
public class Snippet002UpdateComboRetainSelection {
	public static void main(String[] args) {
		final Display display = new Display();

		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			ViewModel viewModel = new ViewModel();
			Shell shell = new View(viewModel).createShell();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}

			// Print the results
			System.out.println(viewModel.getText());
		});

		display.dispose();
	}

	/** Helper class for implementing JavaBeans support. */
	public static abstract class AbstractModelObject {
		private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			propertyChangeSupport.addPropertyChangeListener(listener);
		}

		public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
			propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			propertyChangeSupport.removePropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
			propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
		}

		protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
			propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
		}
	}

	/** The View's model--the root of our Model graph for this particular GUI. */
	public static class ViewModel extends AbstractModelObject {
		private String text = "beef";

		private List<String> choices = new ArrayList<>();
		{
			choices.add("pork");
			choices.add("beef");
			choices.add("poultry");
			choices.add("vegatables");
		}

		public List<String> getChoices() {
			return choices;
		}

		public void setChoices(List<String> choices) {
			List<String> old = this.choices;
			this.choices = choices;
			firePropertyChange("choices", old, choices);
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			String oldValue = this.text;
			this.text = text;
			firePropertyChange("text", oldValue, text);
		}
	}

	/** The GUI view. */
	static class View {
		private final ViewModel viewModel;
		/**
		 * This field is used to make a new choices array unique.
		 */
		static int count;

		public View(ViewModel viewModel) {
			this.viewModel = viewModel;
		}

		public Shell createShell() {
			// Build a UI
			Shell shell = new Shell();
			shell.setLayout(new GridLayout(1, false));

			Combo combo = new Combo(shell, SWT.BORDER | SWT.READ_ONLY);
			combo.setLayoutData(new GridData(SWT.FILL, SWT.END, true, false));

			Button reset = new Button(shell, SWT.NULL);
			reset.setText("reset collection");
			reset.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					List<String> newList = new ArrayList<>();
					newList.add("Chocolate");
					newList.add("Vanilla");
					newList.add("Mango Parfait");
					newList.add("beef");
					newList.add("Cheesecake");
					newList.add(Integer.toString(++count));
					viewModel.setChoices(newList);
				}
			});

			// Print value out first
			System.out.println(viewModel.getText());

			DataBindingContext bindingContext = new DataBindingContext();

			// This demonstrates a problem with Java generics:
			// It is hard to produce a class object with type List<String>
			@SuppressWarnings("unchecked")
			IBeanValueProperty<ViewModel, List<String>> choices = BeanProperties.value(ViewModel.class, "choices",
					(Class<List<String>>) (Object) List.class);

			IObservableList<String> list = MasterDetailObservables.detailList(choices.observe(viewModel),
					createListDetailFactory(), String.class);
			bindingContext.bindList(WidgetProperties.items().observe(combo), list);
			bindingContext.bindValue(WidgetProperties.text().observe(combo),
					BeanProperties.value(ViewModel.class, "text").observe(viewModel));

			shell.pack();
			shell.open();
			return shell;
		}
	}

	private static IObservableFactory<Collection<String>, IObservableList<String>> createListDetailFactory() {
		return target -> {
			WritableList<String> list = WritableList.withElementType(String.class);
			list.addAll(target);
			return list;
		};
	}
}
