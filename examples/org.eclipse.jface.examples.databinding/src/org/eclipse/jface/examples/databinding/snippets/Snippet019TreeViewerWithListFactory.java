/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bugs 260329, 260337
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 442278
 *******************************************************************************/
package org.eclipse.jface.examples.databinding.snippets;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class Snippet019TreeViewerWithListFactory {

	private Button pasteButton;
	private Button copyButton;
	private Shell shell;
	private Button addChildBeanButton;
	private Button removeBeanButton;
	private TreeViewer beanViewer;
	private Tree tree;
	private Text beanText;
	private DataBindingContext m_bindingContext;

	private Bean input = createBean("input");
	private IObservableValue clipboard;

	/**
	 * Launch the application
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = Display.getDefault();
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			@Override
			public void run() {
				try {
					Snippet019TreeViewerWithListFactory window = new Snippet019TreeViewerWithListFactory();
					window.open();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Open the window
	 */
	public void open() {
		final Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}

	/**
	 * Create contents of the window
	 */
	protected void createContents() {
		shell = new Shell();
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		shell.setLayout(gridLayout_1);
		shell.setSize(535, 397);
		shell.setText("SWT Application");

		final Composite group = new Composite(shell, SWT.NONE);
		final RowLayout rowLayout = new RowLayout();
		rowLayout.marginTop = 0;
		rowLayout.marginRight = 0;
		rowLayout.marginLeft = 0;
		rowLayout.marginBottom = 0;
		rowLayout.pack = false;
		group.setLayout(rowLayout);
		group
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false,
						2, 1));

		final Button addRootButton = new Button(group, SWT.NONE);
		addRootButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				List list = input.getList();
				Bean root = createBean("root");
				list.add(root);
				input.setList(list);

				beanViewer.setSelection(new StructuredSelection(root));
				beanText.selectAll();
				beanText.setFocus();
			}
		});
		addRootButton.setText("Add Root");

		addChildBeanButton = new Button(group, SWT.NONE);
		addChildBeanButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				Bean parent = getSelectedBean();
				List list = new ArrayList(parent.getList());
				Bean child = createBean("child");
				list.add(child);
				parent.setList(list);

				beanViewer.setSelection(new StructuredSelection(child));
				beanText.selectAll();
				beanText.setFocus();
			}
		});
		addChildBeanButton.setText("Add Child");

		removeBeanButton = new Button(group, SWT.NONE);
		removeBeanButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				TreeItem selectedItem = beanViewer.getTree().getSelection()[0];
				TreeItem parentItem = selectedItem.getParentItem();
				Bean parent;
				int index;
				if (parentItem == null) {
					parent = input;
					index = beanViewer.getTree().indexOf(selectedItem);
				} else {
					parent = (Bean) parentItem.getData();
					index = parentItem.indexOf(selectedItem);
				}

				List list = new ArrayList(parent.getList());
				list.remove(index);
				parent.setList(list);
			}
		});
		removeBeanButton.setText("Remove");

		copyButton = new Button(group, SWT.NONE);
		copyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				clipboard.setValue(getSelectedBean());
			}
		});
		copyButton.setText("Copy");

		pasteButton = new Button(group, SWT.NONE);
		pasteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				Bean copy = (Bean) clipboard.getValue();
				if (copy == null)
					return;
				Bean parent = getSelectedBean();
				if (parent == null)
					parent = input;

				List list = new ArrayList(parent.getList());
				list.add(copy);
				parent.setList(list);

				beanViewer.setSelection(new StructuredSelection(copy));
				beanText.selectAll();
				beanText.setFocus();
			}
		});
		pasteButton.setText("Paste");

		final Button refreshButton = new Button(group, SWT.NONE);
		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				beanViewer.refresh();
			}
		});
		refreshButton.setText("Refresh");

		beanViewer = new TreeViewer(shell, SWT.FULL_SELECTION | SWT.BORDER);
		beanViewer.setUseHashlookup(true);
		tree = beanViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		final Label itemNameLabel = new Label(shell, SWT.NONE);
		itemNameLabel.setText("Item Name");

		beanText = new Text(shell, SWT.BORDER);
		final GridData gd_beanValue = new GridData(SWT.FILL, SWT.CENTER, true,
				false);
		beanText.setLayoutData(gd_beanValue);
		m_bindingContext = initDataBindings();
		//
		initExtraBindings(m_bindingContext);
	}

	private static Bean createBean(String name) {
		return new Bean(name);
	}

	protected DataBindingContext initDataBindings() {
		IObservableValue treeViewerSelectionObserveSelection = ViewersObservables
				.observeSingleSelection(beanViewer);
		IObservableValue textTextObserveWidget = SWTObservables.observeText(
				beanText, SWT.Modify);
		IObservableValue treeViewerValueObserveDetailValue = BeansObservables
				.observeDetailValue(treeViewerSelectionObserveSelection,
						"text", String.class);
		//
		//
		DataBindingContext bindingContext = new DataBindingContext();
		//
		bindingContext.bindValue(textTextObserveWidget,
				treeViewerValueObserveDetailValue);
		//
		return bindingContext;
	}

	private Bean getSelectedBean() {
		IStructuredSelection selection = beanViewer.getStructuredSelection();
		if (selection.isEmpty())
			return null;
		return (Bean) selection.getFirstElement();
	}

	private void initExtraBindings(DataBindingContext dbc) {
		final IObservableValue beanViewerSelection = ViewersObservables
				.observeSingleSelection(beanViewer);
		IObservableValue beanSelected = new ComputedValue(Boolean.TYPE) {
			@Override
			protected Object calculate() {
				return Boolean.valueOf(beanViewerSelection.getValue() != null);
			}
		};
		dbc.bindValue(SWTObservables.observeEnabled(addChildBeanButton),
				beanSelected);
		dbc.bindValue(SWTObservables.observeEnabled(removeBeanButton),
				beanSelected);

		clipboard = new WritableValue();
		dbc.bindValue(SWTObservables.observeEnabled(copyButton), beanSelected);
		dbc.bindValue(SWTObservables.observeEnabled(pasteButton),
				new ComputedValue(Boolean.TYPE) {
					@Override
					protected Object calculate() {
						return Boolean.valueOf(clipboard.getValue() != null);
					}
				});

		ViewerSupport.bind(beanViewer, input, BeanProperties.list("list",
				Bean.class), BeanProperties.value(Bean.class, "text"));
	}

	static class Bean {
		/* package */PropertyChangeSupport changeSupport = new PropertyChangeSupport(
				this);
		private String text;
		private List list;

		public Bean(String text) {
			this.text = text;
			list = new ArrayList();
		}

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			changeSupport.addPropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			changeSupport.removePropertyChangeListener(listener);
		}

		public String getText() {
			return text;
		}

		public void setText(String value) {
			changeSupport.firePropertyChange("text", this.text,
					this.text = value);
		}

		public List getList() {
			if (list == null)
				return null;
			return new ArrayList(list);
		}

		public void setList(List list) {
			if (list != null)
				list = new ArrayList(list);
			changeSupport.firePropertyChange("list", this.list,
					this.list = list);
		}

		public boolean hasListeners(String propertyName) {
			return changeSupport.hasListeners(propertyName);
		}
	}
}
