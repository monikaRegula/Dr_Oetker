package com.oetker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.oetker.model.Constants;
import com.oetker.model.Upload;

import java.security.acl.Permission;

public class Upload_Activity extends AppCompatActivity {

    Button selectFile, upload;
    TextView notification;
    EditText fileName;

    FirebaseStorage storage;
    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseUser fuser;
    Uri pdfUri;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_);
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        selectFile = findViewById(R.id.select);
        upload = findViewById(R.id.upload);
        notification = findViewById(R.id.textViewStatus);
        fileName = findViewById(R.id.editTextFileName);

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        selectFile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(Upload_Activity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    selectPdf();
                }else{
                    ActivityCompat.requestPermissions(Upload_Activity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},0);
                }
            }
        });

        upload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(pdfUri!=null){
                    uploadFile(pdfUri);
                }else{
                    Toast.makeText(Upload_Activity.this, "Wybierz plik", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       if(requestCode ==9 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
           selectPdf();
       }else{
           Toast.makeText(Upload_Activity.this, "Please provide permission ...", Toast.LENGTH_LONG).show();
       }

    }

    private void selectPdf(){
        //https://www.youtube.com/watch?v=XOf_v2f85RU
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT); // fetching files
        startActivityForResult(intent, 86);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //check whether user has seleceted a file or not
//        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 86 && resultCode == RESULT_OK && data != null) {
            pdfUri = data.getData();
            notification.setText("Wybrany plik: " + data.getData(). getLastPathSegment());
        } else {
            Toast.makeText(Upload_Activity.this, "Wybierz plik ", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFile(Uri pdfUri){
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Dodawanie pliku ...");
        progressDialog.setProgress(0);
        progressDialog.show();

        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        final String filename = fileName.getText().toString();
        StorageReference storageReference = storage.getReference();
        storageReference.child(Constants.STORAGE_PATH_UPLOADS).child(fuser.getUid()).child(filename).putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>(){
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String url = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                        DatabaseReference reference = database.getReference();

                        reference = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_PATH_UPLOADS);

                        Upload upload = new Upload(fileName.getText().toString(), url);
                        reference.child(reference.push().getKey()).setValue(upload).addOnCompleteListener(new OnCompleteListener<Void> (){
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(Upload_Activity.this, "Plik pomyślnie dodany", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(Upload_Activity.this, "Plik nie został dodany!!!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener(){
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>(){
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                int currentProgress = (int)(100*snapshot.getBytesTransferred()/ snapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);
            }
        });
    }
}