/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
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

	private ListenerList partListeners = new ListenerList();
	private ListenerList partListeners2 = new ListenerList();

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

	public void addPartListener(IPartListener listener) {
		partListeners.add(listener);
	}

	public void addPartListener(IPartListener2 listener) {
		partListeners2.add(listener);
	}

	public IWorkbenchPart getActivePart() {
		return page == null ? null : page.getActivePart();
	}

	public IWorkbenchPartReference getActivePartReference() {
		return page == null ? null : page.getActivePartReference();
	}

	public void removePartListener(IPartListener listener) {
		partListeners.remove(listener);
	}

	public void removePartListener(IPartListener2 listener) {
		partListeners2.remove(listener);
	}

	public void partActivated(final IWorkbenchPart part) {
		Object[] listeners = partListeners.getListeners();
		for (final Object listener : listeners) {
			SafeRunner.run(new SafeRunnable() {
				public void run() throws Exception {
					((IPartListener) listener).partActivated(part);
				}
			});
		}
	}

	public void partBroughtToTop(final IWorkbenchPart part) {
		Object[] listeners = partListeners.getListeners();
		for (final Object listener : listeners) {
			SafeRunner.run(new SafeRunnable() {
				public void run() throws Exception {
					((IPartListener) listener).partBroughtToTop(part);
				}
			});
		}
	}

	public void partClosed(final IWorkbenchPart part) {
		Object[] listeners = partListeners.getListeners();
		for (final Object listener : listeners) {
			SafeRunner.run(new SafeRunnable() {
				public void run() throws Exception {
					((IPartListener) listener).partClosed(part);
				}
			});
		}
	}

	public void partDeactivated(final IWorkbenchPart part) {
		Object[] listeners = partListeners.getListeners();
		for (final Object listener : listeners) {
			SafeRunner.run(new SafeRunnable() {
				public void run() throws Exception {
					((IPartListener) listener).partDeactivated(part);
				}
			});
		}
	}

	public void partOpened(final IWorkbenchPart part) {
		Object[] listeners = partListeners.getListeners();
		for (final Object listener : listeners) {
			SafeRunner.run(new SafeRunnable() {
				public void run() throws Exception {
					((IPartListener) listener).partOpened(part);
				}
			});
		}
	}

	public void partActivated(final IWorkbenchPartReference partRef) {
		Object[] listeners = partListeners2.getListeners();
		for (final Object listener : listeners) {
			SafeRunner.run(new SafeRunnable() {
				public void run() throws Exception {
					((IPartListener2) listener).partActivated(partRef);
				}
			});
		}
	}

	public void partBroughtToTop(final IWorkbenchPartReference partRef) {
		Object[] listeners = partListeners2.getListeners();
		for (final Object listener : listeners) {
			SafeRunner.run(new SafeRunnable() {
				public void run() throws Exception {
					((IPartListener2) listener).partBroughtToTop(partRef);
				}
			});
		}
	}

	public void partClosed(final IWorkbenchPartReference partRef) {
		Object[] listeners = partListeners2.getListeners();
		for (final Object listener : listeners) {
			SafeRunner.run(new SafeRunnable() {
				public void run() throws Exception {
					((IPartListener2) listener).partClosed(partRef);
				}
			});
		}
	}

	public void partDeactivated(final IWorkbenchPartReference partRef) {
		Object[] listeners = partListeners2.getListeners();
		for (final Object listener : listeners) {
			SafeRunner.run(new SafeRunnable() {
				public void run() throws Exception {
					((IPartListener2) listener).partDeactivated(partRef);
				}
			});
		}
	}

	public void partOpened(final IWorkbenchPartReference partRef) {
		Object[] listeners = partListeners2.getListeners();
		for (final Object listener : listeners) {
			SafeRunner.run(new SafeRunnable() {
				public void run() throws Exception {
					((IPartListener2) listener).partOpened(partRef);
				}
			});
		}
	}

	public void partHidden(final IWorkbenchPartReference partRef) {
		Object[] listeners = partListeners2.getListeners();
		for (final Object listener : listeners) {
			SafeRunner.run(new SafeRunnable() {
				public void run() throws Exception {
					((IPartListener2) listener).partHidden(partRef);
				}
			});
		}
	}

	public void partVisible(final IWorkbenchPartReference partRef) {
		Object[] listeners = partListeners2.getListeners();
		for (final Object listener : listeners) {
			SafeRunner.run(new SafeRunnable() {
				public void run() throws Exception {
					((IPartListener2) listener).partVisible(partRef);
				}
			});
		}
	}

	public void partInputChanged(final IWorkbenchPartReference partRef) {
		Object[] listeners = partListeners2.getListeners();
		for (final Object listener : listeners) {
			SafeRunner.run(new SafeRunnable() {
				public void run() throws Exception {
					((IPartListener2) listener).partInputChanged(partRef);
				}
			});
		}
	}

	public void pageChanged(final PageChangedEvent event) {
		Object[] listeners = partListeners2.getListeners();
		for (final Object listener : listeners) {
			if (listener instanceof IPageChangedListener) {
				SafeRunner.run(new SafeRunnable() {
					public void run() throws Exception {
						((IPageChangedListener) listener).pageChanged(event);
					}
				});
			}
		}
	}

}
