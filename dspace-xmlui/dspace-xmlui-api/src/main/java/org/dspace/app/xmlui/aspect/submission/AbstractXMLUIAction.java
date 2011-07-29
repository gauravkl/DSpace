package org.dspace.app.xmlui.aspect.submission;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.log4j.Logger;
import org.dspace.app.util.SubmissionInfo;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.Item;
import org.dspace.submission.state.actions.ActionInterface;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: bram Date: 9-aug-2010 Time: 13:04:05 To
 * change this template use File | Settings | File Templates.
 */
public abstract class AbstractXMLUIAction  extends AbstractDSpaceTransformer
		implements ActionInterface {

	protected static Logger log = Logger.getLogger(AbstractXMLUIAction.class);

	protected static final Message T_dspace_home = message("xmlui.general.dspace_home");

	protected static final Message T_showfull = message("xmlui.Submission.general.showfull");
	protected static final Message T_showsimple = message("xmlui.Submission.general.showsimple");
      protected static final Message T_submission_head =
        message("xmlui.Submission.general.submission.head");

	protected static final Message T_workflow_title = message("xmlui.Submission.general.workflow.title");

	protected static final Message T_workflow_trail = message("xmlui.Submission.general.workflow.trail");

	protected static final Message T_workflow_head = message("xmlui.Submission.general.workflow.head");

	/**
	 * The current DSpace SubmissionInfo
	 */
	protected WorkspaceService workspaceItem;
    /**
     * The id of the currently active workspace or workflow, this contains
     * the incomplete DSpace item
     */
	protected String id;

    /**
     * The current DSpace SubmissionInfo
     */
    protected SubmissionInfo submissionInfo;

	/**
	 * The in progress submission, if one is available, this may be either
	 * a workspaceItem or a workspaceItem.
	 */
	protected InProgressSubmission submission;

	/**
	 * The handle being processed by the current step.
	 */
	protected String handle;

	/**
	 * The error flag which was returned by the processing of this step
	 */
	protected int errorFlag;

	/**
	 * A list of fields that may be in error, not all stages support
	 * errored fields but if they do then this is where a list of all
	 * fields in error may be found.
	 */
	protected java.util.List<String> errorFields;


	/** The parameters that are required by this submissions / workflow step */
	protected boolean requireSubmission = false;
	protected boolean requireWorkflow = false;
	protected boolean requireWorkspace = false;
	protected boolean requireStep = false;
	protected boolean requireHandle = false;
    /**
	 * Grab all the page's parameters from the sitemap. This includes
	 * workspaceID, step, and a list of errored fields.
	 *
	 * If the implementer set any required parameters then insure that
	 * they are all present.
	 */
	public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters)
	throws ProcessingException, SAXException, IOException
	{
		super.setup(resolver,objectModel,src,parameters);

		try {
			this.id = parameters.getParameter("id",null);
			log.debug("AbstractStep.setup:  step is " + parameters.getParameter("step","]defaulted[")); // FIXME mhw
			this.handle = parameters.getParameter("handle",null);
			//this.errorFlag = Integer.valueOf(parameters.getParameter("error", String.valueOf(AbstractProcessingStep.STATUS_COMPLETE)));
			//this.errorFields = getErrorFields(parameters);


			//load in-progress submission
			if (this.id != null)
            {
                this.submissionInfo = FlowUtils.obtainSubmissionInfo(objectModel, this.id);
				this.submission = submissionInfo.getSubmissionItem();
            }

			// Check required error conditions
			if (this.requireSubmission && this.submission == null)
            {
                throw new ProcessingException("Unable to find submission for id: " + this.id);
            }

//			if (this.requireWorkflow && !(submission instanceof WorkflowItem))
//            {
//                throw new ProcessingException("The submission is not a workflow, " + this.id);
//            }

			if (this.requireWorkspace && !(submission instanceof WorkspaceItem))
            {
                throw new ProcessingException("The submission is not a workspace, " + this.id);
            }


			if (this.requireHandle && handle == null)
            {
                throw new ProcessingException("Handle is a required parameter.");
            }

		}
		catch (SQLException sqle)
		{
			throw new ProcessingException("Unable to find submission.",sqle);
		}
	}

	@Override
	public void addPageMeta(PageMeta pageMeta) throws SAXException,
			WingException, UIException, SQLException, IOException,
			AuthorizeException {
		super.addPageMeta(pageMeta);

		pageMeta.addMetadata("title").addContent(T_workflow_title);

		Collection collection = workspaceItem.getCollection();

		pageMeta.addTrailLink(contextPath + "/", T_dspace_home);
		HandleUtil.buildHandleTrail(collection, pageMeta, contextPath);
		pageMeta.addTrail().addContent(T_workflow_trail);

	}

	@Override
	public abstract void addBody(Body body) throws SAXException, WingException,
			SQLException, IOException, AuthorizeException;

	protected void addWorkflowItemInformation(Division div, Item item,
			Request request) throws WingException {
		String showfull = request.getParameter("submit_full_item_info");

		// if the user selected showsimple, remove showfull.
		if (showfull != null
				&& request.getParameter("submit_simple_item_info") != null)
			showfull = null;

		if (showfull == null) {
			ReferenceSet referenceSet = div.addReferenceSet("narf",
					ReferenceSet.TYPE_SUMMARY_VIEW);
			referenceSet.setHead(T_workflow_head);
			referenceSet.addReference(item);
			div.addPara().addButton("submit_full_item_info")
					.setValue(T_showfull);
		} else {
			ReferenceSet referenceSet = div.addReferenceSet("narf",
					ReferenceSet.TYPE_DETAIL_VIEW);
			referenceSet.setHead(T_workflow_head);
			referenceSet.addReference(item);
			div.addPara().addButton("submit_simple_item_info")
					.setValue(T_showsimple);

			div.addHidden("submit_full_item_info").setValue("true");
		}
	}

    /**
	 * Add a submission progress list to the current div for this step.
	 *
	 * @param div The division to add the list to.
	 */
	public void addSubmissionProgressList(Division div) throws WingException
	{
		//each entry in progress bar is placed under this "submit-progress" div
		List progress = div.addList("submit-progress",List.TYPE_PROGRESS);

		//get Map of progress bar information
		//key: entry # (i.e. step & page),
		//value: entry name key (i.e. display name)
		Map<String, String> progBarInfo = this.submissionInfo.getProgressBarInfo();

		//add each entry to progress bar
		for (Map.Entry<String, String> progBarEntry : progBarInfo.entrySet())
		{
			//Since we are using XML-UI, we need to prepend the heading key with "xmlui.Submission."
			String entryNameKey = "xmlui.Submission." + progBarEntry.getValue();

			//the value of entryNum is current step & page
			//(e.g. 1.2 is page 2 of step 1)

            //add a button to progress bar for this step & page

		}

	}
}
