/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.expressions.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IPluginDescriptor;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.internal.expressions.SystemTestExpression;


public class ExpressionTests extends TestCase {

	public static Test suite() {
		return new TestSuite(ExpressionTests.class);
	}
	
	public void testSystemProperty() throws Exception {
		SystemTestExpression expression= new SystemTestExpression("os.name", System.getProperty("os.name"));
		EvaluationResult result= expression.evaluate(new EvaluationContext(null, null));
		assertTrue(result == EvaluationResult.TRUE);
	}
	
	public void testResolvePluginDescriptor() throws Exception {
		IEvaluationContext context= new EvaluationContext(null, null);
		IPluginDescriptor descriptor= (IPluginDescriptor)context.resolveVariable(
			IEvaluationContext.PLUGIN_DESCRIPTOR,
			new String[] { "org.eclipse.jdt.ui.tests.refactoring" });
		assertNotNull(descriptor);
	}
}
