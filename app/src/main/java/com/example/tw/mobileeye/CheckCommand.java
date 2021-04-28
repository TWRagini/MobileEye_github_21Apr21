package com.example.tw.mobileeye;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;

import static android.content.ContentValues.TAG;

/**
 * Created by ajinkya on 1/10/16.
 */
public class CheckCommand {

    GlobalVariable globalVariable;
    Context myContext;
    SQLiteDatabase database;


    public CheckCommand(Context context) {
        this.myContext = context;
        globalVariable = (GlobalVariable) myContext.getApplicationContext();
    }

    public void get()
    {
        try {
            String unitId;

            database = myContext.openOrCreateDatabase("Config", myContext.MODE_PRIVATE, null);
            unitId = retrive("unitId");
            Log.e(TAG, "get: "+ unitId);
            database.close();

            String host = "pop.mobile-eye.in";
            String port = "995";
           // String userName = "8991@mobile-eye.in";
           // String password = "transworld@123";

            String userName="jrmroute@mobile-eye.in";
            String password = "jrmroute@123";

            String sub = "UI" + unitId;
            Log.e("CheckCommand", "start: ");

            //new MailAction().execute(host, port, userName, password, sub);
            boolean f = emailAttachmentReceiver(host, port, userName, password, sub);
            if (f) {
                Log.i("***EmailAttach***", "Found");
            } else {
                Log.i("***EmailAttach***", "Not found");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private String retrive(String p) {
        try {
            String res;
            Cursor c = database.rawQuery("select value from file where parameter='" + p + "'", null);
            c.moveToFirst();
            do {
                res = c.getString(0);
            } while (c.moveToNext());
            return res;
        } catch (Exception e) {
            return "error" + e.getMessage();
        }
    }

    /*
     * For to check command exists or not.
     * */

    /*private class MailAction extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {

            String host = (String) objects[0];
            String port = (String) objects[1];
            String userName = (String) objects[2];
            String password = (String) objects[3];
            String sub = (String) objects[4];
            //Log.i("***EmailAttach***","sub : "+sub);
            boolean f;
            f = emailAttachmentReceiver(host, port, userName, password, sub);
            //f = receiveMail();

            return f;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            boolean f = (boolean) o;
            if (f) {
                Log.i("***EmailAttach***", "Found");
                //routePointsList = readPoints(routeNo);

                //jrmTimer();
                //readTable(txtFilename.getText().toString());
            } else {
                Log.i("***EmailAttach***", "Not found");
                //Log.i("jrm-----", "Route not found");
                //Toast.makeText(myContext, "Not found", Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    public boolean emailAttachmentReceiver(String host, String port,
                                           String userName, String password, String sub) {

        boolean found = false;
        String attachFiles = "";

        Properties properties = new Properties();

        // server setting
        properties.put("mail.pop3.host", host);
        properties.put("mail.pop3.port", port);

        // SSL setting
        properties.setProperty("mail.pop3.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.pop3.socketFactory.fallback", "false");
        properties.setProperty("mail.pop3.socketFactory.port",
                String.valueOf(port));

        Session session = Session.getInstance(properties);

        try {
            // connects to the message store
            Store store = session.getStore("pop3");
            store.connect(userName, password);

            // opens the inbox folder
            Folder folderInbox = store.getFolder("INBOX");
            folderInbox.open(Folder.READ_WRITE);

            // fetches new messages from server
            Message[] arrayMessages = folderInbox.getMessages();

            /*Flags seen = new Flags(Flags.Flag.SEEN);
            FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
            Message[] arrayMessages = folderInbox.search(unseenFlagTerm);
            System.out.println("UnreadMessageCount: "+folderInbox.getUnreadMessageCount());
            System.out.println("NewMessageCount: "+folderInbox.getNewMessageCount());*/

            if(arrayMessages.length != 0) {
                for (int i = (arrayMessages.length - 1); i >= 0; i--) {
                    Message message = arrayMessages[i];
                    Address[] fromAddress = message.getFrom();
                    String from = fromAddress[0].toString();
                    String subject = message.getSubject();
                    String sentDate = message.getSentDate().toString();

                    //
                    /*System.out.println("Message #" + (i + 1) + ":");
                    System.out.println("\t From: " + from);
                    System.out.println("\t Subject: " + subject);
                    System.out.println("\t Sent Date: " + sentDate);
                    System.out.println("\t Contents: " + message.getContent());*/

                /*Flags flags = message.getFlags();
                System.out.println("Flags: "+flags.contains(Flags.Flag.SEEN));
                System.out.println(""+flags.toString());
                System.out.println("Msg ID: "+message.getMessageNumber());
                Date date = message.getSentDate();
                Date date1 = new Date();
                System.out.println(""+ date1.toString() +"Is After: "+ date.toString() +" : "+date1.after(date));*/

                /*SQLiteDatabase sqLiteDatabase = openOrCreateDatabase("TestEmailApp.db", MODE_PRIVATE, null);
                sqLiteDatabase.execSQL("create table if not exists LastCommand(mydate datetime)");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String d = sdf.format(date);
                //String t = new SimpleDateFormat("hh:mm:ss").format(date);
                sqLiteDatabase.execSQL("insert into LastCommand values('"+d+"')");
                Cursor c = sqLiteDatabase.rawQuery("select mydate from LastCommand", null);
                c.moveToFirst();
                Date date2 = sdf.parse(c.getString(0));
                System.out.println(""+ date2.toString() +"Is After: "+ date1.toString() +" : "+date2.after(date1));*/

                /*
                ***use***
                if(i == (arrayMessages.length - 1))
                {
                    //message.setFlag(Flags.Flag.SEEN, true);
                    //System.out.println("Contents: "+message.getContent().toString());

                    message.setFlag(Flags.Flag.DELETED, true);

                    System.out.println("in last");
                }
                found = true;*/

                    if (subject.equals(sub)) {
                        String contentType = message.getContentType();

                        if (contentType.contains("multipart")) {
                            // content may contain attachments
                            Multipart multiPart = (Multipart) message.getContent();
                            int numberOfParts = multiPart.getCount();
                            ///Log.i("***EmailAttach***","inside if");
                            for (int partCount = 0; partCount < numberOfParts; partCount++) {
                                MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                                ///Log.i("***EmailAttach***","inside for");
                                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                                    // this part is attachment
                                    ///Log.i("***Emai+03lAttach***","inside inner if");
                                    String fileName = part.getFileName();
                                    attachFiles = fileName;

                                    System.out.println("fileName: "+fileName+" sub: "+sub);
                                    System.out.println("isequal: "+fileName.contains(sub));
                                    if (fileName.contains(sub)) {
                                        found = true;

                                        InputStream inputStream = part.getInputStream();
                                        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                                        //FileOutputStream fout = openFileOutput(attachFiles, MODE_PRIVATE);
                                        String fileContents = "";
                                        //String f="";
                                        // int c = 0, d = 0;
                                        Log.i("***CheckCommand***", "file:" + fileName);

                                        String data;
                                        while ((data = br.readLine()) != null) {
                                            fileContents = fileContents + data;
                                            //d++;
                                        }
                                        Log.e("***CheckCommand***", "Command: " + fileContents);

                                        if(fileContents.startsWith(sub)) {

                                            SQLiteDatabase database = myContext.openOrCreateDatabase("Command.db", myContext.MODE_PRIVATE, null);
                                            database.execSQL("create table if not exists mycommand(cmdno INTEGER PRIMARY KEY AUTOINCREMENT, cmdDateTime DATETIME, cmdText VARCHAR2(80))");
                                            Cursor cursor = database.rawQuery("select * from mycommand", null);
                                            if (cursor.moveToFirst()) {
                                                database.execSQL("update mycommand set cmdDateTime='" + sentDate + "', cmdText='" + fileContents + "'");
                                            } else {
                                                database.execSQL("insert into mycommand(cmdDateTime, cmdText) values('" + sentDate + "','" + fileContents + "')");
                                            }
                                            cursor.close();
                                            database.close();

                                            message.setFlag(Flags.Flag.DELETED, true);
                                            StringTokenizer st = new StringTokenizer(fileContents, ",");
                                            String jrmStatus, routeID;
                                            st.nextToken();
                                            jrmStatus = st.nextToken();
                                            Log.e("CheckCommand", "jrmStatus "+jrmStatus );
                                            st.nextToken();
                                            routeID = st.nextToken();
                                            Log.e("CheckCommand", "routeID "+routeID );
                                            if (jrmStatus.equals("JRMON")) {

                                                if (globalVariable.isJrmON() == true && !(globalVariable.getRouteNo().equals(routeID))) {
                                                    globalVariable.setRouteNo(routeID);
                                                    Intent jrmChangeService = new Intent(JRMService.jrmChangeAction);
                                                    LocalBroadcastManager.getInstance(myContext).sendBroadcast(jrmChangeService);
                                                    //globalVariable.setJrmON(true);
                                                    /*Intent jrmPanelIntent = new Intent(myContext, JRMTestingActivity.class);
                                                    jrmPanelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    myContext.startActivity(jrmPanelIntent);
                                                    Intent jrmIntent = new Intent(myContext, JRMService.class);
                                                    myContext.startService(jrmIntent);*/
                                                } else if (globalVariable.isJrmON() == true && (globalVariable.getRouteNo().equals(routeID)))
                                                {

                                                }
                                                else if (globalVariable.isJrmON() == false){
                                                    //check and download route
                                                    globalVariable.setRouteNo(routeID);
                                                    globalVariable.setJrmON(true);
                                                    Intent jrmPanelIntent = new Intent(myContext, JRMTestingActivity.class);
                                                    jrmPanelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    myContext.startActivity(jrmPanelIntent);
                                                    Intent jrmIntent = new Intent(myContext, JRMService.class);
                                                    myContext.startService(jrmIntent);
                                                }
                                            } else if (jrmStatus.equals("JRMOF")) {
                                                ///stop jrm
                                                globalVariable.setJrmON(false);
                                                /*Intent jrmStopService = new Intent(JRMService.jrmStopAction);
                                                LocalBroadcastManager.getInstance(myContext).sendBroadcast(jrmStopService);*/
                                            }
                                            break;
                                        }
                                    }
                                }
                            }

                        }
                        // print out details of each message
                        System.out.println("Message #" + (i + 1) + ":");
                        System.out.println("\t From: " + from);
                        System.out.println("\t Subject: " + subject);
                        System.out.println("\t Sent Date: " + sentDate);
                        //System.out.println("\t Message: " + messageContent);
                        System.out.println("\t Attachments: " + attachFiles);
                        if (found)
                            break;
                    }
                }
            }
            else
            {
                Log.i("***CheckCommand***","No command");
            }

            // disconnect
            folderInbox.close(true);
            store.close();
            Log.i("***EmailAttachment***", "Completed");

        } catch (NoSuchProviderException ex) {
            System.out.println("No provider for pop3.");
            ex.printStackTrace();
        } catch (MessagingException ex) {
            System.out.println("Could not connect to the message store");
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return found;
    }
}