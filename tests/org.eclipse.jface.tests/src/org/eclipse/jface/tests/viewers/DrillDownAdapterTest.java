package org.eclipse.jface.tests.viewers;

import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.DrillDownAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DrillDownAdapterTest {
	private Shell shell;

	@Before
	public void setUp() {
		shell = new Shell();

		shell.open();
	}

	@After
	public void tearDown() {
		shell.close();
	}

	/**
	 * @see <a href=
	 *      "https://bugs.eclipse.org/bugs/show_bug.cgi?id=564941">Bug#564941</a>
	 */
	@Test
	public void test_goInto_and_goHome() {
		MyModel home = new MyModel("Home", null);
		MyModel node1 = new MyModel("Node1", home);
		MyModel node2 = new MyModel("Node2", node1);
		MyModel node3 = new MyModel("Node3", node2);
		MyModel node4 = new MyModel("Node4", node3);

		TreeViewer viewer = new TreeViewer(shell);
		viewer.setLabelProvider(new LabelProvider());
		viewer.setContentProvider(new MyContentProvider());
		viewer.setInput(home);
		viewer.expandAll();

		DrillDownAdapter dda = new DrillDownAdapter(viewer);

		assertSame(home, viewer.getInput());

		viewer.setSelection(new StructuredSelection(node1));
		dda.goInto();
		assertSame(node1, viewer.getInput());

		viewer.setSelection(new StructuredSelection(node2));
		dda.goInto();
		assertSame(node2, viewer.getInput());

		viewer.setSelection(new StructuredSelection(node3));
		dda.goInto();
		assertSame(node3, viewer.getInput());

		// can't go into further
		viewer.setSelection(new StructuredSelection(node4));
		dda.goInto();
		assertSame(node3, viewer.getInput());

		dda.goHome();
		viewer.setSelection(new StructuredSelection(node1));
		assertSame(home, viewer.getInput());
	}

	static class MyContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			return ((MyModel) inputElement).children.toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		@Override
		public Object getParent(Object element) {
			if (element == null) {
				return null;
			}

			return ((MyModel) element).parent;
		}

		@Override
		public boolean hasChildren(Object element) {
			return element != null && ((MyModel) element).children.size() > 0;
		}
	}

	static class MyModel {
		public MyModel parent;
		public List<MyModel> children = new ArrayList<>();
		public String name;

		public MyModel(String name, MyModel parent) {
			this.name = name;
			this.parent = parent;
			if (parent != null) {
				parent.children.add(this);
			}
		}

		@Override
		public String toString() {
			return name;
		}
	}

}
