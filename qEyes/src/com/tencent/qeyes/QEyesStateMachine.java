package com.tencent.qeyes;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.net.Uri;
import android.os.Message;

/**
 * Qeyes状态机和辅助函数
 */

public class QEyesStateMachine implements MsgType {
	
	enum State{
		STATE_INIT,					//初始状态
		STATE_PHOTO_ACQUIRED,		//拍好照片
		STATE_UPLOAD_FAILURE,		//上传失败
		STATE_WAITING_PHASE_ONE, 	//30s等待抢单
		STATE_WAITING_PHASE_TWO, 	//60s等待回答
		STATE_NO_RESPONSE, 			//无人响应
		STATE_SPEAKING_RESULTS,		//朗读结果
		STATE_EVALUATE_PHASE_ONE,	//评价步骤1
		STATE_EVALUATE_PHASE_TWO	//评价步骤2
	}
	
	public State curState;
	public TextSpeaker textSpeaker;
	public QEyesHttpConnection qHttp;
	public AudioSpeaker audioSpeaker;
	public String response;
	public boolean isAudio;
	public QEyes.MainHandler handler;
	
	QEyesStateMachine(QEyesHttpConnection qHttp, QEyes.MainHandler handler) {
		curState = null;
		textSpeaker = null;
		audioSpeaker = null;
		response = null;
		isAudio = false;
		this.qHttp = qHttp;
		this.handler = handler;
	}
	
	public void setSpeaker(Context context) {
		textSpeaker = new TextSpeaker(context);
		audioSpeaker = new AudioSpeaker(context);
	}
	
	public boolean setState(State s) {
		curState = s;		
		enterState(curState);
		return true;
	}

	private boolean enterState(State s) {
		switch (s) {
			case STATE_INIT : {
				speak("请按音量键拍照!");				
				break;
			}
			case STATE_PHOTO_ACQUIRED : {
				if (isSingleColor()) {
					speak("识别为单色,请按音量加,重新拍摄,或按音量减上传!");
				} else {
					speak("请按音量加,重新拍摄,或按音量减上传!");
				}
				break;
			}
			case STATE_UPLOAD_FAILURE : {
				speak("上传失败,请按音量加,重新上传,或按音量减放弃!");
				break;
			}
			case STATE_WAITING_PHASE_ONE : {
				speak("已上传,抢单中,请耐心等待三十秒!");
				final Timer t1 = new Timer();
				t1.schedule(new TimerTask() {					
					Message msg = new Message();					
					@Override
					public void run() {	
						QEyesHttpResults result = qHttp.httpCheckAns();											
						if (result.ret == 3) {
							// 已被抢
							msg.what = MSG_QUESTION_DISPATCHED;
							handler.sendMessage(msg);
							t1.cancel();
						} else if (result.ret == 1) {
							// 已回答
							if (result.type == 1) {
								isAudio = true;
							} else {
								isAudio = false;
							}
							response = result.content;							
							msg.what = MSG_QUESTION_ANSWERED;
							handler.sendMessage(msg);
							t1.cancel();
						} 										
					}
				}, 2000, 2000);
				t1.schedule(new TimerTask() {					
					Message msg = new Message();					
					@Override
					public void run() {
						msg.what = MSG_SVR_TIMEOUT;
						handler.sendMessage(msg);
						t1.cancel();						
					}
				}, 30000);
				break;
			}		
			case STATE_WAITING_PHASE_TWO : {
				speak("已被抢单，志愿者正在回答，请耐心等待六十秒!");
				final Timer t1 = new Timer();
				t1.schedule(new TimerTask() {					
					Message msg = new Message();					
					@Override
					public void run() {	
						QEyesHttpResults result = qHttp.httpCheckAns(); 
						if (result.ret == 1) {
							if (result.type == 1) {
								isAudio = true;
							} else {
								isAudio = false;
							}
							response = result.content;							
							msg.what = MSG_QUESTION_ANSWERED;
							handler.sendMessage(msg);
							t1.cancel();
						} 										
					}
				}, 5000, 5000);
				t1.schedule(new TimerTask() {					
					Message msg = new Message();					
					@Override
					public void run() {
						msg.what = MSG_SVR_TIMEOUT;
						handler.sendMessage(msg);
						t1.cancel();						
					}
				}, 60000);
				break;
			}
			case STATE_NO_RESPONSE : {
				speak("暂时无人响应，请按音量加，重新上传，或按音量减放弃！");
				break;
			}	
			case STATE_SPEAKING_RESULTS : {
				speakBlocked("收到志愿者回复!");
				if (isAudio) {
					audioSpeaker.play(Uri.parse(response));
				} else {
					speakBlocked(response);
				}
				setState(State.STATE_EVALUATE_PHASE_ONE);
				break;
			}
			case STATE_EVALUATE_PHASE_ONE : {
				speak("请对本次回复做出评价，音量加为满意，音量减为不满意！");
				break;
			}
			case STATE_EVALUATE_PHASE_TWO : {
				speak("是否举报恶意回复，音量加为恶意，音量减为非恶意！");
				break;
			}
			default:
				break;
		}
		return true;
	}	
	private void speak(String text) {
		textSpeaker.speak(text);
	}
	private void speakBlocked(String text) {
		textSpeaker.speakBlocked(text);
	}
	private boolean isSingleColor() {
		return false;
	}
}
