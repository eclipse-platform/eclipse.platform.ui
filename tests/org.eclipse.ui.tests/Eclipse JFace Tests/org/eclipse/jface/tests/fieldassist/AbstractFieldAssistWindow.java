/*******************************************************************************
 * Copyright (c) 2009 Remy Chi Jian Suen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - initial API and implementation
 *     IBM - ongoing development
 ******************************************************************************/
package org.eclipse.jface.tests.fieldassist;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractFieldAssistWindow extends Window {

	private Control fieldAssistControl;

	private IContentProposalProvider proposalProvider;
	private KeyStroke keyStroke = null;
	private char[] autoActivationCharacters = null;
	private int filterStyle = ContentProposalAdapter.FILTER_NONE;
	private boolean propagateKeys = true;
	private int acceptance = ContentProposalAdapter.PROPOSAL_INSERT;
	private int autoActivationDelay = 0;
	private ContentProposalAdapter adapter;

	public AbstractFieldAssistWindow() {
		super((Shell) null);
	}

	public Display getDisplay() {
		return getShell().getDisplay();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite content = (Composite) super.createContents(parent);
		content.setLayout(new FillLayout());

		fieldAssistControl = createFieldAssistControl(parent);
		Assert.isNotNull(fieldAssistControl);

		adapter = createContentProposalAdapter(fieldAssistControl);
		adapter.setAutoActivationDelay(autoActivationDelay);
		adapter.setFilterStyle(filterStyle);
		adapter.setPropagateKeys(propagateKeys);
		adapter.setProposalAcceptanceStyle(acceptance);
		
		createExtraControls(parent);

		return content;
	}
	
	protected void createExtraControls(Composite parent) {
		// default is to do nothing
	}

	@Override
	protected Layout getLayout() {
		return new FillLayout();
	}

	/**
	 * Create and return the content proposal adapter that will be used by this
	 * field assist window.
	 * 
	 * @param control
	 *            the SWT control to provide field assist for
	 */
	protected ContentProposalAdapter createContentProposalAdapter(
			Control control) {
		return new ContentProposalAdapter(control, getControlContentAdapter(),
				getContentProposalProvider(), getKeyStroke(),
				getAutoActivationCharacters());
	}

	protected abstract IControlContentAdapter getControlContentAdapter();

	public Control getFieldAssistControl() {
		return fieldAssistControl;
	}

	protected abstract Control createFieldAssistControl(Composite parent);

	public void setAutoActivationDelay(int autoActivationDelay) {
		this.autoActivationDelay = autoActivationDelay;
	}

	public final int getAutoActivationDelay() {
		return autoActivationDelay;
	}

	protected boolean shouldFilterProposals() {
		return true;
	}

	protected String[] getProposals() {
		return new String[] { "one", "two", "three", "four", "five", "six",
				"seven", "eight", "nine", "ten" };
	}

	protected char[] getAutoActivationCharacters() {
		return autoActivationCharacters;
	}

	public void setAutoActivationCharacters(char[] autoActivationCharacters) {
		this.autoActivationCharacters = autoActivationCharacters;
	}

	protected IContentProposalProvider createContentProposalProvider() {
		SimpleContentProposalProvider proposalProvider = new SimpleContentProposalProvider(
				getProposals());
		proposalProvider.setFiltering(shouldFilterProposals());
		return proposalProvider;
	}

	protected IContentProposalProvider getContentProposalProvider() {
		if (proposalProvider == null) {
			proposalProvider = createContentProposalProvider();
		}
		return proposalProvider;
	}
	
	protected ContentProposalAdapter getContentProposalAdapter() {
		return adapter;
	}

	public void setContentProposalProvider(
			IContentProposalProvider proposalProvider) {
		this.proposalProvider = proposalProvider;
	}

	public void setFilterStyle(int filterStyle) {
		this.filterStyle = filterStyle;
	}

	public void setPropagateKeys(boolean propagateKeys) {
		this.propagateKeys = propagateKeys;
	}

	public void setProposalAcceptanceStyle(int acceptance) {
		this.acceptance = acceptance;
	}

	protected KeyStroke getKeyStroke() {
		return keyStroke;
	}

	public void setKeyStroke(KeyStroke keyStroke) {
		this.keyStroke = keyStroke;
	}
}
