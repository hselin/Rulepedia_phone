package edu.stanford.braincat.rulepedia.ui;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.braincat.rulepedia.R;
import edu.stanford.braincat.rulepedia.model.Rule;
import edu.stanford.braincat.rulepedia.model.RuleDatabase;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RuleManageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RuleManageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RuleManageFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

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

    ListView ruleListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    ArrayList<Rule> listItems = new ArrayList<Rule>();

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    ArrayAdapter<Rule> listAdapter;

    private void loadRules(Context ctx)
    {

        try{
            RuleDatabase db = RuleDatabase.get();
            db.load(ctx);
            Collection <Rule> rules = db.getAllRules();

            //ArrayList<String> ruletList = new ArrayList<String>();


            for (Rule rule : rules) {
                listItems.add(rule);
            }


            //listItems.add(new Rule("r1", null, null));

            listAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1ASDASDASDASDA" + e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_rule_manage, container, false);

        ruleListView = (ListView) v.findViewById( R.id.rule_list );
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




        ruleListView.setAdapter(listAdapter);

        loadRules(this.getActivity().getApplicationContext());
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
