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

import android.app.AlertDialog;
import android.app.enterprise.EnterpriseDeviceManager;
import android.app.enterprise.license.EnterpriseLicenseManager;
import android.content.Context;
import android.util.SparseIntArray;
import android.widget.Toast;

import xyz.lebalex.lockscreen.R;
import xyz.lebalex.lockscreen.SAConstants;


/**
 * This class is used for utilities such as displaying Toast and AlertDialog, checking API
 * support in the device and the likes
 */

public class SAUtils {

    private static final SparseIntArray mapCodes = new SparseIntArray();
    private static EnterpriseDeviceManager mEDM;

    private SAUtils() {
        throw new AssertionError();
    }

    /**
      Initializes EnterpriseDeviceManager
      @param context    the Activity context
     */
    public static void initialize(Context context) {
        if (mEDM == null) {
            mEDM = (EnterpriseDeviceManager) context.getSystemService(EnterpriseDeviceManager.ENTERPRISE_POLICY_SERVICE);
        }
    }

    //Maps error codes to error descriptions
    public static void populateCodes() {
        mapCodes.clear();




        mapCodes.put(EnterpriseLicenseManager.ERROR_INTERNAL,
                R.string.err_internal);

        mapCodes.put(EnterpriseLicenseManager.ERROR_INTERNAL_SERVER,
                R.string.err_internal_server);

        mapCodes.put(EnterpriseLicenseManager.ERROR_INVALID_LICENSE,
                R.string.err_licence_invalid_license);

        mapCodes.put(EnterpriseLicenseManager.ERROR_INVALID_PACKAGE_NAME,
                R.string.err_invalid_package_name);

        mapCodes.put(EnterpriseLicenseManager.ERROR_LICENSE_TERMINATED,
                R.string.err_licence_terminated);

        mapCodes.put(EnterpriseLicenseManager.ERROR_NETWORK_DISCONNECTED,
                R.string.err_network_disconnected);

        mapCodes.put(EnterpriseLicenseManager.ERROR_NETWORK_GENERAL,
                R.string.err_network_general);

        mapCodes.put(EnterpriseLicenseManager.ERROR_NOT_CURRENT_DATE,
                R.string.err_unknown);

        mapCodes.put(EnterpriseLicenseManager.ERROR_NULL_PARAMS,
                R.string.err_not_current_date);

        mapCodes.put(EnterpriseLicenseManager.ERROR_UNKNOWN,
                R.string.err_null_params);

        mapCodes.put(
                EnterpriseLicenseManager.ERROR_USER_DISAGREES_LICENSE_AGREEMENT,

                R.string.err_user_disagrees_license_agreement);


    }

    /**
    Gets the message to be displayed to user by passing in code
    @param  context the Activity context
    @param  code    the error code
    @return         the message corresponding to the code
   */
    public static String getMessage(Context context, int code) {
        if (mapCodes.get(code) != 0)
            return context.getString(mapCodes.get(code));
        else return "";
    }


    /**
      Displays Toast message
      @param context    the Activity context
      @param message    the message displayed in Toast
     */
    public static void displayToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
      Shows an AlertDialog
      @param ctxt           the Activity context
      @param heading        the heading message to be displayed in AlertDialog
      @param message        the message to be displayed in the body of the AlertDialog
      @param okButtonText   the message to be displayed in the button of AlertDialog
     */
    public static void showAlert(Context ctxt, String heading, String message,
                                 String okButtonText) {
        if (ctxt != null)
            new AlertDialog.Builder(ctxt).setTitle(heading).setMessage(message)
                    .setPositiveButton(okButtonText, null).show();
    }

    /**
      Checks if the current device's MDM version is compatible with the passed in MDM version
      @param mdmVersionReqd     MDM version required for the current API
      @return                   true if API is supported in the current device, else false
     */
    public static boolean isAPISupported(SAConstants.MDMVersion mdmVersionReqd) {
        if (mdmVersionReqd != null
                && mEDM.getEnterpriseSdkVer().ordinal() < mdmVersionReqd.ordinal()) {
            return false;
        }
        return true;
    }

}