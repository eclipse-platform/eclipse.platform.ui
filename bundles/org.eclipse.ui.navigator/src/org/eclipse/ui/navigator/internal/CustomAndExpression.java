package org.eclipse.ui.navigator.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.expressions.ElementHandler;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;

/**
 * Create an AND-type core expression from an IConfigurationElement of arbitrary name. 
 *
 */
public class CustomAndExpression extends Expression {

	protected List fExpressions;

	/**
	 * Create an AND-type core expression from an IConfigurationElement of arbitrary name. 
	 *
	 *@param element An IConfigurationElement of arbitrary name.
	 */
	public CustomAndExpression(IConfigurationElement element) {
		Assert.isNotNull(element);

		IConfigurationElement[] children = element.getChildren();

		if (children.length > 0)
			fExpressions = new ArrayList();
		for (int i = 0; i < children.length; i++) {
			try {
				fExpressions.add(ElementHandler.getDefault().create(
						ExpressionConverter.getDefault(), children[i]));
			} catch (CoreException ce) {
				NavigatorPlugin.log(IStatus.ERROR, 0, ce.getMessage(), ce);
			}
		}
	}

	public EvaluationResult evaluate(IEvaluationContext scope)
			throws CoreException {
		if (fExpressions == null)
			return EvaluationResult.TRUE;
		EvaluationResult result = EvaluationResult.TRUE;
		for (Iterator iter = fExpressions.iterator(); iter.hasNext();) {
			Expression expression = (Expression) iter.next();
			result = result.and(expression.evaluate(scope));
			// keep iterating even if we have a not loaded found. It can be
			// that we find a false which will result in a better result.
			if (result == EvaluationResult.FALSE)
				return result;
		}
		return result;
	}

}
