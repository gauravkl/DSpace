package org.dspace.submission.state.actions;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.WorkspaceItem;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;
import org.dspace.submission.RoleMembers;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: bram
 * Date: 2-aug-2010
 * Time: 17:35:54
 * To change this template use File | Settings | File Templates.
 */
public class SubmissionAction {
    
    /** log4j logger */
    private static Logger log = Logger.getLogger(SubmissionAction.class);
    private WorkflowActionConfig parent;
    private TableRow row;
    
    private String bean_id ;
    private int action_id;
    private static String ERROR_FIELDS_ATTRIBUTE = "dspace.workflow.error_fields";
    
    // cache of process by ID (Integer)
    private static HashMap id2action = null;

    public SubmissionAction(TableRow row)
    {
        if (row != null)
        {
          this.action_id = row.getIntColumn("action_id");
          this.bean_id= row.getStringColumn("bean_id");
          this.row = row;
        }
    }
    public SubmissionAction()
    {

    }

//    public void activate(Context c, WorkspaceItem wf) throws SQLException, IOException, AuthorizeException;//, WorkflowException;
//     {
//     }

   // public ActionResult execute(Context c, WorkspaceItem wfi, SubmissionStep step, HttpServletRequest request) throws SQLException, AuthorizeException, IOException;// WorkflowException;

    public String getBean_id() {
           return bean_id;
       }



           /**
            * Get the submissionaction's id.
            *
            * @return action_id
            */
           public int getId()
           {
           return action_id;
           }

           /**
            * Set the inputform name.
            *
            * @param name  name
            */
           public void setBean_id(String bean_id)
           {
               this.bean_id = bean_id;
           }

    public WorkflowActionConfig getParent() {
        return parent;
    }

    public void setParent(WorkflowActionConfig parent) {
        this.parent = parent;
    }

    public String getProvenanceStartId(){
        return "Step: " + getParent().getStep().getId() + " - action:" + getParent().getId();
    }

    public void alertUsersOnActivation(Context c, WorkspaceItem wfi, RoleMembers members) throws SQLException, IOException {

    }

    //public boolean isAuthorized(Context context, HttpServletRequest request, WorkspaceItem wfi) throws SQLException, AuthorizeException, IOException;//, WorkflowConfigurationException;


    /**
     * Sets th list of all UI fields which had errors that occurred during the
     * step processing. This list is for usage in generating the appropriate
     * error message(s) in the UI.
     *
     * @param request
     *            current servlet request object
     * @param errorFields
     *            List of all fields (as Strings) which had errors
     */
    private static void setErrorFields(HttpServletRequest request, List errorFields)
    {
        if(errorFields==null)
            request.removeAttribute(ERROR_FIELDS_ATTRIBUTE);
        else
            request.setAttribute(ERROR_FIELDS_ATTRIBUTE, errorFields);
    }

    /**
     * Return a list of all UI fields which had errors that occurred during the
     * workflow processing. This list is for usage in generating the appropriate
     * error message(s) in the UI.
     *
     * @param request
     *            current servlet request object
     * @return List of error fields (as Strings)
     */
    public static List getErrorFields(HttpServletRequest request)
    {
        List result = new ArrayList();
        if(request.getAttribute(ERROR_FIELDS_ATTRIBUTE) != null)
            result = (List) request.getAttribute(ERROR_FIELDS_ATTRIBUTE);
        return result;
    }

