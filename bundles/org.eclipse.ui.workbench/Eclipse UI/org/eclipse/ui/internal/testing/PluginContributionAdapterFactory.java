/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.testing;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.internal.decorators.DecoratorDefinition;
import org.eclipse.ui.internal.dialogs.WizardCollectionElement;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.internal.preferences.WorkbenchPreferenceExpressionNode;
import org.eclipse.ui.internal.progress.JobInfo;
import org.eclipse.ui.internal.registry.ActionSetDescriptor;
import org.eclipse.ui.internal.registry.Category;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.eclipse.ui.internal.registry.ViewDescriptor;
import org.eclipse.ui.internal.themes.ColorDefinition;
import org.eclipse.ui.internal.themes.ThemeElementCategory;
import org.eclipse.ui.testing.ContributionInfo;
import org.eclipse.ui.views.IViewCategory;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * @since 3.6
 * 
 */
public class PluginContributionAdapterFactory implements IAdapterFactory {

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType != ContributionInfo.class) {
			return null;
		}
		if (adaptableObject instanceof IPluginContribution) {
			IPluginContribution contribution = (IPluginContribution) adaptableObject;

			String elementType;

			if (contribution instanceof EditorDescriptor) {
				elementType = ContributionInfoMessages.ContributionInfo_Editor;
			} else if (contribution instanceof ViewDescriptor) {
				elementType = ContributionInfoMessages.ContributionInfo_View;
			} else if (contribution instanceof ActionSetDescriptor) {
				elementType = ContributionInfoMessages.ContributionInfo_ActionSet;
			} else if (contribution instanceof Category) {
				elementType = ContributionInfoMessages.ContributionInfo_Category;
			} else if (contribution instanceof IViewCategory) {
				elementType = ContributionInfoMessages.ContributionInfo_Category;
			} else if (contribution instanceof ThemeElementCategory) {
				elementType = ContributionInfoMessages.ContributionInfo_Category;
			} else if (contribution instanceof WizardCollectionElement) {
				elementType = ContributionInfoMessages.ContributionInfo_Category;
			} else if (contribution instanceof ColorDefinition) {
				elementType = ContributionInfoMessages.ContributionInfo_ColorDefinition;
			} else if (contribution instanceof WorkbenchWizardElement) {
				elementType = ContributionInfoMessages.ContributionInfo_Wizard;
			} else if (contribution instanceof PerspectiveDescriptor) {
				elementType = ContributionInfoMessages.ContributionInfo_Perspective;
			} else if (contribution instanceof WorkbenchPreferenceExpressionNode) {
				elementType = ContributionInfoMessages.ContributionInfo_Page;
			} else if (contribution instanceof DecoratorDefinition) {
				elementType = ContributionInfoMessages.ContributionInfo_LabelDecoration;
			} else {
				elementType = ContributionInfoMessages.ContributionInfo_Unknown;
			}

			return new ContributionInfo(contribution.getPluginId(), elementType, null);
		}
		if (adaptableObject instanceof JobInfo) {
			JobInfo jobInfo = (JobInfo) adaptableObject;
			Job job = jobInfo.getJob();
			if (job != null) {
				Bundle bundle = FrameworkUtil.getBundle(job.getClass());
				if (bundle != null) {
					return new ContributionInfo(bundle.getSymbolicName(),
							ContributionInfoMessages.ContributionInfo_Job, null);
				}
			}
		}
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[] { ContributionInfo.class };
	}

}
