package org.eclipse.ui.internal;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.OpenPerspectiveMenu;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;
import java.util.*;

/**
 * The PerspectiveContributionItem is the abstract superclass of contribution items
 * that create menus for manipulating the visible perspectives.
 */
public class PerspectiveContributionItem
	extends ContributionItem
	implements SelectionListener {

	/**
	 * The widget created for this item; <code>null</code>
	 * before creation .
	 */
	protected ToolItem widget = null;

	protected IAction action;

	/**
	 * Remembers the parent widget.
	 */
	protected ToolBar parentWidget = null;

	protected IWorkbenchWindow workbenchWindow;

	private IPropertyChangeListener listener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			actionPropertyChange(event);
		}
	};

	class ContributorImageCache {
		/** Map from ImageDescriptor to Entry */
		private Map entries = new HashMap(11);
		private Image missingImage;

		private class Entry {
			Image image;
			Image grayImage;

			void dispose() {
				if (image != null) {
					image.dispose();
					image = null;
				}
				if (grayImage != null) {
					grayImage.dispose();
					grayImage = null;
				}
			}
		}

		/**
		* Dispose of all of the widgets held on to when the receiver is disposed of.
		*/

		void dispose() {
			for (Iterator i = entries.values().iterator(); i.hasNext();) {
				Entry entry = (Entry) i.next();
				entry.dispose();
			}
			entries.clear();
		}
		/*Look up an entry*/

		Entry getEntry(ImageDescriptor desc) {
			Entry entry = (Entry) entries.get(desc);
			if (entry == null) {
				entry = new Entry();
				entries.put(desc, entry);
			}
			return entry;
		}
		/**
		 * Get the grayed version of an image descriptor
		 */

		Image getGrayImage(ImageDescriptor desc) {
			if (desc == null) {
				return null;
			}
			Entry entry = getEntry(desc);
			if (entry.grayImage == null) {
				Image image = getImage(desc);
				if (image != null) {
					entry.grayImage = new Image(null, image, SWT.IMAGE_GRAY);
				}
			}
			return entry.grayImage;
		}
		/**
		* Get the image described by a descriptor
		*/

		Image getImage(ImageDescriptor desc) {
			if (desc == null) {
				return null;
			}
			Entry entry = getEntry(desc);
			if (entry.image == null) {
				entry.image = desc.createImage();
			}
			return entry.image;
		}
		/**
		* Get the default missing image
		*/
		Image getMissingImage() {
			if (missingImage == null) {
				missingImage = getImage(ImageDescriptor.getMissingImageDescriptor());
			}
			return missingImage;
		}
	}
/**
 * Creates a new contribution item from the given action.
 * The id of the action is used as the id of the item.
 *
 */
public PerspectiveContributionItem(
	IWorkbenchWindow window,
	OpenNewAction newAction) {
	super(newAction.getId());
	this.action = newAction;
	this.workbenchWindow = window;
}
/**
 * Handles a property change event on the action (forwarded by nested listener).
 */
private void actionPropertyChange(final PropertyChangeEvent e) {
	// This code should be removed. Avoid using free asyncExec

	if (isVisible() && widget != null) {
		Display display = widget.getDisplay();
		if (display.getThread() == Thread.currentThread()) {
			update(e.getProperty());
		}
		else {
			display.asyncExec(new Runnable() {
				public void run() {
					update(e.getProperty());
				}
			});
		}	

	}	
}
/**
 * The <code>PerspectiveContributionItem</code> implementation of this <code>IContributionItem</code>
 * method creates a SWT ToolItem for the action. 
 */
public void fill(ToolBar parent, int index) {
	if (widget == null && parent != null) {
		int flags = SWT.PUSH;

		ToolItem ti = null;
		if (index >= 0)
			ti = new ToolItem(parent, SWT.PUSH, index);
		else
			ti = new ToolItem(parent, SWT.PUSH);
		ti.setData(this);
		ti.addSelectionListener(this);
		this.action.addPropertyChangeListener(this.listener);

		widget = ti;
		parentWidget = parent;

		update(null);
	}
}
/**
 * Returns the image cache.
 * The cache is kept as data on the parent widget.
 * Can't keep it on this item's widget, since the widget is disposed each time
 * a lazy context menu is popped up.
 * A single image cache is shared by all action contribution items with the same
 * parent widget.
 */
