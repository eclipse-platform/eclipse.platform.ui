/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.api.workbenchpart;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 * @since 3.4
 *
 */
public class DependencyInjectionView extends ViewPart {

	public static final String ID = "org.eclipse.ui.tests.dependencyinjectionview";

	List<String> creationCallOrder = new ArrayList<>();
	List<String> disposeCallOrder = new ArrayList<>();
	boolean fieldAvailable = false;
	boolean methodParameterAvailable = false;
	boolean postConstructParameterAvailable = false;
	boolean predestroyParameterAvailable = false;
	boolean focusParameterAvailable = false;

	@Inject
	Shell shell;

	@Inject
	public void method(IWorkbench wb) {
		// if field is available, DI filled it before calling this method
		if (shell != null) {
			creationCallOrder.add("@field");
		}
		creationCallOrder.add("@method");

		if (shell != null) {
			fieldAvailable = true;
		}

		// Workbench must be present
		if (wb != null) {
			methodParameterAvailable = true;
		}
	}

	public DependencyInjectionView() {
		creationCallOrder.add("constructor");
	}

	@PostConstruct
	public void postconstruct(EPartService ps) {
		creationCallOrder.add("@postconstruct");
		// postconstruct is called before createPartControl
		if (shell == null) {
			fieldAvailable = false;
		}
		if (ps != null) {
			postConstructParameterAvailable = true;
		}
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		creationCallOrder.add("init");
		if (shell == null) {
			fieldAvailable = false;
		}
	}



	@Override
	public void createPartControl(Composite parent) {
		creationCallOrder.add("createPartControl");
		// field shell must be injected before createPartControl
		assertNotNull(shell);
	}

	@Focus
	public void focus(EModelService modelService) {
		creationCallOrder.add("@focus");
		if (modelService != null) {
			focusParameterAvailable = true;
		}
	}

	@Override
	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
		creationCallOrder.add("setInitializationData");
		super.setInitializationData(cfig, propertyName, data);
	}

	@Override
	public void setFocus() {
		creationCallOrder.add("setFocus");
		focusParameterAvailable = true;
	}

	@PreDestroy
	public void predestroy(EModelService modelService) {
		disposeCallOrder.add("@predestroy");
		if (modelService != null) {
			predestroyParameterAvailable = true;
		}
	}
	@Override
	public void dispose() {
		disposeCallOrder.add("dispose");
		super.dispose();
	}
}
