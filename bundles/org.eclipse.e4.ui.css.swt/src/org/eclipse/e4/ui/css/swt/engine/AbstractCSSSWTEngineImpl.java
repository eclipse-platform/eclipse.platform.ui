/*******************************************************************************
 * Copyright (c) 2008, 2014 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.engine;

import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.engine.CSSElementContext;
import org.eclipse.e4.ui.css.core.impl.engine.CSSEngineImpl;
import org.eclipse.e4.ui.css.core.resources.IResourcesRegistry;
import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.e4.ui.css.swt.properties.converters.CSSValueSWTColorConverterImpl;
import org.eclipse.e4.ui.css.swt.properties.converters.CSSValueSWTCursorConverterImpl;
import org.eclipse.e4.ui.css.swt.properties.converters.CSSValueSWTFontConverterImpl;
import org.eclipse.e4.ui.css.swt.properties.converters.CSSValueSWTFontDataConverterImpl;
import org.eclipse.e4.ui.css.swt.properties.converters.CSSValueSWTGradientConverterImpl;
import org.eclipse.e4.ui.css.swt.properties.converters.CSSValueSWTImageConverterImpl;
import org.eclipse.e4.ui.css.swt.properties.converters.CSSValueSWTRGBConverterImpl;
import org.eclipse.e4.ui.css.swt.resources.SWTResourceRegistryKeyFactory;
import org.eclipse.e4.ui.css.swt.resources.SWTResourcesRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.Element;

/**
 * CSS SWT Engine implementation which configure CSSEngineImpl to apply styles
 * to SWT widgets.
 *
 * The redraw listeners are required to workaround the Bug 433858
 */
public abstract class AbstractCSSSWTEngineImpl extends CSSEngineImpl {
	private static final String NEEDS_REDRAW = "AbstractCSSSWTEngineImpl.needsRedraw";

	private static final String HAS_REFRESH_LISTENERS = "AbstractCSSSWTEngineImpl.hasRefreshListeners";

	protected Display display;

	public AbstractCSSSWTEngineImpl(Display display) {
		this(display, false);
	}

	public AbstractCSSSWTEngineImpl(Display display, boolean lazyApplyingStyles) {
		this.display = display;

		/** Initialize SWT CSSValue converter * */

		// Register SWT RGB CSSValue Converter
		super.registerCSSValueConverter(CSSValueSWTRGBConverterImpl.INSTANCE);
		// Register SWT Color CSSValue Converter
		super.registerCSSValueConverter(CSSValueSWTColorConverterImpl.INSTANCE);
		// Register SWT Gradient CSSValue Converter
		super
		.registerCSSValueConverter(CSSValueSWTGradientConverterImpl.INSTANCE);
		// Register SWT Cursor CSSValue Converter
		super
		.registerCSSValueConverter(CSSValueSWTCursorConverterImpl.INSTANCE);
		// Register SWT Font CSSValue Converter
		super.registerCSSValueConverter(CSSValueSWTFontConverterImpl.INSTANCE);
		// Register SWT FontData CSSValue Converter
		super
		.registerCSSValueConverter(CSSValueSWTFontDataConverterImpl.INSTANCE);
		// Register SWT Image CSSValue Converter
		super.registerCSSValueConverter(CSSValueSWTImageConverterImpl.INSTANCE);

		if (lazyApplyingStyles) {
			new CSSSWTApplyStylesListener(display, this);
		}

		initializeCSSElementProvider();
		initializeCSSPropertyHandlers();
		//		SWTElement.setEngine(display, this);

		setResourceRegistryKeyFactory(new SWTResourceRegistryKeyFactory());
	}

	protected abstract void initializeCSSPropertyHandlers();

	protected abstract void initializeCSSElementProvider();

	@Override
	public IResourcesRegistry getResourcesRegistry() {
		IResourcesRegistry resourcesRegistry = super.getResourcesRegistry();
		if (resourcesRegistry == null) {
			super.setResourcesRegistry(new SWTResourcesRegistry(display));
		}
		return super.getResourcesRegistry();
	}

	@Override
	public Element getElement(Object element) {
		if (element instanceof CSSStylableElement
				&& ((CSSStylableElement) element).getNativeWidget() instanceof Widget) {
			return (CSSStylableElement) element;
		} else if (element instanceof Widget) {
			if (isStylable((Widget) element)) {
				return super.getElement(element);
			}
		} else {
			// FIXME: we need to pass through the ThemeElementDefinitions;
			// perhaps they should be handled by a separate engine
			return super.getElement(element);
		}
		return null;
	}

	/**
	 * Return true if the given widget can be styled
	 *
	 * @param widget
	 *            the widget
	 * @return true if the widget can be styled
	 */
	protected boolean isStylable(Widget widget) {
		// allows widgets to be selectively excluded from styling
		return !Boolean.TRUE.equals(widget
				.getData("org.eclipse.e4.ui.css.disabled")); //$NON-NLS-1$
	}

	@Override
	public void reset() {
		for (CSSElementContext elementContext : getElementsContext().values()) {
			Element element = elementContext.getElement();
			if (element instanceof WidgetElement
					&& isApplicableToReset((WidgetElement) element)) {
				((WidgetElement) element).reset();
			}
		}

		getResourcesRegistry().dispose();
		super.reset();
	}

	private boolean isApplicableToReset(WidgetElement element) {
		if (element.getNativeWidget() instanceof Widget) {
			return !((Widget) element.getNativeWidget()).isDisposed();
		}
		return false;
	}

	@Override
	public void applyStyles(Object element, boolean applyStylesToChildNodes,
			boolean computeDefaultStyle) {
		super.applyStyles(element, applyStylesToChildNodes, computeDefaultStyle);

		if (needsRefreshListeners(element)) {
			initRefreshListeners((Widget) element);
		}
	}

	// Workaround for the refreshing issue, reported with the Bug 433858
	private boolean needsRefreshListeners(Object element) {
		return element instanceof Tree;
	}

	private void initRefreshListeners(Widget widget) {
		if (widget.getData(HAS_REFRESH_LISTENERS) != null) {
			return; // already initialized;
		}
		widget.setData(HAS_REFRESH_LISTENERS, true);

		Listener focusInListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				event.widget.setData(NEEDS_REDRAW, true);
			}
		};
		widget.addListener(SWT.FocusIn, focusInListener);

		Listener selectionChangedListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.widget.getData(NEEDS_REDRAW) != null) {
					event.widget.setData(NEEDS_REDRAW, null);
					if (event.widget instanceof Control) {
						((Control) event.widget).redraw();
					}
				}
			}
		};
		widget.addListener(SWT.Selection, selectionChangedListener);
	}
}
