package app.com.mychat.menuActivity.media.tab.document;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import app.com.mychat.R;
import app.com.mychat.menuActivity.media.tab.document.data.ItemDocument;

/**
 * TODO: Replace the implementation with code for your data type.
 */
public class MyDocumentRecyclerViewAdapter extends RecyclerView.Adapter<MyDocumentRecyclerViewAdapter.ViewHolder> {

    private final List<ItemDocument> mValues;
    private Activity activity;

    public MyDocumentRecyclerViewAdapter(Activity activity, List<ItemDocument> items) {
        this.mValues = items;
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity)
                .inflate(R.layout.fragment_document, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        public ViewHolder(View view) {
            super(view);
        }
    }
}