    /**
     * Add a single UI field to the list of all error fields (which can
     * later be retrieved using getErrorFields())
     *
     * @param request
     *              current servlet request object
     * @param fieldName
     *            the name of the field which had an error
     */
    protected static void addErrorField(HttpServletRequest request, String fieldName)
    {
        //get current list
        List errorFields = getErrorFields(request);

        if (errorFields == null)
        {
            errorFields = new ArrayList();
        }

        //add this field
        errorFields.add(fieldName);

        //save updated list
        setErrorFields(request, errorFields);
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
	        row = DatabaseManager.create(context, "submissionaction");
	        row.setColumn("bean_id", bean_id);           
	        DatabaseManager.update(context, row);

	        // Remember the new row number
	        this.action_id = row.getIntColumn("action_id");

	        log
	                .info(LogManager.getHeader(context, "create_submissionstep",
                            "action_id="
                                    + row.getIntColumn("action_id")));
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
	        // Check authorisation: Only admins may update the submissionaction registry
	        if (!AuthorizeManager.isAdmin(context))
	        {
	            throw new AuthorizeException(
	                    "Only administrators may modify the submissionaction registry");
	        }


	        row.setColumn("bean_id", bean_id);
	        DatabaseManager.update(context, row);

	        log.info(LogManager.getHeader(context, "update_submissionstep",
	                "action_id=" + getId() + "name="
	                        + getBean_id()));
	    }

	    /**
	     * Delete the submissionaction.
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
	                    "Only administrators may modify the submissionaction registry");
	        }

	        log.info(LogManager.getHeader(context, "delete_submissionstep",
	                "action_id=" + getId()));

	        DatabaseManager.delete(context, row);
	    }

	    /**
	     * Return all submissionactions.
	     *
	     * @param context DSpace context
	     * @return array of submissionactions
	     * @throws SQLException
	     */
	    public static SubmissionAction[] findAll(Context context) throws SQLException
	    {
	        List submissionactions = new ArrayList();

	        // Get all the SubmissionAction rows
	        TableRowIterator tri = DatabaseManager.queryTable(context, "SubmissionAction",
	                        "SELECT * FROM SubmissionAction ORDER BY action_id");

	        try
	        {

	            while (tri.hasNext())
	            {
	                submissionactions.add(new SubmissionAction(tri.next()));
	            }
	        }
	        finally
	        {
	            // close the TableRowIterator to free up resources
	            if (tri != null)
	                tri.close();
	        }

	        // Convert list into an array
	        SubmissionAction[] typeArray = new SubmissionAction[submissionactions.size()];
	        return (SubmissionAction[]) submissionactions.toArray(typeArray);
	    }

     public static SubmissionAction findByBean(Context context,
	            String bean_id) throws SQLException
	    {
	        // Grab rows from DB
	        TableRowIterator tri = DatabaseManager.queryTable(context,"submissionaction",
	                "SELECT * FROM submissionaction WHERE bean_id= ? ",
	                bean_id);

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
	            return new SubmissionAction(row);
	        }
	    }
      public static SubmissionAction find(Context context, int id)
	            throws SQLException
	    {   decache();
	        initCache(context);
	        Integer iid = new Integer(id);

	        // sanity check
	        if (!id2action.containsKey(iid))
	            return null;

	        return (SubmissionAction) id2action.get(iid);
	    }
	   
	 // invalidate the cache e.g. after something modifies DB state.
	    private static void decache()
	    {
	        id2action = null;

	    }

	    // load caches if necessary
	    private static void initCache(Context context) throws SQLException
	    {
	        if (id2action != null )
	            return;

	        synchronized (SubmissionAction.class)
	        {
	            if (id2action == null )
	            {
	                log.info("Loading form cache for fast finds");
	                HashMap new_id2action = new HashMap();

	                TableRowIterator tri = DatabaseManager.queryTable(context,"SubmissionAction",
	                        "SELECT * from SubmissionAction");

	                try
	                {
	                    while (tri.hasNext())
	                    {
	                        TableRow row = tri.next();

	                        SubmissionAction action = new SubmissionAction(row);
	                        new_id2action.put(new Integer(action.action_id), action);

	                    }
	                }
	                finally
	                {
	                    // close the TableRowIterator to free up resources
	                    if (tri != null)
	                        tri.close();
	                }

	                id2action = new_id2action;

	            }
	        }
	    }
     public static void action2submissionstep(Context context,int step_id,int action_id) throws SQLException,
	    AuthorizeException
	    {
	        TableRow mappingRow = DatabaseManager.create(context,
	        "action2submissionstep");
	       mappingRow.setColumn("step_id", step_id);
            mappingRow.setColumn("action_id", action_id);
	        DatabaseManager.update(context, mappingRow);
	    }
}
