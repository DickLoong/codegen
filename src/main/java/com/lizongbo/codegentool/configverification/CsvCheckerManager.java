package com.lizongbo.codegentool.configverification;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.lizongbo.codegentool.configverification.tai.*;
import com.lizongbo.codegentool.configverification.tarena.*;
import com.lizongbo.codegentool.configverification.tassistant.*;
import com.lizongbo.codegentool.configverification.tbackpack.*;
import com.lizongbo.codegentool.configverification.tbaptization.*;
import com.lizongbo.codegentool.configverification.tbasetask.*;
import com.lizongbo.codegentool.configverification.tbeginner.*;
import com.lizongbo.codegentool.configverification.tblfight.*;
import com.lizongbo.codegentool.configverification.tblskill.*;
import com.lizongbo.codegentool.configverification.tblskillbuff.*;
import com.lizongbo.codegentool.configverification.tblstatus.*;
import com.lizongbo.codegentool.configverification.tchat.*;
import com.lizongbo.codegentool.configverification.tcomm.*;
import com.lizongbo.codegentool.configverification.tcomm4zoneserver.*;
import com.lizongbo.codegentool.configverification.tcommander.*;
import com.lizongbo.codegentool.configverification.tcompeterank.*;
import com.lizongbo.codegentool.configverification.tcompound.*;
import com.lizongbo.codegentool.configverification.tconstellation.*;
import com.lizongbo.codegentool.configverification.tcrossserverwar.*;
import com.lizongbo.codegentool.configverification.tdatacheck.*;
import com.lizongbo.codegentool.configverification.tdestroyrebel.*;
import com.lizongbo.codegentool.configverification.tdiscountsgiftbag.*;
import com.lizongbo.codegentool.configverification.tdrop.*;
import com.lizongbo.codegentool.configverification.tdungeonrecovery.*;
import com.lizongbo.codegentool.configverification.teffectsimplification.*;
import com.lizongbo.codegentool.configverification.teffectstate.*;
import com.lizongbo.codegentool.configverification.tequip.*;
import com.lizongbo.codegentool.configverification.texpedition.*;
import com.lizongbo.codegentool.configverification.tfccolor.*;
import com.lizongbo.codegentool.configverification.tfirstpay.*;
import com.lizongbo.codegentool.configverification.tfriend.*;
import com.lizongbo.codegentool.configverification.tgiftbagexchange.*;
import com.lizongbo.codegentool.configverification.tgroupon.*;
import com.lizongbo.codegentool.configverification.tguardbase.*;
import com.lizongbo.codegentool.configverification.tguideinfo.*;
import com.lizongbo.codegentool.configverification.timbigboss.*;
import com.lizongbo.codegentool.configverification.tinvestreward.*;
import com.lizongbo.codegentool.configverification.tinvitationcode.*;
import com.lizongbo.codegentool.configverification.titil.*;
import com.lizongbo.codegentool.configverification.tlegion.*;
import com.lizongbo.codegentool.configverification.tlight.*;
import com.lizongbo.codegentool.configverification.tlimittest.*;
import com.lizongbo.codegentool.configverification.tloading.*;
import com.lizongbo.codegentool.configverification.tloadingpopupwindow.*;
import com.lizongbo.codegentool.configverification.tloadingsevenday.*;
import com.lizongbo.codegentool.configverification.tlocale.*;
import com.lizongbo.codegentool.configverification.tlottery.*;
import com.lizongbo.codegentool.configverification.tlucky.*;
import com.lizongbo.codegentool.configverification.tmine.*;
import com.lizongbo.codegentool.configverification.tmodel.*;
import com.lizongbo.codegentool.configverification.tmusic.*;
import com.lizongbo.codegentool.configverification.tnewerreward.*;
import com.lizongbo.codegentool.configverification.toperateactivity.*;
import com.lizongbo.codegentool.configverification.toutputpreview.*;
import com.lizongbo.codegentool.configverification.tpay.*;
import com.lizongbo.codegentool.configverification.tpet.*;
import com.lizongbo.codegentool.configverification.tpopularpromotion.*;
import com.lizongbo.codegentool.configverification.tproto.*;
import com.lizongbo.codegentool.configverification.tpve.*;
import com.lizongbo.codegentool.configverification.trank.*;
import com.lizongbo.codegentool.configverification.trankedgame.*;
import com.lizongbo.codegentool.configverification.trankinggame.*;
import com.lizongbo.codegentool.configverification.tranklist.*;
import com.lizongbo.codegentool.configverification.trecommendcombination.*;
import com.lizongbo.codegentool.configverification.treqs.*;
import com.lizongbo.codegentool.configverification.tres.*;
import com.lizongbo.codegentool.configverification.trewardback.*;
import com.lizongbo.codegentool.configverification.trewardsystem.*;
import com.lizongbo.codegentool.configverification.troleadvanced.*;
import com.lizongbo.codegentool.configverification.tserver.*;
import com.lizongbo.codegentool.configverification.tset.*;
import com.lizongbo.codegentool.configverification.tshop.*;
import com.lizongbo.codegentool.configverification.tsocial.*;
import com.lizongbo.codegentool.configverification.tstarcraft.*;
import com.lizongbo.codegentool.configverification.tsystemnotice.*;
import com.lizongbo.codegentool.configverification.ttask.*;
import com.lizongbo.codegentool.configverification.tteamraid.*;
import com.lizongbo.codegentool.configverification.ttestcup.*;
import com.lizongbo.codegentool.configverification.ttimelimitbuffershop.*;
import com.lizongbo.codegentool.configverification.ttimelimitdiscount.*;
import com.lizongbo.codegentool.configverification.ttimelimitrecruit.*;
import com.lizongbo.codegentool.configverification.ttitle.*;
import com.lizongbo.codegentool.configverification.ttransform.*;
import com.lizongbo.codegentool.configverification.ttransformer.*;
import com.lizongbo.codegentool.configverification.tui.*;
import com.lizongbo.codegentool.configverification.tuser.*;
import com.lizongbo.codegentool.configverification.tuser4worldrecord.*;
import com.lizongbo.codegentool.configverification.tuser4zonerecord.*;
import com.lizongbo.codegentool.configverification.tuser4zoneserver.*;
import com.lizongbo.codegentool.configverification.tvip.*;
import com.lizongbo.codegentool.configverification.twarnmechanism.*;
import com.lizongbo.codegentool.configverification.twebadmin.*;
import com.lizongbo.codegentool.configverification.tworldconf.*;
import com.lizongbo.codegentool.configverification.tzidanchushoudian.*;
import com.lizongbo.codegentool.csv2db.*;

