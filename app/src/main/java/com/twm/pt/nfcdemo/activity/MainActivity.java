package com.twm.pt.nfcdemo.activity;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.twm.pt.nfcdemo.R;
import com.twm.pt.nfcdemo.utility.L;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    NfcAdapter gNfcAdapter;

    /**
     * 識別目前是否在Resume狀態。
     */
    private boolean gResumed = false;
    /**
     * 識別目前是否在可寫入的模式。
     */
    private boolean gWriteMode = false;
    /**
     * 標記為Log查詢時專用的標籤。
     */
//    private static final String TAG = "nfcproject";

    PendingIntent gNfcPendingIntent;
    IntentFilter[] gNdefExchangeFilters;
    IntentFilter [] gWriteTagFilters;

    EditText gNote;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        L.d("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 取得該設備預設的無線感應裝置
        NfcManager manager = (NfcManager) getSystemService(Context.NFC_SERVICE);
        NfcAdapter nfcManagerNfcAdapter = manager.getDefaultAdapter();
        gNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcManagerNfcAdapter==gNfcAdapter){
            L.d(TAG , "true");
        }else{
            L.d(TAG , "false");
        }
        if (gNfcAdapter == null) {
            Toast.makeText(this, "不支援NFC感應功能！", Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }

        // 取得EditText與Button，並且註冊對應的事件
        findViewById(R.id.write_tag).setOnClickListener(this.gTagWriter);
        gNote = (EditText)findViewById(R.id.note);
        gNote.addTextChangedListener(gTextWatcher);

        // 註冊讓該Activity負責處理所有接收到的NFC Intents。
        gNfcPendingIntent = PendingIntent.getActivity(this, 0,
                // 指定該Activity為應用程式中的最上層Activity
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // 建立要處理的Intent Filter負責處理來自Tag或p2p交換的資料。
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefDetected.addDataType("text/plain");
        }
        catch (IntentFilter.MalformedMimeTypeException e) {}
        gNdefExchangeFilters = new IntentFilter [] {ndefDetected};
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume()
    {
        L.d("onResume");
        if(getIntent()!=null)L.d(TAG , "getIntent().getAction()="+getIntent().getAction());
        super.onResume();
        gResumed = true;
        // 處理由Android系統送出應用程式處理的intent filter內容

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            // 取得NdefMessage
            NdefMessage [] messages = getNdefMessages(getIntent());
            // 取得實際的內容
            byte [] payload = messages[0].getRecords()[0].getPayload();
            String text = new String(payload);
            L.d(TAG , "payload="+text);
            setNoteBody(text);
            // 往下送出該intent給其他的處理對象
            setIntent(new Intent());
        }
        // 啟動前景模式支持Nfc intent處理
        enableNdefExchangeMode();
    }

    @Override
    protected void onPause()
    {
        L.d("onPause");
        super.onPause();
        gResumed = false;
        // 由於NfcAdapter啟動前景模式將相對花費更多的電力，要記得關閉。
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        L.d("onNewIntent");
        if(getIntent()!=null)L.d(TAG , "intent.getAction()="+intent.getAction());
        setIntent(intent);
        if(getIntent()!=null)L.d(TAG , "getIntent().getAction()="+getIntent().getAction());
        // 覆寫該Intent用於補捉如果有新的Intent進入時，可以觸發的事件任務。
        // NDEF exchange mode
        if (!gWriteMode && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            NdefMessage [] msgs = getNdefMessages(intent);
            promptForContent(msgs[0]);
            L.d();
        }

        // 監測到有指定ACTION進入，代表要寫入資料至Tag中。
        // Tag writing mode
        if (gWriteMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            L.d(TAG, "detectedTag len=" + detectedTag.getTechList().length);
//            writeTag(getNoteAsNdef(), detectedTag);
            readTag(detectedTag);
            L.d();
        }
    }



    /**
     * 取得Intent中放入的NdefMessage。
     * @param intent
     * @return
     */
    NdefMessage [] getNdefMessages(Intent intent)
    {
        // Parse the intent
        NdefMessage [] msgs = null;
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable [] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                msgs = new NdefMessage [rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage)rawMsgs[i];
                }
            }
            else {
                // Unknown tag type
                byte [] empty = new byte [] {};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord [] {record});
                msgs = new NdefMessage [] {msg};
            }
        }
        else {
            Log.d(TAG, "Unknown intent.");
            finish();
        }
        return msgs;
    }

    private void setNoteBody(String body)
    {
        Editable text = gNote.getText();
        text.clear();
        text.append(body);
    }

    /**
     * 將輸入的內容轉成NdefMessage。
     * @return
     */
    private NdefMessage getNoteAsNdef()
    {
        byte [] textBytes = gNote.getText().toString().getBytes();
        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(), new byte [] {}, textBytes);
        return new NdefMessage(new NdefRecord [] {textRecord});
    }

    /**
     * 啟動Ndef交換資料模式。
     */
    private void enableNdefExchangeMode()
    {
        L.d("enableNdefExchangeMode");
        // 讓NfcAdatper啟動前景Push資料至Tag或應用程式。
//        gNfcAdapter.enableForegroundNdefPush(MainActivity.this, getNoteAsNdef());
        gNfcAdapter.setNdefPushMessage(getNoteAsNdef(), this);



        // 讓NfcAdapter啟動能夠在前景模式下進行intent filter的dispatch。
        gNfcAdapter.enableForegroundDispatch(this, gNfcPendingIntent, gNdefExchangeFilters, null);
        NfcAdapter.CreateNdefMessageCallback callback = new NfcAdapter.CreateNdefMessageCallback(){
            @Override
            public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
                L.d(TAG, "nfcEvent=" + nfcEvent.nfcAdapter.isNdefPushEnabled());
                return getNoteAsNdef();
            }
        };
        gNfcAdapter.setNdefPushMessageCallback(callback,this);



        gNfcAdapter.setOnNdefPushCompleteCallback(new NfcAdapter.OnNdefPushCompleteCallback() {
            @Override
            public void onNdefPushComplete(NfcEvent nfcEvent) {
                L.d(TAG, "onNdefPushComplete=" + nfcEvent.nfcAdapter.isNdefPushEnabled());
            }
        }, this);
    }

    private void disableNdefExchangeMode()
    {
        L.d("disableNdefExchangeMode");
        gNfcAdapter.disableForegroundNdefPush(this);
        gNfcAdapter.disableForegroundDispatch(this);
    }

    /**
     * 啟動Tag寫入模式，註冊對應的Intent Filter來前景模式監聽是否有Tag進入的訊息。
     */
    private void enableTagWriteMode()
    {
        L.d("enableTagWriteMode");
        gWriteMode = true;
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        gWriteTagFilters = new IntentFilter [] {tagDetected};
        gNfcAdapter.enableForegroundDispatch(this, gNfcPendingIntent, gWriteTagFilters, null);
    }

    /**
     * 停止Tag寫入模式，取消前景模式的監測。
     */
    private void disableTagWriteMode()
    {
        L.d("disableTagWriteMode");
        gWriteMode = false;
        gNfcAdapter.disableForegroundDispatch(this);
    }

    /**
     * 應用程式補捉到Ndef Message，詢問用戶是否要取代目前畫面中的文件。
     *
     * @param msg
     */
    private void promptForContent(final NdefMessage msg)
    {
        L.d("promptForContent");
        new AlertDialog.Builder(this).setTitle("是否取代現在的內容?").setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1)
            {
                String body = new String(msg.getRecords()[0].getPayload());
                setNoteBody(body);
            }
        }).setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1)
            {

            }
        }).show();
    }

    private View.OnClickListener gTagWriter = new View.OnClickListener() {

        @Override
        public void onClick(View v)
        {
            // 先停止接收任何的Intent，準備寫入資料至tag；
            disableNdefExchangeMode();
            // 啟動寫入Tag模式，監測是否有Tag進入
            enableTagWriteMode();
            // 顯示對話框，告知將Tag或手機靠近本機的NFC感應區
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Touch tag to write")
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog)
                        {
                            // 在取消模式下，先關閉監偵有Tag準備寫入的模式，再啟動等待資料交換的模式。
                            // 停止寫入Tag模式，代表已有Tag進入
                            disableTagWriteMode();
                            // 啟動資料交換
                            enableNdefExchangeMode();
                        }
                    }).create().show();

        }
    };

    private TextWatcher gTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

        }

        @Override
        public void afterTextChanged(Editable arg0) {
            // 如果是在Resume的狀態下，當編輯完後，啟動前景發佈訊息的功能。
            if (gResumed) {
                L.d("afterTextChanged");
//                gNfcAdapter.enableForegroundNdefPush(MainActivity.this, getNoteAsNdef());
                gNfcAdapter.setNdefPushMessage(getNoteAsNdef(),MainActivity.this );

            }
        }
    };

    boolean writeTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;

        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();

                if (!ndef.isWritable()) {
                    toast("Tag is read-only.");
                    return false;
                }
                if (ndef.getMaxSize() < size) {
                    toast("Tag capacity is " + ndef.getMaxSize() + " bytes, message is " + size
                            + " bytes.");
                    return false;
                }

                ndef.writeNdefMessage(message);
                toast("Wrote message to pre-formatted tag.");
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        toast("Formatted tag and wrote message");
                        return true;
                    } catch (IOException e) {
                        toast("Failed to format tag.");
                        return false;
                    }
                } else {
                    toast("Tag doesn't support NDEF.");
                    return false;
                }
            }
        } catch (Exception e) {
            toast("Failed to write tag");
        }

        return false;
    }

    private boolean readTag(Tag detectedTag) {
        L.d(TAG, "readTag");
        try {
            String[] detectedTagTechList = detectedTag.getTechList();
            for(String tagTech : detectedTagTechList ){
                L.d(TAG , "tagTech:"+tagTech);
            }
            Ndef ndef = Ndef.get(detectedTag);
            if (ndef != null) {
                ndef.connect();

                L.d(TAG, "isWritable=" + ndef.isWritable());
                L.d(TAG, "getMaxSize=" + ndef.getMaxSize());
                L.d(TAG, "getNdefMessage=" + ndef.getNdefMessage());
                L.d(TAG, "getCachedNdefMessage=" + ndef.getCachedNdefMessage());
                return true;
            } else if(MifareClassic.get(detectedTag)!=null){
                MifareClassic mTag = MifareClassic.get(detectedTag);
                mTag.connect();

                L.d(TAG, "getMaxTransceiveLength=" + mTag.getMaxTransceiveLength());
                L.d(TAG, "getSectorCount=" + mTag.getSectorCount());
                L.d(TAG, "getSize=" + mTag.getSize());
                L.d(TAG, "getBlockCount=" + mTag.getBlockCount());
                L.d(TAG, "getType=" + mTag.getType());
                L.d(TAG, "readBlock=" + bytesToHex(mTag.readBlock(0)));
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(detectedTag);
                if (format != null) {
                    try {
                        L.d(TAG, "isConnect=" + format.isConnected());
                        format.connect();
                        return true;
                    } catch (IOException e) {
                        L.e(TAG, "Failed to format tag.");
                        return false;
                    }
                } else {
                    L.e(TAG, "Tag doesn't support NDEF.");
                    return false;
                }
            }
        } catch (Exception e) {
            L.e(TAG, "Failed to write tag msg=" + e.getMessage());
        }
        return false;
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }


}
