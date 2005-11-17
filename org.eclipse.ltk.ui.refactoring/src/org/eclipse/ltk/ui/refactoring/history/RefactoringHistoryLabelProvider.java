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

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;

import org.eclipse.ltk.internal.ui.refactoring.Assert;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryDate;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryEntry;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryNode;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.LabelProvider;

/**
 * Label provider to display a refactoring history.
 * <p>
 * Note: this class is not indented to be subclassed outside the refactoring
 * framework.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
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
		fItemImage= RefactoringPluginImages.DESC_OBJS_DEFAULT_CHANGE.createImage();
		fContainerImage= RefactoringPluginImages.DESC_OBJS_REFACTORING_DATE.createImage();
		fElementImage= RefactoringPluginImages.DESC_OBJS_REFACTORING_TIME.createImage();
		fCollectionImage= RefactoringPluginImages.DESC_OBJS_COMPOSITE_CHANGE.createImage();
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
	 * {@inheritDoc}
	 */
	public Image getImage(final Object element) {
		final boolean time= fControlConfiguration.isTimeDisplayed();
		if (element instanceof RefactoringHistoryEntry)
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
			final RefactoringDescriptorProxy proxy= entry.getDescriptor();
			if (fControlConfiguration.isTimeDisplayed()) {
				final long stamp= proxy.getTimeStamp();
				if (stamp > 0)
					return MessageFormat.format(fControlConfiguration.getRefactoringPattern(), new String[] { DateFormat.getTimeInstance().format(new Date(stamp)), proxy.getDescription()});
			}
			return proxy.getDescription();
		} else if (element instanceof RefactoringHistoryNode) {
			final RefactoringHistoryNode node= (RefactoringHistoryNode) element;
			final StringBuffer buffer= new StringBuffer(32);
			final int kind= node.getKind();
			switch (kind) {
				case RefactoringHistoryNode.COLLECTION:
					buffer.append(fControlConfiguration.getCollectionLabel());
					break;
				case RefactoringHistoryNode.LAST_WEEK:
					buffer.append(fControlConfiguration.getLastWeekLabel());
					break;
				case RefactoringHistoryNode.THIS_WEEK:
					buffer.append(fControlConfiguration.getThisWeekLabel());
					break;
				case RefactoringHistoryNode.THIS_MONTH:
					buffer.append(fControlConfiguration.getThisMonthLabel());
					break;
				case RefactoringHistoryNode.LAST_MONTH:
					buffer.append(fControlConfiguration.getLastMonthLabel());
					break;
				default: {
					if (node instanceof RefactoringHistoryDate) {
						final RefactoringHistoryDate date= (RefactoringHistoryDate) node;
						final long stamp= date.getTimeStamp();
						Format format= null;
						String pattern= ""; //$NON-NLS-1$
						switch (kind) {
							case RefactoringHistoryNode.WEEK:
								pattern= fControlConfiguration.getWeekPattern();
								format= new SimpleDateFormat("ww"); //$NON-NLS-1$
								break;
							case RefactoringHistoryNode.YEAR:
								pattern= fControlConfiguration.getYearPattern();
								format= new SimpleDateFormat("yyyy"); //$NON-NLS-1$
								break;
							case RefactoringHistoryNode.MONTH:
								pattern= fControlConfiguration.getMonthPattern();
								format= new SimpleDateFormat("MMMMM"); //$NON-NLS-1$
								break;
							case RefactoringHistoryNode.DAY:
								pattern= fControlConfiguration.getDayPattern();
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
						buffer.append(MessageFormat.format(pattern, new String[] { format.format(new Date(stamp))}));
					}
				}
			}
			return buffer.toString();
		}
		return super.getText(element);
	}
}