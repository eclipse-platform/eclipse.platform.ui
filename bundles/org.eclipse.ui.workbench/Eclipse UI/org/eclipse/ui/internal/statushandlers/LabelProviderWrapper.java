/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.statushandlers;

import com.ibm.icu.text.DateFormat;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.progress.ProgressManager;
import org.eclipse.ui.internal.progress.ProgressMessages;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.statushandlers.IStatusAdapterConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;


/**
 * This is an utility class which is responsible for text and icon decorators in
 * the StatusDialog.
 * 
 * @since 3.6
 */
public class LabelProviderWrapper extends ViewerComparator implements
		ITableLabelProvider {
	/**
	 * The default status label provider.
	 */
	private class DefaultLabelProvider implements ITableLabelProvider {
		ResourceManager manager = new LocalResourceManager(JFaceResources
				.getResources());

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse
		 * .jface.viewers.ILabelProviderListener)
		 */
		public void addListener(ILabelProviderListener listener) {
			// Do nothing
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose() {
			manager.dispose();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java
		 * .lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			Image result = null;
			if (element != null) {
				StatusAdapter statusAdapter = ((StatusAdapter) element);
				Job job = (Job) (statusAdapter.getAdapter(Job.class));
				if (job != null) {
					result = getIcon(job);
				}
			}
			// if somehow disposed image was received (should not happen)
			if (result != null && result.isDisposed()) {
				result = null;
			}
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.
		 * lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			StatusAdapter statusAdapter = (StatusAdapter) element;
			String text = WorkbenchMessages.WorkbenchStatusDialog_ProblemOccurred;
			if (!isMulti()) {
				Job job = (Job) (statusAdapter.getAdapter(Job.class));
				if (job != null) {
					text = getPrimaryMessage(statusAdapter);
				} else {
					text = getSecondaryMessage(statusAdapter);
				}
			} else {
				Job job = (Job) (statusAdapter.getAdapter(Job.class));
				if (job != null) {
					text = job.getName();
				} else {
					text = getPrimaryMessage(statusAdapter);
				}
			}
			Long timestamp = (Long) statusAdapter
					.getProperty(IStatusAdapterConstants.TIMESTAMP_PROPERTY);

			if (timestamp != null && isMulti()) {
				String date = DateFormat.getDateTimeInstance(DateFormat.LONG,
						DateFormat.LONG)
						.format(new Date(timestamp.longValue()));
				return NLS.bind(ProgressMessages.JobInfo_Error, (new Object[] {
						text, date }));
			}
			return text;
		}

		/*
		 * Get the icon for the job.
		 */
		private Image getIcon(Job job) {
			if (job != null) {
				Object property = job
						.getProperty(IProgressConstants.ICON_PROPERTY);

				// Create an image from the job's icon property or family
				if (property instanceof ImageDescriptor) {
					return manager.createImage((ImageDescriptor) property);
				} else if (property instanceof URL) {
					return manager.createImage(ImageDescriptor
							.createFromURL((URL) property));
				} else {
					// Let the progress manager handle the resource management
					return ProgressManager.getInstance().getIconFor(job);
				}
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java
		 * .lang.Object, java.lang.String)
		 */
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse
		 * .jface.viewers.ILabelProviderListener)
		 */
		public void removeListener(ILabelProviderListener listener) {
			// Do nothing
		}
	}

	private ITableLabelProvider labelProvider;

	/**
	 * This field stores the decorator which can override various texts produced
	 * by this class.
	 */
	private ILabelDecorator messageDecorator;

	private Map dialogState;

	/**
	 * @param dialogState
	 */
	public LabelProviderWrapper(Map dialogState) {
		this.dialogState = dialogState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang
	 * .Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		return labelProvider.getColumnImage(element, columnIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang
	 * .Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		return getLabelProvider().getColumnText(element, columnIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.
	 * jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
		getLabelProvider().addListener(listener);
	}

	/**
	 * This method disposes the label provider if and only if the dialog is not
	 * changing its state.
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		boolean modalitySwitch = ((Boolean) dialogState.get(IStatusDialogConstants.MODALITY_SWITCH))
				.booleanValue();
		if (!modalitySwitch) {
			getLabelProvider().dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang
	 * .Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return getLabelProvider().isLabelProperty(element, property);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse
	 * .jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
		getLabelProvider().removeListener(listener);
	}

	/**
	 * Gets {@link Image} associated with current {@link StatusAdapter}
	 * severity.
	 * 
	 * @param statusAdapter
	 * 
	 * @return {@link Image} associated with current {@link StatusAdapter}
	 *         severity.
	 */
	public Image getImage(StatusAdapter statusAdapter) {
		if (statusAdapter != null) {
			int severity = statusAdapter.getStatus().getSeverity();
			switch (severity) {
			case IStatus.OK:
			case IStatus.INFO:
			case IStatus.CANCEL:
				return getSWTImage(SWT.ICON_INFORMATION);
			case IStatus.WARNING:
				return getSWTImage(SWT.ICON_WARNING);
			default: /* IStatus.ERROR */
				return getSWTImage(SWT.ICON_ERROR);
			}
		}
		// should not never happen but if we do not know what is going on, then
		// return error image.
		return getSWTImage(SWT.ICON_ERROR);
	}

	/**
	 * Get an <code>Image</code> from the provide SWT image constant.
	 * 
	 * @param imageID
	 *            the SWT image constant
	 * @return image the image
	 */
	public Image getSWTImage(final int imageID) {
		return Display.getCurrent().getSystemImage(imageID);
	}

	/**
	 * This method computes the dialog main message.
	 * 
	 * If there is only one reported status adapter, main message should be:
	 * <ul>
	 * <li>information about job that reported an error.</li>
	 * <li>primary message, if the statusAdapter was not reported by job</li>
	 * </ul>
	 * 
	 * If there is more reported statusAdapters, main message should be:
	 * <ul>
	 * <li>primary message for job reported statusAdapters</li>
	 * <li>secondary message for statuses not reported by jobs</li>
	 * </ul>
	 * 
	 * If nothing can be found, some general information should be displayed.
	 * 
	 * @param statusAdapter
	 *            A status adapter which is used as the base for computation.
	 * @return main message of the dialog.
	 * 
	 * @see #getPrimaryMessage(StatusAdapter)
	 * @see #getSecondaryMessage(StatusAdapter)
	 */
	public String getMainMessage(StatusAdapter statusAdapter) {
		if (!isMulti()) {
			Job job = (Job) (statusAdapter.getAdapter(Job.class));
			// job
			if (job != null) {
				return NLS
						.bind(
								WorkbenchMessages.WorkbenchStatusDialog_ProblemOccurredInJob,
								job.getName());
			}
			// we are not handling job
			return getPrimaryMessage(statusAdapter);
		}
		// we have a list. primary message or job name or on the list name (both
		// with timestamp if available).
		// we display secondary message or status
		if (isMulti()) {
			Job job = (Job) (statusAdapter.getAdapter(Job.class));
			// job
			if (job != null) {
				return getPrimaryMessage(statusAdapter);
			}

			// plain status
			return getSecondaryMessage(statusAdapter);
		}
		return WorkbenchMessages.WorkbenchStatusDialog_ProblemOccurred;
	}

	/**
	 * Retrieves primary message from passed statusAdapter. Primary message
	 * should be (from the most important):
	 * <ul>
	 * <li>statusAdapter title</li>
	 * <li>IStatus message</li>
	 * <li>pointing to child statuses if IStatus has them.</li>
	 * <li>exception message</li>
	 * <li>exception class</li>
	 * <li>general message informing about error (no details at all)</li>
	 * </ul>
	 * 
	 * @param statusAdapter
	 *            an status adapter to retrieve primary message from
	 * @return String containing primary message
	 * 
	 * @see #getMainMessage(StatusAdapter)
	 * @see #getSecondaryMessage(StatusAdapter)
	 */
	public String getPrimaryMessage(StatusAdapter statusAdapter) {
		// if there was nonempty title set, display the title
		Object property = statusAdapter
				.getProperty(IStatusAdapterConstants.TITLE_PROPERTY);
		if (property instanceof String) {
			String header = (String) property;
			if (header.trim().length() > 0) {
				return decorate(header, statusAdapter);
			}
		}
		// if there was message set in the status
		IStatus status = statusAdapter.getStatus();
		if (status.getMessage() != null
				&& status.getMessage().trim().length() > 0) {
			return decorate(status.getMessage(), statusAdapter);
		}

		// if status has children
		if (status.getChildren().length > 0) {
			return WorkbenchMessages.WorkbenchStatusDialog_StatusWithChildren;
		}

		// check the exception
		Throwable t = status.getException();
		if (t != null) {
			if (t.getMessage() != null && t.getMessage().trim().length() > 0) {
				return decorate(t.getMessage(), statusAdapter);
			}
			return t.getClass().getName();
		}
		return WorkbenchMessages.WorkbenchStatusDialog_ProblemOccurred;
	}

	/**
	 * Retrieves secondary message from the passed statusAdapter. Secondary
	 * message is one level lower than primary. Secondary message should be
	 * (from the most important):
	 * <ul>
	 * <li>IStatus message</li>
	 * <li>pointing to child statuses if IStatus has them.</li>
	 * <li>exception message</li>
	 * <li>exception class</li>
	 * </ul>
	 * Secondary message should not be the same as primary one. If no secondary
	 * message can be extracted, details should be pointed.
	 * 
	 * @param statusAdapter
	 *            an status adapter to retrieve secondary message from
	 * @return String containing secondary message
	 * 
	 * @see #getMainMessage(StatusAdapter)
	 * @see #getPrimaryMessage(StatusAdapter)
	 */
	public String getSecondaryMessage(StatusAdapter statusAdapter) {
		String primary = getPrimaryMessage(statusAdapter);
		// we can skip the title, it is always displayed as primary message

		// if there was message set in the status
		IStatus status = statusAdapter.getStatus();
		String message = status.getMessage();
		String decoratedMessage = message == null ? null : decorate(message,
				statusAdapter);
		if (message != null && message.trim().length() > 0
				&& !primary.equals(decoratedMessage)) {
			/* we have not displayed it yet */
			return decoratedMessage;
		}
		// if status has children
		if (status.getChildren().length > 0
				&& !primary.equals(decoratedMessage)) {
			return WorkbenchMessages.WorkbenchStatusDialog_StatusWithChildren;
		}

		// check the exception
		Throwable t = status.getException();
		if (t != null) {
			if (t.getMessage() != null) {
				String decoratedThrowable = decorate(t.getMessage(),
						statusAdapter);
				if (t.getMessage().trim().length() > 0
						&& !primary.equals(decoratedThrowable)) {
					return decoratedThrowable;
				}
			}
			String throwableName = t.getClass().getName();
			if (!primary.equals(throwableName)) {
				return throwableName;
			}
		}
		return WorkbenchMessages.WorkbenchStatusDialog_SeeDetails;
	}

	private String decorate(String string, StatusAdapter adapter) {
		messageDecorator = (ILabelDecorator) dialogState
				.get(IStatusDialogConstants.DECORATOR);
		if (messageDecorator != null) {
			string = messageDecorator.decorateText(string, adapter);
		}
		return string;
	}

	private int compare(StatusAdapter s1, StatusAdapter s2) {
		Long timestamp1 = ((Long) s1
				.getProperty(IStatusAdapterConstants.TIMESTAMP_PROPERTY));
		Long timestamp2 = ((Long) s2
				.getProperty(IStatusAdapterConstants.TIMESTAMP_PROPERTY));
		if (timestamp1 == null || timestamp2 == null
				|| (timestamp1.equals(timestamp2))) {
			String text1 = getColumnText(s1, 0);
			String text2 = getColumnText(s2, 0);
			return text1.compareTo(text2);
		}

		if (timestamp1.longValue() < timestamp2.longValue()) {
			return -1;
		}
		if (timestamp1.longValue() > timestamp2.longValue()) {
			return 1;
		}
		// should be never called
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.
	 * viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public int compare(Viewer testViewer, Object o1, Object o2) {
		if (o1 instanceof StatusAdapter && o2 instanceof StatusAdapter) {
			return compare((StatusAdapter) o1, (StatusAdapter) o2);
		}
		// should not happen
		if (o1.hashCode() < o2.hashCode()) {
			return -1;
		}
		if (o2.hashCode() > o2.hashCode()) {
			return 1;
		}
		return 0;
	}

	private boolean isMulti() {
		return ((Collection) dialogState
				.get(IStatusDialogConstants.STATUS_ADAPTERS)).size() > 1;
	}

	/**
	 * @return Returns the labelProvider.
	 */
	public ITableLabelProvider getLabelProvider() {
		ITableLabelProvider temp = (ITableLabelProvider) dialogState
				.get(IStatusDialogConstants.CUSTOM_LABEL_PROVIDER);
		if (temp != null) {
			labelProvider = temp;
		}
		if (labelProvider == null) {
			labelProvider = new DefaultLabelProvider();
		}
		return labelProvider;
	}
}