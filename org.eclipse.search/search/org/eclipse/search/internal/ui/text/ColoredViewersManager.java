/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 * Copied from JDT UI: org.eclipse.jdt.internal.ui.viewsupport.ColoredViewersManager.
 * Will be removed again when made API. https://bugs.eclipse.org/bugs/show_bug.cgi?id=196128
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;

import org.eclipse.search.internal.ui.text.ColoredString.Style;

public class ColoredViewersManager {
	
	public static final String LINENR_COLOR_NAME= "org.eclipse.search.ui.ColoredLabels.linenr"; //$NON-NLS-1$
	public static final String HIGHLIGHT_COLOR_NAME= "org.eclipse.search.ui.ColoredLabels.highlight"; //$NON-NLS-1$
	
	private static ColoredViewersManager fgInstance= new ColoredViewersManager();
	
	private Map fManagedViewers;
	private ColorRegistry fColorRegisty;
	
	public ColoredViewersManager() {
		fManagedViewers= new HashMap();
		fColorRegisty= JFaceResources.getColorRegistry();
		// TODO use configurable colors
		fColorRegisty.put(LINENR_COLOR_NAME, new RGB(0, 0, 128));
		fColorRegisty.put(HIGHLIGHT_COLOR_NAME, new RGB(206, 204, 247));
	}
	
	public void installColoredLabels(StructuredViewer viewer) {
		if (fManagedViewers.containsKey(viewer)) {
			return; // already installed
		}
		fManagedViewers.put(viewer, new ManagedViewer(viewer));
	}
	
	
	public void uninstallColoredLabels(StructuredViewer viewer) {
		ManagedViewer mv= (ManagedViewer) fManagedViewers.remove(viewer);
		if (mv == null)
			return; // not installed
	}
	
	public Color getColorForName(String symbolicName) {
		return fColorRegisty.get(symbolicName);
	}
	
		
	private class ManagedViewer implements DisposeListener {
		
		private static final String COLORED_LABEL_KEY= "coloredlabel"; //$NON-NLS-1$
		
		private StructuredViewer fViewer;
		private OwnerDrawSupport fOwnerDrawSupport;
		
		private ManagedViewer(StructuredViewer viewer) {
			fViewer= viewer;
			fOwnerDrawSupport= null;
			fViewer.getControl().addDisposeListener(this);
			installOwnerDraw();
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
		 */
		public void widgetDisposed(DisposeEvent e) {
			uninstallColoredLabels(fViewer);
		}
		
		protected void installOwnerDraw() {
			if (fOwnerDrawSupport == null) {
				// not yet installed
				fOwnerDrawSupport= new OwnerDrawSupport(fViewer.getControl()) { // will install itself as listeners
					public ColoredString getColoredLabel(Item item) {
						return getColoredLabelForView(item);
					}
	
					public Color getColor(String foregroundColorName, Display display) {
						return getColorForName(foregroundColorName);
					}
				};
			}
			refreshViewer();
		}
		
		private void refreshViewer() {
			Control control= fViewer.getControl();
			if (!control.isDisposed()) {
				if (control instanceof Tree) {
					refresh(((Tree) control).getItems());
				} else if (control instanceof Table) {
					refresh(((Table) control).getItems());
				}
			}
		}

		private void refresh(Item[] items) {
			for (int i= 0; i < items.length; i++) {
				Item item= items[i];
				item.setData(COLORED_LABEL_KEY, null);
				String text= item.getText();
				item.setText(""); //$NON-NLS-1$
				item.setText(text);
				if (item instanceof TreeItem) {
					refresh(((TreeItem) item).getItems());
				}
			}
		}
		
		private ColoredString getColoredLabelForView(Item item) {
			ColoredString oldLabel= (ColoredString) item.getData(COLORED_LABEL_KEY);
			String itemText= item.getText();
			if (oldLabel != null && oldLabel.getString().equals(itemText)) {
				// avoid accesses to the label provider if possible
				return oldLabel;
			}
			ColoredString newLabel= null;
			IBaseLabelProvider labelProvider= fViewer.getLabelProvider();
			if (labelProvider instanceof IRichLabelProvider) {
				newLabel= ((IRichLabelProvider) labelProvider).getRichTextLabel(item.getData());
			}
			if (newLabel == null) {
				newLabel= new ColoredString(itemText); // fallback. Should never happen.
			} else if (!newLabel.getString().equals(itemText)) {
				// the decorator manager has already queued an new update 
				newLabel= decorateColoredString(newLabel, itemText, ColoredString.DEFAULT_STYLE);
			}
			item.setData(COLORED_LABEL_KEY, newLabel); // cache the result
			return newLabel;
		}

	}

	public static ColoredString decorateColoredString(ColoredString string, String decorated, Style color) {
		String label= string.getString();
		int originalStart= decorated.indexOf(label);
		if (originalStart == -1) {
			return new ColoredString(decorated); // the decorator did something wild
		}
		if (originalStart > 0) {
			ColoredString newString= new ColoredString(decorated.substring(0, originalStart), color);
			newString.append(string);
			string= newString;
		}
		if (decorated.length() > originalStart + label.length()) { // decorator appended something
			return string.append(decorated.substring(originalStart + label.length()), color);
		}
		return string; // no change
	}
	
	public static void install(StructuredViewer viewer) {
		fgInstance.installColoredLabels(viewer);
	}

}
