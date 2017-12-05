/**
 * DISCLAIMER: PLEASE TAKE NOTE THAT THE SAMPLE APPLICATION AND
 * SOURCE CODE DESCRIBED HEREIN IS PROVIDED FOR TESTING PURPOSES ONLY.
 *
 * Samsung expressly disclaims any and all warranties of any kind,
 * whether express or implied, including but not limited to the implied warranties and conditions
 * of merchantability, fitness for a particular purpose and non-infringement.
 * Further, Samsung does not represent or warrant that any portion of the sample application and
 * source code is free of inaccuracies, errors, bugs or interruptions, or is reliable,
 * accurate, complete, or otherwise valid. The sample application and source code is provided
 * "as is" and "as available", without any warranty of any kind from Samsung.
 *
 * Your use of the sample application and source code is at its own discretion and risk,
 * and licensee will be solely responsible for any damage that results from the use of the sample
 * application and source code including, but not limited to, any damage to your computer system or
 * platform. For the purpose of clarity, the sample code is licensed “as is” and
 * licenses bears the risk of using it.
 *
 * Samsung shall not be liable for any direct, indirect or consequential damages or
 * costs of any type arising out of any action taken by you or others related to the sample application
 * and source code.
 */

package xyz.lebalex.lockscreen;

/**
 * This interface captures all constants used in the project
 */

public interface SAConstants {

    /***********************************************************************************
     * Please insert ELM key here for testing.
     * It is recommended not to save them in the apk but to obtain it from a server.
     * Please visit https://seap.samsung.com/license-keys/how-to for detailed
     * information on license keys and the process of generating them.
     **********************************************************************************/
    /*логин-пароль как gmail*/
    String ELM_KEY = "KLM06-G32WX-LDYRI-STM97-X5VKD-3UNHG";
    String ELM_KEY2 = "6F34C17DE1BE2488593BD8101212623C93A0B5A1F184D74E421D8D8B486C706FBD0D08F976702D26A54D32388AFD1AA52785B6E80E75A62DF88B96577BF0B4C7";

    String MY_PREFS_NAME = "LockSAcreenApps";
    String ADMIN = "admin";
    String ELM = "elm";
    String DEACTIVATION_REQUIRED = "deactivation_required";

    int INITIAL_STATE = 0;
    int ADMIN_ENABLED = 1;
    int ADMIN_DISABLED = 2;
    int RESULT_ELM_ACTIVATED = 3;
    int DEFAULT_ERROR = -1;

    enum MDMVersion {
        VER_2_0, VER_2_1, VER_2_2, VER_3_0, VER_4_0, VER_4_0_1, VER_4_1, VER_5_0, VER_5_1, VER_5_2, VER_5_3, VER_5_4, VER_5_4_1, VER_5_5, NONE
    }

}