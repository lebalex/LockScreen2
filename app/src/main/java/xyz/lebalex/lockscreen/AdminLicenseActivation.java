package xyz.lebalex.lockscreen;

import android.app.admin.DevicePolicyManager;
import android.app.enterprise.license.EnterpriseLicenseManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import android.os.Handler;
import android.os.Message;



import com.sec.enterprise.knox.license.KnoxEnterpriseLicenseManager;

import java.lang.ref.WeakReference;

public class AdminLicenseActivation extends AppCompatActivity {
    private static final String TAG = "AdminLicenseActivation";
    private Button btnActivateELM;
    private SharedPreferences.Editor adminLicensePrefsEditor;
    private SharedPreferences adminLicensePrefs;
    private DevicePolicyManager mDPM;
    private ComponentName mCN;
    private LicenseReceiver mLicenseReceiver;
    private DeviceAdministrator mDeviceAdmin;
    private Button btnAdmin;
    private TextView mNotSupportedNotification;
    private boolean isActivityVisible;
    private static boolean isAPISupported;
    private MessageHandler messageHandler;

    private static class MessageHandler extends Handler {

        private WeakReference<AdminLicenseActivation> adminLicenseActivationWeakReference;

        MessageHandler(AdminLicenseActivation adminLicenseActivation) {
            adminLicenseActivationWeakReference = new WeakReference<AdminLicenseActivation>(adminLicenseActivation);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            AdminLicenseActivation adminLicenseActivation = adminLicenseActivationWeakReference.get();

            if(adminLicenseActivation != null) {

                switch (msg.what) {
                    //ELM activation successful
                    case SAConstants.RESULT_ELM_ACTIVATED:
                        adminLicenseActivation.setUIStates(SAConstants.RESULT_ELM_ACTIVATED);
                        break;
                    //Admin enabled
                    case SAConstants.ADMIN_ENABLED:
                        adminLicenseActivation.setUIStates(SAConstants.ADMIN_ENABLED);
                        break;
                    //Admin disabled
                    case SAConstants.ADMIN_DISABLED:
                        adminLicenseActivation.setUIStates(SAConstants.ADMIN_DISABLED);
                        break;

                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_license_activation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        messageHandler = new MessageHandler(this);
        //Passing Handler reference to LicenseReceiver
        if (mLicenseReceiver == null) {
            mLicenseReceiver = new LicenseReceiver(messageHandler);
        }
        //Passing Handler reference to DeviceAdministrator
        if (mDeviceAdmin == null) {
            mDeviceAdmin = new DeviceAdministrator(messageHandler);
        }

        btnAdmin = (Button) findViewById(R.id.buttonAdmin);
        btnActivateELM = (Button) findViewById(R.id.admin_license_activation_btn_activate_elm);
        mNotSupportedNotification = (TextView) findViewById(R.id.not_supported_notification);

        adminLicensePrefsEditor = getSharedPreferences(SAConstants.MY_PREFS_NAME, MODE_PRIVATE).edit();
        adminLicensePrefs = getSharedPreferences(SAConstants.MY_PREFS_NAME, MODE_PRIVATE);

        if (getIntent().getBooleanExtra(SAConstants.DEACTIVATION_REQUIRED, false)) {
            deactivateAdmin(this);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        isActivityVisible = true;
        //Setting the initial state of UI screen
        setUIStates(SAConstants.INITIAL_STATE);
    }

    public void setUIStates(int condition) {

        switch (condition) {

            //Initial UI state
            case SAConstants.INITIAL_STATE:
                isAPISupported = true;

                /*
                   If the device has a version equal to or greater than
                   EnterpriseDeviceManager.EnterpriseSdkVersion.ENTERPRISE_SDK_VERSION_4,
                   then ELM activation API is supported in it.If this requirement is not met,
                   then ELM activation API is not supported and hence this app will not work.
                */
                /*if (!(SAUtils.isAPISupported(SAConstants.MDMVersion.VER_4_0))) {
                    disableButton(btnActivateELM);
                    isAPISupported = false;
                }*/

                if (isAPISupported) {
                    mNotSupportedNotification.setVisibility(View.INVISIBLE);
                } else {
                    mNotSupportedNotification.setVisibility(View.VISIBLE);
                }
                if (!adminLicensePrefs.getBoolean(SAConstants.ADMIN, false)) {
                    disableButton(btnActivateELM);
                }

                //If admin is enabled
                if (isAPISupported && adminLicensePrefs.getBoolean(SAConstants.ADMIN, false)) {
                    btnAdmin.setText(getString(R.string.disable_admin));
                    enableButton(btnActivateELM);
                }
                //If ELM is activated
                if (adminLicensePrefs.getBoolean(SAConstants.ELM, false)) {
                    //If admin is enabled
                    if (isActivityVisible && adminLicensePrefs.getBoolean(SAConstants.ADMIN, false)) {
                        //startMainActivity();
                    }
                }

                //If ELM is activated
                if (adminLicensePrefs.getBoolean(SAConstants.ELM, false)) {
                    disableButton(btnActivateELM);
                }
                break;

            //State when admin is enabled
            case SAConstants.ADMIN_ENABLED:

                //Changing the Button text to "Disable Admin"
                btnAdmin.setText(getResources().getString(R.string.disable_admin));
                adminLicensePrefsEditor.putBoolean(SAConstants.ADMIN, true);
                adminLicensePrefsEditor.commit();

                SAUtils.displayToast(this, getResources().getString(R.string.device_admin_enabled));

                /*
                  Enabling "Activate ELM" Button if ELM activation API
                  is supported in the device
                */
                if (isAPISupported) {
                    enableButton(btnActivateELM);
                }
                break;

            //State when admin is disabled
            case SAConstants.ADMIN_DISABLED:

                //Changing the Button text to "Enable Admin"
                btnAdmin.setText(getResources().getString(R.string.enable_admin));
                adminLicensePrefsEditor.putBoolean(SAConstants.ADMIN, false);
                adminLicensePrefsEditor.putBoolean(SAConstants.ELM, false);
                adminLicensePrefsEditor.commit();

                //Disabling "Activate ELM" Button
                disableButton(btnActivateELM);

                SAUtils.displayToast(this, getResources().getString(R.string.device_admin_disabled));

                break;

            //State when ELM is activated
            case SAConstants.RESULT_ELM_ACTIVATED:

                disableButton(btnActivateELM);
                adminLicensePrefsEditor.putBoolean(SAConstants.ELM, true);
                adminLicensePrefsEditor.commit();

                //Launching MainActivity if admin and ELM are activated
                if (isActivityVisible && adminLicensePrefs.getBoolean(SAConstants.ADMIN, false)) {
                    //startMainActivity();
                }
                break;

            default:
                break;

        }

    }

    /**
     Enables a Button
     @param buttonObj    the Button instance, which needs to be enabled
     */
    public void enableButton(Button buttonObj) {
        buttonObj.setEnabled(true);
    }

    /**
     Disables a Button
     @param buttonObj     the Button instance, which needs to be disabled
     */

    public void disableButton(Button buttonObj) {
        buttonObj.setEnabled(false);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
    public void onClick(View v) {

        switch (v.getId()) {
            //If "Enable Admin" or "Disable Admin" Button is clicked
            case R.id.buttonAdmin:
                //If admin needs to be enabled
                if (!adminLicensePrefs.getBoolean(SAConstants.ADMIN, false)) {
                    activateAdmin(this);
                }
                //If admin needs to be disabled
                else {
                    deactivateAdmin(this);
                }

                break;

            //If "Activate ELM" Button is clicked
            case R.id.admin_license_activation_btn_activate_elm:

                //If admin is enabled
                if (adminLicensePrefs.getBoolean(SAConstants.ADMIN, false)) {
                    activateELM(SAConstants.ELM_KEY);
                }
                break;

            default:
                break;
        }
    }
    private boolean activateAdmin(Context context) {
        try {
            if (mDPM == null) {
                mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            }
            if (mCN == null) {
                mCN = new ComponentName(context, DeviceAdministrator.class);
            }
            if (mDPM != null && !mDPM.isAdminActive(mCN)) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mCN);
                context.startActivity(intent);
                return true;
            } else {
                return false;
            }
        } catch (ActivityNotFoundException e) {
            /*
              An ActivityNotFoundException will occur if there is no Activity found to run the given Intent
              through the method startActivity()
            */
            SAUtils.displayToast(AdminLicenseActivation.this, getString(R.string.admin_activity_not_found));
            Log.e(TAG, "" + e);
        }
        return false;
    }
    private boolean deactivateAdmin(Context context) {

        if (mDPM == null) {
            mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        }
        if (mCN == null) {
            mCN = new ComponentName(context, DeviceAdministrator.class);
        }

        try {
            if (mDPM != null && mDPM.isAdminActive(mCN)) {
                mDPM.removeActiveAdmin(mCN);
                return true;
            } else {
                return false;
            }
        } catch (SecurityException e) {
            /*
               A SecurityException will occur, if an attempt is made to remove the active admin
               without owning the admin component.
            */
            SAUtils.displayToast(AdminLicenseActivation.this, getString(R.string.non_owner_admin_removal_exception));
            Log.e(TAG, "" + e);
        }
        return false;
    }
    public void activateELM(String elmKey) {

/*2.8
        KnoxEnterpriseLicenseManager klmManager = KnoxEnterpriseLicenseManager.getInstance(context);
        klmManager.activateLicense("KLM06-12345-67890-ABCDE-FGHIJ-KLMNO");
*/
/*2.7*/
        KnoxEnterpriseLicenseManager klmManager = KnoxEnterpriseLicenseManager.getInstance(this);
        klmManager.activateLicense(SAConstants.ELM_KEY);
        EnterpriseLicenseManager elmManager = EnterpriseLicenseManager.getInstance(this);
        elmManager.activateLicense(SAConstants.ELM_KEY2);
    }



}
