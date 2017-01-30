package com.twinc.halmato.autowhatsappmessage;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.twinc.halmato.autowhatsappmessage.Notifications.NotificationScheduler;
import com.twinc.halmato.autowhatsappmessage.Notifications.NotificationUtilities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import static com.twinc.halmato.autowhatsappmessage.MainActivity.MessageApplication.WHATSAPP;
import static java.lang.Long.getLong;


public class MainActivity extends AppCompatActivity {


    public enum MessageApplication {
        WHATSAPP,
        WECHAT,
        LINKEDIN,
        TWITTER,
        FACEBOOK,
        GOOGLEPLUS
    }


    private final MessageApplication DEFAULT_APPLICATION = WHATSAPP;
    //private final Context context = this;
    private final String presetMessagesKey = "PRESET_MESSAGES_KEY";


    private boolean sendAutomatically;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    public ActionMode actionMode;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    private List<PresetMessage> presetMessages;
    private MessageApplication selectedApplication;

    public ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_preset_context, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            int item_postion = Integer.parseInt(mode.getTag().toString());


            switch (item.getItemId()) {
                case R.id.context_edit_message:

                    showPresetMessageToEdit(item_postion);

                    mode.finish(); // Action picked, so close the CAB
                    return true;

                case R.id.context_delete_message:

                    removePresetMessage(item_postion);

                    mode.finish(); // Action picked, so close the CAB
                    return true;

                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
        }
    };

    ////////////////////////////////////////// overrides ///////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        initFloatingActionButton();
        initComponents();

        loadSharedPreferences();

        populatePresetMessagesRecyclerView();

        trySendMessageAutomatically();

        NotificationUtilities.setScheduledNotifications(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            displaySettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displaySettings()  {

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .addToBackStack(getBaseContext().getClass().getSimpleName())
                .commit();
    }

    private void initToolbar()  {

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    private void initFloatingActionButton()  {

        FloatingActionButton myFab = (FloatingActionButton)  findViewById(R.id.fab);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                promptToAddPresetMessage();
            }
        });
    }

    private void initComponents() {

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    }

    private void populatePresetMessagesRecyclerView() {

        mAdapter = new PresetMessagesAdapter(presetMessages,this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
    }

    private void loadSharedPreferences()    {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferencesEditor = sharedPreferences.edit();

        loadPresetMessages();
        loadAutomaticSendPreferences();
        loadMessagingApplicationPreferences();
    }

    private void loadMessagingApplicationPreferences() {

        String defaultAppKey = getResources().getString(R.string.default_application_key);
        String defaultAppName = sharedPreferences.getString(defaultAppKey,"").toUpperCase(Locale.ENGLISH);

        MessageApplication defaultApp = (!defaultAppName.equals("")) ?  MessageApplication.valueOf(defaultAppName) : DEFAULT_APPLICATION;

        selectedApplication = defaultApp;
    }

    private void loadAutomaticSendPreferences()    {

        String sendPreferenceKey = getResources().getString(R.string.auto_send);
        Boolean autoSendPreferences = sharedPreferences.getBoolean(sendPreferenceKey,false);

        sendAutomatically = autoSendPreferences;
    }

    private void promptToAddPresetMessage() {

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(getBaseContext());
        View promptsView = li.inflate(R.layout.add_preset_prompt, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getBaseContext());

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userPromptInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton("Add",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {

                                addPresetMessage(userPromptInput.getText().toString());
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        userPromptInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {


                if (hasFocus) {
                    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                } else {
                    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                }
            }
        });


        // show it
        alertDialog.show();

    }

    private void trySendMessageAutomatically() {

        if(sendAutomatically && presetMessages.size() > 0)    {

            String randomMessage = getRandomPresetMessage().message;

            // Notify user of message selected if it was randomly chosen
            Toast.makeText(this, "Message Selected: /n  " + randomMessage, Toast.LENGTH_SHORT).show();

            sendMessage(randomMessage);
        }
    }

    private void showPresetMessageToEdit(int index)  {
        String messageToEdit = presetMessages.get(index).message;

        // TODO
        // Create Edit Dialog
        // populate dialog's EditTextView with 'messageToEdit'

       // the OnClickListener of the Dialog's YES, would be editPresetMessage callback.

    }

    private void editPresetMessage(int index, String updatedMessage)    {

        presetMessages.get(index).message = updatedMessage;
        onPresetMessagesChange();
    }

    private PresetMessage getRandomPresetMessage() {

        int randomIndex = new Random().nextInt(presetMessages.size());
        return presetMessages.get(randomIndex);
    }

    private void loadPresetMessages() {

        Set<String> storedPresetMessagesSet = sharedPreferences.getStringSet(presetMessagesKey, new HashSet<String>());
        Set<PresetMessage> realStoredPresetMessagesSet = new HashSet<PresetMessage>();

        // Create real hashSet from sharedPreferences. This is a workaround. Need to somehow store the PresetMessage object in the SharedPreferences.
        for (String msg : storedPresetMessagesSet) {

            realStoredPresetMessagesSet.add(new PresetMessage(msg,""));
        }

        presetMessages = new ArrayList<PresetMessage>(realStoredPresetMessagesSet);

    }

    public void sendMessage(String message) {

        PackageManager pm = getPackageManager();

        String appPackage = "com.whatsapp";

        try {

            switch (selectedApplication) {

                case WHATSAPP:
                    appPackage = "com.whatsapp";
                    break;

                case WECHAT:
                    appPackage = "com.tencent.mm";
                    break;

                case LINKEDIN:
                    appPackage = "com.linkedin.android";
                    break;

                case TWITTER:
                    appPackage = "com.twitter.android";
                    break;

                case FACEBOOK:
                    appPackage = "com.facebook.katana";
                    break;

                case GOOGLEPLUS:
                    appPackage = "com.google.android.apps.plus";
                    break;

                default:
                    break;

            }

            PackageInfo info = pm.getPackageInfo(appPackage, PackageManager.GET_META_DATA);

            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");


            //Check if package exists or not. If not then code
            //in catch block will be called
            waIntent.setPackage(appPackage);

            waIntent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(waIntent);

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, appPackage + " is not Installed", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void addPresetMessage(String message) {

        if(message.isEmpty())
            return;

        // If the message does not exist yet, add to presetMessages. Otherwise notify user that message is a duplicate.
        if(!presetMessageListContainsMessage(message)) {

            presetMessages.add(new PresetMessage(message,""));

            Toast toast = Toast.makeText(this, "Message was added", Toast.LENGTH_LONG);
            toast.show();

            onPresetMessagesChange();

        } else {

            Toast toast = Toast.makeText(this, "Message already exists", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private boolean presetMessageListContainsMessage(String message)  {

        for (PresetMessage msg:presetMessages) {
            if(msg.message.equalsIgnoreCase(message))    {
                return true;
            }
        }
        return false;
    }

    public void removePresetMessage(int indx) {

        if(indx < presetMessages.size()) {

            presetMessages.remove(indx);
            onPresetMessagesChange();

        }
    }

    private void onPresetMessagesChange()   {

        refreshPresetMessagesListView();
        updatePresetMessagesSharedPreferences();
    }

    private void refreshPresetMessagesListView()  {

        //presetMessageListAdapter.notifyDataSetChanged();
        mAdapter.notifyDataSetChanged();

    }

    private void updatePresetMessagesSharedPreferences()   {

        Set<String> presetMessagesSet = new HashSet<String>();

        List<String> presetMessagesStringList = new ArrayList<String>();
        for (PresetMessage msg: presetMessages) {

            presetMessagesStringList.add(msg.message);
        }

        presetMessagesSet.addAll(presetMessagesStringList);

        sharedPreferencesEditor.putStringSet(presetMessagesKey,presetMessagesSet);
        sharedPreferencesEditor.commit();
    }

}
