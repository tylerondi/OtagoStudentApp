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
import java.util.List;

public class CasebookFragment extends Fragment {

    private RecyclerView list;
    private CasebookAdapter adapter;
    private TextView txvCasebookLabel;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_casebook, container, false);
        list = (RecyclerView) rootView.findViewById(R.id.rvCasebook);
        txvCasebookLabel = (TextView) rootView.findViewById(R.id.txvCasebookLabel);
        adapter = new CasebookAdapter(getActivity(), getData());
        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));

        return rootView;
    }

    public List<CasebookInfo> getData() {
        List<CasebookInfo> data = new ArrayList<>();

        OtagoDBConnector dbc = new OtagoDBConnector(getActivity());
        if (dbc.getUser() == null || dbc.getCasebooks() == null) {
            txvCasebookLabel.setText("No Casebooks Available - Import to Continue");
            return data;
        }
        Cursor cursor = dbc.getCasebooks();
        while (cursor.moveToNext()) {
            saveToPreferences(getActivity(), cursor.getString(2), cursor.getInt(0) + "");
            saveToPreferences(getActivity(), "userCasebook_id", cursor.getInt(1) + "");
            CasebookInfo current = new CasebookInfo();
            current.title = cursor.getString(2);
            data.add(current);
        }

        cursor.close();
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

    // Adapter ----------------------------------------------------------------------
    class CasebookAdapter extends RecyclerView.Adapter<CasebookAdapter.myViewHolder> {

        private LayoutInflater inflater;
        private List<CasebookInfo> data;
        myViewHolder holder;

        public CasebookAdapter(Context context, List<CasebookInfo> data) {
            inflater = LayoutInflater.from(context);
            this.data = data;
        }

        @Override
        public myViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.casebook_row, viewGroup, false);
            holder = new myViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(myViewHolder viewHolder, int i) {
            final CasebookInfo current = data.get(i);
            holder.txvCasebookTitle.setText(current.title);
            holder.cdvCasebook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String strCasebook_id = readFromPreferences(getActivity(), current.title, "-1");
                    ((MainActivity) getActivity()).openParentCategories(Integer.parseInt(strCasebook_id));
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class myViewHolder extends RecyclerView.ViewHolder {

            TextView txvCasebookTitle;
            CardView cdvCasebook;

            public myViewHolder(View itemView) {
                super(itemView);
                txvCasebookTitle = (TextView) itemView.findViewById(R.id.txvCasebookTitle);
                cdvCasebook = (CardView) itemView.findViewById(R.id.cdvCasebook);
            }
        }
    } // End Adapter
} // End CasebookFragment
