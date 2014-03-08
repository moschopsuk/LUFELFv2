package uk.ac.lancs.LUFELFv2.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import uk.ac.lancs.LUFELFv2.R;
import uk.ac.lancs.LUFELFv2.adapter.EventsListAdapter;
import uk.ac.lancs.LUFELFv2.commsV2.Event;
import uk.ac.lancs.LUFELFv2.commsV2.EventItem;
import uk.ac.lancs.LUFELFv2.commsV2.ServerFactory;
import uk.ac.lancs.LUFELFv2.tasks.RegisterTaskAsync;

public class EventDetailFragment extends Fragment {
    public static final String ARG_ITEM_ID = "item_id";
    String eventID;
    Event event;
    View view;
    private GoogleMap map;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            eventID = getArguments().getString(ARG_ITEM_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_event_detail, container, false);

        FetchEventsTaskAsync task = new FetchEventsTaskAsync();
        task.execute();

        Button button= (Button) view.findViewById(R.id.button_attend);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewFriendDialogFragment dialog = new NewFriendDialogFragment();
                dialog.show(getActivity().getSupportFragmentManager(), "attend_event");
            }
        });

        return view;
    }

    public class NewFriendDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.dialog_attend)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            AttendEventsTaskAsync task = new AttendEventsTaskAsync();
                            task.execute();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            NewFriendDialogFragment.this.getDialog().cancel();
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    public class FetchEventsTaskAsync extends AsyncTask<String, Void, String> {
        private ServerFactory factory = ServerFactory.getInstance();

        @Override
        protected String doInBackground(String... params) {
            event = factory.getEvent(eventID);
            return "executed";
        }

        @Override
        protected void onPostExecute(String e) {
            ((TextView) view.findViewById(R.id.event_name)).setText(event.getName());
            ((TextView) view.findViewById(R.id.event_discription)).setText(event.getDescription());
            ((TextView) view.findViewById(R.id.event_location)).setText(event.getLocationName());
            ((TextView) view.findViewById(R.id.event_address)).setText(event.getLocationAddress());
            ((TextView) view.findViewById(R.id.event_date)).setText(event.getDate());

            map = ((SupportMapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();

            String[] loc = event.getLocation().split(",");
            double lat = Double.parseDouble(loc[0]);
            double lon = Double.parseDouble(loc[1]);
            LatLng coord = new LatLng(lat, lon);

            map.addMarker(new MarkerOptions().position(coord).title(event.getName()));
            // Move the camera instantly to hamburg with a zoom of 15.
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(coord, 4));
            // Zoom in, animating the camera.
            map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

            ListView listView = (ListView) view.findViewById(R.id.attendees_list);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, event.getAttendees());
            listView.setAdapter(adapter);
        }
    }

    public class AttendEventsTaskAsync extends AsyncTask<String, Void, Boolean> {
        private ServerFactory factory = ServerFactory.getInstance();

        @Override
        protected Boolean doInBackground(String... params) {
            return factory.attendEvent(eventID);
        }

        @Override
        protected void onPostExecute(Boolean e) {
            if(e) {
                Toast.makeText(getActivity(), "You are attending the event", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "An error occurred", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
