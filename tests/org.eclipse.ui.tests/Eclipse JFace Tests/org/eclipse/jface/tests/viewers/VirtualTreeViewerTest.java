package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

/**
 * Tests TreeViewer's VIRTUAL support with a normal content provider.
 * @since 3.2
 */
public class VirtualTreeViewerTest extends TreeViewerTest {

	public VirtualTreeViewerTest(String name) {
		super(name);
	}

	protected StructuredViewer createViewer(Composite parent) {
		Tree tree = new Tree(parent, SWT.VIRTUAL);
		fTreeViewer = new TreeViewer(tree);
		fTreeViewer.setContentProvider(new TestModelContentProvider());
		return fTreeViewer;
	}
}
