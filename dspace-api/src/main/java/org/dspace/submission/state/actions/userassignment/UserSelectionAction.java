package org.dspace.submission.state.actions.userassignment;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.WorkspaceService;
import org.dspace.core.Context;
import org.dspace.submission.RoleMembers;
import org.dspace.submission.state.actions.SubmissionAction;

import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: bram
 * Date: 6-aug-2010
 * Time: 16:31:21
 * To change this template use File | Settings | File Templates.
 */
public abstract class UserSelectionAction extends SubmissionAction {

    protected static Logger log = Logger.getLogger(UserSelectionAction.class);

    public abstract boolean isFinished(WorkspaceService wfi);

//    @Override
//    public boolean isAuthorized(Context context, HttpServletRequest request, WorkspaceService wfi) throws SQLException, AuthorizeException, IOException, WorkflowConfigurationException {
//        PoolTask task = null;
//        if(context.getCurrentUser() != null)
//            task = PoolTask.findByWorkflowIdAndEPerson(context, wfi.getID(), context.getCurrentUser().getID());
//
//        //Check if we have pooled the current task
//        //TODO: make sure that claimed tasks get removed if user is removed from group
//        return task != null &&
//                task.getWorkflowID().equals(getParent().getStep().getWorkflow().getID()) &&
//                task.getStepID().equals(getParent().getStep().getId()) &&
//                task.getActionID().equals(getParent().getId());
//    }

    /**
     * Should a person have the option to repool the task the tasks will have to be regenerated
     * @param c the dspace context
     * @param wfi the workflowitem
     * @param roleMembers the list of users for which tasks must be regenerated
     * @throws java.sql.SQLException ...
     */
    public abstract void regenerateTasks(Context c, WorkspaceService wfi, RoleMembers roleMembers) throws SQLException, AuthorizeException;

    public abstract boolean isValidUserSelection(Context context, WorkspaceService wfi, boolean hasUI) throws //WorkflowConfigurationException,
     SQLException;

    /**
     * A boolean indicating wether or not the task pool is used for this type of user selection
     * @return a boolean
     */
    public abstract boolean usesTaskPool();
}
