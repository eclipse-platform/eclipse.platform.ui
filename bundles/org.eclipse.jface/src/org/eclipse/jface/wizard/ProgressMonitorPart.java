/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Eugene Ostroukhov <eugeneo@symbian.org> -  Bug 287887 [Wizards] [api] Cancel button has two distinct roles
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 440270
 *     Jan-Ove Weichel <janove.weichel@vogella.com> - Bug 475879
 *     Karsten Thoms <karsten.thoms@itemis.de> - Bug 520720 Asynchronous, throttled label update
 *******************************************************************************/
package org.eclipse.jface.wizard;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.time.Duration;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Throttler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * A standard implementation of an IProgressMonitor. It consists
 * of a label displaying the task and subtask name, and a
 * progress indicator to show progress. In contrast to
 * <code>ProgressMonitorDialog</code> this class only implements
 * <code>IProgressMonitor</code>.
 */
public class ProgressMonitorPart extends Composite implements
		IProgressMonitorWithBlocking {

	/** the label */
	protected Label fLabel;

	/** the current task name */
	protected String fTaskName;

	/** the current sub task name */
	protected String fSubTaskName;

	/** the progress indicator */
	protected ProgressIndicator fProgressIndicator;

	/** the cancel component */
	protected Control fCancelComponent;

	/** true if canceled */
	protected volatile boolean fIsCanceled;

	/** current blocked status */
	protected IStatus blockedStatus;

	/** the cancel lister attached to the cancel component */
	protected Listener fCancelListener = e -> {
		setCanceled(true);
		if (fCancelComponent != null) {
			fCancelComponent.setEnabled(false);
		}
	};

	/** toolbar for managing stop button **/
	private ToolBar fToolBar;

	/** default tool item for canceling tasks **/
	private ToolItem fStopButton;

	/** <code>true</code> if this monitor part should show stop button **/
	private boolean fHasStopButton = false;

	private Throttler throttledUpdate;

	/**
	 * Creates a <code>ProgressMonitorPart</code> that does not provide a stop button.
	 *
	 * @param parent The SWT parent of the part.
	 * @param layout The SWT grid layout used by the part. A client can supply the layout to control
	 *            how the progress monitor part is laid out. If <code>null</code> is passed the part
	 *            uses its default layout.
	 */
	public ProgressMonitorPart(Composite parent, Layout layout) {
		this(parent, layout, false);
	}

	/**
	 * Creates a <code>ProgressMonitorPart</code> that does not provide a stop button.
	 *
	 * @param parent The SWT parent of the part.
	 * @param layout The SWT grid layout used by the part. A client can supply the layout to control
	 *            how the progress monitor part is laid out. If <code>null</code> is passed the part
	 *            uses its default layout.
	 * @param progressIndicatorHeight The height of the progress indicator in pixels. This value may
	 *            be SWT.DEFAULT in order to get the default height as calculated by the widget and
	 *            its layout.
	 */
	public ProgressMonitorPart(Composite parent, Layout layout, int progressIndicatorHeight) {
		super(parent, SWT.NONE);
		initialize(layout, progressIndicatorHeight);
	}

	/**
	 * Creates a <code>ProgressMonitorPart</code>.
	 *
	 * @param parent the SWT parent of the part
	 * @param layout the SWT grid layout used by the part. A client can supply the layout to control
	 *            how the progress monitor part is laid out. If <code>null</code> is passed the part
	 *            uses its default layout.
	 * @param createStopButton <code>true</code> if the progress indicator should include a stop
	 *            button that can be used to cancel any currently running task, and
	 *            <code>false</code> if no such stop button should be created.
	 *
	 * @since 3.6
	 */
	public ProgressMonitorPart(Composite parent, Layout layout, boolean createStopButton) {
		super(parent, SWT.NONE);
		fHasStopButton= createStopButton;
		initialize(layout, SWT.DEFAULT);
	}

	/**
	 * Attaches the progress monitor part to the given cancel component.
	 *
	 * @param cancelComponent the control whose selection will trigger a cancel. This parameter will
	 *            be ignored and hence can be <code>null</code> if a stop button was requested upon
	 *            construction and instead the stop button will enabled and serve as the cancel
	 *            component.
	 * @see #ProgressMonitorPart(Composite, Layout, boolean)
	 */
	public void attachToCancelComponent(Control cancelComponent) {
		if (fHasStopButton)
			setCancelEnabled(true);
		else {
			fCancelComponent = cancelComponent;
			fCancelComponent.addListener(SWT.Selection, fCancelListener);
		}
	}

	@Override
	public void beginTask(String name, int totalWork) {
		fTaskName = name;
		fSubTaskName = ""; //$NON-NLS-1$
		queueUpdateLabel();
		if (!fProgressIndicator.isDisposed()) {
			if (totalWork == IProgressMonitor.UNKNOWN || totalWork == 0) {
				fProgressIndicator.beginAnimatedTask();
			} else {
				fProgressIndicator.beginTask(totalWork);
			}
		}
		if (fToolBar != null && !fToolBar.isDisposed()) {
			fToolBar.setVisible(true);
			fToolBar.setFocus();
		}
	}

	@Override
	public void done() {
		if (!fLabel.isDisposed()) {
			fLabel.setText("");//$NON-NLS-1$
		}
		fSubTaskName = ""; //$NON-NLS-1$
		if (!fProgressIndicator.isDisposed()) {
			fProgressIndicator.sendRemainingWork();
			fProgressIndicator.done();
		}
		if (fToolBar != null && !fToolBar.isDisposed())
			fToolBar.setVisible(false);
	}

	/**
	 * Escapes any occurrence of '&amp;' in the given String so that it is not
	 * considered as a mnemonic character in SWT ToolItems, MenuItems, Button and
	 * Labels.
	 *
	 * @param in the original String
	 * @return The converted String
	 */
	protected static String escapeMetaCharacters(String in) {
		if (in == null || in.indexOf('&') < 0) {
			return in;
		}
		int length = in.length();
		StringBuilder out = new StringBuilder(length + 1);
		for (int i = 0; i < length; i++) {
			char c = in.charAt(i);
			if (c == '&') {
				out.append("&&");//$NON-NLS-1$
			} else {
				out.append(c);
			}
		}
		return out.toString();
	}

	/**
	 * Creates the progress monitor's UI parts and layouts them
	 * according to the given layout. If the layout is <code>null</code>
	 * the part's default layout is used.
	 * @param layout The layout for the receiver.
	 * @param progressIndicatorHeight The suggested height of the indicator
	 */
	protected void initialize(Layout layout, int progressIndicatorHeight) {
		if (layout == null) {
			GridLayout l = new GridLayout();
			l.marginWidth = 0;
			l.marginHeight = 0;
			layout = l;
		}
		int numColumns = 1;
		if (fHasStopButton)
			numColumns++;
		setLayout(layout);

		if (layout instanceof GridLayout)
			((GridLayout)layout).numColumns = numColumns;

		fLabel = new Label(this, SWT.LEFT);
		fLabel.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, numColumns, 1));

		if (progressIndicatorHeight == SWT.DEFAULT) {
			GC gc = new GC(fLabel);
			FontMetrics fm = gc.getFontMetrics();
			gc.dispose();
			progressIndicatorHeight = fm.getHeight();
		}

		fProgressIndicator = new ProgressIndicator(this);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = false;
		gd.verticalAlignment = GridData.CENTER;
		gd.heightHint = progressIndicatorHeight;
		fProgressIndicator.setLayoutData(gd);

		if (fHasStopButton) {
			fToolBar = new ToolBar(this, SWT.FLAT);

			gd = new GridData();
			gd.grabExcessHorizontalSpace = false;
			gd.grabExcessVerticalSpace = false;
			gd.verticalAlignment = GridData.CENTER;
			fToolBar.setLayoutData(gd);
			fStopButton = new ToolItem(fToolBar, SWT.PUSH);
			// It would have been nice to use the fCancelListener, but that
			// listener operates on the fCancelComponent which must be a control.
			fStopButton.addSelectionListener(widgetSelectedAdapter(e -> {
				setCanceled(true);
				if (fStopButton != null) {
					fStopButton.setEnabled(false);
				}
			}));
			final Image stopImage = ImageDescriptor.createFromFile(
					ProgressMonitorPart.class, "images/stop.svg").createImage(getDisplay()); //$NON-NLS-1$
			fToolBar.setCursor(this.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
			fStopButton.setImage(stopImage);
			fStopButton.addDisposeListener(e -> {
				stopImage.dispose();
			});
			fStopButton.setEnabled(false);
			fStopButton.setToolTipText(JFaceResources.getString("ProgressMonitorPart.cancelToolTip")); //$NON-NLS-1$
		}

		throttledUpdate = new Throttler(fLabel.getDisplay(), Duration.ofMillis(100), this::updateLabel);
	}

	@Override
	public void internalWorked(double work) {
		if (!fProgressIndicator.isDisposed()) {
			fProgressIndicator.worked(work);
		}
	}

	@Override
	public boolean isCanceled() {
		return fIsCanceled;
	}

	/**
	 * Detach the progress monitor part from the given cancel component.
	 *
	 * @param cancelComponent the control that was previously used as a cancel component. This
	 *            parameter will be ignored and hence can be <code>null</code> if a stop button was
	 *            requested upon construction and instead the stop button will be disabled.
	 * @see #ProgressMonitorPart(Composite, Layout, boolean)
	 */
	public void removeFromCancelComponent(Control cancelComponent) {
		if (fHasStopButton) {
			setCancelEnabled(false);
		} else {
			Assert.isTrue(fCancelComponent == cancelComponent && fCancelComponent != null);
			fCancelComponent.removeListener(SWT.Selection, fCancelListener);
			fCancelComponent = null;
		}
	}

	@Override
	public void setCanceled(boolean b) {
		fIsCanceled = b;
	}

	@Override
	public void setFont(Font font) {
		super.setFont(font);
		fLabel.setFont(font);
		fProgressIndicator.setFont(font);
	}

	@Override
	public void setTaskName(String name) {
		fTaskName = name;
		queueUpdateLabel();
	}

	@Override
	public void subTask(String name) {
		fSubTaskName = name;
		queueUpdateLabel();
	}

	/**
	 * Enqueues a label update for asynchronous execution. The update is performed
	 * throttled to 100ms, i.e. updates within the throttle range are not displayed.
	 *
	 * @since 3.14
	 */
	protected void queueUpdateLabel() {
		throttledUpdate.throttledExec();
	}

	/**
	 * Updates the label with the current task and subtask names.
	 */
	protected void updateLabel() {
		if (fLabel.isDisposed() || fLabel.isAutoDirection()) {
			return;
		}
		if (blockedStatus == null) {
			String text = taskLabel();
			fLabel.setText(text);
		} else {
			fLabel.setText(blockedStatus.getMessage());
		}

		// Force an update as we are in the UI Thread
		fLabel.update();
	}

	/**
	 * Return the label for showing tasks
	 * @return String
	 */
	private String taskLabel() {
		boolean hasTask= fTaskName != null && fTaskName.length() > 0;
		boolean hasSubtask= fSubTaskName != null && fSubTaskName.length() > 0;

		if (hasTask) {
			if (hasSubtask)
				return escapeMetaCharacters(JFaceResources.format("Set_SubTask", fTaskName, fSubTaskName));//$NON-NLS-1$
				return escapeMetaCharacters(fTaskName);

		} else if (hasSubtask) {
			return escapeMetaCharacters(fSubTaskName);

		} else {
			return ""; //$NON-NLS-1$
		}
	}

	@Override
	public void worked(int work) {
		internalWorked(work);
	}

	@Override
	public void clearBlocked() {
		blockedStatus = null;
		queueUpdateLabel();
	}

	@Override
	public void setBlocked(IStatus reason) {
		blockedStatus = reason;
		queueUpdateLabel();
	}

	private void setCancelEnabled(boolean enabled) {
		if (fStopButton != null && !fStopButton.isDisposed()) {
			fStopButton.setEnabled(enabled);
			if (enabled)
				fToolBar.setFocus();
		}
	}
}
