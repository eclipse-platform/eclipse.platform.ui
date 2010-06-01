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

import java.util.Collection;
import java.util.Map;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.statushandlers.AbstractStatusAreaProvider;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.WorkbenchStatusDialogManager;

/**
 * This class contains constant necessary to read/write the
 * {@link WorkbenchStatusDialogManager} properties. Some properties may be
 * promoted to the API. Some of those properties are used to configure the
 * dialog, while others are used to describe the state of the dialgo.
 * 
 */
public interface IStatusDialogConstants {
	
	/**
	 * This property can be only read. It will return the current dialog
	 * {@link Shell}. It may be null.
	 */
	public static final Object SHELL = Shell.class;
	
	/**
	 * This property indicates if the support area should be opened when the
	 * dialog appears. The value must be of {@link Boolean} type.
	 * {@link Boolean#TRUE} means that the support area will be opened, while
	 * {@link Boolean#FALSE} means that it will be in the closed state.
	 */
	public static final Object SHOW_SUPPORT = new Object();
	
	/**
	 * This property indicates if the dialog should display a link to the Error
	 * Log if the Error Log view is available. The value must be of
	 * {@link Boolean} type. {@link Boolean#TRUE} means that the link will be
	 * present if the Error Log is available, {@link Boolean#FALSE} means that
	 * it will never be displayed.
	 */
	public static final Object ERRORLOG_LINK = new Object();

	/**
	 * This property indicates if the dialog should threat {@link Status}es with
	 * severity {@link IStatus#OK} as all other statuses. The value must be of
	 * {@link Boolean} type. A {@link Boolean#TRUE} means that those
	 * {@link Status}es will be handled as all others, while
	 * {@link Boolean#FALSE} means that they will be silently ignored.
	 */
	public static final Object HANDLE_OK_STATUSES = new Object();

	/**
	 * This property indicates how the dialog should be named. The default value
	 * comes from jface resources. It may be {@link String} or <code>null</code>
	 * .
	 */
	public static final Object TITLE = new Object();

	/**
	 * This property indicates which status severities should be handled. The
	 * value must be of {@link Integer} type.
	 */
	public static final Object MASK = new Object();

	/**
	 * This flag indicates if the details area was opened before switching the
	 * modality or not. It must be of {@link Boolean} type.
	 */
	public static final Object DETAILS_OPENED = new Object();

	/**
	 * This flag indicates if the support area was opened before switching the
	 * modality or not. It must be of {@link Boolean} type.
	 */
	public static final Object TRAY_OPENED = new Object();

	/**
	 * This flag controls if the support area is opened automatically when the
	 * dialog is opened. It must be of {@link Boolean} type or <code>null</code>
	 * .
	 */
	public static final Object ENABLE_DEFAULT_SUPPORT_AREA = new Object();

	/**
	 * This flag controls if there should be a control allowing for support
	 * opening. It must be of {@link Boolean} type or <code>null</code>.
	 */
	public static final Object HIDE_SUPPORT_BUTTON = new Object();

	/**
	 * This property holds custom support provider which will be used do display
	 * support area for currently selected {@link StatusAdapter}. It must be of
	 * {@link AbstractStatusAreaProvider} or <code>null</code>.
	 */
	public static final Object CUSTOM_SUPPORT_PROVIDER = new Object();

	/**
	 * This property holds custom details provider which will be used do display
	 * details for currently selected {@link StatusAdapter}. It must be of
	 * {@link AbstractStatusAreaProvider} or <code>null</code>.
	 */
	public static final Object CUSTOM_DETAILS_PROVIDER = new Object();

	/**
	 * Currently selected status adapter. It must be of {@link StatusAdapter}
	 * type.
	 */
	public static final Object CURRENT_STATUS_ADAPTER = new Object();

	/**
	 * This property allows for retrieving the list of {@link StatusAdapter}
	 * currently kept by the dialog. The corresponding object must be of
	 * {@link Collection} type.
	 */
	public static final Object STATUS_ADAPTERS = new Object();

	/**
	 * Stores "modal" flags describing {@link StatusAdapter}. It is a
	 * {@link Map} of {@link StatusAdapter}s and {@link Boolean}s.
	 */
	public static final Object STATUS_MODALS = new Object();

	/**
	 * This fields holds the information about dialog position and size when
	 * switching the modality.
	 */
	public static final Object SHELL_BOUNDS = new Object();

	/**
	 * This property stores internal {@link LabelProviderWrapper} which is
	 * responsible for providing all the text and images. This property should
	 * be never changed.
	 */
	public static final Object LABEL_PROVIDER = new Object();

	/**
	 * This property stores custom label provider. LABEL_PROVIDER uses
	 * CUSTOM_LABEL_PROVIDER or default label provider to deliver images and
	 * text to the dialog. Since the custom label provider is not able to
	 * deliver all necessary images, this field should not be used. It is kept
	 * only for backward compatibility.
	 */
	public static final Object CUSTOM_LABEL_PROVIDER = new Object();

	/**
	 * If it is necessary to modify each message, this property may be used. It
	 * should be of {@link ILabelDecorator} type. This decorator will be invoked
	 * for each text that will be later passed to the dialog.
	 */
	public static final Object DECORATOR = new Object();

	/**
	 * This flag indicates if the dialog is during modality switch state. It
	 * must be of {@link Boolean} type.
	 */
	public static final Object MODALITY_SWITCH = new Object();

	/**
	 * This flag controls animations. It is for testing purposes only. If it set
	 * to false, animations will not be created. Animation means shell
	 * closing/opening animation.
	 */
	public static final Object ANIMATION = new Object();

	public static final Object MANAGER_IMPL = WorkbenchStatusDialogManagerImpl.class;
}
