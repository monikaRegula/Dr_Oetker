package com.oetker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

public class RetrieveFirebaseStoragePdf extends AppCompatActivity {

    DatabaseReference reference;
    FirebaseUser fuser;
    FirebaseStorage storage;
    FirebaseDatabase database;
    StorageReference listRef;

    private ListView listView;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrieve_firebase_storage_pdf);
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        listView = findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        listuj();

    }

    private void listuj(){
//        https://www.youtube.com/watch?v=sT8jJPJqMEg
        listRef = storage.getReference().child("Uploads");
        listRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        for (StorageReference prefix : listResult.getPrefixes()) {

                        }

                        for (StorageReference item : listResult.getItems()) {
                            System.out.println(item.getDownloadUrl().toString());
                            // Create a reference to a file from a Google Cloud Storage URI
                            adapter.add(item.getName());
                            adapter.notifyDataSetChanged();
                            item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>(){
                                @Override
                                public void onSuccess(Uri uri){
                                    Log.d("HERE ", "Download url is : " + uri.toString() );
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(uri.toString()));
                                    startActivity(intent);
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Uh-oh, an error occurred!
                    }
                });
    }

}