/*******************************************************************************
 * Copyright (c) 2022 Jens Lidestrom and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jens Lidestrom - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.internal.databinding.bind;

import java.util.Objects;

import org.eclipse.core.databinding.UpdateListStrategy;
import org.eclipse.core.databinding.UpdateSetStrategy;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.validation.IValidator;

final class UpdataStrategyEntry {
	private int defaultUpdatePolicy;
	private Boolean proivdeDefaults = null;
	private IConverter<?, ?> converter = null;
	private IObservable observable = null;
	private Integer updatePolicy = null;
	private IValidator<?> afterGetValidator = null;
	private IValidator<?> afterConvertValidator = null;
	private IValidator<?> beforeSetValidator = null;

	public boolean isProvideDefaults() {
		return proivdeDefaults == null ? false : proivdeDefaults;
	}

	public void setDefaultUpdatePolicy(int defaultUpdatePolicy) {
		this.defaultUpdatePolicy = defaultUpdatePolicy;
	}

	public void setProvideDefaults(boolean provideDefaults) {
		BindingBuilder.verifyNotSet(this.proivdeDefaults);
		this.proivdeDefaults = provideDefaults;
	}

	@SuppressWarnings("unchecked")
	public <S, D> IConverter<S, D> getConverter() {
		return (IConverter<S, D>) converter;
	}

	public void setConverter(IConverter<?, ?> converter) {
		BindingBuilder.verifyNotSet(this.converter);
		this.converter = Objects.requireNonNull(converter);
	}

	public IObservable getObservable() {
		return observable;
	}

	void setObservable(IObservable observable) {
		BindingBuilder.verifyNotSet(this.observable);
		this.observable = Objects.requireNonNull(observable);
	}

	public int getUpdatePolicy() {
		return updatePolicy == null ? defaultUpdatePolicy : updatePolicy;
	}

	public void setUpdatePolicy(int updatePolicy) {
		BindingBuilder.verifyNotSet(this.updatePolicy);
		this.updatePolicy = updatePolicy;
	}

	@SuppressWarnings("unchecked")
	public <T> IValidator<T> getAfterGetValidator() {
		return (IValidator<T>) afterGetValidator;
	}

	public void setAfterGetValidator(IValidator<?> afterGetValidator) {
		BindingBuilder.verifyNotSet(this.afterGetValidator);
		this.afterGetValidator = Objects.requireNonNull(afterGetValidator);
	}

	@SuppressWarnings("unchecked")
	public <T> IValidator<T> getAfterConvertValidator() {
		return (IValidator<T>) afterConvertValidator;
	}

	public void setAfterConvertValidator(IValidator<?> afterConvertValidator) {
		BindingBuilder.verifyNotSet(this.afterConvertValidator);
		this.afterConvertValidator = Objects.requireNonNull(afterConvertValidator);
	}

	@SuppressWarnings("unchecked")
	public <T> IValidator<T> getBeforeSetValidator() {
		return (IValidator<T>) beforeSetValidator;
	}

	public void setBeforeSetValidator(IValidator<?> beforeSetValidator) {
		BindingBuilder.verifyNotSet(this.beforeSetValidator);
		this.beforeSetValidator = Objects.requireNonNull(beforeSetValidator);
	}

	public <S, D> UpdateValueStrategy<S, D> createUpdateValueStrategy() {
		UpdateValueStrategy<S, D> strategy = new UpdateValueStrategy<>(isProvideDefaults(), getUpdatePolicy());
		strategy.setConverter(getConverter());
		strategy.setAfterConvertValidator(getAfterConvertValidator());
		strategy.setAfterGetValidator(getAfterGetValidator());
		strategy.setBeforeSetValidator(getBeforeSetValidator());
		return strategy;
	}

	public <S, D> UpdateListStrategy<S, D> createUpdateListStrategy() {
		UpdateListStrategy<S, D> strategy = new UpdateListStrategy<>(isProvideDefaults(), getUpdatePolicy());
		strategy.setConverter(getConverter());
		return strategy;
	}

	public <S, D> UpdateSetStrategy<S, D> createUpdateSetStrategy() {
		UpdateSetStrategy<S, D> strategy = new UpdateSetStrategy<>(isProvideDefaults(), getUpdatePolicy());
		strategy.setConverter(getConverter());
		return strategy;
	}

}