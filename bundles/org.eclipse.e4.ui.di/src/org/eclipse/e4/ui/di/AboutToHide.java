/*******************************************************************************
 * Copyright (c) 2012, 2013 MEDEVIT, FHV and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * Use this annotation to act on to the list of dynamically shown entries within
 * a DynamicMenuContributionItem. Usage in contribution class:
 * <p>
 * {@literal @}AboutToHide<br>
 * public void aboutToHide(List&lt;MMenuElement&gt; items) { }
 * 
 * @see org.eclipse.jface.action.IMenuListener2
 * @since 1.0
 */
@Documented
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AboutToHide {
	// intentionally left empty
}