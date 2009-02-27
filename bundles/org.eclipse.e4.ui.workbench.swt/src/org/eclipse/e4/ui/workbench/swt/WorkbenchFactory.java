/*******************************************************************************
 * Copyright (c) 2008 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Boris Bokowski, IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.workbench.swt;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.workbench.swt.internal.Workbench;
import org.eclipse.e4.workbench.ui.IWorkbench;
import org.eclipse.emf.common.util.URI;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.service.packageadmin.PackageAdmin;

public class WorkbenchFactory {
	private Location location;
	private PackageAdmin packageAdmin;
	private IExtensionRegistry registry;

	public WorkbenchFactory(Location location, PackageAdmin packageAdmin,
			IExtensionRegistry registry) {
		this.location = location;
		this.packageAdmin = packageAdmin;
		this.registry = registry;
	}

	public IWorkbench create(URI initialWorkbenchDefinitionInstance,
			IEclipseContext applicationContext) {
		return new Workbench(location, registry, packageAdmin,
				initialWorkbenchDefinitionInstance, applicationContext);
	}
}