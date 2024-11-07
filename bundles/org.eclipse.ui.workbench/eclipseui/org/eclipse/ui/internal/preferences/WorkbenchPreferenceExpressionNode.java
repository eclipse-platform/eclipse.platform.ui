/*******************************************************************************
 * Copyright (c) 2008, 2019 Bredex GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jan-Hendrik Diederich, Bredex GmbH - initial API and implementation, bug 201052
 *     IBM Corporation - bug 201052
 ******************************************************************************/

package org.eclipse.ui.internal.preferences;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.activities.WorkbenchActivityHelper;

/**
 * @since 3.4
 * @author Jan Diederich
 */
public class WorkbenchPreferenceExpressionNode extends PreferenceNode implements IPluginContribution {

	/**
	 * @param id The id.
	 * @see PreferenceNode#PreferenceNode(String)
	 */
	public WorkbenchPreferenceExpressionNode(String id) {
		super(id);
	}

	@Override
	public IPreferenceNode findSubNode(String id) {
		return getNodeExpression(super.findSubNode(id));
	}

	@Override
	public IPreferenceNode[] getSubNodes() {
		IPreferenceNode[] prefNodes = super.getSubNodes();
		int size = prefNodes.length;
		List<IPreferenceNode> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			IPreferenceNode prefNode = getNodeExpression(prefNodes[i]);
			if (prefNode != null) {
				list.add(prefNode);
			}
		}
		return list.toArray(new IPreferenceNode[list.size()]);
	}

	/**
	 * Returns the given <code>prefNode</code>, but only if it's no
	 * WorkbenchPreferenceExtensionNode which fails the Expression check.
	 *
	 * @param prefNode The preference node which will be checked. Can be <code>null
	 *            </code>.
	 * @return The given <code>prefNode</code>, or <code>null</code> if it fails the
	 *         Expressions check.
	 */
	public static IPreferenceNode getNodeExpression(IPreferenceNode prefNode) {
		if (prefNode == null)
			return null;
		if (prefNode instanceof WorkbenchPreferenceExtensionNode) {
			WorkbenchPreferenceExpressionNode node = (WorkbenchPreferenceExtensionNode) prefNode;
			if (WorkbenchActivityHelper.restrictUseOf(node)) {
				return null;
			}
		}
		return prefNode;
	}

	@Override
	public String getLocalId() {
		return getId();
	}

	@Override
	public String getPluginId() {
		return ""; //$NON-NLS-1$
	}
}
