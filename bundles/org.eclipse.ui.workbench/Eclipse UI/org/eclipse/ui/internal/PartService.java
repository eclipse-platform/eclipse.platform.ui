/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.internal.misc.UIListenerLogging;

public class PartService implements IPartService {
	private static Point ZERO = new Point(0, 0);

	private PartListenerList listeners = new PartListenerList();

	private PartListenerList2 listeners2 = new PartListenerList2();
	private IWorkbenchPartReference activePart = null;

	private String debugListenersKey;
	private String debugListeners2Key;

	private ArrayList activeControlJobs = new ArrayList();

	public PartService(String debugListenersKey, String debugListeners2Key) {
		this.debugListeners2Key = debugListeners2Key;
		this.debugListenersKey = debugListenersKey;
	}

	/*
	 * (non-Javadoc) Method declared on IPartService
	 */
	public void addPartListener(IPartListener l) {
		listeners.addPartListener(l);
	}

	/*
	 * (non-Javadoc) Method declared on IPartService
	 */
	public void addPartListener(IPartListener2 l) {
		listeners2.addPartListener(l);
	}

	/*
	 * (non-Javadoc) Method declared on IPartService
	 */
	public void removePartListener(IPartListener l) {
		listeners.removePartListener(l);
	}

	/*
	 * (non-Javadoc) Method declared on IPartService
	 */
	public void removePartListener(IPartListener2 l) {
		listeners2.removePartListener(l);
	}

	private abstract class PartJob {
		IWorkbenchPartReference ref;
		Control control;

		public PartJob(IWorkbenchPartReference ref, Control c) {
			this.ref = ref;
			this.control = c;
		}

		abstract void fire();
	}

	private class PartListener implements Listener {
		public void handleEvent(Event event) {
			processPartJobs();
		}
	}

	private PartListener partListener = new PartListener();

	private class PartActivated extends PartJob {
		public PartActivated(IWorkbenchPartReference ref, Control c) {
			super(ref, c);
		}

		void fire() {
			firePartActivated(ref);
		}
	}

	private class PartVisible extends PartJob {
		public PartVisible(IWorkbenchPartReference ref, Control c) {
			super(ref, c);
		}

		void fire() {
			firePartVisible(ref);
		}
	}

	private class PartTop extends PartJob {
		public PartTop(IWorkbenchPartReference ref, Control c) {
			super(ref, c);
		}

		void fire() {
			firePartBroughtToTop(ref);
		}
	}

	private Control deferControl(IWorkbenchPartReference ref) {
		IWorkbenchPart part = ref.getPart(false);
		if (part != null) {
			Control control = ((PartSite) part.getSite()).getPane()
					.getControl();
			if (control != null && !control.isDisposed()
					&& (!control.isVisible() || control.getSize().equals(ZERO))) {
				return control;
			}
		}
		return null;
	}

	private void addControlListener(Control control, PartJob listener) {
		control.addListener(SWT.Resize, partListener);
		control.addListener(SWT.Activate, partListener);
		control.addListener(SWT.Dispose, partListener);
		activeControlJobs.add(listener);
	}

	void processPartJobs() {
		if (activeControlJobs.isEmpty()) {
			return;
		}

		ArrayList toRemove = new ArrayList();
		PartJob[] jobs = (PartJob[]) activeControlJobs
				.toArray(new PartJob[activeControlJobs.size()]);
		for (int i = 0; i < jobs.length; i++) {
			PartJob job = jobs[i];
			Control control = job.control;
			if (!control.isDisposed() && !control.getSize().equals(ZERO)
					&& control.isVisible()) {
				toRemove.add(job);
				control.removeListener(SWT.Resize, partListener);
				control.removeListener(SWT.Activate, partListener);
				control.removeListener(SWT.Dispose, partListener);
				job.fire();
			} else if (control.isDisposed()) {
				toRemove.add(job);
			}
		}

		if (toRemove.isEmpty()) {
			return;
		}

		activeControlJobs.removeAll(toRemove);
	}

	/**
	 * @param ref
	 */
	private void firePartActivated(IWorkbenchPartReference ref) {
		IWorkbenchPart part = ref.getPart(false);
		if (part != null) {
			Control control = deferControl(ref);
			if (control != null) {
				addControlListener(control, new PartActivated(ref, control));
				return;
			}
			UIListenerLogging.logPartListenerEvent(debugListenersKey, this,
					part, UIListenerLogging.PE_ACTIVATED);
			listeners.firePartActivated(part);
		}

		UIListenerLogging.logPartListener2Event(debugListeners2Key, this, ref,
				UIListenerLogging.PE2_ACTIVATED);
		listeners2.firePartActivated(ref);
	}

