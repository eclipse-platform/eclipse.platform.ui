/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.InvalidRegistryObjectException;

import org.eclipse.ui.internal.texteditor.TextEditorPlugin;

/**
 * Describes the placement specification of a contribution to the
 * <code>org.eclipse.ui.texteditor.rulerColumn</code> extension point.
 * 
 * @since 3.3
 */
public final class RulerColumnPlacement {
	/** The extension schema name of the id attribute. */
	private static final String ID= "id"; //$NON-NLS-1$
	/** The extension schema name of the optional weight attribute. */
	private static final String WEIGHT= "weight"; //$NON-NLS-1$
	/** The extension schema name of the before element. */
	private static final String BEFORE= "before"; //$NON-NLS-1$
	/** The extension schema name of the after element. */
	private static final String AFTER= "after"; //$NON-NLS-1$

	/** The placement weight. */
	private final float fWeight;
	/** The ids of columns that this column wants to be placed after. */
	private final Set fAfter;
	/** The ids of columns that this column wants to be placed before. */
	private final Set fBefore;

	public RulerColumnPlacement() {
		fWeight= 1f;
		fAfter= Collections.EMPTY_SET;
		fBefore= Collections.EMPTY_SET;
	}

	public RulerColumnPlacement(IConfigurationElement element) throws InvalidRegistryObjectException {
		Assert.isLegal(element != null);
		ILog log= TextEditorPlugin.getDefault().getLog();
		ExtensionPointHelper helper= new ExtensionPointHelper(element, log);
		
		fWeight= helper.getDefaultAttribute(WEIGHT, 1f);
		fAfter= readIds(log, element.getChildren(AFTER));
		fBefore= readIds(log, element.getChildren(BEFORE));
	}

	private Set readIds(ILog log, IConfigurationElement[] children) {
		Set ids= new HashSet((int) (children.length / 0.75) + 1, 0.75f);
		for (int i= 0; i < children.length; i++) {
			IConfigurationElement child= children[i];
			ExtensionPointHelper childHelper= new ExtensionPointHelper(child, log);
			ids.add(childHelper.getNonNullAttribute(ID));
		}
		return Collections.unmodifiableSet(ids);
	}
	
	public float getWeight() {
		return fWeight;
	}

	public Set getBefore() {
		return fBefore;
	}
	
	public Set getAfter() {
		return fAfter;
	}
}
