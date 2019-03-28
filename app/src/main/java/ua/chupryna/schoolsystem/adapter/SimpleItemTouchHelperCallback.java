package ua.chupryna.schoolsystem.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import ua.chupryna.schoolsystem.R;

import static android.support.v7.widget.helper.ItemTouchHelper.*;

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {
    private final RVAdapterSavedTimetable mAdapter;
    private final Context context;

    public SimpleItemTouchHelperCallback(RVAdapterSavedTimetable adapter, Context context) {
        mAdapter = adapter;
        this.context = context;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, LEFT | RIGHT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int i) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.you_want_remove_timetable);
        builder.setNegativeButton(R.string.not, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
            }
        });
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!mAdapter.remove(viewHolder.getAdapterPosition()))
                    mAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}