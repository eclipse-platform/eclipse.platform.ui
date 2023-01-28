/*******************************************************************************
 * Copyright (c) 2021, 2022 SAP SE and others.
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

package org.eclipse.jface.tests.notifications;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.notifications.NotificationPopup;
import org.eclipse.jface.notifications.NotificationPopup.Builder;
import org.eclipse.jface.notifications.internal.CommonImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class NotificationPopupTest {

	private Display display;
	private Builder builder;

	@Before
	public void setUp() {
		this.display = Display.getDefault();
		this.builder = NotificationPopup.forDisplay(this.display);
	}

	@After
	public void tearDown() {
		if (!Platform.isRunning()) {
			if (this.display != null) {
				this.display.syncExec(() -> this.display.dispose());
			}
		}
	}

	@Test
	public void createsWithTextAndTitle() {
		NotificationPopup notication = this.builder.text("This is a test").title("Hello World", false).delay(1).build();
		notication.open();
		List<Control> controls = getNotificationPopupControls();

		assertThat(controls, hasItem(aLabelWith("Hello World")));
		assertThat(controls, hasItem(aLabelWith("This is a test")));
		notication.close();
	}

	@Test
	public void createsWithCloseButton() {
		NotificationPopup notication = this.builder.text("This is a test").title("Hello World", true).delay(1).build();
		notication.open();
		List<Control> controls = getNotificationPopupControls();

		assertThat(controls, hasItem(aLabelWith(CommonImages.getImage(CommonImages.NOTIFICATION_CLOSE))));
		notication.close();
	}

	@Test
	public void createsWithTextContent() {
		Text[] text = new Text[1];
		NotificationPopup notication =
		this.builder.title("Hello World", false).content(parent -> {
			text[0] = new Text(parent, SWT.NONE);
			text[0].setText("My custom Text");
			return text[0];
		}).delay(1).build();

		notication.open();
		List<Control> controls = getNotificationPopupControls();

		assertThat(controls, hasItem(is(text[0])));
		notication.close();
	}

	@Test
	public void createsWithTitleContent() {
		Text[] text = new Text[1];
		NotificationPopup notication = this.builder.title(parent -> {
			text[0] = new Text(parent, SWT.NONE);
			text[0].setText("My custom Title");
			return text[0];
		}, false).delay(1).build();

		notication.open();
		List<Control> controls = getNotificationPopupControls();
		notication.close();
		assertThat(controls, hasItem(is(text[0])));
	}

	private List<Control> getNotificationPopupControls() {
		Shell[] shells = this.display.getShells();
		Shell shell = shells[shells.length - 1];
		return getChildrenStream(shell).toList();
	}

	private Stream<Control> getChildrenStream(Control c) {
		if (c.getClass() == Composite.class || c.getClass() == Shell.class) {
			Composite composite = (Composite) c;
			return Arrays.stream(composite.getChildren()).flatMap(this::getChildrenStream);
		} else {
			return Stream.of(c);
		}
	}

	private Matcher<? extends Control> aLabelWith(String expectedText) {
		return allOf(isA(Label.class), new LabelMatcher(expectedText));
	}

	private Matcher<? extends Control> aLabelWith(Image expectedImage) {
		return allOf(isA(Label.class), new LabelMatcher(expectedImage));
	}

	private class LabelMatcher extends BaseMatcher<Label> {

		String expectedText;
		private Image expectedImage;

		public LabelMatcher(String expectedText) {
			this.expectedText = expectedText;
		}

		public LabelMatcher(Image expectedImage) {
			this.expectedImage = expectedImage;
		}

		@Override
		public boolean matches(Object item) {
			if (this.expectedImage != null)
				return ((Label) item).getImage() == this.expectedImage;
			else
				return ((Label) item).getText().equals(this.expectedText);
		}

		@Override
		public void describeTo(Description description) {
			if (this.expectedImage != null)
				description.appendText("a Label with image ").appendText(this.expectedText);
			else
				description.appendText("a Label with text ").appendText(this.expectedText);
		}
	}
}