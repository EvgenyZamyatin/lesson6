package odeen.newrssreader.proj.conroller;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.Date;

import odeen.newrssreader.proj.model.Channel;
import odeen.newrssreader.proj.model.Item;

/**
 * Created by Женя on 09.11.2014.
 */
public class ChannelContentProvider extends ContentProvider {
    private static final String TAG = "ChannelContentProvider";

    //----------------
    //DB constants
    //----------------
    private static final String DB_NAME = "rssreader.sqlite";
    private static final int VERSION = 1;

    public static final String TABLE_CHANNELS = "channels";
    public static final String COLUMN_CHANNELS_ID = "_id";
    public static final String COLUMN_CHANNELS_CHANNEL_NAME = "channel_name";
    public static final String COLUMN_CHANNELS_CHANNEL_LINK = "channel_link";
    public static final String COLUMN_CHANNELS_IS_LOADING = "channel_is_loading";


    public static final String TABLE_ITEMS = "items";
    public static final String COLUMN_ITEMS_ID = "_id";
    public static final String COLUMN_ITEMS_CHANNEL_ID = "channel_id";
    public static final String COLUMN_ITEMS_SESSION_ID = "session_id";
    public static final String COLUMN_ITEMS_TITLE = "title";
    public static final String COLUMN_ITEMS_PUBDATE = "pubdate";
    public static final String COLUMN_ITEMS_LINK = "link";
    public static final String COLUMN_ITEMS_DESCRIPTION = "description";
    public static final String COLUMN_ITEMS_IS_WATCHED = "is_watched";

