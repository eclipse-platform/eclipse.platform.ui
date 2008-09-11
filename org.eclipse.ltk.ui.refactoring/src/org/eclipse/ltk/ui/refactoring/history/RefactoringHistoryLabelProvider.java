/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring.history;

import java.text.Format;
import java.util.Date;
import java.util.Locale;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.internal.ui.refactoring.Messages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringDescriptorImageDescriptor;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryDate;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryEntry;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryNode;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringImageDescriptor;

/**
 * Label provider to display a refactoring history. This label provider can be
 * used in conjunction with {@link RefactoringHistoryContentProvider} or
 * directly on {@link RefactoringDescriptorProxy} and {@link RefactoringHistory}
 * elements.
 * <p>
 * Note: this class is not indented to be subclassed outside the refactoring
 * framework.
 * </p>
 *
 * @see IRefactoringHistoryControl
 * @see RefactoringHistoryControlConfiguration
 * @see RefactoringHistoryContentProvider
 *
 * @since 3.2
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RefactoringHistoryLabelProvider extends LabelProvider {

	/** The collection image */
	private final Image fCollectionImage;

	/** The container image */
	private final Image fContainerImage;

	/** The control configuration to use */
	private final RefactoringHistoryControlConfiguration fControlConfiguration;

	/** The current locale to use */
	private final Locale fCurrentLocale;

	/** The cached date format, or <code>null</code> */
	private DateFormat fDateFormat= null;

	/** The decorated element image */
	private Image fDecoratedElementImage= null;

	/** The decorated item image */
	private Image fDecoratedItemImage= null;

	/** The element image */
	private final Image fElementImage;

	/** The item image */
	private final Image fItemImage;

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
		fCurrentLocale= new Locale(RefactoringUIMessages.RefactoringHistoryLabelProvider_label_language, RefactoringUIMessages.RefactoringHistoryLabelProvider_label_country, RefactoringUIMessages.RefactoringHistoryLabelProvider_label_variant);
	}

	/**
	 * Decorates the image for the specified element.
	 *
	 * @param image
	 *            the image to decorate
	 * @param element
	 *            the associated element
	 * @return the decorated image
	 */
	private Image decorateImage(final Image image, final Object element) {
		Image result= image;
		RefactoringDescriptorProxy extended= null;
		if (element instanceof RefactoringHistoryEntry)
			extended= ((RefactoringHistoryEntry) element).getDescriptor();
		else if (element instanceof RefactoringDescriptorProxy)
			extended= (RefactoringDescriptorProxy) element;
		if (extended != null) {
			final String project= extended.getProject();
			if (project == null || "".equals(project)) { //$NON-NLS-1$
				if (image == fElementImage && fDecoratedElementImage != null)
					result= fDecoratedElementImage;
				else if (image == fItemImage && fDecoratedItemImage != null)
					result= fDecoratedItemImage;
				else {
					final Rectangle bounds= image.getBounds();
					result= new RefactoringDescriptorImageDescriptor(new RefactoringImageDescriptor(image), RefactoringDescriptorImageDescriptor.WORKSPACE, new Point(bounds.width, bounds.height)).createImage();
					if (image == fElementImage)
						fDecoratedElementImage= result;
					else if (image == fItemImage)
						fDecoratedItemImage= result;
				}
			}
		}
		return result;
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
		if (fDecoratedElementImage != null)
			fDecoratedElementImage.dispose();
		if (fDecoratedItemImage != null)
			fDecoratedItemImage.dispose();
	}

	/**
	 * Returns a cached date format for the current locale.
	 *
	 * @return a cached date format for the current locale
	 */
	private DateFormat getDateFormat() {
		if (fDateFormat == null)
			fDateFormat= DateFormat.getTimeInstance(DateFormat.SHORT);
		return fDateFormat;
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
				return Messages.format(fControlConfiguration.getRefactoringPattern(), new String[] { getDateFormat().format(new Date(stamp)), descriptor.getDescription()});
		}
		return descriptor.getDescription();
	}

	/**
	 * {@inheritDoc}
	 */
	public Image getImage(final Object element) {
		Image image= null;
		final boolean time= fControlConfiguration.isTimeDisplayed();
		if (element instanceof RefactoringHistoryEntry || element instanceof RefactoringDescriptorProxy)
			image= time ? fElementImage : fItemImage;
		else
			image= time ? fContainerImage : fCollectionImage;
		return decorateImage(image, element);
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
								format= new SimpleDateFormat(RefactoringUIMessages.RefactoringHistoryLabelProvider_this_week_format, fCurrentLocale);
								break;
							case RefactoringHistoryNode.LAST_WEEK:
								pattern= fControlConfiguration.getLastWeekPattern();
								format= new SimpleDateFormat(RefactoringUIMessages.RefactoringHistoryLabelProvider_last_week_format, fCurrentLocale);
								break;
							case RefactoringHistoryNode.WEEK:
								pattern= fControlConfiguration.getWeekPattern();
								format= new SimpleDateFormat(RefactoringUIMessages.RefactoringHistoryLabelProvider_week_format, fCurrentLocale);
								break;
							case RefactoringHistoryNode.YEAR:
								pattern= fControlConfiguration.getYearPattern();
								format= new SimpleDateFormat(RefactoringUIMessages.RefactoringHistoryLabelProvider_year_format, fCurrentLocale);
								break;
							case RefactoringHistoryNode.THIS_MONTH:
								pattern= fControlConfiguration.getThisMonthPattern();
								format= new java.text.SimpleDateFormat(RefactoringUIMessages.RefactoringHistoryLabelProvider_this_month_format, fCurrentLocale);
								break;
							case RefactoringHistoryNode.LAST_MONTH:
								pattern= fControlConfiguration.getLastMonthPattern();
								format= new java.text.SimpleDateFormat(RefactoringUIMessages.RefactoringHistoryLabelProvider_last_month_format, fCurrentLocale);
								break;
							case RefactoringHistoryNode.MONTH:
								pattern= fControlConfiguration.getMonthPattern();
								format= new java.text.SimpleDateFormat(RefactoringUIMessages.RefactoringHistoryLabelProvider_month_format, fCurrentLocale);
								break;
							case RefactoringHistoryNode.DAY:
								pattern= fControlConfiguration.getDayPattern();
								final int type= node.getParent().getKind();
								if (type == RefactoringHistoryNode.THIS_WEEK || type == RefactoringHistoryNode.LAST_WEEK) {
									final SimpleDateFormat simple= new SimpleDateFormat(RefactoringUIMessages.RefactoringHistoryLabelProvider_day_format, fCurrentLocale);
									buffer.append(Messages.format(RefactoringUIMessages.RefactoringHistoryControlConfiguration_day_detailed_pattern, new String[] { simple.format(stamp), DateFormat.getDateInstance().format(stamp)}));
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
							buffer.append(Messages.format(pattern,format.format(stamp)));
					}
				}
			}
			return buffer.toString();
		}
		return super.getText(element);
	}
}
