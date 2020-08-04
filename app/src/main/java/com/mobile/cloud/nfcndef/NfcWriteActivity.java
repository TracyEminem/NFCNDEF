package com.mobile.cloud.nfcndef;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

public class NfcWriteActivity extends AppCompatActivity {
    private static final String TAG = "NfcWriteActivity";
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private Button submit;
    private AppCompatEditText editText;
    private String content;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_write);
        submit = findViewById(R.id.submit);
        editText = findViewById(R.id.et_input);
        if (mNfcAdapter == null) {
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        }
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, NfcWriteActivity.class), 0);
//        doSomethingWithIntent(getIntent());

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(editText.getText().toString())){
                    Toast.makeText(NfcWriteActivity.this, "empty data", Toast.LENGTH_LONG).show();
                    return;
                }else {
                    content = editText.getText().toString();
                    Toast.makeText(NfcWriteActivity.this, "靠近NFC标签，开始写入", Toast.LENGTH_LONG).show();
                    doSomethingWithIntent(getIntent(),editText.getText().toString());
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(!TextUtils.isEmpty(content)) {
            doSomethingWithIntent(intent, content);
        }
    }

    private void doSomethingWithIntent(Intent intent,String content) {
        final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) {
            return;
        }
        writeNFC(tag,content);
    }


        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private void writeNFC(Tag tag,String content) {
        // 这里是将数据写入NFC卡中
        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{NdefRecord.createExternal("vndcn.com", "nfc", content.getBytes()),NdefRecord.createApplicationRecord("com.vendingontrack.vendcoin")});
//        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{NdefRecord.createExternal("vndcn.com", "nfc", "123321".getBytes()),NdefRecord.createApplicationRecord("com.mobile.cloud.nfctest")});
        int size = ndefMessage.toByteArray().length;
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    return;
                }
                if (ndef.getMaxSize() < size) {
                    return;
                }
                try {
                    ndef.writeNdefMessage(ndefMessage);
                    Toast.makeText(this, "写入成功", Toast.LENGTH_LONG).show();
                } catch (FormatException e) {
                    e.printStackTrace();
                }
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                format.connect();
                format.format(ndefMessage);
                if (format.isConnected()) {
                    format.close();
                }
                Toast.makeText(this, "写入成功", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "写入失败", Toast.LENGTH_LONG).show();
        }
    }
}