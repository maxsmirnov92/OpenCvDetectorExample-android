package ru.maxsmr.android.recyclerview.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.widget.Checkable;

import com.bejibx.android.recyclerview.selection.HolderClickObserver;
import com.bejibx.android.recyclerview.selection.SelectionHelper;
import com.bejibx.android.recyclerview.selection.SelectionObserver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class BaseCheckableRecyclerViewAdapter<I, VH extends BaseRecyclerViewAdapter.ViewHolder> extends BaseRecyclerViewAdapter<I, VH> implements HolderClickObserver, SelectionObserver {

    private SelectionHelper mSelectionHelper;

    @Nullable
    private Drawable defaultDrawable, selectionDrawable;

    public BaseCheckableRecyclerViewAdapter(@NonNull Context context, @LayoutRes int itemLayoutId, @Nullable List<I> items) {
        this(context, itemLayoutId, items, null, null);
    }

    public BaseCheckableRecyclerViewAdapter(@NonNull Context context, @LayoutRes int itemLayoutId, @Nullable List<I> items, @Nullable Drawable defaultDrawable, @Nullable Drawable selectionDrawable) {
        super(context, itemLayoutId, items);
        initSelectionHelper();
        if (defaultDrawable != null) {
            setDefaultDrawable(defaultDrawable);
        }
        if (selectionDrawable != null) {
            setSelectionDrawable(selectionDrawable);
        }
    }

    private void initSelectionHelper() {
        mSelectionHelper = new SelectionHelper();
        mSelectionHelper.setSelectable(true);
        mSelectionHelper.registerSelectionObserver(this);
        mSelectionHelper.registerHolderClickObserver(this);
    }

    protected final SelectionHelper getSelectionHelper() {
        return mSelectionHelper;
    }

    @NonNull
    public abstract Set<SelectionHelper.SelectMode> getSelectionModes();

    @Override
    @CallSuper
    protected void processItem(@NonNull VH holder, I item, int position) {
        super.processItem(holder, item, position);

        mSelectionHelper.wrapSelectable(holder, getSelectionModes());

        final boolean isSelected = isItemSelected(position);

        if ((holder.itemView instanceof Checkable)) {
            ((Checkable) holder.itemView).setChecked(isSelected);
        }

        if (isSelected) {
            onProcessItemSelected(holder);
        } else {
            onProcessItemNotSelected(holder);
        }
    }

    @Override
    protected final boolean allowSetClickListener() {
        return false;
    }

    @Nullable
    public Drawable getDefaultDrawable() {
        return defaultDrawable;
    }

    public void setDefaultDrawable(@Nullable Drawable defaultDrawable) {
        this.defaultDrawable = defaultDrawable;
        if (isNotifyOnChange())
            notifyDataSetChanged();
    }

    @Nullable
    public Drawable getSelectionDrawable() {
        return selectionDrawable;
    }

    public void setSelectionDrawable(@Nullable Drawable selectionDrawable) {
        this.selectionDrawable = selectionDrawable;
        if (isNotifyOnChange())
            notifyDataSetChanged();
    }

    @Override
    protected void onItemsSet() {
        super.onItemsSet();
        if (mSelectionHelper != null) {
            if (mSelectionHelper.getSelectedItemsCount() > 0) {
                mSelectionHelper.clearSelection();
            }
        }
    }

    @Override
    protected void onItemsCleared() {
        super.onItemsCleared();
        if (mSelectionHelper != null) {
            if (mSelectionHelper.getSelectedItemsCount() > 0) {
                mSelectionHelper.clearSelection();
            }
        }
    }

    @Override
    protected void onItemAdded(int addedPosition, @Nullable I item) {
        super.onItemAdded(addedPosition, item);
    }

    @Override
    protected void onItemRemoved(int removedPosition, @Nullable I item) {
        super.onItemRemoved(removedPosition, item);
        if (mSelectionHelper != null) {
            if (mSelectionHelper.isItemSelected(removedPosition)) {
                mSelectionHelper.setItemSelectedByPosition(removedPosition, false);
            }
        }
    }

    @NonNull
    public LinkedHashSet<I> getSelectedItems() {
        LinkedHashSet<I> selectedItems = new LinkedHashSet<>();
        LinkedHashSet<Integer> selectedPositions = getSelectedItemsPositions();
        for (Integer pos : selectedPositions) {
            selectedItems.add(getItem(pos));
        }
        return selectedItems;
    }

    @NonNull
    public LinkedHashSet<I> getUnselectedItems() {
        LinkedHashSet<I> unselectedItems = new LinkedHashSet<>();
        LinkedHashSet<Integer> unselectedPositions = getUnselectedItemsPositions();
        for (Integer pos : unselectedPositions) {
            unselectedItems.add(getItem(pos));
        }
        return unselectedItems;
    }

    @NonNull
    public LinkedHashSet<Integer> getSelectedItemsPositions() {
        return mSelectionHelper != null ? mSelectionHelper.getSelectedItems() : new LinkedHashSet<Integer>();
    }

    @NonNull
    public LinkedHashSet<Integer> getUnselectedItemsPositions() {
        LinkedHashSet<Integer> unselectedPositions = new LinkedHashSet<>();
        LinkedHashSet<Integer> selectedPositions = getSelectedItemsPositions();
        for (int pos = 0; pos < getItemCount(); pos++) {
            if (!selectedPositions.contains(pos)) {
                unselectedPositions.add(pos);
            }
        }
        return unselectedPositions;
    }

    public boolean isItemSelected(int position) {
        rangeCheck(position);
        return mSelectionHelper != null && mSelectionHelper.isItemSelected(position);
    }

    public int getSelectedItemsCount() {
        return getItemCount() > 0 && mSelectionHelper != null ? mSelectionHelper.getSelectedItemsCount() : 0;
    }

    public boolean setItemsSelectedByPositions(@Nullable Collection<Integer> positions, boolean isSelected) {
        if (positions != null) {
            for (int pos : positions) {
                rangeCheck(pos);
            }
        }
        return mSelectionHelper != null && mSelectionHelper.setItemsSelectedByPositions(positions, isSelected);
    }

    public boolean setItemSelectedByPosition(int position, boolean isSelected) {
        rangeCheck(position);
        return mSelectionHelper != null && mSelectionHelper.setItemSelectedByPosition(position, isSelected);
    }

    public boolean toggleItemsSelectedByPositions(@Nullable Collection<Integer> positions) {
        if (positions != null) {
            for (int pos : positions) {
                rangeCheck(pos);
            }
        }
        return mSelectionHelper != null && mSelectionHelper.toggleItemsSelectedByPositions(positions);
    }

    public boolean toggleItemSelectedByPosition(int position) {
        rangeCheck(position);
        return mSelectionHelper != null && mSelectionHelper.toggleItemSelectedByPosition(position);
    }

    public boolean setItemsSelected(@Nullable Collection<I> items, boolean isSelected) {
        List<Integer> positions = new ArrayList<>();
        if (items != null) {
            for (I item : items) {
                int index = indexOf(item);
                if (index > -1) {
                    positions.add(index);
                }
            }
        }
        return setItemsSelectedByPositions(positions, isSelected);
    }

    public boolean setItemSelected(I item, boolean isSelected) {
        return setItemsSelected(Collections.singletonList(item), isSelected);
    }

    public boolean toggleItemsSelected(Collection<I> items) {
        List<Integer> positions = new ArrayList<>();
        if (items != null) {
            for (I item : items) {
                int index = indexOf(item);
                if (index > -1) {
                    positions.add(index);
                }
            }
        }
        return toggleItemsSelectedByPositions(positions);
    }

    public boolean toggleItemSelected(I item) {
        return toggleItemsSelected(Collections.singletonList(item));
    }


    protected void onProcessItemSelected(VH holder) {

    }

    protected void onProcessItemNotSelected(VH holder) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public final void onSelectedChanged(RecyclerView.ViewHolder holder, boolean isSelected) {
        if (selectedChangeListener != null) {
            selectedChangeListener.onSelectedChange(holder.getAdapterPosition());
        }
        if (isNotifyOnChange())
            notifyItemChanged(holder.getAdapterPosition());
    }

    @Override
    public void onSelectableChanged(boolean isSelectable) {

    }

    @Override
    public void onHolderClick(RecyclerView.ViewHolder holder) {

    }

    @Override
    public boolean onHolderLongClick(RecyclerView.ViewHolder holder) {
        return false;
    }


    @Nullable
    private OnSelectedChangeListener selectedChangeListener;

    public void setOnSelectedChangeListener(@Nullable OnSelectedChangeListener selectedChangeListener) {
        this.selectedChangeListener = selectedChangeListener;
    }

    public interface OnSelectedChangeListener {
        void onSelectedChange(int position);
    }

}
