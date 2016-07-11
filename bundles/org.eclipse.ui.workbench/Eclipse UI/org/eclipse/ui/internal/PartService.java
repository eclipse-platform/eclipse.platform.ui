/*******************************************************************************
 * Copyright (c) 2010, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

public class PartService implements IPageChangedListener, IPartListener, IPartListener2,
		IPartService {

	private ListenerList<IPartListener> partListeners = new ListenerList<>();
	private ListenerList<IPartListener2> partListeners2 = new ListenerList<>();

	private WorkbenchPage page;

	void setPage(WorkbenchPage page) {
		if (page == null) {
			if (this.page != null) {
				this.page.removePartListener((IPartListener) this);
				this.page.removePartListener((IPartListener2) this);
			}
		} else {
			page.addPartListener((IPartListener) this);
			page.addPartListener((IPartListener2) this);
		}

		this.page = page;
	}

	@Override
	public void addPartListener(IPartListener listener) {
		partListeners.add(listener);
	}

	@Override
	public void addPartListener(IPartListener2 listener) {
		partListeners2.add(listener);
	}

	@Override
	public IWorkbenchPart getActivePart() {
		return page == null ? null : page.getActivePart();
	}

	@Override
	public IWorkbenchPartReference getActivePartReference() {
		return page == null ? null : page.getActivePartReference();
	}

	@Override
	public void removePartListener(IPartListener listener) {
		partListeners.remove(listener);
	}

	@Override
	public void removePartListener(IPartListener2 listener) {
		partListeners2.remove(listener);
	}

	@Override
	public void partActivated(final IWorkbenchPart part) {
		for (final IPartListener listener : partListeners) {
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.partActivated(part);
				}
			});
		}
	}

	@Override
	public void partBroughtToTop(final IWorkbenchPart part) {
		for (final IPartListener listener : partListeners) {
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.partBroughtToTop(part);
				}
			});
		}
	}

	@Override
	public void partClosed(final IWorkbenchPart part) {
		for (final IPartListener listener : partListeners) {
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.partClosed(part);
				}
			});
		}
	}

	@Override
	public void partDeactivated(final IWorkbenchPart part) {
		for (final IPartListener listener : partListeners) {
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.partDeactivated(part);
				}
			});
		}
	}

	@Override
	public void partOpened(final IWorkbenchPart part) {
		for (final IPartListener listener : partListeners) {
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.partOpened(part);
				}
			});
		}
	}

	@Override
	public void partActivated(final IWorkbenchPartReference partRef) {
		for (final IPartListener2 listener : partListeners2) {
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.partActivated(partRef);
				}
			});
		}
	}

	@Override
	public void partBroughtToTop(final IWorkbenchPartReference partRef) {
		for (final IPartListener2 listener : partListeners2) {
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.partBroughtToTop(partRef);
				}
			});
		}
	}

	@Override
	public void partClosed(final IWorkbenchPartReference partRef) {
		for (final IPartListener2 listener : partListeners2) {
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.partClosed(partRef);
				}
			});
		}
	}

	@Override
	public void partDeactivated(final IWorkbenchPartReference partRef) {
		for (final IPartListener2 listener : partListeners2) {
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.partDeactivated(partRef);
				}
			});
		}
	}

	@Override
	public void partOpened(final IWorkbenchPartReference partRef) {
		for (final IPartListener2 listener : partListeners2) {
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.partOpened(partRef);
				}
			});
		}
	}

	@Override
	public void partHidden(final IWorkbenchPartReference partRef) {
		for (final IPartListener2 listener : partListeners2) {
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.partHidden(partRef);
				}
			});
		}
	}

	@Override
	public void partVisible(final IWorkbenchPartReference partRef) {
		for (final IPartListener2 listener : partListeners2) {
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.partVisible(partRef);
				}
			});
		}
	}

	@Override
	public void partInputChanged(final IWorkbenchPartReference partRef) {
		for (final IPartListener2 listener : partListeners2) {
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.partInputChanged(partRef);
				}
			});
		}
	}

	@Override
	public void pageChanged(final PageChangedEvent event) {
		for (final IPartListener2 listener : partListeners2) {
			if (listener instanceof IPageChangedListener) {
				SafeRunner.run(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						((IPageChangedListener) listener).pageChanged(event);
					}
				});
			}
		}
	}

}
