package org.eclipse.compare.internal.patch;

import java.util.ResourceBundle;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.INavigatable;
import org.eclipse.compare.internal.IOpenable;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

public class CheckboxDiffTreeViewer extends ContainerCheckedTreeViewer {
	
	class CheckboxDiffViewerContentProvider implements ITreeContentProvider {
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// empty implementation
		}
	
		public boolean isDeleted(Object element) {
			return false;
		}
			
		public void dispose() {
			inputChanged(CheckboxDiffTreeViewer.this, getInput(), null);
		}
			
		public Object getParent(Object element) {
			if (element instanceof IDiffElement) 
				return ((IDiffElement)element).getParent();
			return null;
		}
		
		public final boolean hasChildren(Object element) {
			if (element instanceof IDiffContainer) 
				return ((IDiffContainer)element).hasChildren();
			return false;
		}
		
		public final Object[] getChildren(Object element) {
			if (element instanceof IDiffContainer)
				return ((IDiffContainer)element).getChildren();
			return new Object[0];
		}
		
		public Object[] getElements(Object element) {
			return getChildren(element);
		}				
	}
	
	/*
	 * Takes care of swapping left and right if fLeftIsLocal
	 * is true.
	 */
	class CheckboxDiffViewerLabelProvider extends LabelProvider {
		
		public String getText(Object element) {
		
			if (element instanceof IDiffElement)
				return ((IDiffElement)element).getName();
			
			
			return Utilities.getString(fBundle, "defaultLabel"); //$NON-NLS-1$
		}
	
		public Image getImage(Object element) {
			if (element instanceof IDiffElement) {
				IDiffElement input= (IDiffElement) element;	
				return input.getImage();
			}
			return null;
		}
	}

	CompareConfiguration fCompareConfiguration;
	private ResourceBundle fBundle;

	public CheckboxDiffTreeViewer(Composite parent, CompareConfiguration compareConfiguration) {
		super(parent, SWT.NONE);
		initialize(compareConfiguration);
	}

	private void initialize(CompareConfiguration compareConfiguration) {
		Control tree= getControl();
		
		INavigatable nav= new INavigatable() {
			public boolean selectChange(int flag) {
				if (flag == INavigatable.FIRST_CHANGE) {
					setSelection(StructuredSelection.EMPTY);
					flag = INavigatable.NEXT_CHANGE;
				} else if (flag == INavigatable.LAST_CHANGE) {
					setSelection(StructuredSelection.EMPTY);
					flag = INavigatable.PREVIOUS_CHANGE;
				}
				// Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
				return internalNavigate(flag == INavigatable.NEXT_CHANGE, true);
			}
			public Object getInput() {
				return CheckboxDiffTreeViewer.this.getInput();
			}
			public boolean openSelectedChange() {
				internalOpen();
				return true;
			}
		};
		tree.setData(INavigatable.NAVIGATOR_PROPERTY, nav);
		
		// Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
		IOpenable openable= new IOpenable() {
			public void openSelected() {
				internalOpen();
			}
		};
		tree.setData(IOpenable.OPENABLE_PROPERTY, openable);
		
		fBundle= ResourceBundle.getBundle("org.eclipse.compare.structuremergeviewer.DiffTreeViewerResources"); //$NON-NLS-1$
				
		setContentProvider(new CheckboxDiffViewerContentProvider());
		setLabelProvider(new CheckboxDiffViewerLabelProvider());
		
		addSelectionChangedListener(
			new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent se) {
					//updateActions();
				}
			}
		);
												
	}

/**
 * Selects the next (or previous) node of the current selection.
 * If there is no current selection the first (last) node in the tree is selected.
 * Wraps around at end or beginning.
 * Clients may override. 
 *
 * @param next if <code>true</code> the next node is selected, otherwise the previous node
 * @param fireOpen if <code>true</code> an open event is fired.
 * @return <code>true</code> if at end (or beginning)
 */
private boolean internalNavigate(boolean next, boolean fireOpen) {
	
	Control c= getControl();
	if (!(c instanceof Tree))
		return false;
		
	Tree tree= (Tree) c;
	TreeItem item= null;
	TreeItem children[]= tree.getSelection();
	if (children != null && children.length > 0)
		item= children[0];
	if (item == null) {
		children= tree.getItems();
		if (children != null && children.length > 0) {
			item= children[0];
			if (item != null && item.getItemCount() <= 0) {
				internalSetSelection(item, fireOpen);				// Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
				return false;
			}
		}
	}
		
	while (true) {
		item= findNextPrev(item, next);
		if (item == null)
			break;
		if (item.getItemCount() <= 0)
			break;
	}
	
	if (item != null) {
		internalSetSelection(item, fireOpen);	// Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
		return false;
	}
	return true;
}

private TreeItem findNextPrev(TreeItem item, boolean next) {
	
	if (item == null)
		return null;
	
	TreeItem children[]= null;

	if (!next) {
	
		TreeItem parent= item.getParentItem();
		if (parent != null)
			children= parent.getItems();
		else
			children= item.getParent().getItems();
		
		if (children != null && children.length > 0) {
			// goto previous child
			int index= 0;
			for (; index < children.length; index++)
				if (children[index] == item)
					break;
			
			if (index > 0) {
				
				item= children[index-1];
				
				while (true) {
					createChildren(item);
					int n= item.getItemCount();
					if (n <= 0)
						break;
						
					item.setExpanded(true);
					item= item.getItems()[n-1];
				}

				// previous
				return item;
			}
		}
		
		// go up
		item= parent;
				
	} else {
		item.setExpanded(true);
		createChildren(item);
		
		if (item.getItemCount() > 0) {
			// has children: go down
			children= item.getItems();
			return children[0];
		}
		
		while (item != null) {
			children= null;
			TreeItem parent= item.getParentItem();
			if (parent != null)
				children= parent.getItems();
			else
				children= item.getParent().getItems();
			
			if (children != null && children.length > 0) {
				// goto next child
				int index= 0;
				for (; index < children.length; index++)
					if (children[index] == item)
						break;
				
				if (index < children.length-1) {
					// next
					return children[index+1];
				}
			}
			
			// go up
			item= parent;
		}
	}
			
	return item;
}

private void internalSetSelection(TreeItem ti, boolean fireOpen) {
	if (ti != null) {
		Object data= ti.getData();
		if (data != null) {
			// Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
			ISelection selection= new StructuredSelection(data);
			setSelection(selection, true);
			ISelection currentSelection= getSelection();
			if (fireOpen && currentSelection != null && selection.equals(currentSelection)) {
				fireOpen(new OpenEvent(this, selection));
			}
		}
	}
}

/*
 * Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
 */ 
private void internalOpen()  {
	ISelection selection= getSelection();
	if (selection != null && !selection.isEmpty()) {
		fireOpen(new OpenEvent(this, selection));
	}
}

/**
 * Creates a new DecoratingLabelProvider (using the passed in ILabelDecorator) and sets
 * it as the label provider for the tree
 * @param decorator
 */
public void setLabelDecorator(ILabelDecorator decorator) {
	setLabelProvider(new DecoratingLabelProvider(new CheckboxDiffViewerLabelProvider(), decorator));
}

}
