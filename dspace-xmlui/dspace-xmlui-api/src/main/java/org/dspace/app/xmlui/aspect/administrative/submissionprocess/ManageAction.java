/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.administrative.submissionprocess;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.submission.state.SubmissionStep;
import org.dspace.submission.state.actions.SubmissionAction;
import org.xml.sax.SAXException;

import java.sql.SQLException;

/**
 * Web interface to Bulk Metadata Import app.
 * ported from org.dspace.app.webui.servlet.MetadataImportServlet
 *
 * Initial select file / upload CSV form
 *
 * @author Kim Shepherd
 */

public class ManageAction extends AbstractDSpaceTransformer {

	/** Language strings */
	private static final Message T_dspace_home = message("xmlui.general.dspace_home");

	private static final Message T_title = message("xmlui.administrative.submissionprocess.ManageAction.general.title");
	private static final Message T_head1 = message("xmlui.administrative.submissionprocess.ManageAction.head1");
        private static final Message T_submit_upload = message("xmlui.administrative.submissionprocess.ManageAction.submit_upload");
        private static final Message T_trail = message("xmlui.administrative.submissionprocess.ManageAction.general.trail");
    private static final Message T_head2 =
		message("xmlui.administrative.submissionprocess.ManageAction.head2");
	private static final Message T_namespace =
		message("xmlui.administrative.submissionprocess.ManageAction.namespace");
	private static final Message T_namespace_help =
		message("xmlui.administrative.submissionprocess.ManageAction.namespace_help");
	private static final Message T_namespace_error =
		message("xmlui.administrative.submissionprocess.ManageAction.namespace_error");
	private static final Message T_name =
		message("xmlui.administrative.submissionprocess.ManageAction.name");
    private static final Message T_column1 =
		message("xmlui.administrative.submissionprocess.ManageAction.column1");
	private static final Message T_column2 =
		message("xmlui.administrative.submissionprocess.ManageAction.column2");
	private static final Message T_column3 =
		message("xmlui.administrative.submissionprocess.ManageAction.column3");
	private static final Message T_name_help =
		message("xmlui.administrative.submissionprocess.ManageAction.name_help");
	private static final Message T_name_error =
		message("xmlui.administrative.submissionprocess.ManageAction.name_error");
	private static final Message T_submit_add =
		message("xmlui.administrative.submissionprocess.ManageAction.submit_add");
    private static final Message T_submit_delete =
		message("xmlui.administrative.submissionprocess.ManageAction.submit_delete");
    private static final Message T_edit_outcome =
		message("xmlui.administrative.submissionprocess.ManageAction.edit_outcome");
	
	public void addPageMeta(PageMeta pageMeta) throws WingException  
	{
		pageMeta.addMetadata("title").addContent(T_title);
		
		pageMeta.addTrailLink(contextPath + "/", T_dspace_home);
		pageMeta.addTrail().addContent(T_trail);
	}

	
	public void addBody(Body body) throws SAXException, WingException, SQLException
	{
		// Get all our parameters first
		Request request = ObjectModelHelper.getRequest(objectModel);
        int stepID = parameters.getParameterAsInteger("stepID", -1);
        SubmissionAction[] actions = SubmissionStep.getActions(context,stepID);
        // DIVISION: submission-process
		Division main = body.addInteractiveDivision("submissionprocess",contextPath+"/admin/submissionprocess",Division.METHOD_POST,"primary administrative metadata-registry ");
		main.setHead(T_head1);
        //main.addPara(T_para1);

		Table table = main.addTable("submissionprocess-main-table", actions.length+1, 4);

		Row header = table.addRow(Row.ROLE_HEADER);
		header.addCellContent(T_column1);
		header.addCellContent(T_column2);
		header.addCellContent(T_column3);

		for (SubmissionAction action : actions)
		{
			int actionID     = action.getId();
			String name      = action.getBean_id();
			String url = contextPath + "/admin/submissionprocess?administrative-continue="+knot.getId()+"&submit_edit&actionID="+actionID;

			Row row = table.addRow();
			if (actionID > 1)
			{
				// If the schema is not in the required DC schema allow the user to delete it.
				CheckBox select = row.addCell().addCheckBox("select_process");
				select.setLabel(String.valueOf(actionID));
				select.addOption(String.valueOf(actionID));
			}
			else
			{
				// The DC schema can not be removed.
				row.addCell();
			}

			row.addCell().addContent(actionID);
			row.addCell().addXref(url,name);
		}
		if (actions.length > 1)
		{
			// Only give the delete option if there are more schema's than the required dublin core.
			main.addPara().addButton("submit_delete").setValue(T_submit_delete);
		}
        main.addPara().addButton("edit_outcome").setValue(T_edit_outcome);
    	// DIVISION: add new action
		Division newProcess = main.addDivision("add-action");
		newProcess.setHead(T_head2);

		Para  select= newProcess.addPara();
        select.addContent(T_name);
        Select actionList =   select.addSelect("action_list");

        SubmissionAction[] allActions = SubmissionAction.findAll(context);
        for (SubmissionAction action : allActions)
    	{
    		actionList.addOption(action.getId(), action.getBean_id());
    	}
        select.addButton("submit_add").setValue(T_submit_add);

//		if (errors.contains("name"))
//        {
//            name.addError(T_name_error);
//        }

		main.addHidden("administrative-continue").setValue(knot.getId());
   }
}
