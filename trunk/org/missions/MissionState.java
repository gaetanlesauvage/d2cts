package org.missions;

public enum MissionState {
	STATE_CURRENT(0),
	STATE_ACHIEVED(-1),
	STATE_TODO(1);
	
	private int code;
	
	private MissionState(int code){
		this.code = code;
	}
	
	public int getCode(){
		return this.code;
	}

	public static MissionState get(int code) {
		for(MissionState state : values())
			if(state.getCode() == code)
				return state;
		return null;
	}

}
