package com.mobile.cloud.nfcndef;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class NfcReadActivity extends AppCompatActivity {
    TextView nfcContentTv;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private String content;
    private TextView mContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_read);
        nfcContentTv = findViewById(R.id.tv_nfc_read_content);
        mContent = findViewById(R.id.content);
        if (nfcAdapter == null) {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        }
        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, NfcReadActivity.class), 0);
        disposeIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        disposeIntent(intent);
    }

    private void disposeIntent(Intent intent){
        String cardId = getCardId(intent);
        if (cardId != null) {
            nfcContentTv.setText(String.format("NFC ID:%s", cardId));
        } else {
            Toast.makeText(this, "未读取到卡ID", Toast.LENGTH_SHORT).show();
        }
    }

    private void readNfcTag(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage msgs[] = null;
            int contentSize = 0;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                    contentSize += msgs[i].toByteArray().length;
                }
            }
            try {
                if (msgs != null) {
                    NdefRecord record = msgs[0].getRecords()[0];
//                    String textRecord = parseTextRecord(record);
                    byte[] payload = record.getPayload();
                    String res = new String(payload);
                    mContent.setText("content url is: "+res);
                }
            } catch (Exception e) {
            }
        }
    }


    private String getCardId(Intent intent) {
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        byte[] bytesId = tagFromIntent.getId();
        readNfcTag(intent);
        Ndef ndef = Ndef.get(tagFromIntent);
        return byteArrayToHexString(bytesId);
    }

    private static String byteArrayToHexString(byte[] bytesId) {
        int i, j, in;
        String[] hex = {
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String output = "";
        for (j = 0; j < bytesId.length; ++j) {
            in = bytesId[j] & 0xff;
            i = (in >> 4) & 0x0f;
            output += hex[i];
            i = in & 0x0f;
            output += hex[i];
        }
        return output;
    }
}