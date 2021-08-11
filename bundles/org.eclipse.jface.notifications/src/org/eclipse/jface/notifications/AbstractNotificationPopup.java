/*******************************************************************************
 * Copyright (c) 2004, 2020 Tasktop Technologies and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Benjamin Pasero - initial API and implementation
 *     Tasktop Technologies - initial API and implementation
 *     SAP SE - port to platform.ui
 *******************************************************************************/
package org.eclipse.jface.notifications;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.notifications.internal.AnimationUtil;
import org.eclipse.jface.notifications.internal.AnimationUtil.FadeJob;
import org.eclipse.jface.notifications.internal.CommonImages;
import org.eclipse.jface.notifications.internal.Messages;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 0.2
 */
public abstract class AbstractNotificationPopup extends Window {

	static final int TITLE_HEIGHT = 24;

	private static final String LABEL_NOTIFICATION = Messages.AbstractNotificationPopup_Label;

	private static final String LABEL_JOB_CLOSE = Messages.AbstractNotificationPopup_CloseJobTitle;

	private static final int MAX_WIDTH = 400;

	private static final int MIN_HEIGHT = 100;

	private static final long DEFAULT_DELAY_CLOSE = 8 * 1000;

	private static final int PADDING_EDGE = 5;

	private long delayClose = DEFAULT_DELAY_CLOSE;

	protected LocalResourceManager resources;

	private final Display display;

	private Shell shell;

