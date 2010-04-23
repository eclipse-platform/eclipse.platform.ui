/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.dnd;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.internal.navigator.CustomAndExpression;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.NavigatorSafeRunnable;
import org.eclipse.ui.internal.navigator.extensions.INavigatorContentExtPtConstants;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;

/**
 * @since 3.2
 * 
 */
public final class CommonDropAdapterDescriptor implements
		INavigatorContentExtPtConstants {

	private final IConfigurationElement element;

	private final INavigatorContentDescriptor contentDescriptor;

	private Expression dropExpr;

	/* package */CommonDropAdapterDescriptor(
			IConfigurationElement aConfigElement,
			INavigatorContentDescriptor aContentDescriptor) {
		element = aConfigElement;
		contentDescriptor = aContentDescriptor;
		init();
	}

	private void init() {
		IConfigurationElement[] children = element.getChildren(TAG_POSSIBLE_DROP_TARGETS);
		if (children.length == 1) {
			dropExpr = new CustomAndExpression(children[0]);
		}
	}

	/**
	 * 
	 * @param anElement
	 *            The element from the set of elements being dragged.
	 * @return True if the element matches the drag expression from the
	 *         extension.
	 */
	public boolean isDragElementSupported(Object anElement) { 
		return contentDescriptor.isPossibleChild(anElement); 
	}

	/**
	 * 
	 * @param aSelection
	 *            The set of elements being dragged.
	 * @return True if the element matches the drag expression from the
	 *         extension.
	 */
	public boolean areDragElementsSupported(IStructuredSelection aSelection) {
		if (aSelection.isEmpty()) {
			return false;
		}
		return contentDescriptor.arePossibleChildren(aSelection);
	}

	/**
	 * 
	 * @param anElement
	 *            The element from the set of elements benig dropped.
	 * @return True if the element matches the drop expression from the
	 *         extension.
	 */
	public boolean isDropElementSupported(Object anElement) {
		if (dropExpr != null && anElement != null) {
			IEvaluationContext context = NavigatorPlugin.getEvalContext(anElement);
			return NavigatorPlugin.safeEvaluate(dropExpr, context) == EvaluationResult.TRUE;
		}
		return false;
	}

	/**
	 * 
	 * @return An instance of {@link CommonDropAdapterAssistant} from the
	 *         descriptor or {@link SkeletonCommonDropAssistant}.
	 */
	public CommonDropAdapterAssistant createDropAssistant() {
		final CommonDropAdapterAssistant[] retValue = new CommonDropAdapterAssistant[1];
		SafeRunner.run(new NavigatorSafeRunnable(element) {
			public void run() throws Exception {
				retValue[0] = (CommonDropAdapterAssistant) element
						.createExecutableExtension(ATT_CLASS);
			}
		});
		if (retValue[0] != null)
			return retValue[0];
		return SkeletonCommonDropAssistant.INSTANCE;
	}

	/**
	 * 
	 * @return The content descriptor that contains this drop descriptor.
	 */
	public INavigatorContentDescriptor getContentDescriptor() {
		return contentDescriptor;
	}

}
