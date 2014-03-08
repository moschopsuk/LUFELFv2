package uk.ac.lancs.LUFELFv2.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import uk.ac.lancs.LUFELFv2.R;
import uk.ac.lancs.LUFELFv2.commsV2.APIException;
import uk.ac.lancs.LUFELFv2.commsV2.Friend;
import uk.ac.lancs.LUFELFv2.commsV2.PendingFriend;
import uk.ac.lancs.LUFELFv2.commsV2.ServerFactory;
import uk.ac.lancs.LUFELFv2.commsV2.User;

/**
 * Created by Luke on 04/03/14.
 */
public class FriendsFragment extends Fragment {
    private ServerFactory factory = ServerFactory.getInstance();
    private FragmentPagerAdapter _fragmentPagerAdapter;
    private ViewPager _viewPager;
    private List<Fragment> _fragments = new ArrayList<Fragment>();

    public static final int VIEW_FRIENDS= 0;
    public static final int VIEW_VIEWPENDING = 1;

    public FriendsFragment() {
        this._fragments.add(VIEW_FRIENDS, new ListFriendsFragment());
        this._fragments.add(VIEW_VIEWPENDING, new FriendsPendingFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final  View view = inflater.inflate(R.layout.fragment_pager_noswipe, container, false);

        // Setup the fragments, defining the number of fragments, the screens and titles.
        this._fragmentPagerAdapter = new FragmentPagerAdapter(getChildFragmentManager()){
            @Override
            public int getCount() {
                return FriendsFragment.this._fragments.size();
            }
            @Override
            public Fragment getItem(final int position) {
                return FriendsFragment.this._fragments.get(position);
            }
        };

        this._viewPager = (ViewPager) view.findViewById(R.id.pager);
        this._viewPager.setAdapter(this._fragmentPagerAdapter);
        this.openFragment(VIEW_FRIENDS);

        return view;
    }

    public void openFragment(final int fragment) {
        this._viewPager.setCurrentItem(fragment);
    }

    public Fragment getFragment(final int fragment) {
        return this._fragments.get(fragment);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_friends, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
                NewFriendDialogFragment dialog = new NewFriendDialogFragment();
                dialog.show(getActivity().getSupportFragmentManager(), "new_friend");
                return true;
            case R.id.show_friend:
                openFragment(VIEW_FRIENDS);
                return true;
            case R.id.show_pending:
                openFragment(VIEW_VIEWPENDING);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class NewFriendDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View view = inflater.inflate(R.layout.dialog_friend_new, null);

            builder.setView(view)
                    // Add action buttons
                    .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            String userInput = ((EditText) view.findViewById(R.id.username)).getText().toString();

                            AddFriendTaskAsync task = new AddFriendTaskAsync(userInput);
                            task.execute();
                        }
                    })

                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            NewFriendDialogFragment.this.getDialog().cancel();
                        }
                    });

            return builder.create();
        }
    }

    public class AddFriendTaskAsync extends AsyncTask<String, Void, Boolean> {
        private String username;

        public AddFriendTaskAsync(String username) {
            this.username = username;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(String... params) {
            //First find the userID.
            try {
                User user = factory.getUser(username);
                if(user != null) {
                    return factory.addFriend(user.getId());
                }
            } catch (APIException e) {
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean e) {
            if(e) {
                Toast.makeText(getActivity(), "Friend Reqest sent", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Error Occured", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class ListFriendsFragment extends Fragment {
        private ListView listView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_friends, container, false);
            listView = (ListView) view.findViewById(R.id.friend_list);

            FetchFriendsTaskAsync task = new FetchFriendsTaskAsync();
            task.execute();

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                               int pos, long id) {
                    Object o = listView.getItemAtPosition(pos);
                    Friend friend = (Friend)o;

                    SendMessageDialogFragment dialog = new SendMessageDialogFragment(friend.getId());
                    dialog.show(getActivity().getSupportFragmentManager(), "accept_friend");

                    return true;
                }
            });

            setHasOptionsMenu(true);
            return view;
        }

        public class SendMessageDialogFragment extends DialogFragment {
            private String userId;
            private String message;

            public SendMessageDialogFragment(String userId) {
                this.userId = userId;
            }

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                // Get the layout inflater
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View view = inflater.inflate(R.layout.dialog_messages_new, null);

                builder.setView(view)
                        // Add action buttons
                        .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                String message = ((EditText) view.findViewById(R.id.message)).getText().toString();

                                SendMessageTaskAsync task = new SendMessageTaskAsync(userId, message);
                                task.execute();
                            }
                        })

                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SendMessageDialogFragment.this.getDialog().cancel();
                            }
                        });

                return builder.create();
            }
        }

        public class SendMessageTaskAsync extends  AsyncTask<String, Void, Boolean> {
            private String id;
            private String message;

            public SendMessageTaskAsync(String id, String message) {
                this.id = id;
                this.message = message;
            }

            @Override
            protected Boolean doInBackground(String... params) {
                return factory.sendMessage(id, message);
            }

            @Override
            protected void onPostExecute(Boolean e) {
                if(e) {
                    Toast.makeText(getActivity(), "Message Sent", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        }

        public class FetchFriendsTaskAsync extends AsyncTask<String, Void, ArrayList<Friend>> {

            @Override
            protected ArrayList<Friend> doInBackground(String... params) {
                return factory.getFriends();
            }

            @Override
            protected void onPostExecute(ArrayList<Friend> e) {
                ArrayAdapter<Friend> adapter = new ArrayAdapter<Friend>(getActivity(), android.R.layout.simple_list_item_1, e);
                listView.setAdapter(adapter);
            }
        }
    }

    class FriendsPendingFragment extends Fragment {
        private ListView listView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_friends, container, false);
            listView = (ListView) view.findViewById(R.id.friend_list);

            FetchFriendsPendingTaskAsync task = new FetchFriendsPendingTaskAsync();
            task.execute();

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                               int pos, long id) {

                    Object o = listView.getItemAtPosition(pos);
                    PendingFriend friend = (PendingFriend)o;

                    AcceptFriendDialog dialog = new AcceptFriendDialog(friend);
                    dialog.show(getActivity().getSupportFragmentManager(), "accept_friend");

                    return true;
                }
            });

            setHasOptionsMenu(true);
            return view;
        }

        public class AcceptFriendDialog extends DialogFragment {
            PendingFriend friend;
            ProcessRequestTaskAsync task;

            public AcceptFriendDialog(PendingFriend friend) {
                this.friend = friend;
            }

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                // Use the Builder class for convenient dialog construction
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.accept_request)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                task = new ProcessRequestTaskAsync(friend, true);
                                task.execute();
                            }
                        })

                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                task = new ProcessRequestTaskAsync(friend, false);
                                task.execute();
                            }
                        });
                // Create the AlertDialog object and return it
                return builder.create();
            }
        }

        public class ProcessRequestTaskAsync extends AsyncTask<String, Void, Boolean> {
            private PendingFriend friend;
            private Boolean s;

            public ProcessRequestTaskAsync(PendingFriend friend, boolean s) {
                this.friend = friend;
                this.s = s;
            }

            @Override
            protected Boolean doInBackground(String... params) {
                return factory.processRequest(friend, s);
            }

            @Override
            protected void onPostExecute(Boolean e) {
                if(e) {
                    Toast.makeText(getActivity(), "Friends Updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Error Occured", Toast.LENGTH_SHORT).show();
                }
            }
        }

        public class FetchFriendsPendingTaskAsync extends AsyncTask<String, Void, ArrayList<PendingFriend>> {

            @Override
            protected ArrayList<PendingFriend> doInBackground(String... params) {
                return factory.getPendingFriends();
            }

            @Override
            protected void onPostExecute(ArrayList<PendingFriend> e) {
                ArrayAdapter<PendingFriend> adapter = new ArrayAdapter<PendingFriend>(getActivity(), android.R.layout.simple_list_item_1, e);
                listView.setAdapter(adapter);
            }
        }
    }
}