	private final Job closeJob = new Job(LABEL_JOB_CLOSE) {

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (!AbstractNotificationPopup.this.display.isDisposed()) {
				AbstractNotificationPopup.this.display.asyncExec(() -> {
					Shell shell = AbstractNotificationPopup.this.getShell();
					if (shell == null || shell.isDisposed()) {
						return;
					}

					if (isMouseOver(shell)) {
						scheduleAutoClose();
						return;
					}

					AbstractNotificationPopup.this.closeFade();
				});
			}
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			return Status.OK_STATUS;
		}
	};

	private FadeJob fadeJob;

	private boolean fadingEnabled;

	public AbstractNotificationPopup(Display display) {
		this(display, SWT.NO_TRIM | SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL);
	}

	public AbstractNotificationPopup(Display display, int style) {
		super((Shell) null);
		setShellStyle(style);

		this.display = display;
		this.resources = new LocalResourceManager(JFaceResources.getResources());

		this.closeJob.setSystem(true);
	}

	public boolean isFadingEnabled() {
		return this.fadingEnabled;
	}

	public void setFadingEnabled(boolean fadingEnabled) {
		this.fadingEnabled = fadingEnabled;
	}

	@Override
	public void create() {
		super.create();
	}

	@Override
	public int open() {
		if (this.shell == null || this.shell.isDisposed()) {
			this.shell = null;
			create();
		}

		constrainShellSize();

		if (isFadingEnabled()) {
			this.shell.setAlpha(0);
		}
		this.shell.setVisible(true);
		this.fadeJob = AnimationUtil.fadeIn(this.shell, (shell, alpha) -> {
			if (shell.isDisposed()) {
				return;
			}

			if (alpha == 255) {
				scheduleAutoClose();
			}
		});

		return Window.OK;
	}

	@Override
	public boolean close() {
		this.resources.dispose();
		return super.close();
	}

	public long getDelayClose() {
		return this.delayClose;
	}

	public void setDelayClose(long delayClose) {
		this.delayClose = delayClose;
	}

	public void closeFade() {
		if (this.fadeJob != null) {
			this.fadeJob.cancelAndWait(false);
		}
		this.fadeJob = AnimationUtil.fadeOut(getShell(), (shell, alpha) -> {
			if (!shell.isDisposed()) {
				if (alpha == 0) {
					shell.close();
				} else if (isMouseOver(shell)) {
					if (AbstractNotificationPopup.this.fadeJob != null) {
						AbstractNotificationPopup.this.fadeJob.cancelAndWait(false);
					}
					AbstractNotificationPopup.this.fadeJob = AnimationUtil.fastFadeIn(shell, (shell1, alpha1) -> {
						if (shell1.isDisposed()) {
							return;
						}

						if (alpha1 == 255) {
							scheduleAutoClose();
						}
					});
				}
			}
		});
	}

	/**
	 * Override to return a customized name. Default is to return the name of the product, specified by the -name (e.g.
	 * "Eclipse SDK") command line parameter that's associated with the product ID (e.g. "org.eclipse.sdk.ide"). Strips
	 * the trailing "SDK" for any name, since this part of the label is considered visual noise.
	 *
	 * @return the name to be used in the title of the popup.
	 */
	protected String getPopupShellTitle() {
		String productName = getProductName();
		if (productName != null) {
			return productName + " " + LABEL_NOTIFICATION; //$NON-NLS-1$
		} else {
			return LABEL_NOTIFICATION;
		}
	}

	protected Image getPopupShellImage(int maximumHeight) {
		return null;
	}

	/**
	 * Override to populate with notifications.
	 *
	 * @param parent Parent for this component.
	 */
	protected void createContentArea(Composite parent) {
		// empty by default
	}

	/**
	 * Override to customize the title bar
	 */
	protected void createTitleArea(Composite parent) {
		((GridData) parent.getLayoutData()).heightHint = TITLE_HEIGHT;

		Label titleImageLabel = new Label(parent, SWT.NONE);
		titleImageLabel.setImage(getPopupShellImage(TITLE_HEIGHT));

		Label titleTextLabel = new Label(parent, SWT.NONE);
		titleTextLabel.setText(getPopupShellTitle());
		titleTextLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		titleTextLabel.setForeground(getTitleForeground());
		titleTextLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		titleTextLabel.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

		createCloseButton(parent);
	}

	void createCloseButton(Composite parent) {
		final Label button = new Label(parent, SWT.NONE);
		button.setImage(CommonImages.getImage(CommonImages.NOTIFICATION_CLOSE));
		button.addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseEnter(MouseEvent e) {
				button.setImage(CommonImages.getImage(CommonImages.NOTIFICATION_CLOSE_HOVER));
			}

			@Override
			public void mouseExit(MouseEvent e) {
				button.setImage(CommonImages.getImage(CommonImages.NOTIFICATION_CLOSE));
			}
		});
		button.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {
				close();
				setReturnCode(CANCEL);
			}

		});
	}

	protected Color getTitleForeground() {
		return display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		this.shell = newShell;
	}

	protected void scheduleAutoClose() {
		if (this.delayClose > 0) {
			this.closeJob.schedule(this.delayClose);
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		((GridLayout) parent.getLayout()).marginWidth = 1;
		((GridLayout) parent.getLayout()).marginHeight = 1;

		/* Outer Composite holding the controls */
		final Composite outerCircle = new Composite(parent, SWT.NO_FOCUS);
		outerCircle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;

		outerCircle.setLayout(layout);

		/* Title area containing label and close button */
		final Composite titleCircle = new Composite(outerCircle, SWT.NO_FOCUS);
		titleCircle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		layout = new GridLayout(4, false);
		layout.marginWidth = 3;
		layout.marginHeight = 0;
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 3;

		titleCircle.setLayout(layout);

		/* Create Title Area */
		createTitleArea(titleCircle);

		/* Outer composite to hold content controlls */
		Composite outerContentCircle = new Composite(outerCircle, SWT.NONE);

		layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;

		outerContentCircle.setLayout(layout);
		outerContentCircle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		/* Middle composite to show a 1px black line around the content controls */
		Composite middleContentCircle = new Composite(outerContentCircle, SWT.NO_FOCUS);

		layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.marginTop = 1;

		middleContentCircle.setLayout(layout);
		middleContentCircle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		/* Inner composite containing the content controls */
		Composite innerContent = new Composite(middleContentCircle, SWT.NO_FOCUS);
		innerContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		innerContent.setLayout(layout);

		innerContent.setBackground(this.shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		/* Content Area */
		createContentArea(innerContent);

		return outerCircle;
	}

	@Override
	protected void initializeBounds() {
		Rectangle clArea = getPrimaryClientArea();
		Point initialSize = this.shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		int height = Math.max(initialSize.y, MIN_HEIGHT);
		int width = Math.min(initialSize.x, MAX_WIDTH);

		Point size = new Point(width, height);
		this.shell.setLocation(clArea.width + clArea.x - size.x - PADDING_EDGE, clArea.height + clArea.y - size.y
				- PADDING_EDGE);
		this.shell.setSize(size);
	}

	private static String getProductName() {
		IProduct product = Platform.getProduct();
		if (product != null) {
			String productName = product.getName();
			if (productName != null) {
				String LABEL_SDK = "SDK"; //$NON-NLS-1$
				if (productName.endsWith(LABEL_SDK)) {
					productName = productName.substring(0, productName.length() - LABEL_SDK.length()).trim();
				}
				return productName;
			}
		}
		return null;
	}

	private boolean isMouseOver(Shell shell) {
		if (this.display.isDisposed()) {
			return false;
		}
		return shell.getBounds().contains(this.display.getCursorLocation());
	}

	private Rectangle getPrimaryClientArea() {
		Shell parentShell = getParentShell();
		if (parentShell != null) {
			// calculate client area in display-relative coordinates
			// (i.e. without window border / decorations)
			Rectangle bounds = parentShell.getBounds();
			Rectangle trim = parentShell.computeTrim(0, 0, 0, 0);
			return new Rectangle(bounds.x - trim.x, bounds.y - trim.y, bounds.width - trim.width,
					bounds.height - trim.height);
		}
		// else display on primary monitor
		Monitor primaryMonitor = this.shell.getDisplay().getPrimaryMonitor();
		return (primaryMonitor != null) ? primaryMonitor.getClientArea() : this.shell.getDisplay().getClientArea();
	}

}
