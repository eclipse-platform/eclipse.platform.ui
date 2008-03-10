/*******************************************************************************
 * Copyright (c) 2008 Bredex GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
public class WorkbenchPreferenceExpressionNode extends PreferenceNode 
	implements IPluginContribution {
	
	
	/**
	 * @param id The id.
	 * @see {@link PreferenceNode#PreferenceNode(String)}.
	 */
	public WorkbenchPreferenceExpressionNode(String id) {
		super(id);
	}
	
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferenceNode#findSubNode(java.lang.String)
     */
    public IPreferenceNode findSubNode(String id) {
        return getNodeExpression(super.findSubNode(id));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferenceNode#getSubNodes()
     */
    public IPreferenceNode[] getSubNodes() {
    	IPreferenceNode[] prefNodes = super.getSubNodes();
        int size = prefNodes.length;
        List list = new ArrayList(size);
        for (int i = 0; i < size; i++) {
        	IPreferenceNode prefNode = getNodeExpression(prefNodes[i]);
            if (prefNode != null) {
                list.add(prefNode);
            }
        }
        return (WorkbenchPreferenceExpressionNode[])list.toArray(new WorkbenchPreferenceExpressionNode[list.size()]);
    }

    /**
     * Returns the given <code>prefNode</code>, but only if it's no
     * WorkbenchPreferenceExtensionNode which fails the Expression check.
     * 
     * @param prefNode
     *            The preference node which will be checked. Can be <code>null
     *            </code>.
     * @return The given <code>prefNode</code>, or <code>null</code> if it
     *         fails the Expressions check.
     */
    public static IPreferenceNode getNodeExpression(
    		IPreferenceNode prefNode) {
    	if (prefNode == null)
    		return null;
        if (prefNode instanceof WorkbenchPreferenceExtensionNode) {
        	WorkbenchPreferenceExpressionNode node = 
        		(WorkbenchPreferenceExtensionNode)prefNode;
            if (WorkbenchActivityHelper.restrictUseOf(node)) {
                return null;
            }
        }
        return prefNode;
    }
    
    /**
     * Removes the PreferenceNodes from the array which fail the Expressions
     * test.
     * 
     * @param prefNode
     * @return An array of IPreferenceNode, but without the PreferenceNodes
     * 		   which failed the Expressions check. 
     */
    public static WorkbenchPreferenceExpressionNode[] getNodeExpressions(
    		WorkbenchPreferenceExpressionNode[] prefNode) {
    	List result = new ArrayList(prefNode.length);
    	for (int i=0; i<prefNode.length; i++) {
    		if (getNodeExpression(prefNode[i]) != null) {
    			result.add(prefNode[i]);
    		}
    	}
        return (WorkbenchPreferenceExpressionNode[])result
        	.toArray(new WorkbenchPreferenceExpressionNode[result.size()]);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPluginContribution#getLocalId()
	 */
	public String getLocalId() {
		return getId();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPluginContribution#getPluginId()
	 */
	public String getPluginId() {
		return ""; //$NON-NLS-1$
	}
}
