/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.jface.action;

import org.eclipse.pde.api.tools.annotations.NoImplement;

/**
 * This interface is used by instances of <code>IContributionItem</code>
 * to determine if the values for certain properties have been overriden
 * by their manager.
 * <p>
 * This interface is internal to the framework; it should not be implemented outside
 * the framework.
 * </p>
 *
 * @since 2.0
 */
@NoImplement
public interface IContributionManagerOverrides {
	/**
	 * Id for the enabled property. Value is <code>"enabled"</code>.
	 *
	 * @since 2.0
	 */
	public static final String P_ENABLED = "enabled"; //$NON-NLS-1$

	/**
	 * Find out the enablement of the item
	 * @param item the contribution item for which the enable override value is
	 * determined
	 * @return <ul>
	 * 				<li><code>Boolean.TRUE</code> if the given contribution item should be enabled</li>
	 * 				<li><code>Boolean.FALSE</code> if the item should be disabled</li>
	 * 				<li><code>null</code> if the item may determine its own enablement</li>
	 * 			</ul>
	 * @since 2.0
	 */
	public Boolean getEnabled(IContributionItem item);

	/**
	 * This is not intended to be called outside of the workbench. This method
	 * is intended to be deprecated in 3.1.
	 *
	 * TODO deprecate for 3.1
	 * @param item the contribution item for which the accelerator value is determined
	 * @return the accelerator
	 */
	public Integer getAccelerator(IContributionItem item);

	/**
	 * This is not intended to be called outside of the workbench. This method
	 * is intended to be deprecated in 3.1.
	 *
	 * TODO deprecate for 3.1
	 * @param item the contribution item for which the accelerator text is determined
	 * @return the text for the accelerator
	 */
	public String getAcceleratorText(IContributionItem item);

	/**
	 * This is not intended to be called outside of the workbench. This method
	 * is intended to be deprecated in 3.1.
	 *
	 * TODO deprecate for 3.1
	 * @param item the contribution item for which the text is determined
	 * @return the text
	 */
	public String getText(IContributionItem item);

	/**
	 * Visibility override.
	 *
	 * @param item the contribution item in question
	 * @return  <ul>
	 * 				<li><code>Boolean.TRUE</code> if the given contribution item should be visible</li>
	 * 				<li><code>Boolean.FALSE</code> if the item should not be visible</li>
	 * 				<li><code>null</code> if the item may determine its own visibility</li>
	 * 			</ul>
	 * @since 3.5
	 */
	public Boolean getVisible(IContributionItem item);
}
