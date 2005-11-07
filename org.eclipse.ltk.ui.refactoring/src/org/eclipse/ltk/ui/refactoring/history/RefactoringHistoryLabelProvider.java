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
import java.util.ResourceBundle;

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
 * This label provider can be customized by providing a resource bundle, which
 * contains localized strings for each of the <code>XXX_FORMAT</code> and the
 * <code>XXX_COLLECTION</code> constants in this class.
 * </p>
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

	/** The day format key */
	public static final String DAY_FORMAT= "dayFormat"; //$NON-NLS-1$

	/** The last month format key */
	public static final String LAST_MONTH_FORMAT= "lastMonthFormat"; //$NON-NLS-1$

	/** The last week format key */
	public static final String LAST_WEEK_FORMAT= "lastWeekFormat"; //$NON-NLS-1$

	/** The month format key */
	public static final String MONTH_FORMAT= "monthFormat"; //$NON-NLS-1$

	/** The refactoring collection key */
	public static final String REFACTORING_COLLECTION= "refactoringCollection"; //$NON-NLS-1$

	/** The refactoring format key */
	public static final String REFACTORING_FORMAT= "refactoringFormat"; //$NON-NLS-1$

	/** The this month format key */
	public static final String THIS_MONTH_FORMAT= "thisMonthFormat"; //$NON-NLS-1$

	/** The this week format key */
	public static final String THIS_WEEK_FORMAT= "thisWeekFormat"; //$NON-NLS-1$

	/** The today format key */
	public static final String TODAY_FORMAT= "todayFormat"; //$NON-NLS-1$

	/** The week format key */
	public static final String WEEK_FORMAT= "weekFormat"; //$NON-NLS-1$

	/** The year format key */
	public static final String YEAR_FORMAT= "yearFormat"; //$NON-NLS-1$

	/** The yesterday format key */
	public static final String YESTERDAY_FORMAT= "yesterdayFormat"; //$NON-NLS-1$

	/** The collection image */
	private Image fCollectionImage= null;

	/** The container image */
	private Image fContainerImage= null;

	/** Should time information be displayed? */
	private boolean fDisplayTime= true;

	/** The element image */
	private Image fElementImage= null;

	/** The item image */
	private Image fItemImage= null;

	/** The resource bundle to use */
	private final ResourceBundle fResourceBundle;

	/**
	 * Creates a new refactoring history label provider.
	 * 
	 * @param bundle
	 *            the resource bundle to use
	 */
	public RefactoringHistoryLabelProvider(final ResourceBundle bundle) {
		Assert.isNotNull(bundle);
		fResourceBundle= bundle;
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
		if (element instanceof RefactoringHistoryEntry)
			return fDisplayTime ? fElementImage : fItemImage;
		else
			return fDisplayTime ? fContainerImage : fCollectionImage;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getText(Object element) {
		if (element instanceof RefactoringHistoryEntry) {
			final RefactoringHistoryEntry entry= (RefactoringHistoryEntry) element;
			final RefactoringDescriptorProxy proxy= entry.getDescriptor();
			if (fDisplayTime) {
				final long stamp= proxy.getTimeStamp();
				if (stamp > 0)
					return MessageFormat.format(fResourceBundle.getString(REFACTORING_FORMAT), new String[] { DateFormat.getTimeInstance().format(new Date(stamp)), proxy.getDescription()});
			}
			return proxy.getDescription();
		} else if (element instanceof RefactoringHistoryNode) {
			final RefactoringHistoryNode node= (RefactoringHistoryNode) element;
			final StringBuffer buffer= new StringBuffer(32);
			final int kind= node.getKind();
			switch (kind) {
				case RefactoringHistoryNode.COLLECTION:
					buffer.append(fResourceBundle.getString(REFACTORING_COLLECTION));
					break;
				case RefactoringHistoryNode.LAST_WEEK:
					buffer.append(fResourceBundle.getString(LAST_WEEK_FORMAT));
					break;
				case RefactoringHistoryNode.THIS_WEEK:
					buffer.append(fResourceBundle.getString(THIS_WEEK_FORMAT));
					break;
				case RefactoringHistoryNode.THIS_MONTH:
					buffer.append(fResourceBundle.getString(THIS_MONTH_FORMAT));
					break;
				case RefactoringHistoryNode.LAST_MONTH:
					buffer.append(fResourceBundle.getString(LAST_MONTH_FORMAT));
					break;
				default: {
					if (node instanceof RefactoringHistoryDate) {
						final RefactoringHistoryDate date= (RefactoringHistoryDate) node;
						final long stamp= date.getTimeStamp();
						Format format= null;
						String key= ""; //$NON-NLS-1$
						switch (kind) {
							case RefactoringHistoryNode.WEEK:
								key= WEEK_FORMAT;
								format= new SimpleDateFormat("ww"); //$NON-NLS-1$
								break;
							case RefactoringHistoryNode.YEAR:
								key= YEAR_FORMAT;
								format= new SimpleDateFormat("yyyy"); //$NON-NLS-1$
								break;
							case RefactoringHistoryNode.MONTH:
								key= MONTH_FORMAT;
								format= new SimpleDateFormat("MMMMM"); //$NON-NLS-1$
								break;
							case RefactoringHistoryNode.DAY:
								key= DAY_FORMAT;
								format= DateFormat.getDateInstance();
								break;
							case RefactoringHistoryNode.YESTERDAY:
								key= YESTERDAY_FORMAT;
								format= DateFormat.getDateInstance();
								break;
							case RefactoringHistoryNode.TODAY:
								key= TODAY_FORMAT;
								format= DateFormat.getDateInstance();
								break;
						}
						buffer.append(MessageFormat.format(fResourceBundle.getString(key), new String[] { format.format(new Date(stamp))}));
					}
				}
			}
			return buffer.toString();
		}
		return super.getText(element);
	}

	/**
	 * Determines whether time information should be displayed.
	 * <p>
	 * Note: the default value is <code>true</code>.
	 * </p>
	 * 
	 * @param display
	 *            <code>true</code> to display time information,
	 *            <code>false</code> otherwise
	 */
	public void setDisplayTime(final boolean display) {
		fDisplayTime= display;
	}
}
