/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 263693
 *******************************************************************************/
package org.eclipse.jface.examples.databinding.contentprovider.test;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.UnionSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.viewers.ObservableSetTreeContentProvider;
import org.eclipse.jface.internal.databinding.provisional.viewers.ViewerLabelProvider;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.viewers.IViewerLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Tests UpdatableTreeContentProvider and DirtyIndicationLabelProvider. Creates
 * a tree containing three randomly-generated sets of integers, and one node
 * that contains the union of the other sets.
 *
 * @since 3.2
 */
public class TreeContentProviderTest {

	private Shell shell;
	private TreeViewer tree;

	// Three randomly-generated sets of doubles
	private AsynchronousTestSet set1;
	private AsynchronousTestSet set2;
	private AsynchronousTestSet set3;

	// The union of the above three sets
	private UnionSet<Object> union;
	private Button randomize;

	public TreeContentProviderTest() {

		// Create the data model
		set1 = new AsynchronousTestSet();
		set2 = new AsynchronousTestSet();
		set3 = new AsynchronousTestSet();

		// A union of the above sets
		union = new UnionSet<>(new IObservableSet[] { set1, set2, set3 });

		// Create shell
		shell = new Shell(Display.getCurrent());

		createTree();

		Composite buttonBar = new Composite(shell, SWT.NONE);
		{
			buttonBar.setLayout(new FillLayout(SWT.HORIZONTAL));
			randomize = new Button(buttonBar, SWT.PUSH);
			randomize.setText("Randomize");
			randomize.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					AsynchronousTestSet.recomputeAll();
					super.widgetSelected(e);
				}
			});

			GridLayoutFactory.fillDefaults().generateLayout(buttonBar);
		}

		GridLayoutFactory.fillDefaults().margins(LayoutConstants.getMargins())
				.generateLayout(shell);

		shell.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		});
	}

	/**
	 *
	 */
	protected void dispose() {
		set1.dispose();
		set2.dispose();
		set3.dispose();
		union.dispose();
	}

	private void createTree() {
		// Create the tree provider. This provides the structure of the tree.
		// This tree will
		// have an instance of RootNode as the root (which is really a
		// placeholder), several
		// SimpleNodes as top-level nodes, and sets of randomly generated
		// Doubles below each
		// SimpleNode.
		IObservableFactory<SimpleNode, IObservable> childrenFactory = new IObservableFactory<SimpleNode, IObservable>() {
			@Override
			public IObservable createObservable(SimpleNode element) {
				// If the parent is the root node, return the union of some
				// randomly-generated
				// nodes and some hardcoded nodes
				if (element == tree.getInput()) {
					// Set of hardcoded nodes
					WritableSet<SimpleNode> topElements = new WritableSet<>();
					topElements.add(new SimpleNode("Random Set 1", set1));
					topElements.add(new SimpleNode("Random Set 2", set2));
					topElements.add(new SimpleNode("Random Set 3", set3));
					topElements.add(new SimpleNode("Union of the other sets",
							union));
					return topElements;
				}

				// We return a new DelegatingObservableSet in order to
				// prevent the
				// original from being disposed.
				return Observables.proxyObservableSet(element.getChildren());
			}
		};

		// Label provider for the tree
		IViewerLabelProvider labelProvider = new ViewerLabelProvider() {
			@Override
			public void updateLabel(ViewerLabel label, Object element) {
				if (element instanceof SimpleNode) {
					SimpleNode node = (SimpleNode) element;

					label.setText(node.getNodeName());
				}

				if (element instanceof Integer) {
					Integer node = (Integer) element;

					label.setText("Integer " + node);
				}
			}
		};

		// Create tree viewer
		tree = new TreeViewer(shell, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

		// UpdatableTreeContentProvider converts an ITreeProvider into a
		// standard JFace content provider
		ObservableSetTreeContentProvider contentProvider = new ObservableSetTreeContentProvider(
				childrenFactory, null);

		tree.setContentProvider(contentProvider);
		tree.setLabelProvider(labelProvider);

		// For the ITreeProvider above, it doesn't matter what we select as the
		// input.
		tree.setInput(new Object());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final Display display = Display.getDefault();
		Realm.runWithDefault(DisplayRealm.getRealm(display), new Runnable() {
			@Override
			public void run() {
				TreeContentProviderTest test = new TreeContentProviderTest();
				Shell s = test.getShell();
				s.pack();
				s.setVisible(true);

				while (!s.isDisposed()) {
					if (!display.readAndDispatch())
						display.sleep();
				}
			}
		});
		display.dispose();
	}

	private Shell getShell() {
		return shell;
	}
}
