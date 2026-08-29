package com.nprotech.moneytracker.helper;

import android.content.Context;
import android.util.TypedValue;

import com.nprotech.moneytracker.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataHelper {

    public static ArrayList<String> getColorList() {
        ArrayList<String> colors = new ArrayList<>();

        // Default (Recommended)
        colors.add("#6A1B9A");

        // Purple
        colors.add("#4A148C");
        colors.add("#7B1FA2");
        colors.add("#8E24AA");
        colors.add("#AB47BC");
        colors.add("#BA68C8");
        colors.add("#9575CD");

        // Indigo
        colors.add("#283593");
        colors.add("#303F9F");
        colors.add("#3949AB");
        colors.add("#3F51B5");
        colors.add("#5C6BC0");
        colors.add("#7986CB");

        // Blue
        colors.add("#1565C0");
        colors.add("#1976D2");
        colors.add("#1E88E5");
        colors.add("#2196F3");
        colors.add("#42A5F5");
        colors.add("#64B5F6");
        colors.add("#039BE5");

        // Cyan
        colors.add("#00838F");
        colors.add("#0097A7");
        colors.add("#00ACC1");
        colors.add("#00BCD4");
        colors.add("#26C6DA");
        colors.add("#00E5FF");

        // Teal
        colors.add("#00695C");
        colors.add("#00796B");
        colors.add("#00897B");
        colors.add("#009688");
        colors.add("#26A69A");
        colors.add("#4DB6AC");

        // Green
        colors.add("#1B5E20");
        colors.add("#2E7D32");
        colors.add("#388E3C");
        colors.add("#43A047");
        colors.add("#4CAF50");
        colors.add("#66BB6A");
        colors.add("#2ECC71");

        // Lime
        colors.add("#689F38");
        colors.add("#9CCC65");
        colors.add("#CDDC39");
        colors.add("#AEEA00");

        // Yellow / Gold
        colors.add("#B8860B");
        colors.add("#F9A825");
        colors.add("#FBC02D");
        colors.add("#FDD835");
        colors.add("#FFD54F");

        // Orange
        colors.add("#E65100");
        colors.add("#EF6C00");
        colors.add("#F57C00");
        colors.add("#FB8C00");
        colors.add("#FFA726");

        // Deep Orange
        colors.add("#BF360C");
        colors.add("#E64A19");
        colors.add("#F4511E");
        colors.add("#FF5722");
        colors.add("#FF7043");

        // Red
        colors.add("#B71C1C");
        colors.add("#C62828");
        colors.add("#D32F2F");
        colors.add("#E53935");
        colors.add("#F44336");

        // Pink
        colors.add("#AD1457");
        colors.add("#C2185B");
        colors.add("#D81B60");
        colors.add("#E91E63");
        colors.add("#EC407A");

        // Brown
        colors.add("#5D4037");
        colors.add("#6D4C41");
        colors.add("#795548");
        colors.add("#8D6E63");

        // Blue Grey
        colors.add("#263238");
        colors.add("#37474F");
        colors.add("#455A64");
        colors.add("#546E7A");
        colors.add("#607D8B");

        return colors;
    }

    public static String getDefaultCategory(Context context, int i) {
        return new String[]{
                getResourceString(context, R.string.category_bills),            //1
                getResourceString(context, R.string.category_clothing),                         //2
                getResourceString(context, R.string.category_education),                        //3
                getResourceString(context, R.string.category_entertainment),                    //4
                getResourceString(context, R.string.category_fitness),                          //5
                getResourceString(context, R.string.category_food),             //6
                getResourceString(context, R.string.category_grocery),          //7
                getResourceString(context, R.string.category_gifts),                            //8
                getResourceString(context, R.string.category_health_and_beauty),                           //9
                getResourceString(context, R.string.category_furniture),        //10
                getResourceString(context, R.string.category_pet),                              //11
                getResourceString(context, R.string.category_shopping),                         //12
                getResourceString(context, R.string.category_transportation),                   //13
                getResourceString(context, R.string.category_travel),                           //14
                getResourceString(context, R.string.category_others),                           //15

                getResourceString(context, R.string.category_allowance),                        //16
                getResourceString(context, R.string.category_award),                            //17
                getResourceString(context, R.string.category_bonus),                            //18
                getResourceString(context, R.string.category_dividend),                         //19
                getResourceString(context, R.string.category_investment),                       //20
                getResourceString(context, R.string.category_lottery),                          //21
                getResourceString(context, R.string.category_salary),                           //22
                getResourceString(context, R.string.category_tips),                             //23
                getResourceString(context, R.string.category_cashback),         //24
                getResourceString(context, R.string.category_others),                           //25

                getResourceString(context, R.string.adjustment),                       //26
                getResourceString(context, R.string.loan),                             //27
                getResourceString(context, R.string.repay),                        //28
                getResourceString(context, R.string.debt),                             //29
                getResourceString(context, R.string.collect),                  //30
                getResourceString(context, R.string.fee),                              //31
                getResourceString(context, R.string.transfer)}[i - 1];                 //32
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
        arrayList.add(R.drawable.ic_grocery);
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

    public static String getWalletTypeName(Context context, int type) {

        type = type + 1;

        return switch (type) {
            case 1 -> context.getString(R.string.general);
            case 2 -> context.getString(R.string.cash);
            case 3 -> context.getString(R.string.bank);
            case 4 -> context.getString(R.string.credit_card);
            case 5 -> context.getString(R.string.debit_card);
            default -> context.getString(R.string.general);
        };
    }

    //----------------------
    //--- GOALS ---
    //----------------------
    public static ArrayList<String> getGoalColorList() {
        return new ArrayList<>(Arrays.asList(
                "#F44336", // Red
                "#E91E63", // Pink
                "#9C27B0", // Purple
                "#673AB7", // Deep Purple
                "#3F51B5", // Indigo
                "#2196F3", // Blue

                "#03A9F4", // Light Blue
                "#00BCD4", // Cyan
                "#009688", // Teal
                "#4CAF50", // Green
                "#8BC34A", // Light Green
                "#CDDC39", // Lime

                "#FFEB3B", // Yellow
                "#FFC107", // Amber
                "#FF9800", // Orange
                "#FF5722", // Deep Orange
                "#795548", // Brown
                "#607D8B", // Blue Grey

                "#9E9E9E", // Grey
                "#000000", // Black
                "#FFFFFF", // White
                "#EC407A", // Rose
                "#26A69A", // Mint
                "#FFD54F"  // Gold
        ));
    }

    public static ArrayList<String> getGoalCategoryList(Context context) {
        return new ArrayList<>(Arrays.asList(
                context.getString(R.string.travel),
                context.getString(R.string.emergency_fund),
                context.getString(R.string.home),
                context.getString(R.string.vehicle),
                context.getString(R.string.education),
                context.getString(R.string.medical),
                context.getString(R.string.wedding),
                context.getString(R.string.business),
                context.getString(R.string.investment),
                context.getString(R.string.retirement),
                context.getString(R.string.vacation),
                context.getString(R.string.shopping),
                context.getString(R.string.gadgets),
                context.getString(R.string.gift),
                context.getString(R.string.festival),
                context.getString(R.string.insurance),
                context.getString(R.string.debt_payoff),
                context.getString(R.string.fitness),
                context.getString(R.string.religious),
                context.getString(R.string.baby_family),
                context.getString(R.string.pets),
                context.getString(R.string.custom),
                context.getString(R.string.others)
        ));
    }

    public static List<Integer> getGoalIcons() {
        List<Integer> arrayList = new ArrayList<>();

        arrayList.add(R.drawable.goal_0);
        arrayList.add(R.drawable.goal_1);
        arrayList.add(R.drawable.goal_2);
        arrayList.add(R.drawable.goal_3);
        arrayList.add(R.drawable.goal_4);
        arrayList.add(R.drawable.goal_5);
        arrayList.add(R.drawable.goal_6);
        arrayList.add(R.drawable.goal_7);
        arrayList.add(R.drawable.goal_8);
        arrayList.add(R.drawable.goal_9);
        arrayList.add(R.drawable.goal_10);
        arrayList.add(R.drawable.goal_11);
        arrayList.add(R.drawable.goal_12);
        arrayList.add(R.drawable.goal_13);
        arrayList.add(R.drawable.goal_14);
        arrayList.add(R.drawable.goal_15);
        arrayList.add(R.drawable.goal_16);
        arrayList.add(R.drawable.goal_17);
        arrayList.add(R.drawable.goal_18);
        arrayList.add(R.drawable.goal_19);
        arrayList.add(R.drawable.goal_20);
        arrayList.add(R.drawable.goal_21);
        arrayList.add(R.drawable.goal_22);
        arrayList.add(R.drawable.goal_23);
        arrayList.add(R.drawable.goal_24);

        return arrayList;
    }
}