	/**
	 * @param ref
	 */
	public void firePartBroughtToTop(IWorkbenchPartReference ref) {
		IWorkbenchPart part = ref.getPart(false);
		if (part != null) {
			Control control = deferControl(ref);
			if (control != null) {
				addControlListener(control, new PartTop(ref, control));
				return;
			}
			UIListenerLogging.logPartListenerEvent(debugListenersKey, this,
					part, UIListenerLogging.PE_PART_BROUGHT_TO_TOP);
			listeners.firePartBroughtToTop(part);
		}
		UIListenerLogging.logPartListener2Event(debugListeners2Key, this, ref,
				UIListenerLogging.PE2_PART_BROUGHT_TO_TOP);
		listeners2.firePartBroughtToTop(ref);
	}

	/**
	 * @param ref
	 */
	public void firePartClosed(IWorkbenchPartReference ref) {
		IWorkbenchPart part = ref.getPart(false);
		if (part != null) {
			UIListenerLogging.logPartListenerEvent(debugListenersKey, this,
					part, UIListenerLogging.PE_PART_CLOSED);
			listeners.firePartClosed(part);
		}
		UIListenerLogging.logPartListener2Event(debugListeners2Key, this, ref,
				UIListenerLogging.PE2_PART_CLOSED);
		listeners2.firePartClosed(ref);
	}

	/**
	 * @param ref
	 */
	private void firePartDeactivated(IWorkbenchPartReference ref) {
		IWorkbenchPart part = ref.getPart(false);
		if (part != null) {
			UIListenerLogging.logPartListenerEvent(debugListenersKey, this,
					part, UIListenerLogging.PE_PART_DEACTIVATED);
			listeners.firePartDeactivated(part);
		}
		UIListenerLogging.logPartListener2Event(debugListeners2Key, this, ref,
				UIListenerLogging.PE2_PART_DEACTIVATED);
		listeners2.firePartDeactivated(ref);
	}

	public void firePartVisible(IWorkbenchPartReference ref) {
		Control control = deferControl(ref);
		if (control != null) {
			addControlListener(control, new PartVisible(ref, control));
			return;
		}
		UIListenerLogging.logPartListener2Event(debugListeners2Key, this, ref,
				UIListenerLogging.PE2_PART_VISIBLE);
		listeners2.firePartVisible(ref);
	}

	public void firePartHidden(IWorkbenchPartReference ref) {
		UIListenerLogging.logPartListener2Event(debugListeners2Key, this, ref,
				UIListenerLogging.PE2_PART_HIDDEN);
		listeners2.firePartHidden(ref);
	}

	public void firePartInputChanged(IWorkbenchPartReference ref) {
		UIListenerLogging.logPartListener2Event(debugListeners2Key, this, ref,
				UIListenerLogging.PE2_PART_INPUT_CHANGED);
		listeners2.firePartInputChanged(ref);
	}

	/**
	 * @param ref
	 */
	public void firePartOpened(IWorkbenchPartReference ref) {
		IWorkbenchPart part = ref.getPart(false);
		if (part != null) {
			UIListenerLogging.logPartListenerEvent(debugListenersKey, this,
					part, UIListenerLogging.PE_PART_OPENED);
			listeners.firePartOpened(part);
		}
		UIListenerLogging.logPartListener2Event(debugListeners2Key, this, ref,
				UIListenerLogging.PE2_PART_OPENED);
		listeners2.firePartOpened(ref);
	}

	public IWorkbenchPart getActivePart() {
		return activePart == null ? null : activePart.getPart(false);
	}

	public IWorkbenchPartReference getActivePartReference() {
		return activePart;
	}

	public void setActivePart(IWorkbenchPartReference ref) {
		IWorkbenchPartReference oldRef = activePart;

		// Filter out redundant activation events
		if (oldRef == ref) {
			return;
		}

		if (oldRef != null) {
			firePartDeactivated(oldRef);
		}

		activePart = ref;

		if (ref != null) {
			firePartActivated(ref);
		}
	}

	public void firePageChanged(PageChangedEvent event) {
		listeners2.firePageChanged(event);
	}

}
