package org.eclipse.ui.internal.progress;

import java.util.HashSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.ListenerList;


/**
 * This singleton remembers all terminated JobTreeElements that 
 * should be preserved (e.g. because their associated Jobs the "keep"
 * property set).
 */
class FinishedJobs {
    
    static interface KeptJobsListener {
        void finished(JobTreeElement jte);
        void removed(JobTreeElement jte);
        void infoVisited();
    }

    private static FinishedJobs theInstance;
    private static ListenerList listeners= new ListenerList();
	private IJobProgressManagerListener listener;
	private HashSet keptjobinfos= new HashSet();
	private long timeStamp;
    
    
    static synchronized FinishedJobs getInstance() {
        if (theInstance == null)
            theInstance= new FinishedJobs();
        return theInstance;
    }
    
    private FinishedJobs() {
	    listener= new IJobProgressManagerListener() {
            public void addJob(JobInfo info) {
            }

            public void addGroup(GroupInfo info) {
            }

            public void refreshJobInfo(JobInfo info) {
            }

            public void refreshGroup(GroupInfo info) {
            }

            public void refreshAll() {
            }

            public void removeJob(JobInfo info) {
                Job job= info.getJob();
                if (job != null) {
                    Object prop= job.getProperty(NewProgressViewer.KEEP_PROPERTY);
                    if (prop instanceof Boolean) {
                        if (((Boolean)prop).booleanValue())
                            add(info);
                    }
                    IStatus status= job.getResult();
                    if (status != null && status.getSeverity() == IStatus.ERROR)
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
       
    void addListener(KeptJobsListener l) {
        listeners.add(l);
    }

    void removeListener(KeptJobsListener l) {
        listeners.remove(l);
    }
    
    void add(JobInfo info) {
    	boolean fire= false;
    	
    	synchronized (keptjobinfos) {
	        if (!keptjobinfos.contains(info)) {
	            keptjobinfos.add(info);
	            
	            Object parent= info.getParent();
	            if (parent != null) {
            		//System.err.println("FinishedJobs: added child to group");
	            	if (!keptjobinfos.contains(parent)) {
	            		//System.err.println("FinishedJobs: added group");
	            		keptjobinfos.add(parent);
	            	}
	            }
	            
	            timeStamp++;
	            fire= true;
	        }
    	}
            
        if (fire) {
            if (NewProgressViewer.DEBUG) System.err.println("FinishedJobs: added job");
            Object l[]= listeners.getListeners();
			for (int i= 0; i < l.length; i++) {
			    KeptJobsListener jv= (KeptJobsListener) l[i];
			    jv.finished(info);
			}
        }
    }

    void remove(KeptJobsListener sender, JobTreeElement jte) {
        if (keptjobinfos.remove(jte)) {
        	if (NewProgressViewer.DEBUG) System.err.println("FinishedJobs: sucessfully removed job");
        	
        	// delete all children
        	JobTreeElement jtes[]= (JobTreeElement[]) keptjobinfos.toArray(new JobTreeElement[keptjobinfos.size()]);
        	for (int i= 0; i < jtes.length; i++)
        		if (jtes[i].getParent() == jte)
        			keptjobinfos.remove(jtes[i]);
        			
        	
            Object l[]= listeners.getListeners();
			for (int i= 0; i < l.length; i++) {
			    KeptJobsListener jv= (KeptJobsListener) l[i];
			    if (jv != sender)
			        jv.removed(jte);
			}
        }
    }
    
    void refresh() {
    	if (NewProgressViewer.DEBUG) System.err.println("FinishedJobs: refresh");
        Object l[]= listeners.getListeners();
		for (int i= 0; i < l.length; i++) {
		    KeptJobsListener jv= (KeptJobsListener) l[i];
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
