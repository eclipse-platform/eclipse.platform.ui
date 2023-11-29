/*******************************************************************************
 * Copyright (c) 2009, 2018 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.actions.TextActionHandler;
import org.eclipse.ui.part.ViewPart;

/**
 * @since 3.5
 */
public class TextControlView extends ViewPart {
	public static final String ID = "org.eclipse.ui.tests.textHandlerView";
	public Action cutAction;
	public Action copyAction;
	public Action selectAllAction;
	public Action pasteAction;
	public Action cutDummyAction;
	public Action copyDummyAction;
	public Action selectDummyAllAction;
	public Action pasteDummyAction;
	public Text editableText;
	public Text nonEditableText;
	private TextActionHandler delegator;

	public TextControlView() {
		cutDummyAction = new Action("Cut") {
		};
		copyDummyAction = new Action("Copy") {
		};
		selectDummyAllAction = new Action("Select All") {
		};
		pasteDummyAction = new Action("Paste") {
		};
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(3, true));
		editableText = new Text(c, SWT.MULTI);
		editableText.setLayoutData(new GridData());
		nonEditableText = new Text(c, SWT.MULTI | SWT.READ_ONLY);
		nonEditableText.setLayoutData(new GridData());
		delegator = new TextActionHandler(getViewSite().getActionBars());
		delegator.addText(editableText);
		delegator.addText(nonEditableText);
		delegator.setCutAction(cutDummyAction);
		delegator.setCopyAction(copyDummyAction);
		delegator.setSelectAllAction(selectDummyAllAction);
		delegator.setPasteAction(pasteDummyAction);
	}

	@Override
	public void setFocus() {
		editableText.setFocus();
	}

	public Action getPasteAction() throws Exception {
		return getAction("textPasteAction");
	}

	public Action getCopyAction() throws Exception {
		return getAction("textCopyAction");
	}

	public Action getCutAction() throws Exception {
		return getAction("textCutAction");
	}

	public Action getSelectAllAction() throws Exception {
		return getAction("textSelectAllAction");
	}

	public void updateEnabledState() throws Exception {
		Method method = TextActionHandler.class.getDeclaredMethod(
				"updateActionsEnableState");
		method.setAccessible(true);
		method.invoke(delegator);
	}

	private Action getAction(String fieldName) throws Exception {
		Field field = TextActionHandler.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		return (Action) field.get(delegator);
	}
}
