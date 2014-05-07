package com.tencent.qeyes;

import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

/**
 * 用于朗读文字的类
 */

public class TextSpeaker {
	
	private TextToSpeech tts;  
	
	public TextSpeaker(final Context context) {
		tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {            
			@Override            
			public void onInit(int status) {                      
				if (status == TextToSpeech.SUCCESS)                
				{                    
					int result = tts.setLanguage(Locale.CHINA); 
					if (result == TextToSpeech.LANG_MISSING_DATA                            
							|| result == TextToSpeech.LANG_NOT_SUPPORTED)                    
					{                        
						Toast.makeText(context, "Language is not available.",                                
								Toast.LENGTH_SHORT).show();                    
					}                
				}            
			}        
		});    
	}         
	public void speak(String text) {       
		// 朗读文字
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);  
		Log.v("-Activity-", text);	
	}
	public void speakBlocked(String text) {		
		// 阻塞朗读文字
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		Log.v("-Activity-", text);	
		while (tts.isSpeaking()) {			
		}
	}
}

