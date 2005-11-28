/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.model.ModelProviderBasedEditorOpenStrategy;
import org.eclipse.ui.internal.provisional.ide.IEditorOpenStrategy;
import org.eclipse.ui.internal.provisional.ide.OpenWithEntry;
import org.eclipse.ui.internal.provisional.ide.OpenWithInfo;

/**
 * A menu for opening files in the workbench.
 * <p>
 * An <code>OpenWithMenu</code> is used to populate a menu with "Open With"
 * actions. One action is added for each editor which is applicable to the
 * selected file. If the user selects one of these items, the corresponding
 * editor is opened on the file.
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class OpenWithMenu extends ContributionItem {
	private IWorkbenchPage page;

	private Object element;

	private IEditorOpenStrategy strategy = new ModelProviderBasedEditorOpenStrategy();

	private static Hashtable imageCache = new Hashtable(11);

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".OpenWithMenu";//$NON-NLS-1$

	/*
	 * Compares the labels from two IEditorDescriptor objects
	 */
	private static final Comparator comparer = new Comparator() {
		private Collator collator = Collator.getInstance();

		public int compare(Object arg0, Object arg1) {
			String s1 = ((OpenWithEntry) arg0).getLabel();
			String s2 = ((OpenWithEntry) arg1).getLabel();
			return collator.compare(s1, s2);
		}
	};

	/**
	 * Constructs a new instance of <code>OpenWithMenu</code>.
	 * <p>
	 * If this method is used be sure to set the selected file by invoking
	 * <code>setFile</code>. The file input is required when the user selects
	 * an item in the menu. At that point the menu will attempt to open an
	 * editor with the file as its input.
	 * </p>
	 * 
	 * @param page
	 *            the page where the editor is opened if an item within the menu
	 *            is selected
	 */
	public OpenWithMenu(IWorkbenchPage page) {
		this(page, null);
	}

	/**
	 * Constructs a new instance of <code>OpenWithMenu</code>.
	 * 
	 * @param page
	 *            the page where the editor is opened if an item within the menu
	 *            is selected
	 * @param file
	 *            the selected file
	 */
	public OpenWithMenu(IWorkbenchPage page, IAdaptable file) {
		this(page, (Object) file);
	}

	/**
	 * Constructs a new instance of <code>OpenWithMenu</code>.
	 * 
	 * @param page
	 *            the page where the editor is opened if an item within the menu
	 *            is selected
	 * @param element
	 *            the selected model element
	 * @since 3.2
	 */
	public OpenWithMenu(IWorkbenchPage page, Object element) {
		super(ID);
		this.page = page;
		this.element = element;
	}
	
	/**
	 * Returns an image to show for the corresponding entry.
	 * 
	 * @param entry the open with entry
	 * @return the image or null
	 */
	private Image getImage(OpenWithEntry entry) {
		ImageDescriptor imageDesc = entry.getImageDescriptor();
		if (imageDesc == null) {
			return null;
		}
		Image image = (Image) imageCache.get(imageDesc);
		if (image == null) {
			image = imageDesc.createImage();
			imageCache.put(imageDesc, image);
		}
		return image;
	}

	/**
	 * Creates the menu item for the entry.
	 * 
	 * @param menu
	 *            the menu to add the item to
	 * @param entry
	 *            the entry
	 * @param isPreferred whether the entry is the preferred one
	 */
	private void createMenuItem(Menu menu, final OpenWithEntry entry,
			boolean isPreferred) {
		// XXX: Would be better to use bold here, but SWT does not support it.
		final MenuItem menuItem = new MenuItem(menu, SWT.NONE);
		if (isPreferred) {
			menu.setDefaultItem(menuItem);
		}
		menuItem.setText(entry.getLabel());
		Image image = getImage(entry);
		if (image != null) {
			menuItem.setImage(image);
		}
		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.Selection:
					openEditor(entry, true);
					break;
				}
			}
		};
		menuItem.addListener(SWT.Selection, listener);
	}

	/**
	 * Creates the menu item for "Default", which picks the default associated
	 * with the file type, clearing any editor associated with the file itself
	 * (i.e. remembered from the last Open With).
	 * 
	 * @param menu
	 *            the menu to add the item to
	 * @param entry the entry
	 */
	private void createDefaultMenuItem(Menu menu, final OpenWithEntry entry) {
		final MenuItem menuItem = new MenuItem(menu, SWT.NONE);
		menuItem.setText(IDEWorkbenchMessages.DefaultEditorDescription_name);

		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.Selection:
					openEditor(entry, false);
					break;
				}
			}
		};

		menuItem.addListener(SWT.Selection, listener);
	}

	/*
	 * (non-Javadoc) Fills the menu with perspective items.
	 */
	public void fill(Menu menu, int index) {
		Object element = getElement();
		if (element == null) {
			return;
		}

		OpenWithInfo info = strategy.getOpenWithInfo(element);
		if (info == null) {
			return;
		}

		OpenWithEntry preferredEntry = info.getPreferredEntry(); // may be null
		OpenWithEntry[] entries = info.getEntries();
		Collections.sort(Arrays.asList(entries), comparer);

		menu.setDefaultItem(null);
		for (int i = 0; i < entries.length; i++) {
			OpenWithEntry entry = entries[i];
			createMenuItem(menu, entry, entry.equals(preferredEntry));
		}

		// Only add a separator if there is something to separate
		if (entries.length > 0)
			new MenuItem(menu, SWT.SEPARATOR);

		// Add entry for system external editor 
		if (info.getExternalEntry() != null) {
			createMenuItem(menu, info.getExternalEntry(), info.getExternalEntry().equals(preferredEntry));
		}

		// Add entry for system in-place editor 
		if (info.getInPlaceEntry() != null) {
			createMenuItem(menu, info.getInPlaceEntry(), info.getInPlaceEntry().equals(preferredEntry));
		}
		
		// Add entry for default 
		if (info.getDefaultEntry() != null) {
			createDefaultMenuItem(menu, info.getDefaultEntry());
		}
	}

	/**
	 * Converts the model element.
	 */
	private Object getElement() {
		return element;
	}

	/*
	 * (non-Javadoc) Returns whether this menu is dynamic.
	 */
	public boolean isDynamic() {
		return true;
	}

	/**
	 * Opens the editor for the given entry.
	 * 
	 * @param entry the entry
	 * @param rememberEditor whether to remember the editor with the element
	 */
	private void openEditor(OpenWithEntry entry, boolean rememberEditor) {
		final int MATCH_BOTH = IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID;
		try {
			entry.openEditor(page, true, MATCH_BOTH, rememberEditor);
		} catch (PartInitException e) {
			DialogUtil.openError(page.getWorkbenchWindow().getShell(),
					IDEWorkbenchMessages.OpenWithMenu_dialogTitle, e
							.getMessage(), e);
		}
	}

}
