/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * This singleton remembers all JobTreeElements that should be
 * preserved (e.g. because their associated Jobs have the "keep" property set).
 */
class FinishedJobs {

    /*
     * Interface for notify listeners.
     */
    static interface KeptJobsListener {
        /*
         * A job to be kept has finished
         */
        void finished(JobTreeElement jte);

        /*
         * A kept job has been removed.
         */
        void removed(JobTreeElement jte);
    }

    private static FinishedJobs theInstance;

    private static ListenerList listeners = new ListenerList();

    private IJobProgressManagerListener listener;

    private HashSet keptjobinfos = new HashSet();

    private HashMap finishedTime = new HashMap();
	
	private static JobTreeElement[] EMPTY_INFOS;

    static synchronized FinishedJobs getInstance() {
        if (theInstance == null){
            theInstance = new FinishedJobs();
			EMPTY_INFOS = new JobTreeElement[0];
        }
        return theInstance;
    }

    private FinishedJobs() {
        listener = new IJobProgressManagerListener() {
            public void addJob(JobInfo info) {
                checkForDuplicates(info);
            }

            public void addGroup(GroupInfo info) {
                checkForDuplicates(info);
            }

            public void refreshJobInfo(JobInfo info) {
                checkTasks(info);
            }

            public void refreshGroup(GroupInfo info) {
            }

            public void refreshAll() {
            }

            public void removeJob(JobInfo info) {
                if (keep(info)) {
                    checkForDuplicates(info);
                    add(info);
                }
            }

            public void removeGroup(GroupInfo group) {
            }

            public boolean showsDebug() {
                return false;
            }
        };
        ProgressManager.getInstance().addListener(listener);
    }

    /**
     * Returns true if JobInfo indicates that it must be kept.
     */
    static boolean keep(JobInfo info) {
        Job job = info.getJob();
        if (job != null) {
            Object prop = job.getProperty(NewProgressViewer.KEEP_PROPERTY);
            if (prop instanceof Boolean) {
                if (((Boolean) prop).booleanValue())
                    return true;
            }

            prop = job.getProperty(NewProgressViewer.KEEPONE_PROPERTY);
            if (prop instanceof Boolean) {
                if (((Boolean) prop).booleanValue())
                    return true;
            }

            IStatus status = job.getResult();
            if (status != null && status.getSeverity() == IStatus.ERROR)
                return true;
        }
        return false;
    }

    /**
     * Register for notification.
     */
    void addListener(KeptJobsListener l) {
        listeners.add(l);
    }

    /**
     * Deregister for notification.
     */
    void removeListener(KeptJobsListener l) {
        listeners.remove(l);
    }

