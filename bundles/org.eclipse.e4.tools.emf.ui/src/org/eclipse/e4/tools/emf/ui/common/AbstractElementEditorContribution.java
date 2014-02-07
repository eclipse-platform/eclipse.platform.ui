/*******************************************************************************
 * Copyright (c) 2014 MEDEVIT, FHV and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Descher <marco@descher.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.tools.emf.ui.common;

import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;

/**
 * This class is extended by plug-ins contributing to the
 * <code>elementEditorTab</code> extension point.
 */
public abstract class AbstractElementEditorContribution {

	/**
	 * Filters the elements this extension point contribution is visible on. The
	 * returned class will be compared using
	 * {@link Class#isAssignableFrom(Class)}. So if for example an element of
	 * type {@link MPart} is returned, it is only contributed to the part
	 * editor. If {@link MUIElement} is returned, the contribution is presented
	 * on all elements of this type.
	 *
	 * @return the class the contributed editor extension is assignable to
	 */
	public abstract Class<?> getContributableTo();

	/**
	 * @return the label to be shown in the {@link CTabItem} contributed
	 */
	public abstract String getTabLabel();

	/**
	 * @param parent
	 *            the {@link Composite} within a {@link CTabItem} created for
	 *            this contribution, added to the elements {@link CTabFolder}
	 * @param context
	 *            the data-binding context of the enclosing editor
	 * @param master
	 * @param editingDomain
	 * @param project
	 */
	public abstract void createContributedEditorTab(Composite parent, EMFDataBindingContext context, WritableValue master, EditingDomain editingDomain, IProject project);

}
