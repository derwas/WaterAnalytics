package jobs;

import controllers.Organizations;
import play.Logger;
import play.jobs.*;

@OnApplicationStart
@Every("1h")
public class Bootstrap extends Job {
    
    public void doJob() {
    	Logger.info("Updating the list of organisations!");
        Organizations.updateOrganisations();
    }
    
}