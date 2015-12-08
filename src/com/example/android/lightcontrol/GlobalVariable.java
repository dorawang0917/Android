package com.example.android.lightcontrol;

import android.app.Application;
import android.content.SharedPreferences;

public class GlobalVariable extends Application {

    public static SharedPreferences checkbox_settings;
    public static String query_short_packet;
    public static String query_dali_packet;
    public static String fade_time;
    public static int t = 2000;
    public static int i = 0;
    static int bytes = 64;
    static int buffer_bytes = 64;
    public static  String Theme_to_group_or_all = "ff";
    public static  String Theme_to_area1_group_8X = "01";
    public static  String Theme_to_area2_group_8X = "03";

    public static String VAT_number = "9055";
    public static String Theme_choice;
    public static String Group_Theme_choice;
    public static String Light_percentage_choice;
    public static String Group_percentage_choice="00";

    public static String saved;
    public static String by1_AA = "AA";
    public static String by2_55 = "55";
    public static String by3_06 = "06";
    public static String by3_07 = "07";
    public static String by4_04 = "04";
    public static String by4_01 = "01";
    public static String by4_06 = "06";

    public static String by5 = "FF";
    public static String by5_00 = "00";
    public static String by6 = "FF";
    public static String by6_00 = "00";

    public static String by7_00 = "00";
    public static String by7_FE = "FE";
    public static String by7_FF = "FF";

    public static String by8_Zigbee_check = "0a";
    public static String by8_ON_MAX = "05";
    public static String by8_OFF = "00";
    public static String by8_ON_MIN = "06";
    public static String by8_query_short = "09";
    public static String by8_query_dali = "08";

    public static String by9_Return = "01";
    public static String by9_NoReturn = "00";



