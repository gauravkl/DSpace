package org.dspace.submission;

import org.apache.log4j.Logger;
import org.dspace.app.util.SubmissionInfo;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.eperson.EPerson;
import org.dspace.submission.state.SubmissionProcess;
import org.dspace.submission.state.SubmissionStep;
import org.dspace.submission.state.actions.ActionResult;
import org.dspace.submission.state.actions.SubmissionAction;
import org.dspace.submission.state.actions.SubmissionActionConfig;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: bram
 * Date: 2-aug-2010
 * Time: 17:32:44
 * To change this template use File | Settings | File Templates.
 */
public class SubmissionManager {

    private static Logger log = Logger.getLogger(SubmissionManager.class);

//    public static void start(Context context, WorkspaceItem wsi) throws SQLException, AuthorizeException, IOException, WorkflowConfigurationException, MessagingException, WorkflowException {
//        Item myitem = wsi.getItem();
//        Collection collection = wsi.getCollection();
//        SubmissionProcess wf = WorkflowFactory.getWorkflow(collection);
//
//        WorkspaceService wfi = WorkspaceService.create(context);
//        wfi.setItem(myitem);
//        wfi.setCollection(wsi.getCollection());
//        wfi.setMultipleFiles(wsi.hasMultipleFiles());
//        wfi.setMultipleTitles(wsi.hasMultipleTitles());
//        wfi.setPublishedBefore(wsi.isPublishedBefore());
//        wfi.update();
//        removeUserItemPolicies(context, myitem, myitem.getSubmitter());
//        context.turnOffAuthorisationSystem();
//        SubmissionStep firstStep = wf.getFirstStep();
//        if(firstStep.isValidStep(context, wfi)){
//             activateFirstStep(context, wf, firstStep, wfi);
//        } else {
//            //Get our next step, if none is found, archive our item
//            firstStep = wf.getNextStep(context, wfi, firstStep, ActionResult.OUTCOME_COMPLETE);
//            if(firstStep == null){
//                archive(context, wfi);
//            }else{
//                activateFirstStep(context, wf, firstStep, wfi);
//            }
//        }
//        // remove the WorkspaceItem
//        wsi.deleteWrapper();
//        context.restoreAuthSystemState();
//    }

//    private static void activateFirstStep(Context context, SubmissionProcess wf, SubmissionStep firstStep, WorkspaceService wfi) throws AuthorizeException, IOException, SQLException, WorkflowException, WorkflowConfigurationException{
//        //TODO: check if this step is valid !
//        SubmissionActionConfig firstActionConfig = firstStep.getUserSelectionMethod();
//        firstActionConfig.getProcessingAction().activate(context, wfi);
//        log.info(LogManager.getHeader(context, "start_workflow", firstActionConfig.getProcessingAction() + " workflow_item_id="
//                + wfi.getID() + "item_id=" + wfi.getItem().getID() + "collection_id="
//                + wfi.getCollection().getID()));
//
//        // record the start of the process w/provenance message
//        recordStart(wfi.getItem(), firstActionConfig.getProcessingAction());
//
//        //If we don't have a UI activate it
//        if(!firstActionConfig.hasUserInterface()){
//            ActionResult outcome = firstActionConfig.getProcessingAction().execute(context, wfi, firstStep, null);
//            //TODO: false??
//            processOutcome(context, null, wf, firstStep, firstActionConfig, outcome, wfi, false);
//        }
//    }

    /*
     * Executes an action and returns the next.
     */
    public static SubmissionActionConfig doState(Context c, EPerson user, HttpServletRequest request, String workspaceItemId, SubmissionProcess process, SubmissionActionConfig currentActionConfig) throws Exception,SQLException, AuthorizeException, IOException, MessagingException//, WorkflowConfigurationException, WorkflowException
    {
        try {
            WorkspaceService wi = WorkspaceService.find(c, Integer.valueOf(workspaceItemId.substring(1)));
            SubmissionInfo subInfo = SubmissionInfo.load(request,wi);
            SubmissionStep currentStep = currentActionConfig.getStep();
            //TODO: maybe we need a check to see whether a claimaction still exists for the action the user wants to execute?
            if(currentActionConfig.getProcessingAction().isAuthorized(c, request, wi)){
                ActionResult outcome = currentActionConfig.getProcessingAction().execute(c,request,subInfo);
                return processOutcome(c, user,request, process, currentStep, currentActionConfig, outcome, subInfo, false);
            }else{
                throw new AuthorizeException("You are not allowed to to perform this task.");
            }
        } catch (Exception e) {
            log.error(LogManager.getHeader(c, "error while executing state", "process:  " + process.getID() + " action: " + currentActionConfig.getId() + " workspaceItemId: " + workspaceItemId), e);
            //WorkflowUtils.sendAlert(request, e);
            throw e;
        }
    }


