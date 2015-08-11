package edu.weber.tondricek.cs4750.otagostudentapp.otagostudentapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestionFragment extends Fragment {

    private TextView txvQuestionLabel;
    private RecyclerView list;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_question, container, false);
        list = (RecyclerView) rootView.findViewById(R.id.rvQuestion);
        txvQuestionLabel = (TextView) rootView.findViewById(R.id.txvQuestionLabel);
        setUp();
        list.setLayoutManager(new LinearLayoutManager(getActivity()));

        return rootView;
    }

    public void setUp() {
        QuestionAdapter questionAdapter = new QuestionAdapter(getActivity(), getQuestionData());
        list.setAdapter(questionAdapter);
    }

    public List<CasebookInfo> getQuestionData() {
        List<CasebookInfo> data = new ArrayList<>();

        OtagoDBConnector dbc = new OtagoDBConnector(getActivity());
        if (dbc.getUser() == null
                || dbc.getCasebooks() == null
                || readFromPreferences(getActivity(), "isParentCategory", "true").equals("true")
                || readFromPreferences(getActivity(), "selectedCategoryID", "-1").equals("-1")) {
            txvQuestionLabel.setText("No Questions Available - Select a category above to continue.");
            return data;
        }

        Cursor cursor = dbc
                .getQuestions(Integer.parseInt(readFromPreferences(getActivity(), "selectedCategoryID", "-1")));

        while (cursor.moveToNext()) {
            CasebookInfo current = new CasebookInfo();
            current.questionID = cursor.getInt(0);
            current.title = cursor.getString(2);
            current.groupOrder = cursor.getInt(1);
            current.user_casebook_id = Integer.parseInt(readFromPreferences(getActivity(), "userCasebook_id", "-1"));
            Cursor answerCursor = dbc.getAnswers(cursor.getInt(0));
            if (answerCursor.moveToNext()) {
                current.answerID = answerCursor.getInt(0);
                current.answer = answerCursor.getString(1);
            } else {
                current.answerID = -1;
                current.answer = "";
            }
            answerCursor.close();
            data.add(current);
        }
        cursor.close();

        if (data.size() == 0) {
            txvQuestionLabel.setText("No Questions Available - Select a category on the first tab to continue.");
        }

        Collections.sort(data);

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

    // Question Adapter ----------------------------------------------------------------------
    class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.myViewHolder> {

        private LayoutInflater inflater;
        private List<CasebookInfo> data;
        myViewHolder holder;

        private String currentText;

        public QuestionAdapter(Context context, List<CasebookInfo> data) {
            inflater = LayoutInflater.from(context);
            this.data = data;
        }

        @Override
        public myViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.question_row, viewGroup, false);
            holder = new myViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(myViewHolder viewHolder, final int i) {
            final CasebookInfo current = data.get(i);
            holder.txvQuestion.setText(current.title);
            if (current.answer != null) {
                holder.edtAnswer.setText(current.answer);
            }
            holder.edtAnswer.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    OtagoDBConnector dbc = new OtagoDBConnector(getActivity());

                    if (current.answerID > 0) {
                        dbc.setAnswer(current.answerID, currentText);
                    } else {
                        dbc.insertNewAnswer(current.userCasebook_id, current.questionID, currentText);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class myViewHolder extends RecyclerView.ViewHolder {

            TextView txvQuestion;
            EditText edtAnswer;

            public myViewHolder(View itemView) {
                super(itemView);
                txvQuestion = (TextView) itemView.findViewById(R.id.txvQuestion);
                edtAnswer = (EditText) itemView.findViewById(R.id.edtAnswer);
                edtAnswer.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        currentText = edtAnswer.getText().toString();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
        } // end myViewHolder
    } // End Question Adapter
} // End CasebookFragment
