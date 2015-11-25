/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Joseph Carroll <jdsalingerjr@gmail.com> - Bug 385414 Contributing wizards
 *     to toolbar always displays icon and text
 *     Bruce Skingle <Bruce.Skingle@immutify.com> - Bug 443092
 *     Daniel Kruegler <daniel.kruegler@gmail.com> - Bug 473779
 ******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import javax.inject.Inject;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;

public class DirectContributionItem extends AbstractContributionItem {

	private static final String DCI_STATIC_CONTEXT = "DCI-staticContext"; //$NON-NLS-1$

	private static final Object missingExecute = new Object();

	private IEclipseContext infoContext;

	@Inject
	private IContributionFactory contribFactory;

	@Override
	protected void updateMenuItem() {
		MenuItem item = (MenuItem) widget;
		String text = getModel().getLocalizedLabel();
		if (text != null) {
			item.setText(text);
		} else {
			item.setText(""); //$NON-NLS-1$
		}
		final String tooltip = getModel().getLocalizedTooltip();
		item.setToolTipText(tooltip);
		item.setSelection(getModel().isSelected());
		item.setEnabled(getModel().isEnabled());
	}

	@Override
	protected void updateToolItem() {
		ToolItem item = (ToolItem) widget;
		final String text = getModel().getLocalizedLabel();
		Image icon = item.getImage();
		boolean mode = getModel().getTags().contains(FORCE_TEXT);
		if ((icon == null || mode) && text != null) {
			item.setText(text);
		} else {
			item.setText(""); //$NON-NLS-1$
		}
		final String tooltip = getModel().getLocalizedTooltip();
		item.setToolTipText(tooltip);
		item.setSelection(getModel().isSelected());
		item.setEnabled(getModel().isEnabled());
	}

	@Override
	protected void handleWidgetDispose(Event event) {
		if (event.widget == widget) {
			if (infoContext != null) {
				infoContext.dispose();
				infoContext = null;
			}
			ToolItemUpdater updater = getUpdater();
			if (updater != null) {
				updater.removeItem(this);
			}
			widget.removeListener(SWT.Selection, getItemListener());
			widget.removeListener(SWT.Dispose, getItemListener());
			widget.removeListener(SWT.DefaultSelection, getItemListener());
			widget = null;
			Object obj = getModel().getTransientData().get(DISPOSABLE);
			if (obj instanceof Runnable) {
				((Runnable) obj).run();
			}
			getModel().setWidget(null);
			disposeOldImages();
		}
	}

	@Override
	public void dispose() {
		if (widget != null) {
			widget.dispose();
			widget = null;
			getModel().setWidget(null);
		}
	}

	private IEclipseContext getStaticContext(Event event) {
		if (infoContext == null) {
			infoContext = EclipseContextFactory.create(DCI_STATIC_CONTEXT);
			ContributionsAnalyzer.populateModelInterfaces(getModel(), infoContext,
					getModel().getClass().getInterfaces());
		}
		if (event == null) {
			infoContext.remove(Event.class);
		} else {
			infoContext.set(Event.class, event);
		}
		return infoContext;
	}

	@Override
	protected void executeItem(Event trigger) {
		final IEclipseContext lclContext = getContext(getModel());
		if (!checkContribution(lclContext)) {
			return;
		}
		MContribution contrib = (MContribution) getModel();
		IEclipseContext staticContext = getStaticContext(trigger);
		Object result = ContextInjectionFactory.invoke(contrib.getObject(),
 Execute.class,
				getExecutionContext(lclContext), staticContext, missingExecute);
		if (result == missingExecute && logger != null) {
			logger.error("Contribution is missing @Execute: " + contrib.getContributionURI()); //$NON-NLS-1$
		}
	}

	@Override
	protected boolean canExecuteItem(Event trigger) {
		final IEclipseContext lclContext = getContext(getModel());
		if (!checkContribution(lclContext)) {
			return false;
		}
		MContribution contrib = (MContribution) getModel();
		IEclipseContext staticContext = getStaticContext(trigger);
		Boolean result = ((Boolean) ContextInjectionFactory.invoke(
				contrib.getObject(), CanExecute.class,
				getExecutionContext(lclContext), staticContext, Boolean.TRUE));
		return result.booleanValue();
	}

	/**
	 * Return the execution context for the @CanExecute and @Execute methods.
	 * This should be the same as the execution context used by the
	 * EHandlerService.
	 *
	 * @param context
	 *            the context for this item
	 * @return the execution context
	 */
	private IEclipseContext getExecutionContext(IEclipseContext context) {
		if (context == null)
			return null;

		return context.getActiveLeaf();
	}

	private boolean checkContribution(IEclipseContext lclContext) {
		if (!(getModel() instanceof MContribution)) {
			return false;
		}
		MContribution contrib = (MContribution) getModel();
		if (contrib.getObject() == null) {
			contrib.setObject(contribFactory.create(
					contrib.getContributionURI(), lclContext));
		}
		return contrib.getObject() != null;
	}

}
