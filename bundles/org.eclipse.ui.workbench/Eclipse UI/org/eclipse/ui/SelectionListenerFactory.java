/*******************************************************************************
 * Copyright (c) 2019 Remain BV, Netherlands
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Remain BV - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import static org.eclipse.ui.SelectionListenerFactory.Predicates.alreadyDelivered;
import static org.eclipse.ui.SelectionListenerFactory.Predicates.selectionAlreadyDelivered;
import static org.eclipse.ui.SelectionListenerFactory.Predicates.selfMute;
import static org.eclipse.ui.SelectionListenerFactory.Predicates.targetPartVisible;

import java.util.Objects;
import java.util.function.Predicate;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.internal.PartSelectionListener;

/**
 * A factory that creates specialised selection services which delegate
 * selections to your selection service based on predicates.
 * <p>
 * <b>Usage:</b> (assumes the part implements ISelectionListener)
 * <p>
 * <u> Only visit the listener if our part is visible: </u>
 *
 * <pre>
 * getSite().getPage().addSelectionListener(SelectionListenerFactory.createVisibleListener(this, this));
 * </pre>
 *
 * <p>
 * <u> Only visit the listener if our part is visible and the selection did not
 * come from us: </u>
 *
 * <pre>
 * getSite().getPage().addSelectionListener(SelectionListenerFactory.createVisibleSelfMutedListener(this, this));
 * </pre>
 *
 * <p>
 * <u> Chained predicates: </u>
 *
 * <pre>
 * import static org.eclipse.ui.SelectionListenerFactory.Predicates.adaptsTo;
 * import static org.eclipse.ui.SelectionListenerFactory.Predicates.selectionPartVisible;
 * import static org.eclipse.ui.SelectionListenerFactory.Predicates.selectionSize;
 * import static org.eclipse.ui.SelectionListenerFactory.Predicates.selfMute;
 * import static org.eclipse.ui.SelectionListenerFactory.Predicates.targetPartVisible;
 *
 * Predicate<ISelectionModel> predicate = adaptsTo(PlatformObject.class))
 *		.and(selectionSize(1))
 *		.and(selfMute)
 *		.and(selectionPartVisible)
 *		.and(targetPartVisible));
 *
 * getSite().getPage().addSelectionListener(SelectionListenerFactory.createListener(this, predicate));
 * </pre>
 *
 * <p>
 * <u> Creating your own predicate in combination with the visible part
 * predicate: </u>
 *
 * <pre>
 * Predicate<ISelectionModel> predicate = new Predicate<SelectionListenerFactory.ISelectionModel>() {
 *
 * 	public boolean test(ISelectionModel model) {
 * 		if (model.getCurrentSelectionPart() == SampleView4.this) {
 * 			return false;
 * 		}
 * 		if (!(model.getCurrentSelectionPart() instanceof SampleView)) {
 * 			return false;
 * 		}
 * 		return true;
 * 	}
 * };
 *
 * GetSite().getPage().addSelectionListener(SelectionListenerFactory.createVisibleListener(this, this, predicate));
 * </pre>
 *
 * @since 3.117
 *
 */
public class SelectionListenerFactory {

	/**
	 * A model containing selection values. A predicate can use this model to
	 * determine if a selection needs to be delivered.
	 *
	 * Clients do not have to implement this model. For usage information see the
	 * javadoc of {@link SelectionListenerFactory}.
	 *
	 */
	public interface ISelectionModel {

		/**
		 * @return our part, never null.
		 */
		IWorkbenchPart getTargetPart();

		/**
		 * @return the current selection, could be null if your listener implements
		 *         {@link INullSelectionListener}.;
		 */
		ISelection getCurrentSelection();

		/**
		 * @return the part from which the selection originated, could be null if your
		 *         listener implements {@link INullSelectionListener}.
		 */
		IWorkbenchPart getCurrentSelectionPart();

		/**
		 * @return the selection that was lastly delivered to us, could be null.
		 */
		ISelection getLastDeliveredSelection();

		/**
		 * @return the part from which the last delivered selection originated, could be
		 *         null.
		 */
		IWorkbenchPart getLastDeliveredSelectionPart();

		/**
		 * @return true if the target part is visible
		 */
		boolean isTargetPartVisible();

		/**
		 * @return true if the selection part is visible
		 */
		boolean isSelectionPartVisible();

	}

	/**
	 * Static class to hold the predicates for this factory.
	 *
	 */
	public static class Predicates {

		/**
		 * Creates a predicate that tests true if the selection is an instance of the
		 * passed selection type.
		 *
		 * <p>
		 * Use this predicate if the listener may only see specific selections like
		 * {@link IStructuredSelection}. This will avoid the listener to be visited on
		 * text selections.
		 * </p>
		 *
		 *
		 * @param selectionType A subclass of {@link ISelection} which may not be null.
		 * @return the {@link Predicate} that filters based on the the selection.
		 */
		public static Predicate<ISelectionModel> selectionType(Class<? extends ISelection> selectionType) {
			return model -> !(model.getCurrentSelection() != null
					&& selectionType.isAssignableFrom(model.getCurrentSelection().getClass()));
		}

