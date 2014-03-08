package uk.ac.lancs.LUFELFv2.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import java.util.ArrayList;
import java.util.List;

import uk.ac.lancs.LUFELFv2.R;
import uk.ac.lancs.LUFELFv2.adapter.EventsListAdapter;
import uk.ac.lancs.LUFELFv2.adapter.MessageListAdapter;
import uk.ac.lancs.LUFELFv2.commsV2.AppMessage;
import uk.ac.lancs.LUFELFv2.commsV2.EventItem;
import uk.ac.lancs.LUFELFv2.commsV2.Friend;
import uk.ac.lancs.LUFELFv2.commsV2.ServerFactory;

/**
 * Created by Luke on 04/03/14.
 */
public class MessagesFragment extends Fragment {

    public static final int MESSAGE_RECEIVED = 0;
    public static final int MESSAGE_SENT = 1;

    private FragmentPagerAdapter _fragmentPagerAdapter;
    private ViewPager _viewPager;
    private List<Fragment> _fragments = new ArrayList<Fragment>();

    private ServerFactory factory = ServerFactory.getInstance();

    public MessagesFragment() {
        this._fragments.add(MESSAGE_RECEIVED, new MessagesReceivedFragment());
        this._fragments.add(MESSAGE_SENT, new MessagesSentFragment());
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //return default view
        View rootView = inflater.inflate(R.layout.fragment_pager_noswipe, container, false);

        // Setup the fragments, defining the number of fragments, the screens and titles.
        this._fragmentPagerAdapter = new FragmentPagerAdapter(getChildFragmentManager()){
            @Override
            public int getCount() {
                return MessagesFragment.this._fragments.size();
            }
            @Override
            public Fragment getItem(final int position) {
                return MessagesFragment.this._fragments.get(position);
            }
        };

        this._viewPager = (ViewPager) rootView.findViewById(R.id.pager);
        this._viewPager.setAdapter(this._fragmentPagerAdapter);

        // Set the default fragment.
        this.openFragment(MESSAGE_RECEIVED);

        //Tell the app we have a custom menu
        setHasOptionsMenu(true);
        return rootView;
    }

    public void openFragment(final int fragment) {
        this._viewPager.setCurrentItem(fragment);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_messages, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_sent:
                openFragment(MESSAGE_SENT);
                return true;

            case R.id.action_received:
                openFragment(MESSAGE_RECEIVED);
        }

        return super.onOptionsItemSelected(item);
    }

    public class MessagesSentFragment extends Fragment {
        private View view;
        private ListView list;

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
            // Create the view from XML layout.
            view = inflater.inflate(R.layout.fragment_messages_sent, null);
            list = (ListView) view.findViewById(R.id.listView);

            FetchSentMessagesTaskAsync task = new FetchSentMessagesTaskAsync();
            task.execute();

            return view;
        }

        public class FetchSentMessagesTaskAsync extends AsyncTask<String, Void, ArrayList<AppMessage>> {

            @Override
            protected ArrayList<AppMessage> doInBackground(String... params) {
                return factory.getSentMessages();
            }

            @Override
            protected void onPostExecute(ArrayList<AppMessage> e) {
                MessageListAdapter listAdapter = new MessageListAdapter(getActivity(), e);
                list.setAdapter(listAdapter);
            }
        }
    }

    public class MessagesReceivedFragment extends Fragment {
        private View view;
        private ListView list;

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
            // Create the view from XML layout.
            view = inflater.inflate(R.layout.fragment_messages_received, null);
            list = (ListView) view.findViewById(R.id.listView);

            FetchMessagesTaskAsync task = new FetchMessagesTaskAsync();
            task.execute();

            return view;
        }

        public class FetchMessagesTaskAsync extends AsyncTask<String, Void, ArrayList<AppMessage>> {
            private ServerFactory factory = ServerFactory.getInstance();

            @Override
            protected ArrayList<AppMessage> doInBackground(String... params) {
                return factory.getMessages();
            }

            @Override
            protected void onPostExecute(ArrayList<AppMessage> e) {
                MessageListAdapter listAdapter = new MessageListAdapter(getActivity(), e);
                list.setAdapter(listAdapter);
                listAdapter.notifyDataSetChanged();

            }
        }
    }
}
