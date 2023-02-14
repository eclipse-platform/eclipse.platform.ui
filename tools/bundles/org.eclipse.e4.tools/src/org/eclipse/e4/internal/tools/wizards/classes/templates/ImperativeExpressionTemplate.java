/*******************************************************************************
 * Copyright (c) 2016 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Simon Scholz <simon.scholz@vogella.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.classes.templates;

import org.eclipse.e4.internal.tools.wizards.classes.NewImperativeExpressionClassWizard.ImperativeExpressionClass;

public class ImperativeExpressionTemplate
{
	protected static String nl;
	public static synchronized ImperativeExpressionTemplate create(String lineSeparator)
	{
		nl = lineSeparator;
		ImperativeExpressionTemplate result = new ImperativeExpressionTemplate();
		nl = null;
		return result;
	}

	public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
	protected final String TEXT_1 = " ";
	protected final String TEXT_2 = NL + "package ";
	protected final String TEXT_3 = ";";
	protected final String TEXT_4 = NL + NL
			+ "import org.eclipse.e4.core.di.annotations.Evaluate;" + NL + NL + "public class ";
	protected final String TEXT_7 = " {" + NL + "\t@Evaluate" + NL + "\tpublic boolean ";
	protected final String TEXT_8 = "() {" + NL + "\t\treturn true;" + NL + "\t}" + NL + "}" + NL;

	public String generate(Object argument)
	{
		final StringBuilder stringBuffer = new StringBuilder();
		ImperativeExpressionClass domainClass = (ImperativeExpressionClass) argument;
		stringBuffer.append(TEXT_1);
		if( domainClass.getPackageFragment() != null && domainClass.getPackageFragment().getElementName().trim().length() > 0 ) {
			stringBuffer.append(TEXT_2);
			stringBuffer.append( domainClass.getPackageFragment().getElementName() );
			stringBuffer.append(TEXT_3);
		}
		stringBuffer.append(TEXT_4);
		stringBuffer.append( domainClass.getName() );
		stringBuffer.append(TEXT_7);
		stringBuffer.append(domainClass.getEvaluateMethodName());
		stringBuffer.append(TEXT_8);
		return stringBuffer.toString();
	}
}
