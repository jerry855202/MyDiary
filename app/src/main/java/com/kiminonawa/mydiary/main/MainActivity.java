package com.kiminonawa.mydiary.main;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kiminonawa.mydiary.R;
import com.kiminonawa.mydiary.db.DBManager;
import com.kiminonawa.mydiary.main.topic.Contacts;
import com.kiminonawa.mydiary.main.topic.Diary;
import com.kiminonawa.mydiary.main.topic.ITopic;
import com.kiminonawa.mydiary.main.topic.Memo;
import com.kiminonawa.mydiary.shared.ThemeManager;
import com.kiminonawa.mydiary.shared.ViewTools;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        DialogCreateTopic.TopicCreatedCallback {


    /**
     * Touch interface
     */
    public interface ItemTouchHelperAdapter {
        void onItemDismiss(int position);
    }

    /**
     * RecyclerView
     */
    private RecyclerView RecyclerView_topic;
    private MainTopicAdapter mainTopicAdapter;
    private List<ITopic> topicList;
    private ItemTouchHelper.Callback touchCallback;
    /**
     * popup
     */
    private PopupWindow mPopupWindow;
    private ImageView IV_main_popup_add;

    /**
     * DB
     */
    private DBManager dbManager;

    /**
     * UI
     */
    private ThemeManager themeManager;
    private LinearLayout LL_main_profile;
    private TextView TV_main_profile_username;
    private RelativeLayout RL_main_bottom_bar;
    private EditText EDT_main_topic_search;
    private ImageView IV_main_setting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        themeManager = ThemeManager.getInstance();

        LL_main_profile = (LinearLayout) findViewById(R.id.LL_main_profile);
        LL_main_profile.setOnClickListener(this);

        TV_main_profile_username = (TextView) findViewById(R.id.TV_main_profile_username);
        RL_main_bottom_bar = (RelativeLayout) findViewById(R.id.RL_main_bottom_bar);

        EDT_main_topic_search = (EditText) findViewById(R.id.EDT_main_topic_search);
        IV_main_setting = (ImageView) findViewById(R.id.IV_main_setting);
        IV_main_setting.setOnClickListener(this);

        RecyclerView_topic = (RecyclerView) findViewById(R.id.RecyclerView_topic);

        topicList = new ArrayList<>();
        dbManager = new DBManager(MainActivity.this);

        initProfile();
        initBottomBar();
        loadTopic();
        initPopupWindow();
        //Init topic adapter
        initTopicAdapter();
    }

    private void initProfile() {
        TV_main_profile_username.setText(themeManager.getThemeUserName(MainActivity.this));
        LL_main_profile.setBackgroundResource(themeManager.getProfileBgResource());
    }

    private void initBottomBar() {
        EDT_main_topic_search.setBackgroundColor(themeManager.getThemeMainColor(this));
        IV_main_setting.setColorFilter(themeManager.getThemeMainColor(this));
    }

    private void loadTopic() {
        topicList.clear();
        dbManager.opeDB();
        Cursor topicCursor = dbManager.selectTopic();
        for (int i = 0; i < topicCursor.getCount(); i++) {
            switch (topicCursor.getInt(2)) {
                case ITopic.TYPE_CONTACTS:
                    topicList.add(
                            new Contacts(topicCursor.getLong(0), topicCursor.getString(1)));
                    break;
                case ITopic.TYPE_DIARY:
                    topicList.add(
                            new Diary(topicCursor.getLong(0), topicCursor.getString(1)));
                    break;
                case ITopic.TYPE_MEMO:
                    topicList.add(
                            new Memo(topicCursor.getLong(0), topicCursor.getString(1)));
                    break;
            }
            topicCursor.moveToNext();
        }
        topicCursor.close();
        dbManager.closeDB();
    }

    private void initPopupWindow() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popuoView = inflater.inflate(R.layout.popup_main, null);
        mPopupWindow = new PopupWindow(MainActivity.this);
        mPopupWindow.setWidth(ViewTools.dpToPixel(getResources(), 80));
        mPopupWindow.setHeight(ViewTools.dpToPixel(getResources(), 100));
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);

        mPopupWindow.setContentView(popuoView);

        IV_main_popup_add = (ImageView) popuoView.findViewById(R.id.IV_main_popup_add);
        IV_main_popup_add.setOnClickListener(this);
        LinearLayout LL_main_popup = (LinearLayout) popuoView.findViewById(R.id.LL_main_popup);
        LL_main_popup.setBackgroundResource(themeManager.getPopupBgResource(this));
    }

    private void initTopicAdapter() {
        //Init topic adapter
        LinearLayoutManager lmr = new LinearLayoutManager(this);
        RecyclerView_topic.setLayoutManager(lmr);
        RecyclerView_topic.setHasFixedSize(true);
        mainTopicAdapter = new MainTopicAdapter(this, topicList);
        RecyclerView_topic.setAdapter(mainTopicAdapter);
        touchCallback = new itemTouchHelperCallback(mainTopicAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchCallback);
        touchHelper.attachToRecyclerView(RecyclerView_topic);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.LL_main_profile:
                themeManager.toggleTheme(this);
                //Send Toast
                Toast.makeText(this, getString(R.string.toast_change_theme), Toast.LENGTH_SHORT).show();
                //Restart App
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                startActivity(i);
                break;
            case R.id.IV_main_setting:
                mPopupWindow.showAsDropDown(IV_main_setting, RL_main_bottom_bar.getWidth(), 0);
                break;
            case R.id.IV_main_popup_add:
                DialogCreateTopic dialogCreateTopic = new DialogCreateTopic();
                dialogCreateTopic.setCallBack(this);
                dialogCreateTopic.show(getSupportFragmentManager(), "dialogCreateTopic");
                mPopupWindow.dismiss();
                break;
        }
    }

    @Override
    public void TopicCreated() {
        loadTopic();
        mainTopicAdapter.notifyDataSetChanged();
    }


    /**
     * Swipe to remove the topic.
     */
    public class itemTouchHelperCallback extends ItemTouchHelper.Callback {

        //TODO Add undo
        private final ItemTouchHelperAdapter mAdapter;

        public itemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }


        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        }
    }
}