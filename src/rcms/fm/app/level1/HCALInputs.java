package rcms.fm.app.level1;

import rcms.statemachine.definition.Input;

/**
	* Definition of HCAL Function Manager Commands
	* 
	* @author Arno Heister
	*/

public class HCALInputs {

	// Defined commands for the level 1 Function Manager
	public static final Input INITIALIZE            =  Input("Initialize");
	public static final Input CONFIGURE             =  Input("Configure");
	public static final Input SETCONFIGURE          =  Input("SetConfigured");
	public static final Input START                 =  Input("Start");
	public static final Input HCALSTART             =  Input("Enable");
	public static final Input HCALASYNCSTART        =  Input("AsyncEnable");
	public static final Input SETSTART              =  Input("SetRunning");
	public static final Input SETRUNNINGDEGRADED    =  Input("SetRunningDegraded");
	public static final Input UNSETRUNNINGDEGRADED  =  Input("UnsetRunningDegraded");
	public static final Input HALT                  =  Input("Halt");
	public static final Input HCALHALT              =  Input("Disable");
	public static final Input STOP                  =  Input("Stop");
	public static final Input STOPPING              =  Input("Stopping");
	public static final Input SETHALT               =  Input("SetHalted");
	public static final Input PAUSE                 =  Input("Pause");
	public static final Input HCALPAUSE             =  Input("Suspend");
	public static final Input SETPAUSE              =  Input("SetPaused");
	public static final Input RESUME                =  Input("Resume");
	public static final Input SETRESUME             =  Input("SetResume");
	public static final Input RECOVER               =  Input("Recover");
	public static final Input SETINITIAL            =  Input("SetInitial");
	public static final Input RESET                 =  Input("Reset");
	public static final Input SETRESET              =  Input("SetReset");
	public static final Input TTSTEST_MODE          =  Input("TTSTestMode");
	public static final Input SETTTSTEST_MODE       =  Input("SetTTSTestMode");
	public static final Input HCALSETTTSTEST_MODE   =  Input("enableTTStest");
	public static final Input HCALLEAVETTSTEST_MODE =  Input("disableTTStest");
	public static final Input HCALSENDTTSTEST_MODE  =  Input("sendTTStestpattern");
	public static final Input TEST_TTS              =  Input("TestTTS");
	public static final Input SETTESTING_TTS        =  Input("SetTestingTTS");
	public static final Input SETERROR              =  Input("SetError");
	public static final Input SENDTTSTESTPATTERN    =  Input("SendTTSTestPattern");
	public static final Input FEDSTREAMERSTART      =  Input("start");
	public static final Input FEDSTREAMERSTOP       =  Input("stop");
	public static final Input COLDRESET             =  Input("ColdReset");
	public static final Input SETCOLDRESET          =  Input("SetColdReset");
}