		/**
		 * Creates a predicate that will test true if the selection is an
		 * {@link IStructuredSelection} and number of elements matches the passed size.
		 *
		 * <p>
		 * A listener might not be able to handle multiple selections. It is bad
		 * practice to only react on the first element in the list. In this case, use
		 * this predicate to tell the selection framework that the listener only wants
		 * to be visited if one element is selected.
		 * </p>
		 *
		 * <p>
		 * A listener might need two or more selected elements (e.g. in a compare). Use
		 * this predicate to tell the selection framework that the listener only wants
		 * to be visited if there are enough elements in the selection.
		 * </p>
		 *
		 *
		 * @param size The number of required elements.
		 *
		 * @return the {@link Predicate} that filters based on the selection size.
		 *
		 * @see #minimalSelectionSize(int)
		 */
		public static Predicate<ISelectionModel> selectionSize(int size) {
			return model -> (model.getCurrentSelection() instanceof IStructuredSelection
					&& ((IStructuredSelection) model.getCurrentSelection()).size() == size);
		}

		/**
		 * Creates a predicate that will test true if the selection is an
		 * {@link IStructuredSelection} and number of elements is at least the passed
		 * size.
		 *
		 * <p>
		 * A listener might need two or more selected elements (e.g. in a compare). Use
		 * this predicate to tell the selection framework that the listener only wants
		 * to be visited if there are enough elements in the selection.
		 * </p>
		 *
		 * @param size The number of elements at least in the list.
		 *
		 * @return the {@link Predicate} that filters based on the selection size.
		 */
		public static Predicate<ISelectionModel> minimalSelectionSize(int size) {
			return model -> (model.getCurrentSelection() instanceof IStructuredSelection
					&& ((IStructuredSelection) model.getCurrentSelection()).size() >= size);
		}

		/**
		 * A predicate that will test true if the selection is not empty.
		 *
		 * <p>
		 * Empty selections can be used to clear the UI but often the UI is left as it
		 * is. Use with care.
		 * </p>
		 *
		 */
		public static Predicate<ISelectionModel> emptySelection = model -> model.getCurrentSelection() != null
				&& !model.getCurrentSelection().isEmpty();

		/**
		 * Creates a predicate that returns true when all the objects contained in an
		 * {@link IStructuredSelection} are adaptable to the passed adapter type.
		 *
		 * <p>
		 * Listeners often want to react only on specific objects. Use this predicate to
		 * only receive selections that contain objects the listener can work with.
		 * </p>
		 *
		 * @param adapterType The class that all elements of the selection must extend
		 *                    or implement. It may not be null.
		 * @return the {@link Predicate} that filters the selection.
		 */
		public static Predicate<ISelectionModel> adaptsTo(Class<?> adapterType) {
			return model -> {
				if (model.getCurrentSelection() instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) model.getCurrentSelection();
					for (Object object : sel.toArray()) {
						if (Adapters.adapt(object, adapterType) == null) {
							return false;
						}
					}
				}
				return true;
			};
		}

		/**
		 * A predicate that tests true when the part that provides the selection is
		 * visible.
		 * <p>
		 * When a part becomes visible it will receive the current selection. However,
		 * this selection may come from a part that now has become invisible. This may
		 * be unwanted because of the missing visual link.
		 * </p>
		 */
		public static Predicate<ISelectionModel> selectionPartVisible = model -> model.isSelectionPartVisible();

		/**
		 * A predicate that tests true if the selection and the part it came from are
		 * the same as the selection listener currently has.
		 * <p>
		 * A part will broadcast its selection again when it gets focus. This can cause
		 * unneeded reactions on selections that the listener already has.
		 * </p>
		 *
		 */
		public static Predicate<ISelectionModel> alreadyDelivered = model -> !(Objects
				.equals(model.getCurrentSelectionPart(), model.getLastDeliveredSelectionPart())
				&& Objects.equals(model.getCurrentSelection(), model.getLastDeliveredSelection()));

		/**
		 * A predicate that tests true if the selection is the same as the selection we
		 * currently have. No matter which part delivered it.
		 * <p>
		 * Multiple parts can broadcast the same selection. This can cause unneeded
		 * reactions on selections that the listener already has.
		 * </p>
		 */
		public static Predicate<ISelectionModel> selectionAlreadyDelivered = model -> !Objects
				.equals(model.getCurrentSelection(), model.getLastDeliveredSelection());

		/**
		 * A predicate that tests true if the selection originates from its own part.
		 * <p>
		 * A selection is also delivered to the part that created it. In most cases this
		 * must be ignored.
		 * </p>
		 */
		public static Predicate<ISelectionModel> selfMute = model -> model.getCurrentSelectionPart() != model
				.getTargetPart();

