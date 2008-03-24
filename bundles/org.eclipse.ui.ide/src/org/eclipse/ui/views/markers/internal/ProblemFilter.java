/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IIdentifier;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.views.markers.MarkerSupportInternalUtilities;

/**
 * ProblemFilters are the filters used in the problems view.
 * 
 */
public class ProblemFilter extends MarkerFilter {

	/**
	 * Tag for contains boolean
	 */
	public static final String TAG_CONTAINS = "contains"; //$NON-NLS-1$
	/**
	 * Tag for contains description.
	 */
	public static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$

	private static final String TAG_SELECT_BY_SEVERITY = "selectBySeverity"; //$NON-NLS-1$

	/**
	 * Tag for the severity value
	 */
	public static final String TAG_SEVERITY = "severity"; //$NON-NLS-1$

	final static boolean DEFAULT_CONTAINS = true;

	final static String DEFAULT_DESCRIPTION = ""; //$NON-NLS-1$	

	final static boolean DEFAULT_SELECT_BY_SEVERITY = false;

	final static int DEFAULT_SEVERITY = 0;

	/**
	 * Severity for errors
	 */
	public final static int SEVERITY_ERROR = 1 << 2;

	/**
	 * Severity for warnings
	 */
	public final static int SEVERITY_WARNING = 1 << 1;

	/**
	 * Severity for infos
	 */
	public final static int SEVERITY_INFO = 1 << 0;

	private boolean contains;

	private String description;

	private boolean selectBySeverity;

	private int severity;

	private IPluginContribution contributionDescriptor = null;

	private IIdentifier identifier;

