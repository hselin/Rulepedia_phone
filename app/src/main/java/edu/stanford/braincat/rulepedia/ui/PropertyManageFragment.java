package edu.stanford.braincat.rulepedia.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import edu.stanford.braincat.rulepedia.R;
import edu.stanford.braincat.rulepedia.model.ObjectDatabase;
import edu.stanford.braincat.rulepedia.model.Property;

public class PropertyManageFragment extends Fragment {
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PropertyManageFragment.
     */
    public static PropertyManageFragment newInstance() {
        PropertyManageFragment fragment = new PropertyManageFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    public PropertyManageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_property_manage, container, false);

        View v = inflater.inflate(R.layout.fragment_property_manage, container, false);

        propertyListView = (ListView) v.findViewById(R.id.property_list);

        //instantiate custom adapter
        listAdapter = new PropertyListItemCustomAdapter(this.getActivity(), this.getActivity().getApplicationContext(), listItems);

        propertyListView.setAdapter(listAdapter);

        loadProperties(this.getActivity().getApplicationContext());
        return v;
    }

    ListView propertyListView;
    ArrayList<Property> listItems = new ArrayList<>();
    PropertyListItemCustomAdapter listAdapter;

    private void loadProperties(Context ctx) {
        listItems.clear();

        try {
            ObjectDatabase db = ObjectDatabase.get();
            Collection<Property> properties = db.getAllProperties();

            Log.d("myTag", "properties.size(): " + properties.size());

            for (Property property : properties) {
                listItems.add(property);
            }

            //listItems.add(new Rule("rule 1", "desc", null, null));

            listAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Timer autoUpdate;

    @Override
    public void onResume() {
        super.onResume();
        autoUpdate = new Timer();
        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        //updateHTML();
                        loadProperties(getActivity().getApplicationContext());
                    }
                });
            }
        }, 0, 5000); // updates each 40 secs
    }

    @Override
    public void onPause() {
        autoUpdate.cancel();
        super.onPause();
    }
}
