package com.nprotech.moneytracker.helper;

import android.content.Context;
import android.util.TypedValue;

import com.nprotech.moneytracker.R;

import java.util.ArrayList;
import java.util.List;

public class DataHelper {

    public static ArrayList<String> getColorList() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("#34BFFF");
        arrayList.add("#0097E6");
        arrayList.add("#0077C5");
        arrayList.add("#055393");
        arrayList.add("#008481");
        arrayList.add("#00A6A4");
        arrayList.add("#00C1BF");
        arrayList.add("#29B473");
        arrayList.add("#7FD000");
        arrayList.add("#FFCA00");
        arrayList.add("#FFBB00");
        arrayList.add("#FFAD00");
        arrayList.add("#FF8000");
        arrayList.add("#F95700");
        arrayList.add("#EE4036");
        arrayList.add("#D52B1E");
        arrayList.add("#B80000");
        arrayList.add("#9C005E");
        arrayList.add("#90278E");
        arrayList.add("#652D90");
        arrayList.add("#4E2B8F");
        arrayList.add("#6436AF");
        arrayList.add("#7A3DD8");
        arrayList.add("#9457FA");
        arrayList.add("#FF59CC");
        arrayList.add("#E31C9E");
        arrayList.add("#C9007A");
        arrayList.add("#810035");
        arrayList.add("#A52A2A");
        arrayList.add("#8B4513");
        arrayList.add("#7A5649");
        arrayList.add("#5E4138");
        arrayList.add("#424243");
        arrayList.add("#455A64");
        arrayList.add("#66757f");
        return arrayList;
    }

    public static String getDefaultCategory(Context context, int i) {
        return new String[]{
                getResourceString(context, R.string.category_bills),
                getResourceString(context, R.string.category_clothing),
                getResourceString(context, R.string.category_education),
                getResourceString(context, R.string.category_entertainment),
                getResourceString(context, R.string.category_fitness),
                getResourceString(context, R.string.category_food_and_beverages),
                getResourceString(context, R.string.category_gifts),
                getResourceString(context, R.string.category_health_and_beauty),
                getResourceString(context, R.string.category_furniture),
                getResourceString(context, R.string.category_pet),
                getResourceString(context, R.string.category_shopping),
                getResourceString(context, R.string.category_transportation),
                getResourceString(context, R.string.category_travel),
                getResourceString(context, R.string.category_others),

                getResourceString(context, R.string.category_allowance),
                getResourceString(context, R.string.category_award),
                getResourceString(context, R.string.category_bonus),
                getResourceString(context, R.string.category_dividend),
                getResourceString(context, R.string.category_investment),
                getResourceString(context, R.string.category_lottery),
                getResourceString(context, R.string.category_salary),
                getResourceString(context, R.string.category_tips),
                getResourceString(context, R.string.category_cashback),
                getResourceString(context, R.string.category_others),

                getResourceString(context, R.string.adjustment),
                getResourceString(context, R.string.loan),
                getResourceString(context, R.string.repay),
                getResourceString(context, R.string.debt),
                getResourceString(context, R.string.collect),
                getResourceString(context, R.string.fee),
                getResourceString(context, R.string.transfer)}[i - 1];
    }

    //----------------------
    //--- CATEGORY ICONS ---
    //----------------------
    public static List<Integer> getCategoryIcons() {
        List<Integer> arrayList = new ArrayList<>();
        arrayList.add(R.drawable.category_0);
        arrayList.add(R.drawable.category_1);
        arrayList.add(R.drawable.category_2);
        arrayList.add(R.drawable.category_3);
        arrayList.add(R.drawable.category_4);
        arrayList.add(R.drawable.category_5);
        arrayList.add(R.drawable.category_6);
        arrayList.add(R.drawable.category_7);
        arrayList.add(R.drawable.category_8);
        arrayList.add(R.drawable.category_9);
        arrayList.add(R.drawable.category_10);
        arrayList.add(R.drawable.category_11);
        arrayList.add(R.drawable.category_12);
        arrayList.add(R.drawable.category_13);
        arrayList.add(R.drawable.category_14);
        arrayList.add(R.drawable.category_15);
        arrayList.add(R.drawable.category_16);
        arrayList.add(R.drawable.category_17);
        arrayList.add(R.drawable.category_18);
        arrayList.add(R.drawable.category_19);
        arrayList.add(R.drawable.category_20);
        arrayList.add(R.drawable.category_21);
        arrayList.add(R.drawable.category_22);
        arrayList.add(R.drawable.category_23);
        arrayList.add(R.drawable.category_24);
        arrayList.add(R.drawable.category_25);
        arrayList.add(R.drawable.category_26);
        arrayList.add(R.drawable.category_27);
        arrayList.add(R.drawable.category_28);
        arrayList.add(R.drawable.category_29);
        arrayList.add(R.drawable.category_30);
        arrayList.add(R.drawable.category_31);
        arrayList.add(R.drawable.category_32);
        arrayList.add(R.drawable.category_33);
        arrayList.add(R.drawable.category_34);
        arrayList.add(R.drawable.category_35);
        arrayList.add(R.drawable.category_36);
        arrayList.add(R.drawable.category_37);
        arrayList.add(R.drawable.category_38);
        arrayList.add(R.drawable.category_39);
        arrayList.add(R.drawable.category_40);
        arrayList.add(R.drawable.category_41);
        arrayList.add(R.drawable.category_42);
        arrayList.add(R.drawable.category_43);
        arrayList.add(R.drawable.category_44);
        arrayList.add(R.drawable.category_45);
        arrayList.add(R.drawable.category_46);
        arrayList.add(R.drawable.category_47);
        arrayList.add(R.drawable.category_48);
        arrayList.add(R.drawable.category_49);
        arrayList.add(R.drawable.category_50);
        arrayList.add(R.drawable.category_51);
        arrayList.add(R.drawable.category_52);
        arrayList.add(R.drawable.category_53);
        arrayList.add(R.drawable.category_54);
        arrayList.add(R.drawable.category_55);
        arrayList.add(R.drawable.category_56);
        arrayList.add(R.drawable.category_57);
        arrayList.add(R.drawable.category_58);
        arrayList.add(R.drawable.category_59);
        arrayList.add(R.drawable.category_60);
        arrayList.add(R.drawable.category_61);
        arrayList.add(R.drawable.category_62);
        arrayList.add(R.drawable.category_63);
        arrayList.add(R.drawable.category_64);
        arrayList.add(R.drawable.category_65);
        arrayList.add(R.drawable.category_66);
        arrayList.add(R.drawable.category_67);
        arrayList.add(R.drawable.category_68);
        arrayList.add(R.drawable.category_69);
        arrayList.add(R.drawable.category_70);
        arrayList.add(R.drawable.category_71);
        arrayList.add(R.drawable.category_72);
        arrayList.add(R.drawable.category_73);
        arrayList.add(R.drawable.category_74);
        arrayList.add(R.drawable.category_75);
        arrayList.add(R.drawable.category_76);
        arrayList.add(R.drawable.category_77);
        arrayList.add(R.drawable.category_78);
        arrayList.add(R.drawable.category_79);
        arrayList.add(R.drawable.category_80);
        arrayList.add(R.drawable.category_81);
        arrayList.add(R.drawable.category_82);
        arrayList.add(R.drawable.category_83);
        arrayList.add(R.drawable.category_84);
        arrayList.add(R.drawable.category_85);
        arrayList.add(R.drawable.category_86);
        arrayList.add(R.drawable.category_87);
        arrayList.add(R.drawable.category_88);
        arrayList.add(R.drawable.category_89);
        arrayList.add(R.drawable.category_90);
        arrayList.add(R.drawable.category_91);
        arrayList.add(R.drawable.category_92);
        arrayList.add(R.drawable.category_93);
        arrayList.add(R.drawable.category_94);
        arrayList.add(R.drawable.category_95);
        arrayList.add(R.drawable.category_96);
        arrayList.add(R.drawable.category_97);
        arrayList.add(R.drawable.category_98);
        arrayList.add(R.drawable.category_99);
        arrayList.add(R.drawable.category_100);
        arrayList.add(R.drawable.category_101);
        arrayList.add(R.drawable.category_102);
        arrayList.add(R.drawable.category_103);
        arrayList.add(R.drawable.category_104);
        arrayList.add(R.drawable.category_105);
        arrayList.add(R.drawable.category_106);
        arrayList.add(R.drawable.category_107);
        arrayList.add(R.drawable.category_108);
        arrayList.add(R.drawable.category_109);
        arrayList.add(R.drawable.category_110);
        arrayList.add(R.drawable.category_111);
        arrayList.add(R.drawable.category_112);
        arrayList.add(R.drawable.category_113);
        arrayList.add(R.drawable.category_114);
        arrayList.add(R.drawable.category_115);
        arrayList.add(R.drawable.category_116);
        arrayList.add(R.drawable.category_117);
        arrayList.add(R.drawable.category_118);
        arrayList.add(R.drawable.category_119);
        arrayList.add(R.drawable.category_120);
        arrayList.add(R.drawable.category_121);
        arrayList.add(R.drawable.category_122);
        arrayList.add(R.drawable.category_123);
        arrayList.add(R.drawable.category_124);
        arrayList.add(R.drawable.category_125);
        arrayList.add(R.drawable.category_126);
        arrayList.add(R.drawable.category_127);
        arrayList.add(R.drawable.category_128);
        arrayList.add(R.drawable.category_129);
        arrayList.add(R.drawable.category_130);
        arrayList.add(R.drawable.category_131);
        arrayList.add(R.drawable.category_132);
        arrayList.add(R.drawable.category_133);
        arrayList.add(R.drawable.category_134);
        arrayList.add(R.drawable.category_135);
        arrayList.add(R.drawable.category_136);
        arrayList.add(R.drawable.category_137);
        arrayList.add(R.drawable.category_138);
        arrayList.add(R.drawable.category_139);
        arrayList.add(R.drawable.category_140);
        arrayList.add(R.drawable.category_141);
        arrayList.add(R.drawable.category_142);
        arrayList.add(R.drawable.category_143);
        arrayList.add(R.drawable.category_144);
        arrayList.add(R.drawable.category_145);
        arrayList.add(R.drawable.category_146);
        arrayList.add(R.drawable.category_147);
        arrayList.add(R.drawable.category_148);
        arrayList.add(R.drawable.category_149);
        arrayList.add(R.drawable.category_150);
        arrayList.add(R.drawable.category_151);
        arrayList.add(R.drawable.category_152);
        arrayList.add(R.drawable.category_153);
        arrayList.add(R.drawable.category_154);
        arrayList.add(R.drawable.category_155);
        arrayList.add(R.drawable.category_156);
        arrayList.add(R.drawable.category_157);
        arrayList.add(R.drawable.category_158);
        arrayList.add(R.drawable.category_159);
        arrayList.add(R.drawable.category_160);
        arrayList.add(R.drawable.category_161);
        arrayList.add(R.drawable.category_162);
        arrayList.add(R.drawable.category_163);
        arrayList.add(R.drawable.category_164);
        arrayList.add(R.drawable.ic_adjust);
        arrayList.add(R.drawable.ic_borrow);
        arrayList.add(R.drawable.ic_repay);
        arrayList.add(R.drawable.ic_lend);
        arrayList.add(R.drawable.ic_receive);
        arrayList.add(R.drawable.ic_fee);
        arrayList.add(R.drawable.ic_transfer);
        arrayList.add(R.drawable.ic_increase_borrow);
        arrayList.add(R.drawable.ic_increase_lend);
        arrayList.add(R.drawable.ic_cashback);
        return arrayList;
    }

    //----------------------
    //--- WALLET ICONS ---
    //----------------------
    public static List<Integer> getWalletIcons() {
        List<Integer> arrayList = new ArrayList<>();
        arrayList.add(R.drawable.wallet_0);
        arrayList.add(R.drawable.wallet_1);
        arrayList.add(R.drawable.wallet_2);
        arrayList.add(R.drawable.wallet_3);
        arrayList.add(R.drawable.wallet_4);
        arrayList.add(R.drawable.wallet_5);
        arrayList.add(R.drawable.wallet_6);
        arrayList.add(R.drawable.wallet_7);
        arrayList.add(R.drawable.wallet_8);
        arrayList.add(R.drawable.wallet_9);
        arrayList.add(R.drawable.wallet_10);
        arrayList.add(R.drawable.wallet_11);
        arrayList.add(R.drawable.wallet_12);
        arrayList.add(R.drawable.wallet_13);
        arrayList.add(R.drawable.wallet_14);
        arrayList.add(R.drawable.wallet_15);
        arrayList.add(R.drawable.wallet_16);
        arrayList.add(R.drawable.wallet_17);
        arrayList.add(R.drawable.wallet_18);
        arrayList.add(R.drawable.wallet_19);
        arrayList.add(R.drawable.wallet_20);
        arrayList.add(R.drawable.wallet_21);
        arrayList.add(R.drawable.wallet_22);
        arrayList.add(R.drawable.wallet_23);
        arrayList.add(R.drawable.wallet_24);
        arrayList.add(R.drawable.wallet_25);
        arrayList.add(R.drawable.wallet_26);
        arrayList.add(R.drawable.wallet_27);
        arrayList.add(R.drawable.wallet_28);
        arrayList.add(R.drawable.wallet_29);
        return arrayList;
    }

    private static String getResourceString(Context context, int id) {
        return context.getResources().getString(id);
    }

    public static int getAttributeColor(Context context, int resource) {
        try {
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(resource, typedValue, true);
            return typedValue.data;
        } catch (Exception e) {
            AppLogger.e(context.getClass(), "getAttributeColor", e);
            return R.color.white;
        }
    }
}