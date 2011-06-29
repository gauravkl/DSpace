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

	private static final Message T_title = message("xmlui.administrative.submissionprocess.general.title");
	private static final Message T_head1 = message("xmlui.administrative.submissionprocess.general.head1");
        private static final Message T_submit_upload = message("xmlui.administrative.submissionprocess.CreateProcessMain.submit_upload");
        private static final Message T_trail = message("xmlui.administrative.submissionprocess.general.trail");
    private static final Message T_head2 =
		message("xmlui.administrative.submissionprocess.head2");
	private static final Message T_namespace =
		message("xmlui.administrative.submissionprocess.namespace");
	private static final Message T_namespace_help =
		message("xmlui.administrative.submissionprocess.namespace_help");
	private static final Message T_namespace_error =
		message("xmlui.administrative.submissionprocess.namespace_error");
	private static final Message T_name =
		message("xmlui.administrative.submissionprocess.name");
	private static final Message T_name_help =
		message("xmlui.administrative.submissionprocess.name_help");
	private static final Message T_name_error =
		message("xmlui.administrative.submissionprocess.name_error");
	private static final Message T_submit_add =
		message("xmlui.administrative.submissionprocess.submit_add");
	
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
        // DIVISION: submission-process
		Division main = body.addInteractiveDivision("submissionprocess",contextPath+"/admin/submissionprocess",Division.METHOD_POST,"primary administrative metadata-registry ");
		main.setHead(T_head1);
    	// DIVISION: add new process
		Division newProcess = main.addDivision("add-process");
		newProcess.setHead(T_head2);

		List form = newProcess.addList("new-process",List.TYPE_FORM);

		Text namespace = form.addItem().addText("namespace");
		namespace.setLabel(T_namespace);
		namespace.setRequired();
		namespace.setHelp(T_namespace_help);
		if (namespaceValue != null)
        {
            namespace.setValue(namespaceValue);
        }
//		if (errors.contains("namespace"))
//        {
//            namespace.addError(T_namespace_error);
//        }

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
