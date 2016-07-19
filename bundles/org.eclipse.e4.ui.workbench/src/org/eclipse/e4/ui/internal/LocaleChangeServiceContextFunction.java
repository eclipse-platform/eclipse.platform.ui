/*******************************************************************************
 * Copyright (c) 2013, 2016 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.ILocaleChangeService;
import org.eclipse.e4.ui.model.application.MApplication;
import org.osgi.service.component.annotations.Component;

/**
 * Context function to provide the LocaleChangeServiceImpl to the application context.
 */
@Component(service = IContextFunction.class, property = "service.context.key=org.eclipse.e4.core.services.nls.ILocaleChangeService")
public class LocaleChangeServiceContextFunction extends ContextFunction {

	@Override
	public Object compute(IEclipseContext context, String contextKey) {
		ILocaleChangeService lcService = ContextInjectionFactory.make(
				LocaleChangeServiceImpl.class, context);

		// add the new object to the application context
		MApplication application = context.get(MApplication.class);
		IEclipseContext ctx = application.getContext();
		ctx.set(ILocaleChangeService.class, lcService);
		return lcService;
	}
}
