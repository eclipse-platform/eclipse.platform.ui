package org.eclipse.debug.internal.ui.views.variables;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Variables viewer. As the user steps through code, this
 * viewer renders variables that have changed with a 
 * different foreground color thereby drawing attention
 * to the values that have changed.
 */
public class VariablesViewer extends TreeViewer {

	private Item fNewItem;
	
	/**
	 * Constructor for VariablesViewer.
	 * @param parent
	 */
	public VariablesViewer(Composite parent) {
		super(parent);
	}

	/**
	 * Constructor for VariablesViewer.
	 * @param parent
	 * @param style
	 */
	public VariablesViewer(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Constructor for VariablesViewer.
	 * @param tree
	 */
	public VariablesViewer(Tree tree) {
		super(tree);
	}
	
	/**
	 * Refresh the view, and then do another pass to
	 * update the foreground color for values that have changed
	 * since the last refresh. Values that have not
	 * changed are drawn with the default system foreground color.
	 * If the viewer has no selection, ensure that new items
	 * are visible.
	 * 
	 * @see Viewer#refresh()
	 */
	public void refresh() {
		getControl().setRedraw(false);
		super.refresh();
		
		Item[] children = getChildren(getControl());
		if (children != null) {
			Color c= DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CHANGED_VARIABLE_RGB);
			for (int i = 0; i < children.length; i++) {
				updateColor((TreeItem)children[i], c);
			}
		}
		
		getControl().setRedraw(true);
		
		if (getSelection().isEmpty() && getNewItem() != null) {
			//ensure that new items are visible
			showItem(getNewItem());
			setNewItem(null);
		}
	}
	
	/**
	 * Updates the color of the given item as well
	 * as all of its children. If the item corresponds
	 * to a variable that has changed in value,
	 * it is rendered with the <code>CHANGED_VARIABLE_RGB</code>
	 * generated foreground color, otherwise the default system 
	 * color is used.
	 * 
	 * @param item tree item
	 */
	protected void updateColor(TreeItem item, Color c) {
		if (item.getData() instanceof IVariable) {
			IVariable var = (IVariable)item.getData();
			try {
				if (var.hasValueChanged()) {
					item.setForeground(c);
				} else {
					item.setForeground(null);
				}
			} catch (DebugException e) {
				DebugUIPlugin.log(e);
			}
		}
		TreeItem[] children = item.getItems();
		for (int i = 0; i < children.length; i++) {
			updateColor(children[i], c);
		}
	}
	
	/**
	 * @see AbstractTreeViewer#newItem(Widget, int, int)
	 */
	protected Item newItem(Widget parent, int style, int index) {
		if (index != -1) {
			//ignore the dummy items
			setNewItem(super.newItem(parent, style, index));
			return getNewItem();
		} 
		return	super.newItem(parent, style, index);
	}
	
	protected Item getNewItem() {
		return fNewItem;
	}

	protected void setNewItem(Item newItem) {
		fNewItem = newItem;
	}
}