    public static SubmissionActionConfig processOutcome(Context c, EPerson user,HttpServletRequest request, SubmissionProcess process, SubmissionStep currentStep, SubmissionActionConfig currentActionConfig, ActionResult currentOutcome,SubmissionInfo subInfo, boolean enteredNewStep) throws IOException,  AuthorizeException, SQLException,ServletException//,WorkflowConfigurationException, WorkflowException
    {
        if(currentOutcome.getType() == ActionResult.TYPE.TYPE_PAGE || currentOutcome.getType() == ActionResult.TYPE.TYPE_ERROR){
            //Our outcome is a page or an error, so return our current action
            return currentActionConfig;
        }else
        if(currentOutcome.getType() == ActionResult.TYPE.TYPE_CANCEL || currentOutcome.getType() == ActionResult.TYPE.TYPE_SUBMISSION_PAGE){
            //We either pressed the cancel button or got an order to return to the submission page, so don't return an action
            //By not returning an action we ensure ourselfs that we go back to the submission page
            return null;
        }else
        if (currentOutcome.getType() == ActionResult.TYPE.TYPE_OUTCOME) {
            //We have completed our action search & retrieve the next action
            SubmissionActionConfig nextActionConfig = null;
            if(currentOutcome.getResult() == ActionResult.OUTCOME_COMPLETE){
                nextActionConfig = currentStep.getNextAction(c,currentActionConfig);
            }

            if (nextActionConfig != null) {
                //nextActionConfig.getProcessingAction().activate(c, wfi);
                if (nextActionConfig.hasUserInterface() && !enteredNewStep) {
                    //TODO: if user is null, then throw a decent exception !
                    //createOwnedTask(c, wfi, currentStep, nextActionConfig, user);
                    return nextActionConfig;
                } else if( nextActionConfig.hasUserInterface() && enteredNewStep){
                    //We have entered a new step and have encountered a UI, return null since the current user doesn't have anything to do with this
                    return null;
                } else {
                    ActionResult newOutcome = nextActionConfig.getProcessingAction().execute(c, request,subInfo);
                    return processOutcome(c, user,request, process, currentStep, nextActionConfig, newOutcome, subInfo, enteredNewStep);
                }
            }else{

                //First add it to our list of finished users, since no more actions remain
//                WorkflowRequirementsManager.addFinishedUser(c, wfi, user);
                c.turnOffAuthorisationSystem();
                //Check if our requirements have been met
                if(true){
                    //Clear all the metadata that might be saved by this step
                    //WorkflowRequirementsManager.clearStepMetadata(wfi);
                    //Remove all the tasks
                    //SubmissionManager.deleteAllTasks(c, wfi);


                    SubmissionStep nextStep = process.getNextStep(c, currentStep, currentOutcome.getResult());

                    if(nextStep!=null){
                        //TODO: is generate tasks nog nodig of kan dit mee in activate?
                        //TODO: vorige step zou meegegeven moeten worden om evt rollen te kunnen overnemen
                        nextActionConfig = nextStep.getUserSelectionMethod();
                        //nextActionConfig.getProcessingAction().activate(c, wfi);
        //                nextActionConfig.getProcessingAction().generateTasks();

                        //TODO VERTALEN Deze kunnen afhangen van de step (rol, min, max, ...). Evt verantwoordelijkheid bij userassignmentaction leggen
                        if (nextActionConfig.hasUserInterface()) {
                            //Since a new step has been started, stop executing actions once one with a user interface is present.
                            c.restoreAuthSystemState();
                            return null;
                        } else {
                            ActionResult newOutcome = nextActionConfig.getProcessingAction().execute(c, request,subInfo);
                            c.restoreAuthSystemState();
                            return processOutcome(c, user,request, process, nextStep, nextActionConfig, newOutcome, subInfo, true);
                        }
                    }else{
                        if(currentOutcome.getResult() != ActionResult.OUTCOME_COMPLETE){
                            c.restoreAuthSystemState();
                            //throw new WorkflowException("No alternate step was found for outcome: " + currentOutcome.getResult());
                        }
                        //archive(c, wfi);
                        c.restoreAuthSystemState();
                        return null;
                    }
                }else{
                    //We are done with our actions so go to the submissions page but remove action ClaimedAction first
//                    ClaimedTask task = ClaimedTask.findByWorkflowIdAndEPerson(c, wfi.getID(), user.getID());
//                    deleteClaimedTask(c, wfi, task);
                    c.restoreAuthSystemState();
                    return null;
                }
            }

        }
        //TODO: log & go back to submission, We should not come here
        //TODO: remove assertion - can be used for testing (will throw assertionexception)
        assert false;
        return null;
    }


    public static String getEPersonName(EPerson e) throws SQLException
    {
        String submitter = e.getFullName();

        submitter = submitter + "(" + e.getEmail() + ")";

        return submitter;
    }

    // Create process start provenance message
    private static void recordStart(Item myitem, SubmissionAction action)
            throws SQLException, IOException, AuthorizeException
    {
        // get date
        DCDate now = DCDate.getCurrent();

        // Create provenance description
        String provmessage = "";

        if (myitem.getSubmitter() != null)
        {
            provmessage = "Submitted by " + myitem.getSubmitter().getFullName()
                    + " (" + myitem.getSubmitter().getEmail() + ") on "
                    + now.toString() + " process start=" + action.getProvenanceStartId() + "\n";
        }
        else
        // null submitter
        {
            provmessage = "Submitted by unknown (probably automated) on"
                    + now.toString() + " process start=" + action.getProvenanceStartId() + "\n";
        }

        // add sizes and checksums of bitstreams
        provmessage += InstallItem.getBitstreamProvenanceMessage(myitem);

        // Add message to the DC
        myitem.addMetadata(MetadataSchema.DC_SCHEMA, "description", "provenance", "en", provmessage);
        myitem.update();
    }

    public static String getMyDSpaceLink() {
        return ConfigurationManager.getProperty("dspace.url") + "/mydspace";
    }
}
