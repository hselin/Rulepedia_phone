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
import edu.stanford.braincat.rulepedia.model.Rule;
import edu.stanford.braincat.rulepedia.model.RuleDatabase;

public class RuleManageFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RuleManageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RuleManageFragment newInstance(String param1, String param2) {
        RuleManageFragment fragment = new RuleManageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public RuleManageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    ListView ruleListView;

    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    ArrayList<Rule> listItems = new ArrayList<Rule>();

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    //ArrayAdapter<Rule> listAdapter;
    RuleListItemCustomAdapter listAdapter;

    private void loadRules(Context ctx) {
        listItems.clear();

        try {
            RuleDatabase db = RuleDatabase.get();
            db.load(ctx);
            Collection<Rule> rules = db.getAllRules();

            Log.d("myTag", "rules.size(): " + rules.size());

            for (Rule rule : rules) {
                listItems.add(rule);
            }

            //listItems.add(new Rule("rule 1", "desc", null, null));

            listAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_rule_manage, container, false);

        ruleListView = (ListView) v.findViewById(R.id.rule_list);
        //listAdapter = new ArrayAdapter<String>(this.getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, listItems);

        /*
        listAdapter = new ArrayAdapter(this.getActivity().getApplicationContext(),android.R.layout.simple_list_item_2, listItems){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                BasicNameValuePair data = listItems.get(position);

                text1.setText(data.getName());
                text2.setText(data.getValue());
                return view;
            }
        };*/

        /*
        listAdapter = new ArrayAdapter (this.getActivity().getApplicationContext(), android.R.layout.simple_list_item_2, android.R.id.text1, listItems)
        {
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                Rule rule = listItems.get(position);

                text1.setText(rule.getName());
                text2.setText(rule.getDescription());
                return view;
            }
        };
        */

        //instantiate custom adapter
        listAdapter = new RuleListItemCustomAdapter(this.getActivity(), this.getActivity().getApplicationContext(), listItems);

        ruleListView.setAdapter(listAdapter);

        loadRules(this.getActivity().getApplicationContext());
        return v;
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
                        loadRules(getActivity().getApplicationContext());
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
