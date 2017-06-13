package app.com.mychat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kennyc.bottomsheet.BottomSheet;
import com.kennyc.bottomsheet.BottomSheetListener;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiClickedListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import app.com.mychat.menuActivity.media.view.MediaActivity;
import app.com.mychat.menuActivity.viewContact.ViewContactActivity;
import app.com.mychat.menuActivity.wallpaper.DialogMenuWallpaperActivity;
import mehdi.sakout.fancybuttons.FancyButton;

import static android.graphics.BitmapFactory.decodeFile;
import static app.com.mychat.R.id.menu_add_shortcut;
import static app.com.mychat.R.id.menu_block;
import static app.com.mychat.R.id.menu_clear_chat;
import static app.com.mychat.R.id.menu_email_chat;
import static app.com.mychat.R.id.menu_media;
import static app.com.mychat.R.id.menu_mute;
import static app.com.mychat.R.id.menu_search;
import static app.com.mychat.R.id.menu_view_contact;
import static app.com.mychat.R.id.menu_wallpaper;


public class ChatRoomActivity extends BaseActivity implements BottomSheetListener {
    ArrayList<GetData> data;
    ChatListAdapter mAdapter;
    final Context context = this;
    private String recipientId, name;
    // private EditText messageBodyField;
    private TextView name1, txtState;
    private String messageBody, img;
    private String currentUserId;
    private ListView messageList;
    ImageView imgUser, btnVideo1, btnCall, menuDown, imgOption, menuCamera;
    private MessageService.MessageServiceInterface messageService;
    private ServiceConnection serviceConnection = new MyServiceConnection();
    private MyMessageClientListener messageClientListener = new MyMessageClientListener();
    SharedPreferences sharedPreferences;
    SinchClient sinchClient = null;
    ConnectionDetector con;
    AsyncTask<Void, Void, Void> SendData;
    JSONObject response_json;
    FancyButton send;
    private Uri uriContact;
    private String contactID;
    String contactName = null, contactNumber = null, selectedVideoPath = "";
    int REQUEST_TAKE_TEXT_FILE = 300;
    public static final int MENU_AUDIO = 4;
    public static final int MENU_CONTACT = 6;
    public static final int MENU_CAMERA = 2;
    int REQUEST_TAKE_GALLERY_VIDEO = 100;
    File file;
    GetData m1;
    String mId;
    static final String JSON_EXCEPTION = "the JSON Exception";
    String old_id = "";

    RelativeLayout header;

    private Pubnub pubnub;
    private ArrayList users;
    private JSONArray hereNowUuids;
    private static final String PUBLISH_KEY = "pub-c-60ab2436-b4ca-4921-bb3a-1b94cbd7b2ff";
    private static final String P_SECRET_KEY = "sec-c-NGVlYWRhODEtM2UyNS00YmZiLWFmMDctMzBmZWQyM2Q5YTNi";
    private static final String SUBSCRIBE_KEY = "sub-c-f917bc56-8154-11e6-8409-0619f8945a4f";
    private boolean typingStarted;

    Uri imageUri = null;
    File f;

    private SharedPreferences mSharedPreferences;
    private String username;
    private String callUser;
    DatabaseHandler db;
    double longitude;
    double latitude;
    GPSTracker gps;
    Bitmap bp;
    String where;
    private EmojiEditText messageBodyField;

    private EmojiPopup emojiPopup;
    private ViewGroup rootView;
    private ImageView emojiButton;
    int lastViewedPosition;

    Toolbar toolbar, searchtollbar;
    Menu search_menu;
    MenuItem item_search;

    public SharedPreferences appPreferences;
    boolean isAppInstalled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_chat);
        setSearchtollbar();

        System.out.println("the create method :");
        dataser();

        appPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    }

    private void setSearchtollbar() {

        searchtollbar = (Toolbar) findViewById(R.id.searchtoolbar);
        if (searchtollbar != null) {
            searchtollbar.inflateMenu(R.menu.menu_search);
            search_menu = searchtollbar.getMenu();

            searchtollbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        circleReveal(R.id.searchtoolbar, 1, true, false);
                    else
                        searchtollbar.setVisibility(View.GONE);
                }
            });

            item_search = search_menu.findItem(R.id.action_filter_search);

            MenuItemCompat.setOnActionExpandListener(item_search, new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    // Do something when collapsed
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        circleReveal(R.id.searchtoolbar, 1, true, false);
                    } else
                        searchtollbar.setVisibility(View.GONE);
                    return true;
                }

                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    // Do something when expanded
                    return true;
                }
            });

            initSearchView();


        } else
            Log.d("toolbar", "setSearchtollbar: NULL");

    }

    public void initSearchView() {
        final SearchView searchView =
                (SearchView) search_menu.findItem(R.id.action_filter_search).getActionView();

        // Enable/Disable Submit button in the keyboard

        searchView.setSubmitButtonEnabled(false);

        // Change search close button image

        ImageView closeButton = (ImageView) searchView.findViewById(R.id.search_close_btn);
        closeButton.setImageResource(R.drawable.ic_close_24dp);


        // set hint and the text colors
        EditText txtSearch = ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text));
        txtSearch.setHint("Search..");
        txtSearch.setHintTextColor(Color.DKGRAY);
        txtSearch.setTextColor(getResources().getColor(R.color.colorPrimary));


        // set the cursor

        AutoCompleteTextView searchTextView = (AutoCompleteTextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchTextView, R.drawable.search_cursor); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
        } catch (Exception e) {
            e.printStackTrace();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                callSearch(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                callSearch(newText);
                return true;
            }

            public void callSearch(String query) {
                //Do searching
                Log.i("query", "" + query);

            }

        });

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void circleReveal(int viewID, int posFromRight, boolean containsOverflow, final boolean isShow) {

        final View myView = findViewById(viewID);

        int width = myView.getWidth();

        if (posFromRight > 0)
            width -= (posFromRight * getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material)) - (getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) / 2);
        if (containsOverflow)
            width -= getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material);

        int cx = width;
        int cy = myView.getHeight() / 2;

        Animator anim;
        if (isShow)
            anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, (float) width);
        else
            anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, (float) width, 0);

        anim.setDuration((long) 220);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isShow) {
                    super.onAnimationEnd(animation);
                    myView.setVisibility(View.INVISIBLE);
                }
            }
        });

        // make the view visible and start the animation
        if (isShow)
            myView.setVisibility(View.VISIBLE);

        // start the animation
        anim.start();

    }

    private void ShortcutIcon() {
        isAppInstalled = appPreferences.getBoolean("isAppInstalled", false);

        if (isAppInstalled == false) {
            Intent shortcutIntent = new Intent(getApplicationContext(), ChatRoomActivity.class);
            shortcutIntent.setAction(Intent.ACTION_CREATE_SHORTCUT);
            Intent intent = new Intent();
            shortcutIntent.putExtra("duplicate", false);
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name1.getText());
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource
                    .fromContext(getApplicationContext(), img.length()));

//                Intent i1 = new Intent(context, ChatRoomActivity.class);

//                Bundle b = new Bundle();
            intent.putExtra("name", name1.getText().toString());
            intent.putExtra("RECIPIENT_ID", recipientId);
            intent.putExtra("img", img);
            intent.putExtra("where", "in");
//                i1.putExtras(b);