private ContributorImageCache getImageCache() {
	String key = "resourceImageCache";
	ContributorImageCache cache = (ContributorImageCache) parentWidget.getData(key);
	if (cache == null) {
		cache = new ContributorImageCache();
		parentWidget.setData(key, cache);
		// Hook a dispose listener to dispose the images in the cache
		// when parent widget is disposed.
		final ContributorImageCache cacheToDispose = cache;
		parentWidget.addListener(SWT.Dispose, new Listener() {
			public void handleEvent (Event event) {
				cacheToDispose.dispose();
			}
		});
	}
	return cache;
}
/**
 * Pop up the supplied menu at the point where the event occured.
 */
protected void popUpMenu(SelectionEvent event, Menu menu) {

	// Show popup menu.
	Point pt = new Point(event.x, event.y);
	pt = parentWidget.toDisplay(pt);
	menu.setLocation(pt.x, pt.y);
	menu.setVisible(true);
}
/**
 * Synchronizes the UI with the given property.
 *
 * @param propertyName the name of the property, or <code>null</code> meaning all applicable
 *   properties 
 */
public void update(String propertyName) {
	if (widget != null) {

		// determine what to do			
		boolean textChanged = propertyName == null || propertyName.equals(Action.TEXT);
		boolean imageChanged =
			propertyName == null || propertyName.equals(Action.IMAGE);
		boolean tooltipTextChanged =
			propertyName == null || propertyName.equals(Action.TOOL_TIP_TEXT);
		boolean enableStateChanged =
			propertyName == null || propertyName.equals(Action.ENABLED);
		boolean checkChanged =
			(action.getStyle() == IAction.AS_CHECK_BOX)
				&& (propertyName == null || propertyName.equals(Action.CHECKED));

		boolean b = true;
		if (enableStateChanged)
			b = action.isEnabled();

		ToolItem ti = (ToolItem) widget;
		if (imageChanged) {
			updateImages(true);
		}
		if (tooltipTextChanged)
			ti.setToolTipText(action.getToolTipText());

		if (enableStateChanged && ti.getEnabled() != b)
			ti.setEnabled(b);

		if (checkChanged) {
			boolean bv = action.isChecked();
			if (ti.getSelection() != bv)
				ti.setSelection(bv);
		}

	}
}
/**
 * Updates the images for this action.
 *
 * @param forceImage <code>true</code> if some form of image is compulsory,
 *  and <code>false</code> if it is acceptable for this item to have no image
 * @return <code>true</code> if there are images for this action, <code>false</code> if not
 */
private boolean updateImages(boolean forceImage) {

	ContributorImageCache cache = getImageCache();

	Image image = cache.getImage(action.getImageDescriptor());
	Image hoverImage = cache.getImage(action.getHoverImageDescriptor());
	Image disabledImage = cache.getImage(action.getDisabledImageDescriptor());

	// If there is no regular image, but there is a hover image,
	// convert the hover image to gray and use it as the regular image.
	if (image == null && hoverImage != null) {
		image = cache.getGrayImage(action.getHoverImageDescriptor());
	} else {
		// If there is no hover image, use the regular image as the hover image,
		// and convert the regular image to gray
		if (hoverImage == null && image != null) {
			hoverImage = image;
			image = cache.getGrayImage(action.getImageDescriptor());
		}
	}

	// Make sure there is a valid image.
	if (hoverImage == null && image == null && forceImage) {
		image = cache.getMissingImage();
	}

	// performance: more efficient in SWT to set disabled and hot image before regular image
	if (disabledImage != null) {
		// Set the disabled image if we were able to create one.
		// Assumes that SWT.ToolItem will use platform's default
		// behavior to show item when it is disabled and a disabled
		// image has not been set. 
		 ((ToolItem) widget).setDisabledImage(disabledImage);
	}
	((ToolItem) widget).setHotImage(hoverImage);
	((ToolItem) widget).setImage(image);

	return image != null;

}
/**
 * Default implementation ignores double-click.
 */
public void widgetDefaultSelected(SelectionEvent e) {
}
/**
 * Handles selection by populating a menu and popping it up.
 */
public void widgetSelected(SelectionEvent event) {

	Menu menu = new Menu(this.parentWidget);
	OpenPerspectiveMenu menuDescription =
		new OpenPerspectiveMenu(
			this.workbenchWindow,
			ResourcesPlugin.getWorkspace().getRoot());
	menuDescription.fill(menu, 0);
	popUpMenu(event, menu);

}
}
