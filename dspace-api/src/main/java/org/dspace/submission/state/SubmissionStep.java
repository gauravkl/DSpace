package org.dspace.submission.state;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.WorkspaceService;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;
import org.dspace.submission.Role;
import org.dspace.submission.SubmissionProcessFactory;
import org.dspace.submission.state.actions.SubmissionAction;
import org.dspace.submission.state.actions.UserSelectionActionConfig;
import org.dspace.submission.state.actions.SubmissionActionConfig;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: gaurav
 * Date: 6/13/11
 * Time: 12:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class SubmissionStep {

    /** log4j logger */
    private static Logger log = Logger.getLogger(SubmissionStep.class);
    private UserSelectionActionConfig userSelectionMethod;
    private HashMap<String, SubmissionActionConfig> actionConfigsMap;
    private List<String> actionConfigsList;
    private Map<Integer, Integer> outcomes = new HashMap<Integer, Integer>();
    private int step_id;
    private Role role;
    private SubmissionProcess submissionprocess;
    //TODO:
    private String name ;
    private int requiredUsers;
    private TableRow row;
    // cache of process by ID (Integer)
    private static HashMap id2step = null;

    public SubmissionStep(String name , Role role, UserSelectionActionConfig userSelectionMethod, List<String> actionConfigsList, Map<Integer, Integer> outcomes){
        this.actionConfigsMap = new HashMap<String, SubmissionActionConfig>();
        this.outcomes = outcomes;
        this.userSelectionMethod = userSelectionMethod;
        this.role = role;
        this.actionConfigsList = actionConfigsList;
        //userSelectionMethod.setStep(this);
        this.name = name;
    }

      /**
	     * Constructor for loading the submissionstep from the database.
	     *
	     * @param row table row object from which to populate this submissionstep.
	     */
    public SubmissionStep(TableRow row)
    {
        if (row != null)
        {
          this.step_id = row.getIntColumn("step_id");
          this.name= row.getStringColumn("name");
          this.row = row;
        }
    }

    public SubmissionStep(){
        
    }
    
    public SubmissionActionConfig getActionConfig(String actionID) {
        if(actionConfigsMap.get(actionID)!=null){
            return actionConfigsMap.get(actionID);
        }
        else{
            SubmissionActionConfig action = SubmissionProcessFactory.createSubmissionActionConfig(actionID);
            action.setStep(this);
            actionConfigsMap.put(actionID, action);
            return action;
        }
    }

    /**
     * Boolean that returns whether or not the actions in this step have a ui
     * @return a boolean
     */
    public boolean hasUI(){
        for (String actionConfigId : actionConfigsList) {
            SubmissionActionConfig actionConfig = getActionConfig(actionConfigId);
            if (actionConfig.hasUserInterface()) {
                return true;
            }
        }
        return false;
    }

    public int getNextStepID(int outcome) //throws WorkflowException, IOException, WorkflowConfigurationException, SQLException
    {
        return outcomes.get(outcome);
    }


    public boolean isValidStep(Context context, WorkspaceService wfi) throws //WorkflowConfigurationException,
            SQLException
    {
        //Check if our next step has a UI, if not then the step is valid, no need for a group
        if(getUserSelectionMethod() == null || getUserSelectionMethod().getProcessingAction() == null){
            return false;
        }else{
            return getUserSelectionMethod().getProcessingAction().isValidUserSelection(context, wfi, hasUI());
        }
    }

    public UserSelectionActionConfig getUserSelectionMethod() {
            return userSelectionMethod;
    }

    public SubmissionActionConfig getNextAction(SubmissionActionConfig currentAction) {
        int index = actionConfigsList.indexOf(currentAction.getId());
        if(index < actionConfigsList.size()-1){
            return getActionConfig(actionConfigsList.get(index+1));
        }else{
            return null;
        }
    }

    public int getId() {
        return step_id;
    }

    public SubmissionProcess getWorkflow() {
        return submissionprocess;
    }


    /**
     * Check if enough users have finished this step for it to continue
     * @param wfi the workspace item to check
     * @return if enough users have finished this task
     */
//    public boolean isFinished(WorkspaceService wfi){
//        //return WorkflowRequirementsManager.getNumberOfFinishedUsers(wfi) == requiredUsers;
//    }

    public int getRequiredUsers(){
        return requiredUsers;
    }

    public String getName() {
        return name;
    }
    
    public Role getRole() {
        return role;
    }

//    public boolean skipStep(){
//    }


	   

	    /**
	     * Get the submissionstep's id.
	     *
	     * @return step_id
	     */
	    public int getSubmissionStepID()
	    {
	    return step_id;
	    }

	    /**
	     * Set the inputform name.
	     *
	     * @param name  name
	     */
	    public void setName(String name)
	    {
	        this.name = name;
	    }


	    /**
	     * Creates a new inputform in the database, out of this object.
	     *
	     * @param context
	     *            DSpace context object
	     * @throws SQLException
	     * @throws AuthorizeException
	     * @throws NonUniqueMetadataException
	     */
	    public void create(Context context) throws SQLException,
                AuthorizeException
	    {
	        // Check authorisation: Only admins may create new process
	        if (!AuthorizeManager.isAdmin(context))
	        {
	            throw new AuthorizeException(
	                    "Only administrators may modify the inputform registry");
	        }

	        // Create a table row and update it with the values
	        row = DatabaseManager.create(context, "submissionstep");
	        row.setColumn("name", name);
           // row.setColumn("next_step_id",getNextStepID(0));
            //row.setColumn("role_id",role.getId());
            //row.setColumn("selection__method_id",userSelectionMethod.getId());
	        DatabaseManager.update(context, row);

	        // Remember the new row number
	        this.step_id = row.getIntColumn("step_id");

	        log
	                .info(LogManager.getHeader(context, "create_submissionstep",
                            "step_id="
                                    + row.getIntColumn("step_id")));
	    }


	    /**
	     * Update the inputform in the database.
	     *
	     * @param context DSpace context
	     * @throws SQLException
	     * @throws AuthorizeException
	     *
	     */
	    public void update(Context context) throws SQLException,
	            AuthorizeException
	    {
	        // Check authorisation: Only admins may update the submissionstep registry
	        if (!AuthorizeManager.isAdmin(context))
	        {
	            throw new AuthorizeException(
	                    "Only administrators may modify the submissionstep registry");
	        }


	        row.setColumn("name", getName());
	        DatabaseManager.update(context, row);

	        log.info(LogManager.getHeader(context, "update_submissionstep",
	                "step_id=" + getSubmissionStepID() + "name="
	                        + getName()));
	    }

	    /**
	     * Delete the submissionstep.
	     *
	     * @param context DSpace context
	     * @throws SQLException
	     * @throws AuthorizeException
	     */
	    public void delete(Context context) throws SQLException, AuthorizeException
	    {
	        // Check authorisation: Only admins may create DC types
	        if (!AuthorizeManager.isAdmin(context))
	        {
	            throw new AuthorizeException(
	                    "Only administrators may modify the submissionstep registry");
	        }

	        log.info(LogManager.getHeader(context, "delete_submissionstep",
	                "step_id=" + getSubmissionStepID()));

	        DatabaseManager.delete(context, row);
	    }

	    /**
	     * Return all submissionsteps.
	     *
	     * @param context DSpace context
	     * @return array of submissionactions
	     * @throws SQLException
	     */
	    public static SubmissionStep[] findAll(Context context) throws SQLException
	    {
	        List submissionsteps = new ArrayList();

	        // Get all the SubmissionStep rows
	        TableRowIterator tri = DatabaseManager.queryTable(context, "SubmissionStep",
	                        "SELECT * FROM SubmissionStep ORDER BY step_id");

	        try
	        {

	            while (tri.hasNext())
	            {
	                submissionsteps.add(new SubmissionStep(tri.next()));
	            }
	        }
	        finally
	        {
	            // close the TableRowIterator to free up resources
	            if (tri != null)
	                tri.close();
	        }

	        // Convert list into an array
	        SubmissionStep[] typeArray = new SubmissionStep[submissionsteps.size()];
	        return (SubmissionStep[]) submissionsteps.toArray(typeArray);
	    }



	    /**
	     * Get the submissionstep corresponding with this numeric ID.
	     * The ID is a database key internal to DSpace.
	     *
	     * @param context
	     *            context, in case we need to read it in from DB
	     * @param id
	     *            the submissionstep ID
	     * @return the submissionstep object
	     * @throws SQLException
	     */
	    public static SubmissionStep find(Context context, int id)
	            throws SQLException
	    {   decache();
	        initCache(context);
	        Integer iid = new Integer(id);

	        // sanity check
	        if (!id2step.containsKey(iid))
	            return null;

	        return (SubmissionStep) id2step.get(iid);
	    }

	/**
	     * Get the submissionstep object corresponding to this name.
	     *
	     * @param context DSpace context
	     * @param name
	     * @return input-form object or null if none found.
	     * @throws SQLException
	     */
	    public static SubmissionStep findByName(Context context,
	            String name) throws SQLException
	    {
	        // Grab rows from DB
	        TableRowIterator tri = DatabaseManager.queryTable(context,"submissionstep",
	                "SELECT * FROM submissionstep WHERE name= ? ",
	                name);

	        TableRow row = null;
	        try
	        {
	            if (tri.hasNext())
	            {
	                row = tri.next();
	            }
	        }
	        finally
	        {
	            // close the TableRowIterator to free up resources
	            if (tri != null)
	                tri.close();
	        }

	        if (row == null)
	        {
	            return null;
	        }
	        else
	        {
	            return new SubmissionStep(row);
	        }
	    }

	    public static void step2submissionprocess(Context context,int processID,int step_id) throws SQLException,
	    AuthorizeException
	    {
	        TableRow mappingRow = DatabaseManager.create(context,
	        "step2submissionprocess");
	        mappingRow.setColumn("process_id", processID);
	        mappingRow.setColumn("step_id", step_id);
	        DatabaseManager.update(context, mappingRow);
	    }
	 // invalidate the cache e.g. after something modifies DB state.
	    private static void decache()
	    {
	        id2step = null;

	    }

	    // load caches if necessary
	    private static void initCache(Context context) throws SQLException
	    {
	        if (id2step != null )
	            return;

	        synchronized (SubmissionStep.class)
	        {
	            if (id2step == null )
	            {
	                log.info("Loading form cache for fast finds");
	                HashMap new_id2step = new HashMap();

	                TableRowIterator tri = DatabaseManager.queryTable(context,"SubmissionStep",
	                        "SELECT * from SubmissionStep");

	                try
	                {
	                    while (tri.hasNext())
	                    {
	                        TableRow row = tri.next();

	                        SubmissionStep form = new SubmissionStep(row);
	                        new_id2step.put(new Integer(form.step_id), form);

	                    }
	                }
	                finally
	                {
	                    // close the TableRowIterator to free up resources
	                    if (tri != null)
	                        tri.close();
	                }

	                id2step = new_id2step;

	            }
	        }
	    }
	  /**
	     * Return all actions that are found in a given submissionstep.
	     *
	     * @param context dspace context
	     * @param step_id submissionstep by db ID
	     * @return array of input form fields
	     * @throws SQLException
	     */

//	    public static SubmissionStepField[] findAllInSubmissionStep(Context context,int step_id)
//	            throws SQLException
//	    {
//	        List fields = new ArrayList();
//
//	       SubmissionStep form=SubmissionStep.find(context,step_id);
//	        try
//	        {
//	           SubmissionStepPage[] pages=form.getPages(context,step_id);
//			log.info("number of pages"+pages.length);
//			for(int i=0;i<pages.length;i++)
//			{
//			System.out.println("pageID"+pages[i].getSubmissionStepPageID());
//			SubmissionStepField[] pagefields=pages[i].getFields(context,pages[i].getSubmissionStepPageID());
//			for(int j=0;j<pagefields.length;j++)
//			fields.add(pagefields[j]);
//			log.info("pgnum"+i+"number of fields"+pagefields.length);
//			}
//	        }
//	        catch(SQLException e)
//			{}
//
//	        // Convert list into an array
//	        SubmissionStepField[] typeArray = new SubmissionStepField[fields.size()];
//	        return (SubmissionStepField[]) fields.toArray(typeArray);
//	    }

	/**
	     * Return all actions that are found in a given submissionstep.
	     *
	     * @param context dspace context
	     * @param step_id submissionstep by db ID
	     * @return array of input form fields
	     * @throws SQLException
	     */

	    public static SubmissionAction[] getActions(Context context,int step_id)
	            throws SQLException
	    {
	        List actions = new ArrayList();

	        // Get all the action2submissionstep rows
	        TableRowIterator tri = DatabaseManager.queryTable(context,"action2submissionstep",
	                "SELECT * FROM action2submissionstep WHERE step_id= ? " +
	                " ORDER BY place",step_id);

	        try
	        {
	            // get Page objects
	            while (tri.hasNext())
	            {   int id=tri.next().getIntColumn("action_id");
	                //System.out.println("pageID"+id);
	                actions.add(SubmissionAction.find(context,id));
	            }
			log.info("length"+actions.size());
	        }
	        finally
	        {
	            // close the TableRowIterator to free up resources
	            if (tri != null)
	                tri.close();
	        }

	        // Convert list into an array
	        SubmissionAction[] typeArray = new SubmissionAction[actions.size()];
	        return (SubmissionAction[]) actions.toArray(typeArray);
	    }

		public SubmissionProcess getProcess(Context context)
	            throws SQLException
	     {
	        SubmissionProcess process=null ;

	        // Get all the step2submissionprocess rows
	        TableRowIterator tri = DatabaseManager.queryTable(context,"step2submissionprocess",
	                "SELECT * FROM step2submissionprocess WHERE step_id= ? ",step_id);

	        try
	        {
	            // get Collection objects
	            while (tri.hasNext())
	            {   int id=tri.next().getIntColumn("process_id");
	                process=SubmissionProcess.find(context,id);
			    }

		    }
	        finally
	        {
	            // close the TableRowIterator to free up resources
	            if (tri != null)
	                tri.close();
	        }

	        // Convert list into an array
	        return  process;
	    }

	public static void removeAction(Context context,int step_id,int actionID) throws SQLException,
	    AuthorizeException
	    {
	        TableRowIterator tri = DatabaseManager.queryTable(context,"action2submissionstep",
	                "SELECT * FROM action2submissionstep WHERE step_id= ? " +
	                " AND action_id=?",step_id,actionID);
		 try
	        {
	            // get Page objects
	            while (tri.hasNext())
	            {  DatabaseManager.delete(context,tri.next());
	    	 }
		  }
	        finally
	        {
	            // close the TableRowIterator to free up resources
	            if (tri != null)
	                tri.close();
	        }
	    }
    public static void addAction(Context context,int stepID,int actionID) throws SQLException,
           AuthorizeException
           {
           SubmissionAction.action2submissionstep(context,stepID,actionID);
           }

     public static void addOutcome(Context context,int step_id,int outcome) throws SQLException,
           AuthorizeException
           {
           TableRow mappingRow = DatabaseManager.create(context,
	        "step2outcome");
	       mappingRow.setColumn("step_id", step_id);
            mappingRow.setColumn("outcome_id", outcome);
	        DatabaseManager.update(context, mappingRow);
           }
     private void setStepActionConfigs(Context context) throws SQLException{
        List<String> actionConfigIDs = new ArrayList<String>();
        SubmissionAction[] actions = getActions(context,step_id);
        for(int i = 0; i < actions.length; i++){
            actionConfigIDs.add(actions[i].getBean_id());
        }
        this.actionConfigsList = actionConfigIDs;
    }
}
