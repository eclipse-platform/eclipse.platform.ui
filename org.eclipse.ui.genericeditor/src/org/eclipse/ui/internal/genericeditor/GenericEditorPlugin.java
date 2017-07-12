/*******************************************************************************
 * Copyright (c) 2016, 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Sopot Cela, Mickael Istria (Red Hat Inc.) - initial implementation
 *   Lucas Bullen (Red Hat Inc.) - Bug 508829 custom reconciler support
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Generic editor plugin activator and singletons.
 * 
 * @since 1.0
 */
public class GenericEditorPlugin extends AbstractUIPlugin {

	public static final String BUNDLE_ID = "org.eclipse.ui.genericeditor"; //$NON-NLS-1$

	private static GenericEditorPlugin INSTANCE;

	private TextHoverRegistry textHoversRegistry;
	private ContentAssistProcessorRegistry contentAssistProcessorsRegistry;
	private ReconcilerRegistry reconcilierRegistry;
	private PresentationReconcilerRegistry presentationReconcilierRegistry;
	private AutoEditStrategyRegistry autoEditStrategyRegistry;

	@Override
	public void start(BundleContext context) throws Exception{
		INSTANCE = this;
		super.start(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		INSTANCE = null;
	}

	public static GenericEditorPlugin getDefault() {
		return INSTANCE;
	}

	/**
	 * @return the registry allowing to access contributed {@link ITextHover}s.
	 * @since 1.0
	 */
	public synchronized TextHoverRegistry getHoverRegistry() {
		if (this.textHoversRegistry == null) {
			this.textHoversRegistry = new TextHoverRegistry(getPreferenceStore());
		}
		return this.textHoversRegistry;
	}

	/**
	 * @return the registry allowing to access contributed {@link IContentAssistProcessor}s.
	 * @since 1.0
	 */
	public synchronized ContentAssistProcessorRegistry getContentAssistProcessorRegistry() {
		if (this.contentAssistProcessorsRegistry == null) {
			this.contentAssistProcessorsRegistry = new ContentAssistProcessorRegistry();
		}
		return this.contentAssistProcessorsRegistry;
	}
	
	/**
	 * @return the registry allowing to access contributed {@link IReconciler}s.
	 * @since 1.1
	 */
	public synchronized ReconcilerRegistry getReconcilerRegistry() {
		if (this.reconcilierRegistry == null) {
			this.reconcilierRegistry = new ReconcilerRegistry();
		}
		return this.reconcilierRegistry;
	}

	/**
	 * @return the registry allowing to access contributed {@link IPresentationReconciler}s.
	 * @since 1.0
	 */
	public synchronized PresentationReconcilerRegistry getPresentationReconcilerRegistry() {
		if (this.presentationReconcilierRegistry == null) {
			this.presentationReconcilierRegistry = new PresentationReconcilerRegistry();
		}
		return this.presentationReconcilierRegistry;
	}

	/**
	 * @return the registry allowing to access contributed {@link IAutoEditStrategy}s.
	 * @since 1.1
	 */
	public synchronized AutoEditStrategyRegistry getAutoEditStrategyRegistry() {
		if (this.autoEditStrategyRegistry == null) {
			this.autoEditStrategyRegistry = new AutoEditStrategyRegistry();
		}
		return this.autoEditStrategyRegistry;
	}
}
