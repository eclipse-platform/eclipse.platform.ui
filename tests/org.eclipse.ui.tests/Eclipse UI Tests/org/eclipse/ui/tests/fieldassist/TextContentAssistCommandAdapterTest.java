/*******************************************************************************
 * Copyright (c) 2009, 2010 Remy Chi Jian Suen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - initial API and implementation
 *     IBM - ongoing development
 *     David Green <dgreen99@gmail.com> - test for bug 301196
 ******************************************************************************/
package org.eclipse.ui.tests.fieldassist;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.tests.fieldassist.AbstractFieldAssistWindow;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;

public class TextContentAssistCommandAdapterTest extends
		AbstractContentAssistCommandAdapterTest {

	@Override
	protected AbstractFieldAssistWindow createFieldAssistWindow() {
		return new TextCommandFieldAssistWindow();
	}

	/**
	 * bug 301196: [FieldAssist] ContentAssistCommandAdapter should provide an activation expression when activating the command handler
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=301196
	 * 
	 */
	public void testBug301196CorrectHandlerConflictResolution() throws Exception {
		
		final boolean[] handlerInvocationIndicator = new boolean[] {false};
		
		IHandlerService service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		IHandlerActivation handlerActivation = service.activateHandler(IWorkbenchCommandConstants.EDIT_CONTENT_ASSIST, new AbstractHandler() {
			
			@Override
			public Object execute(ExecutionEvent event) {
				handlerInvocationIndicator[0] = true;
				return null;
			}
		},new Expression() {
			@Override
			public void collectExpressionInfo(ExpressionInfo info) {
				info.addVariableNameAccess(ISources.ACTIVE_SHELL_NAME);
			}
			@Override
			public EvaluationResult evaluate(IEvaluationContext context) {
				return EvaluationResult.TRUE; // always enabled
			}
		});
		try {
			getFieldAssistWindow().open();
			sendFocusInToControl();
			executeContentAssistHandler();
			assertTwoShellsUp();
			// The handler registered by the command adapter should win over the test handler.
			assertFalse("test handler should not have been activated", handlerInvocationIndicator[0]);
		} finally {
			service.deactivateHandler(handlerActivation);
		}
	}
}
