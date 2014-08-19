/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 426460, 441150
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * Default SWT renderer responsible for an MPart. See
 * {@link WorkbenchRendererFactory}
 */
public class ContributedPartRenderer extends SWTPartRenderer {

	@Inject
	private IPresentationEngine engine;

	@Inject
	@Optional
	private Logger logger;

	private MPart partToActivate;

	private Listener activationListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			// we only want to activate the part if the activated widget is
			// actually bound to a model element
			MPart part = (MPart) event.widget.getData(OWNING_ME);
			if (part != null) {
				try {
					partToActivate = part;
					activate(partToActivate);
				} finally {
					partToActivate = null;
				}
			}
		}
	};

	@Override
	public Object createWidget(final MUIElement element, Object parent) {
		if (!(element instanceof MPart) || !(parent instanceof Composite))
			return null;

		Widget parentWidget = (Widget) parent;
		Widget newWidget = null;
		final MPart part = (MPart) element;

		final Composite newComposite = new Composite((Composite) parentWidget,
				SWT.NONE) {

			/**
			 * Field to determine whether we are currently in the midst of
			 * granting focus to the part.
			 */
			private boolean beingFocused = false;

			@Override
			public boolean setFocus() {
				if (!beingFocused) {
					try {
						// we are currently asking the part to take focus
						beingFocused = true;

						// delegate an attempt to set the focus here to the
						// part's implementation (if there is one)
						Object object = part.getObject();
						if (object != null && isEnabled()) {
							IPresentationEngine pe = part.getContext().get(
									IPresentationEngine.class);
							pe.focusGui(part);
							return true;
						}
						return super.setFocus();
					} finally {
						// we are done, unset our flag
						beingFocused = false;
					}
				}

				// already being focused, likely some strange recursive call,
				// just return
				return true;
			}
		};

		newComposite.setLayout(new FillLayout(SWT.VERTICAL));

		newWidget = newComposite;
		bindWidget(element, newWidget);

		// Create a context for this part
		IEclipseContext localContext = part.getContext();
		localContext.set(Composite.class, newComposite);

		IContributionFactory contributionFactory = localContext
				.get(IContributionFactory.class);
		Object newPart = contributionFactory.create(part.getContributionURI(),
				localContext);
		part.setObject(newPart);

		return newWidget;
	}

	public static void setDescription(MPart part, String description) {
		if (!(part.getWidget() instanceof Composite))
			return;

		Composite c = (Composite) part.getWidget();

		// Do we already have a label?
		if (c.getChildren().length == 3) {
			Label label = (Label) c.getChildren()[0];
			if (description == null)
				description = ""; //$NON-NLS-1$
			// hide the label if there is no text to show
			boolean hasText = !description.equals(""); //$NON-NLS-1$
			label.setVisible(hasText);
			label.setText(description);
			label.setToolTipText(description);

			// also hide the separator
			c.getChildren()[1].setVisible(hasText);
			c.layout();
		} else if (c.getChildren().length == 1) {
			c.setLayout(new Layout() {

				@Override
				protected Point computeSize(Composite composite, int wHint,
						int hHint, boolean flushCache) {
					return new Point(0, 0);
				}

				@Override
				protected void layout(Composite composite, boolean flushCache) {
					Rectangle bounds = composite.getBounds();
					if (composite.getChildren().length == 1) {
						composite.getChildren()[0].setBounds(composite
								.getBounds());
					} else if (composite.getChildren().length == 3) {
						Label label = (Label) composite.getChildren()[0];
						Label separator = (Label) composite.getChildren()[1];
						Control partCtrl = composite.getChildren()[2];

						// if the label is not visible, give it a zero size
						int labelHeight = label.isVisible() ? label
								.computeSize(bounds.width, SWT.DEFAULT).y : 0;
						label.setBounds(0, 0, bounds.width, labelHeight);

						int separatorHeight = separator.isVisible() ? separator
								.computeSize(bounds.width, SWT.DEFAULT).y : 0;
						separator.setBounds(0, labelHeight, bounds.width,
								separatorHeight);

						partCtrl.setBounds(0, labelHeight + separatorHeight,
								bounds.width, bounds.height - labelHeight
										- separatorHeight);
					}
				}
			});

			Label separator = new Label(c, SWT.SEPARATOR | SWT.HORIZONTAL);
			separator.moveAbove(null);
			Label label = new Label(c, SWT.NONE);
			label.setText(description);
			label.setToolTipText(description);
			label.moveAbove(null);
			c.layout();
		}
	}

	@Override
	protected boolean requiresFocus(MPart element) {
		if (element == partToActivate) {
			return true;
		}
		return super.requiresFocus(element);
	}

	@Override
	public void hookControllerLogic(final MUIElement me) {
		super.hookControllerLogic(me);
		if (!(me instanceof MPart)) {
			return;
		}
		Widget widget = (Widget) me.getWidget();
		if (widget instanceof Composite) {
			widget.addListener(SWT.Activate, activationListener);
		}

	}

	@Override
	public Object getUIContainer(MUIElement element) {
		if (element instanceof MToolBar) {
			MUIElement container = (MUIElement) ((EObject) element)
					.eContainer();
			MUIElement parent = container.getParent();
			if (parent == null) {
				MPlaceholder placeholder = container.getCurSharedRef();
				if (placeholder != null) {
					return placeholder.getParent().getWidget();
				}
			} else {
				return parent.getWidget();
			}
		}
		return super.getUIContainer(element);
	}

	@Override
	public void disposeWidget(MUIElement element) {
		if (element instanceof MPart) {
			MPart part = (MPart) element;
			MToolBar toolBar = part.getToolbar();
			if (toolBar != null) {
				Widget widget = (Widget) toolBar.getWidget();
				if (widget != null) {
					unbindWidget(toolBar);
					widget.dispose();
				}
			}

			for (MMenu menu : part.getMenus()) {
				engine.removeGui(menu);
			}
		}

		Composite parent = null;
		if (element.getWidget() instanceof Composite) {
			parent = ((Composite) element.getWidget()).getParent();
		}

		if (parent != null) {
			try {
				parent.setRedraw(false);
				super.disposeWidget(element);
			} finally {
				parent.setRedraw(true);
			}
		} else {
			super.disposeWidget(element);
		}
	}
}
