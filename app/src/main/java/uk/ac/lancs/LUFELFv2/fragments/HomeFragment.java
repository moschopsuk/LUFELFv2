package uk.ac.lancs.LUFELFv2.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import uk.ac.lancs.LUFELFv2.R;
import uk.ac.lancs.LUFELFv2.commsV2.ServerFactory;
import uk.ac.lancs.LUFELFv2.tasks.LoginTaskAsync;
import uk.ac.lancs.LUFELFv2.tasks.RegisterTaskAsync;


/**
 * Created by Luke on 04/03/14.
 */
public class HomeFragment extends Fragment {
    protected String email;
    protected String password;
    private ServerFactory factory = ServerFactory.getInstance();
    private FragmentPagerAdapter _fragmentPagerAdapter;
    private ViewPager _viewPager;
    private List<Fragment> _fragments = new ArrayList<Fragment>();

    public static final int VIEW_REGISTRATION = 0;
    public static final int VIEW_LOGIN = 1;
    public static final int VIEW_HOME = 2;

    public HomeFragment() {
        this._fragments.add(VIEW_REGISTRATION, new RegistrationFragment());
        this._fragments.add(VIEW_LOGIN, new LoginFragment());
        this._fragments.add(VIEW_HOME, new WelcomeFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final  View view = inflater.inflate(R.layout.fragment_pager_noswipe, container, false);

        // Setup the fragments, defining the number of fragments, the screens and titles.
        this._fragmentPagerAdapter = new FragmentPagerAdapter(getChildFragmentManager()){
            @Override
            public int getCount() {
                return HomeFragment.this._fragments.size();
            }
            @Override
            public Fragment getItem(final int position) {
                return HomeFragment.this._fragments.get(position);
            }
        };

        this._viewPager = (ViewPager) view.findViewById(R.id.pager);
        this._viewPager.setAdapter(this._fragmentPagerAdapter);

        if(factory.isLoggedIn()) {
            this.openFragment(VIEW_HOME);
        } else {
            this.openFragment(VIEW_LOGIN);
        }

        return view;
    }


    public void openFragment(final int fragment) {
        this._viewPager.setCurrentItem(fragment);
    }

    public Fragment getFragment(final int fragment) {
        return this._fragments.get(fragment);
    }

    public class RegistrationFragment extends Fragment {

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
            // Create the view from XML layout.
            final View view = inflater.inflate(R.layout.fragment_register, null);
            final EditText dobEdit = (EditText) view.findViewById(R.id.dob);

            final Handler registration_status = new Handler(){
                @Override public void handleMessage(Message msg) {
                    if((Boolean)msg.obj)
                        openFragment(VIEW_LOGIN);
                }
            };

            final DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int yyyy, int m, int d) {
                    String mm, dd;

                    if(d < 10) {
                        dd = "0" + d;
                    }else {
                        dd = Integer.toString(d);
                    }

                    if(m < 10) {
                        mm  = "0" + m ;
                    }else {
                        mm = Integer.toString(m);
                    }

                    dobEdit.setText(dd + "/" + mm + "/" + yyyy);
                }
            };

            dobEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Calendar calendar = Calendar.getInstance();
                    int yy = calendar.get(Calendar.YEAR);
                    int mm = calendar.get(Calendar.MONTH);
                    int dd = calendar.get(Calendar.DAY_OF_MONTH);

                    Dialog datepicker = new DatePickerDialog(getActivity(), dateListener, yy, mm, dd);
                    datepicker.show();
                }
            });

            Button button= (Button) view.findViewById(R.id.btnRegister);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = ((EditText) view.findViewById(R.id.email)).getText().toString();
                    String password = ((EditText) view.findViewById(R.id.password)).getText().toString();
                    String name = ((EditText) view.findViewById(R.id.name)).getText().toString();
                    String libaryCard = ((EditText) view.findViewById(R.id.lib)).getText().toString();
                    String dob = ((EditText) view.findViewById(R.id.dob)).getText().toString();
                    String accountType = ((Spinner) view.findViewById(R.id.accountType)).getSelectedItem().toString();
                    Integer account;

                    if(accountType.equals("Student")) {
                        account = 0;
                    } else {
                        account = 1;
                    }

                    RegisterTaskAsync task = new RegisterTaskAsync(registration_status, getActivity(), email, password, libaryCard, name, dob, account);
                    task.execute();
                }
            });

            return view;
        }
    }

    public class LoginFragment extends Fragment {

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
            // Create the view from XML layout.
            final View view = inflater.inflate(R.layout.fragment_login, null);

            final Handler login_status = new Handler(){
                @Override public void handleMessage(Message msg) {
                    if((Boolean)msg.obj)
                        openFragment(VIEW_HOME);
                }
            };

            Button regButton = (Button) view.findViewById(R.id.btnRegister);
            regButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFragment(VIEW_REGISTRATION);
                }
            });

            Button loginButton = (Button) view.findViewById(R.id.btnLogin);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String username = ((EditText) view.findViewById(R.id.email)).getText().toString();
                    String password = ((EditText) view.findViewById(R.id.password)).getText().toString();

                    LoginTaskAsync task = new LoginTaskAsync(login_status, getActivity(), username, password);
                    task.execute();
                }
            });

            return view;
        }
    }

    public class WelcomeFragment extends Fragment {
        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
            // Create the view from XML layout.
            final View view = inflater.inflate(R.layout.fragment_home, null);

            return view;
        }
    }

}
