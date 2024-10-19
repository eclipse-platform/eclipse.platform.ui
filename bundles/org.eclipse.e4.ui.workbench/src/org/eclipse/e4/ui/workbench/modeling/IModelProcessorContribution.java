/*******************************************************************************
 * Copyright (c) 2021 Dirk Fauth and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.modeling;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;
import org.osgi.service.component.annotations.ComponentPropertyType;

/**
 * Service component interface to be able to register model processors via OSGi
 * services.
 * <p>
 * Programmatic processor that gets called with the intention of modifying UI
 * model.
 * </p>
 *
 * @since 1.13
 */
public interface IModelProcessorContribution {

	/**
	 * Service property key for specifying the beforeFragment attribute, which
	 * specifies if the processor has to be invoked before model fragments are
	 * added. If not specified it defaults to {@code true}.
	 */
	String BEFORE_FRAGMENT_PROPERTY_KEY = "beforefragment"; //$NON-NLS-1$
	/**
	 * Service property key for specifying the beforeFragment attribute, which
	 * specifies if the processor has to be invoked before model fragments are
	 * added. If not specified it defaults to <code>true</code>.
	 * <p>
	 * This constant can be used to simplify the property definition in the
	 * Component annotation:<br>
	 * <code>@Component(property = { IModelProcessorContribution.BEFORE_FRAGMENT_PROPERTY_PREFIX + "false" })</code>
	 * </p>
	 *
	 * @deprecated Instead annotate the component with the
	 *             {@link Beforefragment @Beforefragment(true|false)} component
	 *             property type
	 */
	@Deprecated(forRemoval = true, since = "1.16")
	String BEFORE_FRAGMENT_PROPERTY_PREFIX = "beforefragment:Boolean="; //$NON-NLS-1$

	/**
	 * An OSGi service component property type used to specify the value of the
	 * {@code beforeFragment} attribute , which specifies if the processor has to be
	 * invoked before model fragments are added. If not specified it defaults to
	 * {@code true}.
	 *
	 * @since 1.16
	 * @see #BEFORE_FRAGMENT_PROPERTY_KEY
	 */
	@ComponentPropertyType
	@Target(ElementType.TYPE)
	@interface Beforefragment {
		boolean value() default true;
	}

	/**
	 * Service property key for specifying the apply attribute, which defines in
	 * which case a processor is run. If not specified it defaults to
	 * {@code always}.
	 */
	String APPLY_PROPERTY_KEY = "apply"; //$NON-NLS-1$
	/**
	 * Service property key for specifying the apply attribute, which defines in
	 * which case a processor is run. If not specified it defaults to <i>always</i>.
	 * <p>
	 * This constant can be used to simplify the property definition in the
	 * Component annotation:<br>
	 * <code>@Component(property = { IModelProcessorContribution.APPLY_PROPERTY_PREFIX + "initial" })</code>
	 * </p>
	 *
	 * @deprecated Instead annotate the component with the {@link Apply @Apply(&lt;
	 *             a-value &gt;)} component property type
	 */
	@Deprecated(forRemoval = true, since = "1.16")
	String APPLY_PROPERTY_PREFIX = "apply="; //$NON-NLS-1$

	/**
	 * An OSGi service component property type used to specify the {@code apply}
	 * attribute, which defines in which case a processor is run. If not specified
	 * it defaults to {@code always}.
	 *
	 * @since 1.16
	 * @see #APPLY_PROPERTY_KEY
	 * @see #APPLY_ALWAYS
	 * @see #APPLY_INITIAL
	 *
	 */
	@ComponentPropertyType
	@Target(ElementType.TYPE)
	@interface Apply {
		String value() default APPLY_ALWAYS;
	}

	/**
	 * Value for the <code>apply</code> attribute. If set the processor is executed
	 * each time the application is started.
	 */
	String APPLY_ALWAYS = "always"; //$NON-NLS-1$
	/**
	 * Value for the <code>apply</code> attribute. If set the processor is executed
	 * only when coming from a none persistent state.
	 */
	String APPLY_INITIAL = "initial"; //$NON-NLS-1$

	/**
	 *
	 * @return Java class containing model processor. A class method with the
	 *         qualifier <code>"org.eclipse.e4.core.di.annotations.Execute"</code>,
	 *         will be invoked as a part of the model processing. If this method
	 *         returns <code>null</code> it is expected that this
	 *         {@link IModelProcessorContribution} contains a method with the
	 *         qualifier <code>"org.eclipse.e4.core.di.annotations.Execute"</code>.
	 */
	default Class<?> getProcessorClass() {
		return null;
	}

	/**
	 *
	 * @return model elements to be added to the context used to invoke the
	 *         processor.
	 */
	default List<ModelElement> getModelElements() {
		return Collections.emptyList();
	}

	/**
	 * A model element to be added to the context used to invoke the processor.
	 */
	class ModelElement {
		private final String id;
		private final String contextKey;

		/**
		 *
		 * @param id Identifier of a model element to be added to the context.
		 */
		public ModelElement(String id) {
			this(id, null);
		}

		/**
		 *
		 * @param id         Identifier of a model element to be added to the context.
		 * @param contextKey An optional key under which to store the model element in
		 *                   the context. The value of "id" is used if this value is not
		 *                   specified.
		 */
		public ModelElement(String id, String contextKey) {
			this.id = id;
			this.contextKey = contextKey;
		}

		/**
		 * @return Identifier of a model element to be added to the context.
		 */
		public String getId() {
			return id;
		}

		/**
		 * @return An optional key under which to store the model element in the
		 *         context. The value of "id" is used if this value is not specified.
		 */
		public String getContextKey() {
			return contextKey;
		}

	}
}
