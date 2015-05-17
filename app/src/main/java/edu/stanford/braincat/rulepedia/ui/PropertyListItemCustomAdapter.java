package edu.stanford.braincat.rulepedia.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import edu.stanford.braincat.rulepedia.R;
import edu.stanford.braincat.rulepedia.model.ObjectDatabase;
import edu.stanford.braincat.rulepedia.model.Property;

public class PropertyListItemCustomAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<Property> list = new ArrayList<>();
    private Activity activity;
    private Context context;

    public PropertyListItemCustomAdapter(Activity activity, Context context, ArrayList<Property> list) {
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
        TextView listItemText = (TextView) view.findViewById(R.id.list_item_string);
        listItemText.setText(list.get(position).key + " : " + list.get(position).value);

        //Handle buttons and add onClickListeners
        Button deleteBtn = (Button) view.findViewById(R.id.delete_btn);
        //Button addBtn = (Button)view.findViewById(R.id.add_btn);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //do something
                try {
                    String propertyID = list.get(position).key;
                    ObjectDatabase db = ObjectDatabase.get();
                    db.remove(propertyID);
                    notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
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