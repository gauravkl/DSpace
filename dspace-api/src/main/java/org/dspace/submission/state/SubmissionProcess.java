package org.dspace.submission.state;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.Collection;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;
import org.dspace.submission.Role;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: gaurav
 * To change this template use File | Settings | File Templates.
 */
public class SubmissionProcess {

    /** log4j logger */
    private static Logger log = Logger.getLogger(SubmissionProcess.class);

	private int process_id;
    private String name;
	private SubmissionStep firstStep;
	private HashMap<String, SubmissionStep> steps;
	private HashMap<String, Role> roles;
    private TableRow row;

     // cache of process by ID (Integer)
    private static HashMap id2process = null;


    public SubmissionProcess(int processID,String name, HashMap<String, Role> roles) {
		this.process_id = processID;
        this.name = name;
		this.roles = roles;
		this.steps = new HashMap<String, SubmissionStep>();
	}
	/**
		 * Constructor for loading the submissionprocess from the database.
		 *
		 * @param row table row object from which to populate this submissionprocess.
		 */
	public SubmissionProcess(TableRow row){
		if (row != null)
		{
		  this.process_id = row.getIntColumn("process_id");
          this.name = row.getStringColumn("name");
		  this.row = row;
		}
	}

    public SubmissionProcess(){
        
    }

	public SubmissionStep getFirstStep(Context context) throws SQLException{
		return getSteps(context,process_id)[0];
	}

	public int getID(){
		return process_id;
	}

    public String getName(){
        return name;
    }

     public void setName(String name){
	        this.name = name;
     }

	/*
	 * Return a step with a given id
	 */
	public SubmissionStep getStep(Context context,int stepID) throws //WorkflowConfigurationException,
	 IOException,SQLException {
//		if(steps.get(stepID)!=null){
//			return steps.get(stepID);
//		}else{
			//SubmissionStep step = WorkflowFactory.createStep(this, stepID);
//			if(step== null){
//				throw new WorkflowConfigurationException("SubmissionStep definition not found for: "+stepID);
//			}
			//steps.put(stepID, step);
			//return step;
//            return null;
//		}
        return SubmissionStep.find(context,stepID);
	}

	public SubmissionStep getNextStep(Context context, SubmissionStep currentStep, int outcome) throws IOException,AuthorizeException,// WorkflowConfigurationException, WorkflowException,
            SQLException {
		Integer nextStepID = currentStep.getNextStepID(context,outcome);
		if(nextStepID != -1){
			SubmissionStep nextStep = getStep(context,nextStepID);
//			if(nextStep == null)
//				throw new WorkflowException("Error while processing outcome, the following action was undefined: " + nextStepID);
				return nextStep;
		}else{
			//No next step, archive it
			return null;
		}
	}

	public void setFirstStep(SubmissionStep firstStep) {
		this.firstStep = firstStep;
	}

	public HashMap<String, Role> getRoles() {
		return roles;
	}

/**
		 * Creates a new submission-process in the database, out of this object.
		 *
		 * @param context
		 *            DSpace context object
		 * @throws SQLException
		 * @throws AuthorizeException

		 */
		public void create(Context context) throws SQLException,
                AuthorizeException
		{
			// Check authorisation: Only admins may create new process
			if (!AuthorizeManager.isAdmin(context))
			{
				throw new AuthorizeException(
						"Only administrators may modify the submission registry");
			}

			// Create a table row and update it with the values
			row = DatabaseManager.create(context, "submissionprocess");
            if(firstStep!=null)
                row.setColumn("start_step_id",firstStep.getId());
            row.setColumn("name",name);
			DatabaseManager.update(context, row);

			// Remember the new row number
			this.process_id = row.getIntColumn("process_id");

			log
					.info(LogManager.getHeader(context, "create_submissionprocess",
                            "process_id="
                                    + row.getIntColumn("process_id")));
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
               // Check authorisation: Only admins may update the submissionprocess registry
               if (!AuthorizeManager.isAdmin(context))
               {
                   throw new AuthorizeException(
                           "Only administrators may modify the submissionprocess registry");
               }

	           row.setColumn("name", name);
               row.setColumn("start_step_id",firstStep.getId());
               DatabaseManager.update(context, row);

               log.info(LogManager.getHeader(context, "update_submissionprocess",
                       "process_id=" + getID() ));
           }

           /**
            * Delete the submissionprocess.
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
                           "Only administrators may modify the submissionprocess registry");
               }

               log.info(LogManager.getHeader(context, "delete_submissionprocess",
                       "process_id=" + getID()));

               DatabaseManager.delete(context, row);
           }

           /**
            * Return all submissionprocesss.
            *
            * @param context DSpace context
            * @return array of submissionprocesses
            * @throws SQLException
            */
           public static SubmissionProcess[] findAll(Context context) throws SQLException
           {
               List submissionprocesses = new ArrayList();

               // Get all the SubmissionProcess rows
               TableRowIterator tri = DatabaseManager.queryTable(context, "SubmissionProcess",
                               "SELECT * FROM SubmissionProcess ORDER BY process_id");

               try
               {
	            
                   while (tri.hasNext())
                   {
                       submissionprocesses.add(new SubmissionProcess(tri.next()));
                   }
               }
               finally
               {
                   // close the TableRowIterator to free up resources
                   if (tri != null)
                       tri.close();
               }

               // Convert list into an array
               SubmissionProcess[] typeArray = new SubmissionProcess[submissionprocesses.size()];
               return (SubmissionProcess[]) submissionprocesses.toArray(typeArray);
           }

	  

