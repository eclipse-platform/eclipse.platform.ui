/*******************************************************************************
 * Copyright (c) 2008, 2026 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.engine;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.engine.CSSElementContext;
import org.eclipse.e4.ui.css.core.impl.engine.CSSEngineImpl;
import org.eclipse.e4.ui.css.core.impl.engine.RegistryCSSElementProvider;
import org.eclipse.e4.ui.css.core.impl.engine.RegistryCSSPropertyHandlerProvider;
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
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.Element;

/**
 * CSS SWT Engine. Configures {@link CSSEngineImpl} with the SWT-specific
 * value converters and the registry-driven element + property handler
 * providers, and applies styles to SWT widgets.
 */
public class CSSSWTEngineImpl extends CSSEngineImpl {

	protected Display display;

	private final DisposeListener disposeListener = e -> handleWidgetDisposed(e.widget);

	public CSSSWTEngineImpl(Display display) {
		this(display, false);
	}

	public CSSSWTEngineImpl(Display display, boolean lazyApplyingStyles) {
		this.display = display;

		registerCSSValueConverter(CSSValueSWTRGBConverterImpl.INSTANCE);
		registerCSSValueConverter(CSSValueSWTColorConverterImpl.INSTANCE);
		registerCSSValueConverter(CSSValueSWTGradientConverterImpl.INSTANCE);
		registerCSSValueConverter(CSSValueSWTCursorConverterImpl.INSTANCE);
		registerCSSValueConverter(CSSValueSWTFontConverterImpl.INSTANCE);
		registerCSSValueConverter(CSSValueSWTFontDataConverterImpl.INSTANCE);
		registerCSSValueConverter(CSSValueSWTImageConverterImpl.INSTANCE);

		if (lazyApplyingStyles) {
			new CSSSWTApplyStylesListener(display, this);
		}

		setElementProvider(new RegistryCSSElementProvider(RegistryFactory.getRegistry()));
		propertyHandlerProviders.add(new RegistryCSSPropertyHandlerProvider(RegistryFactory.getRegistry()));

		setResourceRegistryKeyFactory(new SWTResourceRegistryKeyFactory());
	}

	@Override
	protected void hookNativeWidget(Object widget) {
		if (widget instanceof Widget swtWidget) {
			swtWidget.addDisposeListener(disposeListener);
		}
	}

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
	 * Return true if the given widget can be styled.
	 */
	protected boolean isStylable(Widget widget) {
		return !widget.isDisposed()
				&& !Boolean.TRUE.equals(widget.getData("org.eclipse.e4.ui.css.disabled")); //$NON-NLS-1$
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
	public void reapply() {
		Shell[] shells = display.getShells();
		for (Shell s : shells) {
			try {
				s.setRedraw(false);
				s.reskin(SWT.ALL);
				applyStyles(s, true);
			} catch (Exception e) {
				ILog.of(getClass()).error(e.getMessage(), e);
			} finally {
				s.setRedraw(true);
			}
		}
	}
}
