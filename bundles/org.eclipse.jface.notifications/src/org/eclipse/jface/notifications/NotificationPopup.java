/*******************************************************************************
 * Copyright (c) 2021 SAP SE and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial api
 *******************************************************************************/
package org.eclipse.jface.notifications;

import java.util.function.Function;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * NotificationPopup is a default implementation of
 * {@link AbstractNotificationPopup}. Instances can be created with a fluent
 * {@link Builder} api.
 * <p>
 * It covers use cases such as setting a title and/or a text as content. Also
 * functions can be passed to create the title and/or content. More complex
 * cases with e.g. several SWT controls should still be created by extending
 * {@link AbstractNotificationPopup}.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * NotificationPopup.forDisplay(Display.getDefault()).text("A problem occurred.").title("Warning", true).open();
 * </pre>
 *
 * @since 0.4
 */
public class NotificationPopup extends AbstractNotificationPopup {

	/**
	 * The Builder to create NotificationPopup instances. It has a fluent API (every
	 * method returns the same builder instance).
	 *
	 * @since 0.4
	 *
	 */
	public static class Builder {

		private Display display;
		private Function<Composite, Control> contentCreator;
		private Function<Composite, Control> titleCreator;
		private Long delay;
		private Boolean fadeIn;
		private boolean hasCloseButton;
		private Image titleImage;

		private Builder(Display display) {
			this.display = display;
		}

		/**
		 * Sets the function to create the main content of the notification popup in the
		 * given composite. Is called when the content is created.
		 *
		 * @param contentCreator the content creation function
		 * @return this
		 */
		public Builder content(Function<Composite, Control> contentCreator) {
			this.contentCreator = contentCreator;
			return this;
		}

		/**
		 * Sets the content text. A label is created to show the given text.
		 *
		 * @param text the content text
		 * @return this
		 */
		public Builder text(String text) {
			return content(WidgetFactory.label(SWT.NONE).text(text)::create);
		}

		/**
		 * Sets the function to create the title area's content of the notification
		 * popup in the given composite. Is called when the title is created.
		 *
		 * @param titleCreator   the title creation function
		 * @param hasCloseButton whether to show or not to show a close image
		 * @return this
		 */
		public Builder title(Function<Composite, Control> titleCreator, boolean hasCloseButton) {
			this.titleCreator = titleCreator;
			this.hasCloseButton = hasCloseButton;
			return this;
		}

		/**
		 * Sets the title text. A label is created to show the given title.
		 *
		 * @param title          the title text
		 * @param hasCloseButton whether to show or not to show a close image
		 * @return this
		 */
		public Builder title(String title, boolean hasCloseButton) {
			return title(WidgetFactory.label(SWT.NONE).text(title)::create, hasCloseButton);
		}

		/**
		 * Sets the title image.
		 *
		 * @param image the title image
		 * @return this
		 */
		public Builder titleImage(Image image) {
			this.titleImage = image;
			return this;
		}

		/**
		 * Sets the visible time of the popup before it disappears.
		 *
		 * @param delay the time in milliseconds
		 * @return this
		 */
		public Builder delay(long delay) {
			this.delay = delay;
			return this;
		}

		/**
		 * Sets whether the popup should fade in or just appear.
		 *
		 * @param fadeIn whether or not to fade in
		 * @return this
		 */
		public Builder fadeIn(boolean fadeIn) {
			this.fadeIn = fadeIn;
			return this;
		}

		/**
		 * Creates and opens the popup. This is a shorthand for:
		 *
		 * <pre>
		 * NotificationPopup popup = builder.build();
		 * int open = popup.open();
		 * </pre>
		 *
		 * @return the return code of open()
		 */
		public int open() {
			return build().open();
		}

		/**
		 * Creates the notification popup with all parameters set in the builder.
		 *
		 * @return the notification popup
		 */
		public NotificationPopup build() {
			return new NotificationPopup(this);
		}
	}

	/**
	 * Creates a new builder instance.
	 *
	 * @param display the display to use
	 * @return the builder instance
	 */
	public static Builder forDisplay(Display display) {
		return new Builder(display);
	}

	private Function<Composite, ? extends Control> contentCreator;
	private Function<Composite, Control> titleCreator;
	private boolean hasCloseButton;
	private Image titleImage;

	private NotificationPopup(Builder builder) {
		super(builder.display);
		this.contentCreator = builder.contentCreator;
		this.titleCreator = builder.titleCreator;
		this.hasCloseButton = builder.hasCloseButton;
		this.titleImage = builder.titleImage;

		if (builder.delay != null) {
			setDelayClose(builder.delay);
		}
		if (builder.fadeIn != null) {
			setFadingEnabled(builder.fadeIn);
		}
	}

	@Override
	protected void createTitleArea(Composite parent) {
		if (this.titleCreator == null) {
			super.createTitleArea(parent);
			return;
		}
		((GridData) parent.getLayoutData()).heightHint = TITLE_HEIGHT;

		int numColums = 1;
		if (hasCloseButton)
			numColums++;
		if (titleImage != null)
			numColums++;
		GridLayoutFactory.fillDefaults().numColumns(numColums).applyTo(parent);

		if (titleImage != null) {
			WidgetFactory.label(SWT.NONE).image(titleImage);
		}

		Control control = this.titleCreator.apply(parent);
		if (control.getLayoutData() == null) {
			GridDataFactory.fillDefaults().grab(true, false).applyTo(control);
		}

		control.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		control.setForeground(getTitleForeground());
		control.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

		if (hasCloseButton) {
			super.createCloseButton(parent);
		}
	}

	@Override
	protected void createContentArea(Composite parent) {
		if (this.contentCreator == null) {
			super.createContentArea(parent);
			return;
		}
		GridLayoutFactory.fillDefaults().applyTo(parent);
		Control control = this.contentCreator.apply(parent);
		if (control.getLayoutData() == null) {
			GridDataFactory.fillDefaults().grab(true, false).applyTo(control);
		}
	}
}