/*******************************************************************************
 * Copyright (c) 2009 Remy Chi Jian Suen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyCTabItemShowCloseHandler implements ICSSPropertyHandler {

	public static final ICSSPropertyHandler INSTANCE = new CSSPropertyCTabItemShowCloseHandler();

	private static final String CSS_CTABITEM_SELECTED_SHOW_CLOSE_LISTENER_KEY = "CSS_CTABFOLDER_SELECTED_SHOW_CLOSE_LISTENER_KEY"; //$NON-NLS-1$

	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		Widget widget = SWTElementHelpers.getWidget(element);
		if (widget instanceof CTabItem) {
			CTabItem item = (CTabItem) widget;
			boolean showClose = ((Boolean) engine.convert(value, Boolean.class,
					null)).booleanValue();
			if ("selected".equals(pseudo)) {
				ShowCloseSelectionListener listener = (ShowCloseSelectionListener) item
						.getParent().getData(
								CSS_CTABITEM_SELECTED_SHOW_CLOSE_LISTENER_KEY);
				if (listener == null) {
					listener = new ShowCloseSelectionListener(engine);
					item.getParent().addListener(SWT.Paint, listener);
					item.getParent().setData(
							CSS_CTABITEM_SELECTED_SHOW_CLOSE_LISTENER_KEY,
							listener);
				} else {
					listener.setEngine(engine);
				}
				item = item.getParent().getSelection();
				listener.setSelection(item);
				if (item != null) {
					item.setShowClose(showClose);
				}
			} else {
				item.setShowClose(showClose);
			}
			return true;
		}
		return false;
	}

	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		Widget widget = SWTElementHelpers.getWidget(element);
		if (widget instanceof CTabItem) {
			CTabItem item = (CTabItem) widget;
			return Boolean.toString(item.getShowClose());
		}
		return null;
	}

	private class ShowCloseSelectionListener implements Listener {

		private CSSEngine engine;
		
		private CTabItem selection;

		public ShowCloseSelectionListener(CSSEngine engine) {
			this.engine = engine;
		}
		
		public void setSelection(CTabItem selection) {
			this.selection = selection;
		}

		public void setEngine(CSSEngine engine) {
			this.engine = engine;
		}

		public void handleEvent(Event e) {
			CTabFolder folder = (CTabFolder) e.widget;
			CTabItem selection = folder.getSelection();
			if (this.selection == selection) {
				return;
			}
			
			CTabItem[] items = folder.getItems();
			int selectionIndex = folder.getSelectionIndex();
			boolean selectionSet = false;

			CSSStyleDeclaration selectedStyle = engine.getViewCSS()
					.getComputedStyle(engine.getElement(folder.getSelection()),
							"selected");
			if (selectedStyle != null) {
				CSSValue value = selectedStyle
						.getPropertyCSSValue("show-close");
				if (value != null) {
					folder.getSelection().setShowClose(
							Boolean.parseBoolean(value.getCssText()));
					selectionSet = true;
				}
			}

			CSSStyleDeclaration unselectedStyle = engine.getViewCSS()
					.getComputedStyle(engine.getElement(folder.getSelection()),
							null);
			if (unselectedStyle == null) {
				for (int i = 0; i < items.length; i++) {
					if (selectionSet && i != selectionIndex) {
						items[i].setShowClose(false);
					}
				}
			} else {
				CSSValue value = unselectedStyle
						.getPropertyCSSValue("show-close");
				boolean unselectedShowClose = value == null ? false : Boolean
						.parseBoolean(value.getCssText());
				for (int i = 0; i < items.length; i++) {
					if (selectionSet && i != selectionIndex) {
						items[i].setShowClose(unselectedShowClose);
					}
				}
			}
			
			this.selection = selection;
		}
	}

}