	/**
	 * Create a new instance of the receiver with name filterName.
	 * 
	 * @param filterName
	 *            A human readable name for the filter.
	 */
	public ProblemFilter(String filterName) {
		super(filterName, new String[] { IMarker.PROBLEM });
		if ((PlatformUI.getPreferenceStore()
				.getBoolean(IWorkbenchPreferenceConstants.USE_WINDOW_WORKING_SET_BY_DEFAULT))) {
			IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			if (window != null) {
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					setOnResource(MarkerFilter.ON_WORKING_SET);
					setWorkingSet(page.getAggregateWorkingSet());
				}
			}
		}

	}

	public boolean selectMarker(ConcreteMarker marker) {
		if (!(marker instanceof ProblemMarker)) {
			return false;
		}

		ProblemMarker problemMarker = (ProblemMarker) marker;

		return !isEnabled()
				|| (super.selectMarker(problemMarker)
						&& selectByDescription(problemMarker) && selectBySeverity(problemMarker));
	}

	private boolean selectByDescription(ConcreteMarker item) {
		if (description == null || description.equals("")) { //$NON-NLS-1$
			return true;
		}

		String markerDescription = item.getDescription();
		int index = markerDescription.indexOf(description);
		return contains ? (index >= 0) : (index < 0);
	}

	private boolean selectBySeverity(ProblemMarker item) {
		if (selectBySeverity) {
			int markerSeverity = item.getSeverity();

			if (markerSeverity == IMarker.SEVERITY_ERROR) {
				return (severity & SEVERITY_ERROR) > 0;
			} else if (markerSeverity == IMarker.SEVERITY_WARNING) {
				return (severity & SEVERITY_WARNING) > 0;
			} else if (markerSeverity == IMarker.SEVERITY_INFO) {
				return (severity & SEVERITY_INFO) > 0;
			}
		}

		return true;
	}

	/**
	 * Get the value for if there is a check for containing a phrase.
	 * 
	 * @return boolean
	 */
	public boolean getContains() {
		return contains;
	}

	/**
	 * Get the value for the description.
	 * 
	 * @return boolean
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Get the value for if there is a check for severity.
	 * 
	 * @return boolean
	 */
	public boolean getSelectBySeverity() {
		return selectBySeverity;
	}

	/**
	 * Get the value for if there is a severity.
	 * 
	 * @return boolean
	 */
	public int getSeverity() {
		return severity;
	}

	/**
	 * Set the value for if there is a check for containing a phrase.
	 * 
	 * @param contains
	 */
	public void setContains(boolean contains) {
		this.contains = contains;
	}

	/**
	 * Set the value for the description.
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Set the value for if there is a check for severity
	 * 
	 * @param selectBySeverity
	 */
	public void setSelectBySeverity(boolean selectBySeverity) {
		this.selectBySeverity = selectBySeverity;
	}

	/**
	 * Set the value for the severity to match against.
	 * 
	 * @param severity
	 */
	public void setSeverity(int severity) {
		this.severity = severity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerFilter#resetState()
	 */
	public void resetState() {
		super.resetState();
		contains = DEFAULT_CONTAINS;
		description = DEFAULT_DESCRIPTION;
		selectBySeverity = DEFAULT_SELECT_BY_SEVERITY;
		severity = DEFAULT_SEVERITY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerFilter#restoreFilterSettings(org.eclipse.jface.dialogs.IDialogSettings)
	 */
	public void restoreFilterSettings(IDialogSettings settings) {

		super.restoreFilterSettings(settings);

		String setting = settings.get(TAG_CONTAINS);

		if (setting != null) {
			contains = Boolean.valueOf(setting).booleanValue();
		}

		setting = settings.get(TAG_DESCRIPTION);

		if (setting != null) {
			description = new String(setting);
		}

		setting = settings.get(TAG_SELECT_BY_SEVERITY);

		if (setting != null) {
			selectBySeverity = Boolean.valueOf(setting).booleanValue();
		}

		setting = settings.get(TAG_SEVERITY);

		if (setting != null) {
			try {
				severity = Integer.parseInt(setting);
			} catch (NumberFormatException eNumberFormat) {
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerFilter#restoreFilterSettings(org.eclipse.ui.IMemento)
	 */
	protected void restoreFilterSettings(IMemento memento) {

		super.restoreFilterSettings(memento);

		String setting = memento.getString(TAG_CONTAINS);

		if (setting != null) {
			contains = Boolean.valueOf(setting).booleanValue();
		}

		setting = memento.getString(TAG_DESCRIPTION);

		if (setting != null) {
			description = new String(setting);
		}

		setting = memento.getString(TAG_SELECT_BY_SEVERITY);

		if (setting != null) {
			selectBySeverity = Boolean.valueOf(setting).booleanValue();
		}

		Integer severitySetting = memento.getInteger(TAG_SEVERITY);

		if (setting != null) {
			severity = severitySetting.intValue();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerFilter#saveFilterSettings(org.eclipse.ui.IMemento)
	 */
	public void saveFilterSettings(IMemento settings) {
		super.saveFilterSettings(settings);
		settings.putString(TAG_CONTAINS, String.valueOf(contains));
		settings.putString(TAG_DESCRIPTION, description);
		settings.putString(TAG_SELECT_BY_SEVERITY, String
				.valueOf(selectBySeverity));
		settings.putInteger(TAG_SEVERITY, severity);

	}

	/**
	 * Get the id of the filter. <code>null</code> if the filter is user
	 * defined.
	 * 
	 * @return String
	 */
	public String getId() {
		if (contributionDescriptor == null) {
			return null;
		}
		return contributionDescriptor.getLocalId();
	}

	void createContributionFrom(IConfigurationElement element) {
		final String id = element.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_ID);
		final String namespace = element.getNamespace();
		contributionDescriptor = new IPluginContribution() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.IPluginContribution#getLocalId()
			 */
			public String getLocalId() {
				return id;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.IPluginContribution#getPluginId()
			 */
			public String getPluginId() {
				return namespace;
			}
		};
	}

	/**
	 * Return whether or not the receiver will be filtered out due to an
	 * activity match.
	 * 
	 * @return boolean <code>true</code> if it is filtered out.
	 */
	public boolean isFilteredOutByActivity() {
		if (contributionDescriptor == null) {
			return false;
		}
		if (identifier == null) {
			identifier = WorkbenchActivityHelper
					.getIdentifier(contributionDescriptor);
		}
		return !identifier.isEnabled();
	}

	public boolean isEnabled() {
		return super.isEnabled() && !isFilteredOutByActivity();
	}

}
