package ru.maxsmr.opencv.detectorexample.gui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import ru.maxsmr.android.recyclerview.adapters.BaseRecyclerViewAdapter;
import ru.maxsmr.commonutils.graphic.GraphicUtils;
import ru.maxsmr.opencv.detectorexample.R;

public class FileAdapter extends BaseRecyclerViewAdapter<File, FileAdapter.ViewHolder> {

    public FileAdapter(@NonNull Context context, @Nullable List<File> items) {
        super(context, R.layout.listitem_file, items);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mContext, onInflateView(parent, viewType));
    }

    @Override
    protected boolean allowSetClickListener() {
        return true;
    }

    @Override
    protected boolean allowSetLongClickListener() {
        return true;
    }

    @Override
    protected void processItem(@NonNull ViewHolder holder, @Nullable final File item, int position) {
        super.processItem(holder, item, position);
        holder.deleteItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem(item);
            }
        });
    }

    protected static class ViewHolder extends BaseRecyclerViewAdapter.ViewHolder<File> {

        private TextView fileNameView;
        private ImageView deleteItemView;

        public ViewHolder(@NonNull Context context, @NonNull View view) {
            super(context, view);
            fileNameView = (TextView) view.findViewById(R.id.tvFileName);
            deleteItemView = (ImageView) view.findViewById(R.id.ivDelete);
        }

        @Override
        protected void displayData(int position, @NonNull File item) {
            super.displayData(position, item);
            if (GraphicUtils.canDecodeVideo(item)) {
                fileNameView.setText(item.getAbsolutePath());
            } else {
                fileNameView.setText(R.string.placeholder_incorrect_file);
            }
        }

        @Override
        protected void displayNoData(int position) {
            fileNameView.setText(R.string.placeholder_no_file);
        }
    }
}
