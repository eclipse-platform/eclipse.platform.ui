/*******************************************************************************
 * Copyright (c) 2008, 2013 Angelo Zerr and others.
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

import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.ui.css.core.impl.engine.RegistryCSSElementProvider;
import org.eclipse.e4.ui.css.core.impl.engine.RegistryCSSPropertyHandlerProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * CSS SWT Engine implementation which configure CSSEngineImpl to apply styles
 * to SWT widgets with static handler strategy.
 */
public class CSSSWTEngineImpl extends AbstractCSSSWTEngineImpl {

	private DisposeListener disposeListener;

	public CSSSWTEngineImpl(Display display) {
		super(display);
		init();
	}

	public CSSSWTEngineImpl(Display display, boolean lazyApplyingStyles) {
		super(display, lazyApplyingStyles);
		init();
	}

	private void init() {
		disposeListener = new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				handleWidgetDisposed(e.widget);
			}
		};
	}

	@Override
	protected void hookNativeWidget(Object widget) {
		if (widget instanceof Widget) {
			Widget swtWidget = (Widget) widget;
			swtWidget.addDisposeListener(disposeListener);
		}
	}

	@Override
	protected void initializeCSSPropertyHandlers() {
		propertyHandlerProviders.add(new RegistryCSSPropertyHandlerProvider(
				RegistryFactory.getRegistry()));
	}

	@Override
	protected void initializeCSSElementProvider() {
		setElementProvider(new RegistryCSSElementProvider(
				RegistryFactory.getRegistry()));
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				s.setRedraw(true);
			}
		}
	}

}
