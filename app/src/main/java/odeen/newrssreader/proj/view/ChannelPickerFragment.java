package odeen.newrssreader.proj.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import odeen.newrssreader.R;

/**
 * Created by Женя on 03.11.2014.
 */
public class ChannelPickerFragment extends DialogFragment {
    public static final String EXTRA_CHANNEL_NAME = "newrssreader.CHANNEL_NAME";
    public static final String EXTRA_CHANNEL_LINK = "newrssreader.CHANNEL_LINK";
    public static final String EXTRA_CHANNEL_ID = "newrssreader.CHANNEL_ID";

    private String mName;
    private String mLink;
    private long mId;

    public static ChannelPickerFragment newInstance(long id, String name, String link) {
        Bundle args = new Bundle();
        args.putLong(EXTRA_CHANNEL_ID, id);
        args.putString(EXTRA_CHANNEL_NAME, name);
        args.putString(EXTRA_CHANNEL_LINK, link);
        ChannelPickerFragment fragment = new ChannelPickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void sendResult(int resultCode) {
        if (getTargetFragment() == null)
            return;
        Intent i = new Intent();
        i.putExtra(EXTRA_CHANNEL_NAME, mName);
        i.putExtra(EXTRA_CHANNEL_LINK, mLink);
        i.putExtra(EXTRA_CHANNEL_ID, mId);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);

        mName = getArguments().getString(EXTRA_CHANNEL_NAME);
        mLink = getArguments().getString(EXTRA_CHANNEL_LINK);
        mId = getArguments().getLong(EXTRA_CHANNEL_ID);
        if (mName == null)
            mName = "";
        if (mLink == null)
            mLink = "";
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_channel, null);
        EditText name = (EditText) v.findViewById(R.id.dialog_channel_namePicker);
        EditText link = (EditText) v.findViewById(R.id.dialog_channel_linkPicker);
        if (mName != null)
            name.setText(mName);
        if (mLink != null)
            link.setText(mLink);
        //TODO: Empty input
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                getArguments().putString(EXTRA_CHANNEL_NAME, charSequence.toString());
                mName = charSequence.toString();
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        link.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                getArguments().putString(EXTRA_CHANNEL_LINK, charSequence.toString());
                mLink = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendResult(Activity.RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendResult(Activity.RESULT_CANCELED);
                    }
                }).create();
    }
}
