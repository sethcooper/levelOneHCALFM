package rcms.fm.app.level1;

import java.util.List;

import rcms.util.logger.RCMSLogger;

import rcms.fm.fw.parameter.type.StringT;
import rcms.fm.fw.parameter.FunctionManagerParameter;

import rcms.resourceservice.db.Group;
import rcms.resourceservice.db.resource.Resource;
import rcms.common.db.DBConnectorException;
import rcms.fm.resource.QualifiedGroup;
import rcms.fm.resource.QualifiedResource;
import rcms.fm.resource.qualifiedresource.FunctionManager;

/**
 *  @author John Hakala
 *
 */

public class HCALMasker {

  protected HCALFunctionManager functionManager = null;
  static RCMSLogger logger = null;

  public HCALMasker(HCALFunctionManager parentFunctionManager) {
    this.logger = new RCMSLogger(HCALFunctionManager.class);
    logger.warn("Constructing masker.");
    this.functionManager = parentFunctionManager;
    logger.warn("Done constructing masker.");
  }

  protected Boolean isEvmTrigCandidate(List<Resource> level2Children) {
    boolean hasAtriggerAdapter = false;
    boolean hasAdummy = false;
    boolean hasAnEventBuilder = false;
    boolean hasAnFU = false;
    for (Resource level2resource : level2Children) {
      if (level2resource.getName().contains("TriggerAdapter") || level2resource.getName().contains("FanoutTTCciTA")) {
        logger.info("[JohnLog2] " + functionManager.FMname + ": the FM being checked now has a TA.");
        hasAtriggerAdapter=true;
        if (level2resource.getName().contains("DummyTriggerAdapter")) {
          logger.info("[JohnLog2] " + functionManager.FMname + ": the FM being checked now has a DummyTriggerAdapter.");
          hasAdummy=true;
        }
      }
      if (level2resource.getName().contains("hcalTrivialFU")) {
        logger.info("[JohnLog2] " + functionManager.FMname + ": the FM being checked now has a FU.");
        hasAnFU=true;
      }
      if (level2resource.getName().contains("hcalEventBuilder")) {
        logger.info("[JohnLog2] " + functionManager.FMname + ": the FM being checked now has an eventBuilder.");
        hasAnEventBuilder=true;
      }
    }
    return new Boolean( hasAtriggerAdapter && hasAnFU && hasAnEventBuilder );
  }
  protected void pickEvmTrig() {
    // Function to pick an FM that has the needed applications for triggering and eventbuilding, and put it in charge of those duties
    // This will prefer an FM with a DummyTriggerAdapter to other kinds of trigger adapters.

    QualifiedGroup qg = functionManager.getQualifiedGroup();
    String MaskedFMs =  ((StringT)functionManager.getHCALparameterSet().get(HCALParameters.MASKED_RESOURCES).getValue()).getString();
    if (MaskedFMs.length() > 0) {
      MaskedFMs = MaskedFMs.substring(0, MaskedFMs.length()-1);
    }

    List<QualifiedResource> level2list = qg.seekQualifiedResourcesOfType(new FunctionManager());
    for (QualifiedResource level2 : level2list) {
      try {
        QualifiedGroup level2group = ((FunctionManager)level2).getQualifiedGroup();
        logger.debug("[HCAL " + functionManager.FMname + "]: the qualified group has this DB connector" + level2group.rs.toString());
        Group fullConfig = level2group.rs.retrieveLightGroup(level2.getResource());
        List<Resource> level2Children = fullConfig.getChildrenResources();
        logger.warn("[JohnLog2] " + functionManager.FMname + ": pickEvmTrig is calling isEvmTrigCandidate() on " + level2.getName());
        logger.warn("[JohnLog2] " + functionManager.FMname + ": the result of isEvmTrigCandidate() is: " + isEvmTrigCandidate(level2Children).toString());

      }
      catch (DBConnectorException ex) {
        logger.error("[HCAL " + functionManager.FMname + "]: Got a DBConnectorException when trying to retrieve level2s' children resources: " + ex.getMessage());
      }
    }

  }

