package org.eclipse.jface.action;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.resource.*;
import org.eclipse.jface.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import java.util.*;

/**
 * A contribution item which delegates to an action.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class ActionContributionItem extends ContributionItem {

	private static ImageCache globalImageCache;
	
	/**
	 * The action.
	 */
	private IAction action;

	/**
	 * The widget created for this item; <code>null</code>
	 * before creation and after disposal.
	 */
	private Widget widget = null;

	/**
	 * Remembers the parent widget.
	 */
	private Widget parentWidget = null;
	
	/**
	 * Nested class handles notification from SWT widget and from Action,
	 * to avoid polluting ActionContributionItem with public listener methods.
	 */
	private class ActionListener implements Listener, IPropertyChangeListener {
		public void handleEvent(Event event) {
			ActionContributionItem.this.handleWidgetEvent(event);
		}
		public void propertyChange(PropertyChangeEvent event) {
			ActionContributionItem.this.actionPropertyChange(event);
		}
	}

	private ActionListener listener = new ActionListener();

	private class ImageCache {
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

		Entry getEntry(ImageDescriptor desc) {
			Entry entry = (Entry) entries.get(desc);
			if (entry == null) {
				entry = new Entry();
				entries.put(desc, entry);
			}
			return entry;
		}

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

		Image getMissingImage() {
			if (missingImage == null) {
				missingImage = getImage(ImageDescriptor.getMissingImageDescriptor());
			}
			return missingImage;
		}
		
		void dispose() {
			for (Iterator i = entries.values().iterator(); i.hasNext();) {
				Entry entry = (Entry) i.next();
				entry.dispose();
			}
			entries.clear();
		}
	}
/**
 * Creates a new contribution item from the given action.
 * The id of the action is used as the id of the item.
 *
 * @param action the action
 */
