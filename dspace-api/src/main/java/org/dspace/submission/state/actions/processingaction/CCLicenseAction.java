/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.submission.state.actions.processingaction;

import org.apache.log4j.Logger;
import org.dspace.app.util.SubmissionInfo;
import org.dspace.app.util.Util;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.license.CreativeCommons;
import org.dspace.submission.state.actions.ActionResult;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;

/**
 * License step for DSpace Submission Process. Processes the
 * user response to the license.
 * <P>
 * This class performs all the behind-the-scenes processing that
 * this particular step requires.  This class's methods are utilized 
 * by both the JSP-UI and the Manakin XML-UI
 * <P>
 * 
 * @see org.dspace.app.util.SubmissionConfig
 * @see org.dspace.app.util.SubmissionStepConfig
 * @see org.dspace.submit.AbstractProcessingStep
 * 
 * @author Tim Donohue
 * @version $Revision$
 */
public class CCLicenseAction extends ProcessingAction
{
    /***************************************************************************
     * STATUS / ERROR FLAGS (returned by doProcessing() if an error occurs or
     * additional user interaction may be required)
     * 
     * (Do NOT use status of 0, since it corresponds to STATUS_COMPLETE flag
     * defined in the JSPStepManager class)
     **************************************************************************/
    // user rejected the license
    public static final int STATUS_LICENSE_REJECTED = 1;

    /** log4j logger */
    private static Logger log = Logger.getLogger(CCLicenseAction.class);


    /**
     * Do any processing of the information input by the user, and/or perform
     * step processing (if no user interaction required)
     * <P>
     * It is this method's job to save any data to the underlying database, as
     * necessary, and return error messages (if any) which can then be processed
     * by the appropriate user interface (JSP-UI or XML-UI)
     * <P>
     * NOTE: If this step is a non-interactive step (i.e. requires no UI), then
     * it should perform *all* of its processing in this method!
     * 
     * @param context
     *            current DSpace context
     * @param request
     *            current servlet request object
     * @param response
     *            current servlet response object
     * @param subInfo
     *            submission info object
     * @return Status or error flag which will be processed by
     *         doPostProcessing() below! (if STATUS_COMPLETE or 0 is returned,
     *         no errors occurred!)
     */
   public ActionResult execute(Context context, HttpServletRequest request,
            SubmissionInfo subInfo)
            throws ServletException, IOException, SQLException,
            AuthorizeException
    {
        String buttonPressed = Util.getSubmitButton(request, NEXT_BUTTON);

        // RLR hack - need to distinguish between progress bar real submission
        // (if cc_license_url exists, then users has accepted the CC License)
        String ccLicenseUrl = request.getParameter("cc_license_url");

        if (buttonPressed.equals("submit_no_cc"))
        {
            // Skipping the CC license - remove any existing license selection
            CreativeCommons.removeLicense(context, subInfo.getSubmissionItem()
                    .getItem());
        }
        else if ((ccLicenseUrl != null) && (ccLicenseUrl.length() > 0))
        {
            Item item = subInfo.getSubmissionItem().getItem();

            // save the CC license
            CreativeCommons.setLicense(context, item, ccLicenseUrl);
        }

        // commit changes
        context.commit();

        // completed without errors
        return  new ActionResult(ActionResult.TYPE.TYPE_OUTCOME,ActionResult.OUTCOME_COMPLETE); // no errors!
    }

    /**
     * Retrieves the number of pages that this "step" extends over. This method
     * is used to build the progress bar.
     * <P>
     * This method may just return 1 for most steps (since most steps consist of
     * a single page). But, it should return a number greater than 1 for any
     * "step" which spans across a number of HTML pages. For example, the
     * configurable "Describe" step (configured using input-forms.xml) overrides
     * this method to return the number of pages that are defined by its
     * configuration file.
     * <P>
     * Steps which are non-interactive (i.e. they do not display an interface to
     * the user) should return a value of 1, so that they are only processed
     * once!
     * 
     * @param request
     *            The HTTP Request
     * @param subInfo
     *            The current submission information object
     * 
     * @return the number of pages in this step
     */
    public int getNumberOfPages(HttpServletRequest request,
            SubmissionInfo subInfo) throws ServletException
    {
        return 1;

    }

}
