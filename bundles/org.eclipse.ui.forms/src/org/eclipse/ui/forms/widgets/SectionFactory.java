/*******************************************************************************
 * Copyright (c) 2020 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marcus Hoepfner (SAP SE) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.widgets;

import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.jface.widgets.AbstractCompositeFactory;
import org.eclipse.jface.widgets.AbstractWidgetFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;

/**
 * This class provides a convenient shorthand for creating and initializing
 * {@link Section}. This offers several benefits over creating Section normal
 * way:
 *
 * <ul>
 * <li>The same factory can be used many times to create several Section
 * instances</li>
 * <li>The setters on SectionFactory all return "this", allowing them to be
 * chained</li>
 * <li>SectionFactory accepts a Lambda for {@link ExpansionEvent} (see
 * {@link #onExpanded(Consumer)}) and {@link #onExpanding(Consumer)}</li>
 * </ul>
 *
 * Example usage:
 *
 * <pre>
 * Section section = SectionFactory.newSection(Section.TWISTIE | Section.DESCRIPTION) //
 * 		.title("My Section") //
 * 		.description("My section created with a factory") //
 * 		.onExpand(event -&gt; sectionExpanded(event)) //
 * 		.create(parent);
 * </pre>
 * <p>
 * The above example creates a section with a title, a description, registers an
 * IExpansionListener and finally creates the section in "parent".
 * </p>
 *
 * <pre>
 * SectionFactory sectionFactory = SectionFactory.newSection(Section.TWISTIE);
 * sectionFactory.title("Section 1").create(parent);
 * sectionFactory.title("Section 2").create(parent);
 * sectionFactory.title("Section 3").create(parent);
 * </pre>
 * <p>
 * The above example creates three section using the same instance of
 * SectionFactory.
 * </p>
 *
 * @since 3.10
 *
 */
public final class SectionFactory extends AbstractCompositeFactory<SectionFactory, Section> {

	private SectionFactory(int style) {
		super(SectionFactory.class, (Composite parent) -> new Section(parent, style));
	}

	/**
	 * Creates a new SectionFactory with the given style. Refer to
	 * {@link Section#Section(Composite, int)} for possible styles.
	 *
	 * @param style the style to use
	 * @return a new SectionFactory instance
	 */
	public static SectionFactory newSection(int style) {
		return new SectionFactory(style);
	}

	/**
	 * Sets the title of the section. The title will act as a hyperlink and
	 * activating it will toggle the client between expanded and collapsed state.
	 *
	 * @param title the new title string
	 *
	 * @see Section#setText(String)
	 */
	public SectionFactory title(String title) {
		addProperty(section -> section.setText(title));
		return this;
	}

	/**
	 * Sets the description text. Has no effect if DESCRIPTION style was not used in
	 * {@link AbstractWidgetFactory#create}
	 *
	 * @param description new description text; not <code>null</code>
	 *
	 * @see Section#setDescription(String)
	 */
	public SectionFactory description(String description) {
		addProperty(section -> section.setDescription(description));
		return this;
	}

	/**
	 * Sets a function which must provide a description control for the given
	 * Section. The control must not be <samp>null</samp> and must be a direct child
	 * of the section.
	 *
	 * <p>
	 * This method and <code>DESCRIPTION</code> style are mutually exclusive. Use
	 * the method only if you want to create the description control yourself.
	 * </p>
	 *
	 * @param controlFunction the function to create the description control
	 *
	 * @see Section#setDescriptionControl(Control)
	 */
	public SectionFactory description(Function<Section, Control> controlFunction) {
		addProperty(section -> section.setDescriptionControl(controlFunction.apply(section)));
		return this;
	}

	/**
	 * Creates an {@link IExpansionListener} and registers it for the
	 * expansionStateChanged event. If the section <i>was</i> expanded by the user
	 * the given consumer is invoked. The {@link ExpansionEvent} is passed to the
	 * consumer.
	 *
	 * @param consumer the consumer whose accept method is called
	 * @return this
	 *
	 * @see Section#addExpansionListener(IExpansionListener)
	 * @see IExpansionListener#expansionStateChanged(ExpansionEvent)
	 */
	public SectionFactory onExpanded(Consumer<ExpansionEvent> consumer) {
		addProperty(
				section -> section.addExpansionListener(IExpansionListener.expansionStateChangingAdapter(consumer)));
		return this;
	}

	/**
	 * Creates an {@link IExpansionListener} and registers it for the
	 * expansionStateChanging event. If the section <i>is</i> expanded by the user
	 * the given consumer is invoked. The {@link ExpansionEvent} is passed to the
	 * consumer.
	 *
	 * @param consumer the consumer whose accept method is called
	 * @return this
	 *
	 * @see Section#addExpansionListener(IExpansionListener)
	 * @see IExpansionListener#expansionStateChanging(ExpansionEvent)
	 */
	public SectionFactory onExpanding(Consumer<ExpansionEvent> consumer) {
		addProperty(
				section -> section.addExpansionListener(IExpansionListener.expansionStateChangingAdapter(consumer)));
		return this;
	}
}