public ActionContributionItem(IAction action) {
	super(action.getId());
	this.action = action;
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
 * Checks whether the given menu item belongs to a context menu
 * (the one that pops up if the user presses the right mouse button).
 */
private static boolean belongsToContextMenu(MenuItem item) {
	Menu menu = item.getParent();
	if (menu == null)
		return false;
	while (menu.getParentMenu() != null)
		menu = menu.getParentMenu();
	return (menu.getStyle() & SWT.BAR) == 0;
}
/**
 * Compares this action contribution item with another object.
 * Two action contribution items are equal if they refer to the identical Action.
 */
public boolean equals(Object o) {
	if (!(o instanceof ActionContributionItem)) {
		return false;
	}
	return action.equals(((ActionContributionItem) o).action);
}
/**
 * The <code>ActionContributionItem</code> implementation of this <code>IContributionItem</code>
 * method creates a SWT Button for the action. 
 * If the action's checked property has been set, a toggle button is created 
 * and primed to the value of the checked property.
 */
public void fill(Composite parent) {
	if (widget == null && parent != null) {
		int flags = SWT.PUSH;
		
		if (action != null) {
			if (action.getStyle() == IAction.AS_CHECK_BOX)
				flags = SWT.TOGGLE;
		}
		
		Button b = new Button(parent, flags);
		b.setData(this);
		b.addListener(SWT.Dispose, listener);
		// Don't hook a dispose listener on the parent
		b.addListener(SWT.Selection, listener);
		if (action.getHelpListener() != null)
			b.addHelpListener(action.getHelpListener());
		widget = b;
		parentWidget = parent;
		
		update(null);
		
		action.addPropertyChangeListener(listener);
	}
}
/**
 * The <code>ActionContributionItem</code> implementation of this <code>IContributionItem</code>
 * method creates a SWT MenuItem for the action. 
 * If the action's checked property has been set, a toggle button is created 
 * and primed to the value of the checked property.
 * If the action's menu creator property has been set, a cascading submenu is created.
 */
public void fill(Menu parent, int index) {
	if (widget == null && parent != null) {
		int flags = SWT.PUSH;
		Menu subMenu= null;
		
		if (action != null) {
			int style = action.getStyle();
			if (style == IAction.AS_CHECK_BOX)
				flags= SWT.CHECK;
			else if (style == IAction.AS_DROP_DOWN_MENU) {
				IMenuCreator mc = action.getMenuCreator();
				subMenu = mc.getMenu(parent);
				flags = SWT.CASCADE;
			}
		}
		
		MenuItem mi = null;
		if (index >= 0)
			mi = new MenuItem(parent, flags, index);
		else
			mi = new MenuItem(parent, flags);
		widget = mi;
		parentWidget = parent;

		mi.setData(this);
		mi.addListener(SWT.Arm, listener);
		mi.addListener(SWT.Dispose, listener);
		mi.addListener(SWT.Selection, listener);
		if (action.getHelpListener() != null)
			mi.addHelpListener(action.getHelpListener());
		
		if (subMenu != null)
			mi.setMenu(subMenu);
					
		update(null);
		
		action.addPropertyChangeListener(listener);
	}
}
/**
 * The <code>ActionContributionItem</code> implementation of this <code>IContributionItem</code>
 * method creates a SWT ToolItem for the action. 
 * If the action's checked property has been set, a toggle button is created 
 * and primed to the value of the checked property.
 * If the action's menu creator property has been set, a drop-down
 * tool item is created.
 */
public void fill(ToolBar parent, int index) {
	if (widget == null && parent != null) {
		int flags = SWT.PUSH;
		
		if (action != null) {
			int style = action.getStyle();
			if (style == IAction.AS_CHECK_BOX)
				flags = SWT.CHECK;
			else if (style == IAction.AS_DROP_DOWN_MENU)
				flags = SWT.DROP_DOWN;					
		}
		
		ToolItem ti = null;
		if (index >= 0)
			ti = new ToolItem(parent, flags, index);
		else
			ti = new ToolItem(parent, flags);
		ti.setData(this);
		ti.addListener(SWT.Selection, listener);
		ti.addListener(SWT.Dispose, listener);

		widget = ti;
		parentWidget = parent;
		
		update(null);
		
		action.addPropertyChangeListener(listener);
	}
}
/**
 * Returns the action associated with this contribution item.
 *
 * @return the action
 */
public IAction getAction() {
	return action;
}
/**
 * Returns the image cache.
 * The cache is global, and is shared by all action contribution items.
 * This has the disadvantage that once an image is allocated, it is never freed until the display
 * is disposed.  However, it has the advantage that the same image in different contribution managers
 * is only ever created once.
 */
private ImageCache getImageCache() {
	ImageCache cache = globalImageCache;
	if (cache == null) {
		globalImageCache = cache = new ImageCache();
		Display display = Display.getDefault();
		if (display != null) {
			display.disposeExec(new Runnable() {
				public void run() {
					if (globalImageCache != null) {
						globalImageCache.dispose();
						globalImageCache = null;
					}
				}	
			});
		}
	}
	return cache;
}
/** 
 * Handles a widget arm event.
 */
private void handleWidgetArm(Event e) {
	/*
	String description= null;
	if (fAction instanceof Action)	// getDescription should go into IAction
		description= ((Action)fAction).getDescription();
	if (description != null)
		ApplicationWindow.showDescription(e.widget, description);
	else
		ApplicationWindow.resetDescription(e.widget);
	*/
}
/**
 * Handles a widget dispose event for the widget corresponding to this item.
 */
private void handleWidgetDispose(Event e) {
	if (e.widget == widget) {
		// the item is being disposed
		action.removePropertyChangeListener(listener);
		widget = null;
	}
}
/**
 * Handles an event from the widget (forwarded from nested listener).
 */
private void handleWidgetEvent(Event e) {
	switch (e.type) {
		case SWT.Arm:
			handleWidgetArm(e);
			break;
		case SWT.Dispose:
			handleWidgetDispose(e);
			break;
		case SWT.Selection:
			handleWidgetSelection(e);
			break;
	}
}
/**
 * Handles a widget selection event.
 */
private void handleWidgetSelection(Event e) {
	Widget item= e.widget;
	if (item != null) {
		
		int style = item.getStyle();
					
		if ((style & (SWT.TOGGLE | SWT.CHECK)) != 0) {
			if (action.getStyle() == IAction.AS_CHECK_BOX) {
				action.setChecked(!action.isChecked());
			}
			
		} else if ((style & SWT.DROP_DOWN) != 0) {
			if (e.detail == 4) {	// on drop-down button
				if (action.getStyle() == IAction.AS_DROP_DOWN_MENU) {
					IMenuCreator mc = action.getMenuCreator();
					ToolItem ti = (ToolItem) item;
					// we create the menu as a sub-menu of "dummy" so that we can use
					// it in a cascading menu too.
					// If created on a SWT control we would get an SWT error...
					//Menu dummy= new Menu(ti.getParent());
					//Menu m= mc.getMenu(dummy);
					//dummy.dispose();
					
					Menu m= mc.getMenu(ti.getParent());
					if (m != null) {
						// position the menu below the drop down item
						Rectangle b = ti.getBounds();
						Point p = ti.getParent().toDisplay(new Point(b.x, b.y+b.height));
						m.setLocation(p.x, p.y);	// waiting for SWT 0.42
						m.setVisible(true);
						return;	// we don't fire the action
					}
				}
			}
		}

		// Ensure action is enabled first.
		// See 1GAN3M6: ITPUI:WINNT - Any IAction in the workbench can be executed while disabled.
		if (action.isEnabled()) {
			action.runWithEvent(e);
		}
	}
}
/* (non-Javadoc)
 * Method declared on Object.
 */
public int hashCode() {
	return action.hashCode();
}
/**
 * The action item implementation of this <code>IContributionItem</code>
 * method returns <code>true</code> for menu items and <code>false</code>
 * for everything else.
 */
public boolean isDynamic() {
	//actions in menus are always dynamic
	return widget instanceof MenuItem;
}
/**
 * Returns <code>true</code> if this item is allowed to enable,
 * <code>false</code> otherwise.
 * 
 * @return if this item is allowed to be enabled
 * @since 2.0
 */
protected boolean isEnabledAllowed() {
	if (getParent() == null)
		return true;
	return getParent().getOverrides().getEnabledAllowed(this);
}

/**
 * The action item implementation of this <code>IContributionItem</code>
 * method calls <code>update(null)</code>.
 */
public final void update() {
	update(null);
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
		boolean imageChanged = propertyName == null || propertyName.equals(Action.IMAGE);
		boolean tooltipTextChanged = propertyName == null || propertyName.equals(Action.TOOL_TIP_TEXT);
		boolean enableStateChanged = propertyName == null || propertyName.equals(Action.ENABLED) || 
			propertyName.equals(IContributionManagerOverrides.P_ENABLE_ALLOWED);
		boolean checkChanged = (action.getStyle() == IAction.AS_CHECK_BOX) &&
			(propertyName == null || propertyName.equals(Action.CHECKED));
					
		if (widget instanceof ToolItem) {
			ToolItem ti = (ToolItem) widget;
			if (imageChanged) {
				updateImages(true);
			}
			if (tooltipTextChanged)
				ti.setToolTipText(action.getToolTipText());
				
			if (enableStateChanged) {
				boolean shouldBeEnabled = action.isEnabled() && isEnabledAllowed();
				if (ti.getEnabled() != shouldBeEnabled)
					ti.setEnabled(shouldBeEnabled);
			}
				
			if (checkChanged) {
				boolean bv = action.isChecked();
				if (ti.getSelection() != bv)
					ti.setSelection(bv);
			}
			return;
		}
		
		if (widget instanceof MenuItem) {
			MenuItem mi = (MenuItem) widget;
			boolean isContextMenu = belongsToContextMenu(mi);
			
			if (textChanged) {
				String text = action.getText();
				if (text != null) {
					boolean accAllowed = MenuManager.getAcceleratorsAllowed(mi.getParent());
					if (isContextMenu || !accAllowed)
						text = Action.removeAcceleratorText(text);
					else {
						// We only install an accelerator if the menu item doesn't
						// belong to a context menu (right mouse button menu).
						int acc = action.getAccelerator();
						if (acc > 0)
							mi.setAccelerator(acc);
						text = text.replace('@', '\t');
					}
					mi.setText(text);
				}
			}
			if (imageChanged) {
				updateImages(false);
			}
			if (enableStateChanged) {
				boolean shouldBeEnabled = action.isEnabled() && isEnabledAllowed();
				if (mi.getEnabled() != shouldBeEnabled) 
					mi.setEnabled(shouldBeEnabled);
			}
	
			if (checkChanged) {	
				boolean bv = action.isChecked();
				if (mi.getSelection() != bv)
					mi.setSelection(bv);
			}
			return;
		}

		if (widget instanceof Button) {
			Button button= (Button) widget;
			if (imageChanged) {
				if (updateImages(false)) {
					// don't update text if it has an image
					textChanged = false;
				}
			}
			if (textChanged) {
				String text = action.getText();
				if (text != null)
					button.setText(text);
			}
			if (tooltipTextChanged)
				button.setToolTipText(action.getToolTipText());
				
			if (enableStateChanged) {
				boolean shouldBeEnabled = action.isEnabled() && isEnabledAllowed();
				if (button.getEnabled() != shouldBeEnabled)
					button.setEnabled(shouldBeEnabled);
			}
				
			if (checkChanged) {
				boolean bv = action.isChecked();
				if (button.getSelection() != bv)
					button.setSelection(bv);
			}
			return;
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

	ImageCache cache = getImageCache();

	if (widget instanceof ToolItem) {
		Image image = cache.getImage(action.getImageDescriptor());
		Image hoverImage = cache.getImage(action.getHoverImageDescriptor());
		Image disabledImage = cache.getImage(action.getDisabledImageDescriptor());

		// If there is no regular image, but there is a hover image,
		// convert the hover image to gray and use it as the regular image.
		if (image == null && hoverImage != null) { 
			image = cache.getGrayImage(action.getHoverImageDescriptor());
		}
		else {
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
	else if (widget instanceof Item || widget instanceof Button) {
		// Use hover image if there is one, otherwise use regular image.
		Image image = cache.getImage(action.getHoverImageDescriptor());
		if (image == null) {
			image = cache.getImage(action.getImageDescriptor());
		}
		// Make sure there is a valid image.
		if (image == null && forceImage) {
			image = cache.getMissingImage();
		}
		if (widget instanceof Item) {
			((Item) widget).setImage(image);
		}
		else if (widget instanceof Button) {
			((Button) widget).setImage(image);
		}
		return image != null;
	}
	return false;
}
}
