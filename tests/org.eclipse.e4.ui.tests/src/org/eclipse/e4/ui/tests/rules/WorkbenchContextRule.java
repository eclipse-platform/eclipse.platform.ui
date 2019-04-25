/*******************************************************************************
 * Copyright (c) 2019 Rolf Theunissen and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Rolf Theunissen <rolf.theunissen@gmail.com> - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.rules;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.suppliers.ExtendedObjectSupplier;
import org.eclipse.e4.core.internal.di.osgi.ProviderHelper;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.widgets.Display;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class WorkbenchContextRule implements MethodRule {

	private IEclipseContext context;
	private E4Workbench wb;

	public void createAndRunWorkbench(MWindow window) {
		if (wb == null) {
			wb = new E4Workbench(context.get(MApplication.class), context);
		}
		wb.createAndRunUI(window);
	}

	public void spinEventLoop() {
		while (Display.getDefault().readAndDispatch()) {
		}
	}

	@Override
	public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				createContext();
				ContextInjectionFactory.inject(target, context);
				try {
					base.evaluate();
				} finally {
					dispose();
				}
			}
		};
	}

	protected void createContext() throws Throwable {
		context = E4Application.createDefaultContext();
		context.set(IWorkbench.PRESENTATION_URI_ARG, PartRenderingEngine.engineURI);

		final Display display = Display.getDefault();
		context.set(UISynchronize.class, new UISynchronize() {
			@Override
			public void syncExec(Runnable runnable) {
				display.syncExec(runnable);
			}

			@Override
			public void asyncExec(Runnable runnable) {
				display.asyncExec(runnable);
			}
		});

		ContextInjectionFactory.setDefault(context);

		// Workaround, enforce injection on UIEventTopic provider with current context
		ExtendedObjectSupplier supplier = ProviderHelper.findProvider(UIEventTopic.class.getName(), null);
		ContextInjectionFactory.inject(supplier, context);

		EModelService ems = context.get(EModelService.class);
		MApplication application = ems.createModelElement(MApplication.class);
		application.setContext(context);
		context.set(MApplication.class, application);
	}

	protected void dispose() {
		if (wb != null) {
			wb.close();
		}

		ContextInjectionFactory.setDefault(null);

		// Workaround, enforce uninjection on the UIEventTopic provider from context
		ExtendedObjectSupplier supplier = ProviderHelper.findProvider(UIEventTopic.class.getName(), null);
		ContextInjectionFactory.uninject(supplier, context);

		context.dispose();
	}

}