//                sharedPreferences.edit().putString("userImg",img.toString()).commit();
//                context.startActivity(i1);

            intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            getApplicationContext().sendBroadcast(intent);

            //Make preference true
            SharedPreferences.Editor editor = appPreferences.edit();
            editor.putBoolean("isAppInstalled", true);
            editor.commit();

        }
    }

    void dataser() {
        name1 = (TextView) findViewById(R.id.textchatername);
        txtState = (TextView) findViewById(R.id.txtState);
        messageList = (ListView) findViewById(R.id.listChat);
        imgUser = (ImageView) findViewById(R.id.imgUser);
        btnCall = (ImageView) findViewById(R.id.imgCall);
        menuDown = (ImageView) findViewById(R.id.menu_down);
        menuCamera = (ImageView) findViewById(R.id.menu_camera);
        btnVideo1 = (ImageView) findViewById(R.id.imgVdoCall);
        imgOption = (ImageView) findViewById(R.id.imgOption);
        header = (RelativeLayout) findViewById(R.id.rl);
        users = new ArrayList();

        txtState.setText("offline");

        db = new DatabaseHandler(this);

        this.mSharedPreferences = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);
        this.username = this.mSharedPreferences.getString(Constants.USER_NAME, "");

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        sharedPreferences = getSharedPreferences("pref", Context.MODE_PRIVATE);
        bindService(new Intent(this, MessageService.class), serviceConnection, BIND_AUTO_CREATE);

        Bundle intent = getIntent().getExtras();
        name = intent.getString("name");
        currentUserId = sharedPreferences.getString("mymobile", "");
        System.out.println("the my mobile is :" + currentUserId);
        name1.setText("" + name);
        recipientId = intent.getString("RECIPIENT_ID");
        where = intent.getString("where");
        System.out.println("the recieve mobile is : " + where);
        sharedPreferences.edit().putString("rec_no", recipientId).commit();
        img = intent.getString("img");

        // String myimg = sharedpreferences.getString("myImage","");

        Picasso.with(context).load(img).placeholder(R.drawable.img_pic).transform(new CircleTransform()).into(imgUser);

        imgOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(ChatRoomActivity.this, imgOption);
                popupMenu.getMenuInflater().inflate(R.menu.menu_chat, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case menu_view_contact:
                                Intent i = new Intent(ChatRoomActivity.this, ViewContactActivity.class);
                                i.putExtra("number", recipientId);
                                i.putExtra("image", img);
                                i.putExtra("name", name1.getText());
                                startActivity(i);

                                return true;
                            case menu_media:
                                startActivity(new Intent(ChatRoomActivity.this, MediaActivity.class));

                                return true;
                            case menu_search:

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                    circleReveal(R.id.searchtoolbar, 1, true, true);
                                else
                                    searchtollbar.setVisibility(View.VISIBLE);

                                item_search.expandActionView();
                                return true;
                            case menu_mute:

                                return true;
                            case menu_wallpaper:
                                DialogMenuWallpaperActivity menuWallpaper = new DialogMenuWallpaperActivity(ChatRoomActivity.this);
                                menuWallpaper.getWindow().setTitle("Wallpaper");
                                menuWallpaper.show();

                                return true;
                            case menu_block:

                                return true;
                            case menu_clear_chat:

                                return true;
                            case menu_email_chat:

                                return true;
                            case menu_add_shortcut:
                                ShortcutIcon();
                                return true;
                            default:
                                return true;
                        }
                    }


                });

                popupMenu.show();
            }
        });


        btnVideo1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("img_call", "" + sharedPreferences.getString("myImage", ""));
                Call call = getSinchServiceInterface().callUserVideo(recipientId, headers);
                String callId = call.getCallId();
                Intent callScreen = new Intent(ChatRoomActivity.this, CallScreenActivity.class);
                callScreen.putExtra(MessageService.CALL_ID, callId);
                callScreen.putExtra("img", img);
                startActivity(callScreen);
            }
        });

        //Intent intent = getIntent();
        messageBodyField = (EmojiEditText) findViewById(R.id.editMessage);
        send = (FancyButton) findViewById(R.id.buttonsend);
        data = new ArrayList<GetData>();

        sinchClient = ((MyWTF) this.getApplication()).getSomeVariable();
        header.setGravity(View.VISIBLE);

        mAdapter = new ChatListAdapter(context, currentUserId, data, messageList);
        messageList.setAdapter(mAdapter);
        messageList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        messageList.clearTextFilter();

        // Capture ListView item click

        messageList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // Capture total checked items
                final int checkedCount = messageList.getCheckedItemCount();
                // Set the CAB title according to total checked items
                mode.setTitle(checkedCount + " Selected");
                // Calls toggleSelection method from ListViewAdapter Class
                System.out.println("the onItem change state " + position);
                mAdapter.toggleSelection(position);
                sharedPreferences.edit().putInt("pos_list", position).commit();
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    /*case R.id.selectAll:
                        //
                        final int checkedCount  = data.size();
                        System.out.println("the size :" + checkedCount);
                        // If item  is already selected or checked then remove or
                        // unchecked  and again select all
                        mAdapter.removeSelection();
                        for (int i = 0; i <  checkedCount; i++)
                        {
                            messageList.setItemChecked(i,   true);
                            //  listviewadapter.toggleSelection(i);
                        }
                        // Set the  CAB title according to total checked items
                        // Calls  toggleSelection method from ListViewAdapter Class
                        // Count no.  of selected item and print it
                        mode.setTitle(checkedCount  + "  Selected");
                        return true;*/

                    case R.id.delete:
                        // Calls getSelectedIds method from ListViewAdapter Class
                        SparseBooleanArray selected = mAdapter.getSelectedIds();
                        for (int i = (selected.size() - 1); i >= 0; i--) {
                            if (selected.valueAt(i)) {
                                GetData selecteditem = mAdapter.getItem(selected.keyAt(i));
                                String delete_id = selecteditem.getMessage_Id();
                                mAdapter.remove(selecteditem);
                                db.delete_row(delete_id);
                            }
                        }
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                header.setVisibility(View.GONE);
                mode.getMenuInflater().inflate(R.menu.menu_delete, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                header.setVisibility(View.VISIBLE);
                mAdapter.removeSelection();
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }
        });

        messageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int pos, long l) {
                GetData message = mAdapter.getItem(pos);

                String t = message.getType();
                if (t.equals("Text") || t.equals("text"))
                    System.out.println("the link is  " + message.getBody());
                else if (t.equals("image")) {
                    Intent fullScreenIntent = new Intent(context, FullScreenImage.class);
                    fullScreenIntent.putExtra("image", "" + message.getImgLink());
                    fullScreenIntent.putExtra("thumb", "" + message.getBody());
                    context.startActivity(fullScreenIntent);
                } else if (t.equals("video")) {
                    Intent i = new Intent(context, VideoActivity.class);
                    i.putExtra("vdo", message.getBody());
                    context.startActivity(i);
                } else if (t.equals("audio")) {
                    Intent i = new Intent(context, AudioSelectActivity.class);
                    i.putExtra("audio", message.getBody());
                    context.startActivity(i);
                } else {
                    Intent i = new Intent(context, FDFOpen.class);
                    i.putExtra("pdf", message.getBody());
                    context.startActivity(i);
                }
            }
        });


        con = new ConnectionDetector(ChatRoomActivity.this);

        setListChat();
        receiveMessage();

        rootView = (ViewGroup) findViewById(R.id.main_activity_root_view);
        emojiButton = (ImageView) findViewById(R.id.main_activity_emoji);
        emojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                emojiPopup.toggle();
            }
        });

