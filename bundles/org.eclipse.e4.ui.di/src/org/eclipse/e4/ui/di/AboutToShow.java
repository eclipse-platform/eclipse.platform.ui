/*******************************************************************************
 * Copyright (c) 2012, 2015 MEDEVIT, FHV and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marco Descher <marco@descher.at> - initial API and implementation
 *     IBM Corporation - bug fixes
 *******************************************************************************/
package org.eclipse.e4.ui.di;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to provide MMenuElements to the list of dynamically shown entries
 * within a DynamicMenuContributionItem. Usage in contribution class:
 * <p>
 * {@literal @}AboutToShow<br>
 * public void aboutToShow(List&lt;MMenuElement&gt; items) { }
 *
 * @see org.eclipse.jface.action.IMenuListener
 * @since 1.0
 */
@Documented
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AboutToShow {
	// intentionally left empty
}