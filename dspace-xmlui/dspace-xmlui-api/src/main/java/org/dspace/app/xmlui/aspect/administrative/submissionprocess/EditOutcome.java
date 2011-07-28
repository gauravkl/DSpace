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

public class EditOutcome extends AbstractDSpaceTransformer {

	/** Language strings */
	private static final Message T_dspace_home = message("xmlui.general.dspace_home");

	private static final Message T_title = message("xmlui.administrative.submissionprocess.EditOutcome.general.title");
	private static final Message T_head1 = message("xmlui.administrative.submissionprocess.EditOutcome.head1");
        private static final Message T_submit_upload = message("xmlui.administrative.submissionprocess.EditOutcome.submit_upload");
        private static final Message T_trail = message("xmlui.administrative.submissionprocess.EditOutcome.trail");
    private static final Message T_head2 =
		message("xmlui.administrative.submissionprocess.EditOutcome.head2");
	private static final Message T_namespace =
		message("xmlui.administrative.submissionprocess.EditOutcome.namespace");
    private static final Message T_status =
            message("xmlui.administrative.submissionprocess.EditOutcome.status");
	private static final Message T_namespace_help =
		message("xmlui.administrative.submissionprocess.EditOutcome.namespace_help");
	private static final Message T_namespace_error =
		message("xmlui.administrative.submissionprocess.EditOutcome.namespace_error");
	private static final Message T_name =
		message("xmlui.administrative.submissionprocess.EditOutcome.name");
    private static final Message T_column1 =
		message("xmlui.administrative.submissionprocess.EditOutcome.column1");
	private static final Message T_column2 =
		message("xmlui.administrative.submissionprocess.EditOutcome.column2");
	private static final Message T_column3 =
		message("xmlui.administrative.submissionprocess.EditOutcome.column3");
	private static final Message T_name_help =
		message("xmlui.administrative.submissionprocess.EditOutcome.name_help");
	private static final Message T_name_error =
		message("xmlui.administrative.submissionprocess.EditOutcome.name_error");
	private static final Message T_submit_add =
		message("xmlui.administrative.submissionprocess.EditOutcome.submit_add");
    private static final Message T_save_outcome =
            message("xmlui.administrative.submissionprocess.EditOutcome.save_outcome");
    private static final Message T_submit_delete =
		message("xmlui.administrative.submissionprocess.EditOutcome.submit_delete");
    private static final Message T_submit_assign =
		message("xmlui.administrative.submissionprocess.EditOutcome.submit_assign");
	
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
		String namespaceValue = request.getParameter("namespace");
		String nameValue = request.getParameter("name");
        SubmissionAction[] actions = SubmissionAction.findAll(context);
        // DIVISION: submission-process
		Division main = body.addInteractiveDivision("submissionprocess",contextPath+"/admin/submissionprocess",Division.METHOD_POST,"primary administrative metadata-registry ");
		main.setHead(T_head1);
        //main.addPara(T_para1);

//		Table table = main.addTable("submissionprocess-main-table", actions.length+1, 4);
//
//		Row header = table.addRow(Row.ROLE_HEADER);
//		header.addCellContent(T_column1);
//		header.addCellContent(T_column2);
//		header.addCellContent(T_column3);
//
//		for (SubmissionAction action : actions)
//		{
//			int processID     = action.getId();
//			String name      = action.getBean_id();
//			String url = contextPath + "/admin/submissionprocess?administrative-continue="+knot.getId()+"&submit_edit&processID="+processID;
//
//			Row row = table.addRow();
//			if (processID > 1)
//			{
//				// If the schema is not in the required DC schema allow the user to delete it.
//				CheckBox select = row.addCell().addCheckBox("select_process");
//				select.setLabel(String.valueOf(processID));
//				select.addOption(String.valueOf(processID));
//			}
//			else
//			{
//				// The DC schema can not be removed.
//				row.addCell();
//			}
//
//			row.addCell().addContent(processID);
//			row.addCell().addXref(url,name);
//		}
        List form = main.addList("new-outcome-form",List.TYPE_FORM);
		form.addLabel(T_status);
		Text name = form.addItem().addText("outcome");
		name.setSize(15);
		name.setValue(nameValue);
		Item stepName = form.addItem();
        stepName.addContent(T_name);
        Select stepList =   stepName.addSelect("step_list");

        SubmissionStep[] allSteps = SubmissionStep.findAll(context);
        for (SubmissionStep step : allSteps)
    	{
    		stepList.addOption(step.getId(), step.getName());
    	}
		form.addItem().addButton("save_outcome").setValue(T_save_outcome);
        // DIVISION: add new action


        main.addPara().addButton("submit_add").setValue(T_submit_add);

//		if (errors.contains("name"))
//        {
//            name.addError(T_name_error);
//        }

		main.addHidden("administrative-continue").setValue(knot.getId());
   }
}
