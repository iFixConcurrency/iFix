package fix.iDebugger.util;

public class UpdateLine {
	private String packageName;
	private String className;
	private int updateNumber = 0;

	public UpdateLine() {
		super();
	}

	public UpdateLine(String packageName, String className) {
		super();
		this.packageName = packageName;
		this.className = className;
	}

	// Insert a statement,update line number;
	public void update() {
		updateNumber++;
	}

	public boolean isSameFile(String packageName, String className) {
		if (this.packageName.equals(packageName) && this.className.equals(className)) {
			return true;
		} else {
			return false;
		}
	}

	public int getUpdtaeNumber() {
		return updateNumber;
	}

	@Override
	public boolean equals(Object obj) {
		UpdateLine upLine = (UpdateLine) obj;
		return (className.equals(upLine.getClassName()) && packageName.equals(upLine.getPackageName()));
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

}
