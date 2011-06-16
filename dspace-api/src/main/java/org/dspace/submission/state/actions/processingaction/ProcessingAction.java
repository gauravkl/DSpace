package org.dspace.submission.state.actions.processingaction;

import org.dspace.submission.state.actions.SubmissionAction;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 13-aug-2010
 * Time: 14:43:18
 */
public abstract class ProcessingAction extends SubmissionAction {

//    @Override
//    public boolean isAuthorized(Context context, HttpServletRequest request, WorkspaceItem wfi) throws SQLException {
//        ClaimedTask task = null;
//        if(context.getCurrentUser() != null)
//            task = ClaimedTask.findByWorkflowIdAndEPerson(context, wfi.getID(), context.getCurrentUser().getID());
//        //Check if we have claimed the current task
//        //TODO: make sure that claimed tasks get removed if user is removed from group
//        return task != null &&
//                task.getWorkflowID().equals(getParent().getStep().getWorkflow().getID()) &&
//                task.getStepID().equals(getParent().getStep().getId()) &&
//                task.getActionID().equals(getParent().getId());
//    }
}
