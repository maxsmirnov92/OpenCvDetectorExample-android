package ru.maxsmr.android.recyclerview.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bejibx.android.recyclerview.selection.SelectionHelper;

import java.util.List;
import java.util.Set;

public abstract class BaseSingleSelectionRecyclerViewAdapter<I, VH extends BaseRecyclerViewAdapter.ViewHolder> extends BaseRecyclerViewAdapter<I, VH> {

    @Nullable
    private Drawable defaultDrawable, selectionDrawable;

    public BaseSingleSelectionRecyclerViewAdapter(@NonNull Context context, @LayoutRes int itemLayoutId, @Nullable List<I> items) {
        this(context, itemLayoutId, items, null, null);
    }

    public BaseSingleSelectionRecyclerViewAdapter(Context context, @LayoutRes int itemLayoutId, @Nullable List<I> items, Drawable defaultDrawable, Drawable selectionDrawable) {
        super(context, itemLayoutId, items);
        if (defaultDrawable != null) {
            setDefaultDrawable(defaultDrawable);
        }
        if (selectionDrawable != null) {
            setSelectionDrawable(selectionDrawable);
        }
    }

    @NonNull
    public abstract Set<SelectionHelper.SelectMode> getSelectionModes();

    @CallSuper
    protected void processSelection(VH holder, final I item, final int position) {
        for (SelectionHelper.SelectMode mode : getSelectionModes()) {
            switch (mode) {
                case CLICK:
                    holder.itemView.setClickable(true);
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (position == selection) {
                                toggleSelection(position);
                            } else {
                                setSelection(position);
                            }
                        }
                    });
                    break;

                case LONG_CLICK:
                    holder.itemView.setLongClickable(true);
                    holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (position == selection) {
                                toggleSelection(position);
                            } else {
                                setSelection(position);
                            }
                            return true;
                        }
                    });
                    break;
            }
        }
    }

    @Nullable
    public Drawable getDefaultDrawable() {
        return defaultDrawable;
    }

    @CallSuper
    public void setDefaultDrawable(@Nullable Drawable defaultDrawable) {
        this.defaultDrawable = defaultDrawable;
        if (isNotifyOnChange())
            notifyDataSetChanged();
    }

    @Nullable
    public Drawable getSelectionDrawable() {
        return selectionDrawable;
    }

    @CallSuper
    public void setSelectionDrawable(@Nullable Drawable selectionDrawable) {
        this.selectionDrawable = selectionDrawable;
        if (isNotifyOnChange())
            notifyDataSetChanged();
    }

    @Override
    @CallSuper
    protected void processItem(@NonNull VH holder, I item, int position) {
        super.processItem(holder, item, position);
        processSelection(holder, item, position);
    }

    private int selection = RecyclerView.NO_POSITION;

    public boolean isSelected() {
        return selection != RecyclerView.NO_POSITION;
    }

    public I getSelectedItem() {
        return getItem(selection);
    }

    public int getSelectedPosition() {
        return selection;
    }

    public void setSelectionByItem(I selection) {
        setSelection(indexOf(selection));
    }

    public void setSelection(int selection) {
        rangeCheck(selection);
        if (this.selection != selection) {
            int previousSelection = this.selection;
            this.selection = selection;
            onSelectionChanged(previousSelection, this.selection);
            if (isNotifyOnChange()) {
                notifyItemChanged(previousSelection);
                notifyItemChanged(selection);
            }
        }
    }

    public void resetSelection() {
        if (isSelected()) {
            int previousSelection = this.selection;
            this.selection = RecyclerView.NO_POSITION;
            onSelectionChanged(previousSelection, this.selection);
            if (isNotifyOnChange()) {
                notifyItemChanged(previousSelection);
            }
        }
    }

    public void toggleSelection(int selection) {
        if (isSelected()) {
            resetSelection();
        } else {
            setSelection(selection);
        }
    }

    protected void onSelectionChanged(int from, int to) {
        if (selectedChangeListener != null) {
            if (selection != RecyclerView.NO_POSITION) {
                selectedChangeListener.onSetSelection(from, to);
            } else {
                selectedChangeListener.onResetSelection(from);
            }
        }
    }

    @Nullable
    private OnSelectedChangeListener selectedChangeListener;

    public void setOnSelectedChangeListener(@Nullable OnSelectedChangeListener selectedChangeListener) {
        this.selectedChangeListener = selectedChangeListener;
    }

    public interface OnSelectedChangeListener {

        void onSetSelection(int fromIndex, int toIndex);

        void onResetSelection(int fromIndex);
    }
}
