package org.eclipse.jface.tests.viewers.interactive;

import org.eclipse.jface.tests.viewers.TestElement;
import org.eclipse.jface.tests.viewers.TestModelLazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class TestLazyVirtualTree extends TestTree {

	public Viewer createViewer(Composite parent) {
		Tree tree = new Tree(parent, SWT.VIRTUAL);
		tree.addListener(SWT.SetData, new Listener() {
			private String getPosition(TreeItem item) {
				TreeItem parentItem = item.getParentItem();
				if (parentItem == null) {
					return "" + item.getParent().indexOf(item);
				}
				return getPosition(parentItem) + "." + parentItem.indexOf(item);
			}

			public void handleEvent(Event event) {
				String position = getPosition((TreeItem) event.item);
				if (position.endsWith(".32"))
					Thread.dumpStack();
				System.out.println("updating " + position);
			}
		});
		TreeViewer viewer = new TreeViewer(tree);
		viewer.setContentProvider(new TestModelLazyTreeContentProvider(viewer));
		viewer.setUseHashlookup(true);

		if (fViewer == null)
			fViewer = viewer;
		return viewer;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestBrowser browser = new TestLazyVirtualTree();
		if (args.length > 0 && args[0].equals("-twopanes"))
			browser.show2Panes();
		browser.setBlockOnOpen(true);
		browser.open(TestElement.createModel(3, 10));
	}

}
