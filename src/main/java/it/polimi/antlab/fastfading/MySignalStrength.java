package it.polimi.antlab.fastfading;

public class MySignalStrength {
	int dbm;
	String type;

	public MySignalStrength() {}

	public void setDbm(int dbm) {
		this.dbm = dbm;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getDbm() {
		return dbm;
	}

	public String getType() {
		return type;
	}


}