    private void checkForDuplicates(GroupInfo info) {
        Object[] objects = info.getChildren();
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] instanceof JobInfo)
                checkForDuplicates((JobInfo) objects[i]);
        }
    }

    private void checkForDuplicates(JobTreeElement info) {
        JobTreeElement[] toBeRemoved = findJobsToRemove(info);
        if (toBeRemoved != null) {
            for (int i = 0; i < toBeRemoved.length; i++) {
                remove(toBeRemoved[i]);
            }
        }
    }

    /**
     * Add given Job to list of kept jobs.
     */
    private void add(JobInfo info) {
        boolean fire = false;

        synchronized (keptjobinfos) {
            if (!keptjobinfos.contains(info)) {
                keptjobinfos.add(info);

                long now = System.currentTimeMillis();
                finishedTime.put(info, new Long(now));

                Object parent = info.getParent();
                if (parent != null && !keptjobinfos.contains(parent)) {
                    keptjobinfos.add(parent);
                    finishedTime.put(parent, new Long(now));
                }

                fire = true;
            }
        }

        if (fire) {
            Object l[] = listeners.getListeners();
            for (int i = 0; i < l.length; i++) {
                KeptJobsListener jv = (KeptJobsListener) l[i];
                jv.finished(info);
            }
        }
    }

    static void disposeAction(JobTreeElement jte) {
        if (jte.isJobInfo()) {
            JobInfo ji = (JobInfo) jte;
            Job job = ji.getJob();
            if (job != null) {
                Object prop = job
                        .getProperty(IProgressConstants.ACTION_PROPERTY);
                if (prop instanceof ActionFactory.IWorkbenchAction)
                    ((ActionFactory.IWorkbenchAction) prop).dispose();
            }
        }
    }

    private JobTreeElement[] findJobsToRemove(JobTreeElement info) {

        if (info.isJobInfo()) {
            Job myJob = null;
            if (info instanceof JobInfo)
                myJob = ((JobInfo) info).getJob();
            else if (info instanceof SubTaskInfo) {
                JobInfo parent = (JobInfo) ((SubTaskInfo) info).getParent();
                if (parent != null)
                    myJob = parent.getJob();
            }

            if (myJob != null) {

                Object prop = myJob
                        .getProperty(NewProgressViewer.KEEPONE_PROPERTY);
                if (prop instanceof Boolean && ((Boolean) prop).booleanValue()) {
                    ArrayList found = null;
                    Object myRoot = getRoot(info);
                    JobTreeElement[] all;
                    synchronized (keptjobinfos) {
                        all = (JobTreeElement[]) keptjobinfos
                                .toArray(new JobTreeElement[keptjobinfos.size()]);
                    }
                    for (int i = 0; i < all.length; i++) {
                        JobTreeElement jte = all[i];
                        Object otherRoot = getRoot(jte);
                        if (otherRoot != myRoot && jte.isJobInfo()) {
                            JobInfo ji = (JobInfo) jte;
                            Job job = ji.getJob();
                            if (job != null && job != myJob
                                    && job.belongsTo(myJob)) {
                                if (NewProgressViewer.DEBUG)
                                    System.err
                                            .println("found other from family " + otherRoot); //$NON-NLS-1$
                                if (found == null)
                                    found = new ArrayList();
                                found.add(otherRoot);
                            }
                        }
                    }
                    if (found != null)
                        return (JobTreeElement[]) found
                                .toArray(new JobTreeElement[found.size()]);
                }
            }
        }
        return null;
    }

    private static Object getRoot(JobTreeElement jte) {
        Object parent;
        while ((parent = jte.getParent()) != null)
            jte = (JobTreeElement) parent;
        return jte;
    }

    private void checkTasks(JobInfo info) {
        if (keep(info)) {
            TaskInfo tinfo = info.getTaskInfo();
            if (tinfo != null) {
                JobTreeElement[] toBeRemoved = null;
                boolean fire = false;
                JobTreeElement element = (JobTreeElement) tinfo.getParent();
                synchronized (keptjobinfos) {
                    if (element == info && !keptjobinfos.contains(tinfo)) {
                        toBeRemoved = findJobsToRemove(element);
                        keptjobinfos.add(tinfo);
                        finishedTime.put(tinfo, new Long(System
                                .currentTimeMillis()));
                    }
                }

                if (toBeRemoved != null) {
                    for (int i = 0; i < toBeRemoved.length; i++) {
                        remove(toBeRemoved[i]);
                    }
                }

                if (fire) {
                    Object l[] = listeners.getListeners();
                    for (int i = 0; i < l.length; i++) {
                        KeptJobsListener jv = (KeptJobsListener) l[i];
                        jv.finished(info);
                    }
                }
            }
        }
    }

    boolean remove(JobTreeElement jte) {
        boolean fire = false;
        boolean removed = false;

        synchronized (keptjobinfos) {
            if (keptjobinfos.remove(jte)) {
                removed = true;
                finishedTime.remove(jte);
                disposeAction(jte);
                if (NewProgressViewer.DEBUG)
                    System.err.println("FinishedJobs: sucessfully removed job"); //$NON-NLS-1$

                // delete all elements that have jte as their direct or indirect parent
                JobTreeElement jtes[] = (JobTreeElement[]) keptjobinfos
                        .toArray(new JobTreeElement[keptjobinfos.size()]);
                for (int i = 0; i < jtes.length; i++) {
                    JobTreeElement parent = (JobTreeElement) jtes[i]
                            .getParent();
                    if (parent != null) {
                        if (parent == jte || parent.getParent() == jte) {
                            if (keptjobinfos.remove(jtes[i]))
                                disposeAction(jtes[i]);
                            finishedTime.remove(jtes[i]);
                        }
                    }
                }
                fire = true;
            }
        }

        if (fire) {
            // notify listeners
            Object l[] = listeners.getListeners();
            for (int i = 0; i < l.length; i++) {
                KeptJobsListener jv = (KeptJobsListener) l[i];
                jv.removed(jte);
            }
        }
        return removed;
    }

    /**
     * Returns all kept elements sorted by finished date.
     */
    JobTreeElement[] getJobInfos() {
        JobTreeElement[] all;
		if(keptjobinfos.isEmpty())
			return EMPTY_INFOS;
		
        synchronized (keptjobinfos) {
            all = (JobTreeElement[]) keptjobinfos
                    .toArray(new JobTreeElement[keptjobinfos.size()]);
        }
        Arrays.sort(all, new Comparator() {
            public int compare(Object o1, Object o2) {
                long t1 = getFinishedDateAsLong((JobTreeElement) o1);
                long t2 = getFinishedDateAsLong((JobTreeElement) o2);
                if (t1 < t2)
                    return -1;
                if (t1 > t2)
                    return 1;
                return 0;
            }
        });
        return all;
    }

    private long getFinishedDateAsLong(JobTreeElement jte) {
        Object o = finishedTime.get(jte);
        if (o instanceof Long)
            return ((Long) o).longValue();
        return 0;
    }

    public Date getFinishDate(JobTreeElement jte) {
        Object o = finishedTime.get(jte);
        if (o instanceof Long)
            return new Date(((Long) o).longValue());
        return null;
    }

    public boolean isFinished(JobTreeElement element) {
        return keptjobinfos.contains(element);
    }

    public void clearAll() {
        synchronized (keptjobinfos) {
            JobTreeElement[] all = (JobTreeElement[]) keptjobinfos
                    .toArray(new JobTreeElement[keptjobinfos.size()]);
            for (int i = 0; i < all.length; i++)
                disposeAction(all[i]);
            keptjobinfos.clear();
            finishedTime.clear();
        }

        // notify listeners
        Object l[] = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            KeptJobsListener jv = (KeptJobsListener) l[i];
            jv.removed(null);
        }
    }
}
