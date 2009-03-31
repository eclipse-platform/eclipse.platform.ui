/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 268472
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional.fieldassist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.core.databinding.observable.IDecoratingObservable;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.ISWTObservable;
import org.eclipse.jface.databinding.viewers.IViewerObservable;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

/**
 * <b>EXPERIMENTAL</b>: This class is not API. It is experimental and subject to
 * arbitrary change, including removal. Please provide feedback if you would
 * like to see this become API.
 * <p>
 * Decorates the underlying controls of the target observables of a
 * {@link ValidationStatusProvider} with {@link ControlDecoration}s mirroring
 * the current validation status. Only those target observables which implement
 * {@link ISWTObservable} or {@link IViewerObservable} are decorated.
 * 
 * @since 1.3
 */
public class ControlDecorationSupport {
	/**
	 * <b>EXPERIMENTAL</b>: This method is not API. It is experimental and
	 * subject to arbitrary change, including removal. Please provide feedback
	 * if you would like to see this become API.
	 * 
	 * @param validationStatusProvider
	 * @param position
	 * @return .
	 */
	public static ControlDecorationSupport create(
			ValidationStatusProvider validationStatusProvider, int position) {
		return create(validationStatusProvider, position, null,
				new ControlDecorationUpdater());
	}

	/**
	 * <b>EXPERIMENTAL</b>: This method is not API. It is experimental and
	 * subject to arbitrary change, including removal. Please provide feedback
	 * if you would like to see this become API.
	 * 
	 * @param validationStatusProvider
	 * @param position
	 * @param composite
	 * @return .
	 */
	public static ControlDecorationSupport create(
			ValidationStatusProvider validationStatusProvider, int position,
			Composite composite) {
		return create(validationStatusProvider, position, composite,
				new ControlDecorationUpdater());
	}

	/**
	 * <b>EXPERIMENTAL</b>: This method is not API. It is experimental and
	 * subject to arbitrary change, including removal. Please provide feedback
	 * if you would like to see this become API.
	 * 
	 * @param validationStatusProvider
	 * @param position
	 * @param composite
	 * @param updater
	 * @return .
	 */
	public static ControlDecorationSupport create(
			ValidationStatusProvider validationStatusProvider, int position,
			Composite composite, ControlDecorationUpdater updater) {
		return new ControlDecorationSupport(validationStatusProvider, position,
				composite, updater);
	}

	private final int position;
	private final Composite composite;
	private final ControlDecorationUpdater updater;

	private IObservableValue validationStatus;
	private IObservableList targets;

	private IDisposeListener disposeListener = new IDisposeListener() {
		public void handleDispose(DisposeEvent staleEvent) {
			dispose();
		}
	};

	private IValueChangeListener statusChangeListener = new IValueChangeListener() {
		public void handleValueChange(ValueChangeEvent event) {
			statusChanged((IStatus) validationStatus.getValue());
		}
	};

	private IListChangeListener targetsChangeListener = new IListChangeListener() {
		public void handleListChange(ListChangeEvent event) {
			event.diff.accept(new ListDiffVisitor() {
				public void handleAdd(int index, Object element) {
					targetAdded((IObservable) element);
				}

				public void handleRemove(int index, Object element) {
					targetRemoved((IObservable) element);
				}
			});
			statusChanged((IStatus) validationStatus.getValue());
		}
	};

	private static class TargetDecoration {
		public final IObservable target;
		public final ControlDecoration decoration;

		TargetDecoration(IObservable target, ControlDecoration decoration) {
			this.target = target;
			this.decoration = decoration;
		}
	}

	private List targetDecorations;

	private ControlDecorationSupport(
			ValidationStatusProvider validationStatusProvider, int position,
			Composite composite, ControlDecorationUpdater updater) {
		this.position = position;
		this.composite = composite;
		this.updater = updater;

		this.validationStatus = validationStatusProvider.getValidationStatus();
		Assert.isTrue(!this.validationStatus.isDisposed());

		this.targets = validationStatusProvider.getTargets();
		Assert.isTrue(!this.targets.isDisposed());

		this.targetDecorations = new ArrayList();

		validationStatus.addDisposeListener(disposeListener);
		validationStatus.addValueChangeListener(statusChangeListener);

		targets.addDisposeListener(disposeListener);
		targets.addListChangeListener(targetsChangeListener);

		for (Iterator it = targets.iterator(); it.hasNext();)
			targetAdded((IObservable) it.next());

		statusChanged((IStatus) validationStatus.getValue());
	}

	private void targetAdded(IObservable target) {
		Control control = findControl(target);
		if (control != null)
			targetDecorations.add(new TargetDecoration(target,
					new ControlDecoration(control, position, composite)));
	}

	private void targetRemoved(IObservable target) {
		for (Iterator it = targetDecorations.iterator(); it.hasNext();) {
			TargetDecoration targetDecoration = (TargetDecoration) it.next();
			if (targetDecoration.target == target) {
				targetDecoration.decoration.dispose();
				it.remove();
			}
		}
	}

	private Control findControl(IObservable target) {
		if (target instanceof ISWTObservable) {
			Widget widget = ((ISWTObservable) target).getWidget();
			if (widget instanceof Control)
				return (Control) widget;
		}

		if (target instanceof IViewerObservable) {
			Viewer viewer = ((IViewerObservable) target).getViewer();
			return viewer.getControl();
		}

		if (target instanceof IDecoratingObservable) {
			IObservable decorated = ((IDecoratingObservable) target)
					.getDecorated();
			Control control = findControl(decorated);
			if (control != null)
				return control;
		}

		if (target instanceof IObserving) {
			Object observed = ((IObserving) target).getObserved();
			if (observed instanceof IObservable)
				return findControl((IObservable) observed);
		}

		return null;
	}

	private void statusChanged(IStatus status) {
		for (Iterator it = targetDecorations.iterator(); it.hasNext();) {
			TargetDecoration targetDecoration = (TargetDecoration) it.next();
			ControlDecoration decoration = targetDecoration.decoration;
			updater.update(decoration, status);
		}
	}

	/**
	 * <b>EXPERIMENTAL</b>: This method is not API. It is experimental and
	 * subject to arbitrary change, including removal. Please provide feedback
	 * if you would like to see this become API.
	 * <p>
	 * Disposes this ControlDecorationSupport, including all control decorations
	 * managed by it. A ControlDecorationSupport is automatically disposed when
	 * its target ValidationStatusProvider is disposed.
	 */
	public void dispose() {
		if (validationStatus != null) {
			validationStatus.removeDisposeListener(disposeListener);
			validationStatus.removeValueChangeListener(statusChangeListener);
			validationStatus = null;
		}

		if (targets != null) {
			targets.removeDisposeListener(disposeListener);
			targets.removeListChangeListener(targetsChangeListener);
			targets = null;
		}

		disposeListener = null;
		statusChangeListener = null;
		targetsChangeListener = null;

		if (targetDecorations != null) {
			for (Iterator it = targetDecorations.iterator(); it.hasNext();) {
				TargetDecoration targetDecoration = (TargetDecoration) it
						.next();
				targetDecoration.decoration.dispose();
			}
			targetDecorations.clear();
			targetDecorations = null;
		}
	}
}
