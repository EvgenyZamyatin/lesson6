package odeen.newrssreader.proj.conroller;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;


/**
 * Created by Женя on 03.11.2014.
 */
public abstract class SQLiteCursorLoader extends AsyncTaskLoader<Cursor> {

    private Cursor mCursor;

    public SQLiteCursorLoader(Context context) {
        super(context);
    }

    protected abstract Cursor loadCursor();

    @Override
    public Cursor loadInBackground() {
        Cursor cursor = loadCursor();
        /*
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
        if (cursor != null) {
            cursor.getCount();
        }
        return cursor;
    }

    @Override
    public void deliverResult(Cursor data) {
        Cursor oldCursor = mCursor;
        mCursor = data;
        if (isStarted()) {
            super.deliverResult(data);
        }
        if (oldCursor != null && oldCursor != data && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    @Override
    public void onStartLoading() {
        if (mCursor != null) {
            deliverResult(mCursor);
        }
        if (takeContentChanged() || mCursor == null) {
            forceLoad();
        }
    }

    @Override
    public void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }
    @Override
    public void onReset() {
        super.onReset();
        onStopLoading();
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
    }

}
