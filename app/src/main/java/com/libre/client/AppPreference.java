package com.libre.client;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class AppPreference {
	public static SharedPreferences PREF = null;


	public static boolean getKillProcessStatus() {
		return PREF != null && PREF.getBoolean("kill_process", true);
	}
	public static boolean HideGroupMembers() {
		return PREF != null && PREF.getBoolean("hide_dmr_list", true);

	}
	public static boolean HideDMRList() {
		return PREF != null && PREF.getBoolean("hide_dmr",true);

    }
    public static boolean ShowDMRintheLIST() {
        return PREF != null && PREF.getBoolean("dmr_text",false);

    }
    public static boolean ShowDMRRefreshButtomn() {
        return PREF != null && PREF.getBoolean("dmr_refresh",false);

    }
	public static int getMaxGroups() {
		return PREF != null ? Integer.valueOf(PREF.getString("max_ddms_groups", "5")) : 4;
	}

    public static boolean ShowExtesnions() {
        return PREF != null && PREF.getBoolean("ext", false);

    }
    public static boolean ShowErrorlogs() {
        return PREF != null && PREF.getBoolean("toast", false);

    }
    public static boolean ShowZoneCount() {
        return PREF != null && PREF.getBoolean("zone", true);

    }
    public static boolean ShowStationCount() {
        return PREF != null && PREF.getBoolean("station", true);

    }
    public static boolean ForceRemoveThePlaYList() {
        return PREF != null && PREF.getBoolean("force", false);

    }
	
}
