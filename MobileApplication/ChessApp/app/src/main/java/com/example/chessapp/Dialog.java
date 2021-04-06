package com.example.chessapp;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.List;

public class Dialog  extends AppCompatDialogFragment {

    private Spinner spinner_cat;
    private Spinner spinner_color;
    private DialogListener listener;
    private Bitmap image;
    private ImageView sample;

    public Dialog(Bitmap bitmap) {
        image = bitmap;
    }


    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);

        spinner_cat = (Spinner) view.findViewById(R.id.spinner_cat);
        ArrayAdapter<CharSequence> adapter_cat = ArrayAdapter.createFromResource(getContext(),
                R.array.pieces_array, android.R.layout.simple_spinner_item);
        adapter_cat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_cat.setAdapter(adapter_cat);


        spinner_color = (Spinner) view.findViewById(R.id.spinner_color);
        ArrayAdapter<CharSequence> adapter_color = ArrayAdapter.createFromResource(getContext(),
                R.array.colors_array, android.R.layout.simple_spinner_item);
        adapter_color.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_color.setAdapter(adapter_color);

        sample = (ImageView) view.findViewById(R.id.imageView_sample);

        sample.setImageBitmap(image);

        builder.setView(view)
                .setTitle("CORRECT PREDICTION")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.applyChanges(spinner_cat.getSelectedItem().toString(), spinner_color.getSelectedItem().toString());
                    }
                });


        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (DialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException((context.toString() + "must implement ExampleDialogListener"));
        }
    }

    public interface DialogListener{
        void applyChanges(String category, String color);
    }
}
