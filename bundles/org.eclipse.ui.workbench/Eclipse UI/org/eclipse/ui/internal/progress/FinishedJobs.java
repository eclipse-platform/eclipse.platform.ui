package org.eclipse.ui.internal.progress;

import java.util.HashSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.ListenerList;

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
        /*
         * All kept jobs have been been viewed.
         */
        void infoVisited();
    }

    private static FinishedJobs theInstance;
    private static ListenerList listeners = new ListenerList();
    private IJobProgressManagerListener listener;
    private HashSet keptjobinfos = new HashSet();
    private long timeStamp;

    
    static synchronized FinishedJobs getInstance() {
        if (theInstance == null) theInstance = new FinishedJobs();
        return theInstance;
    }

    private FinishedJobs() {
        listener = new IJobProgressManagerListener() {
            public void addJob(JobInfo info) {
            }
            public void addGroup(GroupInfo info) {
            }
            public void refreshJobInfo(JobInfo info) {
                checkTasks(info);
            }
            public void refreshGroup(GroupInfo info) {
            }
            public void refreshAll() {
            }
            public void removeJob(JobInfo info) {
                if (keep(info))
                    add(info);
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
    boolean keep(JobInfo info) {
        Job job = info.getJob();
        if (job != null) {
            Object prop = job.getProperty(NewProgressViewer.KEEP_PROPERTY);
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

    /**
     * Add given Job to list of kept jobs.
     */
    private void add(JobInfo info) {
        boolean fire = false;

        synchronized (keptjobinfos) {
            if (!keptjobinfos.contains(info)) {
                keptjobinfos.add(info);

                Object parent = info.getParent();
                if (parent != null && !keptjobinfos.contains(parent))
                	keptjobinfos.add(parent);

                timeStamp++;
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

    private void checkTasks(JobInfo info) {
        if (keep(info)) {
	        TaskInfo tinfo = info.getTaskInfo();
	        if (tinfo != null) {
	            boolean fire = false;
	        	JobTreeElement element = (JobTreeElement) tinfo.getParent();
	        	synchronized (keptjobinfos) {
		        	if (element == info && !keptjobinfos.contains(tinfo)) {
		                keptjobinfos.add(tinfo);
		                timeStamp++;
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

    void remove(KeptJobsListener sender, JobTreeElement jte) {
        boolean fire = false;
   	
        synchronized (keptjobinfos) {
	        if (keptjobinfos.remove(jte)) {
	            if (NewProgressViewer.DEBUG) System.err.println("FinishedJobs: sucessfully removed job");
	
	            // delete all elements that have jte as their direct or indirect parent
	            JobTreeElement jtes[] = (JobTreeElement[]) keptjobinfos.toArray(new JobTreeElement[keptjobinfos.size()]);
	            for (int i = 0; i < jtes.length; i++) {
	            	JobTreeElement parent = (JobTreeElement) jtes[i].getParent();
	                if (parent != null) {
	                	if (parent == jte || parent.getParent() == jte)
	                		keptjobinfos.remove(jtes[i]);
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
	            if (jv != sender) jv.removed(jte);
	        }
        }
    }
    
    void refresh() {
        if (NewProgressViewer.DEBUG) System.err.println("FinishedJobs: refresh");
        Object l[] = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            KeptJobsListener jv = (KeptJobsListener) l[i];
            jv.infoVisited();
        }
    }

    JobTreeElement[] getJobInfos() {
        synchronized (keptjobinfos) {
            return (JobTreeElement[]) keptjobinfos.toArray(new JobTreeElement[keptjobinfos.size()]);
        }
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
