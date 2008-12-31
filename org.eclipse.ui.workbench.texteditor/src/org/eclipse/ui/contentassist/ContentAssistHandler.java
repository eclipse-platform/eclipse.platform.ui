/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.contentassist;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;

import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.contentassist.AbstractControlContentAssistSubjectAdapter;
import org.eclipse.jface.contentassist.ComboContentAssistSubjectAdapter;
import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.contentassist.TextContentAssistSubjectAdapter;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.texteditor.NLSUtility;
import org.eclipse.ui.keys.IBindingService;

import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;


/**
 * A content assistant handler which handles the key binding and
 * the cue for a {@link org.eclipse.jface.text.contentassist.ContentAssistant}
 * and its subject adapter.
 *
 * @since 3.0
 * @deprecated As of 3.2, replaced by JFace field assist support
 */
public class ContentAssistHandler {
	/**
	 * The target control.
	 */
	private Control fControl;
	/**
	 * The content assist subject adapter.
	 */
	private AbstractControlContentAssistSubjectAdapter fContentAssistSubjectAdapter;
	/**
	 * The content assistant.
	 */
	private SubjectControlContentAssistant fContentAssistant;
	/**
	 * The currently installed FocusListener, or <code>null</code> iff none installed.
	 * This is also used as flag to tell whether content assist is enabled
	 */
	private FocusListener fFocusListener;
	/**
	 * The currently installed IHandlerActivation, or <code>null</code> iff none installed.
	 */
	private IHandlerActivation fHandlerActivation;

	/**
	 * Creates a new {@link ContentAssistHandler} for the given {@link Combo}.
	 * Only a single {@link ContentAssistHandler} may be installed on a {@link Combo} instance.
	 * Content Assist is enabled by default.
	 *
	 * @param combo target combo
	 * @param contentAssistant a configured content assistant
	 * @return a new {@link ContentAssistHandler}
	 */
	public static ContentAssistHandler createHandlerForCombo(Combo combo, SubjectControlContentAssistant contentAssistant) {
		return new ContentAssistHandler(combo, new ComboContentAssistSubjectAdapter(combo), contentAssistant);
	}

	/**
	 * Creates a new {@link ContentAssistHandler} for the given {@link Text}.
	 * Only a single {@link ContentAssistHandler} may be installed on a {@link Text} instance.
	 * Content Assist is enabled by default.
	 *
	 * @param text target text
	 * @param contentAssistant a configured content assistant
	 * @return a new {@link ContentAssistHandler}
	 */
	public static ContentAssistHandler createHandlerForText(Text text, SubjectControlContentAssistant contentAssistant) {
		return new ContentAssistHandler(text, new TextContentAssistSubjectAdapter(text), contentAssistant);
	}

	/**
	 * Internal constructor.
	 *
	 * @param control target control
	 * @param subjectAdapter content assist subject adapter
	 * @param contentAssistant content assistant
	 */
	private ContentAssistHandler(
			Control control,
			AbstractControlContentAssistSubjectAdapter subjectAdapter,
			SubjectControlContentAssistant contentAssistant) {
		fControl= control;
		fContentAssistant= contentAssistant;
		fContentAssistSubjectAdapter= subjectAdapter;
		setEnabled(true);
		fControl.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				setEnabled(false);
			}
		});
	}

	/**
	 * @return <code>true</code> iff content assist is enabled
	 */
	public boolean isEnabled() {
		return fFocusListener != null;
	}

	/**
	 * Controls enablement of content assist.
	 * When enabled, a cue is shown next to the focused field
	 * and the affordance hover shows the shortcut.
	 *
	 * @param enable enable content assist iff true
	 */
	public void setEnabled(boolean enable) {
		if (enable == isEnabled())
			return;

		if (enable)
			enable();
		else
			disable();
	}

	/**
	 * Enable content assist.
	 */
	private void enable() {
		if (! fControl.isDisposed()) {
			fContentAssistant.install(fContentAssistSubjectAdapter);
			installCueLabelProvider();
			installFocusListener();
			if (fControl.isFocusControl())
				activateHandler();
		}
	}

	/**
	 * Disable content assist.
	 */
	private void disable() {
		if (! fControl.isDisposed()) {
			fContentAssistant.uninstall();
			fContentAssistSubjectAdapter.setContentAssistCueProvider(null);
			fControl.removeFocusListener(fFocusListener);
			fFocusListener= null;
			if (fHandlerActivation != null)
				deactivateHandler();
		}
	}

	/**
	 * Create and install the {@link LabelProvider} for fContentAssistSubjectAdapter.
	 */
	private void installCueLabelProvider() {
		ILabelProvider labelProvider= new LabelProvider() {
			/*
			 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				IBindingService bindingService= (IBindingService) PlatformUI.getWorkbench().getAdapter(IBindingService.class);
				TriggerSequence[] activeBindings= bindingService.getActiveBindingsFor(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
				if (activeBindings.length == 0)
					return ContentAssistMessages.ContentAssistHandler_contentAssistAvailable;
				return NLSUtility.format(ContentAssistMessages.ContentAssistHandler_contentAssistAvailableWithKeyBinding, activeBindings[0].format());
			}
		};
		fContentAssistSubjectAdapter.setContentAssistCueProvider(labelProvider);
	}

	/**
	 * Create fFocusListener and install it on fControl.
	 */
	private void installFocusListener() {
		fFocusListener= new FocusListener() {
			public void focusGained(final FocusEvent e) {
				if (fHandlerActivation == null)
					activateHandler();
			}
			public void focusLost(FocusEvent e) {
				if (fHandlerActivation != null)
					deactivateHandler();
			}
		};
		fControl.addFocusListener(fFocusListener);
	}

	/**
	 * Create and register fHandlerSubmission.
	 */
	private void activateHandler() {
		IHandlerService handlerService= (IHandlerService)PlatformUI.getWorkbench().getAdapter(IHandlerService.class);
		if (handlerService == null)
			return;

		IHandler handler= new AbstractHandler() {
			public Object execute(ExecutionEvent event) throws ExecutionException {
				if (ContentAssistHandler.this.isEnabled()) // don't call AbstractHandler#isEnabled()!
					fContentAssistant.showPossibleCompletions();
				return null;
			}
		};
		fHandlerActivation= handlerService.activateHandler(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, handler);
	}

	/**
	 * Unregister the {@link IHandlerActivation} from the shell.
	 */
	private void deactivateHandler() {
		IHandlerService handlerService= (IHandlerService)PlatformUI.getWorkbench().getAdapter(IHandlerService.class);
		if (handlerService != null)
			handlerService.deactivateHandler(fHandlerActivation);
		fHandlerActivation= null;
	}
}
