package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Variables viewer. As the user steps through code, the
 * variables view renders variables that have not changed
 * in value with a disabled image, thus drawing attention
 * to the values that have changed (which will appear with
 * their appropriate color image).
 */
public class VariablesViewer extends TreeViewer {
	
	/**
	 * Map of disabled images corresponding to enabled
	 * images. Keys are enabled images, and values are
	 * corresponding disabled images.
	 */
	private HashMap fDisabledImages = new HashMap(10);
	
	/**
	 * Map of previous values for variables. Keys
	 * are variables, values are values.
	 */
	private HashMap fPreviousValues = new HashMap(10);

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
	 * Returns the disabled image the corresponds to the given
	 * image, building one if required.
	 * 
	 * @param image enabled image
	 * @return disabled image
	 */
	protected Image getDisabledImage(Image image) {
		Image disabled = (Image)fDisabledImages.get(image);
		if (disabled == null) {
			disabled = new Image(getTree().getDisplay(), image, SWT.IMAGE_DISABLE);
			fDisabledImages.put(image, disabled);
		}
		return disabled;
	}
	
	/**
	 * Dispose disabled images
	 */
	protected void dispose() {
		Iterator images = fDisabledImages.values().iterator();
		while (images.hasNext()) {
			Image image = (Image)images.next();
			image.dispose();
		}
		fDisabledImages.clear();
	}
	
	/**
	 * Resets for a new stack frame.
	 */
	protected void reset() {
		fPreviousValues.clear();
	}
	
	/**
	 * @see Viewer#inputChanged(Object, Object)
	 */
	protected void inputChanged(Object input, Object oldInput) {
		reset();
		super.inputChanged(input, oldInput);
	}

	/**
	 * Refresh the view, and then do another pass to
	 * update images for values that have not changed
	 * since the last refresh. Values that have not
	 * changed are drawn with a disabled image.
	 * 
	 * @see Viewer#refresh()
	 */
	public void refresh() {
		getControl().setRedraw(false);
		super.refresh();
		
		if (isShowingChanges()) {
			Item[] children = getChildren(getControl());
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					updateIcons((TreeItem)children[i]);
				}
			}
		}
		
		getControl().setRedraw(true);
	}
	
	/**
	 * Updates the icon of the given item as well
	 * as all of its children. If the item corresponds
	 * to a variable that has not changed in value,
	 * it is rendered with a disabled image, otherwise
	 * its default image is used.
	 * 
	 * @param item tree item
	 */
	protected void updateIcons(TreeItem item) {
		if (item.getData() instanceof IVariable) {
			IVariable var = (IVariable)item.getData();
			Object prevValue = fPreviousValues.get(var);
			Object currentValue = null;
			try {
				currentValue = var.getValue();
			} catch (DebugException e) {
				DebugUIPlugin.log(e.getStatus());
			}
			if (currentValue != null) {
				fPreviousValues.put(var, currentValue);
			}
			if (prevValue != null && prevValue.equals(currentValue)) {
				Image disabled = getDisabledImage(item.getImage());
				item.setImage(disabled);
			}	
		}
		TreeItem[] children = item.getItems();
		for (int i = 0; i < children.length; i++) {
			updateIcons(children[i]);
		}
	}

	/**
	 * When children are created, we cache their current
	 * values in the previous value cache, such that the
	 * fist time they are drawn with disabled (unchanged)
	 * icons.
	 * 
	 * @see AbstractTreeViewer#createChildren(Widget)
	 */
	protected void createChildren(Widget widget) {
		super.createChildren(widget);
		if (isShowingChanges()) {
			Item[] children = getChildren(widget);
			for (int i = 0; i < children.length; i++) {
				TreeItem treeItem = (TreeItem)children[i];
				Object data = treeItem.getData();
				if (data != null && data instanceof IVariable) {
					try {
						fPreviousValues.put(data, ((IVariable)data).getValue());
						Image image = treeItem.getImage();
						if (image != null) {
							treeItem.setImage(getDisabledImage(image));
						}
					} catch (DebugException e) {
						DebugUIPlugin.log(e.getStatus());
					}
				}
			}
		}
	}

	/**
	 * Returns whether this viewer is displaying
	 * variable changes.
	 * 
	 * @return whether this viewer is displaying
	 *  variable changes
	 */
	protected boolean isShowingChanges() {
		return DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IDebugUIConstants.PREF_SHOW_VARIABLE_VALUE_CHANGES);
	}
}