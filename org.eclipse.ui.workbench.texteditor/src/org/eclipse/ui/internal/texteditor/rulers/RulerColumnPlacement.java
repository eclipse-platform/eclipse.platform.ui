/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.rulers;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.InvalidRegistryObjectException;

/**
 * Describes the placement specification of a contribution to the
 * <code>org.eclipse.ui.texteditor.rulerColumns</code> extension point.
 *
 * @since 3.3
 */
public final class RulerColumnPlacement {
	/** The extension schema name of the id attribute. */
	private static final String ID= "id"; //$NON-NLS-1$
	/** The extension schema name of the optional gravity attribute. */
	private static final String GRAVITY= "gravity"; //$NON-NLS-1$
	/** The extension schema name of the before element. */
	private static final String BEFORE= "before"; //$NON-NLS-1$
	/** The extension schema name of the after element. */
	private static final String AFTER= "after"; //$NON-NLS-1$

	/** The placement gravity. */
	private final float fGravity;
	/** The placement constraints (element type: {@link RulerColumnPlacementConstraint}). */
	private final Set fConstraints;

	public RulerColumnPlacement() {
		fGravity= 1f;
		fConstraints= Collections.EMPTY_SET;
	}

	public RulerColumnPlacement(IConfigurationElement element) throws InvalidRegistryObjectException, CoreException {
		Assert.isLegal(element != null);
		ExtensionPointHelper helper= new ExtensionPointHelper(element);

		fGravity= helper.getDefaultAttribute(GRAVITY, 1f);
		if (fGravity < 0 || fGravity > 1)
			helper.fail(RulerColumnMessages.RulerColumnPlacement_illegal_gravity_msg);
		fConstraints= readIds(element.getChildren());
	}

	private Set readIds(IConfigurationElement[] children) throws CoreException {
		Set constraints= new LinkedHashSet((int) (children.length / 0.75) + 1, 0.75f);
		for (int i= 0; i < children.length; i++) {
			IConfigurationElement child= children[i];
			String name= child.getName();
			ExtensionPointHelper childHelper= new ExtensionPointHelper(child);
			boolean before;
			if (AFTER.equals(name))
				before= false;
			else if (BEFORE.equals(name))
				before= true;
			else {
				childHelper.fail(RulerColumnMessages.RulerColumnPlacement_illegal_child_msg);
				continue;
			}
			constraints.add(new RulerColumnPlacementConstraint(childHelper.getNonNullAttribute(ID), before));
		}
		return Collections.unmodifiableSet(constraints);
	}

	/**
	 * The gravity of the placement specification, a float in the range <code>[0, 1]</code>.
	 *
	 * @return the gravity of the placement specification
	 */
	public float getGravity() {
		return fGravity;
	}

	/**
	 * Returns the placement constraints in the order that they appear in the extension declaration.
	 *
	 * @return the unmodifiable set of placement constraints in the order that they appear in the
	 *         extension declaration
	 */
	public Set getConstraints() {
		return fConstraints;
	}
}
