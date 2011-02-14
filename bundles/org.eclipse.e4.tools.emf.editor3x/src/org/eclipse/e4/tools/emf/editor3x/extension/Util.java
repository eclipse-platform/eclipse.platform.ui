/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.editor3x.extension;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.pde.core.project.IBundleProjectService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class Util {
	public static boolean isTypeOrSuper(EClass eClass, EClass element) {
		return eClass.equals(element) || element.getEAllSuperTypes().contains(eClass);
	}
	
	public static String getBundleSymbolicName(IProject project) {
		BundleContext context = FrameworkUtil.getBundle(AddonContributionEditor.class).getBundleContext();
		ServiceReference<IBundleProjectService> ref = context.getServiceReference(IBundleProjectService.class);
		IBundleProjectService service = context.getService(ref);
		try {
			IBundleProjectDescription description = service.getDescription(project);
			return description.getSymbolicName();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
