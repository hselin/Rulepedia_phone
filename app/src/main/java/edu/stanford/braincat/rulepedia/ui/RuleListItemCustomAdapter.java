package edu.stanford.braincat.rulepedia.ui;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;

import java.util.ArrayList;

import android.content.Context;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import edu.stanford.braincat.rulepedia.R;
import edu.stanford.braincat.rulepedia.model.Rule;
import edu.stanford.braincat.rulepedia.service.Callback;
import edu.stanford.braincat.rulepedia.service.RuleExecutor;

public class RuleListItemCustomAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<Rule> list = new ArrayList<Rule>();
    private Activity activity;
    private Context context;

    public RuleListItemCustomAdapter(Activity activity, Context context, ArrayList<Rule> list) {
        this.list = list;
        this.context = context;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return 0;
        //return list.get(pos).getId();
        //just return 0 if your list items do not have an Id variable.
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.custom_list_item, null);
        }

        //Handle TextView and display string from your list
        TextView listItemText = (TextView)view.findViewById(R.id.list_item_string);
        listItemText.setText(list.get(position).getName());

        //Handle buttons and add onClickListeners
        Button deleteBtn = (Button)view.findViewById(R.id.delete_btn);
        //Button addBtn = (Button)view.findViewById(R.id.add_btn);

        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //do something
                String ruleID = list.get(position).getId();

                RuleExecutor executor = ((MainActivity) activity).getRuleExecutor();

                if (executor == null)
                    return;

                try {
                    executor.deleteRule(ruleID, new Callback<Boolean>() {
                        @Override
                        public void run(Boolean success, Exception error) {
                            if(success != null) {
                                list.remove(position); //or some other task
                                notifyDataSetChanged();
                            }
                            else
                            {
                                Log.d("a", "CANNOT REMOVE RULE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.d("a", null, e);
                }
            }
        });
        /*
        addBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //do something
                notifyDataSetChanged();
            }
        });
        */

        return view;
    }

}