//        Bundle bundle = getIntent().getExtras();
//        String background = bundle.getString("wallpaper");
//
//        rootView.setBackgroundColor(Integer.parseInt(background));

        setUpEmojiPopup();

        if (con.isConnectingToInternet()) {
            pubnub = new Pubnub(PUBLISH_KEY, SUBSCRIBE_KEY);
            pubnub.setUUID(currentUserId);
            sharedPreferences.edit().putString("on" + recipientId, "f").commit();
            try {
                pubnub.subscribe("calling_channel",
                        new Callback() {
                            @Override
                            public void connectCallback(String channel, Object message) {
                                pubnub.publish("calling_channel", "Hello from the PubNub Java SDK", new Callback() {
                                });
                            }
                        });
            } catch (PubnubException e) {
                System.out.println("the exe in  subs:" + e);
            }
        } else {
            //callFunction("No internet connection");
        }
        if (con.isConnectingToInternet()) {
            subsribe();
            time();
        }

        messageBodyField.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                menuCamera.setVisibility(View.VISIBLE);
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                menuCamera.setVisibility(View.GONE);
            }

            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s.toString()) && s.toString().trim().length() == 1) {
                    menuCamera.setVisibility(View.GONE);
                    if (con.isConnectingToInternet()) {
                        typingStarted = true;
                        try {
                            Callback callback = new Callback() {
                                public void successCallback(String channel, Object response) {
                                    //System.out.println("the response type :"+response.toString());
                                }

                                public void errorCallback(String channel, PubnubError error) {
                                    // System.out.println(error.toString());
                                }
                            };
                            JSONObject jso = new JSONObject();
                            jso.put("type", "" + recipientId);
                            pubnub.setState("calling_channel", currentUserId, jso, callback);
                        } catch (Exception e) {
                            System.out.println("the user is type" + e);
                        }
                    } else {
                        System.out.println("the user is type else ");
                    }
                } else if (s.toString().trim().length() == 0 && typingStarted) {
                    menuCamera.setVisibility(View.VISIBLE);
                    typingStarted = false;
                    if (con.isConnectingToInternet()) {
                        try {
                            Callback callback = new Callback() {
                                public void successCallback(String channel, Object response) {
                                    //System.out.println("the response type :"+response.toString());
                                }

                                public void errorCallback(String channel, PubnubError error) {
                                    //System.out.println(error.toString());
                                }
                            };
                            JSONObject jso = new JSONObject();
                            jso.put("type", "false");
                            pubnub.setState("calling_channel", currentUserId, jso, callback);
                        } catch (Exception e) {
                            System.out.println("the user is eee" + e);
                        }
                    } else {
                        System.out.println("the user is else lenghth");
                    }
                }
            }
        });

        messageBodyField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (typingStarted) {
//                        menuCamera.setVisibility(View.GONE);
                        if (con.isConnectingToInternet()) {
                            try {
                                Callback callback = new Callback() {
                                    public void successCallback(String channel, Object response) {
                                        //System.out.println("the response type :"+response.toString());
                                    }

                                    public void errorCallback(String channel, PubnubError error) {
                                        //System.out.println(error.toString());
                                    }
                                };
                                JSONObject jso = new JSONObject();
                                jso.put("type", "" + recipientId);
                                pubnub.setState("calling_channel", currentUserId, jso, callback);
                            } catch (Exception e) {
                                System.out.println("the user is eee" + e);
                            }
                        } else {
                            System.out.println("the user is type flase");
                        }
                    } else {
//                        menuCamera.setVisibility(View.VISIBLE);
                        if (con.isConnectingToInternet()) {
                            try {
                                Callback callback = new Callback() {
                                    public void successCallback(String channel, Object response) {
                                        //System.out.println("the response type :"+response.toString());
                                    }

                                    public void errorCallback(String channel, PubnubError error) {
                                        //System.out.println(error.toString());
                                    }
                                };
                                JSONObject jso = new JSONObject();
                                jso.put("type", "false");
                                pubnub.setState("calling_channel", currentUserId, jso, callback);
                            } catch (Exception e) {
                                System.out.println("the user is eee" + e);
                            }
                        } else {
                            System.out.println("the user is else 2");
                        }
                    }
                }
            }
        });

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ChatRoomActivity.this, CallActivity.class);
                i.putExtra("call_id", recipientId);
                i.putExtra("call_img", img);
                startActivity(i);
            }
        });

        menuDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new BottomSheet.Builder(ChatRoomActivity.this)
                        .setSheet(R.menu.menu_grid)
                        .grid()
                        .setListener(ChatRoomActivity.this)
                        .show();
            }
        });

        menuCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String Bx1 = Build.MANUFACTURER;
                if (Bx1.equalsIgnoreCase("samsung")) {
                    System.out.println("the Device man" + Bx1);
                    String fileName = "new-photo-name.jpg";
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, fileName);
                    values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");
                    imageUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri);
                    intent.putExtra(android.provider.MediaStore.EXTRA_VIDEO_QUALITY, 1);
                    startActivityForResult(intent, MENU_CAMERA);
                } else {
                    String fileName = "new-photo-name.jpg";
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, fileName);
                    values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");
                    imageUri = context.getContentResolver().insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri);
                    intent.putExtra(android.provider.MediaStore.EXTRA_VIDEO_QUALITY, 1);
                    startActivityForResult(intent, MENU_CAMERA);
                }

            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                con = new ConnectionDetector(ChatRoomActivity.this);
                if (con.isConnectingToInternet()) {
                    messageBody = messageBodyField.getText().toString();
                    if (messageBody.isEmpty()) {
                        System.out.println("the enter text");
                        return;
                    } else {
                        try {
                            //messageService.sendMessage(recipientId, "Text");
                            messageBody = "";
                            //messageBody = StringEscapeUtils.escapeJava(messageBodyField.getText().toString());
                            messageBody = URLEncoder.encode(messageBodyField.getText().toString(), "UTF-8");

                            sendTextMessage("" + messageBody);

                            new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        Thread.sleep(3000);
                                    } catch (InterruptedException e) {
                                        System.out.println("the exe in send message :" + e);
                                    }
                                }
                            }).start();
                        } catch (Exception e) {
                            System.out.println("the mp else");
                            System.out.print("the Send : " + e);

                            AlertDialog.Builder al3 = new AlertDialog.Builder(ChatRoomActivity.this);
                            al3.setMessage("the Error send \n " + e);
                            al3.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                            al3.show();
                            return;
                        }
                    }
                    messageBodyField.setText("");
                } else
                    callFunction("No internet connection");
            }
        });
    }

    private void time() {
        if (sharedPreferences.getString("on" + recipientId, "").equals("t")) {
            txtState.setText("Online");
            if (sharedPreferences.getString("type", "").equals("true")) {
                txtState.setText("Typing..");
            } else {
                txtState.setText("Online");
            }
        } else {
            txtState.setText("offline");
            if (sharedPreferences.getString("last_seen" + recipientId, "").equals("")) {
                txtState.setText("offline");
            } else {
                txtState.setText("Last seen :" + sharedPreferences.getString("last_seen" + recipientId, ""));
            }
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                time();
            }
        }, 5000);
    }

    private void receiveMessage() {
        SendData = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                // String serverUrl = StaticService.getListofChat;

                String serverUrl = StaticService.get_last_message;
                System.out.println("the server url :" + serverUrl);

                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(serverUrl);
                try {
                    JSONObject msg = new JSONObject();

                    // sharedPreferences.edit().putString("last_id",jo.getString("message_id")).commit();
                    // sharedPreferences.edit().putString("last_from_id",jo.getString("fromid")).commit();
                    // sharedPreferences.edit().putString("last_to_id",jo.getString("toid")).commit();

                    msg.put("message_id", sharedPreferences.getString("last_id" + recipientId, ""));
                    msg.put("fromid", sharedPreferences.getString("last_from_id" + recipientId, currentUserId));
                    msg.put("toid", sharedPreferences.getString("last_to_id" + recipientId, recipientId));

                   /*
                    msg.put("fromid", currentUserId);
                    msg.put("toid", recipientId);
                   */

                    System.out.println("the json object tag :" + msg);
                    StringEntity se = new StringEntity(msg.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    post.setEntity(se);
                    HttpResponse response = client.execute(post);
                    HttpEntity entity = response.getEntity();
                    String responseText = EntityUtils.toString(entity);
                    response_json = new JSONObject(responseText);
                    System.out.println("the json response got list :" + response_json);
                } catch (Exception e) {
                    System.out.println("the exception in technique : " + e);
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                con = new ConnectionDetector(getApplicationContext());

                if (con.isConnectingToInternet()) {

                } else {
                    SendData.cancel(true);
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    if (response_json.getString("success").equals("1")) {
                        try {
                            // db.delete();
                            JSONArray j_id = new JSONArray();
                            JSONArray j = response_json.getJSONArray("posts");
                            data.clear();
                            mAdapter.notifyDataSetChanged();
                            for (int i = 0; i < j.length(); i++) {
                                JSONObject jo = j.getJSONObject(i);
                                String type = "" + jo.getString("type");
                                Bitmap b = null;

                                if (sharedPreferences.getString("last_id" + recipientId, "").equals(jo.getString("message_id"))) {
                                    System.out.println("the message is already got");
                                    return;
                                }
                                boolean check = db.presentId(jo.getString("message_id"));
                                System.out.println("the message is check:" + check);
                                if (check) {
                                    return;
                                }

                                if (type.equals("text") || type.equals("Text")) {
                                    db.addChat(new ChatModel(jo.getString("message_id"), jo.getString("time"),
                                            jo.getString("type"), jo.getString("fromid"), jo.getString("toid"),
                                            jo.getString("message"), "image", "video", "thumb", "" + b, "", jo.getString("status")));
                                } else if (jo.getString("type").equals("image")) {
                                    db.addChat(new ChatModel(jo.getString("message_id"), jo.getString("time"),
                                            jo.getString("type"), jo.getString("fromid"), jo.getString("toid"),
                                            "msg", jo.getString("image"), "video", jo.getString("image_thum"), "" + b, "", jo.getString("status")));
                                } else if (jo.getString("type").equals("video")) {
                                    db.addChat(new ChatModel(jo.getString("message_id"), jo.getString("time")
                                            , jo.getString("type"), jo.getString("fromid"), jo.getString("toid"),
                                            "msg", "image", jo.getString("video"), jo.getString("video_thum"), "" + b, "", jo.getString("status")));
                                } else if (jo.getString("type").equals("audio")) {
                                    db.addChat(new ChatModel(jo.getString("message_id"), jo.getString("time")
                                            , jo.getString("type"), jo.getString("fromid"), jo.getString("toid"),
                                            "msg", "image", "", "", "" + b, jo.getString("audio"), jo.getString("status")));
                                } else if (jo.getString("type").equals("pdf")) {
                                    db.addChat(new ChatModel(jo.getString("message_id"), jo.getString("time")
                                            , jo.getString("type"), jo.getString("fromid"), jo.getString("toid"),
                                            jo.getString("pdf"), "image", "", "", "" + b, "", jo.getString("status")));
                                } else {

                                }
                                sharedPreferences.edit().putString("last_id" + recipientId, jo.getString("message_id")).commit();
                                sharedPreferences.edit().putString("last_from_id" + recipientId, jo.getString("fromid")).commit();
                                sharedPreferences.edit().putString("last_to_id" + recipientId, jo.getString("toid")).commit();
                                j_id.put(jo.getString("message_id"));
                                messageService.sendMessage(recipientId, "" + jo.getString("message_id"));
                            }
                            sendReadReacipt(j_id);
                        } catch (Exception e) {
                            System.out.println("the exe is :" + e);
                        }
                        setListChat();
                    } else {
                        System.out.println("Something going wrong on server");
                    }
                } catch (Exception e) {
                    System.out.println("the technique exception is  " + e);
                }
            }
        };
        SendData.execute();
    }

    private void setListChat() {
        data.clear();

        List<ChatModel> contacts = db.getAllContacts();
        JSONArray j_id = new JSONArray();
        for (ChatModel cn : contacts) {
            if (cn.get_fromId().equals(currentUserId)) {
                if (cn.get_toId().equals(recipientId)) {
                    GetData m = new GetData();
                    m.setUid(cn.get_fromId());
                    m.setMessage_Id(cn.get_msgId());

                    if (cn.get_read().equals("read")) {
                        System.out.println("rr read message is SQLITE :" + cn.get_read());
                        m.setRead("read");
                    } else {
                        System.out.println("rr nop read message is SQLITE :" + cn.get_read());
                        m.setRead("");
                        j_id.put("" + cn.get_msgId());
                    }

                    if (cn.get_type().equals("text") || cn.get_type().equals("Text")) {
                        m.setBody(cn.get_messaeg());
                        m.setType("Text");
                    } else if (cn.get_type().equals("image")) {
                        m.setType("image");
                        m.setImgLink(cn.get_file());
                        m.setBody(cn.get_thumb());
                    } else if (cn.get_type().equals("video")) {
                        m.setType("video");
                        m.setImgLink(cn.get_thumb());
                        m.setBody(cn.get_video());

                    } else if (cn.get_type().equals("audio")) {
                        m.setType("audio");
                        m.setBody(cn.get_audio());
                    } else if (cn.get_type().equals("pdf")) {
                        m.setType("pdf");
                        m.setBody(cn.get_messaeg());
                    }
                    String v_date_str = cn.get_time();
                    String inputPattern = "dd-MMM-yyyy HH:mm:ss";
                    String outputPattern = "HH:mm:ss";
                    SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
                    SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
                    Date date = null;
                    String str = null;
                    try {
                        date = inputFormat.parse(v_date_str);
                        str = outputFormat.format(date);
                    } catch (Exception e) {
                        System.out.println("the date exe is :" + e);
                    }
                    m.setStatus(true);
                    m.setMsgtime(str);
                    m.setDelever("3");
                    data.add(m);
                    mAdapter.notifyDataSetChanged();
                    // messageList.smoothScrollToPosition(lastViewedPosition);
                }
            } else if (cn.get_fromId().equals(recipientId)) {
                if (cn.get_toId().equals(currentUserId)) {
                    GetData m = new GetData();
                    m.setUid(cn.get_fromId());
                    m.setMessage_Id(cn.get_msgId());
                    m.setRead("read");
                    if (cn.get_type().equals("text") || cn.get_type().equals("Text")) {
                        m.setBody(cn.get_messaeg());
                        m.setType("Text");
                    } else if (cn.get_type().equals("image")) {
                        m.setType("image");
                        m.setImgLink(cn.get_file());
                        m.setBody(cn.get_thumb());
                    } else if (cn.get_type().equals("video")) {
                        m.setType("video");
                        m.setImgLink(cn.get_thumb());
                        m.setBody(cn.get_video());
                    } else if (cn.get_type().equals("audio")) {
                        m.setType("audio");
                        m.setBody(cn.get_audio());
                    } else if (cn.get_type().equals("pdf")) {
                        m.setType("pdf");
                        m.setBody(cn.get_messaeg());
                    }

                    String v_date_str = cn.get_time();
                    String inputPattern = "dd-MMM-yyyy HH:mm:ss";
                    String outputPattern = "HH:mm:ss";
                    SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
                    SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
                    Date date = null;
                    String str = null;
                    try {
                        date = inputFormat.parse(v_date_str);
                        str = outputFormat.format(date);
                    } catch (Exception e) {
                        System.out.println("the date exe is :" + e);
                    }
                    m.setStatus(false);
                    m.setMsgtime(str);
                    data.add(m);
                    mAdapter.notifyDataSetChanged();
                    //  messageList.smoothScrollToPosition(lastViewedPosition);
                }
            }
        }
        System.out.println("rr not message read" + j_id.length());
        if (j_id.length() > 0)
            getReadReacipt(j_id);
        else {
            mAdapter.notifyDataSetChanged();
            System.out.println("rr all message read");
        }
    }

    private void getReadReacipt(final JSONArray j_id) {
        SendData = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                String serverUrl = StaticService.get_last_read;
                System.out.println("rr get message service is :" + serverUrl);
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(serverUrl);
                try {
                    JSONObject jo = new JSONObject();
                    jo.put("message_id", j_id);
                    System.out.println("rr send message array jo :" + jo);
                    StringEntity se = new StringEntity(jo.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    post.setEntity(se);
                    HttpResponse response = client.execute(post);
                    HttpEntity entity = response.getEntity();
                    String responseText = EntityUtils.toString(entity);
                    response_json = new JSONObject(responseText);
                    System.out.println("the response of new message  :" + response_json);
                } catch (Exception e) {
                    System.out.println("the exception in got message : " + e);
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                con = new ConnectionDetector(getApplicationContext());
                if (con.isConnectingToInternet()) {

                } else {
                    SendData.cancel(true);
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    if (response_json.getString("success").equals("1")) {
                        System.out.println("the success got read");
                        JSONArray j = response_json.getJSONArray("posts");


                        for (int i = 0; i < j.length(); i++) {
                            JSONObject jo = j.getJSONObject(i);
                            String mk = jo.getString("id");
                            if (jo.getString("status").equals("true")) {
                                System.out.println("rr read id :" + jo.getString("id"));
                                db.updateREAD(mk);
                                for (int k = 0; k < data.size(); k++) {
                                    GetData g = data.get(k);
                                    String mid = g.getMessage_Id();

                                    System.out.println("rr message id [" + k + "] :" + mid);

                                    if (mid.equals(mk)) {
                                        System.out.println("rr got id is :" + mk + " on " + k + " position");
                                        g.setRead("read");
                                        mAdapter.notifyDataSetChanged();
                                    } else {

                                    }
                                }
                            } else {
                                System.out.println("the not read id :" + jo.getString("id"));
                            }
                        }
                        //lastViewedPosition = messageList.getFirstVisiblePosition();
                        //setListChat();
                        //mAdapter.notifyDataSetChanged();
                        //messageList.smoothScrollToPosition(lastViewedPosition);
                        //sharedPreferences.edit().putString("last_read"+recipientId,""+message_id).commit();
                    } else {
                        System.out.println("the Something going wrong on server");
                    }
                } catch (Exception e) {
                    System.out.println("the message exception is  " + e);
                }
            }
        };
        SendData.execute();
    }

    private void callFunction(String s) {
        LayoutInflater li = LayoutInflater.from(ChatRoomActivity.this);
        View promptsView = li.inflate(R.layout.dialogloerror, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ChatRoomActivity.this);
        final TextView text = (TextView) promptsView.findViewById(R.id.textError);
        text.setText("" + s);
        text.setTypeface(Typeface.DEFAULT);
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setCancelable(false)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onDestroy() {
        sharedPreferences.edit().putString("rec_no", "").commit();

        con = new ConnectionDetector(getApplicationContext());
        if (con.isConnectingToInternet()) {
            unbindService(serviceConnection);
            pubnub.unsubscribe("calling_channel");
        } else {

        }

        super.onDestroy();
    }

    @Override
    public void onSheetShown(@NonNull BottomSheet bottomSheet) {

    }

    @Override
    public void onSheetItemSelected(@NonNull BottomSheet bottomSheet, MenuItem menuItem) {
        String t = "" + menuItem.getTitle();
        if (t.equals("Document")) {
            Intent intent = new Intent(getApplicationContext(), FileChooser.class);
            ArrayList<String> extensions = new ArrayList<String>();
            // extensions.add(".txt");
            // extensions.add(".doc");
            //extensions.add(".ppt");
            extensions.add(".pdf");
            //extensions.add(".apk");
            //extensions.add(".jpg");
            //extensions.add(".png");
            //extensions.add(".zip");
            //extensions.add(".rtf");
            //extensions.add(".gif");
            intent.putStringArrayListExtra("filterFileExtension", extensions);
            startActivityForResult(intent, REQUEST_TAKE_TEXT_FILE);
        } else if(t.equals("Camera"))
        {
            String Bx1= Build.MANUFACTURER;
            if(Bx1.equalsIgnoreCase("samsung"))
            {
                System.out.println("the Device man"+Bx1);
                String fileName = "new-photo-name.jpg";
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, fileName);
                values.put(MediaStore.Images.Media.DESCRIPTION,"Image capture by camera");
                imageUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri);
                intent.putExtra(android.provider.MediaStore.EXTRA_VIDEO_QUALITY, 1);
                startActivityForResult(intent, MENU_CAMERA);
            }
            else
            {
                String fileName = "new-photo-name.jpg";
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, fileName);
                values.put(MediaStore.Images.Media.DESCRIPTION,"Image capture by camera");
                imageUri = context.getContentResolver().insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri);
                intent.putExtra(android.provider.MediaStore.EXTRA_VIDEO_QUALITY, 1);
                startActivityForResult(intent, MENU_CAMERA);
            }
        }
        else if (t.equals("Gallery")) {
            Intent intent;
            if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
                intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            } else {
                intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            }

            intent.setType("image/* video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, REQUEST_TAKE_GALLERY_VIDEO);
        } else if (t.equals("Audio")) {
            Intent intent_upload = new Intent();
            intent_upload.setType("audio/*");
            intent_upload.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent_upload, MENU_AUDIO);

        } else if (t.equals("Location")) {
            GPSTracker gps = new GPSTracker(ChatRoomActivity.this);
            if (gps.canGetLocation()) {
                longitude = gps.getLongitude();
                latitude = gps.getLatitude();

                Bitmap googleMapThumbnail = getGoogleMapThumbnail(latitude + "", longitude + "");
                sendImagefile(googleMapThumbnail);
            } else {
                gps.showSettingsAlert();
            }
        } else if (t.equals("Contact")) {
            startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), MENU_CONTACT);
        } else
            System.out.println("the menu else");

    }

    @Override
    public void onSheetDismissed(@NonNull BottomSheet bottomSheet, @DismissEvent int i) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == MENU_CONTACT) {
                uriContact = data.getData();
                retrieveContactName();
            } else if (requestCode == MENU_AUDIO) {
                Uri selectedImageUri = data.getData();
                System.out.println("the selected audio uri : " + selectedImageUri);
                selectedVideoPath = getPath(selectedImageUri);
                System.out.println("the audio path :" + selectedVideoPath);
                sendVideoFile(selectedVideoPath, "audio");

               /* File imgFolder;
                File imgDir = new File(Environment.getExternalStorageDirectory().getPath()+"/Chat Messanger/Audio");
                if (!imgDir.exists())
                {
                    imgDir.mkdirs();
                    imgFolder = new File(Environment.getExternalStorageDirectory().getPath()+"/Chat Messanger/Audio/Send");
                    if(!imgFolder.exists())
                    {
                        imgFolder.mkdirs();
                        String sendImgDir = imgFolder.toString();
                        System.out.println("the  send Audio directory is :" + sendImgDir);
                    }
                    else
                    {

                    }
                }
                else
                {
                    System.out.println("the directory exist");
                    imgFolder = new File(Environment.getExternalStorageDirectory().getPath()+"/Chat Messanger/Audio/Send");
                    if(!imgFolder.exists())
                    {
                        imgFolder.mkdirs();
                        String sendImgDir = imgFolder.toString();
                        System.out.println("the  send Audio directory is :" + sendImgDir);
                    }
                    else
                    {

                    }
                }
                try
                {
                    Random generator = new Random();
                    int n = 10000;
                    n = generator.nextInt(n);
                    String fname = "CM"+ n;

                    f = new File(imgFolder + File.separator + fname + ".mp3");
                    try
                    {
                        byte[]  soundBytes;
                        InputStream inputStream = getContentResolver().openInputStream(Uri.fromFile(new File(selectedVideoPath)));
                        soundBytes = new byte[inputStream.available()];
                        soundBytes = toByteArray(inputStream);
                        System.out.println("the sound bytes : "+ soundBytes);
                        f.createNewFile();
                        FileOutputStream os = new FileOutputStream(f, true);
                        os.write(soundBytes);
                        os.close();
                    }
                    catch (Exception e)
                    {
                        System.out.println("the exception in onActivityResult audio " + e);
                    }
                }
                catch (Exception e)
                {
                    System.out.println("the send exe in write audio file :" + e);
                }*/
            } else if (requestCode == MENU_CAMERA) {
                Uri selectedImageUri = imageUri;
                System.out.println("the send url :" + imageUri);
                String path = getPath(selectedImageUri);
                Bitmap bm = decodeFile(path);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                File imgFolder;

                File imgDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Chat Messanger/Images");
                if (!imgDir.exists()) {
                    imgDir.mkdirs();
                    imgFolder = new File(Environment.getExternalStorageDirectory().getPath() + "/Chat Messanger/Images/Send");
                    if (!imgFolder.exists()) {
                        imgFolder.mkdirs();
                        String sendImgDir = imgFolder.toString();
                        System.out.println("the  send image directory is :" + sendImgDir);
                    } else {

                    }
                } else {
                    System.out.println("the directory exist");
                    imgFolder = new File(Environment.getExternalStorageDirectory().getPath() + "/Chat Messanger/Images/Send");
                    if (!imgFolder.exists()) {
                        imgFolder.mkdirs();
                        String sendImgDir = imgFolder.toString();
                        System.out.println("the  send image directory is :" + sendImgDir);
                    } else {

                    }
                }
                try {
                    Random generator = new Random();
                    int n = 10000;
                    n = generator.nextInt(n);
                    String fname = "CM" + n;
                    f = new File(imgFolder + File.separator + fname + ".jpg");
                    try {
                        f.createNewFile();
                        FileOutputStream fo = new FileOutputStream(f);
                        fo.write(stream.toByteArray());
                    } catch (Exception e) {
                        System.out.println("the exception in onActivityResult 0: " + e);
                    }
                } catch (Exception e) {
                    System.out.println("the send exe in write file :" + e);
                }
                Bitmap googleMapThumbnail = null;
                //messageService.sendMessage(recipientId,"image");
                sendImagefile(googleMapThumbnail);
            } else if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                Uri selectedImageUri = data.getData();
                System.out.println("the gallery String : " + selectedImageUri);

                if (selectedImageUri.toString().contains("images") || selectedImageUri.toString().contains("image") || selectedImageUri.toString().contains("png")) {
                    System.out.println("The image String : " + selectedImageUri);
                    imageUri = selectedImageUri;
                    InputStream imageStream = null;
                    try {
                        imageStream = getContentResolver().openInputStream(selectedImageUri);
                    } catch (Exception e) {
                        System.out.println("the exe is:" + e);
                    }

                    Bitmap bmp = BitmapFactory.decodeStream(imageStream);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    File imgFolder;
                    File imgDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Chat Messanger/Images");
                    if (!imgDir.exists()) {
                        imgDir.mkdirs();
                        imgFolder = new File(Environment.getExternalStorageDirectory().getPath() + "/Chat Messanger/Images/Send");
                        if (!imgFolder.exists()) {
                            imgFolder.mkdirs();
                            String sendImgDir = imgFolder.toString();
                            System.out.println("the  send image directory is :" + sendImgDir);
                        } else {

                        }
                    } else {
                        System.out.println("the directory exist");
                        imgFolder = new File(Environment.getExternalStorageDirectory().getPath() + "/Chat Messanger/Images/Send");
                        if (!imgFolder.exists()) {
                            imgFolder.mkdirs();
                            String sendImgDir = imgFolder.toString();
                            System.out.println("the  send image directory is :" + sendImgDir);
                        } else {

                        }
                    }
                    try {
                        Random generator = new Random();
                        int n = 10000;
                        n = generator.nextInt(n);
                        String fname = "CM" + n;
                        f = new File(imgFolder + File.separator + fname + ".jpg");
                        try {
                            f.createNewFile();
                            FileOutputStream fo = new FileOutputStream(f);
                            fo.write(stream.toByteArray());
                        } catch (Exception e) {
                            System.out.println("the exception in onActivityResult 0: " + e);
                        }
                    } catch (Exception e) {
                        System.out.println("the send exe in write file :" + e);
                    }
                    Bitmap googleMapThumbnail = null;
                    sendImagefile(googleMapThumbnail);
                } else //if (selectedImageUri.toString().contains("video"))
                {
                    System.out.println("The video String : " + selectedImageUri);
                    selectedVideoPath = getPath(selectedImageUri);
                    System.out.println("The video path : " + selectedVideoPath);

                    if (selectedVideoPath != null) {
                        System.out.println("The vv video path :" + selectedVideoPath);
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(getApplicationContext(), selectedImageUri);
                        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        long millis = Long.parseLong(time);
                        long second = (millis / 1000) % 60;
                        long minute = (millis / (1000 * 60)) % 60;
                        long hour = (millis / (1000 * 60 * 60)) % 24;
                        System.out.println("the time Minut :" + minute + " Second :" + second);
                        String time2 = String.format("%02d:%02d", minute, second);
                        //System.out.println("the time :" + time2);
                        if (minute <= 10) {
                            sendVideoFile(selectedVideoPath, "video");
                            //  Toast.makeText(getApplicationContext(), "Video file :"+selectedVideoPath, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Video file is big", Toast.LENGTH_LONG).show();
                            selectedVideoPath = "";
                        }
                        // messageService.sendMessage(recipientId,"image");
                    }
                    /*else
                    {
                        System.out.println("The vv video else :" + selectedVideoPath);
                    }*/
                }
            } else if (requestCode == REQUEST_TAKE_TEXT_FILE) {
                String fileSelected = data.getStringExtra("fileSelected");
                System.out.println("the file is :" + fileSelected);
                sendPdfFile(fileSelected);
                //Toast.makeText(getApplicationContext(),"File select :"+fileSelected,Toast.LENGTH_SHORT).show();
            } else {
                System.out.println("The else part");
            }
        }
    }

    private void sendPdfFile(final String fileSelected) {
        System.out.println("the pdf is : " + fileSelected);
        m1 = new GetData();
        m1.setUid(currentUserId);
        m1.setImgLink("" + fileSelected);
        m1.setType("pdf");
        m1.setRead("");

        String outputPattern = "HH:mm:ss";
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
        Date date = null;
        String str = null;

        try {
            //date = inputFormat.parse(v_date_str);
            str = outputFormat.format(new Date());
        } catch (Exception e) {
            System.out.println("the date exe send is :" + e);
        }
        System.out.println("the date send is :" + str);

        m1.setMsgtime(str);
        m1.setStatus(true);
        m1.setDelever("1");


        Random generator = new Random();
        int n = 1000000;
        n = generator.nextInt(n);
        String fname = "CV" + n;

        System.out.println("the message id :" + fname);
        mId = fname;
        m1.setMessage_Id(mId);

        data.add(m1);
        mAdapter.notifyDataSetChanged();

        SendData = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                System.gc();
                String serverUrl = StaticService.sendMessage;
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(serverUrl);
                try {
                    File tempFile = new File(fileSelected);

                    String encodedString = null;
                    InputStream inputStream = null;
                    try {
                        inputStream = new FileInputStream(tempFile);
                    } catch (Exception e) {

                    }
                    byte[] bytes;
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    try {
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            output.write(buffer, 0, bytesRead);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bytes = output.toByteArray();
                    encodedString = Base64.encodeToString(bytes, Base64.DEFAULT);

                    System.out.println("the encode pdf string :" + encodedString);

                    JSONObject msg = new JSONObject();

                    /*Random generator = new Random();
                    int n = 1000000;
                    n = generator.nextInt(n);
                    String fname = "CV"+ n;

                    System.out.println("the message id :" + fname);
                    mId = fname;
                    */

                    msg.put("message_id", mId);
                    msg.put("fromid", currentUserId);
                    msg.put("toid", recipientId);
                    msg.put("message", "");
                    msg.put("type", "pdf");
                    msg.put("file", encodedString);

                    System.out.println("the send jo :" + msg);

                    StringEntity se = new StringEntity(msg.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    post.setEntity(se);
                    HttpResponse response = client.execute(post);
                    HttpEntity entity = response.getEntity();
                    String responseText = EntityUtils.toString(entity);
                    response_json = new JSONObject(responseText);

                    System.out.println("the response  in video : " + response_json);
                } catch (Exception e) {
                    System.out.println("the vv exception in technique : " + e);
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                con = new ConnectionDetector(getApplicationContext());
                if (con.isConnectingToInternet()) {

                } else {
                    SendData.cancel(true);
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    if (response_json.getString("success").equals("1")) {
                        messageService.sendMessage(recipientId, "pdf");
                        if (m1.getDelever().equals("3")) {

                        } else {
                            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.knob);
                            Ringtone r = RingtoneManager.getRingtone(context.getApplicationContext(), uri);
                            r.play();
                            m1.setDelever("2");
                            mAdapter.notifyDataSetChanged();
                            // messageService.sendMessage(recipientId,"video");
                        }
                    } else {
                        System.out.println("Something going wrong on server");
                    }
                } catch (Exception e) {
                    System.out.println("the technique exception is  " + e);
                }
            }
        };
        SendData.execute();
    }

    private void sendTextMessage(String s) {
        m1 = new GetData();
        m1.setUid(currentUserId);
        m1.setBody(messageBody);
        m1.setType("Text");
        m1.setRead("");

        String outputPattern = "HH:mm:ss";
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
        Date date = null;
        String str = null;
        try {
            str = outputFormat.format(new Date());
        } catch (Exception e) {
            System.out.println("the date exe send is :" + e);
        }
        System.out.println("the date send is :" + str);

        m1.setMsgtime(str);
        m1.setStatus(true);
        m1.setDelever("1");

        Random generator = new Random();
        int n = 1000000;
        n = generator.nextInt(n);
        String fname = "CV" + n;

        System.out.println("the message id :" + fname);
        mId = fname;
        m1.setMessage_Id(mId);

        data.add(m1);
        mAdapter.notifyDataSetChanged();
        SendData = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                String serverUrl = StaticService.sendMessage;
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(serverUrl);
                try {
                    JSONObject msg = new JSONObject();

                    /*Random generator = new Random();
                    int n = 1000000;
                    n = generator.nextInt(n);
                    String fname = "CV"+ n;
                    mId = fname;*/

                    msg.put("message_id", mId);
                    msg.put("fromid", currentUserId);
                    msg.put("toid", recipientId);
                    msg.put("message", messageBody);
                    msg.put("type", "Text");
                    System.out.println("the send message is :" + msg);
                    StringEntity se = new StringEntity(msg.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    post.setEntity(se);
                    HttpResponse response = client.execute(post);
                    HttpEntity entity = response.getEntity();
                    String responseText = EntityUtils.toString(entity);
                    response_json = new JSONObject(responseText);
                    System.out.println("the json response :" + responseText);
                } catch (Exception e) {
                    System.out.println("the exception in technique : " + e);
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                con = new ConnectionDetector(getApplicationContext());
                if (con.isConnectingToInternet()) {

                } else {
                    SendData.cancel(true);
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    if (response_json.getString("success").equals("1")) {
                        messageService.sendMessage(recipientId, "Text");
                        if (m1.getDelever().equals("3")) {

                        } else {
                            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.knob);
                            Ringtone r = RingtoneManager.getRingtone(context.getApplicationContext(), uri);
                            r.play();
                            m1.setDelever("2");
                            mAdapter.notifyDataSetChanged();
                        }
                    } else {
                        System.out.println("Something going wrong on server");
                    }
                } catch (Exception e) {
                    System.out.println("the technique exception is  " + e);
                }
            }
        };
        SendData.execute();
    }

    private void sendImagefile(final Bitmap googleMapThumbnail) {

        m1 = new GetData();
        m1.setUid(currentUserId);
        m1.setImgLink("" + imageUri);
        m1.setType("image");
        m1.setRead("");

        m1.setBody("" + imageUri);

        String outputPattern = "HH:mm:ss";
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
        Date date = null;
        String str = null;
        try {
            //date = inputFormat.parse(v_date_str);
            str = outputFormat.format(new Date());
        } catch (Exception e) {
            System.out.println("the date exe send is :" + e);
        }
        System.out.println("the date send is :" + str);

        m1.setMsgtime(str);
        m1.setStatus(true);
        m1.setDelever("1");


        Random generator = new Random();
        int n = 1000000;
        n = generator.nextInt(n);
        String fname = "CV" + n;

        System.out.println("the message id :" + fname);
        mId = fname;
        m1.setMessage_Id(mId);

        data.add(m1);
        mAdapter.notifyDataSetChanged();
        SendData = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                System.gc();

                String serverUrl = StaticService.sendMessage;
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(serverUrl);
                try {
                    Bitmap bm = null;
                    if (googleMapThumbnail == null) {
                        FileInputStream fis = null;
                        try {
                            fis = new FileInputStream(f.getAbsolutePath());
                        } catch (FileNotFoundException e) {
                            System.out.println("the exe send in file ");
                        }
                        bm = BitmapFactory.decodeStream(fis);
                    } else {
                        System.out.println("the thumbnail is work");
                        bm = googleMapThumbnail;
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                    byte[] b = baos.toByteArray();
                    String encImage = Base64.encodeToString(b, Base64.DEFAULT);
                    System.out.println("the image string is :" + encImage);

                    JSONObject msg = new JSONObject();
                   /* Random generator = new Random();
                    int n = 1000000;
                    n = generator.nextInt(n);

                    String fname = "CV"+ n;

                    mId = fname;
                   */
                    msg.put("message_id", mId);
                    msg.put("fromid", currentUserId);
                    msg.put("toid", recipientId);
                    msg.put("message", "");
                    msg.put("type", "image");
                    msg.put("file", encImage);

                    System.out.println("the image send is  :" + msg);

                    StringEntity se = new StringEntity(msg.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    post.setEntity(se);
                    HttpResponse response = client.execute(post);
                    HttpEntity entity = response.getEntity();
                    String responseText = EntityUtils.toString(entity);
                    response_json = new JSONObject(responseText);
                } catch (Exception e) {
                    System.out.println("the vv exception in technique : " + e);
                }
                return null;
            }


            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                con = new ConnectionDetector(getApplicationContext());
                if (con.isConnectingToInternet()) {

                } else {
                    SendData.cancel(true);
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    if (response_json.getString("success").equals("1")) {
                        messageService.sendMessage(recipientId, "image");
                        if (m1.getDelever().equals("3")) {

                        } else {
                            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.knob);
                            Ringtone r = RingtoneManager.getRingtone(context.getApplicationContext(), uri);
                            r.play();
                            m1.setDelever("2");
                            mAdapter.notifyDataSetChanged();
                            // messageService.sendMessage(recipientId,"video");
                        }
                    } else {
                        System.out.println("Something going wrong on server");
                    }
                } catch (Exception e) {
                    System.out.println("the technique exception is  " + e);
                }
            }
        };
        SendData.execute();
    }

    private void sendVideoFile(final String selectedVideoPath, final String type) {
        System.out.println("the video is : " + selectedVideoPath);
        m1 = new GetData();
        m1.setUid(currentUserId);
        m1.setRead("");

        /* if(type.equals("video"))
        {
            final Bitmap bmThumbnail = ThumbnailUtils.createVideoThumbnail(selectedVideoPath, MediaStore.Video.Thumbnails.MICRO_KIND);
            m1.setBitmap(bmThumbnail);
        }
        */
        // m1.setBody(selectedVideoPath);

        m1.setImgLink("" + selectedVideoPath);
        m1.setType(type);
        String outputPattern = "HH:mm:ss";
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
        Date date = null;
        String str = null;
        try {
            //date = inputFormat.parse(v_date_str);
            str = outputFormat.format(new Date());
        } catch (Exception e) {
            System.out.println("the date exe send is :" + e);
        }
        System.out.println("the date send is :" + str);
        m1.setMsgtime(str);
        m1.setStatus(true);
        m1.setDelever("1");

        Random generator = new Random();
        int n = 1000000;
        n = generator.nextInt(n);
        String fname = "CV" + n;

        System.out.println("the message id :" + fname);
        mId = fname;
        m1.setMessage_Id(mId);

        data.add(m1);
        mAdapter.notifyDataSetChanged();
        SendData = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                System.gc();
                String serverUrl = StaticService.sendMessage;
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(serverUrl);
                try {
                    File tempFile = new File(selectedVideoPath);
                    String encodedString = null;
                    InputStream inputStream = null;
                    try {
                        inputStream = new FileInputStream(tempFile);
                    } catch (Exception e) {

                    }
                    byte[] bytes;
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    try {
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            output.write(buffer, 0, bytesRead);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bytes = output.toByteArray();
                    encodedString = Base64.encodeToString(bytes, Base64.DEFAULT);

                    String th = "";
                    if (type.equals("video")) {
                        Bitmap bmThumbnail = ThumbnailUtils.createVideoThumbnail(selectedVideoPath, MediaStore.Video.Thumbnails.MICRO_KIND);
                        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
                        bmThumbnail.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOS);
                        th = Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
                        System.out.println("the encode string thumb:" + th);
                    } else
                        th = "";

                    JSONObject msg = new JSONObject();

                    msg.put("message_id", mId);
                    msg.put("fromid", currentUserId);
                    msg.put("toid", recipientId);
                    msg.put("message", "" + th);
                    msg.put("type", type);
                    msg.put("file", encodedString);

                    System.out.println("the send jo :" + msg);

                    StringEntity se = new StringEntity(msg.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    post.setEntity(se);
                    HttpResponse response = client.execute(post);
                    HttpEntity entity = response.getEntity();
                    String responseText = EntityUtils.toString(entity);
                    response_json = new JSONObject(responseText);

                    System.out.println("the response  in video : " + response_json);
                } catch (Exception e) {
                    System.out.println("the vv exception in technique : " + e);
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                con = new ConnectionDetector(getApplicationContext());
                if (con.isConnectingToInternet()) {

                } else {
                    SendData.cancel(true);
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    if (response_json.getString("success").equals("1")) {
                        messageService.sendMessage(recipientId, type);
                        if (m1.getDelever().equals("3")) {

                        } else {
                            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.knob);
                            Ringtone r = RingtoneManager.getRingtone(context.getApplicationContext(), uri);
                            r.play();
                            m1.setDelever("2");
                            mAdapter.notifyDataSetChanged();
                            // messageService.sendMessage(recipientId,"video");
                        }
                    } else {
                        System.out.println("Something going wrong on server");
                    }
                } catch (Exception e) {
                    System.out.println("the technique exception is  " + e);
                }
            }
        };
        SendData.execute();
    }

    private String getRealPathFromURI(Uri tempUri) {
        Cursor cursor = getContentResolver().query(tempUri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    private String getPath(Uri selectedImageUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(selectedImageUri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    private void retrieveContactName() {
        // querying contact data store
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);
        if (cursor.moveToFirst()) {
            // DISPLAY_NAME = The display name for the contact.
            // HAS_PHONE_NUMBER =   An indicator of whether this contact has at least one phone number.
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }

        cursor.close();
        System.out.println("the contact name :" + contactName);
        retrieveContactNumber();
    }

    private void retrieveContactNumber() {
        // getting contacts ID
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);
        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }
        cursorPhone.close();

        LayoutInflater li = LayoutInflater.from(ChatRoomActivity.this);
        View promptsView = li.inflate(R.layout.dialogsendcontact, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ChatRoomActivity.this);
        final TextView name = (TextView) promptsView
                .findViewById(R.id.textcontactname);
        final TextView no = (TextView) promptsView
                .findViewById(R.id.textcontactno);
        name.setText(contactName);
        // name.setTypeface(Typeface.DEFAULT);
        // set prompts.xml to alertdialog builder
        no.setText(contactNumber);
        alertDialogBuilder.setView(promptsView);

        alertDialogBuilder

                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        messageBody = contactName + " \n " + contactNumber;
                        // messageService.sendMessage(recipientId, "Text");
                        sendTextMessage("" + messageBody);


                    }
                });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog1, int which) {
                dialog1.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    public static Bitmap getGoogleMapThumbnail(String lati1, String longi1) {
        String URL = "http://maps.google.com/maps/api/staticmap?center=" + lati1 + "," + longi1 + "&markers=color:red%7Clabel:C%7C" + lati1 + "," + longi1 + "&zoom=15&size=300x300&sensor=false";
        System.out.println("the url bitmap is:" + URL);
        Bitmap bmp = null;
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet request = new HttpGet(URL);
        InputStream in = null;
        try {
            in = httpclient.execute(request).getEntity().getContent();
            bmp = BitmapFactory.decodeStream(in);
            in.close();
        } catch (IOException e) {
            System.out.println("the exe e :" + e);
        }
        return bmp;
    }

    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            messageService = (MessageService.MessageServiceInterface) iBinder;
            messageService.addMessageClientListener(messageClientListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            messageService = null;
        }
    }

    private class MyMessageClientListener implements MessageClientListener {
        @Override
        public void onIncomingMessage(MessageClient messageClient, final Message message) {
            System.out.println("the incoming Message Type : " + message.getTextBody());
            if (message.getTextBody().toString().length() > 7) {
                System.out.println("rr read message is live :" + message.getTextBody());

                String mk = message.getTextBody();
                db.updateREAD(mk);

                // lastViewedPosition = messageList.getFirstVisiblePosition();
                // setListChat();
                // mAdapter.notifyDataSetChanged();
                // messageList.smoothScrollToPosition(lastViewedPosition);

                for (int k = 0; k < data.size(); k++) {
                    GetData g = data.get(k);
                    String mid = g.getMessage_Id();
                    System.out.println("rr message id [" + k + "] :" + mid);

                    if (mid.equals(mk)) {
                        System.out.println("rr got id is :" + mk + " on " + k + " position");
                        g.setRead("read");
                        mAdapter.notifyDataSetChanged();
                    } else {

                    }
                }
                sharedPreferences.edit().putString("last_read" + recipientId, "" + message.getTextBody()).commit();
            } else {
                final String id = message.getMessageId();
                old_id = sharedPreferences.getString("lastMsg" + recipientId, "");
                if (old_id.equals(id)) {
                    System.out.println("the last message id is inside:" + old_id);
                    return;
                }
                old_id = id;
                sharedPreferences.edit().putString("lastMsg" + recipientId, old_id).commit();

                new Thread(new Runnable() {
                    public void run() {
                        try {
                        /*if(message.getTextBody().equals("Text"))
                            Thread.sleep(5000);
                        else if(message.getTextBody().equals("image"))
                            Thread.sleep(5000);
                        else */

                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            System.out.println("the exe in got :" + e);
                        }

                        if (message.getSenderId().equals(recipientId)) {
                            getMessage(id);
                        } else {

                        }
                    }
                }).start();
            }
        }

        @Override
        public void onMessageSent(MessageClient messageClient, final Message message, String s) {
            System.out.println("the sent Message Id : " + message.getMessageId() + "  Type : " + message.getTextBody() + " Reciever Id  :" + message.getRecipientIds());
            try {
                if (message.getTextBody() != null) {
                    if (message.getTextBody().toString().length() > 7) {
                        System.out.println("rr id send :" + message.getTextBody());
                    } else {
                        SendData = new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                System.gc();

                                String serverUrl = StaticService.update_message_id;
                                HttpClient client = new DefaultHttpClient();
                                HttpPost post = new HttpPost(serverUrl);
                                try {
                                    JSONObject msg = new JSONObject();
                                    String id = message.getMessageId();

                                    System.out.println("the new id :" + id + " old id :" + mId);

                                    msg.put("old_id", mId);
                                    msg.put("new_id", id);

                                    System.out.println("the update send jo " + msg);

                                    StringEntity se = new StringEntity(msg.toString());
                                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                                    post.setEntity(se);
                                    HttpResponse response = client.execute(post);
                                    HttpEntity entity = response.getEntity();
                                    String responseText = EntityUtils.toString(entity);
                                    response_json = new JSONObject(responseText);
                                    System.out.println("the update response " + response_json);
                                } catch (Exception e) {
                                    System.out.println("the vv exception in technique : " + e);
                                }
                                return null;
                            }

                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                                con = new ConnectionDetector(getApplicationContext());
                                if (con.isConnectingToInternet()) {

                                } else {
                                    SendData.cancel(true);
                                }
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                try {
                                    if (response_json.getString("success").equals("1")) {
                                        JSONArray j = response_json.getJSONArray("posts");
                                        JSONObject msg = j.getJSONObject(0);

                                        String type = "" + msg.getString("type");
                                        if (sharedPreferences.getString("last_id" + recipientId, "").equals(msg.getString("message_id"))) {
                                            System.out.println("the message is already got");
                                            return;
                                        }

                                        boolean check = db.presentId(msg.getString("message_id"));
                                        System.out.println("the message is check:" + check);

                                        if (check) {
                                            return;
                                        }

                                        if (type.equals("text") || type.equals("Text")) {

                                        } else if (type.equals("image")) {
                                            m1.setType("image");
                                            m1.setImgLink(msg.getString("image"));
                                            m1.setBody(msg.getString("image_thum"));
                                        } else if (type.equals("video")) {
                                            m1.setType("video");
                                            m1.setImgLink(msg.getString("video_thum"));
                                        } else if (type.equals("audio")) {
                                            m1.setType("audio");
                                            m1.setBody(msg.getString("audio"));
                                        } else if (type.equals("pdf")) {
                                            m1.setType("pdf");
                                            m1.setBody(msg.getString("pdf"));
                                        }
                                        if (m1.getDelever().equals("3")) {

                                        } else {
                                            m1.setDelever("2");
                                        }
                                        m1.setMessage_Id(msg.getString("message_id"));
                                        mAdapter.notifyDataSetChanged();
                                        Bitmap b = null;

                                        JSONObject jo = new JSONObject();
                                        jo = msg;

                                        if (type.equals("text") || type.equals("Text")) {
                                            db.addChat(new ChatModel(jo.getString("message_id"), jo.getString("time"),
                                                    jo.getString("type"), jo.getString("fromid"), jo.getString("toid"),
                                                    jo.getString("message"), "image", "video", "thumb", "" + b, "", jo.getString("status")));
                                        } else if (jo.getString("type").equals("image")) {
                                            System.out.println("the get image thumb :" + msg.getString("image_thum"));

                                            db.addChat(new ChatModel(jo.getString("message_id"), jo.getString("time"),
                                                    jo.getString("type"), jo.getString("fromid"), jo.getString("toid"),
                                                    "msg", jo.getString("image"), "video", jo.getString("image_thum"), "" + b, "", jo.getString("status")));
                                        } else if (jo.getString("type").equals("video")) {
                                            db.addChat(new ChatModel(jo.getString("message_id"), jo.getString("time")
                                                    , jo.getString("type"), jo.getString("fromid"), jo.getString("toid"),
                                                    "msg", "image", jo.getString("video"), jo.getString("video_thum"), "" + b, "", jo.getString("status")));

                                        } else if (jo.getString("type").equals("audio")) {
                                            db.addChat(new ChatModel(jo.getString("message_id"), jo.getString("time")
                                                    , jo.getString("type"), jo.getString("fromid"), jo.getString("toid"),
                                                    "msg", "image", "", "", "" + b, jo.getString("audio"), jo.getString("status")));
                                        } else if (jo.getString("type").equals("pdf")) {
                                            db.addChat(new ChatModel(jo.getString("message_id"), jo.getString("time")
                                                    , jo.getString("type"), jo.getString("fromid"), jo.getString("toid"),
                                                    jo.getString("pdf"), "image", "", "", "" + b, "", jo.getString("status")));
                                        } else {

                                        }
                                        sharedPreferences.edit().putString("last_id" + recipientId, jo.getString("message_id")).commit();
                                        sharedPreferences.edit().putString("last_from_id" + recipientId, jo.getString("fromid")).commit();
                                        sharedPreferences.edit().putString("last_to_id" + recipientId, jo.getString("toid")).commit();
                                    } else {
                                        System.out.println("the something going wrong on server");
                                    }
                                } catch (Exception e) {
                                    System.out.println("the technique exception is  " + e);
                                }
                            }
                        };
                        SendData.execute();
                    }
                } else {
                    System.out.println("the null put message");
                }
            } catch (Exception e) {
                System.out.println("the message ex :" + e);
            }
        }

        @Override
        public void onMessageFailed(MessageClient messageClient, Message message, MessageFailureInfo messageFailureInfo) {
            System.out.println("the send message failed to send");
            m1.setDelever("f");
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onMessageDelivered(MessageClient messageClient, MessageDeliveryInfo messageDeliveryInfo) {
            System.out.println("the message delevery :" + messageDeliveryInfo.getMessageId());

            try {
                if (m1.getDelever().equals("2")) {
                    // System.out.println("the sund already");
                } else {
                    Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.knob);
                    Ringtone r = RingtoneManager.getRingtone(context.getApplicationContext(), uri);
                    r.play();
                }
                // m1.setDelever("3");
                mAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                System.out.print("the exe in deliver :" + e);
            }
        }

        @Override
        public void onShouldSendPushData(MessageClient messageClient, Message message, List<PushPair> pushPairs) {

        }
    }

    private void getMessage(final String id) {
        boolean check = db.presentId(id);
        if (check) {
            return;
        }
        SendData = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                String serverUrl = StaticService.getMessage;
                System.out.println("the get message service is :" + serverUrl);
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(serverUrl);
                try {
                    JSONObject jo = new JSONObject();
                    jo.put("mesage_id", id);
                    System.out.println("the send jo :" + jo);
                    StringEntity se = new StringEntity(jo.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    post.setEntity(se);
                    HttpResponse response = client.execute(post);
                    HttpEntity entity = response.getEntity();
                    String responseText = EntityUtils.toString(entity);
                    response_json = new JSONObject(responseText);
                    System.out.println("the response of new message  :" + response_json);
                } catch (Exception e) {
                    System.out.println("the exception in got message : " + e);
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                con = new ConnectionDetector(getApplicationContext());
                if (con.isConnectingToInternet()) {

                } else {
                    SendData.cancel(true);
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    if (response_json.getString("success").equals("1")) {
                        JSONArray j = response_json.getJSONArray("posts");
                        sharedPreferences.edit().putString("json", "" + j).commit();
                        JSONObject msg = j.getJSONObject(0);
                        String type = "" + msg.getString("type");


                        if (sharedPreferences.getString("last_id" + recipientId, "").equals(msg.getString("message_id"))) {
                            System.out.println("the message is already got");
                            return;
                        }
                        GetData m = new GetData();
                        m.setUid(msg.getString("fromid"));
                        m.setMessage_Id(id);

                        if (type.equals("text") || type.equals("Text")) {
                            m.setBody(msg.getString("message"));
                            m.setType("Text");
                            System.out.println("the type is " + " txt inside");
                        } else if (type.equals("image")) {
                            m.setType("image");
                            m.setImgLink(msg.getString("image"));
                            m.setBody(msg.getString("image_thum"));
                        } else if (type.equals("video")) {
                            m.setType("video");
                            m.setImgLink(msg.getString("video_thum"));
                            m.setBody(msg.getString("video"));
                        } else if (type.equals("audio")) {
                            m.setType("audio");
                            m.setBody(msg.getString("audio"));
                        } else if (type.equals("pdf")) {
                            m.setType("pdf");
                            m.setBody(msg.getString("pdf"));
                        }

                        String v_date_str = msg.getString("time");
                        String inputPattern = "dd-MMM-yyyy HH:mm:ss";
                        String outputPattern = "HH:mm:ss";
                        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
                        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
                        Date date = null;
                        String str = null;
                        try {
                            date = inputFormat.parse(v_date_str);
                            str = outputFormat.format(date);
                        } catch (Exception e) {
                            System.out.println("the date exe is :" + e);
                        }
                        m.setStatus(false);
                        m.setMsgtime(str);

                        System.out.println("the here got is 1");
                        data.add(m);

                        try {
                            mAdapter.notifyDataSetChanged();
                            System.out.println("the notify data changed");
                        } catch (Exception e) {
                            System.out.println("the exe is :" + e);
                        }

                        Bitmap b = null;

                        JSONObject jo = new JSONObject();
                        jo = msg;

                        if (type.equals("text") || type.equals("Text")) {
                            db.addChat(new ChatModel(jo.getString("message_id"), jo.getString("time"),
                                    jo.getString("type"), jo.getString("fromid"), jo.getString("toid"),
                                    jo.getString("message"), "image", "video", "thumb", "" + b, "", jo.getString("status")));
                        } else if (jo.getString("type").equals("image")) {
                            db.addChat(new ChatModel(jo.getString("message_id"), jo.getString("time"),
                                    jo.getString("type"), jo.getString("fromid"), jo.getString("toid"),
                                    "msg", jo.getString("image"), "video", jo.getString("image_thum"), "" + b, "", jo.getString("status")));
                        } else if (jo.getString("type").equals("video")) {
                            db.addChat(new ChatModel(jo.getString("message_id"), jo.getString("time")
                                    , jo.getString("type"), jo.getString("fromid"), jo.getString("toid"),
                                    "msg", "image", jo.getString("video"), jo.getString("video_thum"), "" + b, "", jo.getString("status")));
                        } else if (jo.getString("type").equals("audio")) {
                            db.addChat(new ChatModel(jo.getString("message_id"), jo.getString("time")
                                    , jo.getString("type"), jo.getString("fromid"), jo.getString("toid"),
                                    "msg", "image", "", "", "" + b, jo.getString("audio"), jo.getString("status")));
                        } else if (jo.getString("type").equals("pdf")) {
                            db.addChat(new ChatModel(jo.getString("message_id"), jo.getString("time")
                                    , jo.getString("type"), jo.getString("fromid"), jo.getString("toid"),
                                    jo.getString("pdf"), "image", "", "", "" + b, "", jo.getString("status")));
                        } else {

                        }
                        sharedPreferences.edit().putString("last_id" + recipientId, jo.getString("message_id")).commit();
                        sharedPreferences.edit().putString("last_from_id" + recipientId, jo.getString("fromid")).commit();
                        sharedPreferences.edit().putString("last_to_id" + recipientId, jo.getString("toid")).commit();

                        if (sharedPreferences.getString("rec_no", "").equals(recipientId)) {
                            final JSONArray j_id = new JSONArray();
                            j_id.put(jo.getString("message_id"));
                            sharedPreferences.edit().putString("last_read" + recipientId, "" + jo.getString("message_id")).commit();
                            if (j_id.length() > 0) {
                                sendReadReacipt(j_id);
                                messageService.sendMessage(recipientId, jo.getString("message_id"));
                            }
                        } else
                            System.out.println("the ");
                    } else {
                        System.out.println("the Something going wrong on server");
                    }
                } catch (Exception e) {
                    System.out.println("the message exception is  " + e);
                }
            }
        };
        SendData.execute();
    }

    private void subsribe() {
        users.clear();
        pubnub.hereNow("calling_channel", new Callback() {
            public void successCallback(String channel, Object response) {
                try {
                    JSONObject hereNowResponse = new JSONObject(response.toString());
                    hereNowUuids = new JSONArray(hereNowResponse.get("uuids").toString());
                } catch (JSONException e) {
                    Log.d(JSON_EXCEPTION, e.toString());
                }

                String currentUuid;
                for (int i = 0; i < hereNowUuids.length(); i++) {
                    try {
                        currentUuid = hereNowUuids.get(i).toString();
                        if (!currentUuid.equals(pubnub.getUUID())) {
                            users.add(currentUuid);
                        }
                    } catch (JSONException e) {
                        System.out.println("the exe in user hear:" + e);
                    }
                }
                HashSet<String> hashSet = new HashSet<String>();
                hashSet.addAll(users);
                users.clear();
                users.addAll(hashSet);
                System.out.println("the user hear:" + users);
            }

            public void errorCallback(String channel, PubnubError e) {
                System.out.println("the exe in  subs:" + e);
            }
        });

        pubnub.getState("calling_channel", recipientId, new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                System.out.println("the success typing : " + message);
            }

            @Override
            public void errorCallback(String channel, PubnubError error) {
                System.out.println("the success error : " + error);
            }
        });

        try {
            pubnub.presence("calling_channel", new Callback() {
                @Override
                public void successCallback(String channel, Object message) {
                    try {
                        System.out.println("the presense  :" + message);
                        JSONObject jsonMessage = new JSONObject(message.toString());
                        String action = jsonMessage.get("action").toString();
                        String uuid = jsonMessage.get("uuid").toString();

                        if (uuid.equals(recipientId)) {
                            JSONObject data = jsonMessage.getJSONObject("data");
                            String type = data.getString("type").toString();

                            System.out.println("the typing is :" + type);

                            if (type.equals(currentUserId))
                                sharedPreferences.edit().putString("type", "true").commit();
                            else
                                sharedPreferences.edit().putString("type", "false").commit();
                        }
                        // sharedPreferences.edit().putString("last_seen",""+dt).commit();
                        if (!uuid.equals(pubnub.getUUID())) {
                            if (action.equals("state-change")) {
                                users.add(uuid);
                            }

                            if (action.equals("join")) {
                                users.add(uuid);
                            } else if (action.equals("leave")) {
                                if (uuid.equals(recipientId)) {
                                    String v_date_str = jsonMessage.get("timestamp").toString();
                                    String inputPattern = "EEE, dd MMM yyyy hh:mm:ss Z";
                                    String outputPattern = "hh:mm aa";
                                    SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
                                    SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
                                    Date date = null;
                                    String str = null;
                                    try {
                                        // date = inputFormat.parse(v_date_str);
                                        date = new Date();
                                        //  System.out.println("the date exe is :" + date);
                                        str = outputFormat.format(date);
                                        sharedPreferences.edit().putString("last_seen" + recipientId, "" + str).commit();
                                    } catch (Exception e) {
                                        System.out.println("the date exe is :" + e);
                                    }
                                }
                                for (int i = 0; i < users.size(); i++) {
                                    if (users.get(i).equals(uuid)) {
                                        users.remove(i);
                                    }
                                }
                            }
                        }
                    } catch (JSONException e) {
                        Log.d("the JSONException", e.toString());
                    }
                    if (users.contains(recipientId)) {
                        sharedPreferences.edit().putString("on" + recipientId, "t").commit();
                    } else {
                        sharedPreferences.edit().putString("on" + recipientId, "f").commit();
                    }
                }
            });
        } catch (PubnubException e) {
            Log.d("the Pubnub Exception", e.toString());
        }
        try {
            pubnub.subscribe("calling_channel", new Callback() {

            });
        } catch (PubnubException e) {
            Log.d("the Pubnub Exception ", e.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        con = new ConnectionDetector(getApplicationContext());
        if (con.isConnectingToInternet()) {
            subsribe();
            time();
        } else {

        }
        sharedPreferences.edit().putString("rec_no", "" + recipientId).commit();
    }

    @Override
    public void onPause() {
        super.onPause();

        con = new ConnectionDetector(getApplicationContext());
        if (con.isConnectingToInternet()) {
            try {
                Callback callback = new Callback() {
                    public void successCallback(String channel, Object response) {
                        System.out.println("the response type :" + response.toString());
                    }

                    public void errorCallback(String channel, PubnubError error) {
                        System.out.println(error.toString());
                    }
                };
                JSONObject jso = new JSONObject();
                jso.put("type", "false");
                pubnub.setState("calling_channel", currentUserId, jso, callback);
            } catch (Exception e) {
                System.out.println("the user is pause" + e);
            }
            pubnub.unsubscribe("calling_channel");
            sharedPreferences.edit().putString("rec_no", "").commit();
            time();
        } else {
            System.out.println("the user is pause else");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        con = new ConnectionDetector(getApplicationContext());
        if (con.isConnectingToInternet()) {
            try {
                Callback callback = new Callback() {
                    public void successCallback(String channel, Object response) {
                        //System.out.println("the response type :"+response.toString());
                    }

                    public void errorCallback(String channel, PubnubError error) {
                        System.out.println(error.toString());
                    }
                };
                JSONObject jso = new JSONObject();
                jso.put("type", "false");
                pubnub.setState("calling_channel", currentUserId, jso, callback);
            } catch (Exception e) {
                //System.out.println("the user is stop" + e);
            }
            pubnub.unsubscribe("calling_channel");
            sharedPreferences.edit().putString("rec_no", "").commit();
            time();
        } else {
            //System.out.println("the user is else");
        }
    }

    private void setUpEmojiPopup() {
        emojiPopup = EmojiPopup.Builder.fromRootView(rootView).setOnEmojiBackspaceClickListener(new OnEmojiBackspaceClickListener() {
            @Override
            public void onEmojiBackspaceClicked(final View v) {
                Log.d("MainActivity", "Clicked on Backspace");
                System.out.println("the click on backspace");
            }
        }).setOnEmojiClickedListener(new OnEmojiClickedListener() {
            @Override
            public void onEmojiClicked(final Emoji emoji) {
                Log.d("MainActivity", "Clicked on emoji");
                System.out.println("the click on emoji");

            }
        }).setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
            @Override
            public void onEmojiPopupShown() {
                emojiButton.setImageResource(R.drawable.keyboard_);
            }
        }).setOnSoftKeyboardOpenListener(new OnSoftKeyboardOpenListener() {
            @Override
            public void onKeyboardOpen(final int keyBoardHeight) {
                Log.d("MainActivity", "Opened soft keyboard");
                System.out.println("the opensoft keyboard");
            }
        }).setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
            @Override
            public void onEmojiPopupDismiss() {
                emojiButton.setImageResource(R.drawable.emoji_people);
            }
        }).setOnSoftKeyboardCloseListener(new OnSoftKeyboardCloseListener() {
            @Override
            public void onKeyboardClose() {
                emojiPopup.dismiss();
            }
        }).build(messageBodyField);
    }

    @Override
    public void onBackPressed() {
        if (emojiPopup != null && emojiPopup.isShowing()) {
            emojiPopup.dismiss();
        } else {
            super.onBackPressed();
            con = new ConnectionDetector(getApplicationContext());
            if (con.isConnectingToInternet()) {
                try {
                    Callback callback = new Callback() {
                        public void successCallback(String channel, Object response) {
                            System.out.println("the response type :" + response.toString());
                        }

                        public void errorCallback(String channel, PubnubError error) {
                            System.out.println(error.toString());
                        }
                    };
                    JSONObject jso = new JSONObject();
                    jso.put("type", "false");
                    pubnub.setState("calling_channel", currentUserId, jso, callback);
                } catch (Exception e) {
                    System.out.println("the user is pause" + e);
                }
                pubnub.unsubscribe("calling_channel");
                sharedPreferences.edit().putString("rec_no", "").commit();
                time();
            } else {
                System.out.println("the user is pause else");
            }
        }
    }

    @Override
    protected void onNewIntent(Intent i) {
        super.onNewIntent(i);
        System.out.println("the onNowIntent method");

        /*Bundle intent = i.getExtras();
        name = intent.getString("name");
        currentUserId = sharedPreferences.getString("mymobile","");
        System.out.println("the my mobile is :" + currentUserId);
        name1.setText(""+name);
        recipientId = intent.getString("RECIPIENT_ID");
        where = intent.getString("where");
        System.out.println("the recieve mobile is : " + where);
        sharedPreferences.edit().putString("rec_no",recipientId).commit();
        img = intent.getString("img");
        Picasso.with(context).load(img).placeholder(R.drawable.img_pic).transform(new CircleTransform()).into(imgUser);*/
        // dataser();
    }

    private void sendReadReacipt(final JSONArray message_id) {
        SendData = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                String serverUrl = StaticService.read_me;
                System.out.println("the get message service is :" + serverUrl);
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(serverUrl);
                try {
                    JSONObject jo = new JSONObject();
                    jo.put("message_id", message_id);
                    System.out.println("the send message array jo :" + jo);
                    StringEntity se = new StringEntity(jo.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    post.setEntity(se);
                    HttpResponse response = client.execute(post);
                    HttpEntity entity = response.getEntity();
                    String responseText = EntityUtils.toString(entity);
                    response_json = new JSONObject(responseText);
                    System.out.println("the response of new message  :" + response_json);
                } catch (Exception e) {
                    System.out.println("the exception in got message : " + e);
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                con = new ConnectionDetector(getApplicationContext());
                if (con.isConnectingToInternet()) {

                } else {
                    SendData.cancel(true);
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    JSONObject vv = response_json.getJSONObject("posts");
                    if (vv.getString("success").equals("1")) {
                        System.out.println("rr success got read");
                    } else {
                        System.out.println("the Something going wrong on server");
                    }
                } catch (Exception e) {
                    System.out.println("the message exception is  " + e);
                }
            }
        };
        SendData.execute();
    }

//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//
//        MenuItem item = menu.findItem(R.id.menu_view_contact);
//
//        if (item == menu.findItem(R.id.menu_view_contact)){
//
//        } else if (item == menu.findItem(R.id.menu_media)){
//
//        } else if (item == menu.findItem(R.id.menu_search)){
//
//        } else if (item == menu.findItem(R.id.menu_mute)){
//
//        } else if (item == menu.findItem(R.id.menu_wallpaper)){
//
//        } else if (item == menu.findItem(R.id.menu_block)){
//
//        } else if (item == menu.findItem(R.id.menu_clear_chat)){
//
//        } else if (item == menu.findItem(R.id.menu_email_chat)){
//
//        } else if (item == menu.findItem(R.id.menu_add_shortcut)){
//
//        }
//
//        return true;
//    }
}