  protected void setMaskedFMs() {

    // functionManager.getHCALparameterSet().put(new FunctionManagerParameter<StringT>(HCALParameters.MASKED_APPLICATIONS,new StringT(MaskedApplications)));

    QualifiedGroup qg = functionManager.getQualifiedGroup();
    // TODO send all masked applications defined in global parameter 
    String MaskedFMs =  ((StringT)functionManager.getHCALparameterSet().get(HCALParameters.MASKED_RESOURCES).getValue()).getString();
    if (MaskedFMs.length() > 0) {
      MaskedFMs = MaskedFMs.substring(0, MaskedFMs.length()-1);
    }
    List<QualifiedResource> level2list = qg.seekQualifiedResourcesOfType(new FunctionManager());
    boolean somebodysHandlingTA = false;
    boolean itsThisLvl2 = false;
    boolean itsAdummy = false;
    String allMaskedResources = "";
    String ruInstance = "";
    String lpmSupervisor = "";
    String EvmTrigsApps = "";
    for (QualifiedResource qr : level2list) {
      itsThisLvl2 = false;
      try {
        QualifiedGroup level2group = ((FunctionManager)qr).getQualifiedGroup();
        logger.debug("[HCAL " + functionManager.FMname + "]: the qualified group has this DB connector" + level2group.rs.toString());
        Group fullConfig = level2group.rs.retrieveLightGroup(qr.getResource());
        // TODO see here
        List<Resource> fullconfigList = fullConfig.getChildrenResources();
        if (MaskedFMs.length() > 0) {
          logger.info("[HCAL " + functionManager.FMname + "]:: Got MaskedFMs " + MaskedFMs);
          String[] MaskedResourceArray = MaskedFMs.split(";");
          for (String MaskedFM: MaskedResourceArray) {
            logger.debug("[HCAL " + functionManager.FMname + "]: " + functionManager.FMname + ": Starting to mask FM " + MaskedFM);
            if (qr.getName().equals(MaskedFM)) {
              logger.debug("[HCAL " + functionManager.FMname + "]: Going to call setActive(false) on "+qr.getName());
              qr.setActive(false);

              //logger.info("[HCAL " + functionManager.FMname + "]: LVL2 " + qr.getName() + " has rs group " + level2group.rs.toString());
              allMaskedResources = ((StringT)functionManager.getHCALparameterSet().get(HCALParameters.MASKED_RESOURCES).getValue()).getString();
              for (Resource level2resource : fullconfigList) {
                logger.debug("[HCAL " + functionManager.FMname + "]: The masked level 2 function manager " + qr.getName() + " has this in its XdaqExecutive list: " + level2resource.getName());
                allMaskedResources+=level2resource.getName();
                allMaskedResources+=";";
                logger.info("[HCAL " + functionManager.FMname + "]: The new list of all masked resources is: " + allMaskedResources);
              }
            }
          }
        }
        for (Resource level2resource : fullconfigList) {
          logger.debug("[HCAL " + functionManager.FMname + "]: the FM with name: " + qr.getName() + " has a resource named " + level2resource.getName() );
          if (!MaskedFMs.contains(qr.getName())) { 
            if (!allMaskedResources.contains(qr.getName()) && (level2resource.getName().contains("TriggerAdapter") || level2resource.getName().contains("FanoutTTCciTA")))          {
              if (somebodysHandlingTA ) { 
                if (level2resource.getName().contains("DummyTriggerAdapter") && !EvmTrigsApps.contains("DummyTriggerAdapter")) {
                  // itsAdummy=true;
                  logger.warn("[JohnLog] found a DummyTriggerAdapter in " + qr.getName() + " after somebody else is already handling the TA.");
                  allMaskedResources += EvmTrigsApps;
                  qr.getResource().setRole("EvmTrig");
                  logger.warn("[JohnLog] just set the role EvmTrig for the FM with name: " + qr.getName());
                  logger.warn("[JohnLog] starting to look for the old EvmTrig to be replaced.");
                  for (QualifiedResource otherLevel2FM : level2list) {
                    logger.warn("[JohnLog] found other level2 with name : " + otherLevel2FM.getName() + " and role: " + otherLevel2FM.getRole().toString());

                    if (otherLevel2FM.getRole().toString().equals("EvmTrig") && !qr.getName().equals(otherLevel2FM.getName())) {
                      otherLevel2FM.getResource().setRole("HCAL");
                      logger.warn("[JohnLog] just reset the role HCAL for the FM with name: "  + otherLevel2FM.getName());
                      itsThisLvl2=true;
                      functionManager.getHCALparameterSet().put(new FunctionManagerParameter<StringT>(HCALParameters.EVM_TRIG_FM, new StringT(qr.getName())));
                      logger.warn("[JohnLog] just reset the role EVM_TRIG_FM");
                    }
                  }
                }
                else {
                  allMaskedResources+=level2resource.getName()+";"; 
                  logger.info("[HCAL " + functionManager.FMname + "]: Just masked the redundant trigger adapter " + level2resource.getName());
                }
              }

              else {
                qr.getResource().setRole("EvmTrig");
                logger.info("[HCAL " + functionManager.FMname + "]: The following FM is handling the trigger adapter: " + qr.getName());
                somebodysHandlingTA=true;
                itsThisLvl2=true;
                // if (qr.getName().contains("DummyTriggerAdapter")){
                //   itsAdummy = true;
                //  }
                logger.debug("[HCAL " + functionManager.FMname + "]: About to set EVM_TRIG_FM.");
                functionManager.getHCALparameterSet().put(new FunctionManagerParameter<StringT>(HCALParameters.EVM_TRIG_FM, new StringT(qr.getName())));
                logger.info("[HCAL " + functionManager.FMname + "]: Just set EVM_TRIG_FM.");
                EvmTrigsApps += level2resource.getName()+";";
                logger.warn("[JohnLog] filled the list of applications which may need to be masked if a DummyTriggerAdapter is found: " + EvmTrigsApps);
              }
            }
            if (!allMaskedResources.contains(qr.getName()) && level2resource.getName().contains("hcalTrivialFU"))          {
              if (somebodysHandlingTA && !itsThisLvl2) { 
                allMaskedResources+=level2resource.getName()+";"; 
                logger.info("[HCAL " + functionManager.FMname + "]: Just masked the redundant TrivialFU " + level2resource.getName());
              }
              else {
                EvmTrigsApps += level2resource.getName()+";";
              }
            }
            if (!allMaskedResources.contains(qr.getName()) && level2resource.getName().contains("hcalEventBuilder"))          {
              if (somebodysHandlingTA && !itsThisLvl2) { 
                allMaskedResources+=level2resource.getName()+";"; 
                logger.info("[HCAL " + functionManager.FMname + "]: Just masked the redundant EventBuilder " + level2resource.getName());
              }
              else {
                EvmTrigsApps += level2resource.getName()+";";
                ruInstance=level2resource.getName();
                logger.info("[HCAL " + functionManager.FMname + "]: Just found the remaining EventBuilder " + level2resource.getName());
              }
            }
            if (!allMaskedResources.contains(qr.getName()) && level2resource.getName().contains("hcalSupervisor"))          {
              if (somebodysHandlingTA && !itsThisLvl2) { 
                logger.debug("[HCAL " + functionManager.FMname + "]: Found a Supervisor who is not handling the LPM." + level2resource.getName());
              }
              else if (somebodysHandlingTA && itsThisLvl2) {
                logger.info("[HCAL " + functionManager.FMname + "]: Found a Supervisor who is handling the LPM." + level2resource.getName());
                lpmSupervisor=level2resource.getName();
              }
              else {
                logger.info("[HCAL " + functionManager.FMname + "]: Found the Supervisor that is handling the LPM." + level2resource.getName());
                lpmSupervisor=level2resource.getName();
              }
            }
          }
        }
        logger.debug("[HCAL " + functionManager.FMname + "]: About to set the new MASKED_RESOURCES list.");
        functionManager.getHCALparameterSet().put(new FunctionManagerParameter<StringT>(HCALParameters.MASKED_RESOURCES, new StringT(allMaskedResources)));
        logger.info("[HCAL " + functionManager.FMname + "]: Just set the new MASKED_RESOURCES list.");
        logger.debug("[HCAL " + functionManager.FMname + "]: About to set the RU_INSTANCE.");
        functionManager.getHCALparameterSet().put(new FunctionManagerParameter<StringT>(HCALParameters.RU_INSTANCE, new StringT(ruInstance)));
        logger.info("[HCAL " + functionManager.FMname + "]: Just set the RU_INSTANCE to " + ruInstance);
        logger.debug("[HCAL " + functionManager.FMname + "]: About to set the LPM_SUPERVISOR.");
        functionManager.getHCALparameterSet().put(new FunctionManagerParameter<StringT>(HCALParameters.LPM_SUPERVISOR, new StringT(lpmSupervisor)));
        logger.info("[HCAL " + functionManager.FMname + "]: Just set the LPM_SUPERVISOR to " + lpmSupervisor);
      }
      catch (DBConnectorException ex) {
        logger.error("[HCAL " + functionManager.FMname + "]: Got a DBConnectorException when trying to retrieve level2s' children resources: " + ex.getMessage());
      }
    }
    if (!somebodysHandlingTA) logger.warn("[HCAL " + functionManager.FMname + "]: Got through the list of level2's but didn't find anybody to handle the triggeradapter! Bad...");

  }

}