public class CsvCheckerManager {

	private static ConcurrentHashMap<String, CsvChecker> checkerMap = new ConcurrentHashMap<String, CsvChecker>();

	public static void initCheckerMap() {
		checkerMap.put("tshop_vipstore", new TshopVipstoreCsvChecker());
		checkerMap.put("trankedgame_trapgroup", new TrankedgameTrapgroupCsvChecker());
		checkerMap.put("tteamraid_tteamrevenue", new TteamraidTteamrevenueCsvChecker());
		checkerMap.put("toperateactivity_newserveractivity", new ToperateactivityNewserveractivityCsvChecker());
		checkerMap.put("trankedgame_transportunit", new TrankedgameTransportunitCsvChecker());
		checkerMap.put("troleadvanced_morion", new TroleadvancedMorionCsvChecker());
		checkerMap.put("tstarcraft_dantable", new TstarcraftDantableCsvChecker());
		checkerMap.put("tblskill_trigger", new TblskillTriggerCsvChecker());
		checkerMap.put("tassistant_skillgroup", new TassistantSkillgroupCsvChecker());
		checkerMap.put("tloadingpopupwindow_loadingpopupwindow", new TloadingpopupwindowLoadingpopupwindowCsvChecker());
		checkerMap.put("tmine_mineopen", new TmineMineopenCsvChecker());
		checkerMap.put("texpedition_expeditionsweep", new TexpeditionExpeditionsweepCsvChecker());
		checkerMap.put("tassistant_star", new TassistantStarCsvChecker());
		checkerMap.put("tui_resousourcepersistence", new TuiResousourcepersistenceCsvChecker());
		checkerMap.put("tconstellation_constellationlevel", new TconstellationConstellationlevelCsvChecker());
		checkerMap.put("tequip_equipuplevel", new TequipEquipuplevelCsvChecker());
		checkerMap.put("ttransform_starcraftplunder", new TtransformStarcraftplunderCsvChecker());
		checkerMap.put("tbasetask_basetasktype", new TbasetaskBasetasktypeCsvChecker());
		checkerMap.put("tcomm_errcode", new TcommErrcodeCsvChecker());
		checkerMap.put("tblskill_skillfc", new TblskillSkillfcCsvChecker());
		checkerMap.put("tcomm_mechaattribute", new TcommMechaattributeCsvChecker());
		checkerMap.put("tlottery_recruitequipmentodds", new TlotteryRecruitequipmentoddsCsvChecker());
		checkerMap.put("tpay_iosiapitem", new TpayIosiapitemCsvChecker());
		checkerMap.put("tarena_rankawardinfo", new TarenaRankawardinfoCsvChecker());
		checkerMap.put("tassistant_baptizecost", new TassistantBaptizecostCsvChecker());
		checkerMap.put("tpve_chapterlevel", new TpveChapterlevelCsvChecker());
		checkerMap.put("twebadmin_fullupdateversioncontrol", new TwebadminFullupdateversioncontrolCsvChecker());
		checkerMap.put("twebadmin_toperationsubaction", new TwebadminToperationsubactionCsvChecker());
		checkerMap.put("tblfight_fightclasscfg", new TblfightFightclasscfgCsvChecker());
		checkerMap.put("tdungeonrecovery_dungeonrecovery", new TdungeonrecoveryDungeonrecoveryCsvChecker());
		checkerMap.put("ttimelimitdiscount_timelimitdiscount", new TtimelimitdiscountTimelimitdiscountCsvChecker());
		checkerMap.put("tloading_loading", new TloadingLoadingCsvChecker());
		checkerMap.put("tcrossserverwar_resourceoutput", new TcrossserverwarResourceoutputCsvChecker());
		checkerMap.put("tcrossserverwar_groupearnings", new TcrossserverwarGroupearningsCsvChecker());
		checkerMap.put("treqs_effectreqinfo", new TreqsEffectreqinfoCsvChecker());
		checkerMap.put("tassistant_qualification", new TassistantQualificationCsvChecker());
		checkerMap.put("tmusic_ui", new TmusicUiCsvChecker());
		checkerMap.put("tchat_chatemotion", new TchatChatemotionCsvChecker());
		checkerMap.put("tmusic_volume", new TmusicVolumeCsvChecker());
		checkerMap.put("ttransformer_crystalpropinfo", new TtransformerCrystalpropinfoCsvChecker());
		checkerMap.put("tlimittest_tlimittest", new TlimittestTlimittestCsvChecker());
		checkerMap.put("titil_appinfo", new TitilAppinfoCsvChecker());
		checkerMap.put("tblfight_npcinfo", new TblfightNpcinfoCsvChecker());
		checkerMap.put("tblskill_effect4bulletinfo", new TblskillEffect4BulletinfoCsvChecker());
		checkerMap.put("tlocale_plot", new TlocalePlotCsvChecker());
		checkerMap.put("tai_aiparameter", new TaiAiparameterCsvChecker());
		checkerMap.put("trankedgame_trapbulletgroup", new TrankedgameTrapbulletgroupCsvChecker());
		checkerMap.put("tuser4worldrecord_orderrecordstatus", new Tuser4WorldrecordOrderrecordstatusCsvChecker());
		checkerMap.put("tvip_currencybuyprice", new TvipCurrencybuypriceCsvChecker());
		checkerMap.put("tblstatus_actionstatusmap", new TblstatusActionstatusmapCsvChecker());
		checkerMap.put("trewardback_energyguardian", new TrewardbackEnergyguardianCsvChecker());
		checkerMap.put("tlocale_zh", new TlocaleZhCsvChecker());
		checkerMap.put("trewardsystem_fightvaluereward", new TrewardsystemFightvaluerewardCsvChecker());
		checkerMap.put("tuser_reginfo", new TuserReginfoCsvChecker());
		checkerMap.put("tarena_robotconf", new TarenaRobotconfCsvChecker());
		checkerMap.put("tuser4worldrecord_taiqitransnotify", new Tuser4WorldrecordTaiqitransnotifyCsvChecker());
		checkerMap.put("trankedgame_trapbullet", new TrankedgameTrapbulletCsvChecker());
		checkerMap.put("tset_set", new TsetSetCsvChecker());
		checkerMap.put("ttransformer_levelinfo", new TtransformerLevelinfoCsvChecker());
		checkerMap.put("tres_battlegoods", new TresBattlegoodsCsvChecker());
		checkerMap.put("trankinggame_rankinggame", new TrankinggameRankinggameCsvChecker());
		checkerMap.put("tblskill_animationinfo", new TblskillAnimationinfoCsvChecker());
		checkerMap.put("titil_redisserverinfo", new TitilRedisserverinfoCsvChecker());
		checkerMap.put("trankedgame_rankedlevel", new TrankedgameRankedlevelCsvChecker());
		checkerMap.put("tarena_matchrange", new TarenaMatchrangeCsvChecker());
		checkerMap.put("twebadmin_clientversion", new TwebadminClientversionCsvChecker());
		checkerMap.put("trewardsystem_physicalpowerreward", new TrewardsystemPhysicalpowerrewardCsvChecker());
		checkerMap.put("trewardsystem_signin", new TrewardsystemSigninCsvChecker());
		checkerMap.put("tuser4zonerecord_schedulejobrecord", new Tuser4ZonerecordSchedulejobrecordCsvChecker());
		checkerMap.put("tserver_warzone", new TserverWarzoneCsvChecker());
		checkerMap.put("tblskill_animationlength", new TblskillAnimationlengthCsvChecker());
		checkerMap.put("tres_levelinfo", new TresLevelinfoCsvChecker());
		checkerMap.put("tfirstpay_firstpay", new TfirstpayFirstpayCsvChecker());
		checkerMap.put("troleadvanced_advancedeffect", new TroleadvancedAdvancedeffectCsvChecker());
		checkerMap.put("trewardsystem_energydiamonds", new TrewardsystemEnergydiamondsCsvChecker());
		checkerMap.put("tarena_awardtitle", new TarenaAwardtitleCsvChecker());
		checkerMap.put("trewardback_rewardtype", new TrewardbackRewardtypeCsvChecker());
		checkerMap.put("tequip_failodds", new TequipFailoddsCsvChecker());
		checkerMap.put("trankinggame_playerattribute", new TrankinggamePlayerattributeCsvChecker());
		checkerMap.put("tpve_starrequst", new TpveStarrequstCsvChecker());
		checkerMap.put("tpve_stationbackground", new TpveStationbackgroundCsvChecker());
		checkerMap.put("teffectstate_effectstatemap", new TeffectstateEffectstatemapCsvChecker());
		checkerMap.put("tblskill_bulletinfo", new TblskillBulletinfoCsvChecker());
		checkerMap.put("tlight_tmodelinpvedialog", new TlightTmodelinpvedialogCsvChecker());
		checkerMap.put("trewardback_colosseum", new TrewardbackColosseumCsvChecker());
		checkerMap.put("tinvestreward_investreward", new TinvestrewardInvestrewardCsvChecker());
		checkerMap.put("tproto_datanotfiy", new TprotoDatanotfiyCsvChecker());
		checkerMap.put("tuser4worldrecord_schedulejobrecord", new Tuser4WorldrecordSchedulejobrecordCsvChecker());
		checkerMap.put("tgroupon_addrechargenum", new TgrouponAddrechargenumCsvChecker());
		checkerMap.put("tserver_gamezone", new TserverGamezoneCsvChecker());
		checkerMap.put("tmusic_music", new TmusicMusicCsvChecker());
		checkerMap.put("tuser4worldrecord_codepackfetchrecord", new Tuser4WorldrecordCodepackfetchrecordCsvChecker());
		checkerMap.put("tdestroyrebel_rebelforceslevel", new TdestroyrebelRebelforceslevelCsvChecker());
		checkerMap.put("ttransformer_resonantinfo", new TtransformerResonantinfoCsvChecker());
		checkerMap.put("tcomm_counter", new TcommCounterCsvChecker());
		checkerMap.put("tmine_intimacyplus", new TmineIntimacyplusCsvChecker());
		checkerMap.put("toperateactivity_preposition", new ToperateactivityPrepositionCsvChecker());
		checkerMap.put("tcomm_channel", new TcommChannelCsvChecker());
		checkerMap.put("tmodel_effectmaininfo", new TmodelEffectmaininfoCsvChecker());
		checkerMap.put("tmusic_assistantvolume", new TmusicAssistantvolumeCsvChecker());
		checkerMap.put("troleadvanced_reformadvanced", new TroleadvancedReformadvancedCsvChecker());
		checkerMap.put("tmodel_actionreqinfo", new TmodelActionreqinfoCsvChecker());
		checkerMap.put("tassistant_assistant", new TassistantAssistantCsvChecker());
		checkerMap.put("tuser4worldrecord_codepack", new Tuser4WorldrecordCodepackCsvChecker());
		checkerMap.put("tres_levelmap", new TresLevelmapCsvChecker());
		checkerMap.put("tfriend_friend", new TfriendFriendCsvChecker());
		checkerMap.put("tgiftbagexchange_giftbagexchange", new TgiftbagexchangeGiftbagexchangeCsvChecker());
		checkerMap.put("tzidanchushoudian_chushoudian", new TzidanchushoudianChushoudianCsvChecker());
		checkerMap.put("trecommendcombination_recommendcombination", new TrecommendcombinationRecommendcombinationCsvChecker());
		checkerMap.put("tcomm_clientchangelog", new TcommClientchangelogCsvChecker());
		checkerMap.put("teffectsimplification_effectsimplification", new TeffectsimplificationEffectsimplificationCsvChecker());
		checkerMap.put("tpet_followset", new TpetFollowsetCsvChecker());
		checkerMap.put("tstarcraft_rankingreward", new TstarcraftRankingrewardCsvChecker());
		checkerMap.put("tmine_transformation", new TmineTransformationCsvChecker());
		checkerMap.put("tuser4zoneserver_gameroleinfo", new Tuser4ZoneserverGameroleinfoCsvChecker());
		checkerMap.put("tblstatus_statuscondition", new TblstatusStatusconditionCsvChecker());
		checkerMap.put("tmodel_commoneffectinfo", new TmodelCommoneffectinfoCsvChecker());
		checkerMap.put("tequip_baptizetype", new TequipBaptizetypeCsvChecker());
		checkerMap.put("tteamraid_tminions", new TteamraidTminionsCsvChecker());
		checkerMap.put("tblskill_targetcond", new TblskillTargetcondCsvChecker());
		checkerMap.put("titil_serverinfo", new TitilServerinfoCsvChecker());
		checkerMap.put("trankinggame_danreward", new TrankinggameDanrewardCsvChecker());
		checkerMap.put("tnewerreward_newerintegral", new TnewerrewardNewerintegralCsvChecker());
		checkerMap.put("ttimelimitrecruit_baseconfig", new TtimelimitrecruitBaseconfigCsvChecker());
		checkerMap.put("troleadvanced_rolepassive", new TroleadvancedRolepassiveCsvChecker());
		checkerMap.put("tlottery_makeequipment", new TlotteryMakeequipmentCsvChecker());
		checkerMap.put("ttransformer_enchantinfo", new TtransformerEnchantinfoCsvChecker());
		checkerMap.put("tlight_tmodelpos", new TlightTmodelposCsvChecker());
		checkerMap.put("tuser_pwdinfo", new TuserPwdinfoCsvChecker());
		checkerMap.put("tnewerreward_newerrewardcondition", new TnewerrewardNewerrewardconditionCsvChecker());
		checkerMap.put("tuser_gameuser", new TuserGameuserCsvChecker());
		checkerMap.put("trewardsystem_onlinereward", new TrewardsystemOnlinerewardCsvChecker());
		checkerMap.put("tlight_tmodelintask", new TlightTmodelintaskCsvChecker());
		checkerMap.put("tuser4worldrecord_taiqiorder", new Tuser4WorldrecordTaiqiorderCsvChecker());
		checkerMap.put("ttransform_bossbossmodel1", new TtransformBossbossmodel1CsvChecker());
		checkerMap.put("ttransform_bossbossmodel2", new TtransformBossbossmodel2CsvChecker());
		checkerMap.put("tcrossserverwar_bossattribute", new TcrossserverwarBossattributeCsvChecker());
		checkerMap.put("tmine_mineyield", new TmineMineyieldCsvChecker());
		checkerMap.put("trankedgame_trapconfig", new TrankedgameTrapconfigCsvChecker());
		checkerMap.put("ttimelimitrecruit_box", new TtimelimitrecruitBoxCsvChecker());
		checkerMap.put("twebadmin_activitiesschedule", new TwebadminActivitiesscheduleCsvChecker());
		checkerMap.put("tcomm_attrmapping", new TcommAttrmappingCsvChecker());
		checkerMap.put("tproto_syncdata", new TprotoSyncdataCsvChecker());
		checkerMap.put("tcompeterank_levelrank", new TcompeterankLevelrankCsvChecker());
		checkerMap.put("tuser4zoneserver_gameplayer", new Tuser4ZoneserverGameplayerCsvChecker());
		checkerMap.put("tguideinfo_functionopen", new TguideinfoFunctionopenCsvChecker());
		checkerMap.put("twarnmechanism_warntype", new TwarnmechanismWarntypeCsvChecker());
		checkerMap.put("tassistant_skillscore", new TassistantSkillscoreCsvChecker());
		checkerMap.put("tmine_mysteriousmine", new TmineMysteriousmineCsvChecker());
		checkerMap.put("ttimelimitrecruit_timesreward", new TtimelimitrecruitTimesrewardCsvChecker());
		checkerMap.put("tblskillbuff_type", new TblskillbuffTypeCsvChecker());
		checkerMap.put("tmodel_actiondescinfo", new TmodelActiondescinfoCsvChecker());
		checkerMap.put("tlegion_armygrouprankreward", new TlegionArmygrouprankrewardCsvChecker());
		checkerMap.put("toperateactivity_mouthcard", new ToperateactivityMouthcardCsvChecker());
		checkerMap.put("tbackpack_itemparam", new TbackpackItemparamCsvChecker());
		checkerMap.put("tlegion_answer", new TlegionAnswerCsvChecker());
		checkerMap.put("tlegion_questionbank", new TlegionQuestionbankCsvChecker());
		checkerMap.put("tuser4worldrecord_testrewardrecord", new Tuser4WorldrecordTestrewardrecordCsvChecker());
		checkerMap.put("tnewerreward_newerloadingreward", new TnewerrewardNewerloadingrewardCsvChecker());
		checkerMap.put("tsocial_mailattachment", new TsocialMailattachmentCsvChecker());
		checkerMap.put("tcompeterank_levelreward", new TcompeterankLevelrewardCsvChecker());
		checkerMap.put("toperateactivity_activitycondition", new ToperateactivityActivityconditionCsvChecker());
		checkerMap.put("tcrossserverwar_attenuationrule", new TcrossserverwarAttenuationruleCsvChecker());
		checkerMap.put("tinvitationcode_invitationcode", new TinvitationcodeInvitationcodeCsvChecker());
		checkerMap.put("tdestroyrebel_rebelforces", new TdestroyrebelRebelforcesCsvChecker());
		checkerMap.put("ttestcup_worldcup", new TtestcupWorldcupCsvChecker());
		checkerMap.put("tcompeterank_fightvaluerank", new TcompeterankFightvaluerankCsvChecker());
		checkerMap.put("tbeginner_tbeginner", new TbeginnerTbeginnerCsvChecker());
		checkerMap.put("trankinggame_activereward", new TrankinggameActiverewardCsvChecker());
		checkerMap.put("tui_componentdefineinfo", new TuiComponentdefineinfoCsvChecker());
		checkerMap.put("timbigboss_ranktable", new TimbigbossRanktableCsvChecker());
		checkerMap.put("twebadmin_startupactivityschedule", new TwebadminStartupactivityscheduleCsvChecker());
		checkerMap.put("tcrossserverwar_specialresource", new TcrossserverwarSpecialresourceCsvChecker());
		checkerMap.put("tmine_minebasics", new TmineMinebasicsCsvChecker());
		checkerMap.put("ttransformer_skilltriggerinfo", new TtransformerSkilltriggerinfoCsvChecker());
		checkerMap.put("tmodel_nodedisplaysetting", new TmodelNodedisplaysettingCsvChecker());
		checkerMap.put("tcommander_commanderlevel", new TcommanderCommanderlevelCsvChecker());
		checkerMap.put("tguideinfo_guidetrigger", new TguideinfoGuidetriggerCsvChecker());
		checkerMap.put("tnewerreward_newerreward", new TnewerrewardNewerrewardCsvChecker());
		checkerMap.put("tcomm4zoneserver_commconf", new Tcomm4ZoneserverCommconfCsvChecker());
		checkerMap.put("timbigboss_bossattribute", new TimbigbossBossattributeCsvChecker());
		checkerMap.put("ttransformer_crystalinlay", new TtransformerCrystalinlayCsvChecker());
		checkerMap.put("tcompound_compound", new TcompoundCompoundCsvChecker());
		checkerMap.put("tlucky_lucky", new TluckyLuckyCsvChecker());
		checkerMap.put("ttransform_bosswindow", new TtransformBosswindowCsvChecker());
		checkerMap.put("tcrossserverwar_crossserverwar", new TcrossserverwarCrossserverwarCsvChecker());
		checkerMap.put("tdrop_drop", new TdropDropCsvChecker());
		checkerMap.put("tlocale_collect", new TlocaleCollectCsvChecker());
		checkerMap.put("tchat_chatsmallemotion", new TchatChatsmallemotionCsvChecker());
		checkerMap.put("tbasetask_npc", new TbasetaskNpcCsvChecker());
		checkerMap.put("twarnmechanism_warn", new TwarnmechanismWarnCsvChecker());
		checkerMap.put("timbigboss_bosswin", new TimbigbossBosswinCsvChecker());
		checkerMap.put("tpet_petinfo", new TpetPetinfoCsvChecker());
		checkerMap.put("tcomm_gmcommandwhitelist", new TcommGmcommandwhitelistCsvChecker());
		checkerMap.put("tbackpack_giftpack", new TbackpackGiftpackCsvChecker());
		checkerMap.put("tequip_equipquality", new TequipEquipqualityCsvChecker());
		checkerMap.put("ttimelimitdiscount_timelimitdiscounttime", new TtimelimitdiscountTimelimitdiscounttimeCsvChecker());
		checkerMap.put("tloading_referroleinfo", new TloadingReferroleinfoCsvChecker());
		checkerMap.put("ttransform_headview", new TtransformHeadviewCsvChecker());
		checkerMap.put("tblfight_roleinfo", new TblfightRoleinfoCsvChecker());
		checkerMap.put("tpve_maplevel", new TpveMaplevelCsvChecker());
		checkerMap.put("toutputpreview_outputpreview", new ToutputpreviewOutputpreviewCsvChecker());
		checkerMap.put("twebadmin_toperationactiontype", new TwebadminToperationactiontypeCsvChecker());
		checkerMap.put("tdatacheck_sqlchecker", new TdatacheckSqlcheckerCsvChecker());
		checkerMap.put("tbaptization_aptitudesuit", new TbaptizationAptitudesuitCsvChecker());
		checkerMap.put("tteamraid_tteamraid", new TteamraidTteamraidCsvChecker());
		checkerMap.put("ttimelimitrecruit_odds", new TtimelimitrecruitOddsCsvChecker());
		checkerMap.put("tblfight_gobalcfg", new TblfightGobalcfgCsvChecker());
		checkerMap.put("troleadvanced_forge", new TroleadvancedForgeCsvChecker());
		checkerMap.put("tlegion_healthdegree", new TlegionHealthdegreeCsvChecker());
		checkerMap.put("tbackpack_item", new TbackpackItemCsvChecker());
		checkerMap.put("tuser_taiqiuser", new TuserTaiqiuserCsvChecker());
		checkerMap.put("toperateactivity_rechargerebate", new ToperateactivityRechargerebateCsvChecker());
		checkerMap.put("twebadmin_minioniplist", new TwebadminMinioniplistCsvChecker());
		checkerMap.put("tuser4worldrecord_iospaylog", new Tuser4WorldrecordIospaylogCsvChecker());
		checkerMap.put("twebadmin_updatenotice", new TwebadminUpdatenoticeCsvChecker());
		checkerMap.put("tui_windowinfo", new TuiWindowinfoCsvChecker());
		checkerMap.put("tequip_affixbaptize", new TequipAffixbaptizeCsvChecker());
		checkerMap.put("trewardback_arena", new TrewardbackArenaCsvChecker());
		checkerMap.put("toperateactivity_consumerrebate", new ToperateactivityConsumerrebateCsvChecker());
		checkerMap.put("tcomm_schedulejobconfig", new TcommSchedulejobconfigCsvChecker());
		checkerMap.put("tequip_tequip", new TequipTequipCsvChecker());
		checkerMap.put("twebadmin_gamezonehaltcontrol", new TwebadminGamezonehaltcontrolCsvChecker());
		checkerMap.put("tfriend_frienddegree", new TfriendFrienddegreeCsvChecker());
		checkerMap.put("tassistant_level", new TassistantLevelCsvChecker());
		checkerMap.put("ttransform_guildboss", new TtransformGuildbossCsvChecker());
		checkerMap.put("tlegion_basetable", new TlegionBasetableCsvChecker());
		checkerMap.put("titil_idcinfo", new TitilIdcinfoCsvChecker());
		checkerMap.put("tconstellation_constellationcrit", new TconstellationConstellationcritCsvChecker());
		checkerMap.put("trewardback_hostagerescue", new TrewardbackHostagerescueCsvChecker());
		checkerMap.put("toperateactivity_buyreward", new ToperateactivityBuyrewardCsvChecker());
		checkerMap.put("tblfight_damageenlarge", new TblfightDamageenlargeCsvChecker());
		checkerMap.put("tcomm_playeractivity", new TcommPlayeractivityCsvChecker());
		checkerMap.put("tlegion_positionchart", new TlegionPositionchartCsvChecker());
		checkerMap.put("tlottery_recruit", new TlotteryRecruitCsvChecker());
		checkerMap.put("tlottery_recruitmakeequippreview", new TlotteryRecruitmakeequippreviewCsvChecker());
		checkerMap.put("tpopularpromotion_shopslot", new TpopularpromotionShopslotCsvChecker());
		checkerMap.put("tbasetask_basetask", new TbasetaskBasetaskCsvChecker());
		checkerMap.put("timbigboss_basictable", new TimbigbossBasictableCsvChecker());
		checkerMap.put("tshop_shop", new TshopShopCsvChecker());
		checkerMap.put("tpet_petproinfo", new TpetPetproinfoCsvChecker());
		checkerMap.put("tstarcraft_replaceprice", new TstarcraftReplacepriceCsvChecker());
		checkerMap.put("twebadmin_tbannedrecord", new TwebadminTbannedrecordCsvChecker());
		checkerMap.put("tstarcraft_loottable", new TstarcraftLoottableCsvChecker());
		checkerMap.put("tblfight_bossbottle", new TblfightBossbottleCsvChecker());
		checkerMap.put("ttask_taskconfig", new TtaskTaskconfigCsvChecker());
		checkerMap.put("tmodel_effectreqinfo", new TmodelEffectreqinfoCsvChecker());
		checkerMap.put("tsystemnotice_systemnotice", new TsystemnoticeSystemnoticeCsvChecker());
		checkerMap.put("tlegion_train", new TlegionTrainCsvChecker());
		checkerMap.put("tcrossserverwar_rankreward", new TcrossserverwarRankrewardCsvChecker());
		checkerMap.put("ttask_tasktype", new TtaskTasktypeCsvChecker());
		checkerMap.put("tblskill_attackarea", new TblskillAttackareaCsvChecker());
		checkerMap.put("tcomm_commconf", new TcommCommconfCsvChecker());
		checkerMap.put("tpve_gatelevel", new TpveGatelevelCsvChecker());
		checkerMap.put("tlimittest_limitactivity", new TlimittestLimitactivityCsvChecker());
		checkerMap.put("tassistant_passiveskillunlock", new TassistantPassiveskillunlockCsvChecker());
		checkerMap.put("tguardbase_scenebuff", new TguardbaseScenebuffCsvChecker());
		checkerMap.put("tmodel_effectzhanshiinfo", new TmodelEffectzhanshiinfoCsvChecker());
		checkerMap.put("tuser_guestinfo", new TuserGuestinfoCsvChecker());
		checkerMap.put("trank_rank", new TrankRankCsvChecker());
		checkerMap.put("twebadmin_serveronlinestatussimple", new TwebadminServeronlinestatussimpleCsvChecker());
		checkerMap.put("tlegion_donationform", new TlegionDonationformCsvChecker());
		checkerMap.put("tranklist_worshipreward", new TranklistWorshiprewardCsvChecker());
		checkerMap.put("toperateactivity_activityopen", new ToperateactivityActivityopenCsvChecker());
		checkerMap.put("ttransform_team", new TtransformTeamCsvChecker());
		checkerMap.put("tarena_conf", new TarenaConfCsvChecker());
		checkerMap.put("timbigboss_playerattribute", new TimbigbossPlayerattributeCsvChecker());
		checkerMap.put("tranklist_ranklist", new TranklistRanklistCsvChecker());
		checkerMap.put("tarena_luckyreward", new TarenaLuckyrewardCsvChecker());
		checkerMap.put("tstarcraft_yieldtable", new TstarcraftYieldtableCsvChecker());
		checkerMap.put("tgroupon_groupon", new TgrouponGrouponCsvChecker());
		checkerMap.put("ttransformer_starinfo", new TtransformerStarinfoCsvChecker());
		checkerMap.put("tequip_resonancelevel", new TequipResonancelevelCsvChecker());
		checkerMap.put("toperateactivity_activetype", new ToperateactivityActivetypeCsvChecker());
		checkerMap.put("tuser_resetpwdlog", new TuserResetpwdlogCsvChecker());
		checkerMap.put("ttitle_title", new TtitleTitleCsvChecker());
		checkerMap.put("ttransform_pvewin", new TtransformPvewinCsvChecker());
		checkerMap.put("tbasetask_basetaskpoints", new TbasetaskBasetaskpointsCsvChecker());
		checkerMap.put("ttransform_trial", new TtransformTrialCsvChecker());
		checkerMap.put("tblskill_skillinfo", new TblskillSkillinfoCsvChecker());
		checkerMap.put("tlottery_itembag", new TlotteryItembagCsvChecker());
		checkerMap.put("tres_scenegroundbox", new TresScenegroundboxCsvChecker());
		checkerMap.put("trankinggame_everydayrankreward", new TrankinggameEverydayrankrewardCsvChecker());
		checkerMap.put("tequip_afficxsuit", new TequipAfficxsuitCsvChecker());
		checkerMap.put("tnewerreward_newerrewardopen", new TnewerrewardNewerrewardopenCsvChecker());
		checkerMap.put("trewardback_armynuclei", new TrewardbackArmynucleiCsvChecker());
		checkerMap.put("tblstatus_statusinfo", new TblstatusStatusinfoCsvChecker());
		checkerMap.put("tequip_resonancestarlevel", new TequipResonancestarlevelCsvChecker());
		checkerMap.put("tshop_itembox", new TshopItemboxCsvChecker());
		checkerMap.put("texpedition_expedition", new TexpeditionExpeditionCsvChecker());
		checkerMap.put("tsocial_mail", new TsocialMailCsvChecker());
		checkerMap.put("tbasetask_basetaskreward", new TbasetaskBasetaskrewardCsvChecker());
		checkerMap.put("tworldconf_worldconf", new TworldconfWorldconfCsvChecker());
		checkerMap.put("toperateactivity_growthfund", new ToperateactivityGrowthfundCsvChecker());
		checkerMap.put("tcomm_activereward", new TcommActiverewardCsvChecker());
		checkerMap.put("timbigboss_aibosscoefficient", new TimbigbossAibosscoefficientCsvChecker());
		checkerMap.put("tui_fixedjumppointmapping", new TuiFixedjumppointmappingCsvChecker());
		checkerMap.put("tloadingsevenday_loadingsevenday", new TloadingsevendayLoadingsevendayCsvChecker());
		checkerMap.put("tconstellation_constellationslist", new TconstellationConstellationslistCsvChecker());
		checkerMap.put("titil_processinfo", new TitilProcessinfoCsvChecker());
		checkerMap.put("tlegion_armygroupmemberrewad", new TlegionArmygroupmemberrewadCsvChecker());
		checkerMap.put("trewardsystem_levelreward", new TrewardsystemLevelrewardCsvChecker());
		checkerMap.put("toperateactivity_facebookactivity", new ToperateactivityFacebookactivityCsvChecker());
		checkerMap.put("tblskillbuff_buff", new TblskillbuffBuffCsvChecker());
		checkerMap.put("tequip_baptizeintervaodds", new TequipBaptizeintervaoddsCsvChecker());
		checkerMap.put("tguardbase_guardbase", new TguardbaseGuardbaseCsvChecker());
		checkerMap.put("tlimittest_bossattribute", new TlimittestBossattributeCsvChecker());
		checkerMap.put("tcompeterank_fightvaluereward", new TcompeterankFightvaluerewardCsvChecker());
		checkerMap.put("trewardback_intrusioncarrier", new TrewardbackIntrusioncarrierCsvChecker());
		checkerMap.put("tassistant_passiveskill", new TassistantPassiveskillCsvChecker());
		checkerMap.put("tstarcraft_basetable", new TstarcraftBasetableCsvChecker());
		checkerMap.put("ttransformer_chipmixinfo", new TtransformerChipmixinfoCsvChecker());
		checkerMap.put("tcomm_moduleinfo", new TcommModuleinfoCsvChecker());
		checkerMap.put("trewardsystem_signindayreward", new TrewardsystemSignindayrewardCsvChecker());
		checkerMap.put("tdestroyrebel_rebelforcesactivity", new TdestroyrebelRebelforcesactivityCsvChecker());
		checkerMap.put("tsocial_mailtemplate", new TsocialMailtemplateCsvChecker());
		checkerMap.put("trankedgame_energypackage", new TrankedgameEnergypackageCsvChecker());
		checkerMap.put("tcomm_logicsceneinfo", new TcommLogicsceneinfoCsvChecker());
		checkerMap.put("tui_componentdesc", new TuiComponentdescCsvChecker());
		checkerMap.put("tassistant_baptizeodds", new TassistantBaptizeoddsCsvChecker());
		checkerMap.put("ttitle_ranktitle", new TtitleRanktitleCsvChecker());
		checkerMap.put("tai_gobalcfg", new TaiGobalcfgCsvChecker());
		checkerMap.put("tarena_toprankawardinfo", new TarenaToprankawardinfoCsvChecker());
		checkerMap.put("tpve_station", new TpveStationCsvChecker());
		checkerMap.put("twebadmin_serveronlinestatussnapshot", new TwebadminServeronlinestatussnapshotCsvChecker());
		checkerMap.put("tcomm_moneyenumeration", new TcommMoneyenumerationCsvChecker());
		checkerMap.put("tlegion_armygroupcrystallucleus", new TlegionArmygroupcrystallucleusCsvChecker());
		checkerMap.put("tguardbase_guardbaserank", new TguardbaseGuardbaserankCsvChecker());
		checkerMap.put("tpopularpromotion_itemodds", new TpopularpromotionItemoddsCsvChecker());
		checkerMap.put("ttimelimitbuffershop_bufferconfig", new TtimelimitbuffershopBufferconfigCsvChecker());
		checkerMap.put("twebadmin_minionlist", new TwebadminMinionlistCsvChecker());
		checkerMap.put("tui_componentcommon", new TuiComponentcommonCsvChecker());
		checkerMap.put("troleadvanced_explore", new TroleadvancedExploreCsvChecker());
		checkerMap.put("toperateactivity_everydayrecharge", new ToperateactivityEverydayrechargeCsvChecker());
		checkerMap.put("tres_goblinmap", new TresGoblinmapCsvChecker());
		checkerMap.put("tblfight_fightdata", new TblfightFightdataCsvChecker());
		checkerMap.put("tmine_attrattenuation", new TmineAttrattenuationCsvChecker());
		checkerMap.put("tbaptization_baptizeaptitude", new TbaptizationBaptizeaptitudeCsvChecker());
		checkerMap.put("tlight_tlight", new TlightTlightCsvChecker());
		checkerMap.put("tlegion_bosslevel", new TlegionBosslevelCsvChecker());
		checkerMap.put("tguideinfo_guideinfo", new TguideinfoGuideinfoCsvChecker());
		checkerMap.put("tset_name", new TsetNameCsvChecker());
		checkerMap.put("tproto_cmd", new TprotoCmdCsvChecker());
		checkerMap.put("tlegion_armygroupboss", new TlegionArmygroupbossCsvChecker());
		checkerMap.put("tres_gameaudio", new TresGameaudioCsvChecker());
		checkerMap.put("tbaptization_attributeweight", new TbaptizationAttributeweightCsvChecker());
		checkerMap.put("tuser4worldrecord_iosverifylog", new Tuser4WorldrecordIosverifylogCsvChecker());
		checkerMap.put("tcrossserverwar_expreward", new TcrossserverwarExprewardCsvChecker());
		checkerMap.put("timbigboss_bossopen", new TimbigbossBossopenCsvChecker());
		checkerMap.put("ttimelimitrecruit_show", new TtimelimitrecruitShowCsvChecker());
		checkerMap.put("tmodel_effectreqdetail", new TmodelEffectreqdetailCsvChecker());
		checkerMap.put("toperateactivity_newserverintegral", new ToperateactivityNewserverintegralCsvChecker());
		checkerMap.put("tcrossserverwar_copyreward", new TcrossserverwarCopyrewardCsvChecker());
		checkerMap.put("tfccolor_fccolor", new TfccolorFccolorCsvChecker());
		checkerMap.put("tdiscountsgiftbag_tdiscountsgiftbag", new TdiscountsgiftbagTdiscountsgiftbagCsvChecker());
		checkerMap.put("tuser_logininfo", new TuserLogininfoCsvChecker());
		checkerMap.put("tteamraid_tteamcopytype", new TteamraidTteamcopytypeCsvChecker());
		checkerMap.put("tblskill_targetselect", new TblskillTargetselectCsvChecker());
		checkerMap.put("tequip_equipstar", new TequipEquipstarCsvChecker());
		checkerMap.put("tbackpack_itemeffect", new TbackpackItemeffectCsvChecker());
		checkerMap.put("twebadmin_toperationactionschedule", new TwebadminToperationactionscheduleCsvChecker());
		checkerMap.put("tarena_pointaward", new TarenaPointawardCsvChecker());
		checkerMap.put("ttransformer_propertyinfo", new TtransformerPropertyinfoCsvChecker());
		checkerMap.put("tvip_levelinfo", new TvipLevelinfoCsvChecker());
		checkerMap.put("trankedgame_buffconfig", new TrankedgameBuffconfigCsvChecker());
		checkerMap.put("twebadmin_recommandserverqueue", new TwebadminRecommandserverqueueCsvChecker());

	}

	public static Map<String, List<String>> errorMap = new HashMap<String, List<String>>();

	public static void CheckCsv(String csvPath) {
		File csvFile = new File(csvPath);
		String tableName = CSVUtil.getTableNameFromCSVFile(csvFile.getAbsolutePath());

		CsvChecker cc = checkerMap.get(tableName);
		System.err.println(tableName + "|" + cc);
		if (cc != null) {
			List<String> list = cc.checkCsv(csvPath);
			if (list != null && list.size() > 0) {
				errorMap.put(csvPath, list);
			}
		}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
