package uk.ac.lancs.LUFELFv2.fragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import uk.ac.lancs.LUFELFv2.R;
import uk.ac.lancs.LUFELFv2.adapter.EventsListAdapter;
import uk.ac.lancs.LUFELFv2.commsV2.EventItem;
import uk.ac.lancs.LUFELFv2.commsV2.Place;
import uk.ac.lancs.LUFELFv2.commsV2.ServerFactory;

/**
 * Created by Luke on 08/03/14.
 */
public class PlacesFragmnet extends Fragment {
    private View view;
    private GoogleMap map;
    public static final LatLng LANC = new LatLng(54.010128960293116, -2.7854418754577637);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_places, container, false);
        map = ((SupportMapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LANC, 10));
        map.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);

        FetchPlacesTaskAsync task = new FetchPlacesTaskAsync();
        task.execute();

        return view;
    }

    public class FetchPlacesTaskAsync extends AsyncTask<String, Void, ArrayList<Place>> {
        private ServerFactory factory = ServerFactory.getInstance();
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            dialog = ProgressDialog.show(getActivity(), "Please wait", "Loading data from database.", true, false);
        }

        @Override
        protected ArrayList<Place> doInBackground(String... params) {
            return factory.getPlaces();
        }

        @Override
        protected void onPostExecute(ArrayList<Place> e) {
            for(Place place : e) {
                LatLng coord = new LatLng(place.getLat(), place.getLon());
                map.addMarker(new MarkerOptions().position(coord).title(place.getName()));
            }

            if(dialog.isShowing())
                dialog.dismiss();
        }
    }
}
