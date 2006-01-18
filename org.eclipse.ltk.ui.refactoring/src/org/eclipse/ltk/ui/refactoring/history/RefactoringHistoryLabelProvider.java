/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring.history;

import java.text.DateFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.core.runtime.Assert;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryDate;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryEntry;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryNode;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.osgi.util.NLS;

/**
 * Label provider to display a refactoring history. This label provider can be
 * used in conjunction with {@link RefactoringHistoryContentProvider} or
 * directly on {@link RefactoringDescriptorProxy} and {@link RefactoringHistory}
 * elements.
 * <p>
 * Note: this class is not indented to be subclassed outside the refactoring
 * framework.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @see IRefactoringHistoryControl
 * @see RefactoringHistoryControlConfiguration
 * @see RefactoringHistoryContentProvider
 * 
 * @since 3.2
 */
public class RefactoringHistoryLabelProvider extends LabelProvider {

	/** The collection image */
	private Image fCollectionImage= null;

	/** The container image */
	private Image fContainerImage= null;

	/** The resource bundle to use */
	private final RefactoringHistoryControlConfiguration fControlConfiguration;

	/** The element image */
	private Image fElementImage= null;

	/** The item image */
	private Image fItemImage= null;

	/**
	 * Creates a new refactoring history label provider.
	 * 
	 * @param configuration
	 *            the refactoring history control configuration to use
	 */
	public RefactoringHistoryLabelProvider(final RefactoringHistoryControlConfiguration configuration) {
		Assert.isNotNull(configuration);
		fControlConfiguration= configuration;
		fItemImage= RefactoringPluginImages.DESC_OBJS_REFACTORING.createImage();
		fContainerImage= RefactoringPluginImages.DESC_OBJS_REFACTORING_DATE.createImage();
		fElementImage= RefactoringPluginImages.DESC_OBJS_REFACTORING_TIME.createImage();
		fCollectionImage= RefactoringPluginImages.DESC_OBJS_REFACTORING_COLL.createImage();
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
		if (fContainerImage != null)
			fContainerImage.dispose();
		if (fCollectionImage != null)
			fCollectionImage.dispose();
		if (fElementImage != null)
			fElementImage.dispose();
		if (fItemImage != null)
			fItemImage.dispose();
	}

	/**
	 * Returns the label for the specified refactoring descriptor.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor
	 * @return the label of the descriptor
	 */
	private String getDescriptorLabel(final RefactoringDescriptorProxy descriptor) {
		if (fControlConfiguration.isTimeDisplayed()) {
			final long stamp= descriptor.getTimeStamp();
			if (stamp >= 0)
				return MessageFormat.format(fControlConfiguration.getRefactoringPattern(), new String[] { DateFormat.getTimeInstance().format(new Date(stamp)), descriptor.getDescription()});
		}
		return descriptor.getDescription();
	}

	/**
	 * {@inheritDoc}
	 */
	public Image getImage(final Object element) {
		final boolean time= fControlConfiguration.isTimeDisplayed();
		if (element instanceof RefactoringHistoryEntry || element instanceof RefactoringDescriptorProxy)
			return time ? fElementImage : fItemImage;
		else
			return time ? fContainerImage : fCollectionImage;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getText(Object element) {
		if (element instanceof RefactoringHistoryEntry) {
			final RefactoringHistoryEntry entry= (RefactoringHistoryEntry) element;
			return getDescriptorLabel(entry.getDescriptor());
		} else if (element instanceof RefactoringDescriptorProxy) {
			return getDescriptorLabel((RefactoringDescriptorProxy) element);
		} else if (element instanceof RefactoringHistory) {
			return RefactoringUIMessages.RefactoringHistoryControlConfiguration_collection_label;
		} else if (element instanceof RefactoringHistoryNode) {
			final RefactoringHistoryNode node= (RefactoringHistoryNode) element;
			final StringBuffer buffer= new StringBuffer(32);
			final int kind= node.getKind();
			switch (kind) {
				case RefactoringHistoryNode.COLLECTION:
					buffer.append(fControlConfiguration.getCollectionLabel());
					break;
				default: {
					if (node instanceof RefactoringHistoryDate) {
						final RefactoringHistoryDate date= (RefactoringHistoryDate) node;
						final Date stamp= new Date(date.getTimeStamp());
						Format format= null;
						String pattern= ""; //$NON-NLS-1$
						switch (kind) {
							case RefactoringHistoryNode.THIS_WEEK:
								pattern= fControlConfiguration.getThisWeekPattern();
								format= new SimpleDateFormat("w"); //$NON-NLS-1$
								break;
							case RefactoringHistoryNode.LAST_WEEK:
								pattern= fControlConfiguration.getLastWeekPattern();
								format= new SimpleDateFormat("w"); //$NON-NLS-1$
								break;
							case RefactoringHistoryNode.WEEK:
								pattern= fControlConfiguration.getWeekPattern();
								format= new SimpleDateFormat("w"); //$NON-NLS-1$
								break;
							case RefactoringHistoryNode.YEAR:
								pattern= fControlConfiguration.getYearPattern();
								format= new SimpleDateFormat("yyyy"); //$NON-NLS-1$
								break;
							case RefactoringHistoryNode.THIS_MONTH:
								pattern= fControlConfiguration.getThisMonthPattern();
								format= new SimpleDateFormat("MMMMM"); //$NON-NLS-1$
								break;
							case RefactoringHistoryNode.LAST_MONTH:
								pattern= fControlConfiguration.getLastMonthPattern();
								format= new SimpleDateFormat("MMMMM"); //$NON-NLS-1$
								break;
							case RefactoringHistoryNode.MONTH:
								pattern= fControlConfiguration.getMonthPattern();
								format= new SimpleDateFormat("MMMMM"); //$NON-NLS-1$
								break;
							case RefactoringHistoryNode.DAY:
								pattern= fControlConfiguration.getDayPattern();
								final int type= node.getParent().getKind();
								if (type == RefactoringHistoryNode.THIS_WEEK || type == RefactoringHistoryNode.LAST_WEEK) {
									final Locale locale= new Locale(RefactoringUIMessages.RefactoringHistoryLabelProvider_label_language, RefactoringUIMessages.RefactoringHistoryLabelProvider_label_country, RefactoringUIMessages.RefactoringHistoryLabelProvider_label_variant);
									final SimpleDateFormat simple= new SimpleDateFormat("EEEE", locale); //$NON-NLS-1$
									buffer.append(NLS.bind(RefactoringUIMessages.RefactoringHistoryControlConfiguration_day_detailed_pattern, new String[] { simple.format(stamp), DateFormat.getDateInstance().format(stamp)}));
								} else
									format= DateFormat.getDateInstance();
								break;
							case RefactoringHistoryNode.YESTERDAY:
								pattern= fControlConfiguration.getYesterdayPattern();
								format= DateFormat.getDateInstance();
								break;
							case RefactoringHistoryNode.TODAY:
								pattern= fControlConfiguration.getTodayPattern();
								format= DateFormat.getDateInstance();
								break;
						}
						if (format != null)
							buffer.append(NLS.bind(pattern, new String[] { format.format(stamp)}));
					}
				}
			}
			return buffer.toString();
		}
		return super.getText(element);
	}
}