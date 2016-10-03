package rcms.fm.app.level1;

import rcms.statemachine.definition.StateMachineDefinitionException;
import rcms.util.logger.RCMSLogger;

/**
	* Function Machine to control a Level 1 HCAL Function Manager
	*
	* @author Arno Heister
	*
	*/

public class HCALlevelOneFunctionManager extends HCALFunctionManager {

	static RCMSLogger logger = new RCMSLogger(HCALlevelOneFunctionManager.class);

	public HCALlevelOneFunctionManager() {}

	public void init() throws StateMachineDefinitionException,rcms.fm.fw.EventHandlerException {

		super.init();

    // get session ID
    //getSessionId();

		// add event handler
		theEventHandler = new HCALlevelOneEventHandler();
		addEventHandler(theEventHandler);
	}
}