    //----------------
    //Uri constants
    //----------------
    private static final String AUTHORITY = "odeen.newrssreader.providers.channels_provider";
    public static final Uri CHANNEL_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + TABLE_CHANNELS);
    public static final Uri ITEM_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + TABLE_ITEMS);
    private static final int URI_CHANNELS = 1;
    private static final int URI_CHANNELS_ID = 2;
    private static final int URI_ITEMS = 3;
    private static final int URI_ITEMS_ID = 4;
    private static final UriMatcher mUriMatcher;
    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, TABLE_CHANNELS, URI_CHANNELS);
        mUriMatcher.addURI(AUTHORITY, TABLE_CHANNELS + "/#", URI_CHANNELS_ID);
        mUriMatcher.addURI(AUTHORITY, TABLE_ITEMS, URI_ITEMS);
        mUriMatcher.addURI(AUTHORITY, TABLE_ITEMS + "/#", URI_ITEMS_ID);
    }

    private DatabaseHelper mHelper;

    @Override
    public boolean onCreate() {
        mHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        String table = null;
        Uri contentUri = null;
        switch (mUriMatcher.match(uri)) {
            case URI_CHANNELS:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = COLUMN_CHANNELS_ID + " asc";
                }
                table = TABLE_CHANNELS;
                contentUri = CHANNEL_CONTENT_URI;
                break;
            case URI_CHANNELS_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = COLUMN_CHANNELS_ID + " = " + uri.getLastPathSegment();
                } else {
                    selection = selection + " and " + COLUMN_CHANNELS_ID + " = " + uri.getLastPathSegment();
                }
                table = TABLE_CHANNELS;
                contentUri = CHANNEL_CONTENT_URI;
                break;
            case URI_ITEMS:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = COLUMN_ITEMS_PUBDATE + " desc";
                }
                table = TABLE_ITEMS;
                contentUri = ITEM_CONTENT_URI;
                break;
            case URI_ITEMS_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = COLUMN_ITEMS_ID + " = " + uri.getLastPathSegment();
                } else {
                    selection = selection + " and " + COLUMN_ITEMS_ID + " = " + uri.getLastPathSegment();
                }
                table = TABLE_ITEMS;
                contentUri = ITEM_CONTENT_URI;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        Cursor cursor = mHelper.getWritableDatabase().query(table, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), contentUri);
        Log.d(TAG, contentUri.toString());
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        String table = null;
        switch (mUriMatcher.match(uri)) {
            case URI_CHANNELS:
                table = TABLE_CHANNELS;
                break;
            case URI_ITEMS:
                table = TABLE_ITEMS;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        long id = mHelper.getReadableDatabase().insert(table, null, contentValues);
        Uri resultUri = ContentUris.withAppendedId(CHANNEL_CONTENT_URI, id);
        getContext().getContentResolver().notifyChange(uri, null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String table = null;
        switch (mUriMatcher.match(uri)) {
            case URI_CHANNELS:
                table = TABLE_CHANNELS;
                break;
            case URI_CHANNELS_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = COLUMN_CHANNELS_ID + " = " + uri.getLastPathSegment();
                } else {
                    selection = selection + " and " + COLUMN_CHANNELS_ID + " = " + uri.getLastPathSegment();
                }
                table = TABLE_CHANNELS;
                break;
            case URI_ITEMS:
                table = TABLE_ITEMS;
                break;
            case URI_ITEMS_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = COLUMN_ITEMS_ID + " = " + uri.getLastPathSegment();
                } else {
                    selection = selection + " and " + COLUMN_ITEMS_ID + " = " + uri.getLastPathSegment();
                }
                table = TABLE_ITEMS;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        int cnt = mHelper.getWritableDatabase().delete(table, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        String table = null;
        switch (mUriMatcher.match(uri)) {
            case URI_CHANNELS:
                table = TABLE_CHANNELS;
                break;
            case URI_CHANNELS_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = COLUMN_CHANNELS_ID + " = " + uri.getLastPathSegment();
                } else {
                    selection = selection + " and " + COLUMN_CHANNELS_ID + " = " + uri.getLastPathSegment();
                }
                table = TABLE_CHANNELS;
                break;
            case URI_ITEMS:
                table = TABLE_ITEMS;
                break;
            case URI_ITEMS_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = COLUMN_ITEMS_ID + " = " + uri.getLastPathSegment();
                } else {
                    selection = selection + " and " + COLUMN_ITEMS_ID + " = " + uri.getLastPathSegment();
                }
                table = TABLE_ITEMS;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        int cnt = mHelper.getWritableDatabase().update(table, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    //Special for items
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        String table = null;
        switch (mUriMatcher.match(uri)) {
            case URI_CHANNELS:
                table = TABLE_CHANNELS;
                break;
            case URI_ITEMS:
                table = TABLE_ITEMS;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        int cnt = 0;
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.beginTransactionNonExclusive();
        for (ContentValues cv : values) {
            String selection = ChannelContentProvider.COLUMN_ITEMS_CHANNEL_ID + " = ? and "
                    + ChannelContentProvider.COLUMN_ITEMS_LINK + " = ?";
            String[] selectionArgs = new String[]{String.valueOf(cv.getAsLong(COLUMN_ITEMS_CHANNEL_ID)), cv.getAsString(COLUMN_ITEMS_LINK)};
            int affected = db.update(table, cv, selection, selectionArgs);
            if (affected == 0) {
                affected = db.insert(table, null, cv) > 0 ? 1 : 0;
            }
            cnt += affected;
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table channels (" +
                            "_id integer primary key autoincrement, channel_name string, channel_link string, channel_is_loading integer default 0)"
            );
            db.execSQL("create table items (" +
                            "_id integer primary key autoincrement, " +
                            "channel_id integer references channels(_id), session_id integer default 0, " +
                            "title string, pubdate long, link string, " +
                            "description string, is_watched integer default 0)"
            );
            db.execSQL("create unique index my_index on items (channel_id, link)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        }
    }

    public static class ChannelCursor extends CursorWrapper {
        public ChannelCursor(Cursor cursor, Context context) {
            super(cursor);
            setNotificationUri(context.getContentResolver(), CHANNEL_CONTENT_URI);
        }
        public Channel getChannel() {
            if (isBeforeFirst() || isAfterLast())
                return null;
            Channel channel = new Channel();
            String channelName = getString(getColumnIndex(COLUMN_CHANNELS_CHANNEL_NAME));
            channel.setChannelName(channelName);
            String channelLink = getString(getColumnIndex(COLUMN_CHANNELS_CHANNEL_LINK));
            channel.setChannelLink(channelLink);
            long channelId = getLong(getColumnIndex(COLUMN_CHANNELS_ID));
            channel.setId(channelId);
            return channel;
        }

    }

    public static class ItemCursor extends CursorWrapper {
        public ItemCursor(Cursor cursor, Context context) {
            super(cursor);
            setNotificationUri(context.getContentResolver(), ITEM_CONTENT_URI);
        }
        public Item getItem() {
            if (isBeforeFirst() || isAfterLast())
                return null;
            Item item = new Item();

            String title = getString(getColumnIndex(COLUMN_ITEMS_TITLE));
            item.setTitle(title);

            Long time = getLong(getColumnIndex(COLUMN_ITEMS_PUBDATE));
            item.setPubDate(new Date(time));

            String link = getString(getColumnIndex(COLUMN_ITEMS_LINK));
            item.setLink(link);

            String description = getString(getColumnIndex(COLUMN_ITEMS_DESCRIPTION));
            item.setDescription(description);

            int isWatched = getInt(getColumnIndex(COLUMN_ITEMS_IS_WATCHED));
            item.setWatched(isWatched == 1);

            long sessionId = getLong(getColumnIndex(COLUMN_ITEMS_SESSION_ID));
            item.setSessionId(sessionId);

            return item;
        }
    }


}
