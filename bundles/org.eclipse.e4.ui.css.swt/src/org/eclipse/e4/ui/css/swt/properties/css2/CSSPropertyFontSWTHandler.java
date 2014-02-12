/*******************************************************************************
 * Copyright (c) 2008, 2014 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     Remy Chi Jian Suen <remy.suen@gmail.com>
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 422702
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.css2;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler2;
import org.eclipse.e4.ui.css.core.dom.properties.css2.AbstractCSSPropertyFontHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyFontHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.exceptions.UnsupportedPropertyException;
import org.eclipse.e4.ui.css.swt.dom.ItemElement;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper;
import org.eclipse.e4.ui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.e4.ui.css.swt.properties.custom.CTabETabHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyFontSWTHandler extends AbstractCSSPropertyFontHandler
implements ICSSPropertyHandler2 {

	public final static ICSSPropertyFontHandler INSTANCE = new CSSPropertyFontSWTHandler();

	/**
	 * The key for the SWT event listener that will be attached to a CTabFolder
	 * for applying fonts to a CTabItem.
	 */
	private static final String CSS_CTABITEM_SELECTED_FONT_LISTENER_KEY = "CSS_CTABFOLDER_SELECTED_FONT_LISTENER_KEY"; //$NON-NLS-1$

	private static void setFont(Widget widget, Font font) {

		if (widget instanceof CTabItem) {
			CTabItem item = (CTabItem) widget;
			if (item.getFont() != font) {
				CSSSWTFontHelper.storeDefaultFont(item);
				item.setFont(font);
			}
		} else if (widget instanceof CTabFolder) {
			CTabFolder folder = (CTabFolder) widget;
			try {
				folder.setRedraw(false);
				if (folder.getFont() != font) {
					CSSSWTFontHelper.storeDefaultFont(folder);
					folder.setFont(font);
				}
				updateChildrenFonts(folder, font);
			} finally {
				folder.setRedraw(true);
			}
		} else if (widget instanceof Control) {
			Control control = (Control) widget;
			if (control.getFont() != font) {
				CSSSWTFontHelper.storeDefaultFont(control);
				control.setFont(font);
			}
		}
	}

	private static void updateChildrenFonts(CTabFolder folder, Font font) {
		for (CTabItem item : folder.getItems()) {
			if (item.getFont() != font) {
				CSSSWTFontHelper.storeDefaultFont(item);
				item.setFont(font);
			}
		}
	}

	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		Widget widget = SWTElementHelpers.getWidget(element);
		if (widget != null) {
			CSS2FontProperties fontProperties = CSSSWTFontHelper
					.getCSS2FontProperties(widget, engine
							.getCSSElementContext(widget));
			if (fontProperties != null) {
				super.applyCSSProperty(element, property, value, pseudo,
						engine);
				if (widget instanceof CTabItem) {
					Control parent = CTabETabHelper.getParent(widget);
					FontSelectionListener listener = (FontSelectionListener) parent.getData(
							CSS_CTABITEM_SELECTED_FONT_LISTENER_KEY);
					if (listener == null) {
						listener = new FontSelectionListener(engine);
						parent.addListener(SWT.Paint, listener);
						parent.setData(
								CSS_CTABITEM_SELECTED_FONT_LISTENER_KEY,
								listener);
					} else {
						// update our engine
						listener.setEngine(engine);
					}
					listener.setShouldStyle(true);
				}
			}
			return true;
		} else {
			if (element instanceof CSS2FontProperties) {
				super
				.applyCSSProperty(element, property, value, pseudo,
						engine);
				return true;
			}
		}
		return false;
	}

	@Override
	public void applyCSSPropertyFontFamily(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		Widget widget = SWTElementHelpers.getWidget(element);
		CSS2FontProperties fontProperties = CSSSWTFontHelper
				.getCSS2FontProperties(widget, engine
						.getCSSElementContext(widget));
		if (fontProperties != null) {
			applyCSSPropertyFontFamily(fontProperties, value,
					pseudo, engine);
			return;
		}
		throw new UnsupportedPropertyException("font-family");
	}

	@Override
	public void applyCSSPropertyFontSize(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		Widget widget = SWTElementHelpers.getWidget(element);
		CSS2FontProperties fontProperties = CSSSWTFontHelper
				.getCSS2FontProperties(widget, engine
						.getCSSElementContext(widget));
		if (fontProperties != null) {
			applyCSSPropertyFontSize(fontProperties, value,
					pseudo, engine);
			return;
		}
		throw new UnsupportedPropertyException("font-size");
	}

	@Override
	public void applyCSSPropertyFontWeight(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		Widget widget = SWTElementHelpers.getWidget(element);
		CSS2FontProperties fontProperties = CSSSWTFontHelper
				.getCSS2FontProperties(widget, engine
						.getCSSElementContext(widget));
		if (fontProperties != null) {
			applyCSSPropertyFontWeight(fontProperties, value,
					pseudo, engine);
			return;
		}
		throw new UnsupportedPropertyException("font-weight");
	}

	@Override
	public void applyCSSPropertyFontStyle(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		Widget widget = SWTElementHelpers.getWidget(element);
		CSS2FontProperties fontProperties = CSSSWTFontHelper
				.getCSS2FontProperties(widget, engine
						.getCSSElementContext(widget));
		if (fontProperties != null) {
			applyCSSPropertyFontStyle(fontProperties, value,
					pseudo, engine);
			return;
		}
		throw new UnsupportedPropertyException("font-style");
	}

	@Override
	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		Widget widget = SWTElementHelpers.getWidget(element);
		if (widget != null) {
			return super.retrieveCSSProperty(widget, property, pseudo, engine);
		}
		return null;
	}

	@Override
	public String retrieveCSSPropertyFontAdjust(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyFontFamily(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		Widget widget = (Widget) element;
		return CSSSWTFontHelper.getFontFamily(widget);
	}

	@Override
	public String retrieveCSSPropertyFontSize(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		Widget widget = (Widget) element;
		return CSSSWTFontHelper.getFontSize(widget);
	}

	@Override
	public String retrieveCSSPropertyFontStretch(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyFontStyle(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		Widget widget = (Widget) element;
		return CSSSWTFontHelper.getFontStyle(widget);

	}

	@Override
	public String retrieveCSSPropertyFontVariant(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyFontWeight(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		Widget widget = (Widget) element;
		return CSSSWTFontHelper.getFontWeight(widget);
	}

	@Override
	public void onAllCSSPropertiesApplyed(Object element, CSSEngine engine)
			throws Exception {
		final Widget widget = SWTElementHelpers.getWidget(element);
		if (widget == null || widget instanceof CTabItem) {
			return;
		}
		CSS2FontProperties fontProperties = CSSSWTFontHelper
				.getCSS2FontProperties(widget, engine
						.getCSSElementContext(widget));
		if (fontProperties == null) {
			return;
		}
		Font font = (Font) engine.convert(fontProperties, Font.class, widget);
		setFont(widget, font);
	}

	private class FontSelectionListener implements Listener {

		/**
		 * The font attributes that we currently "support" and that should be
		 * retrieved from the style sheet. This list must be updated if
		 * AbstractCSSPropertyFontHandler's listing changes.
		 */
		private String[] fontAttributes = { "font", "font-family", "font-size",
				"font-adjust", "font-stretch", "font-style", "font-variant",
		"font-weight" };

		private CSSEngine engine;

		private Item selection;

		/**
		 * Determines whether styling should be performed on the next paint
		 * event.
		 */
		private boolean shouldStyle;

		public FontSelectionListener(CSSEngine engine) {
			this.engine = engine;
		}

		public void setEngine(CSSEngine engine) {
			this.engine = engine;
		}

		/**
		 * Sets whether the tab items should be restyled on the next paint
		 * event. As paint events are fired multiple times while an application
		 * is running, it is not desirable to restyle the items unless there is
		 * a need. This method can be used to force the next paint event to be
		 * processed as a restyling request.
		 *
		 * @param shouldStyle
		 *            whether the tab items should be restyled on the next paint
		 *            event or not
		 */
		public void setShouldStyle(boolean shouldStyle) {
			this.shouldStyle = shouldStyle;
		}

		/**
		 * Applies the styles defined in the provided declaration onto the item.
		 * Pseudo classes will be considered if one has been specified.
		 *
		 * @param styleDeclaration
		 *            the style declaration to be used to style the specified
		 *            item
		 * @param pseudo
		 *            the pseudo class to use, or <code>null</code> if none is
		 *            required
		 * @param item
		 *            the item to style
		 */
		private void applyStyles(CSSStyleDeclaration styleDeclaration,
				String pseudo, Item item) {
			// retrieve the css font properties pertaining to the item
			CSS2FontProperties fontProperties = CSSSWTFontHelper
					.getCSS2FontProperties(item, engine
							.getCSSElementContext(item));
			ItemElement itemElement = new ItemElement(item, engine);
			if (fontProperties != null) {
				// reset ourselves to prevent the stacking of properties
				reset(fontProperties);

				for (String fontAttribute : fontAttributes) {
					CSSValue value = styleDeclaration
							.getPropertyCSSValue(fontAttribute);
					if (value != null) {
						try {
							// we have a value, so apply it to the properties
							CSSPropertyFontSWTHandler.super.applyCSSProperty(
									itemElement, fontAttribute, value,
									pseudo, engine);
						} catch (Exception e) {
							engine.handleExceptions(e);
						}
					}
				}

				try {
					// set the font
					Font font = (Font) engine.convert(fontProperties,
							Font.class, item);
					setFont(item, font);
				} catch (Exception e) {
					engine.handleExceptions(e);
				}
			}
		}

		/**
		 * Applies the regular unselected styles to the specified items.
		 *
		 * @param items
		 *            the items to style
		 */
		private void styleUnselected(Item[] items) {
			for (Item item : items) {
				CSSStyleDeclaration unselectedStyle = engine.getViewCSS()
						.getComputedStyle(engine.getElement(item), null);
				if (unselectedStyle == null) {
					// no styles defined, just reset the font
					setFont(item, null);
				} else {
					applyStyles(unselectedStyle, null, item);
				}
			}
		}

		/**
		 * Applies the style declaration for selected items to the specified
		 * item if one has been defined.
		 *
		 * @param selection
		 *            the item to style
		 */
		private boolean styleSelected(Item selection) {
			CSSStyleDeclaration selectedStyle = engine.getViewCSS()
					.getComputedStyle(engine.getElement(selection), "selected");
			if (selectedStyle == null) {
				return false;
			}

			applyStyles(selectedStyle, "selected", selection);
			return true;
		}

		private void reset(CSS2FontProperties properties) {
			properties.setFamily(null);
			properties.setSize(null);
			properties.setSizeAdjust(null);
			properties.setWeight(null);
			properties.setStyle(null);
			properties.setVariant(null);
			properties.setStretch(null);
		}

		@Override
		public void handleEvent(Event e) {
			if (e.widget instanceof CTabFolder) {
				Item[] items;
				Item selection;
				CTabFolder folder = (CTabFolder) e.widget;
				selection = folder.getSelection();
				items = folder.getItems();

				// only style if the selection has changed
				if (!shouldStyle && this.selection == selection) {
					return;
				}
				// style individual items
				styleUnselected(items);

				if (selection != null && !styleSelected(selection)) {
					setFont(selection, null);
				}
				this.selection = selection;
				shouldStyle = false;
			}
		}
	}
}
