/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import java.util.*;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.internal.ui.synchronize.StructuredViewerAdvisor;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.operations.ModelSynchronizePage;
import org.eclipse.team.ui.operations.ModelSynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.internal.extensions.INavigatorContentServiceListener;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentExtension;

/**
 * Provides a Common Navigator based viewer for use by a {@link ModelSynchronizePage}.
 */
public class CommonViewerAdvisor extends StructuredViewerAdvisor implements INavigatorContentServiceListener {

	private static final String TEAM_NAVIGATOR_CONTENT = "org.eclipse.team.ui.navigatorViewer"; //$NON-NLS-1$
	
	Set extensions = new HashSet();
	Map properties = new HashMap();
	
	/**
	 * Create a common viewer
	 * @param parent the parent composite of the common viewer
	 * @param configuration the configuration for the viewer
	 * @return a newly created common viewer
	 */
	private static CommonViewer createViewer(Composite parent, ISynchronizePageConfiguration configuration) {
		CommonViewer v = new CommonViewer(TEAM_NAVIGATOR_CONTENT, parent, SWT.NONE) {
			/* (non-Javadoc)
			 * @see org.eclipse.ui.navigator.CommonViewer#wrapLabelProvider(org.eclipse.jface.viewers.ILabelProvider)
			 */
			protected ILabelProvider wrapLabelProvider(ILabelProvider provider) {
				// Don't wrap since we don't want any decoration
				return provider;
			}
		};
		configuration.getSite().setSelectionProvider(v);
		return v;
	}
	
	/**
	 * Create the advisor using the given configuration
	 * @param configuration the configuration
	 */
	public CommonViewerAdvisor(Composite parent, ISynchronizePageConfiguration configuration) {
		super(configuration);
		CommonViewer viewer = CommonViewerAdvisor.createViewer(parent, configuration);
		GridData data = new GridData(GridData.FILL_BOTH);
		viewer.getControl().setLayoutData(data);
        viewer.getNavigatorContentService().addListener(this);
        viewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
        initializeViewer(viewer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorContentServiceListener#onLoad(org.eclipse.ui.navigator.internal.extensions.NavigatorContentExtension)
	 */
	public void onLoad(NavigatorContentExtension anExtension) {
		extensions.add(anExtension);
		anExtension.getStateModel().setProperty(TeamUI.RESOURCE_MAPPING_SCOPE, getParticipant().getContext().getScope());
		if (getParticipant().getContext() != null) {
			anExtension.getStateModel().setProperty(TeamUI.SYNCHRONIZATION_CONTEXT, getParticipant().getContext());
		}
		for (Iterator iter = properties.keySet().iterator(); iter.hasNext();) {
			String element = (String) iter.next();
			Object value = properties.get(element);
			if (value instanceof Integer) {
				Integer integer = (Integer) value;
				anExtension.getStateModel().setIntProperty(element, integer.intValue());
			}
		}
	}

	private ModelSynchronizeParticipant getParticipant() {
		return (ModelSynchronizeParticipant)getConfiguration().getParticipant();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.StructuredViewerAdvisor#navigate(boolean)
	 */
	public boolean navigate(boolean next) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Set the given property for all active extensions.
	 * @param property the property
	 * @param value the value
	 */
	public void setExtentionProperty(String property, int value) {
		properties.put(property, new Integer(value));
		for (Iterator iter = extensions.iterator(); iter.hasNext();) {
			NavigatorContentExtension extension = (NavigatorContentExtension) iter.next();
			extension.getStateModel().setIntProperty(property, value);
		}
	}

}
