package com.example.wonderv2.Exp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wonderv2.MainActivity;
import com.example.wonderv2.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;

public class Exp_camera_food extends AppCompatActivity {

    private Button btn_save;
    private TextView view_productname, view_shopname, view_expday, view_productdetail, view_productingredient, view_productguide, view_dday, textresult;
    private IntentIntegrator qrScan;
    private DatabaseReference mDatabase;


    int a = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exp_camera);

        btn_save = (Button) findViewById(R.id.btn_save);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        readExp_productdetail();

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String getProductName = view_productname.getText().toString();
                String getShopName = view_shopname.getText().toString();
                String getExpDay = view_expday.getText().toString();
                String getProductDetail= view_productdetail.getText().toString();
                String getProductIngredient = view_productingredient.getText().toString();
                String getProductGuide = view_productguide.getText().toString();
                String getDDay = view_dday.getText().toString();

                //hashmap ?????????
                HashMap result = new HashMap<>();
                result.put("????????????",getProductName);
                result.put("??????",getShopName);
                result.put("????????????",getExpDay);
                result.put("????????????",getProductDetail);
                result.put("????????????",getProductIngredient);
                result.put("??????/?????????",getProductGuide);
                result.put("",getDDay);


                a = a+1;



                writeNewExp_productdetail(a,getProductName, getShopName,getExpDay,getProductDetail,getProductIngredient,getProductGuide,getDDay);

                Intent intent = new Intent(Exp_camera_food.this, MainActivity.class);
                startActivity(intent);

            }
        });



        view_productname = (TextView) findViewById(R.id.view_productname);
        view_shopname = (TextView) findViewById(R.id.view_shopname);
        view_expday = (TextView) findViewById(R.id.view_expday);
        view_productdetail = (TextView) findViewById(R.id.view_productdetail);
        view_productingredient = (TextView) findViewById(R.id.view_productingredient);
        view_productguide = (TextView) findViewById(R.id.view_productguide);
        view_dday = (TextView) findViewById(R.id.view_dday);
        textresult = (TextView) findViewById(R.id.textresult);

        qrScan = new IntentIntegrator(this);


        qrScan.setCaptureActivity(CaptureForm.class);
        qrScan.setOrientationLocked(false);
        qrScan.initiateScan();
    }


    //Getting the scan results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(Exp_camera_food.this, "????????????", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Exp_camera_food.this, "????????????", Toast.LENGTH_SHORT).show();
                try {
                    //data??? json?????? ??????
                    JSONObject obj = new JSONObject(result.getContents());
                    view_productname.setText(obj.getString("product"));
                    view_shopname.setText(obj.getString("shop"));
                    view_expday.setText(obj.getString("exp"));
                    view_productdetail.setText(obj.getString("detail"));
                    view_productingredient.setText(obj.getString("ingredient"));
                    view_productguide.setText(obj.getString("guide"));
                    view_dday.setText(obj.getString("dday"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    textresult.setText(result.getContents());
                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void writeNewExp_productdetail(int a, String productName, String shopName, String expDay, String productDetail, String productIngredient, String productGuide, String dDay) {
        Exp_productdetail Exp_productdetail = new Exp_productdetail(productName,  shopName,  expDay, productDetail, productIngredient,productGuide,dDay);



        int ONE_DAY = 24*60*60*1000;//millisecond ?????? ?????? 24??????

        //???????????? ??????
        String expday = Exp_productdetail.getExpDay().toString();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy??? MM??? dd???");
        LocalDate date = LocalDate.parse(expday, format);
        int expYear = date.getYear();
        int expMonth = date.getMonthValue();
        int expDate = date.getDayOfMonth();
        Calendar ddayCalendar = Calendar.getInstance();
        ddayCalendar.set(expYear, expMonth, expDate);
        long DateDday = ddayCalendar.getTimeInMillis() / ONE_DAY;

        //System.out.println(date);

        //?????? ?????? ?????????
        LocalDateTime today = LocalDateTime.now();
        int toYear = today.getYear();
        int toMonth = today.getMonthValue();
        int toDay = today.getDayOfMonth();
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(toYear, toMonth, toDay);
        long DateToday = todayCalendar.getTimeInMillis() / ONE_DAY;

        long result = DateDday - DateToday;

        String dday;
        if(result > 0){
            dday = "D-" + result;
        }
        else if(result == 0){
            dday = "D-Day";
        }
        else{
            result *= -1;
            dday = "D+" + result;
        }

        Exp_productdetail.setDDay(dday);


        mDatabase.child("exp_food").child(productName).setValue(Exp_productdetail)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Write was successful!
                        Toast.makeText(Exp_camera_food.this, "????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    }



                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Write failed
                        Toast.makeText(Exp_camera_food.this, "????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void readExp_productdetail(){
        mDatabase.child("exp_food").child("1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                if(dataSnapshot.getValue(Exp_productdetail.class) != null){
                    Exp_productdetail post = dataSnapshot.getValue(Exp_productdetail.class);
                    Log.w("FireBaseData", "getData" + post.toString());
                } else {
                    Toast.makeText(Exp_camera_food.this, "????????? ??????", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("FireBaseData", "loadPost:onCancelled", databaseError.toException());
            }
        });
    }


}
