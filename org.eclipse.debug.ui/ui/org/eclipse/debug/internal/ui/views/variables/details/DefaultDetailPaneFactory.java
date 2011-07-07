/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables.details;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.debug.ui.IDetailPaneFactory;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * The default detail pane factory is contributed to the <code>org.eclipse.debug.ui.detailPaneFactories</code>
 * extension without an enablement expression so it is always loaded.  For any given selection (even
 * null or empty selections), the factory can produce a <code>SourceDetailsPane</code>
 * 
 * @since 3.3
 */
public class DefaultDetailPaneFactory implements IDetailPaneFactory {

	public static final String DEFAULT_DETAIL_PANE_ID = DefaultDetailPane.ID;
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.IDetailsFactory#createDetailsArea(java.lang.String)
	 */
	public IDetailPane createDetailPane(String id) {
		if (MessageDetailPane.ID.equals(id)) {
			return new MessageDetailPane();
		} else {
			return new DefaultDetailPane();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.IDetailsFactory#getDetailsTypes(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public Set getDetailPaneTypes(IStructuredSelection selection) {
		Set possibleIDs = new HashSet(1);
		possibleIDs.add(DefaultDetailPane.ID);
		return possibleIDs;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPaneFactory#getDefaultDetailPane(java.util.Set, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public String getDefaultDetailPane(IStructuredSelection selection) {
		// Return null so that any contributed detail pane can override the default
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.IDetailsFactory#getName(java.lang.String)
	 */
	public String getDetailPaneName(String id) {
		if (id.equals(DefaultDetailPane.ID)){
			return DefaultDetailPane.NAME;
		}
		if (id.equals(MessageDetailPane.ID)) {
			return MessageDetailPane.NAME;
		}
		return null;
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.IDetailsFactory#getDescription(java.lang.String)
	 */
	public String getDetailPaneDescription(String id) {
		if (id.equals(DefaultDetailPane.ID)){
			return DefaultDetailPane.DESCRIPTION;
		}
		if (id.equals(MessageDetailPane.ID)) {
			return MessageDetailPane.DESCRIPTION;
		}
		return null;
	}

}
