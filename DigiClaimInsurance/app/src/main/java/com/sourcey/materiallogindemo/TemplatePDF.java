package com.sourcey.materiallogindemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class TemplatePDF {
    private  Context context;
    private File pdfFile;
    private Document document;
    private PdfWriter pdfWriter;
    private Paragraph paragraph;
    private Font fTitle=new Font(Font.FontFamily.TIMES_ROMAN,20,Font.BOLD);
    private Font fSubTitle=new Font(Font.FontFamily.TIMES_ROMAN,18,Font.BOLD);
    private Font fText=new Font(Font.FontFamily.TIMES_ROMAN,12,Font.BOLD);
    private Font fHighText=new Font(Font.FontFamily.TIMES_ROMAN,15,Font.BOLD, BaseColor.RED);
    public TemplatePDF(Context context)
    {
        this.context=context;
    }
    FirebaseStorage storage;
    FirebaseDatabase mbase1;
    DatabaseReference dbref1;

    public  void openDocument()
    {
        createFile();
        try
        {
            document=new Document(PageSize.A4);
            pdfWriter=PdfWriter.getInstance(document,new FileOutputStream(pdfFile));
            document.open();
        }
        catch (Exception e)
        {

        }
    }

    private  void createFile()
    {
        File folder=new File(Environment.getExternalStorageDirectory().toString(),"PDF");
        if(!folder.exists())
        {
            folder.mkdirs();
        }
        pdfFile=new File(folder,"TemplatePDF.pdf");
    }

    public  void addMetaDeta(String title,String subject,String author)
    {
        document.addTitle(title);
        document.addSubject(subject);
        document.addAuthor(author);
    }

    public  void addTitles(String title,String subTitle,String date)
    {
        try {
            paragraph = new Paragraph();
            addChildP(new Paragraph(title, fTitle));
            addChildP(new Paragraph(subTitle, fSubTitle));
            addChildP(new Paragraph("Microtek Services", fHighText));
            paragraph.setSpacingAfter(30);
            document.add(paragraph);
        }
        catch (Exception e)
        {
            Log.e("AddTitles",e.toString());
        }

    }

    public  void addParagraph(String text)
    {
        try {
            paragraph=new Paragraph(text,fText);
            paragraph.setSpacingAfter(5);
            paragraph.setSpacingBefore(5);
            document.add(paragraph);
        }
        catch (Exception e)
        {}
    }

    public void createTable(String[] header, ArrayList<String[]>clients)
    {
        try {
            paragraph = new Paragraph();
            paragraph.setFont(fText);
            paragraph.setSpacingBefore(30);
            PdfPTable pdfPTable = new PdfPTable(header.length);
            pdfPTable.setWidthPercentage(100);
            PdfPCell pdfPCell;
            Log.d("Length",header.length+" ");

            int indexC = 0;
            while (indexC < header.length) {

                pdfPCell = new PdfPCell(new Phrase(header[indexC++], fSubTitle));
                pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setBackgroundColor(BaseColor.GREEN);
                pdfPTable.addCell(pdfPCell);
            }
            for (int indexR = 0; indexR < clients.size(); indexR++) {
                String[] row = clients.get(indexR);

                for (indexC = 0; indexC < row.length; indexC++) {
                    pdfPCell = new PdfPCell(new Phrase(row[indexC]));
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setFixedHeight(40);
                    pdfPTable.addCell(pdfPCell);
                }
            }
            paragraph.setSpacingBefore(15);
            paragraph.setSpacingAfter(15);
            paragraph.add(pdfPTable);
            document.add(paragraph);
        }
        catch (Exception e)
        {
            Log.e("addParagraph",e.toString());
        }

    }

    private  void addChildP(Paragraph childParagraph)
    {
        childParagraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.add(childParagraph);
    }

    public  void closeDocument()
    {
        document.close();
    }
    public  void  viewPDF()
    {
        mbase1=FirebaseDatabase.getInstance();
        dbref1=mbase1.getReference("users/"+ FirebaseAuth.getInstance().getUid()+"/PhoneAttributes");
        Intent intent=new Intent(context, PDFActivity.class);
        storage=FirebaseStorage.getInstance();
        StorageReference storageReference=storage.getReference();
        storageReference.child(FirebaseAuth.getInstance().getUid()).child("PDF").putFile(Uri.fromFile(new File(pdfFile.getAbsolutePath()))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

            }
        });
        storageReference.child("Manager").child(FirebaseAuth.getInstance().getUid()).putFile(Uri.fromFile(new File(pdfFile.getAbsolutePath()))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

            }
        });
//        final String[] url = new String[1];
//        storageReference.child(FirebaseAuth.getInstance().getUid()).child("PDF").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//                url[0] = taskSnapshot.getMetadata().getDownloadUrl();;
//            }
//        });
       // dbref1.child("PDFURL").setValue(url[0]);
        intent.putExtra("path",pdfFile.getAbsolutePath());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