           /**
            * Get the submissionprocess corresponding with this numeric ID.
            * The ID is a database key internal to DSpace.
            *
            * @param context
            *            context, in case we need to read it in from DB
            * @param id
            *            the submissionprocess ID
            * @return the submissionprocess object
            * @throws SQLException
            */
           public static SubmissionProcess find(Context context, int id)
                   throws SQLException
           {   decache();
               initCache(context);
               Integer iid = new Integer(id);

               // sanity check
               if (!id2process.containsKey(iid))
                   return null;

               return (SubmissionProcess) id2process.get(iid);
           }

       /**
            * Get the submissionprocess object corresponding to this name.
            *
            * @param context DSpace context
            * @param name
            * @return input-form object or null if none found.
            * @throws SQLException
            */
           public static SubmissionProcess findByName(Context context,
                   String name) throws SQLException
           {
               // Grab rows from DB
               TableRowIterator tri = DatabaseManager.queryTable(context,"submissionprocess",
                       "SELECT * FROM submissionprocess WHERE name= ? ", 
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
                   return new SubmissionProcess(row);
               }
           }

           public static void process2collection(Context context,int collectionID,int processID) throws SQLException,
           AuthorizeException
           {
               TableRow mappingRow = DatabaseManager.create(context,
               "collection2submissionprocess");
               mappingRow.setColumn("collection_id", collectionID);
               mappingRow.setColumn("process_id", processID);
               DatabaseManager.update(context, mappingRow);
	    
           }
       // invalidate the cache e.g. after something modifies DB state.
           private static void decache()
           {
               id2process = null;

           }

           //load caches if necessary
           private static void initCache(Context context) throws SQLException
           {
               if (id2process != null )
                   return;

               synchronized (SubmissionProcess.class)
               {
                   if (id2process == null )
                   {
                       log.info("Loading form cache for fast finds");
                       HashMap new_id2process = new HashMap();

                       TableRowIterator tri = DatabaseManager.queryTable(context,"SubmissionProcess",
                               "SELECT * from SubmissionProcess");

                       try
                       {
                           while (tri.hasNext())
                           {
                               TableRow row = tri.next();

                               SubmissionProcess process = new SubmissionProcess(row);
                               new_id2process.put(new Integer(process.process_id), process);

                           }
                       }
                       finally
                       {
                           // close the TableRowIterator to free up resources
                           if (tri != null)
                               tri.close();
                       }

                       id2process = new_id2process;

                   }
               }
           }

       /**
            * Return all steps that are found in a given submissionprocess.
            *
            * @param context dspace context
            * @param processID submissionprocess by db ID
            * @return array of input form fields
            * @throws SQLException
            */
	    
           public static SubmissionStep[] getSteps(Context context,int processID)
                   throws SQLException
           {
               List steps = new ArrayList();

               // Get all the step2submissionprocess rows
               TableRowIterator tri = DatabaseManager.queryTable(context,"step2submissionprocess",
                       "SELECT * FROM step2submissionprocess WHERE process_id= ? " +
                       " ORDER BY place",processID);

               try
               {
                   // get Page objects
                   while (tri.hasNext())
                   {   int id=tri.next().getIntColumn("step_id");
                       steps.add(SubmissionStep.find(context,id));
                   }
               log.info("length"+steps.size());
               }
               finally
               {
                   // close the TableRowIterator to free up resources
                   if (tri != null)
                       tri.close();
               }

               // Convert list into an array
               SubmissionStep[] typeArray = new SubmissionStep[steps.size()];
               return (SubmissionStep[]) steps.toArray(typeArray);
           }

           public static Collection getCollection(Context context,int processID)
                   throws SQLException
            {
               Collection collection=null ;

               // Get all the collection2submissionprocess rows
               TableRowIterator tri = DatabaseManager.queryTable(context,"collection2submissionprocess",
                       "SELECT * FROM collection2submissionprocess WHERE process_id= ? ",processID);

               try
               {
                   // get Collection objects
                   while (tri.hasNext())
                   {   int id=tri.next().getIntColumn("collection_id");
                       collection=Collection.find(context,id);
                   }
			
               }
               finally
               {
                   // close the TableRowIterator to free up resources
                   if (tri != null)
                       tri.close();
               }

               // Convert list into an array
               return  collection;
           }

       public static void removeStep(Context context,int processID,int stepID) throws SQLException,
           AuthorizeException
           {
               TableRowIterator tri = DatabaseManager.queryTable(context,"step2submissionprocess",
                       "SELECT * FROM step2submissionprocess WHERE process_id= ? " +
                       " AND step_id=?",processID,stepID);
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
     public static void addStep(Context context,int processID,int stepID) throws SQLException,
           AuthorizeException
           {
           SubmissionStep.step2submissionprocess(context,processID,stepID);

           }
}
