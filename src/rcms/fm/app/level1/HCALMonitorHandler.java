package rcms.fm.app.level1;

import rcms.fm.fw.EventHandlerException;
import rcms.fm.fw.user.UserActionException;
import rcms.fm.fw.user.UserEventHandler;
import rcms.util.logger.RCMSLogger;

import java.util.Calendar;

import net.hep.cms.xdaqctl.XDAQMessageException;
import net.hep.cms.xdaqctl.XMASMessage;
import net.hep.cms.xdaqctl.xdata.FlashList;

/**
	* Monitor Event Handler class i.e. XMAS flashlist collector for HCAL Function Managers
	* 
	* @author Arno Heister
	*
	*/

public class HCALMonitorHandler extends UserEventHandler {

	static RCMSLogger logger = new RCMSLogger(HCALMonitorHandler.class);

	protected HCALFunctionManager functionManager = null;

	private long lastReport=0;
	private long msgCount = 0;

	public HCALMonitorHandler() throws EventHandlerException {

		subscribeForEvents(XMASMessage.class);

		// adding callbacks action associated to a specific FM state
		addAnyStateAction("onXmasMessage");
	}

	public void init() throws EventHandlerException {
		functionManager = (HCALFunctionManager) getUserFunctionManager();
		logger.debug("[HCAL] init() called: functionManager = " + functionManager );
	}

	public void onXmasMessage(XMASMessage xmas) throws UserActionException {

		try {
			++msgCount;
			logger.info("[HCAL " + functionManager.FMname + "] WSE received " + (msgCount-1) + "-th XMAS message: " + xmas.getFlashListName());

			// check for flashlist - host map 
			FlashList fl = xmas.getFlashList();
			String flName = xmas.getFlashListName();
			String flOriginator = xmas.getOriginator();

			String msg="" ;int iitem=-1;
			for ( String item : fl.getItemNames() ) {
				++iitem;
				msg += iitem+") "+item +" : "+fl.get(item)+"\n";
			}

			long timeSinceLastReport = Calendar.getInstance().getTimeInMillis() - lastReport;
			if (flName.equals(functionManager.RunInfoFlashlistName)) {
				lastReport = Calendar.getInstance().getTimeInMillis();
			}
			String msg2 = ""+(msgCount-1)+"-th XMAS "+flName+": delta(t)="+timeSinceLastReport+" ms = "+ (timeSinceLastReport/1000)+" s = "+(timeSinceLastReport/(60*1000))+"m "+((timeSinceLastReport%(60*1000))/1000)+"s ";
			msg+="\nName="+flName+" Originator="+flOriginator;
			logger.info("[HCAL " + functionManager.FMname + "] WSE "+(msgCount-1)+") ("+(timeSinceLastReport/1000)+"s) flashlist ("+flName+") content:\n" + msg + "\n" + msg2);

		} catch (XDAQMessageException e) {
			String errMessage = "[HCAL " + functionManager.FMname + "] Error! XDAQMessageException when collecting info about XMAS flashlist - this is bad!";
			logger.error(errMessage,e);
			functionManager.sendCMSError(errMessage);

		}
	}

	public void destroy() {
		super.destroy();
	}
}