		/**
		 * A predicate that tests true if the part that receives the selection is
		 * visible.
		 * <p>
		 * A selection is also delivered to parts that are not visible to the user (e.g.
		 * in a different perspective or obscured by other parts). In most cases this
		 * must be ignored.
		 * </p>
		 */
		public static Predicate<ISelectionModel> targetPartVisible = model -> model.isTargetPartVisible();
	}

	/**
	 * Create a listener for a part that also acts as the selection listener.
	 * <p>
	 * The listener will be automatically removed when the part is closed.
	 * </p>
	 *
	 * @param part      the part which also implements the
	 *                  {@link ISelectionListener} to be notified.
	 * @param predicate the predicates must test true before the selection is
	 *                  delivered.
	 * @return the listener
	 */
	public static ISelectionListener createListener(IWorkbenchPart part, Predicate<ISelectionModel> predicate) {
		return new PartSelectionListener(part, (ISelectionListener) part, predicate);
	}

	/**
	 * Create a listener for a part that also acts as the selection listener.
	 * <p>
	 * The listener will be automatically removed when the part is closed.
	 * </p>
	 *
	 * @param part      the part.
	 * @param listener  the selection listener to be notified. It can be the part
	 *                  itself if it implements {@link ISelectionChangedListener}.
	 * @param predicate the predicates must test true before the selection is
	 *                  delivered.
	 * @return the listener
	 */
	public static ISelectionListener createListener(IWorkbenchPart part, ISelectionListener listener,
			Predicate<ISelectionModel> predicate) {
		return new PartSelectionListener(part, listener, predicate);
	}

	/**
	 * Convenience method to create a listener that only gets notified when:
	 * <ul>
	 * <li>the selection has changed;</li>
	 * <li>the part is visible.</li>
	 * </ul>
	 * <p>
	 * The listener will be automatically removed when the part is closed.
	 * </p>
	 *
	 * @param part     the part.
	 * @param listener the selection listener to be notified. It can be the part
	 *                 itself if it implements {@link ISelectionChangedListener}.
	 * @return the listener
	 */
	public static ISelectionListener createVisibleListener(IWorkbenchPart part, ISelectionListener listener) {
		return new PartSelectionListener(part, listener, alreadyDelivered.and(targetPartVisible));
	}

	/**
	 * Convenience method to create a listener that only gets notified when:
	 * <ul>
	 * <li>the selection has changed;</li>
	 * <li>the part is visible.</li>
	 * </ul>
	 * <p>
	 * The listener will be automatically removed when the part is closed.
	 * </p>
	 *
	 * @param part      the part.
	 * @param listener  the selection listener to be notified. It can be the part
	 *                  itself if it implements {@link ISelectionChangedListener}.
	 * @param predicate the predicates must test true before the selection is
	 *                  delivered.
	 * @return the listener
	 */
	public static ISelectionListener createVisibleListener(IWorkbenchPart part, ISelectionListener listener,
			Predicate<ISelectionModel> predicate) {
		return ((PartSelectionListener) createVisibleListener(part, listener)).addPredicate(predicate);
	}

	/**
	 * Provides a listener that only gets notified of selection events when:
	 * <ul>
	 * <li>the selection has changed;</li>
	 * <li>the part is visible;</li>
	 * <li>the selection does not originate from the part.</li>
	 * </ul>
	 * <p>
	 * The listener will be automatically removed when the part is closed.
	 * </p>
	 *
	 * @param part
	 * @param listener the selection listener to be notified. It can be the part
	 *                 itself if it implements {@link ISelectionChangedListener}.
	 *
	 * @return the listener
	 */
	public static ISelectionListener createVisibleSelfMutedListener(IWorkbenchPart part, ISelectionListener listener) {
		return new PartSelectionListener(part, listener,
				selectionAlreadyDelivered.and(targetPartVisible).and(selfMute));
	}

	/**
	 * Provides a listener that only gets notified of selection events when:
	 * <ul>
	 * <li>the selection has changed;</li>
	 * <li>the part is visible;</li>
	 * <li>the selection does not originate from the part.</li>
	 * </ul>
	 * <p>
	 * The listener will be automatically removed if the part is closed.
	 * </p>
	 *
	 * @param part
	 * @param listener  the selection listener to be notified. It can be the part
	 *                  itself if it implements {@link ISelectionChangedListener}.
	 * @param predicate the predicates must test true before the selection is
	 *                  delivered.
	 * @return the listener
	 */
	public static ISelectionListener createVisibleSelfMutedListener(IWorkbenchPart part, ISelectionListener listener,
			Predicate<ISelectionModel> predicate) {
		return ((PartSelectionListener) createVisibleSelfMutedListener(part, listener)).addPredicate(predicate);
	}

	/**
	 * Decorates the passed listener with the passed predicate. The listener must be
	 * created by this factory otherwise a {@link ClassCastException} is thrown.
	 *
	 * @param listener  the listener that was created by this factory.
	 * @param predicate the predicate to and-chain to the existing predicates of
	 *                  this listener
	 * @param replace   true of the passed predicate is the new predicate, false to
	 *                  and-chain it to the existing predicate
	 * @return the listener
	 */
	public static ISelectionListener decorate(ISelectionListener listener, Predicate<ISelectionModel> predicate,
			boolean replace) {
		return ((PartSelectionListener) listener).addPredicate(predicate);
	}
}
