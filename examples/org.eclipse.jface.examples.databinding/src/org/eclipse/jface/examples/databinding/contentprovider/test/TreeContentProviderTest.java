package org.eclipse.jface.examples.databinding.contentprovider.test;

import org.eclipse.jface.databinding.IReadableSet;
import org.eclipse.jface.databinding.ITreeProvider;
import org.eclipse.jface.databinding.updatables.UnionSet;
import org.eclipse.jface.databinding.updatables.WritableSet;
import org.eclipse.jface.databinding.viewers.DirtyIndicationLabelProvider;
import org.eclipse.jface.databinding.viewers.UpdatableTreeContentProvider;
import org.eclipse.jface.databinding.viewers.ViewerLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IViewerLabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Tests UpdatableTreeContentProvider and DirtyIndicationLabelProvider.
 * Creates a tree containing three randomly-generated sets of integers,
 * and one node that contains the union of the other sets.
 * 
 * @since 3.2
 */
public class TreeContentProviderTest {
	
	private Shell shell;
	private TreeViewer tree;
	private ListViewer unionViewer;
	private ListViewer transformViewer;
	private Label exploredNodesLabel;
	
	// Three randomly-generated sets of doubles
	private AsynchronousTestSet set1;
	private AsynchronousTestSet set2;
	private AsynchronousTestSet set3;
	
	// The union of the above three sets
	private UnionSet union;
	private Button randomize;
	private Button delete;

	public TreeContentProviderTest() {
		
		// Create the data model
		set1 = new AsynchronousTestSet();
		set2 = new AsynchronousTestSet();
		set3 = new AsynchronousTestSet();
		
		// A union of the above sets
		union = new UnionSet();
		union.add(set1);
		union.add(set2);
		union.add(set3);
		
		// Create shell
		shell = new Shell(Display.getCurrent());
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		shell.setLayout(layout);
		
		createTree();
		
		Composite buttonBar = new Composite(shell, SWT.NONE);
		buttonBar.setLayout(new FillLayout(SWT.HORIZONTAL));
		randomize = new Button(buttonBar, SWT.PUSH);
		randomize.setText("Randomize");
		randomize.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				AsynchronousTestSet.recomputeAll();
				super.widgetSelected(e);
			}
		});
		
		GridData data = new GridData();
		buttonBar.setLayoutData(data);
	}

	private void createTree() {
		// Create the tree provider. This provides the structure of the tree. This tree will
		// have an instance of RootNode as the root (which is really a placeholder), several
		// SimpleNodes as top-level nodes, and sets of randomly generated Doubles below each
		// SimpleNode.
		ITreeProvider treeProvider = new ITreeProvider() {
			public IReadableSet createChildList(Object element) {
				// If the parent is the root node, return the union of some randomly-generated
				// nodes and some hardcoded nodes
				if (element == tree.getInput()) {
					// Set of hardcoded nodes
					WritableSet topElements = new WritableSet();
					topElements.add(new SimpleNode("Random Set 1", set1));
					topElements.add(new SimpleNode("Random Set 2", set2));
					topElements.add(new SimpleNode("Random Set 3", set3));
					topElements.add(new SimpleNode("Union of the other sets", union));
					return topElements;
				}
				
				// If the parent is a RandomChildrenNode, return a randomly-generated
				// set of Doubles for its children
				if (element instanceof SimpleNode) {
					return ((SimpleNode)element).getChildren();
				}
				
				// Otherwise the node is a Double, which will have no children  
				return null;
			}
		};
		
		// Label provider for the tree
		IViewerLabelProvider labelProvider = new ViewerLabelProvider() {
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
		
		// UpdatableTreeContentProvider converts an ITreeProvider into a standard JFace content provider
		UpdatableTreeContentProvider contentProvider = new UpdatableTreeContentProvider(treeProvider);
		
		// Here we wrap our real label provider in a DirtyIndicationLabelProvider. This will 
		// add an affordance whenever a node is being fetched synchronously by the content provider.
		ILabelProvider dirtyDecoratingLabelProvider = new DirtyIndicationLabelProvider(tree.getControl(),
				contentProvider, labelProvider);
		
		tree.setContentProvider(contentProvider);
		tree.setLabelProvider(dirtyDecoratingLabelProvider);
		
		// For the ITreeProvider above, it doesn't matter what we select as the input.
		tree.setInput(new Object());
		
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 200;
		data.heightHint = 200;
		tree.getControl().setLayoutData(data);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = Display.getDefault();
		TreeContentProviderTest test = new TreeContentProviderTest();
		Shell s = test.getShell();
		s.setVisible(true);

		while (!s.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private Shell getShell() {
		return shell;
	}
}