    static int SumFF = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
            + (Integer.parseInt(GlobalVariable.by2_55, 16))
            + (Integer.parseInt(GlobalVariable.by3_06, 16))
            + (Integer.parseInt(GlobalVariable.by4_04, 16))
            + (Integer.parseInt(GlobalVariable.by5, 16))
            + (Integer.parseInt(GlobalVariable.by6, 16))
            + (Integer.parseInt(GlobalVariable.by7_FF, 16)));

    static int SumFE = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
            + (Integer.parseInt(GlobalVariable.by2_55, 16))
            + (Integer.parseInt(GlobalVariable.by3_06, 16))
            + (Integer.parseInt(GlobalVariable.by4_04, 16))
            + (Integer.parseInt(GlobalVariable.by5, 16))
            + (Integer.parseInt(GlobalVariable.by6, 16))
            + (Integer.parseInt(GlobalVariable.by7_FE, 16)));
    // aa 55 06 01 00 00 00 09 01
    static int SumQuery_short = ((Integer.parseInt(GlobalVariable.by1_AA, 16))
            + (Integer.parseInt(GlobalVariable.by2_55, 16))
            + (Integer.parseInt(GlobalVariable.by3_06, 16))
            + (Integer.parseInt(GlobalVariable.by4_01, 16))
            + (Integer.parseInt(GlobalVariable.by5_00, 16))
            + (Integer.parseInt(GlobalVariable.by6_00, 16))
            + (Integer.parseInt(GlobalVariable.by7_00, 16))
            + (Integer.parseInt(GlobalVariable.by8_query_short, 16))
            + (Integer.parseInt(GlobalVariable.by9_Return, 16)));

   /* static int spinner_dali_send_on_checksum =((Integer.parseInt(GlobalVariable.by1_AA, 16))
            + (Integer.parseInt(GlobalVariable.by2_55, 16))
            + (Integer.parseInt(GlobalVariable.by3_06, 16))
            + (Integer.parseInt(GlobalVariable.by4_01, 16))
            + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2), 16))
            + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4), 16))
            + (Integer.parseInt(GlobalVariable.OnSpinnerItemSelected, 16))
            + (Integer.parseInt(GlobalVariable.by8_ON_MAX, 16))
            + (Integer.parseInt(GlobalVariable.by9_NoReturn, 16)));*/


    public static String String_FF = GlobalVariable.by1_AA + GlobalVariable.by2_55
            + GlobalVariable.by3_06 + GlobalVariable.by4_04 + GlobalVariable.by5
            + GlobalVariable.by6 + GlobalVariable.by7_FF;

    public static String String_FE = GlobalVariable.by1_AA + GlobalVariable.by2_55
            + GlobalVariable.by3_06 + GlobalVariable.by4_04 + GlobalVariable.by5
            + GlobalVariable.by6 + GlobalVariable.by7_FE;
    // aa 55 06 01 00 00 00 09 01
    public static String String_query_short = GlobalVariable.by1_AA + GlobalVariable.by2_55
            + GlobalVariable.by3_06 + GlobalVariable.by4_01 + GlobalVariable.by5_00
            + GlobalVariable.by6_00 + GlobalVariable.by7_00 + GlobalVariable.by8_query_short + GlobalVariable.by9_Return+"10";
    // aa 55 06 01 __ __ 00 08 01


    public static String String_query_dali_1 = GlobalVariable.by1_AA + GlobalVariable.by2_55
            + GlobalVariable.by3_06 + GlobalVariable.by4_01;
    public static String String_query_dali_2 =
            GlobalVariable.by7_00 + GlobalVariable.by8_query_dali + GlobalVariable.by9_Return;

    public static String OnSpinnerItemSelected;
    public static String OnSpinnerItemSelectedOfGroup;

    public static String query_is_zigbee_exist_or_not = GlobalVariable.by1_AA + GlobalVariable.by2_55
            + GlobalVariable.by3_06 + GlobalVariable.by4_01 + GlobalVariable.by5_00
            + GlobalVariable.by6_00 + GlobalVariable.by7_00 + GlobalVariable.by8_Zigbee_check+GlobalVariable.by9_Return+"11";

    public static String set_group = GlobalVariable.by1_AA + GlobalVariable.by2_55
            + "07" + "06" + GlobalVariable.by5_00
            + GlobalVariable.by6_00;

    //aa 55 06 06 00 00
    public static String send_group_command = GlobalVariable.by1_AA + GlobalVariable.by2_55
            + GlobalVariable.by3_06 + GlobalVariable.by4_06 + GlobalVariable.by5_00
            + GlobalVariable.by6_00 + "8";



   /* public static String String_spinner_send_on = GlobalVariable.by1_AA + GlobalVariable.by2_55
            + GlobalVariable.by3_06 + GlobalVariable.by4_01 + GlobalVariable.OnSpinnerItemSelectedOfShort.substring(0, 2)
            + GlobalVariable.OnSpinnerItemSelectedOfShort.substring(2, 4) + GlobalVariable.OnSpinnerItemSelected
            + GlobalVariable.by8_ON_MAX + GlobalVariable.by9_NoReturn;*/
   public static int getpercentage(int a) {
       int b=0;
       switch (a) {
           case 0 :return 0;
           case 1 :b = 86 ;break;case 2 :b = 112;break;case 3 :b = 126;break;case 4 :b = 137;break;
           case 5 :b = 145;break;case 6 :b = 151;break;case 7 :b = 157;break;case 8 :b = 162;break;
           case 9 :b = 166;break;case 10:b = 170;break;case 11:b = 174;break;case 12:b = 177;break;
           case 13:b = 180;break;case 14:b = 182;break;case 15:b = 185;break;case 16:b = 187;break;
           case 17:b = 190;break;case 18:b = 192;break;case 19:b = 194;break;case 20:b = 196;break;

           case 21:b = 197;break;case 22:b = 199;break;case 23:b = 201;break;case 24:b = 202;break;
           case 25:b = 204;break;case 26:b = 205;break;case 27:b = 207;break;case 28:b = 208;break;
           case 29:b = 209;break;case 30:b = 210;break;case 31:b = 212;break;case 32:b = 213;break;
           case 33:b = 214;break;case 34:b = 215;break;case 35:b = 216;break;case 36:b = 217;break;
           case 37:b = 218;break;case 38:b = 219;break;case 39:b = 220;break;case 40:b = 221;break;

           case 41:b = 222;break;case 42:b = 223;break;case 43:b = 223;break;case 44:b = 224;break;
           case 45:b = 225;break;case 46:b = 226;break;case 47:b = 227;break;case 48:b = 228;break;
           case 49:b = 228;break;case 50:b = 229;break;case 51:b = 230;break;case 52:b = 230;break;
           case 53:b = 231;break;case 54:b = 232;break;case 55:b = 232;break;case 56:b = 233;break;
           case 57:b = 234;break;case 58:b = 234;break;case 59:b = 235;break;case 60:b = 235;break;

           case 61:b = 236;break;case 62:b = 237;break;case 63:b = 237;break;case 64:b = 238;break;
           case 65:b = 238;break;case 66:b = 239;break;case 67:b = 239;break;case 68:b = 240;break;
           case 69:b = 240;break;case 70:b = 241;break;case 71:b = 241;break;case 72:b = 242;break;

           case 73:b = 242;break;case 74:b = 243;break;case 75:b = 243;break;case 76:b = 244;break;
           case 77:b = 244;break;case 78:b = 245;break;case 79:b = 245;break;case 80:b = 246;break;

           case 81:b = 246;break;case 82:b = 247;break;case 83:b = 247;break;case 84:b = 248;break;
           case 85:b = 248;break;case 86:b = 249;break;case 87:b = 249;break;case 88:b = 250;break;
           case 89:b = 250;break;case 90:b = 250;break;case 91:b = 250;break;case 92:b = 251;break;
           case 93:b = 251;break;case 94:b = 252;break;case 95:b = 252;break;case 96:b = 252;break;
           case 97:b = 253;break;case 98:b = 253;break;case 99:b = 253;break;case 100:b = 254;break;
       }
       return b;
   }
}
