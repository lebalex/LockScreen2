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

import android.app.enterprise.license.EnterpriseLicenseManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;




/**
 * This BroadcastReceiver handles ELM activation
 */

public class LicenseReceiver extends BroadcastReceiver {

    private Context mContext;
    private static Handler handler;

    public LicenseReceiver() {

    }

    //Get the Handler instance
    public LicenseReceiver(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context;

        if (intent != null) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            //If ELM activation result Intent is obtained
            else if (action
                    .equals(EnterpriseLicenseManager.ACTION_LICENSE_STATUS)) {
                int errorCode = intent.getIntExtra(
                        EnterpriseLicenseManager.EXTRA_LICENSE_ERROR_CODE,
                        SAConstants.DEFAULT_ERROR);

                //If ELM is successfully activated
                if (handler != null && errorCode == EnterpriseLicenseManager.ERROR_NONE) {

                    SAUtils.displayToast(context, context.getResources().getString(R.string.elm_activated_successfully));
                    Message msg = handler.obtainMessage();
                    msg.what = SAConstants.RESULT_ELM_ACTIVATED;
                    handler.sendMessage(msg);
                }
                //If ELM activation failed
                else {
                    String errorMessage = SAUtils.getMessage(mContext,errorCode);
                    SAUtils.displayToast(context, errorMessage);
                }
            }
        }
    }
}