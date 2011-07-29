package org.dspace.app.xmlui.aspect.submission;

import org.dspace.core.ConfigurationManager;
import org.dspace.submission.state.actions.ActionInterface;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 20-aug-2010
 * Time: 10:08:19
 */
public class SubmissionProcessXMLUIFactory {

    private static String path = ConfigurationManager.getProperty("dspace.dir")+"/config/submission-actions-xmlui.xml";


    public static ActionInterface getActionInterface(String id){
        ApplicationContext applicationContext = new FileSystemXmlApplicationContext("file:" + path);
        return (ActionInterface) applicationContext.getBean(id, ActionInterface.class);
    }

}
