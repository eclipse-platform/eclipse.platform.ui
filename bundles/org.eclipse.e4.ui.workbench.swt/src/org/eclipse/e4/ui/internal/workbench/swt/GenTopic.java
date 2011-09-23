/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.swt;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class GenTopic implements IApplication {

	public Object start(IApplicationContext context) throws Exception {
		try {
			processLiterals(CommandsPackageImpl.Literals.class);
			processLiterals(BasicPackageImpl.Literals.class);
			processLiterals(ApplicationPackageImpl.Literals.class);
			processLiterals(AdvancedPackageImpl.Literals.class);
			processLiterals(org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl.Literals.class);
			processLiterals(UiPackageImpl.Literals.class);
			processLiterals(MenuPackageImpl.Literals.class);

			// Add a newline to the last generated line
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return IApplication.EXIT_OK;
	}

	private void processLiterals(Class<?> literals)
			throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = literals.getFields();
		Map<String, EClass> classes = new TreeMap<String, EClass>();
		for (int i = 0; i < fields.length; i++) {
			Object value = fields[i].get(null);
			if (value instanceof EClass) {
				classes.put(((EClass) value).getName(), (EClass) value);
			}
		}
		for (EClass ec : classes.values()) {
			processEClass(ec);
		}
	}

	private void processEClass(EClass eClass) {
		EList<EStructuralFeature> features = eClass.getEStructuralFeatures();
		if (features.isEmpty()) {
			return;
		}
		String pkgName = eClass.getEPackage().getName();
		String className = eClass.getName();
		System.out.print("\r\r\t@SuppressWarnings(\"javadoc\")");
		System.out
				.print("\n\tpublic static interface " + className + " {" //$NON-NLS-1$ //$NON-NLS-2$
						+ "\n\t\tpublic static final String TOPIC = UIModelTopicBase + \"/" //$NON-NLS-1$
						+ pkgName + '/' + className + "\"; //$NON-NLS-1$"); //$NON-NLS-1$
		Set<String> names = new TreeSet<String>();
		for (EStructuralFeature feature : features) {
			names.add(feature.getName());
		}
		for (String name : names) {
			System.out.print("\n\t\tpublic static final String " //$NON-NLS-1$
					+ name.toUpperCase() + " = \""); //$NON-NLS-1$
			System.out.print(name + "\"; //$NON-NLS-1$"); //$NON-NLS-1$
		}
		System.out.print("\n\t}"); //$NON-NLS-1$
	}

	public void stop() {
		// nothing to do
	}

}
