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
import org.dspace.submission.state.SubmissionProcess;
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

public class CreateProcessMain extends AbstractDSpaceTransformer {

	/** Language strings */
	private static final Message T_dspace_home = message("xmlui.general.dspace_home");

	private static final Message T_title = message("xmlui.administrative.submissionprocess.CreateProcessMain.general.title");
	private static final Message T_head1 = message("xmlui.administrative.submissionprocess.CreateProcessMain.head1");
        private static final Message T_submit_upload = message("xmlui.administrative.submissionprocess.CreateProcessMain.submit_upload");
        private static final Message T_trail = message("xmlui.administrative.submissionprocess.CreateProcessMain.general.trail");
    private static final Message T_head2 =
		message("xmlui.administrative.submissionprocess.CreateProcessMain.head2");
	private static final Message T_namespace =
		message("xmlui.administrative.submissionprocess.CreateProcessMain.namespace");
	private static final Message T_namespace_help =
		message("xmlui.administrative.submissionprocess.CreateProcessMain.namespace_help");
	private static final Message T_namespace_error =
		message("xmlui.administrative.submissionprocess.CreateProcessMain.namespace_error");
	private static final Message T_name =
		message("xmlui.administrative.submissionprocess.CreateProcessMain.name");
    private static final Message T_column1 =
		message("xmlui.administrative.submissionprocess.CreateProcessMain.column1");
	private static final Message T_column2 =
		message("xmlui.administrative.submissionprocess.CreateProcessMain.column2");
	private static final Message T_column3 =
		message("xmlui.administrative.submissionprocess.CreateProcessMain.column3");
	private static final Message T_name_help =
		message("xmlui.administrative.submissionprocess.CreateProcessMain.name_help");
	private static final Message T_name_error =
		message("xmlui.administrative.submissionprocess.CreateProcessMain.name_error");
	private static final Message T_submit_add =
		message("xmlui.administrative.submissionprocess.CreateProcessMain.submit_add");
    private static final Message T_submit_delete =
		message("xmlui.administrative.submissionprocess.CreateProcessMain.submit_delete");
    private static final Message T_submit_assign =
		message("xmlui.administrative.submissionprocess.CreateProcessMain.submit_assign");
	
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
        SubmissionProcess[] processes = SubmissionProcess.findAll(context);
        // DIVISION: submission-process
		Division main = body.addInteractiveDivision("submissionprocess",contextPath+"/admin/submissionprocess",Division.METHOD_POST,"primary administrative metadata-registry ");
		main.setHead(T_head1);
        //main.addPara(T_para1);

		Table table = main.addTable("submissionprocess-main-table", processes.length+1, 4);

		Row header = table.addRow(Row.ROLE_HEADER);
		header.addCellContent(T_column1);
		header.addCellContent(T_column2);
		header.addCellContent(T_column3);

		for (SubmissionProcess process : processes)
		{
			int processID     = process.getID();
			String name      = process.getName();
			String url = contextPath + "/admin/submissionprocess?administrative-continue="+knot.getId()+"&submit_edit&processID="+processID;

			Row row = table.addRow();
			if (processID > 1)
			{
				// If the schema is not in the required DC schema allow the user to delete it.
				CheckBox select = row.addCell().addCheckBox("select_process");
				select.setLabel(String.valueOf(processID));
				select.addOption(String.valueOf(processID));
			}
			else
			{
				// The DC schema can not be removed.
				row.addCell();
			}

			row.addCell().addContent(processID);
			row.addCell().addXref(url,name);
		}
		if (processes.length > 1)
		{
			// Only give the delete option if there are more schema's than the required dublin core.
			main.addPara().addButton("submit_delete").setValue(T_submit_delete);
		}
        main.addPara().addButton("submit_assign").setValue(T_submit_assign);
    	// DIVISION: add new process
		Division newProcess = main.addDivision("add-process");
		newProcess.setHead(T_head2);

		List form = newProcess.addList("new-process",List.TYPE_FORM);

		Text name = form.addItem().addText("name");
		name.setLabel(T_name);
		name.setRequired();
		name.setHelp(T_name_help);
		if (nameValue != null)
        {
            name.setValue(nameValue);
        }
//		if (errors.contains("name"))
//        {
//            name.addError(T_name_error);
//        }

		form.addItem().addButton("submit_add").setValue(T_submit_add);
		main.addHidden("administrative-continue").setValue(knot.getId());
   }
}
