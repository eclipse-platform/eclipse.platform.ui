/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.commands.ws;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.ExternalActionManager;
import org.eclipse.jface.action.IContributionManagerOverrides;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.commands.CommandEvent;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandListener;
import org.eclipse.ui.commands.NotDefinedException;
import org.eclipse.ui.commands.NotHandledException;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * <p>
 * A contribution item which delegates to a command. This is a contribution
 * item that just passes as much of the complexity as it can on to the
 * underlying command.
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 3.0
 */
public class CommandContributionItem
	extends ContributionItem
	implements ICommandListener {

	/**
	 * A cache of images loaded by the command contribution item.
	 */
	private static class ImageCache {
		/**
		 * An entry in the cache for tracking images.
		 */
		private class Entry {
			/** The greyed representation of the image. */
			Image grayImage;
			/** The plain image (unmodified). */
			Image image;

			/** Disposes this entry by releasing its native resources, if any. */
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

		/** Map from ImageDescriptor to Entry */
		private Map entries = new HashMap(11);
		/** The image used to represent an image that is not available. */
		private Image missingImage;

		/** Disposes the cache by calling dispose an all of its entries. */
		void dispose() {
			for (Iterator i = entries.values().iterator(); i.hasNext();) {
				((Entry) i.next()).dispose();
			}
			entries.clear();
		}

		/**
		 * If the entry is already in the cache, then it simply returns a
		 * reference. Otherwise, it creates a new entry in the cache, and
		 * returns a reference to that.
		 * 
		 * @param descriptor
		 *            The descriptor to look up in the cache; should not be
		 *            <code>null</code>.
		 * @return The entry for that descriptor; never <code>null</code>.
		 */
		Entry getEntry(ImageDescriptor descriptor) {
			Entry entry = (Entry) entries.get(descriptor);

			if (entry == null) {
				entry = new Entry();
				entries.put(descriptor, entry);
			}

			return entry;
		}

		/**
		 * Retrieves a greyed representation of the image of the corresponding
		 * URI from the cache. If the image is not in the cache, then it tries
		 * to load the image. If the image URI is invalid, then it returns the
		 * missing image.
		 * 
		 * @param imageURI
		 *            The URI at which the image is located; may be <code>null</code>
		 * @return The image at the URI, or the missing image if it is invalid.
		 *         If the URI is <code>null</code>, then this returns <code>null</code>
		 *         as well.
		 */
		Image getGrayImage(String imageURI) {
			if (imageURI == null) {
				return null;
			}

			try {
				ImageDescriptor descriptor =
					ImageDescriptor.createFromURL(new URL(imageURI));
				Entry entry = getEntry(descriptor);
				if (entry.grayImage == null) {
					Image image = getImage(imageURI);
					if (image != null) {
						entry.grayImage =
							new Image(null, image, SWT.IMAGE_GRAY);
					}
				}

				return entry.grayImage;

			} catch (MalformedURLException e) {
				return new Image(null, getMissingImage(), SWT.IMAGE_GRAY);
			}
		}

		/**
		 * Retrieves the image of the corresponding URI from the cache. If the
		 * image is not in the cache, then it tries to load the image. If the
		 * image URI is invalid, then it returns the missing image.
		 * 
		 * @param imageURI
		 *            The URI at which the image is located; may be <code>null</code>
		 * @return The image at the URI, or the missing image if it is invalid.
		 *         If the URI is <code>null</code>, then this returns <code>null</code>
		 *         as well.
		 */
		Image getImage(String imageURI) {
			if (imageURI == null) {
				return null;
			}

			try {
				ImageDescriptor descriptor =
					ImageDescriptor.createFromURL(new URL(imageURI));
				Entry entry = getEntry(descriptor);
				if (entry.image == null) {
					entry.image = descriptor.createImage();
				}

				return entry.image;

			} catch (MalformedURLException e) {
				return getMissingImage();
			}
		}

		/**
		 * Retrieves the missing image from the cache. If the image isn't
		 * available, then it loads it into the cache.
		 * 
		 * @return The missing image; never <code>null</code>.
		 */
		Image getMissingImage() {
			if (missingImage == null) {
				ImageDescriptor descriptor =
					ImageDescriptor.getMissingImageDescriptor();
				Entry entry = getEntry(descriptor);
				if (entry.image == null) {
					entry.image = descriptor.createImage();
				}
				missingImage = entry.image;
			}

			return missingImage;
		}
	}

	/** The global cache of images used by this contribution item. */
	private static ImageCache globalImageCache;
	/** Whether command contribution items should use colour icons. */
	private static boolean useColourIcons = true;

	/**
	 * Checks whether the given menu item belongs to a context menu (the one
	 * that pops up if the user presses the right mouse button).
	 */
	//	private static boolean belongsToContextMenu(MenuItem item) {
	//		Menu menu = item.getParent();
	//		if (menu == null)
	//			return false;
	//		while (menu.getParentMenu() != null)
	//			menu = menu.getParentMenu();
	//		return (menu.getStyle() & SWT.BAR) == 0;
	//	}

	/**
	 * Returns whether color icons should be used in toolbars.
	 * 
	 * @return <code>true</code> if color icons should be used in toolbars;
	 *         <code>false</code> otherwise.
	 */
	public static boolean getUseColourIconsInToolbars() {
		return useColourIcons;
	}

	/**
	 * Convenience method for removing any optional accelerator text from the
	 * given string. The accelerator text appears at the end of the text, and
	 * is separated from the main part by a single tab character <code>'\t'</code>.
	 * 
	 * @param text
	 *            The text to be stripped; must not be <code>null</code>.
	 * @return The text sans accelerator; never <code>null</code>.
	 */
	public static String removeAcceleratorText(String text) {
		int index = text.lastIndexOf('\t');
		if (index == -1)
			index = text.lastIndexOf('@');
		if (index >= 0)
			return text.substring(0, index);
		return text;
	}

	/**
	 * Sets whether color icons should be used in toolbars.
	 * 
	 * @param newValue
	 *            <code>true</code> if color icons should be used in
	 *            toolbars, <code>false</code> otherwise
	 */
	public static void setUseColourIconsInToolbars(boolean newValue) {
		useColourIcons = newValue;
	}

	/** Listener for SWT button widget events. */
	private Listener buttonListener;
	/** The command that this contribution item represents. */
	private ICommand command;
	/** The help listener for the command. */
	private HelpListener helpListener;
	/** Listener for SWT menu item widget events. */
	private Listener menuItemListener;
	/** Listener for SWT tool item widget events. */
	private Listener toolItemListener;
	/**
	 * The widget created for this item; <code>null</code> before creation
	 * and after disposal.
	 */
	private Widget widget = null;

	/**
	 * Creates a new contribution item from the given command. The id of the
	 * command is used as the id of the item.
	 * 
	 * @param command
	 *            The command from which this contribution item should be
	 *            constructed; must not be <code>null</code>.
	 */
	public CommandContributionItem(ICommand commandToUse) {
		super(commandToUse.getId());
		command = commandToUse;
		helpListener = WorkbenchHelp.createHelpListener(commandToUse);
	}

	/**
	 * Handles a change event on the command. This performs an update of the
	 * underlying widget to reflect the change.
	 * 
	 * @param e
	 *            The triggering event; must not be <code>null</code>.
	 */
	public void commandChanged(final CommandEvent e) {
		// This code should be removed. Avoid using free asyncExec

		if (isVisible() && widget != null) {
			Display display = widget.getDisplay();
			if (display.getThread() == Thread.currentThread()) {
				update(e);
			} else {
				display.asyncExec(new Runnable() {
					public void run() {
						update(e);
					}
				});
			}

		}
	}

	/**
	 * Compares this command contribution item with another object. Two command
	 * contribution items are equal if they refer to the equivalent command.
	 * 
	 * @param o
	 *            The object with which to compare; may be <code>null</code>.
	 */
	public boolean equals(Object o) {
		if (!(o instanceof CommandContributionItem)) {
			return false;
		}
		return command.equals(((CommandContributionItem) o).command);
	}

	/**
	 * The <code>CommandContributionItem</code> implementation of this <code>IContributionItem</code>
	 * method creates an SWT <code>Button</code> for the command using the
	 * command's style. If the command's checked property has been set, the
	 * button is created and primed to the value of the checked property.
	 * 
	 * @param parent
	 *            The composite parent which this contribution should place
	 *            itself on; must not be <code>null</code>.
	 */
	public void fill(Composite parent) {
		if (widget == null && parent != null) {
			int flags = SWT.PUSH;
			if (command != null) {
				// TODO STYLE
				// if (action.getStyle() == IAction.AS_CHECK_BOX)
				//	flags = SWT.TOGGLE;
				//if (action.getStyle() == IAction.AS_RADIO_BUTTON)
				//	flags = SWT.RADIO;
			}

			Button b = new Button(parent, flags);
			b.setData(this);
			b.addListener(SWT.Dispose, getButtonListener());
			// Don't hook a dispose listener on the parent.
			b.addListener(SWT.Selection, getButtonListener());
			b.addHelpListener(helpListener);
			widget = b;

			update();

			command.addCommandListener(this);
		}
	}

	/**
	 * The <code>CommandContributionItem</code> implementation of this <code>IContributionItem</code>
	 * method creates an SWT <code>MenuItem</code> for the action using the
	 * command's style. If the command's checked property has been set, a
	 * button is created and primed to the value of the checked property. If
	 * the command's menu creator property has been set, a cascading submenu is
	 * created.
	 * 
	 * @param parent
	 *            The menu on which this contribution item should place itself;
	 *            must not be <code>null</code>.
	 * @param index
	 *            The index at which this contribution item should place
	 *            itself. If it is a negative number, then this simply appends
	 *            the item.
	 */
	public void fill(Menu parent, int index) {
		if (widget == null && parent != null) {
			Menu subMenu = null;
			int flags = SWT.PUSH;
			if (command != null) {
				// TODO STYLE
				//int style = action.getStyle();
				//if (style == IAction.AS_CHECK_BOX)
				//	flags = SWT.CHECK;
				//else if (style == IAction.AS_RADIO_BUTTON)
				//	flags = SWT.RADIO;
				//else if (style == IAction.AS_DROP_DOWN_MENU) {
				//	IMenuCreator mc = action.getMenuCreator();
				//	if (mc != null) {
				//		subMenu = mc.getMenu(parent);
				//		flags = SWT.CASCADE;
				//	}
				//}
			}

			MenuItem mi = null;
			if (index >= 0)
				mi = new MenuItem(parent, flags, index);
			else
				mi = new MenuItem(parent, flags);
			widget = mi;

			mi.setData(this);
			mi.addListener(SWT.Dispose, getMenuItemListener());
			mi.addListener(SWT.Selection, getMenuItemListener());
			mi.addHelpListener(helpListener);

			if (subMenu != null)
				mi.setMenu(subMenu);

			update();

			command.addCommandListener(this);
		}
	}

	/**
	 * The <code>CommandContributionItem</code> implementation of this <code>IContributionItem</code>
	 * method creates an SWT <code>ToolItem</code> for the command using the
	 * command's style. If the command's checked property has been set, a
	 * button is created and primed to the value of the checked property. If
	 * the command's menu creator property has been set, a drop-down tool item
	 * is created.
	 * 
	 * @param parent
	 *            The tool bar on which this contribution item should place
	 *            itself; must not be <code>null</code>.
	 * @param index
	 *            The index at which this contribution item should place
	 *            itself. If it is a negative number, then this simply appends
	 *            the item.
	 */
	public void fill(ToolBar parent, int index) {
		if (widget == null && parent != null) {
			int flags = SWT.PUSH;
			if (command != null) {
				// TODO STYLE
				//int style = action.getStyle();
				//if (style == IAction.AS_CHECK_BOX)
				//	flags = SWT.CHECK;
				//else if (style == IAction.AS_RADIO_BUTTON)
				//	flags = SWT.RADIO;
				//else if (style == IAction.AS_DROP_DOWN_MENU)
				//	flags = SWT.DROP_DOWN;
			}

			ToolItem ti = null;
			if (index >= 0)
				ti = new ToolItem(parent, flags, index);
			else
				ti = new ToolItem(parent, flags);
			ti.setData(this);
			ti.addListener(SWT.Selection, getToolItemListener());
			ti.addListener(SWT.Dispose, getToolItemListener());

			widget = ti;

			update();

			command.addCommandListener(this);
		}
	}

	/**
	 * Returns the listener for SWT button widget events. This lazy initializes
	 * the listener.
	 * 
	 * @return A listener for button events; never <code>null</code>.
	 */
	private Listener getButtonListener() {
		if (buttonListener == null) {
			buttonListener = new Listener() {
				public void handleEvent(Event event) {
					switch (event.type) {
						case SWT.Dispose :
							handleWidgetDispose(event);
							break;
						case SWT.Selection :
							Widget ew = event.widget;
							if (ew != null) {
								handleWidgetSelection(
									event,
									((Button) ew).getSelection());
							}
							break;
					}
				}
			};
		}
		return buttonListener;
	}

	/**
	 * Returns the command associated with this contribution item.
	 * 
	 * @return The associated command; never <code>null</code>.
	 */
	public ICommand getCommand() {
		return command;
	}

	/**
	 * Returns the image cache. The cache is global, and is shared by all
	 * command contribution items. This has the disadvantage that once an image
	 * is allocated, it is never freed until the display is disposed. However,
	 * it has the advantage that the same image in different contribution
	 * managers is only ever created once.
	 * 
	 * @param The
	 *            global image cache for command contribution items; never
	 *            <code>null</code>.
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
	 * Returns the listener for SWT menu item widget events. The listener is
	 * lazy initialized when this method is first called.
	 * 
	 * @return A listener for menu item events; never <code>null</code>.
	 */
	private Listener getMenuItemListener() {
		if (menuItemListener == null) {
			menuItemListener = new Listener() {
				public void handleEvent(Event event) {
					switch (event.type) {
						case SWT.Dispose :
							handleWidgetDispose(event);
							break;
						case SWT.Selection :
							Widget ew = event.widget;
							if (ew != null) {
								handleWidgetSelection(
									event,
									((MenuItem) ew).getSelection());
							}
							break;
					}
				}
			};
		}
		return menuItemListener;
	}

	/**
	 * Returns the listener for SWT tool item widget events. The listener is
	 * lazy initialized when this method is first called.
	 * 
	 * @return A listener for tool item events; never <code>null</code>.
	 */
	private Listener getToolItemListener() {
		if (toolItemListener == null) {
			toolItemListener = new Listener() {
				public void handleEvent(Event event) {
					switch (event.type) {
						case SWT.Dispose :
							handleWidgetDispose(event);
							break;
						case SWT.Selection :
							Widget ew = event.widget;
							if (ew != null) {
								handleWidgetSelection(
									event,
									((ToolItem) ew).getSelection());
							}
							break;
					}
				}
			};
		}
		return toolItemListener;
	}

	/**
	 * Handles a widget dispose event for the widget corresponding to this
	 * item. This detaches the command listener, and disposes the menu creator
	 * if there is one.
	 * 
	 * @param e
	 *            The triggering dispose event; must not be <code>null</code>.
	 */
	private void handleWidgetDispose(Event e) {
		if (e.widget == widget) {
			// the item is being disposed
			// TODO STYLE
			//if (action.getStyle() == IAction.AS_DROP_DOWN_MENU) {
			//	IMenuCreator mc = action.getMenuCreator();
			//	if (mc != null) {
			//		mc.dispose();
			//	}
			//}
			command.removeCommandListener(this);
			widget = null;
		}
	}

	/**
	 * Handles a widget selection event.
	 * 
	 * @param e
	 *            The triggering selection event; must not be <code>null</code>
	 * @param selection
	 *            Whether the item is becoming selected (as opposed to
	 *            de-selected).
	 */
	private void handleWidgetSelection(Event e, boolean selection) {
		Widget item = e.widget;
		if (item != null) {
			// TODO STYLE
			//int style = item.getStyle();
			//
			//if ((style & (SWT.TOGGLE | SWT.CHECK)) != 0) {
			//	if (action.getStyle() == IAction.AS_CHECK_BOX) {
			//		action.setChecked(selection);
			//	}
			//} else if ((style & SWT.RADIO) != 0) {
			//	if (action.getStyle() == IAction.AS_RADIO_BUTTON) {
			//		action.setChecked(selection);
			//	}
			//} else if ((style & SWT.DROP_DOWN) != 0) {
			//	if (e.detail == 4) { // on drop-down button
			//		if (action.getStyle() == IAction.AS_DROP_DOWN_MENU) {
			//			IMenuCreator mc = action.getMenuCreator();
			//			ToolItem ti = (ToolItem) item;
			//			// we create the menu as a sub-menu of "dummy" so that
			//			// we can use
			//			// it in a cascading menu too.
			//			// If created on a SWT control we would get an SWT
			//			// error...
			//			//Menu dummy= new Menu(ti.getParent());
			//			//Menu m= mc.getMenu(dummy);
			//			//dummy.dispose();
			//			if (mc != null) {
			//				Menu m = mc.getMenu(ti.getParent());
			//				if (m != null) {
			//					// position the menu below the drop down item
			//					Rectangle b = ti.getBounds();
			//					Point p = ti.getParent().toDisplay(new Point(b.x, b.y +
			// b.height));
			//					m.setLocation(p.x, p.y); // waiting for SWT
			//					// 0.42
			//					m.setVisible(true);
			//					return; // we don't fire the action
			//				}
			//			}
			//		}
			//	}
			//}

			// Ensure command is enabled first.
			// See 1GAN3M6: ITPUI:WINNT - Any IAction in the workbench can be
			// executed while disabled.
		    
			if (isEnabled(command)) {
				boolean trace = "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.jface/trace/actions")); //$NON-NLS-1$ //$NON-NLS-2$
				long ms = System.currentTimeMillis();
				try {
					if (trace)
						System.out.println("Running command: " + command.getName()); //$NON-NLS-1$

					// TODO Dispatching commands
					//command.runWithEvent(e);

					if (trace)
						System.out.println((System.currentTimeMillis() - ms) + " ms to run command: " + command.getName()); //$NON-NLS-1$
				} catch (NotDefinedException nde) {
					// TODO Warn the user that the command is now gone.
					update(); // update the GUI
				}
			}
		}
	}

	/*
	 * (non-Javadoc) Method declared on Object.
	 */
	public int hashCode() {
		return command.hashCode();
	}

	/**
	 * The command item implementation of this <code>IContributionItem</code>
	 * method returns <code>true</code> for menu items and <code>false</code>
	 * for everything else.
	 */
	public boolean isDynamic() {
		if (widget instanceof MenuItem) {
			//Optimization. Only recreate the item is the check or radio style
			// has changed.
			// TODO STYLE
			boolean itemIsCheck = (widget.getStyle() & SWT.CHECK) != 0;
			//boolean actionIsCheck = getAction() != null &&
			// getAction().getStyle() == IAction.AS_CHECK_BOX;
			boolean itemIsRadio = (widget.getStyle() & SWT.RADIO) != 0;
			//boolean actionIsRadio = getAction() != null &&
			// getAction().getStyle() == IAction.AS_RADIO_BUTTON;
			//return (itemIsCheck != actionIsCheck) || (itemIsRadio !=
			// actionIsRadio);
			return false;
		}

		return false;
	}

	/*
	 * (non-Javadoc) Method declared on IContributionItem.
	 */
	public boolean isEnabled() {
		return isEnabled(command);
	}

	private static boolean isEnabled(ICommand command) {
	    try {
	        Map attributeValuesByName = command.getAttributeValuesByName();

	        if (attributeValuesByName.containsKey("enabled") //$NON-NLS-1$
	                && !Boolean.TRUE.equals(attributeValuesByName.get("enabled"))) //$NON-NLS-1$
	            return false;
	        else
	            return true;
	    } catch (NotHandledException eNotHandled) {		        
	        return false;
	    } 
	}    	
	
	/**
	 * Returns <code>true</code> if this item is allowed to enable, <code>false</code>
	 * otherwise.
	 * 
	 * @return If this item is allowed to be enabled
	 * @since 2.0
	 */
	protected boolean isEnabledAllowed() {
		if (getParent() == null)
			return true;
		Boolean value = getParent().getOverrides().getEnabled(this);
		return (value == null) ? true : value.booleanValue();
	}

	/**
	 * Whether this contribution item should be visible.
	 * 
	 * @return <code>true</code> if the command is active; <code>false</code>
	 *         otherwise.
	 */
	public boolean isVisible() {
		return true; // TODO visiblity should consider the activity and context managers.
	}

	/**
	 * The command item implementation of this <code>IContributionItem</code>
	 * method calls <code>update(null)</code>.
	 */
	public void update() {
		update((CommandEvent) null);
	}

	/**
	 * Synchronizes the UI with the given property.
	 * 
	 * @param event
	 *            The event triggering the update (which specifies how much of
	 *            the command changed). If <code>null</code>, then
	 *            everything is updated.
	 */
	public void update(CommandEvent event) {
		if (widget != null) {
			ICommand currentCommand = getCommand();

			// Determine what to do
			boolean descriptionChanged = true;
			boolean nameChanged = true;
			boolean enabledChanged = true;
			boolean checkedChanged = true;
			if (event != null) {
				descriptionChanged = event.hasDescriptionChanged();
				nameChanged = event.hasNameChanged();
				// TODO Enable change notification
				//enabledChanged = event.hasEnabledChanged();
				// TODO Checked state notification
				//checkedChanged = event.hasSelectionChanged();

				if ((event.hasDefinedChanged() && !currentCommand.isDefined())
					/* TODO || (event.hasActiveChanged() && !currentCommand.isActive())*/) {
					// TODO Dispose of the item?
				}
			}

			try {
				// Update the widget as a ToolItem
				if (widget instanceof ToolItem) {
					ToolItem ti = (ToolItem) widget;

					if (descriptionChanged)
						ti.setToolTipText(currentCommand.getDescription());

					if (enabledChanged) {
						boolean shouldBeEnabled =
							isEnabled(currentCommand) && isEnabledAllowed();

						if (ti.getEnabled() != shouldBeEnabled)
							ti.setEnabled(shouldBeEnabled);
					}

					if (checkedChanged) {
						// TODO Selection state
						//boolean bv = command.isChecked();
						//
						//if (ti.getSelection() != bv)
						//	ti.setSelection(bv);
					}

					return;
				}

				// Update the widget as a MenuItem
				if (widget instanceof MenuItem) {
					MenuItem mi = (MenuItem) widget;

					if (nameChanged) {
						Integer accelerator = null;
						String acceleratorText = null;
						String name = null;

						ExternalActionManager.ICallback callback =
							ExternalActionManager.getInstance().getCallback();
						if (callback != null) {
							String commandId = currentCommand.getId();
							if (commandId != null) {
								accelerator =
									callback.getAccelerator(commandId);
								acceleratorText =
									callback.getAcceleratorText(commandId);
							}
						}

						IContributionManagerOverrides overrides = null;

						if (getParent() != null)
							overrides = getParent().getOverrides();

						if (overrides != null)
							name = getParent().getOverrides().getText(this);

						//
						//if (accelerator == null)
						//	// TODO Not necessary?
						//	accelerator = new Integer(command.getAccelerator());

						mi.setAccelerator(accelerator.intValue());

						if (name == null)
							name = currentCommand.getName();

						if (name == null)
							name = ""; //$NON-NLS-1$
						else
							name = removeAcceleratorText(name);

						if (acceleratorText == null)
							mi.setText(name);
						else
							mi.setText(name + '\t' + acceleratorText);
					}

					if (enabledChanged) {
						boolean shouldBeEnabled =
							isEnabled(currentCommand) && isEnabledAllowed();

						if (mi.getEnabled() != shouldBeEnabled)
							mi.setEnabled(shouldBeEnabled);
					}

					if (checkedChanged) {
						// TODO Selection state needed.
						//boolean bv = command.isChecked();
						//
						//if (mi.getSelection() != bv)
						//	mi.setSelection(bv);
					}

					return;
				}

				// Update the widget as a button.
				if (widget instanceof Button) {
					Button button = (Button) widget;

					if (nameChanged) {
						String name = currentCommand.getName();

						if (name != null)
							button.setText(name);
					}

					if (descriptionChanged)
						button.setToolTipText(currentCommand.getDescription());

					if (enabledChanged) {
						boolean shouldBeEnabled =
							isEnabled(currentCommand) && isEnabledAllowed();

						if (button.getEnabled() != shouldBeEnabled)
							button.setEnabled(shouldBeEnabled);
					}

					if (checkedChanged) {
						// TODO Selection state needed.
						//boolean bv = action.isChecked();
						//
						//if (button.getSelection() != bv)
						//	button.setSelection(bv);
					}

					return;
				}
			} catch (NotDefinedException e) {
				/*
				 * This shouldn't happen very often. It can only happen in a
				 * multi-threaded environment where a command becomes undefined
				 * on one thread while another thread is attempting an update.
				 */

				// TODO Dispose of the item.
			}
		}
	}
}
