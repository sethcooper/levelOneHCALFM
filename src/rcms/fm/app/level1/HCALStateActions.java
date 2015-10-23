package rcms.fm.app.level1;

import rcms.fm.fw.user.UserActionException;
import rcms.fm.fw.user.UserActions;
import rcms.fm.fw.StateEnteredEvent;
import rcms.fm.fw.user.UserEvent;
import rcms.util.logger.RCMSLogger;
import rcms.fm.fw.user.UserActionException;
import rcms.fm.fw.user.UserActions;
import rcms.ns.event.NotificationEvent;
import rcms.ns.utils.NotificationHelper;

/**
* Class for callbacks associated to Entered and Exiting States.
* 
* @author Arno Heister
*/

public class HCALStateActions extends UserActions {

	static RCMSLogger logger = new RCMSLogger(HCALStateActions.class);

	private HCALFunctionManager functionManager = null;

	public void init() {
		functionManager = (HCALFunctionManager)getUserFunctionManager();
	}

	public void fireUserEventAction() throws UserActionException {
		System.out.println("Executing fireUserEventAction");
		logger.info("Executing fireUserEventAction");

		functionManager.fireEvent(new StateEnteredEvent());

		logger.info("fireUserEventAction executed ...");
		System.out.println("fireUserEventAction executed ...");
	}	
}