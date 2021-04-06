package com.example.chessapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirtualBoardActivity extends AppCompatActivity implements Dialog.DialogListener {

    private Connection connection;
    List<Integer> ids = new ArrayList<>();

    Button btn_predict_white, btn_predict_black;
    TextView show_prediction;
    ImageView temp_view;
    int width;
    int new_line_id = 1;
    int dark_color = Color.rgb(0, 0, 0);
    int light_color = Color.rgb(255, 255, 255);
    int current_color = light_color;
    int switch_current_color = dark_color;
    boolean switch_colors = true;
    private final String TAG = "VirtualBoardActivity";
    public static List<Bitmap> cropped_images;

    private int selectedImageId = 0;
    private int selectedButtonId = 0;

    //------------------------------------------------------------------
    private final String URL = "https://b5b64816932c.ngrok.io/";
    private final String API = "images";
    //------------------------------------------------------------------



    RelativeLayout rl;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_virtual_board);
        if(!Python.isStarted()){
            Python.start(new AndroidPlatform((this)));
        }
        btn_predict_white = (Button) findViewById(R.id.predict_white);
        btn_predict_black = (Button) findViewById(R.id.predict_black);
        show_prediction = (TextView) findViewById(R.id.predicted_move);
        temp_view = (ImageView) findViewById(R.id.temp_view);
        setScaledButtonWidth();

        rl = (RelativeLayout) findViewById(R.id.chessBoard);

        connection = new Connection(getApplicationContext(), findViewById(android.R.id.content).getRootView(), this, TAG);


        createGrid();


        final String boardString = getIntent().getExtras().getString("Board");
        String[] boardSplitted = splitAndReversString(boardString);

        for(int i = 0; i < 8; i++){

            for(int j = 0; j < 8 ; j++){

                char piece = boardSplitted[i].charAt(j);
                int index_id = (i * 8) + j ;
                Button btn = (Button) findViewById( ids.get(index_id));
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Log.d(TAG, String.valueOf(v.getId()));
                        selectedButtonId = Integer.parseInt(String.valueOf(v.getId())) - 1;
                        selectedImageId = getImageId(selectedButtonId);
                        openDialog(selectedImageId);
                    }
                });
                String drawableString = Utilities.mapDrawableFromIndex(String.valueOf(piece));
                if(drawableString != null){
                    Resources resources = getApplicationContext().getResources();
                    final int resourceId = resources.getIdentifier(drawableString, "drawable", getApplicationContext().getPackageName());
                    btn.setForeground(resources.getDrawable(resourceId));

                }
            }


        }

        btn_predict_white.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                predictMove(boardString);
            }
        });

    }

    private void openDialog(int imageId) {
        Dialog dialog = new Dialog(cropped_images.get(imageId));
        dialog.show(getSupportFragmentManager(), "dialog");
    }


    private int getImageId(int id){
        int column = id % 8;
        int row = id / 8;
        int newRow = 7 - row;

        return (newRow * 8) + column;
    }


    @Override
    protected void onStart() {
        super.onStart();

        connection.registerNetworkCallback();

    }

    @Override
    protected void onResume() {
        super.onResume();
        connection.showSnackbar();
    }


    private String[] splitAndReversString(String stringBoard){
        int len = stringBoard.length();
        //n determines the variable that divide the string in 'n' equal parts
        int n = 8;
        int temp = 0, chars = len/n;
        //Stores the array of string
        String[] equalStr = new String [n];
        //Check whether a string can be divided into n equal parts
        if(len % n != 0) {
            System.out.println("Sorry this string cannot be divided into "+ n +" equal parts.");
        }
        else {
            for (int i = 0; i < len; i = i + chars) {
                //Dividing string in n equal part using substring()
                String part = stringBoard.substring(i, i + chars);
                equalStr[temp] = part;
                temp++;
            }
        }
        Collections.reverse(Arrays.asList(equalStr));
        return equalStr;
    }





    private void createGrid(){
        for(int i = 1; i <= 8;i ++){
            switch_colors = !switch_colors;
            LayoutParams first_lp = new LayoutParams(width,width);
            Button first_btn = new Button(this);
            if(i != 1) {
                first_lp.addRule(RelativeLayout.BELOW, new_line_id);
                new_line_id +=  8;

            }
            first_btn.setId(new_line_id);
            ids.add(first_btn.getId());
            first_btn.setBackgroundColor(getSquareColor());

            rl.addView(first_btn, first_lp);
            for(int j = 1; j < 8; j++){
                LayoutParams lp = new LayoutParams(width,width);
                Button btn = new Button(this);
                if(i != 1) {
                    lp.addRule(RelativeLayout.BELOW, new_line_id - 8);
                }
                lp.addRule(RelativeLayout.RIGHT_OF, new_line_id + j -1);
                btn.setId(new_line_id + j);
                btn.setBackgroundColor(getSquareColor());
                ids.add(btn.getId());
                
                rl.addView(btn, lp);
            }
        }
    }

    private int getSquareColor(){
        int temp;
        if(switch_colors){
            temp = switch_current_color;
            if(switch_current_color == light_color){
                switch_current_color = dark_color;
            }else{
                switch_current_color = light_color;
            }
        }else{
            temp = current_color;
            if(current_color == light_color){
                current_color = dark_color;
            }else{
                current_color = light_color;
            }
        }
        return temp;
    }

    private void setScaledButtonWidth(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels / 8;
    }


    private void predictMove(String boardString){
        Python py = Python.getInstance();
        final PyObject predictor = py.getModule("ChessMoveGenerator");
        PyObject obj= predictor.callAttr("GetBestMoveFromString",boardString);
        colorPredictedMove(Integer.parseInt(obj.asList().get(0).toString()), Integer.parseInt(obj.asList().get(1).toString()));
        show_prediction.setText(obj.asList().get(2).toString());
    }

    private void colorPredictedMove(int x, int y){
        Button btn_dest = (Button) findViewById( ids.get(x));
        Button btn_piece = (Button) findViewById( ids.get(y));
        btn_piece.setBackgroundColor(Color.GREEN);
        btn_dest.setBackgroundColor(Color.YELLOW);

    }

    /**function that send a POST request to the server by confirming the cropped image and in return gets the Text*/
    private void sendImageToServer(Bitmap imageToSend, final String category, final String color){

       final String encodedImage = Utilities.BitMapToString(imageToSend);

        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);

        String url = URL+API;

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        try {

                            JSONObject jsonObject = new JSONObject(response);

                            String mappedPiece;

                            Button btn = (Button) findViewById( ids.get(selectedButtonId));

                            if(category.equals("Empty")){
                                btn.setForeground(null);
                            }else{
                                mappedPiece = Utilities.mapStringToIndex(color.toLowerCase() + "_" + category.toLowerCase());
                                Resources resources = getApplicationContext().getResources();
                                final int resourceId = resources.getIdentifier(Utilities.mapDrawableFromIndex(mappedPiece), "drawable", getApplicationContext().getPackageName());
                                btn.setForeground(resources.getDrawable(resourceId));
                            }

                            Toast.makeText(getApplicationContext(), "SENT", Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.toString());
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("encodedImage", encodedImage);
                params.put("category", category.toLowerCase());
                params.put("color", color.toLowerCase());
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        stringRequest.setTag(TAG);
        connection.addRequest(stringRequest);

    }

    @Override
    public void applyChanges(String category, String color) {

        sendImageToServer(cropped_images.get(selectedImageId), category, color);

    }


}