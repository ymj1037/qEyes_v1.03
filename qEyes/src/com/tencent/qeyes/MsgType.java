package com.tencent.qeyes;

public interface MsgType {
	int TTS_INITIAL_SUCCESS = 0x0001;
	int TTS_INITIAL_FAIL = 0x0002;
	int MSG_QUESTION_DISPATCHED = 0x0003;
	int MSG_QUESTION_ANSWERED = 0x0004;
	int MSG_SVR_TIMEOUT = 0x0005;
}
