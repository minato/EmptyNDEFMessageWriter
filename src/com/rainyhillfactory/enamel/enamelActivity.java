package com.rainyhillfactory.enamel;

import java.io.IOException;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class enamelActivity extends Activity {
    private TextView mText;
    private MifareTagTester  tagobj = new MifareTagTester();
    
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    
    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        setContentView(R.layout.main);
        mText = (TextView) findViewById(R.id.text);
        mText.setText("Touch a Mifare Ultralight tag.");

        mAdapter = NfcAdapter.getDefaultAdapter(this);

        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter nfcFilter = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);

        mFilters = new IntentFilter[] {
        		nfcFilter,
        };

        mTechLists = new String[][] { new String[] { NfcF.class.getName() } };
    }

    
    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,
                mTechLists);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) mAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
    	String tostmsg ;
        //String action = intent.getAction();
            
        Tag tag = (Tag)intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String[] tagTechList = tag.getTechList();
        
        //種類毎に処理を変えたい
        if(tagTechList[0].equalsIgnoreCase("android.nfc.tech.MifareUltralight")){
        	// MifareUltralight向けの実装
        	// 初期化処理

        	ProgressDialog writeDialog = new ProgressDialog(this);
        	writeDialog.setMessage("Please wait...");
        	writeDialog.setIndeterminate(true);
        	writeDialog.setCancelable(true);
        	writeDialog.show();
        	
        	if(tagobj.writeTagEmptyNDEFMsg(tag)){
        		//処理成功
            	tostmsg = getString(R.string.write_OK_msg);            		

        	}else{
        		//処理でエラー
            	tostmsg = getString(R.string.write_NG_msg);
        	}
        	writeDialog.dismiss();
        	

        	 
        }else{
        	// 非対応タグ
        	tostmsg = getString(R.string.unsupported_tag_msg);
	  	    
        }
        /*
        else if(tagTechList[0].equalsIgnoreCase("android.nfc.tech.ç")){
        	//MifareTagTester向けの実装はここにする
        	
        }
        */
		Toast toast = Toast.makeText(getApplicationContext(), tostmsg.toString(), Toast.LENGTH_SHORT);
		toast.show();

    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait while writing...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        return dialog;
    }
    
    public class MifareTagTester {

    	String TAG = MifareTagTester.class.getSimpleName();
    	
        //NDEFフォーマットする
    	public boolean writeTagEmptyNDEFMsg(Tag tag) {
    		boolean ret = true;
    		MifareUltralight ultralight = MifareUltralight.get(tag);
    		try {
    			ultralight.connect();
    			//初期化用のデータ。
    			byte[][] writedata = {{0x03,0x03,(byte) 0xd0,0x00},
    								  {0x00,0x00,0x00,0x00},
    								  {(byte) 0xef,0x00,0x00,0x00}};
    			
    			//4ページ目からNDEF初期化データを書き込む。
    			for(int pagecnt = 0;pagecnt < 3;pagecnt++ ){
    				ultralight.writePage(pagecnt + 4, writedata[pagecnt]);
    			}
 
    		} catch (IOException e) {
    			Log.e(TAG, "IOException while closing MifareUltralight...", e);
    			ret = false;
    		} finally {
    			try {
    				ultralight.close();
    			} catch (IOException e) {
    				Log.e(TAG, "IOException while closing MifareUltralight...", e);
    				ret = false;
    			}
    		}
    		
    		return ret;
    	}

    }
}