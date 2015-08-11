package edu.weber.tondricek.cs4750.otagostudentapp.otagostudentapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CategoryFragment extends Fragment {

    private TextView txvCategoryLabel;
    private RecyclerView list;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_category, container, false);
        list = (RecyclerView) rootView.findViewById(R.id.rvCategory);
        txvCategoryLabel = (TextView) rootView.findViewById(R.id.txvCategoryLabel);
        setUp();
        list.setLayoutManager(new LinearLayoutManager(getActivity()));

        return rootView;
    }

    public void setUp() {
        CategoryAdapter categoryAdapter = new CategoryAdapter(getActivity(), getCategoryData());
        list.setAdapter(categoryAdapter);
    }

    public List<CasebookInfo> getCategoryData() {
        List<CasebookInfo> data = new ArrayList<>();

        OtagoDBConnector dbc = new OtagoDBConnector(getActivity());
        if (dbc.getUser() == null
            || dbc.getCasebooks() == null
            || readFromPreferences(getActivity(), "selectedCasebookID", "-1").equals("-1")
            || (readFromPreferences(getActivity(), "isParentCategory", "true").equals("false")
                && readFromPreferences(getActivity(), "selectedCategoryID", "-1").equals("-1"))) {
            txvCategoryLabel.setText("No Categories Available - Import to Continue");
            return data;
        }

        if (readFromPreferences(getActivity(), "isParentCategory", "true").equals("true")) {
            Cursor cursor = dbc
                    .getParentCategories(Integer.parseInt(readFromPreferences(getActivity(), "selectedCasebookID", "-1")));

            return addCategoryData(cursor, data);
        } else if (readFromPreferences(getActivity(), "isParentCategory", "true").equals("false")) {
            Cursor cursor = dbc
                    .getChildCategories(Integer.parseInt(readFromPreferences(getActivity(), "selectedCasebookID", "-1")),
                            Integer.parseInt(readFromPreferences(getActivity(), "selectedCategoryID", "-1")));

            return addCategoryData(cursor, data);
        }

        return data;
    }

    public List<CasebookInfo> addCategoryData (Cursor cursor,  List<CasebookInfo> data) {
        while (cursor.moveToNext()) {
            saveToPreferences(getActivity(), "cat" + cursor.getString(1), cursor.getInt(0) + "");
            CasebookInfo current = new CasebookInfo();
            current.title = cursor.getString(1);
            current.groupOrder = cursor.getInt(2);
            data.add(current);
        }
        cursor.close();

        if (data.size() == 0) {
            txvCategoryLabel.setText("No Sub-Categories Available - Answer questions on the next tab to continue.");
        } else {
            Collections.sort(data);
        }

        return data;
    }

    public static void saveToPreferences(Context context, String preferenceName, String preferenceValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("CasebookFragment", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferenceName, preferenceValue);
        editor.apply();
    }

    public static String readFromPreferences(Context context, String preferenceName, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("CasebookFragment", Context.MODE_PRIVATE);
        return sharedPreferences.getString(preferenceName, defaultValue);
    }

    // Category Adapter ----------------------------------------------------------------------
    class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.myViewHolder> {

        private LayoutInflater inflater;
        private List<CasebookInfo> data;
        myViewHolder holder;

        public CategoryAdapter(Context context, List<CasebookInfo> data) {
            inflater = LayoutInflater.from(context);
            this.data = data;
        }

        @Override
        public myViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.category_row, viewGroup, false);
            holder = new myViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(myViewHolder viewHolder, int i) {
            final CasebookInfo current = data.get(i);
            holder.txvCategoryTitle.setText(current.title);
            holder.cdvCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String strCategory_id = readFromPreferences(getActivity(), "cat" + current.title, "-1");
                    ((MainActivity) getActivity()).openChildCategories(Integer.parseInt(strCategory_id));
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class myViewHolder extends RecyclerView.ViewHolder {

            TextView txvCategoryTitle;
            CardView cdvCategory;

            public myViewHolder(View itemView) {
                super(itemView);
                txvCategoryTitle = (TextView) itemView.findViewById(R.id.txvCategoryTitle);
                cdvCategory = (CardView) itemView.findViewById(R.id.cdvCategory);
            }
        } // End myViewHolder
    } // End Category Adapter
} // End CasebookFragment
