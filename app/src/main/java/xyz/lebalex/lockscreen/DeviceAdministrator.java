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

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;


import xyz.lebalex.lockscreen.SAConstants;
import xyz.lebalex.lockscreen.SAUtils;


/**
 * This BroadcastReceiver handles device admin activation and deactivation
 */

public class DeviceAdministrator extends DeviceAdminReceiver {

    private static Handler handler;

    public DeviceAdministrator() {

    }

    //Get the Handler instance
    public DeviceAdministrator(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        if(handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = SAConstants.ADMIN_ENABLED;
            handler.sendMessage(msg);
        }
        else{
            updateState(context, SAConstants.ADMIN_ENABLED);
        }
    }


    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return context.getResources().getString(R.string.disable_admin_confirmation);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        if(handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = SAConstants.ADMIN_DISABLED;
            handler.sendMessage(msg);
        }
        else{
            updateState(context, SAConstants.ADMIN_DISABLED);
        }
    }

    /**
     *  When AdminLicenseActivation Activity is not running and the user chooses to enable/disable
     *  admin via Settings, this method is used to update the values in SharedPreferences.
     *
     *  @param context      the context received from DeviceAdminReceiver's callback method
     *  @param condition    an integer specifying whether the admin is enabled or disabled
     */

    public void updateState(Context context,int condition){

        SharedPreferences.Editor adminLicensePrefsEditor = context.getSharedPreferences(SAConstants.MY_PREFS_NAME, context.MODE_PRIVATE).edit();

        switch (condition){

            case SAConstants.ADMIN_ENABLED:
                adminLicensePrefsEditor.putBoolean(SAConstants.ADMIN, true);
                adminLicensePrefsEditor.commit();
                SAUtils.displayToast(context, context.getResources().getString(R.string.device_admin_enabled));
                break;

            case SAConstants.ADMIN_DISABLED:
                adminLicensePrefsEditor.putBoolean(SAConstants.ADMIN, false);
                adminLicensePrefsEditor.putBoolean(SAConstants.ELM, false);
                adminLicensePrefsEditor.commit();
                SAUtils.displayToast(context, context.getResources().getString(R.string.device_admin_disabled));
                break;

            default:
                break;

        }


     }

}