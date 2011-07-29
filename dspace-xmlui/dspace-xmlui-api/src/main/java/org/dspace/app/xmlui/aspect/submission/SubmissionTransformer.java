package org.dspace.app.xmlui.aspect.submission;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Options;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.app.xmlui.wing.element.UserMeta;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.WorkspaceService;
import org.dspace.core.LogManager;

import org.dspace.submission.SubmissionProcessFactory;
import org.dspace.submission.state.SubmissionProcess;
import org.dspace.submission.state.SubmissionStep;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: bram
 * Date: 3-aug-2010
 * Time: 16:42:34
 * To change this template use File | Settings | File Templates.
 */
public class SubmissionTransformer extends AbstractDSpaceTransformer {

    private AbstractXMLUIAction xmluiActionUI;
    private boolean authorized = true;


    private static Logger log = Logger.getLogger(SubmissionTransformer.class);


    @Override
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters) throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, parameters);
        authorized = true;
        try {
            String processID = parameters.getParameter("process_id");
            String stepID = parameters.getParameter("step_id");
            String actionID = parameters.getParameter("action_id");
            int workspaceID = parameters.getParameterAsInteger("workspace_item_id");
            WorkspaceService wi = WorkspaceService.find(context, workspaceID);
            SubmissionProcess wf = SubmissionProcessFactory.getSubmissionProcess(context, wi.getCollection());

            SubmissionStep step = wf.getStep(context,Integer.parseInt(stepID));
            xmluiActionUI = (AbstractXMLUIAction) SubmissionProcessXMLUIFactory.getActionInterface(actionID);
            authorized = step.getActionConfig(actionID).getProcessingAction().isAuthorized(context, ObjectModelHelper.getRequest(objectModel), wi);

            if(xmluiActionUI != null)
                xmluiActionUI.setup(resolver, objectModel, src, parameters);
//            else
//                throw new ProcessingException("SubmissionStep class is null!  We do not have a valid AbstractStep in " + this.transformerClassName + ". ");
        } catch (Exception e) {
            log.error(LogManager.getHeader(context, "error while setting up SubmissionTransformer", ""), e);
            e.printStackTrace();
            throw new ProcessingException("Something went wrong while setting up the workflow");
        }
            //TODO: throw exception !
    }

    public void addBody(Body body) throws SAXException, WingException, SQLException, IOException, AuthorizeException {
        if(!authorized)
            throw new AuthorizeException("You are not authorized to perform this task");

        xmluiActionUI.addBody(body);
    }

    /** What to add to the options list */
    public void addOptions(Options options) throws SAXException, WingException,
            UIException, SQLException, IOException, AuthorizeException
    {
        //call addOptions for this step
    	xmluiActionUI.addOptions(options);
    }

    /** What user metadata to add to the document */
    public void addUserMeta(UserMeta userMeta) throws SAXException,
            WingException, UIException, SQLException, IOException,
            AuthorizeException
    {
    	//call addUserMeta for this step
    	xmluiActionUI.addUserMeta(userMeta);
    }

    /** What page metadata to add to the document */
    public void addPageMeta(PageMeta pageMeta) throws SAXException,
            WingException, UIException, SQLException, IOException,
            AuthorizeException
    {
    	//call addPageMeta for this step
    	xmluiActionUI.addPageMeta(pageMeta);
    }

    /**
     * Recycle
     */
    public void recycle() 
    {
        if(xmluiActionUI!=null)
        {
            this.xmluiActionUI.recycle();
            this.xmluiActionUI = null;
        }
        super.recycle();
    }
}
